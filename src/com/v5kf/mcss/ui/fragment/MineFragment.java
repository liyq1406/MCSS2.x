package com.v5kf.mcss.ui.fragment;

import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.v5kf.client.ui.ClientChatActivity;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.ArchWorkerBean;
import com.v5kf.mcss.entity.WorkerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.ui.activity.BaseActivity;
import com.v5kf.mcss.ui.activity.WorkerTreeActivity;
import com.v5kf.mcss.ui.activity.info.SettingModeActivity;
import com.v5kf.mcss.ui.activity.info.SettingMoreActivity;
import com.v5kf.mcss.ui.activity.info.SettingStatusActivity;
import com.v5kf.mcss.ui.activity.info.WorkerLogActivity;
import com.v5kf.mcss.ui.activity.md2x.WorkerInfoActivity;
import com.v5kf.mcss.ui.widget.CircleImageView;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.cache.ImageLoader;

/**
 * @author chenhy
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-2 下午11:24:47
 * @package com.v5kf.mcss.ui.fragment.ChatFragment.java
 * @description
 *
 */
public class MineFragment extends BaseFragment implements View.OnClickListener {
	
	private static final String TAG = "MineFragment";
	private View mView;
	
	private RelativeLayout layout_myinfo, layout_set_more, layout_status, layout_mode, 
		layout_arch_workers, layout_consult;
	
	private TextView mStatusTv;
	private TextView mModeTv;
	
	private CircleImageView mPhotoIv;
	private TextView mNameTv;
	private TextView mFromTv;
	
	private TextView mWorkerServiceRightTv;
	
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    
    // 在线咨询消息广播
   	private ClientServiceReceiver mClientReceiver;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MineFragment newInstance(int sectionNumber) {
    	MineFragment fragment = new MineFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MineFragment() {
	}

    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {    	
    	if(mView != null){
            return mView;
        }
    	Logger.v(TAG, "onCreateView");
    	
    	
    	mView = inflater.inflate(R.layout.fragment_mine, container, false);

    	return mView;
    }

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	Logger.d(TAG, "onActivityCreated");
    	
    	mClientReceiver = new ClientServiceReceiver();
    	/* 注册广播接收 */
		IntentFilter filter=new IntentFilter();
		filter.addAction("com.v5kf.android.intent.action_message");
		mActivity.registerReceiver(mClientReceiver, filter);
    	
    	findView();
    	initView();
    	loadData();
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mActivity.unregisterReceiver(mClientReceiver);
	}
	
	private void findView() {
		layout_myinfo = (RelativeLayout) getView().findViewById(R.id.layout_myinfo);
		layout_set_more = (RelativeLayout) getView().findViewById(R.id.layout_set_more);
    	layout_status = (RelativeLayout) getView().findViewById(R.id.layout_status);
    	layout_mode = (RelativeLayout) getView().findViewById(R.id.layout_mode);
    	layout_arch_workers = (RelativeLayout) getView().findViewById(R.id.layout_arch_workers);
    	layout_consult = (RelativeLayout) getView().findViewById(R.id.layout_consult_service);
		
		mStatusTv = (TextView) getView().findViewById(R.id.id_mine_status_tv);
		mModeTv = (TextView) getView().findViewById(R.id.id_mine_mode_tv);
		
		mPhotoIv = (CircleImageView) getView().findViewById(R.id.id_my_photo);
		mNameTv = (TextView) getView().findViewById(R.id.tv_name);
		mFromTv = (TextView) getView().findViewById(R.id.id_from_tv);
		
		mWorkerServiceRightTv = (TextView) getView().findViewById(R.id.id_worker_service_right_tv);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	/**
     * 初始化界面
     */
    @SuppressLint("ResourceAsColor") 
    private void initView() {		
		mPhotoIv.setOnClickListener(this);
		layout_set_more.setOnClickListener(this);
		layout_status.setOnClickListener(this);
		layout_mode.setOnClickListener(this);
		layout_arch_workers.setOnClickListener(this);
		layout_consult.setOnClickListener(this);
		
		ImageLoader imgLoader = new ImageLoader(mActivity, true, R.drawable.v5_photo_default);
    	imgLoader.DisplayImage(mAppInfo.getUser().getPhoto(), mPhotoIv);
		mNameTv.setText(mAppInfo.getUser().getDefaultName());
		initFromGroup();
		
		// 设置状态和模式
		setStatusTvText(mAppInfo.getUser().getStatus());
		setModeTvText(mAppInfo.getUser().getMode());
		
		// 坐席日志
		if (Config.ENABLE_WORKER_LOG) {
			getView().findViewById(R.id.layout_worker_log).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.layout_log).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					mActivity.gotoActivity(WorkerLogActivity.class);
				}
			});
		}
    }
    
    private void initFromGroup() {
    	String from = null;
    	from = mAppInfo.getUser().getJob();
    	if (from == null || from.isEmpty()) {
    		ArchWorkerBean coWorker = mAppInfo.getCoWorker(mAppInfo.getUser().getW_id());
    		if (null != coWorker) {
    			from = coWorker.getGroup_name();
    		} else {
    			from = "";
    		}
    	}
    	
    	mFromTv.setText(from);
	}

	private void loadData() {
    	
    }
    
    private void setStatusTvText(int status) {
    	switch (status) {
    	case QAODefine.STATUS_OFFLINE:
    		mStatusTv.setText(R.string.status_offline);
    		break;
    		
    	case QAODefine.STATUS_ONLINE:
    		mStatusTv.setText(R.string.status_online);
    		break;
    		
    	case QAODefine.STATUS_HIDE:
    		mStatusTv.setText(R.string.status_hide);
    		break;
    		
    	case QAODefine.STATUS_LEAVE:
    		mStatusTv.setText(R.string.status_leave);
    		break;
    		
    	case QAODefine.STATUS_BUSY:
    		mStatusTv.setText(R.string.status_busy);
    		break;    		
    	}
    }
    
    private void setModeTvText(int mode) {
    	if (mode == QAODefine.MODE_AUTO) { // 自动接入
    		mModeTv.setText(getString(R.string.set_mode_auto)
    				+ "(" + mAppInfo.getUser().getAccepts() + ")");
    	} else if (mode == QAODefine.MODE_SWITCH_ONLY) { // 仅转接
    		mModeTv.setText(getString(R.string.set_mode_switchable)
    				+ "(" + mAppInfo.getUser().getConnects() + ")");
    	}
    }
  	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			if (resultCode == 1) {
				// 返回码
				
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_my_photo: // 个人信息
			Bundle bundle = new Bundle();
			bundle.putInt(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_ACTIVITY_START);
			bundle.putString(Config.EXTRA_KEY_W_ID, mAppInfo.getUser().getW_id());			
			Intent intent = new Intent(getActivity(), WorkerInfoActivity.class);
			intent.putExtras(bundle);
			startActivity(intent);
			mActivity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
			break;
		
		case R.id.layout_status: // 设置登录状态
			mActivity.gotoActivity(SettingStatusActivity.class);
			break;
			
		case R.id.layout_arch_workers: // 启动到企业架构页面
			mActivity.gotoActivity(WorkerTreeActivity.class);
			break;
			
		case R.id.layout_mode: // 设置接入模式
			mActivity.gotoActivity(SettingModeActivity.class);
			break;
			
		case R.id.layout_set_more: // 更多设置
			mActivity.gotoActivity(SettingMoreActivity.class);
			break;
		
		case R.id.layout_consult_service: // 客服
			mActivity.gotoActivity(ClientChatActivity.class);
			break;
		}
	}

	
	@Override
	protected void handleMessage(Message msg, BaseActivity baseActivity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onReceive(Context context, Intent intent) {
		if (!intent.getAction().equals(Config.ACTION_ON_MAINTAB)) {
			return;
		}
		
		Bundle bundle = intent.getExtras();
		int intent_type = bundle.getInt(Config.EXTRA_KEY_INTENT_TYPE);
		
		switch (intent_type) {		
		case Config.EXTRA_TYPE_CLEAR_CLIENT_MSG:
			mWorkerServiceRightTv.setText("");
			break;
		}
	}

	@Override
	public void onFragmentSelected() {
		Logger.d(TAG, "onFragmentSelected - MineFragment");
		updateUserInfo(mAppInfo.getUser());
	}
	
	class ClientServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (null == intent) {
				return;
			}
			if (intent.getAction().equals("com.v5kf.android.intent.action_message")) {
				mWorkerServiceRightTv.setText(R.string.new_client_message);
				mWorkerServiceRightTv.setTextColor(getResources().getColor(R.color.v5_badge_color));
			}
		}
	}
	
	/***** event *****/
	
	@Subscriber(tag = EventTag.ETAG_UPDATE_USER_INFO, mode = ThreadMode.MAIN)
	private void updateUserInfo(WorkerBean user) {
		Logger.d(TAG + "-eventbus", "updateUserInfo -> ETAG_UPDATE_USER_INFO");
		setStatusTvText(mAppInfo.getUser().getStatus());
		setModeTvText(mAppInfo.getUser().getMode());
		String photo = mApplication.getWorkerSp().readWorkerPhoto();
		if (!TextUtils.isEmpty(mAppInfo.getUser().getPhoto())) {
			photo = mAppInfo.getUser().getPhoto();
		}
		ImageLoader imgLoader = new ImageLoader(mActivity, true, R.drawable.v5_photo_default);
    	imgLoader.DisplayImage(photo, mPhotoIv);
		mNameTv.setText(mAppInfo.getUser().getDefaultName());
	}
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		if (isConnect) {
			setModeTvText(mAppInfo.getUser().getMode());
			setStatusTvText(mAppInfo.getUser().getStatus());
		} else {
			setStatusTvText(QAODefine.STATUS_OFFLINE);
		}
	}
}
