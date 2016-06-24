package com.v5kf.mcss.ui.fragment;

import java.util.ArrayList;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.entity.ArchWorkerBean;
import com.v5kf.mcss.entity.WorkerArch;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.qao.request.WorkerRequest;
import com.v5kf.mcss.ui.activity.info.BaseActivity;
import com.v5kf.mcss.ui.activity.md2x.WorkerInfoActivity;
import com.v5kf.mcss.ui.adapter.tree.Node;
import com.v5kf.mcss.ui.adapter.tree.SimpleTreeAdapter;
import com.v5kf.mcss.ui.adapter.tree.TreeListViewAdapter;
import com.v5kf.mcss.ui.adapter.tree.TreeListViewAdapter.OnTreeNodeClickListener;
import com.v5kf.mcss.ui.widget.PinnedSectionListView;
import com.v5kf.mcss.utils.Logger;

/**
 * @author chenhy
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-2 下午11:24:47
 * @package com.v5kf.mcss.ui.fragment.SessionFragment.java
 * @description
 *
 */
public class WorkerTreeFragment extends BaseFragment implements OnRefreshListener {
	
	private static final String TAG = "WorkerTreeFragment";
	
	private List<WorkerArch> mDatas;
	private View mView;
	private PinnedSectionListView mSectionListView;
	private TreeListViewAdapter<WorkerArch> mAdapter;
	
	private SwipeRefreshLayout mSwipeRefresh;
	
	private int list_mode;	
	private String c_id;
	private String w_id;
	private long g_id;
	
    
    public WorkerTreeFragment() {
    	this.list_mode = Config.LIST_MODE_NORMAL;
    	mDatas = new ArrayList<>();
    }

//    public WorkerListFragment(int listmode) {
//    	mDatas = new ArrayList<>();
//    	this.list_mode = listmode;
//	}
    
    
	public WorkerTreeFragment(int listmode, String cid) {
    	mDatas = new ArrayList<>();
    	this.list_mode = listmode;
    	this.c_id = cid;
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
    	mView = inflater.inflate(R.layout.fragment_contact_worker, container, false);
    	
    	return mView;
    }
	

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);    	
    	Logger.d(TAG, "onActivityCreated");
		
    	initData();
    	initView();
    	mAdapter.notifyTreeListDataChange();
    	checkListEmpty();
    }

	
	private void initData() {
		g_id = 0;
		w_id = null;
		
		/* 获取企业架构列表 */
		if (Config.USE_DB) {
			mDatas = DataSupport.where("type <> ?", "worker").find(WorkerArch.class);
		} else {
			for (WorkerArch arch : mAppInfo.getWorkerArchs()) {
				if (arch.getType().equals("worker")) {
					Logger.d("WorkerTreeFragment", "WorkerArch type:" + arch.getType());
					continue;
				}
				Logger.d("WorkerTreeFragment", "WorkerArch " + " Type:" + arch.getType() + " name:" + arch.getName()
				+ " id:" + arch.getObjId() + " pid:" + arch.getParentId());
				mDatas.add(arch);
			}
		}
	}
	
	
	/**
     * 初始化界面Adapter和RecycleView
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
     */
    private void initView() {
    	if (null == mSectionListView) {
    		mSectionListView = (PinnedSectionListView) mView.findViewById(R.id.id_list_view);
    	}
    	if (null == mAdapter) {
    		try {
				mAdapter = new SimpleTreeAdapter<WorkerArch>(mSectionListView, this.mActivity, mDatas, 1, list_mode);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
    	}
    	mSectionListView.setAdapter(mAdapter);
    	
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
		mAdapter.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {
			@Override
			public void onClick(Node node, int position) {
				if (list_mode == Config.LIST_MODE_SWITCH) {
					// 转接选择列表点击
					if (node.getType().equals(QAODefine.ORGANIZATION)) {
						return;
					} else {
						Logger.i("WorkerListFragment-Tree", "setOnTreeNodeClickListener--- --click - -");
						node.setSelect(!node.isSelect());
						mAdapter.notifyDataSetChanged();
						if (node.getType().equals(QAODefine.WORKER)) {
							w_id = node.getWorker().getW_id();
							g_id = 0;
						} else if (node.getType().equals(QAODefine.GROUP)) {
							w_id = null;
							g_id = node.getId();
						}
					}
				} else if (list_mode == Config.LIST_MODE_NORMAL) {
					// 坐席显示列表点击事件
					if (node.getType().equals(QAODefine.WORKER) && node.getWorker() != null) {
						Bundle bundle = new Bundle();
						bundle.putInt(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_ACTIVITY_START);
						bundle.putString(Config.EXTRA_KEY_W_ID, node.getWorker().getW_id());			
						Intent intent = new Intent(getActivity(), WorkerInfoActivity.class);
						intent.putExtras(bundle);
						startActivity(intent);
						mActivity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
					}
				}
			}
		});
		
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
    	if (null == mSectionListView) {
    		mSectionListView = (PinnedSectionListView) mView.findViewById(R.id.id_list_view);
    	}
    	if (mDatas.size() == 0) {
    		mSectionListView.setVisibility(View.INVISIBLE);
			getView().findViewById(R.id.layout_container_empty).setVisibility(View.VISIBLE);
		} else {
			mSectionListView.setVisibility(View.VISIBLE);
			getView().findViewById(R.id.layout_container_empty).setVisibility(View.GONE);
		}
    }
	
    
    private void onFinishWorkerList() {
		if (list_mode == Config.LIST_MODE_SWITCH &&
				w_id != null || g_id != 0) {
			try {
				CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, mActivity);
				if (w_id != null && g_id == 0) {
					cReq.switchCustomerToWorker(c_id, w_id);
				} else if (w_id == null && g_id != 0) {
					cReq.switchCustomerToGroup(c_id, g_id);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
    
    
	@Override
	public void onResume() {
		super.onResume();
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		onFinishWorkerList();
	}
	
	
//	private void updateContactBean(String wid) {
//		if (null == wid || wid.isEmpty()) {
//			Logger.e(TAG, "[updateContactBean] null wid");
//			return;
//		}
//		mAdapter.notifyTreeListDataChange();
//		checkListEmpty();
//	}
	
	/**
	 * 更新整个坐席列表
	 * @param updateWorkersList WorkerContactFragment 
	 * @return void
	 */
	private void updateWorkersList() {
		mDatas.clear();
		initData();
		mAdapter.notifyTreeListDataChange();
		checkListEmpty();
	}
    
	/**
	 * 所有坐席状态置为离线
	 * @param clearWorkersStatus WorkerContactFragment 
	 * @return void
	 */
    private void clearWorkersStatus() {
    	for (ArchWorkerBean wrkr : mAppInfo.getWorkerMap().values()) {
			wrkr.setStatus(QAODefine.STATUS_OFFLINE);
		}
    	mAdapter.notifyTreeListDataChange();
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRefresh() {
		/* 请求坐席列表数据 */
		try {
			WorkerRequest wReq = (WorkerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WWRKR, mActivity);
			wReq.getArchWorkers();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		mHandler.sendEmptyMessageDelayed(HDL_STOP_REFRESH, 3000);
	}

	/***** event *****/
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		clearWorkersStatus();
	}

	@Subscriber(tag = EventTag.ETAG_ARCH_WORKER_CHANGE, mode = ThreadMode.MAIN)
	private void archWorkerChange(AppInfoKeeper appinfo) {
		updateWorkersList();
	}
}
