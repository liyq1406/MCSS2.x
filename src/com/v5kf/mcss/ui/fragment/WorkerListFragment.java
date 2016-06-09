package com.v5kf.mcss.ui.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.litepal.crud.DataSupport;
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
import com.v5kf.mcss.entity.ArchWorkerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.WorkerRequest;
import com.v5kf.mcss.ui.activity.BaseActivity;
import com.v5kf.mcss.ui.adapter.WorkerListAdapter;
import com.v5kf.mcss.ui.widget.Divider;
import com.v5kf.mcss.utils.Logger;

/**
 * @author chenhy
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-2 下午11:24:47
 * @package com.v5kf.mcss.ui.fragment.SessionFragment.java
 * @description
 *
 */
public class WorkerListFragment extends BaseFragment implements OnRefreshListener {
	
	private static final String TAG = "WorkerListFragment";
	
	private List<ArchWorkerBean> mDatas;
	private View mView;
	private RecyclerView mListView;
	private LinearLayoutManager mLayoutManager;
	private WorkerListAdapter mAdapter;	
	private SwipeRefreshLayout mSwipeRefresh;
	
    public WorkerListFragment() {
    	mDatas = new ArrayList<>();
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
    	mView = inflater.inflate(R.layout.fragment_contact_wlist, container, false);
    	
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

	
	private void loadData() {
		/* 获取企业坐席列表 */
		if (Config.USE_DB) {
			for (ArchWorkerBean cow : DataSupport.findAll(ArchWorkerBean.class)) {
				addWorkerBean(cow);
			}
		} else {
			for (ArchWorkerBean cow : mAppInfo.getWorkerMap().values()) {
				addWorkerBean(cow);
			}
		}
		
		// 排序：在线最前，然后是忙碌，离开，离线
		WorkerStatusCompartor wsc = new WorkerStatusCompartor();
		Collections.sort(mDatas, wsc);		
		mAdapter.notifyDataSetChanged();
	}
	
	private void addWorkerBean(ArchWorkerBean cow) {
		if (null == mDatas) {
			mDatas = new ArrayList<>();
		}
		if (mDatas.contains(cow)) {
			return;
		} else {
			mDatas.add(cow);
			Logger.d(TAG, "addWorkerBean " + cow.getName() + " photo:" + cow.getPhoto());
		}
	}


	/**
     * 初始化界面Adapter和RecycleView
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
     */
    private void initView() {
    	if (null == mListView) {
    		mListView = (RecyclerView) mView.findViewById(R.id.id_recycle_view);
    	}
    	if (null == mAdapter) {
    		mAdapter = new WorkerListAdapter(mActivity, mDatas);
    	}
    	mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
    	mListView.setLayoutManager(mLayoutManager);
    	mListView.addItemDecoration(new Divider());
    	mListView.setAdapter(mAdapter);
    	mListView.setScrollbarFadingEnabled(true);
    	mListView.setScrollBarStyle(RecyclerView.SCROLLBAR_POSITION_RIGHT);
    	mListView.setAdapter(mAdapter);
    	
    	/* 刷新控件 */
        if (null == mSwipeRefresh) {
        	mSwipeRefresh = (SwipeRefreshLayout) mView.findViewById(R.id.swipe_refresh_layout);
        }
        mSwipeRefresh.setOnRefreshListener(this);
        mSwipeRefresh.setColorSchemeColors(R.color.green, R.color.red,  
        	    R.color.blue, R.color.yellow);
        
    	addListListener();
    }
    
    
    private void addListListener() {
		getView().findViewById(R.id.layout_container_tv).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Logger.d(TAG, "--- 点击消息为空提示图标 ---");
//				mAppInfo.clearRunTimeInfo();
//				mActivity.onWebsocketLogin();
				onRefresh();
//				updateWorkersList();
			}
		});
	}
     
    
    private void checkListEmpty() {
    	if (null == mListView) {
    		mListView = (RecyclerView) mView.findViewById(R.id.id_recycle_view);
    	}
    	if (mDatas.size() == 0) {
    		mSwipeRefresh.setVisibility(View.GONE);
			getView().findViewById(R.id.layout_container_empty).setVisibility(View.VISIBLE);
		} else {
			mSwipeRefresh.setVisibility(View.VISIBLE);
			getView().findViewById(R.id.layout_container_empty).setVisibility(View.GONE);
		}
    }
    
    
	@Override
	public void onResume() {
		super.onResume();
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	
//	private void updateContactBean(String wid) {
//		if (null == wid || wid.isEmpty()) {
//			Logger.e(TAG, "[updateContactBean] null wid");
//			return;
//		}
//		int i = 0;
//		for (; i < mDatas.size(); i++) {
//			if (mDatas.get(i).getW_id().equals(wid)) {
//				mDatas.remove(i);
//				mDatas.add(i, mAppInfo.getCoWorker(wid));
//				break;
//			}
//		}
//
//		// 排序：在线最前，然后是忙碌，离开，离线
//		WorkerStatusCompartor wsc = new WorkerStatusCompartor();
//		Collections.sort(mDatas, wsc);
//		mAdapter.notifyDataSetChanged();
//	}
	
	/**
	 * 更新整个坐席列表
	 * @param updateWorkersList WorkerContactFragment 
	 * @return void
	 */
	private void updateWorkersList() {
		mDatas.clear();
		loadData();
		checkListEmpty();
	}
    
//	/**
//	 * 所有坐席状态置为离线
//	 * @param clearWorkersStatus WorkerContactFragment 
//	 * @return void
//	 */
//    private void clearWorkersStatus() {
//		for (ArchWorkerBean cow : mDatas) {
//			cow.setStatus(QAODefine.STATUS_OFFLINE);
//		}
//    	mAdapter.notifyDataSetChanged();
//    	checkListEmpty();
//    }
    
  	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Logger.d(TAG, "onActivityResult REQ_code:" + requestCode + " resultCOde:" + resultCode);
		if (null == data) {
			Logger.w(TAG, "Activity result with no Intent data");
			return;
		}
		if (requestCode == Config.REQUEST_CODE_WORKER_CONTACT_FRAGMENT) {
//			long w_id = data.getLongExtra(Config.EXTRA_KEY_W_ID, Config.NOTIFY_ID_NULL);
			if (mAdapter == null) {
				Logger.e(TAG, "[onActivityResult] mAdapter is null, activity not create");
				return;
			}
			
			/**/
			if (resultCode == Config.RESULT_CODE_WORKER_INFO_CHANGE) {
				//
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
		Logger.d(TAG, "onFragmentSelected - WorkerList");
	}

	@Override
	public void onRefresh() {
		/* 请求等待列表数据 */
		try {
			WorkerRequest wReq = (WorkerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WWRKR, mActivity);
			wReq.getArchWorkers();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		mHandler.sendEmptyMessageDelayed(HDL_STOP_REFRESH, 3000);
	}

	/**
	 * 节点坐席状态比较器
	 * @author Chenhy	
	 * @email chenhy@v5kf.com
	 * @version v1.0 2015-9-28 上午9:53:14
	 * @package com.v5kf.mcss.ui.adapter.tree of MCSS-Native
	 * @file TreeHelper.java 
	 *
	 */
	static class WorkerStatusCompartor implements Comparator<ArchWorkerBean> {

		@Override
		public int compare(ArchWorkerBean lhs, ArchWorkerBean rhs) {
			short r = lhs.getStatus();
			short l = rhs.getStatus();
			if (l == r) {
				return 0;
			} else {
				switch (l) {
				case QAODefine.STATUS_ONLINE:
					if (r == QAODefine.STATUS_ONLINE) {
						return 0;
					} else {
						return 1;
					}
				case QAODefine.STATUS_OFFLINE:
					if (r == QAODefine.STATUS_OFFLINE || r == QAODefine.STATUS_HIDE) {
						return 0;
					} else {
						return -1;
					}
				case QAODefine.STATUS_BUSY:
					if (r == QAODefine.STATUS_ONLINE) {
						return -1;
					} else {
						return 1;
					}
				case QAODefine.STATUS_LEAVE:
					if (r == QAODefine.STATUS_OFFLINE || r == QAODefine.STATUS_HIDE) {
						return 1;
					} else {
						return -1;
					}
				case QAODefine.STATUS_HIDE:
					if (r == QAODefine.STATUS_OFFLINE || r == QAODefine.STATUS_HIDE) {
						return 0;
					} else {
						return -1;
					}
				}
			}
			return 0;
		}		
	}
	
	/***** event *****/
	
	@Subscriber(tag = EventTag.ETAG_ARCH_WORKER_CHANGE, mode = ThreadMode.MAIN)
	private void archWorkerChange(AppInfoKeeper appinfo) {
		Logger.d(TAG + "-eventbus", "archWorkerChange -> ETAG_ARCH_WORKER_CHANGE");
		updateWorkersList();
		mHandler.sendEmptyMessage(HDL_STOP_REFRESH);
	}
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		updateWorkersList();
	}
}
