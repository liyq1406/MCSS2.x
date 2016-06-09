package com.v5kf.mcss.ui.activity.info;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;

import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.v5kf.mcss.R;
import com.v5kf.mcss.entity.WorkerLogBean;
import com.v5kf.mcss.ui.activity.BaseActivity;
import com.v5kf.mcss.ui.adapter.WorkerLogAdapter;
import com.v5kf.mcss.ui.widget.RecyclerViewDivider;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.WorkerLogUtil;

public class WorkerLogActivity extends BaseActivity {
	private static final String TAG = "WorkerLogActivity";
	private SuperRecyclerView mLogList;
	private ArrayList<WorkerLogBean> mLogDatas;
	private WorkerLogAdapter mAdapter;
	private int mOffset = 0;
	private boolean mFinish = false;
	private boolean firstFinish = true;
	private static final int NUM_PER_PAGE = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_worker_log);
		
		if (mLogDatas == null) {
			mLogDatas = new ArrayList<>();
		}
		if (mAdapter == null) {
			mAdapter = new WorkerLogAdapter(mLogDatas, this);
		}
		initTopBarForLeftBack(R.string.worker_log);
		initRecyclerView();
		updateData();
	}
	
	private void initRecyclerView() {
		mLogList = (SuperRecyclerView) findViewById(R.id.id_worker_log_list);
		mLogList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		mLogList.addItemDecoration(new RecyclerViewDivider(this, LinearLayoutManager.HORIZONTAL));
		mLogList.setAdapter(mAdapter);
		// when there is only 10 items to see in the recycler, this is triggered
		mLogList.setupMoreListener(new OnMoreListener() {
	      @Override
	      public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {
	    	  // Fetch more from Api or DB
	    	  Logger.d(TAG, "[onMoreAsked]");
	    	  if (mFinish) {
	    		  if (firstFinish) {
	    			  firstFinish = false;
	    			  // 已加载完全部数据
	    			  ShowShortToast("日志已全部加载完成");
	    		  }
	    		  mAdapter.notifyDataSetChanged();
	    		  return;
	    	  }
	    	  updateData();
	      }}, 1);
	}
	
	private void updateData() {
		// Auto-generated method stub
		mFinish = WorkerLogUtil.queryLogs(mLogDatas, mOffset, NUM_PER_PAGE);
		Logger.d(TAG, "[updateData] size:" + mLogDatas.size());
		mOffset = mLogDatas.size();
		// update UI
		mAdapter.notifyDataSetChanged();		
	}

	@Override
	protected void handleMessage(Message msg) {
		// Auto-generated method stub

	}

}
