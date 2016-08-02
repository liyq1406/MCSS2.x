package com.v5kf.mcss.ui.fragment.md2x;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import pl.tajchert.sample.DotsTextView;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.entity.WorkerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.service.CoreService;
import com.v5kf.mcss.ui.activity.MainTabActivity;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.ui.adapter.WaitingSessionAdapter;
import com.v5kf.mcss.ui.widget.Divider;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.WorkerSP;
import com.zcw.togglebutton.ToggleButton;

/**
 * @author chenhy
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-2 下午11:24:47
 * @package com.v5kf.mcss.ui.fragment.SessionFragment.java
 * @description
 *
 */
public class TabMonitorFragment extends TabBaseFragment implements OnRefreshListener {
	
	private static final String TAG = "TabMonitorFragment";
	private List<CustomerBean> mRecycleBeans;
	
	private RecyclerView mRecycleView;
	private WaitingSessionAdapter mRecycleAdapter;
	
	// 监控开关
	private ToggleButton mMonitorBtn;
//	private TextView mMonitorTv;
//	private DotsTextView mDotsTv;

	private DotsTextView mEmptyDots;
	private ViewGroup mEmptyLayout;
	private TextView mEmptyTipsTv;

	public TabMonitorFragment() {
		super();
		// TODO Auto-generated constructor stub
		mRecycleBeans = new ArrayList<>();
	}
	
    public TabMonitorFragment(MainTabActivity activity, int index) {
		super(activity, index);
		mRecycleBeans = new ArrayList<>();
	}

    @Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		setContentView(R.layout.fragment_md2x_monitor);

		Logger.d(TAG, TAG + " 将要创建View " + this);
		initView();
    	initData();
    	checkListEmpty();
    	
//		try {
//			WorkerRequest wReq = (WorkerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WWRKR, mParentActivity);
//			wReq.getWorkerMonitor();
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	protected void onResumeLazy() {
		super.onResumeLazy();
		resetRecyclerList();
		Logger.d(TAG, TAG + "所在的Activity onResume, onResumeLazy " + this);
	}

	@Override
	protected void onFragmentStartLazy() {
		super.onFragmentStartLazy();
		Logger.d(TAG, TAG + " 显示 " + this);
		updateMonitorStatus(mAppInfo.getUser().isMonitor());//mApplication.getWorkerSp().readBoolean(WorkerSP.SP_MONITOR_STATUS)
//		this.mParentActivity.showToolbar();
//		this.mParentActivity.hideFab();
//		this.mParentActivity.setBarColor(UITools.getColor(R.color.main_color));
//		//
//		this.mParentActivity.setStatusbarColor(UITools.getColor(R.color.main_color));
	}

	@Override
	protected void onFragmentStopLazy() {
		super.onFragmentStopLazy();
		Logger.d(TAG, TAG + " 掩藏 " + this);
//		mAppInfo.stopMonitor();
	}

	@Override
	protected void onPauseLazy() {
		super.onPauseLazy();
		Logger.d(TAG, TAG + "所在的Activity onPause, onPauseLazy " + this);
	}

	@Override
	protected void onDestroyViewLazy() {
		super.onDestroyViewLazy();
		Logger.d(TAG, TAG + " View将被销毁 " + this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Logger.d(TAG, TAG + " 所在的Activity onDestroy " + this);
	}
    
	
	private void initData() {
		Logger.d(TAG, "[initData]");
		Map<String, CustomerBean> cstmMap = mAppInfo.getMonitorMap();
		mRecycleBeans.clear();
		for (CustomerBean cstm : cstmMap.values()) {
//			if (cstm.getAccessable() != null &&
//					cstm.getAccessable().equals(QAODefine.ACCESSABLE_IDLE)) { // 获取到客户信息
			Logger.d(TAG, "[initData] cstm:" + cstm.getDefaultName());
			if (cstm.getIface() != 0) { 
				addRecycleBean(cstm);
			}
		}
	}
	
	/**
     * 初始化界面Adapter和RecycleView
     */
    private void initView() {
    	/* 初始化列表对象 */
    	if (null == mRecycleAdapter) {
    		mRecycleAdapter = new WaitingSessionAdapter(mRecycleBeans, mParentActivity);
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
        
        /* 空白按钮 */
        findViewById(R.id.layout_container_tv).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//onRefresh();
				Logger.d(TAG, "monitor:" + mAppInfo.getUser().isMonitor());
			}
		});
        
        mMonitorBtn = (ToggleButton) findViewById(R.id.id_monitor_toogle);
//        mMonitorTv = (TextView) findViewById(R.id.id_monitor_tips);
//        mDotsTv = (DotsTextView) findViewById(R.id.id_dots);
        
        mEmptyDots = (DotsTextView) findViewById(R.id.layout_container_dots);
        mEmptyLayout = (ViewGroup) findViewById(R.id.layout_container_empty);
        mEmptyTipsTv = (TextView) findViewById(R.id.layout_container_tv);
        updateMonitorStatus(mAppInfo.getUser().isMonitor());
        
        mMonitorBtn.setOnToggleChanged(new ToggleButton.OnToggleChanged(){
            @Override
            public void onToggle(ToggleButton btn, boolean on) {
            	Logger.i(TAG, "[onToggle]:" + on);
            	if (!on) { // 停止监控
            		mAppInfo.stopMonitor();
    			} else { // 开启监控
    				mAppInfo.startMonitor();
    			}
            	// 保存监控状态
            	mApplication.getWorkerSp().saveBoolean(WorkerSP.SP_MONITOR_STATUS, on);
            	
            	resetRecyclerList();
//            	mHandler.obtainMessage(HDL_UPDATE_UI).sendToTarget();
            	mHandler.sendEmptyMessageDelayed(HDL_UPDATE_UI, 200);
            }
	    });
    }
    
    private void updateMonitorStatus(boolean monitor) {
    	Logger.d(TAG, "[updateMonitorStatus]" + monitor);
    	mParentActivity.updateMonitorState(monitor);
    	mMonitorBtn.setSmoothChecked(monitor);
    	if (monitor) {
        	//mMonitorBtn.setToggleOn();
//			mMonitorTv.setText(R.string.in_monitor_tips);
//			mDotsTv.showAndPlay();
//			mMonitorTv.setTextColor(UITools.getColor(R.color.md2x_blue_dark));
//			mDotsTv.setTextColor(UITools.getColor(R.color.md2x_blue_dark));
			
			mEmptyTipsTv.setText(R.string.in_monitor_tips);
			mEmptyTipsTv.setTextColor(UITools.getColor(R.color.md2x_blue_dark));
			mEmptyDots.setTextColor(UITools.getColor(R.color.md2x_blue_dark));
			mEmptyDots.setVisibility(View.VISIBLE);
			mEmptyDots.showAndPlay();
		} else {
			//mMonitorBtn.setToggleOff();
//			mMonitorTv.setText(R.string.start_monitor_tips);
//			mDotsTv.hideAndStop();
//			mMonitorTv.setTextColor(UITools.getColor(R.color.md2x_gray));
//			mDotsTv.setTextColor(UITools.getColor(R.color.md2x_gray));
			
			mEmptyTipsTv.setText(R.string.monitor_close_tips);
			mEmptyTipsTv.setTextColor(UITools.getColor(R.color.content_empty_tips_text_color));
			//mEmptyDots.setTextColor(UITools.getColor(R.color.content_empty_tips_text_color));
			mEmptyDots.setVisibility(View.GONE);
			mEmptyDots.hideAndStop();
		}
	}

	private void checkListEmpty() {
    	if (null == mRecycleView) {
    		mRecycleView = (RecyclerView) findViewById(R.id.id_recycle_view);
    	}
    	if (mRecycleBeans.size() == 0) {
    		mRecycleView.setVisibility(View.GONE);
    		mEmptyLayout.setVisibility(View.VISIBLE);
		} else {
			mRecycleView.setVisibility(View.VISIBLE);
			mEmptyLayout.setVisibility(View.GONE);
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
		mRecycleAdapter.notifyDataSetChanged(); /*Null?*/
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
		if (requestCode == Config.REQUEST_CODE_SERVING_SESSION_FRAGMENT) {
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
		case HDL_UPDATE_UI:
//        	if (mAppInfo.getUser().isMonitor()) {
//    			mMonitorTv.setText(R.string.in_monitor_tips);
//    			mDotsTv.showAndPlay();
//    		} else {
//    			mMonitorTv.setText(R.string.start_monitor_tips);
//    			mDotsTv.hideAndStop();
//    		}
        	updateMonitorStatus(mAppInfo.getUser().isMonitor());
			break;
		case HDL_STOP_REFRESH:
			break;
		}
	}
	
	@Override
	public void onRefresh() {
		if (!CoreService.isConnected()) {
			//CoreService.reConnect(mParentActivity);
		
			EventBus.getDefault().post(Boolean.valueOf(true), EventTag.ETAG_ON_LINE);
			mHandler.sendEmptyMessage(HDL_STOP_REFRESH);
			return;
		}
		
		mHandler.sendEmptyMessageDelayed(HDL_STOP_REFRESH, 3000);
	}

	/***** event *****/
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		if (isConnect) {
			updateMonitorStatus(mAppInfo.getUser().isMonitor());
//			try {
//				WorkerRequest wReq = (WorkerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WWRKR, mParentActivity);
//				wReq.getWorkerMonitor();
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
		} else {
			//mAppInfo.getUser().setMonitor(false);
			updateMonitorStatus(false);
			mParentActivity.updateMonitorBadge();
		}
		resetRecyclerList();
	}
	
	@Subscriber(tag = EventTag.ETAG_MONITOR_IN, mode = ThreadMode.MAIN)
	private void updateMonitorIn(CustomerBean cstm) {
		Logger.d(TAG + "-eventbus", "updateMonitorIn -> ETAG_MONITOR_IN");
		resetRecyclerList();
    }

	@Subscriber(tag = EventTag.ETAG_MONITOR_OUT, mode = ThreadMode.MAIN)
	private void updateMonitorOut(CustomerBean cstm) {
		Logger.d(TAG + "-eventbus", "updateMonitorOut -> ETAG_MONITOR_OUT");
		resetRecyclerList();
	}

	@Subscriber(tag = EventTag.ETAG_MONITOR_MESSAGE, mode = ThreadMode.MAIN)
	private void newMonitorMessage(V5Message message) {
		Logger.d(TAG + "-eventbus", "newMonitorMessage -> ETAG_MONITOR_MESSAGE");
		resetRecyclerList();
	}

	@Subscriber(tag = EventTag.ETAG_MONITOR_STATE_CHANGE, mode = ThreadMode.MAIN)
	private void updateMonitorStatus(WorkerBean user) {
		Logger.d(TAG + "-eventbus", "updateMonitorStatus -> ETAG_MONITOR_STATE_CHANGE");
		resetRecyclerList();
		updateMonitorStatus(mAppInfo.getUser().isMonitor());
		mParentActivity.updateMonitorBadge();
	}
	
	@Subscriber(tag = EventTag.ETAG_ACCESSABLE_CHANGE, mode = ThreadMode.MAIN)
	private void onAccessableChange(CustomerBean cstm) {
		Logger.d(TAG + "-eventbus", "onAccessableChange -> ETAG_ACCESSABLE_CHANGE");
		if (mAppInfo.getMonitorMap().containsValue(cstm)) {
			resetRecyclerList();
		}
    }
}
