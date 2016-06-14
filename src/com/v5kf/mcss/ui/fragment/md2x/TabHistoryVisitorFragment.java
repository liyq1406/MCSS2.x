package com.v5kf.mcss.ui.fragment.md2x;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.TicketRequest;
import com.v5kf.mcss.ui.activity.MainTabActivity;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.ui.adapter.HistoryVisitorAdapter;
import com.v5kf.mcss.ui.widget.Divider;
import com.v5kf.mcss.utils.DateUtil;
import com.v5kf.mcss.utils.Logger;

/**
 * @author chenhy
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-2 下午11:24:47
 * @package com.v5kf.mcss.ui.fragment.HistoryVisitorFragment.java
 * @description
 *
 */
public class TabHistoryVisitorFragment extends TabBaseFragment implements OnRefreshListener {
	
	private static final String TAG = "HistoryVisitorFragment";
	private List<CustomerBean> mRecycleBeans; // c_id替代v_id
	
	private RecyclerView mRecyclerView;
	private HistoryVisitorAdapter mRecyclerAdapter;
	private LinearLayoutManager mLayoutManager;
	
	private SwipeRefreshLayout mSwipeRefresh;
	private boolean isLoadingMore = false;
	
	private boolean mHasMore = true;
	private int mCurYear, mCurMonth, mCurDay;
	
	private static final int NUM_PER_PAGE = 10; // 每次显示10个
	private int mPages = 1;
	private int mDayCount = 0;
	
    public TabHistoryVisitorFragment(MainTabActivity activity, int index) {
		super(activity, index);
		mRecycleBeans = new ArrayList<>();
		mCurYear = DateUtil.getYear();
    	mCurMonth = DateUtil.getMonth();
    	mCurDay = DateUtil.getDay();
	}
    

    @Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		setContentView(R.layout.fragment_contact_customer);

		Logger.d(TAG, TAG + " 将要创建View " + this);
		if (mRecyclerView == null) {
			initView();
		}
		if (mRecycleBeans.isEmpty()) {
			mParentActivity.showProgress();
			initData(false);
	    	checkListEmpty();
		}
	}

	@Override
	protected void onResumeLazy() {
		super.onResumeLazy();
		Logger.d(TAG, TAG + "所在的Activity onResume, onResumeLazy " + this);
	}

	@Override
	protected void onFragmentStartLazy() {
		super.onFragmentStartLazy();
		Log.d(TAG, TAG + " 显示 " + this);
//		this.mParentActivity.showToolbar();
//		this.mParentActivity.hideFab();
//		this.mParentActivity.setBarColor(UITools.getColor(R.color.main_color));
//		//
//		this.mParentActivity.setStatusbarColor(UITools.getColor(R.color.main_color));
	}

	@Override
	protected void onFragmentStopLazy() {
		super.onFragmentStopLazy();
		Log.d(TAG, TAG + " 掩藏 " + this);
	}

	@Override
	protected void onPauseLazy() {
		super.onPauseLazy();
		Log.d(TAG, TAG + "所在的Activity onPause, onPauseLazy " + this);
	}

	@Override
	protected void onDestroyViewLazy() {
		super.onDestroyViewLazy();
		Log.d(TAG, TAG + " View将被销毁 " + this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, TAG + " 所在的Activity onDestroy " + this);
	}
    
	
	private void initData(boolean addPage) {
		if (addPage) {
			Logger.w(TAG, "mPages++");
			mPages++;
		}
		
		Map<String, CustomerBean> cstmMap = mAppInfo.getVisitorMap();
		if (cstmMap.isEmpty() && mHasMore) { // 2015/12/12 修复BUG：加入mHasMore判断
			mCurYear = DateUtil.getYear();
	    	mCurMonth = DateUtil.getMonth();
	    	mCurDay = DateUtil.getDay();
	    	mHasMore = true;
			getHistoricalCustomerOfDay(mCurYear, mCurMonth, mCurDay);
			Logger.d(TAG, "initcstmdata [onToday customer get]");
			return;
		}
		Logger.i(TAG, "initData(...):" + cstmMap.size() + " mPages:" + mPages + " mHasMore:" + mHasMore);
		
		for (CustomerBean cstm : cstmMap.values()) {
			if (cstm.getIface() == QAODefine.CSTM_IF_NULL) { // 排除(v_id = 2251356029924705000)错误数据
				continue;
			}
			
        	addRecyclerBean(cstm);
        	// 获取到的客户数量达到最少显示量
        	if (mRecycleBeans.size() >= mPages * NUM_PER_PAGE) {
    			mDayCount = 0;
        		break;
        	}
		}
		
		if (mRecycleBeans.size() < (mPages * NUM_PER_PAGE) && mHasMore) {
//			getLastMonthCustomer();
			getLastDayCustomer();
		} else {
			// 排序按照最新活动时间排序
			if (mRecycleBeans.size() > 1) {
				CustomerTimeCompartor ctc = new CustomerTimeCompartor();
				Collections.sort(mRecycleBeans, ctc);
			}
			
			mHandler.sendEmptyMessage(HDL_STOP_REFRESH);
			mHandler.sendEmptyMessage(HDL_STOP_LOAD);
		}
	}
	

	/**
     * 初始化界面Adapter和RecycleView
     */
    private void initView() {
    	mRecyclerAdapter = new HistoryVisitorAdapter(mRecycleBeans, mParentActivity);
    	mRecyclerView = (RecyclerView) findViewById(R.id.id_recycle_view);
    	mLayoutManager = new LinearLayoutManager(mParentActivity, LinearLayoutManager.VERTICAL, false);
		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.addItemDecoration(new Divider());
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setScrollbarFadingEnabled(true);
        mRecyclerView.setScrollBarStyle(RecyclerView.SCROLLBAR_POSITION_RIGHT);
        
        /* 刷新控件 */
        if (null == mSwipeRefresh) {
        	mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        }
        mSwipeRefresh.setOnRefreshListener(this);
        mSwipeRefresh.setColorSchemeColors(R.color.green, R.color.red,  
        	    R.color.blue, R.color.yellow);
        
        /* 空白按钮 */
        findViewById(R.id.layout_container_tv).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onRefresh();
			}
		});
        
        /* 上拉监听 */
        mRecyclerView.addOnScrollListener(new OnScrollListener() {
        	@Override
        	public void onScrollStateChanged(RecyclerView recyclerView,
        			int newState) {
        		super.onScrollStateChanged(recyclerView, newState);
        		int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
        		if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == mRecyclerAdapter.getItemCount()
        				&& mRecyclerAdapter.getItemCount() >= 10) {
                    if (isLoadingMore) {
                    	// 正在刷新中取消执行
                    } else {
                    	isLoadingMore = true;
                    	if (mHasMore || mRecyclerAdapter.getItemCount() < mAppInfo.getVisitorMap().size()) {
                        	setLoadMoreVisible(true);
                        	mRecycleBeans.clear();
            				Logger.d(TAG, "initcstmdata [LoadMore]");
                    		initData(true);
                    		mRecyclerAdapter.notifyDataSetChanged();
                    		mHandler.sendEmptyMessageDelayed(HDL_STOP_LOAD, 5000);
                    	} else {
                    		isLoadingMore = false;
                    		mHandler.sendEmptyMessage(HDL_STOP_LOAD);
                    		mParentActivity.ShowShortToast(R.string.no_more);
                    	}
                    	Logger.i(TAG, "上拉加载 ...");
                    }
                }
        	}
        	
        	@Override
        	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        		super.onScrolled(recyclerView, dx, dy);        		        		
        	}
		});
    }
    
    
    private void checkListEmpty() {
    	if (null == mRecyclerView) {
    		mRecyclerView = (RecyclerView) getView().findViewById(R.id.id_recycle_view);
    	}
    	if (null == mSwipeRefresh) {
    		mSwipeRefresh = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);
    	}
    	if (mRecycleBeans.size() == 0) {
    		mSwipeRefresh.setVisibility(View.GONE);
    		findViewById(R.id.layout_container_empty).setVisibility(View.VISIBLE);
		} else {
			mSwipeRefresh.setVisibility(View.VISIBLE);
			findViewById(R.id.layout_container_empty).setVisibility(View.GONE);
		}
    }

    
    private boolean hasRecyclerBeans(String v_id) {
    	if (null == v_id || v_id.isEmpty()) {
    		Logger.e(TAG, "[hasRecyclerBeans] Null v_id");
    		return true;
    	}
    	
    	for (CustomerBean bean : mRecycleBeans) {
    		if (bean.getVisitor_id().equals(v_id)) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    private boolean addRecyclerBean(CustomerBean cstm) {
    	if (null == cstm || hasRecyclerBeans(cstm.getVisitor_id())) {
    		Logger.d(TAG, "Already hasRecyclerBeans ====");
    		return false;
    	}
//		SessionRecyclerBean recycleBean = new SessionRecyclerBean();
//		recycleBean.setC_id(cstm.getVisitor_id());
//    	recycleBean.setTitle(cstm.getDefaultName());
//    	recycleBean.setIface(cstm.getIface());
//    	recycleBean.setPic(cstm.getDefaultPhoto());
//    	recycleBean.setInTrust(false);
//    	recycleBean.setAccessable(cstm.getAccessable());
////    	if (cstm.getAccessable() != null) {
////    		Logger.i(TAG, "[addRecycleBean] getAccessable ->" + cstm.getAccessable());
////    	}
//    	if (cstm.getVirtual() != null) {
//    		recycleBean.setDate(cstm.getVirtual().getActive_time());
//    	}
    	
		mRecycleBeans.add(cstm);
		Logger.d(TAG, "[addRecycleBean(" + mRecycleBeans.size() + ")] v_id = " + cstm.getVisitor_id());
		return true;
	}
    

	private void getMonthCustomer() {
		Logger.d(TAG, "[getLastMonthCustomer] Year:" + mCurYear + " Month:" + mCurMonth + " hasMore:" + mHasMore);
		if (!isValidDate(mCurYear, mCurMonth, mCurDay)) {
			mHasMore = false;
			Logger.w(TAG, "[getLastMonthCustomer] mHasMore:" + mHasMore);
			mHandler.sendEmptyMessageDelayed(HDL_UPDATE_UI, 500);
			return;
		}
		try {
			TicketRequest tReq = (TicketRequest) RequestManager.getRequest(QAODefine.O_TYPE_WTICKET, mParentActivity);
			tReq.getHistoricalCustomer(mCurYear, mCurMonth, 0);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 查找前一天的客户
	 * @param getLastDayCustomer HistoryVisitorFragment 
	 * @return void
	 */
	private void getLastDayCustomer() {
		Logger.d(TAG, "[getLastDayCustomer] mCurMonth:" + mCurMonth + " mCurDay:" + mCurDay);
		if (mCurDay == 1) {
			if (mRecycleBeans.size() < NUM_PER_PAGE) { // 跨月查找且数量不足直接查整月
				if (mCurMonth == 1) {
					mCurMonth = 12;
					mCurYear--;
				} else {
					mCurMonth--;
				}
				getMonthCustomer();
				mDayCount = 0;
				return;
			}
			if (mCurMonth == 1) {
				mCurMonth = 12;
				mCurYear--;
			} else {
				mCurMonth--;
			}
			mCurDay = DateUtil.getMonthDays(mCurYear, mCurMonth);
		} else {
			mCurDay--;
		}
		getHistoricalCustomerOfDay(mCurYear, mCurMonth, mCurDay);
		mDayCount++;
	}
	

	private void getHistoricalCustomerOfDay(int mCurYear, int mCurMonth,
			int mCurDay) {
		Logger.d(TAG, "[getHistoricalCustomerOfDay] Year:" + mCurYear + " Month:" + mCurMonth + " Day:" + mCurDay + " hasMore:" + mHasMore);
		if (!isValidDate(mCurYear, mCurMonth, mCurDay)) {
			mHasMore = false;
			mHandler.sendEmptyMessageDelayed(HDL_UPDATE_UI, 500);
			return;
		}
		try {
			TicketRequest tReq = (TicketRequest) RequestManager.getRequest(QAODefine.O_TYPE_WTICKET, mParentActivity);
			tReq.getHistoricalCustomer(mCurYear, mCurMonth, mCurDay);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
    
  	
	private boolean isValidDate(int year, int month, int day) {
		long date = DateUtil.getDate(year, month, day);
		if (date > DateUtil.getCurrentLongTime()) {
			return false;
		}
		if (mAppInfo.getUser().getCreate_time() > 0) { // 查询时间不小于账号创建时间
			date = date/1000 + 24*3600;
			if (date < mAppInfo.getUser().getCreate_time()) {
				return false;
			}
		}
		if (year < Config.SYS_YEAR || (year == Config.SYS_YEAR && month < Config.SYS_MONTH)) {
			return false;
		}
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Logger.d(TAG, "onActivityResult REQ_code:" + requestCode + " resultCOde:" + resultCode);
		if (null == data) {
			Logger.w(TAG, "Activity result with no Intent data");
			return;
		}
	}
	
	
	@Override
	protected void handleMessage(Message msg, ActivityBase baseActivity) {
		if (mRecyclerView == null || mSwipeRefresh == null) {
			initView();
		}
		switch (msg.what) {
		case HDL_STOP_REFRESH: // 停止刷新
			if (mSwipeRefresh.isRefreshing()) {
				mSwipeRefresh.setRefreshing(false);
				updateUI();
			}
			break;
			
		case HDL_STOP_LOAD: // 停止加载
			if (isLoadingMore) {
				isLoadingMore = false;
				setLoadMoreVisible(false);
				updateUI();
			}
			break;
			
		case HDL_UPDATE_UI: // 更新ui
			Logger.d(TAG, "initcstmdata [UPDATE_UI]");
			updateUI();
			break;
			
		case HDL_STOP_PROGRESS: // 停止progress
			mParentActivity.dismissProgress();
			break;
		}
	}
	
	private void updateInfoOfVisitor(String vid) {
		int i = 0;
		for (; i < mRecycleBeans.size(); i++) {
			CustomerBean cstm = mRecycleBeans.get(i);
			if (cstm.getVisitor_id().equals(vid)) {
				//
				break;
			}
		}
		if (i < mRecycleBeans.size()) {
			mRecyclerAdapter.notifyItemChanged(i);
		}
	}

	private void updateUI() {
		mRecycleBeans.clear();				
		initData(false);
		mRecyclerAdapter.notifyDataSetChanged();
		checkListEmpty();
	}

	@Override
	public void onRefresh() { // 下拉加载内存中数据，上拉加载则继续请求
		Logger.i(TAG, "onRefresh ...");
    	mParentActivity.showProgress();
		
		/* 请求等待列表数据 */
		if (!mAppInfo.getVisitorMap().isEmpty()) { // 已获取数据则只刷新今天数据
			getHistoricalCustomerOfDay(DateUtil.getYear(), DateUtil.getMonth(), DateUtil.getDay());
			Logger.d(TAG, "initcstmdata [onRefresh - today]");
		} else {
			mCurYear = DateUtil.getYear();
	    	mCurMonth = DateUtil.getMonth();
	    	mCurDay = DateUtil.getDay();
	    	mHasMore = true;
			Logger.d(TAG, "initcstmdata [onRefresh - all]");
			updateUI();
		}
		
		mHandler.sendEmptyMessageDelayed(HDL_STOP_REFRESH, 3000);
	}

	
	private void setLoadMoreVisible(boolean visible) {
		if (visible) {
			findViewById(R.id.load_more_layout).setVisibility(View.VISIBLE);
			mRecyclerView.scrollToPosition(mRecyclerAdapter.getItemCount() - 1);
		} else {
			findViewById(R.id.load_more_layout).setVisibility(View.GONE);
//			mRecyclerView.scrollToPosition(mRecyclerAdapter.getItemCount() - 1);
		}
	}
	
	/**
	 * 客户活动时间比较器
	 * @author Chenhy	
	 * @email chenhy@v5kf.com
	 * @version v1.0 2015-10-16 上午10:56:15
	 * @package com.v5kf.mcss.ui.fragment of MCSS-Native
	 * @file HistoryVisitorFragment.java 
	 *
	 */
	static class CustomerTimeCompartor implements Comparator<CustomerBean> {

		@Override
		public int compare(CustomerBean lhs, CustomerBean rhs) {
			long l = lhs.getVirtual() == null ? 0 : lhs.getVirtual().getActive_time();
			long r = rhs.getVirtual() == null ? 0 : rhs.getVirtual().getActive_time();
			if (l == 0 || r == 0) {
				//Logger.w(TAG, "CustomerBean sort -> null Active_time virtual:" + lhs.getDefaultName() + lhs.getVirtual() + rhs.getDefaultName() + rhs.getVirtual());
				if (l == 0 && r == 0) {
					return 0;
				} else if (l == 0) {
					return 1;
				} else {
					return -1;
				}
			} else if (l == r) {
				return 0;
			} else {
				if (r > l) {
					return 1;
				} else if (r < l) {
					return -1;
				} else {
					return 0;
				}
				//return l.compareTo(r);
			}
		}
	}
	
	/***** event *****/
	
	@Subscriber(tag = EventTag.ETAG_UPDATE_CSTM_INFO, mode = ThreadMode.MAIN)
	private void updateCustomerInfo(CustomerBean cstm) {
		Logger.d(TAG + "-eventbus", "updateCustomerInfo -> ETAG_UPDATE_CSTM_INFO");
		if (cstm.getVisitor_id() != null) {
			updateInfoOfVisitor(cstm.getVisitor_id());
		}
	}

	@Subscriber(tag = EventTag.ETAG_VISITORS_CHANGE, mode = ThreadMode.MAIN)
	private void visitorsChange(AppInfoKeeper appinfo) {
		Logger.d(TAG + "-eventbus", "visitorsChange -> ETAG_VISITORS_CHANGE");
		if ((mAppInfo.getVisitorMap().size() < (mPages * NUM_PER_PAGE)) && mHasMore) {
			if (mDayCount >= 5) {
				getMonthCustomer();
				mCurDay = 1;
				mDayCount = 0;
			} else {
				getLastDayCustomer();
			}
			if (mAppInfo.getVisitorMap().size() > mRecycleBeans.size()) {
				mHandler.sendEmptyMessageDelayed(HDL_STOP_PROGRESS, 2000);
				updateUI();
			}
		} else {
			mDayCount = 0;
			Logger.d(TAG, "initcstmdata [Receive HISTORICAL_CUSTOMER]");
			mHandler.sendEmptyMessageDelayed(HDL_STOP_PROGRESS, 2000);
			updateUI();
		}
//		mHandler.sendEmptyMessage(HDL_STOP_REFRESH);
//		mHandler.sendEmptyMessage(HDL_STOP_LOAD);
    }
	
	/**
	 * 单个客户的accessable状态改变
	 * @param customerBean
	 */
	@Subscriber(tag = EventTag.ETAG_VISITORS_CHANGE, mode = ThreadMode.MAIN)
	private void visitorsChange(CustomerBean customerBean) {
		Logger.d(TAG + "-eventbus", "visitorsChange -> ETAG_VISITORS_CHANGE");
		mDayCount = 0;
		Logger.d(TAG, "initcstmdata [Receive HISTORICAL_CUSTOMER]");
		updateUI();
    }
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		if (isConnect) {
			//
		} else {
			mCurYear = DateUtil.getYear();
	    	mCurMonth = DateUtil.getMonth();
	    	mCurDay = DateUtil.getDay();
	    	mHasMore = true;
			updateUI();
		}
	}
}
