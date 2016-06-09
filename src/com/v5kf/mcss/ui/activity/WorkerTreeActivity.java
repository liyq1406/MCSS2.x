package com.v5kf.mcss.ui.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.litepal.crud.DataSupport;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.WorkerArch;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.ui.adapter.tree.Node;
import com.v5kf.mcss.ui.adapter.tree.SimpleTreeAdapter;
import com.v5kf.mcss.ui.adapter.tree.TreeListViewAdapter;
import com.v5kf.mcss.ui.adapter.tree.TreeListViewAdapter.OnTreeNodeClickListener;
import com.v5kf.mcss.ui.view.HeaderLayout.onLeftImageButtonClickListener;
import com.v5kf.mcss.ui.view.HeaderLayout.onRightImageButtonClickListener;
import com.v5kf.mcss.ui.widget.PinnedSectionListView;
import com.v5kf.mcss.utils.Logger;

public class WorkerTreeActivity extends BaseActivity {
	
	private List<WorkerArch> mDatas;
	private PinnedSectionListView mListView;
	private TreeListViewAdapter<WorkerArch> mAdapter;
	
	private int list_mode;
	private String s_id;
	private String c_id;
	private int service;
	
	private String w_id;
	private long g_id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_workertree);
		
		handleIntent();
		initData();
		findView();
		initView();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void handleIntent() {
		Intent intentNotify = getIntent();
		int type = intentNotify.getIntExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_NULL);
		if (Config.EXTRA_TYPE_ACTIVITY_START == type) {
			s_id = intentNotify.getStringExtra(Config.EXTRA_KEY_S_ID);
			c_id = intentNotify.getStringExtra(Config.EXTRA_KEY_C_ID);
			service = intentNotify.getIntExtra(Config.EXTRA_KEY_SERVICE, Config.NOTIFY_ID_NULL);
			Logger.d("WorkerTreeActivity", "ServingSessionFragment -> Intent -> WorkerTreeActivity" +
					"\n s_id:" + s_id + " c_id:" + c_id + " service:" + service);
		}
		
		if (c_id == null && s_id == null) {
			list_mode = Config.LIST_MODE_NORMAL;
		} else {
			list_mode = Config.LIST_MODE_SWITCH;
			Logger.i("Tree", "list_mode is switch");
		}
	}
	
	private void initData() {
		g_id = 0;
		w_id = null;
		mDatas = new ArrayList<WorkerArch>();
		
		/* 获取企业架构列表 */
		if (Config.USE_DB) {
			mDatas = DataSupport.where("type <> ?", "worker").find(WorkerArch.class);
		} else {
			/* [修改]修复架构为空时的Nullpoint错误 */
			if (mAppInfo.getWorkerArchs() != null && mAppInfo.getWorkerArchs().size() > 0) {
				for (WorkerArch arch : mAppInfo.getWorkerArchs()) {
					if (arch.getType().equals("worker")) {
						Logger.d("WorkerTreeActivity", "WorkerArch type:" + arch.getType());
						continue;
					}
					Logger.d("WorkerTreeActivity", "WorkerArch " + " Type:" + arch.getType() + " name:" + arch.getName()
					+ " id:" + arch.getObjId() + " pid:" + arch.getParentId());
					mDatas.add(arch);
				}
			}
		}
	}
	

	private void findView() {
		mListView = (PinnedSectionListView) findViewById(R.id.id_list_view);
	}

	private void initView() {
		if (Config.LIST_MODE_NORMAL == list_mode) {
			initTopBarForLeftBack(R.string.set_arch_workers);
		} else {
			initTopBarForLeftImageAndRightImage(
					R.string.title_switch_activity, 
					R.drawable.v5_action_bar_cancel, 
					R.drawable.v5_action_bar_ok, 
					new onLeftImageButtonClickListener() {			
						@Override
						public void onClick(View v) {
							finishActivity();
						}
					}, 
					new onRightImageButtonClickListener() {						
						@Override
						public void onClick(View arg0) {
							if (list_mode == Config.LIST_MODE_SWITCH &&
									w_id == null && g_id == 0) {
								ShowToast(R.string.on_switch_cstm_empty);
							} else {
								doSwitchWorker();
							}
						}
					});
		}
		
		checkListEmpty();
		try {
			Logger.i("WorkerTreeActivity", "Trees:" + mDatas.size());
			for (WorkerArch org : mDatas) {
				Logger.i("Tree", "id:" + org.getObjId() + " pid:" + org.getParentId() + " name:" + org.getName());
			}
			
            mAdapter = new SimpleTreeAdapter<WorkerArch>(mListView, this, mDatas, 1, list_mode);
            mListView.setAdapter(mAdapter);
    		addListListener();
        } catch (IllegalAccessException e) {  
            e.printStackTrace();  
        }
	}
	
	private void addListListener() {
		mAdapter.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {
			@Override
			public void onClick(Node node, int position) {
				if (list_mode == Config.LIST_MODE_SWITCH) {
					if (node.getType().equals(QAODefine.ORGANIZATION)) {
						return;
					} else if (node.getType().equals(QAODefine.WORKER)) {
						node.setSelect(!node.isSelect());
						mAdapter.notifyDataSetChanged();
						if (node.getWorker().getStatus() != QAODefine.STATUS_OFFLINE) {
							w_id = node.getWorker().getW_id();
							g_id = 0;
						} else {
							w_id = null;
							g_id = 0;
						}
					} else if (node.getType().equals(QAODefine.GROUP)) {
						node.setSelect(!node.isSelect());
						mAdapter.notifyDataSetChanged();
						w_id = null;
						g_id = node.getId();
					}
				}
			}
		});
	}


	private void checkListEmpty() {
    	if (null == mListView) {
    		mListView = (PinnedSectionListView) findViewById(R.id.id_list_view);
    	}
    	
    	if (mDatas.size() == 0) {
    		mListView.setVisibility(View.GONE);
			findViewById(R.id.layout_container_empty).setVisibility(View.VISIBLE);
		} else {
			mListView.setVisibility(View.VISIBLE);
			findViewById(R.id.layout_container_empty).setVisibility(View.GONE);
		}
    }
	
	
	private void doSwitchWorker() {
		if (list_mode == Config.LIST_MODE_SWITCH &&
				(w_id != null || g_id != 0)) {
			try {
				CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, this);
				if (w_id != null && g_id == 0) {
					cReq.switchCustomerToWorker(c_id, w_id);
				} else if (w_id == null && g_id != 0) {
					cReq.switchCustomerToGroup(c_id, g_id);
				}
				setResult(Config.RESULT_CODE_SWITCH_OK);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		finishActivity();
	}
	
	/***** event *****/
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		if (isConnect) {
			
		} else {
			finishActivity();
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}
}
