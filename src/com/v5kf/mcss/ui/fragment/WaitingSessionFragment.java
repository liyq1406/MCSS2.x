package com.v5kf.mcss.ui.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.entity.CustomerBean.CustomerType;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.ui.activity.BaseActivity;
import com.v5kf.mcss.ui.adapter.WaitingSessionAdapter;
import com.v5kf.mcss.ui.widget.Divider;
import com.v5kf.mcss.utils.Logger;

/**
 * @author chenhy
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-2 下午11:24:47
 * @package com.v5kf.mcss.ui.fragment.WaitingSessionFragment.java
 * @description
 *
 */
public class WaitingSessionFragment extends BaseFragment implements OnRefreshListener {
	
	private static final String TAG = "WaitingSessionFragment";
	private List<CustomerBean> mRecycleBeans;
	
	private View mView;
	private RecyclerView mRecycleView;
	private WaitingSessionAdapter mRecycleAdapter;
	private BaseFragment mFragment; // 父Fragment
	
	private SwipeRefreshLayout mSwipeRefresh;
	
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static WaitingSessionFragment newInstance(BaseFragment fragment) {
    	WaitingSessionFragment wsfragment = new WaitingSessionFragment(fragment);
        return wsfragment;
    }
    
    public WaitingSessionFragment() {
    	mRecycleBeans = new ArrayList<>();
	}

    public WaitingSessionFragment(BaseFragment fragment) {
    	mRecycleBeans = new ArrayList<>();
    	mFragment = fragment;
	}

    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    }    
    

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.d(TAG, "onCreate");
	}
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {    	
    	if(mView != null){
            return mView;
        }
    	Logger.d(TAG, "onCreateView");    	
    	mView = inflater.inflate(R.layout.fragment_waiting_session, container, false);
    	
    	return mView;
    }
	

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);    	
    	Logger.d(TAG, "onActivityCreated");
		
    	initView();
    	loadData();
    	checkListEmpty();
    }
	
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
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
	
	private void checkListEmpty() {
		if (null == mRecycleView) {
    		mRecycleView = (RecyclerView) mView.findViewById(R.id.id_recycle_view);
    	}
    	if (mRecycleBeans.size() == 0) {
//			mRecycleView.setVisibility(View.INVISIBLE);
    		mSwipeRefresh.setVisibility(View.GONE);
//    		mRecycleView.setBackgroundResource(R.color.transparent);
			getView().findViewById(R.id.layout_container_empty).setVisibility(View.VISIBLE);
		} else {
//			mRecycleView.setVisibility(View.VISIBLE);
			mSwipeRefresh.setVisibility(View.VISIBLE);
//			mRecycleView.setBackgroundResource(R.color.base_list_content_bg);
			getView().findViewById(R.id.layout_container_empty).setVisibility(View.GONE);
		}
    }
	

	/**
     * 初始化界面Adapter和RecycleView
     */
    private void initView() {
    	if (null == mRecycleAdapter) {
    		mRecycleAdapter = new WaitingSessionAdapter(mRecycleBeans, mActivity);
    	}
    	if (null == mRecycleView) {
    		mRecycleView = (RecyclerView) mView.findViewById(R.id.id_recycle_view);
    	}
    	
    	LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
		mRecycleView.setLayoutManager(layoutManager);
		mRecycleView.addItemDecoration(new Divider());
        mRecycleView.setAdapter(mRecycleAdapter);
//        mRecycleView.setScrollbarFadingEnabled(true);
//        mRecycleView.setScrollBarStyle(RecyclerView.SCROLLBAR_POSITION_RIGHT);
        
        /* 刷新控件 */
        if (null == mSwipeRefresh) {
        	mSwipeRefresh = (SwipeRefreshLayout) mView.findViewById(R.id.swipe_refresh_layout);
        }
        mSwipeRefresh.setOnRefreshListener(this);
//        mSwipeRefresh.setProgressViewOffset(false, start, end);
        mSwipeRefresh.setColorSchemeColors(R.color.green, R.color.red,  
        	    R.color.blue, R.color.yellow);
        
        /* 空白按钮 */
        getView().findViewById(R.id.layout_container_tv).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onRefresh();
			}
		});
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
    		Logger.d(TAG, "Already hasRecycleBeans ====");
    		return false;
    	}
//		SessionRecyclerBean recycleBean = new SessionRecyclerBean();
//		recycleBean.setC_id(cstm.getC_id());
//    	recycleBean.setTitle(cstm.getDefaultName());
//    	recycleBean.setIface(cstm.getIface());
//    	recycleBean.setPic(cstm.getDefaultPhoto());
//    	recycleBean.setInTrust(false);
//    	
//    	V5Message msg = mAppInfo.getLastestMessageOfCustomer(cstm);
//    	String content = "";
//    	String s_id = null;
//    	if (null != msg) {
//    		content = msg.getDefaultContent(mActivity);
//    		s_id = msg.getS_id();
//    	} else {
//    		s_id = cstm.getSession().getS_id();
//    	}
//    	recycleBean.setContent(content);
//    	
//    	String date = null;
//    	if (s_id != null) {
//			recycleBean.setS_id(s_id);
//			SessionBean session = mAppInfo.getSessionBean(s_id);
//			if (session != null) {
//				recycleBean.setInTrust(session.isInTrust());
//				recycleBean.setService(session.getService());
//				recycleBean.setUnreadNum(session.getUnreadMessageNum());
//				date = mAppInfo.getCreateTimeOfSession(session);
//			}
//		}
//    	if (null == date || date.isEmpty()) {
//    		date = DateUtil.getCurrentTime();
//    	}
//    	recycleBean.setDate(date);
		mRecycleBeans.add(0, cstm);
		mRecycleAdapter.notifyDataSetChanged();
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
            	if (isUnread) {
            		bean.getSession().addUnreadMessageNum();
            	} 
            	if (clearUnread) {
            		bean.getSession().clearUnreadMessageNum();
            	}
        		Logger.d(TAG, "mRecycleBeans.update " + position);
        		break;
        	}
        	position++;
		}
		if (null != tmpBean && goTop) {
			mRecycleBeans.remove(position);
			mRecycleBeans.add(0, tmpBean);
			mRecycleAdapter.notifyDataSetChanged();
			position = 0;
		}
		
		if (position >= mRecycleBeans.size()) {
			return -1;
		}		
		return position;
	}
	
	private void resetRecyclerList() {
		mRecycleBeans.clear();
		loadData();
		mRecycleAdapter.notifyDataSetChanged();
		checkListEmpty();
	}
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Logger.d(TAG, "onActivityResult REQ_code:" + requestCode + " resultCOde:" + resultCode);
		if (null == data) {
			Logger.w(TAG, "Activity result with no Intent data");
			return;
		}
		if (requestCode == Config.REQUEST_CODE_WAITING_SESSION_FRAGMENT) {
//			String s_id = data.getStringExtra(Config.EXTRA_KEY_S_ID);
			String c_id = data.getStringExtra(Config.EXTRA_KEY_C_ID);
			if (mRecycleAdapter == null) {
				Logger.e(TAG, "[onActivityResult] mRecycleAdapter is null, activity not create");
				return;
			}
			if (resultCode == Config.RESULT_CODE_CHAT_CONTENT_ADD) {
				int pos = updateRecycleBean(c_id, false, false, true);
				if (pos >= 0) {
					mRecycleAdapter.notifyItemRangeChanged(0, pos + 1);
				}
			} else if (resultCode == Config.RESULT_CODE_CHAT_CONTENT_NOCHANGE) {
				int pos = updateRecycleBean(c_id, false, false, true);
				if (pos >= 0) {
					mRecycleAdapter.notifyItemRangeChanged(0, pos + 1);
				}
			}
		}
	}
	
	
	@Override
	protected void handleMessage(Message msg, BaseActivity baseActivity) {
		switch (msg.what) {
		case HDL_STOP_REFRESH:
			if (mSwipeRefresh.isRefreshing()) {
				mSwipeRefresh.setRefreshing(false);
			}
			break;
		}
	}
	
	@Override
	public void onFragmentSelected() {
		// TODO Auto-generated method stub
		Logger.d(TAG, "onFragmentSelected - WaitingSession");
	}

	@Override
	public void onRefresh() {
		Logger.i(TAG, "onRefresh...");
		/* 请求等待列表数据 */
		try {
			CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, mActivity);
			cReq.getWaitingCustomer();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		mHandler.sendEmptyMessageDelayed(HDL_STOP_REFRESH, 3000);
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
		mHandler.sendEmptyMessage(HDL_STOP_REFRESH);
    }
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		resetRecyclerList();
	}
}
