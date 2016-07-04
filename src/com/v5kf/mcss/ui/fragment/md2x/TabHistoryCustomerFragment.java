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
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chyrain.irecyclerview.RefreshRecyclerView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.TicketRequest;
import com.v5kf.mcss.ui.activity.MainTabActivity;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.ui.adapter.HistoryVisitorAdapter;
import com.v5kf.mcss.ui.view.V5RefreshLayout;
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
public class TabHistoryCustomerFragment extends TabBaseFragment implements OnRefreshListener {
	
	private static final String TAG = "TabHistoryCustomerFragment";
	private List<CustomerBean> mRecycleBeans; // c_id替代v_id
	
	private RefreshRecyclerView mRefreshRecyclerView;
	private HistoryVisitorAdapter mRecyclerAdapter;
	private LinearLayoutManager mLayoutManager;
	
	private TextView mEmptyTipsTv;
	
	private boolean isLoadingMore = false;
	
	private boolean mHasMore = true;
//	private int mCurYear, mCurMonth, mCurDay;
//	private long mCurUTC;
//	private String mAfterSid; // 最新sid, 大
	private String mBeforeSid; // 最小sid
	
	private static final int NUM_PER_PAGE = 10; // 每次显示10个
	private int mPages = 1;
	
	public TabHistoryCustomerFragment() {
		super();
		// TODO Auto-generated constructor stub
		mRecycleBeans = new ArrayList<>();
	}
	
    public TabHistoryCustomerFragment(MainTabActivity activity, int index) {
		super(activity, index);
		mRecycleBeans = new ArrayList<>();
	}
    

    @Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		setContentView(R.layout.fragment_md2x_visitor_list);

		Logger.d(TAG, TAG + " 将要创建View " + this);
		if (mRefreshRecyclerView == null) {
			initView();
		}
		if (mRecycleBeans.isEmpty()) {
			mParentActivity.showProgress();
			mRefreshRecyclerView.setRefreshing();
			mHandler.sendEmptyMessageDelayed(HDL_TIME_OUT, Config.WS_TIME_OUT);
			initData(false);
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
//		if (mRecycleBeans.isEmpty()) {
//			mParentActivity.showProgress();
//			if (!mRefreshRecyclerView.isRefreshing()) {
//				mRefreshRecyclerView.setRefreshing();
//			}
//			mHandler.sendEmptyMessageDelayed(HDL_TIME_OUT, Config.WS_TIME_OUT);
//			initData(false);
//		}
		
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
		if (mRefreshRecyclerView.isRefreshing()) {
			mRefreshRecyclerView.onRefreshComplete();
		}
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
			getHistoricalCustomer(mBeforeSid, true);
			return;
		}
		
		Map<String, CustomerBean> vMap = mAppInfo.getVisitorMap();
		if (mRecycleBeans.isEmpty() && mHasMore) { // 2015/12/12 修复BUG：加入mHasMore判断
	    	boolean alreadyHasVisitors = false;
	    	for (CustomerBean cstm : vMap.values()) {
	    		if (cstm.getClosingReason() == 0) { // 没有正在进行的会话
	    			alreadyHasVisitors = true;
	    			break;
	    		}
	    	}
	    	
			if (!alreadyHasVisitors) {
				mHasMore = true;
				getHistoricalCustomer("0", true);
				Logger.d(TAG, "initcstmdata [first group customer get]");
				return;
	    	}
		}
		Logger.i(TAG, "initData(...):" + vMap.size() + " mPages:" + mPages + " mHasMore:" + mHasMore);
		
		for (CustomerBean cstm : vMap.values()) {
			if (cstm.getIface() == QAODefine.CSTM_IF_NULL) { // 排除(v_id = 2251356029924705000)错误数据
				continue;
			}
			
        	addRecyclerBean(cstm);
        	// 获取到的客户数量达到最少显示量
//        	if (mAfterSid == null) {
//        		mAfterSid = cstm.getS_id();
//        	} else if (cstm.getS_id() != null && cstm.getS_id().compareTo(mAfterSid) > 0) {
//        		mAfterSid = cstm.getS_id();
//        	}
        	if (mBeforeSid == null || mBeforeSid.equals("0")) {
        		mBeforeSid = cstm.getS_id();
        	} else if (cstm.getS_id() != null && cstm.getS_id().compareTo(mBeforeSid) < 0) {
        		mBeforeSid = cstm.getS_id();
        	}
		}
		
		// 排序按照最新活动时间排序
		if (mRecycleBeans.size() > 1) {
			CustomerTimeCompartor ctc = new CustomerTimeCompartor();
			Collections.sort(mRecycleBeans, ctc);
		}
		for (CustomerBean cstm : mRecycleBeans) {
			Logger.d(TAG, "last_time:" + cstm.getLast_time() + " name:" + cstm.getDefaultName());
		}
		
//		mHandler.sendEmptyMessage(HDL_STOP_REFRESH);
//		mHandler.sendEmptyMessage(HDL_STOP_LOAD);
		mRecyclerAdapter.notifyDataSetChanged();
		mHandler.sendEmptyMessage(HDL_STOP_PROGRESS);
	}
	

	/**
     * 初始化界面Adapter和RecycleView
     */
    private void initView() {
    	mRecyclerAdapter = new HistoryVisitorAdapter(mRecycleBeans, mParentActivity);
    	mRefreshRecyclerView = (RefreshRecyclerView) findViewById(R.id.id_irecycler_visitor);
    	mLayoutManager = new LinearLayoutManager(mParentActivity, LinearLayoutManager.VERTICAL, false);
    	mRefreshRecyclerView.setLayoutManager(mLayoutManager);
    	mRefreshRecyclerView.addItemDecoration(new Divider());
    	mRefreshRecyclerView.setAdapter(mRecyclerAdapter);
    	mRefreshRecyclerView.getRefreshableView().setScrollbarFadingEnabled(true);
    	mRefreshRecyclerView.getRefreshableView().setScrollBarStyle(RecyclerView.SCROLLBAR_POSITION_RIGHT);
    	mRefreshRecyclerView.setHasPullUpFriction(false); // 没有上拉阻力
    	mRefreshRecyclerView.setLoadingMoreWhenLastVisible(true);
    	mRefreshRecyclerView.setHeaderLayout(new V5RefreshLayout(mParentActivity));
    	mRefreshRecyclerView.setFooterLayout(new V5RefreshLayout(mParentActivity, Mode.PULL_FROM_END));
    	mRefreshRecyclerView.setOnRefreshListener(new OnRefreshListener2<RecyclerView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
				//Toast.makeText(mParentActivity, "Pull Down!", Toast.LENGTH_SHORT).show();
				Logger.d(TAG, "[onPullDownToRefresh]");
				onRefresh();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
				//Toast.makeText(mParentActivity, "Pull Up!", Toast.LENGTH_SHORT).show();
				Logger.d(TAG, "[onPullUpToRefresh]");
				isLoadingMore = true;
            	if (mHasMore || mRecyclerAdapter.getItemCount() < mAppInfo.getVisitorMap().size()) {
    				Logger.d(TAG, "initcstmdata [LoadMore]");
            		initData(true);
                	Logger.i(TAG, "上拉加载 ...");
            		mHandler.sendEmptyMessageDelayed(HDL_TIME_OUT, Config.WS_TIME_OUT);
            	} else {
            		mParentActivity.ShowShortToast(R.string.no_more);
            		Logger.i(TAG, "没有更多了");
            		mHandler.sendEmptyMessage(HDL_STOP_PROGRESS);
            	}
			}
		});
		
		if (mRefreshRecyclerView.getEmptyView() != null) {
			mEmptyTipsTv = (TextView) mRefreshRecyclerView.getEmptyView().findViewById(R.id.layout_container_tv);
		}
        /* 空白按钮 */
		if (mEmptyTipsTv != null) {
			mEmptyTipsTv.setText(R.string.visitor_list_empty_tips);
			mEmptyTipsTv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (!mRefreshRecyclerView.isRefreshing()) {
						mRefreshRecyclerView.setRefreshing();
						onRefresh();
					}
				}
			});
		}
    }
    
    
    /**
     * -1表示未找到，其他值表示对于的position
     * @param v_id
     * @return
     */
    private int hasRecyclerBeans(String v_id) {
    	if (null == v_id || v_id.isEmpty()) {
    		Logger.e(TAG, "[hasRecyclerBeans] Null v_id");
    		return -1;
    	}
    	
    	for (int i = 0; i < mRecycleBeans.size(); i++) {
    		if (mRecycleBeans.get(i).getVisitor_id().equals(v_id)) {
    			return i;
    		}
    	}
    	
    	return -1;
    }
    
    private boolean addRecyclerBean(CustomerBean cstm) {
    	if (null == cstm || hasRecyclerBeans(cstm.getVisitor_id()) >= 0) {
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

    private void getHistoricalCustomer(String s_id, boolean before) {
    	try {
	    	TicketRequest tReq = (TicketRequest)RequestManager.getRequest(QAODefine.O_TYPE_WTICKET, mApplication);
	    	tReq.getHistoricalCustomer(s_id, NUM_PER_PAGE, before);
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    }
    
  	
//	private boolean isValidDate(int year, int month, int day) {
//		long date = DateUtil.getDate(year, month, day);
//		if (date > DateUtil.getCurrentLongTime()) {
//			return false;
//		}
//		if (mAppInfo.getUser().getCreate_time() > 0) { // 查询时间不小于账号创建时间
//			date = date/1000 + 24*3600;
//			if (date < mAppInfo.getUser().getCreate_time()) {
//				return false;
//			}
//		}
//		if (year < Config.SYS_YEAR || (year == Config.SYS_YEAR && month < Config.SYS_MONTH)) {
//			return false;
//		}
//		return true;
//	}

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
		if (mRefreshRecyclerView == null) {
			initView();
		}
		switch (msg.what) {
		case HDL_TIME_OUT: // 超时
			mParentActivity.dismissProgress();
			if (mRefreshRecyclerView.isRefreshing()) {
				mRefreshRecyclerView.onRefreshComplete();
				mRecyclerAdapter.notifyDataSetChanged();
				onRefreshTimeOut();
			}
			break;
		case HDL_STOP_REFRESH: // 停止刷新
			mHandler.removeMessages(HDL_TIME_OUT);
			initData(false);
			if (mRefreshRecyclerView.isRefreshing()) {
				mRefreshRecyclerView.onRefreshComplete();
			}
			break;
			
		case HDL_STOP_LOAD: // 停止加载
			mHandler.removeMessages(HDL_TIME_OUT);
			initData(false);
			if (isLoadingMore) {
				isLoadingMore = false;
			}
			if (mRefreshRecyclerView.isRefreshing()) {
				mRefreshRecyclerView.onRefreshComplete();
			}
			break;
			
		case HDL_UPDATE_UI: // 更新ui
			Logger.d(TAG, "initcstmdata [UPDATE_UI]");
			mHandler.removeMessages(HDL_TIME_OUT);
			initData(false);
			if (isLoadingMore) {
				isLoadingMore = false;
			}
			if (mRefreshRecyclerView.isRefreshing()) {
				mRefreshRecyclerView.onRefreshComplete();
			}
			break;
			
		case HDL_STOP_PROGRESS: // 停止progress
			mParentActivity.dismissProgress();
			if (mRefreshRecyclerView.isRefreshing()) {
				mRefreshRecyclerView.onRefreshComplete();
			}
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

	@Override
	public void onRefresh() { // 下拉加载内存中数据，上拉加载则继续请求
		Logger.i(TAG, "onRefresh ...");
//    	mParentActivity.showProgress();
		
		/* 请求等待列表数据 */
//		if (!mAppInfo.getVisitorMap().isEmpty()) { // 已获取数据则只刷新今天数据
//			getHistoricalCustomer(mAfterSid, false);
//			Logger.d(TAG, "initcstmdata [onRefresh - today]");
//		} else {
//	    	mHasMore = true;
//	    	mPages = 0;
//			Logger.d(TAG, "initcstmdata [onRefresh - all]");
//			initData(false);
//		}
		mHasMore = true;
    	mPages = 0;
    	mBeforeSid = null;
		Logger.d(TAG, "initcstmdata [onRefresh - all]");
		initData(true);
		
		Logger.i(TAG, "下拉刷新...");
		mHandler.sendEmptyMessageDelayed(HDL_TIME_OUT, Config.WS_TIME_OUT);
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
//			// [取消]可接入客户优先显示
//			if (lhs.getAccessable().equals(QAODefine.ACCESSABLE_IDLE) && rhs.getAccessable().equals(QAODefine.ACCESSABLE_IDLE)) {
//				return 0;
//			} else if (lhs.getAccessable().equals(QAODefine.ACCESSABLE_IDLE)) {
//				return -1;
//			} else if (rhs.getAccessable().equals(QAODefine.ACCESSABLE_IDLE)) {
//				return 1;
//			}
//			long l = lhs.getVirtual() == null ? 0 : lhs.getVirtual().getActive_time();
//			long r = rhs.getVirtual() == null ? 0 : rhs.getVirtual().getActive_time();
			long l = lhs.getLast_time();
			long r = rhs.getLast_time();
			if (l == 0 || r == 0) {
				//Logger.w(TAG, "CustomerBean sort -> null Active_time virtual:" + lhs.getDefaultName() + lhs.getVirtual() + rhs.getDefaultName() + rhs.getVirtual());
				if (l == 0 && r == 0) {
					return 0;
				} else if (l == 0) {
					return 1;
				} else {
					return -1;
				}
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
	
	/**
	 * 增减历史客户
	 * @param appinfo
	 */
	@Subscriber(tag = EventTag.ETAG_VISITORS_CHANGE, mode = ThreadMode.MAIN)
	private void visitorsChange(Integer numChanged) {
		Logger.d(TAG + "-eventbus", "visitorsChange -> ETAG_VISITORS_CHANGE");
//		if ((mAppInfo.getVisitorMap().size() < (mPages * NUM_PER_PAGE)) && mHasMore) {
//			getHistoricalCustomer(mBeforeSid, true);
//			if (mAppInfo.getVisitorMap().size() > mRecycleBeans.size()) {
//				mHandler.sendEmptyMessageDelayed(HDL_STOP_PROGRESS, 2000);
//			}
//		} else {
//			Logger.d(TAG, "initcstmdata [Receive HISTORICAL_CUSTOMER]");
//			mHandler.sendEmptyMessageDelayed(HDL_STOP_PROGRESS, 2000);
//		}
//		mHandler.sendEmptyMessage(HDL_STOP_REFRESH);
//		mHandler.sendEmptyMessage(HDL_STOP_LOAD);
		if (numChanged < NUM_PER_PAGE) {
			mHasMore = false;
		}
		mHandler.sendEmptyMessage(HDL_UPDATE_UI);
		mHandler.sendEmptyMessageDelayed(HDL_STOP_PROGRESS, 2000);
    }
	
	/**
	 * 单个客户的accessable状态改变
	 * @param customerBean
	 */
	@Subscriber(tag = EventTag.ETAG_VISITORS_CHANGE, mode = ThreadMode.MAIN)
	private void visitorChange(CustomerBean customerBean) {
		int position = mRecycleBeans.indexOf(customerBean);
		Logger.d(TAG + "-eventbus", "visitorsChange(CustomerBean) -> ETAG_VISITORS_CHANGE pos:" + position);
		if (position >= 0) {
			mRecyclerAdapter.notifyItemChanged(position);
		}
		//mHandler.sendEmptyMessage(HDL_UPDATE_UI);
    }

	/**
	 * 加入一个历史客户(列表顶部)
	 * @param customerBean
	 */
	@Subscriber(tag = EventTag.ETAG_VISITORS_IN, mode = ThreadMode.MAIN)
	private void visitorIn(CustomerBean customerBean) {
		Logger.d(TAG + "-eventbus", "visitorIn(CustomerBean) -> ETAG_VISITORS_IN");
		customerBean.setLast_time(DateUtil.getCurrentLongTime()/1000);
		int pos = hasRecyclerBeans(customerBean.getVisitor_id());
		if (pos >= 0) {
			mRecycleBeans.add(0, mRecycleBeans.remove(pos));
			mRecyclerAdapter.notifyItemRangeChanged(0, pos);
		} else {
			mRecycleBeans.add(0, customerBean);
			mRecyclerAdapter.notifyItemInserted(0);
		}
	}
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		if (isConnect) {
			//
		} else {
	    	mHasMore = true;
	    	initData(false);
		}
	}
}
