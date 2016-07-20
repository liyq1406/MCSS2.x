package com.v5kf.mcss.ui.activity.md2x;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.chyrain.irecyclerview.RefreshRecyclerView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.v5kf.client.lib.V5WebSocketHelper;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.entity.CustomerBean.CustomerType;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.ui.adapter.WaitingSessionAdapter;
import com.v5kf.mcss.ui.view.V5RefreshLayout;
import com.v5kf.mcss.ui.widget.Divider;
import com.v5kf.mcss.utils.Logger;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class WaitingCustomerActivity extends BaseToolbarActivity implements OnRefreshListener {
	private static final String TAG = "WaitingCustomerActivity";
	protected static final int HDL_STOP_REFRESH = 11;
	protected static final int HDL_STOP_LOAD = 12;
	protected static final int HDL_UPDATE_UI = 13;
	protected static final int HDL_TIME_OUT = 14;
	
	private List<CustomerBean> mRecycleBeans;
	
	private RefreshRecyclerView mRefreshRecyclerView;
	private WaitingSessionAdapter mRecycleAdapter;
	private TextView mEmptyTipsTv;
	
	private SmoothProgressBar mTopProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_md2x_waiting_customer);
		
		mRecycleBeans = new ArrayList<>();
		initView();
		loadData();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	private void loadData() {
		Map<String, CustomerBean> cstmMap = mAppInfo.getCustomerMap();
		mRecycleBeans.clear();
		for (CustomerBean cstm : cstmMap.values()) {
			if (cstm.getCstmType() == CustomerType.CustomerType_WaitingAlive) {
				addRecycleBean(cstm);
			}
		}
	}

	private void initView() {
		initToolbar();
		
		mTopProgressBar = (SmoothProgressBar)findViewById(R.id.top_progress_bar);
		if (null == mRecycleAdapter) {
    		mRecycleAdapter = new WaitingSessionAdapter(mRecycleBeans, this);
    	}
    	if (null == mRefreshRecyclerView) {
    		mRefreshRecyclerView = (RefreshRecyclerView) findViewById(R.id.id_irecycler_waiting);
    	}
    	
    	LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    	mRefreshRecyclerView.setLayoutManager(layoutManager);
    	mRefreshRecyclerView.addItemDecoration(new Divider());
    	mRefreshRecyclerView.setAdapter(mRecycleAdapter);
//        mRecycleView.setScrollbarFadingEnabled(true);
//        mRecycleView.setScrollBarStyle(RecyclerView.SCROLLBAR_POSITION_RIGHT);
        
    	mRefreshRecyclerView.setHeaderLayout(new V5RefreshLayout(this));
    	mRefreshRecyclerView.setOnRefreshListener(new OnRefreshListener2<RecyclerView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
				//Toast.makeText(mParentActivity, "Pull Down!", Toast.LENGTH_SHORT).show();
				onRefresh();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
				//Toast.makeText(mParentActivity, "Pull Up!", Toast.LENGTH_SHORT).show();
				
			}
		});
		
		if (mRefreshRecyclerView.getEmptyView() != null) {
			mEmptyTipsTv = (TextView) mRefreshRecyclerView.getEmptyView().findViewById(R.id.layout_container_tv);
		}
        /* 空白按钮 */
		if (mEmptyTipsTv != null) {
			mEmptyTipsTv.setText(R.string.waiting_content_empty_tips);
			mEmptyTipsTv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mRefreshRecyclerView.setRefreshing();
					onRefresh();
				}
			});
		}
	}

	private void initToolbar() {
		// 父类已初始化
		initTopBarForOnlyTitle(R.string.seg_title_waiting);
//		this.setNavigationBarColor(UITools.getColor(R.color.main_color));
//		this.setStatusbarColor(UITools.getColor(R.color.main_color));
//		//this.setWindowBackground(UITools.getColor(R.color.transparent));
		//getWindow().setBackgroundDrawable(new ColorDrawable(UITools.getColor(R.color.transparent)));
	}
	
	private boolean addRecycleBean(CustomerBean cstm) {
    	if (null == cstm || hasRecycleBeans(cstm.getC_id())) {
    		Logger.d(TAG, "Already hasRecycleBeans ====");
    		return false;
    	}
		mRecycleBeans.add(0, cstm);
		mRecycleAdapter.notifyDataSetChanged();
		Logger.i(TAG, "[[[mRecycleBeans.add]]] c_id = " + cstm.getC_id());
		return true;
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
	
	private void resetRecyclerList() {
		mRecycleBeans.clear();
		loadData();
		mRecycleAdapter.notifyDataSetChanged();
	}
	
	private void showProgress() {
		mTopProgressBar.setVisibility(View.VISIBLE);
	}
	
	private void dismissProgress() {
		mTopProgressBar.setVisibility(View.GONE);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case android.R.id.home: // Toolbar home点击
            finishActivity();
            return true;
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case HDL_STOP_REFRESH:
			mHandler.removeMessages(HDL_TIME_OUT);
			if (mRefreshRecyclerView.isRefreshing()) {
				mRefreshRecyclerView.onRefreshComplete();
			}
			break;
		case HDL_TIME_OUT:
			if (mRefreshRecyclerView.isRefreshing()) {
				mRefreshRecyclerView.onRefreshComplete();
				onRefreshTimeOut();
			}
			dismissProgress();
			break;
		}
	}

	@Override
	protected void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Config.REQUEST_CODE_WAITING_SESSION_FRAGMENT
				&& resultCode == Config.RESULT_CODE_PICKUP_CSTM) {
			finish();
		}
	}

	@Override
	public void onRefresh() {
		Logger.i(TAG, "onRefresh...");
		/* 请求等待列表数据 */
		try {
			CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, this);
			cReq.getWaitingCustomer();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		mHandler.sendEmptyMessageDelayed(HDL_TIME_OUT, Config.WS_TIME_OUT);
	}

	/***** event *****/

//	@Subscriber(tag = EventTag.ETAG_CSTM_OUT, mode = ThreadMode.POST)
//	private void updateCustomerOut(CustomerBean cstm) {
//		Logger.d(TAG + "-updateCustomerOut", ">>>CSTM_WAIT_OUT");
//		if (cstm.getCstmType() == CustomerType.CustomerType_WaitingAlive) {
//			int pos = deleteRecycleBean(cstm.getC_id());
//			if (pos >= 0) {
//				mRecycleAdapter.notifyDataSetChanged();
//			}
//		}
//    }
	
	@Subscriber(tag = EventTag.ETAG_WAITING_CSTM_CHANGE, mode = ThreadMode.MAIN)
	private void waitingCustomerChange(AppInfoKeeper appinfo) {
		Logger.d(TAG + "-eventbus", "waitingCustomerChange -> ETAG_SERVING_CSTM_CHANGE");
		// 更新整个列表
		resetRecyclerList();
		dismissProgress();
		mHandler.sendEmptyMessageDelayed(HDL_STOP_REFRESH, 200);
    }
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		resetRecyclerList();
		if (isConnect) {
			
		} else {
			finishActivity();
		}
	}
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_START, mode = ThreadMode.MAIN)
	private void onConnectionStart(V5WebSocketHelper client) {
		Logger.d(TAG + "-eventbus", "onConnectionStart -> ETAG_CONNECTION_START");
		showProgress();
	}
	
	@Subscriber(tag = EventTag.ETAG_PICK_CSTM_OK, mode = ThreadMode.MAIN)
	private void pickUpSuccess(AppInfoKeeper appinfo) {
		Logger.d(TAG + "-eventbus", "pickUpSuccess -> ETAG_PICK_CSTM_OK");
		finish();
	}
}
