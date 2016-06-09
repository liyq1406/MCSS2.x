package com.v5kf.mcss.ui.fragment.md2x;

import java.util.ArrayList;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.entity.CustomerBean.CustomerType;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.ui.activity.ActivityBase;
import com.v5kf.mcss.ui.activity.MainTabActivity;
import com.v5kf.mcss.ui.adapter.ServingSessionAdapter;
import com.v5kf.mcss.ui.widget.Divider;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;

/**
 * @author chenhy
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-2 下午11:24:47
 * @package com.v5kf.mcss.ui.fragment.SessionFragment.java
 * @description
 *
 */
public class TabServingSessionFragment extends TabBaseFragment implements OnRefreshListener {
	
	private static final String TAG = "ServingSessionFragment";
	private List<CustomerBean> mRecycleBeans;
	
	private RecyclerView mRecycleView;
	private ServingSessionAdapter mRecycleAdapter;
	
	private SwipeRefreshLayout mSwipeRefresh;

    public TabServingSessionFragment(MainTabActivity activity, int index) {
		super(activity, index);
		mRecycleBeans = new ArrayList<>();
	}

    @Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		setContentView(R.layout.fragment_serving_session);

		Logger.d(TAG, TAG + " 将要创建View " + this);
		initView();
    	initData();
    	checkListEmpty();
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
		this.mParentActivity.hideFab();
		this.mParentActivity.updateToolbar(mIndex);
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
    
	
	private void initData() {
		Map<String, CustomerBean> cstmMap = mAppInfo.getCustomerMap();
		mRecycleBeans.clear();
		for (CustomerBean cstm : cstmMap.values()) {
			if (cstm.getCstmType() == CustomerType.CustomerType_WaitingAlive) {
				continue;
			}
        	addRecycleBean(cstm);
		}
	}
	

	/**
     * 初始化界面Adapter和RecycleView
     */
    private void initView() {
    	/* 初始化列表对象 */
    	if (null == mRecycleAdapter) {
    		mRecycleAdapter = new ServingSessionAdapter(mRecycleBeans, mParentActivity);
    	}
    	if (null == mRecycleView) {
    		mRecycleView = (RecyclerView) findViewById(R.id.id_recycle_view);
    	}
    	
    	LinearLayoutManager layoutManager = new LinearLayoutManager(mParentActivity, LinearLayoutManager.VERTICAL, false);
		mRecycleView.setLayoutManager(layoutManager);
		mRecycleView.addItemDecoration(new Divider());
        mRecycleView.setAdapter(mRecycleAdapter);
        mRecycleView.setScrollbarFadingEnabled(true);
        mRecycleView.setScrollBarStyle(RecyclerView.SCROLLBAR_POSITION_RIGHT);
        
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
    }
    
    
    private void checkListEmpty() {
    	if (null == mRecycleView) {
    		mRecycleView = (RecyclerView) findViewById(R.id.id_recycle_view);
    	}
    	if (mRecycleBeans.size() == 0) {
    		mSwipeRefresh.setVisibility(View.GONE);
			findViewById(R.id.layout_container_empty).setVisibility(View.VISIBLE);
		} else {
			mSwipeRefresh.setVisibility(View.VISIBLE);
			findViewById(R.id.layout_container_empty).setVisibility(View.GONE);
		}
    }
    

	private void resetRecyclerList() {
		mRecycleBeans.clear();
		initData();
		mRecycleAdapter.notifyDataSetChanged();
		checkListEmpty();
	}
    
    private boolean hasRecycleBeans(String c_id) {
    	if (null == c_id || c_id.isEmpty()) {
    		Logger.e(TAG, "[hasRecycleBeans] Null c_id");
    		return false;
    	}
    	for (CustomerBean bean : mRecycleBeans) {
    		if (bean.getC_id().equals(c_id)) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    
    private boolean addRecycleBean(CustomerBean cstm) {
    	if (null == cstm || hasRecycleBeans(cstm.getC_id())) {
    		Logger.w(TAG, "Already hasRecycleBeans ====");
    		return false;
    	}
		mRecycleBeans.add(0, cstm);
		mRecycleAdapter.notifyDataSetChanged();/*Null?*/
//		mRecycleAdapter.notifyItemInserted(0);
		checkListEmpty();
		Logger.i(TAG, "[[[mRecycleBeans.add]]] c_id = " + cstm.getC_id());
		return true;
	}
    
    
    private int updateRecycleBean(String c_id, boolean goTop, boolean isUnread, boolean clearUnread) {
		if (null == c_id || c_id.isEmpty()) {
			return -1;
		}
		
		int position = 0;
		CustomerBean tmpBean = null;
		for (CustomerBean bean : mRecycleBeans) {
        	if (bean.getC_id().equals(c_id)) {
        		tmpBean = bean;
        		if (isUnread && bean.getSession() != null) {
            		bean.getSession().addUnreadMessageNum();
            	}
            	if (clearUnread && bean.getSession() != null) {
            		bean.getSession().clearUnreadMessageNum();
            	}
        		break;
        	}
        	position++;
		}
		if (null != tmpBean && goTop) {
			mRecycleBeans.remove(tmpBean);
			mRecycleBeans.add(0, tmpBean);
			mRecycleAdapter.notifyDataSetChanged();
			position = 0;
		} else {
			if (position >= mRecycleBeans.size()) {
				return -1;
			}
			
			if (position > 0) {
				mRecycleAdapter.notifyItemChanged(position);
			}
		}
		return position;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Logger.d(TAG, "onActivityResult REQ_code:" + requestCode + " resultCOde:" + resultCode);
		if (null == data) {
			Logger.w(TAG, "Activity result with no Intent data");
			return;
		}
		if (requestCode == Config.REQUEST_CODE_SERVING_SESSION_FRAGMENT || 
				requestCode == Config.REQUEST_CODE_WAITING_SESSION_FRAGMENT) {
//			String s_id = data.getStringExtra(Config.EXTRA_KEY_S_ID);
			String c_id = data.getStringExtra(Config.EXTRA_KEY_C_ID);
			if (mRecycleAdapter == null) {
				Logger.e(TAG, "[onActivityResult] mRecycleAdapter is null, activity not create");
				return;
			}
			switch (resultCode) {
			case Config.RESULT_CODE_CHAT_CONTENT_ADD: { // 消息内容增加且未读->已读
				int pos = updateRecycleBean(c_id, true, false, true);
				if (pos >= 0) {
					mRecycleAdapter.notifyItemRangeChanged(0, pos + 1);
				}
				break;
			} 
			
			case Config.RESULT_CODE_CHAT_CONTENT_NOCHANGE: { // 聊天内容未变，仅改变消息未读->已读
				int pos = updateRecycleBean(c_id, false, false, true);
				if (pos >= 0) {
					mRecycleAdapter.notifyItemRangeChanged(0, pos + 1);
				}
			}
			
			case Config.RESULT_CODE_SWITCH_OK: { // 转接成功
				
				break;
			}
			
			case Config.RESULT_CODE_SWITCH_FAIL: { // 转接失败
				
				break;
			}
			}
		}
	}
	
	
	@Override
	protected void handleMessage(Message msg, ActivityBase activityBase) {
		switch (msg.what) {
		case HDL_STOP_REFRESH:
			if (mSwipeRefresh.isRefreshing()) {
				mSwipeRefresh.setRefreshing(false);
			}
			break;
		}
	}
	
	@Override
	public void onRefresh() {
		/* 请求等待列表数据 */
		try {
			CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, mParentActivity);
			cReq.getCustomerList();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		mHandler.sendEmptyMessageDelayed(HDL_STOP_REFRESH, 3000);
	}

	/***** event *****/

	@Subscriber(tag = EventTag.ETAG_SERVING_CSTM_CHANGE, mode = ThreadMode.MAIN)
	private void servingCustomerChange(AppInfoKeeper appinfo) {
		Logger.d(TAG + "-eventbus", "servingCustomerChange -> ETAG_SERVING_CSTM_CHANGE");
		// 更新整个列表
		resetRecyclerList();
		mHandler.sendEmptyMessage(HDL_STOP_REFRESH);
    }
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		resetRecyclerList();
	}
	
	@Subscriber(tag = EventTag.ETAG_CSTM_OUT, mode = ThreadMode.MAIN)
	private void updateCustomerOut(CustomerBean cstm) {
		Logger.d(TAG + "-eventbus", "updateCustomerOut -> ETAG_CSTM_OUT");
		UITools.noticeEndSession(this.getContext(), cstm);
    }
}
