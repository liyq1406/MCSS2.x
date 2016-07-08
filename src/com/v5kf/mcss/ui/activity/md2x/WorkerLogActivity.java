package com.v5kf.mcss.ui.activity.md2x;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.chyrain.irecyclerview.RefreshRecyclerView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.v5kf.mcss.R;
import com.v5kf.mcss.entity.WorkerLogBean;
import com.v5kf.mcss.ui.adapter.WorkerLogAdapter;
import com.v5kf.mcss.ui.view.V5RefreshLayout;
import com.v5kf.mcss.ui.widget.RecyclerViewDivider;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.WorkerLogUtil;

public class WorkerLogActivity extends BaseToolbarActivity {
	private static final String TAG = "WorkerLogActivity";
	private RefreshRecyclerView mLogList;
	private ArrayList<WorkerLogBean> mLogDatas;
	private WorkerLogAdapter mAdapter;
	private int mOffset = 0;
	private boolean mFinish = false;
//	private boolean firstFinish = true;
	private static final int NUM_PER_PAGE = 20;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_md2x_worker_log);
		
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem itemSave = menu.add(0, 1, Menu.NONE, R.string.save_vedio);
		itemSave.setIcon(R.drawable.v5_action_bar_help);
		itemSave.setShortcut('0', 'h');
		itemSave.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Logger.d(TAG, "help");
			showAlertDialog(R.string.help, R.string.worker_log_help, null);
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void initRecyclerView() {
		mLogList = (RefreshRecyclerView) findViewById(R.id.id_worker_log_list);
		mLogList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		mLogList.addItemDecoration(new RecyclerViewDivider(this, LinearLayoutManager.HORIZONTAL));
		mLogList.setAdapter(mAdapter);
		mLogList.getRefreshableView().setScrollbarFadingEnabled(true);
//		mLogList.getRefreshableView().setScrollBarStyle(RecyclerView.SCROLLBAR_POSITION_RIGHT);
		mLogList.setHasPullUpFriction(false); // 没有上拉阻力
		mLogList.setLoadingMoreWhenLastVisible(true);
    	mLogList.setFooterLayout(new V5RefreshLayout(mApplication, Mode.PULL_FROM_END));
		// Set a listener to be invoked when the list should be refreshed.
		mLogList.setOnRefreshListener(new OnRefreshListener2<RecyclerView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
				//Toast.makeText(mApplication, "Pull Down!", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
				//Toast.makeText(mApplication, "Pull Up!", Toast.LENGTH_SHORT).show();
				Logger.d(TAG, "[onMoreAsked]");
		    	  if (mFinish) {
//		    		  if (firstFinish) {
//		    			  firstFinish = false;
//		    			  // 已加载完全部数据
//		    			  ShowShortToast("日志已全部加载完成");
//		    		  }
		    		  // 已加载完全部数据
	    			  ShowShortToast("日志已全部加载完成");
		    		  mAdapter.notifyDataSetChanged();
		    		  mLogList.onRefreshComplete();
		    		  return;
		    	  }
		    	  updateData();
			}
		});
		
	}
	
	private void updateData() {
		// Auto-generated method stub
//		firstFinish = true;
		mFinish = WorkerLogUtil.queryLogs(mLogDatas, mOffset, NUM_PER_PAGE);
		Logger.d(TAG, "[updateData] size:" + mLogDatas.size());
		mOffset = mLogDatas.size();
		// update UI
		mAdapter.notifyDataSetChanged();
		mLogList.onRefreshComplete();
	}

	@Override
	protected void handleMessage(Message msg) {
		// Auto-generated method stub

	}

}
