package com.v5kf.mcss.ui.fragment.md2x;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.litepal.crud.DataSupport;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.chyrain.irecyclerview.RefreshRecyclerView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.entity.ArchWorkerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.WorkerRequest;
import com.v5kf.mcss.ui.activity.MainTabActivity;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.ui.adapter.WorkerListAdapter;
import com.v5kf.mcss.ui.view.V5RefreshLayout;
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
public class TabWorkerListFragment extends TabBaseFragment implements OnRefreshListener {
	
	private static final String TAG = "WorkerListFragment";
	
	private List<ArchWorkerBean> mDatas;
	private RefreshRecyclerView mRefreshRecyclerView;
	private LinearLayoutManager mLayoutManager;
	private WorkerListAdapter mAdapter;
	
	private TextView mEmptyTipsTv;
	
	public TabWorkerListFragment() {
		super();
		// TODO Auto-generated constructor stub
		mDatas = new ArrayList<>();
	}
	
    public TabWorkerListFragment(MainTabActivity activity, int index) {
		super(activity, index);
		mDatas = new ArrayList<>();
	}


    @Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		setContentView(R.layout.fragment_md2x_contact_wlist);

		Logger.d(TAG, TAG + " 将要创建View " + this);
		initView();
    	loadData();
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
//		this.mParentActivity.updateToolbar(mIndex);
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
    
	private void loadData() {
		//mParentActivity.showProgress();
		mRefreshRecyclerView.setRefreshing();
		
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
		
		mHandler.obtainMessage(HDL_STOP_REFRESH).sendToTarget();
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
    	if (null == mRefreshRecyclerView) {
    		mRefreshRecyclerView = (RefreshRecyclerView) findViewById(R.id.id_irecycler_worker);
    	}
    	if (null == mAdapter) {
    		mAdapter = new WorkerListAdapter(mParentActivity, mDatas);
    	}
    	mLayoutManager = new LinearLayoutManager(mParentActivity, LinearLayoutManager.VERTICAL, false);
    	mRefreshRecyclerView.setLayoutManager(mLayoutManager);
    	mRefreshRecyclerView.addItemDecoration(new Divider());
    	mRefreshRecyclerView.setAdapter(mAdapter);
    	mRefreshRecyclerView.getRefreshableView().setScrollbarFadingEnabled(true);
    	mRefreshRecyclerView.getRefreshableView().setScrollBarStyle(RecyclerView.SCROLLBAR_POSITION_RIGHT);
    	
    	mRefreshRecyclerView.setHeaderLayout(new V5RefreshLayout(mParentActivity));
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
			mEmptyTipsTv.setText(R.string.worker_list_empty_tips);
//			mEmptyTipsTv.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					onRefresh();
//				}
//			});
		}
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
	protected void handleMessage(Message msg, ActivityBase baseActivity) {
		switch (msg.what) {
		case HDL_STOP_REFRESH:
			mHandler.removeMessages(HDL_TIME_OUT);
			if (mRefreshRecyclerView.isRefreshing()) {
				mRefreshRecyclerView.onRefreshComplete();
			}
			mParentActivity.dismissProgress();
			break;
		case HDL_TIME_OUT:
			if (mRefreshRecyclerView.isRefreshing()) {
				mRefreshRecyclerView.onRefreshComplete();
				onRefreshTimeOut();
			}
			mParentActivity.dismissProgress();
			break;
		}
	}

	@Override
	public void onRefresh() {
		/* 请求等待列表数据 */
		try {
			WorkerRequest wReq = (WorkerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WWRKR, mParentActivity);
			wReq.getArchWorkers();
//			mParentActivity.showProgress();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		mHandler.sendEmptyMessageDelayed(HDL_TIME_OUT, Config.WS_TIME_OUT);
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
		mHandler.obtainMessage(HDL_STOP_REFRESH).sendToTarget();
	}
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		updateWorkersList();
	}
}
