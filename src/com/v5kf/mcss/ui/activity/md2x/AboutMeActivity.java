package com.v5kf.mcss.ui.activity.md2x;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.v5kf.client.ui.ClientChatActivity;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.service.UpdateService;
import com.v5kf.mcss.utils.Logger;

public class AboutMeActivity extends BaseToolbarActivity implements OnClickListener {
	private static final String TAG = "AboutMeActivity";
	private TextView mNameTv, mAuthTv, mTokenTv, mVersionTv;
	private ViewGroup mHomeRl, mFeedbackRl, mHideRl, rl_service, rl_update;
	private ProgressBar mUpdateProgress;
	private CheckUpdateReceiver mUpdateReceiver;
	long[] mHits = new long[5];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_md2x_about);
		
		findView();
		initView();
		
		mUpdateReceiver = new CheckUpdateReceiver();
		/* 注册广播接收 */
		IntentFilter filter=new IntentFilter();
		filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		filter.addAction(Config.ACTION_ON_UPDATE);
		LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateReceiver, filter);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateReceiver);
	}
	
	private void findView() {
		mNameTv = (TextView) findViewById(R.id.id_version);
		mAuthTv = (TextView) findViewById(R.id.id_auth_tv);
		mTokenTv = (TextView) findViewById(R.id.id_token_tv);
		mFeedbackRl = (ViewGroup) findViewById(R.id.layout_feedback);
		mHomeRl = (ViewGroup) findViewById(R.id.layout_home);
		mHideRl = (ViewGroup) findViewById(R.id.id_hide_layout);
		rl_service = (ViewGroup) findViewById(R.id.layout_service);
		rl_update = (ViewGroup) findViewById(R.id.layout_update);
		
		mVersionTv = (TextView) findViewById(R.id.id_update_tv);
		mUpdateProgress = (ProgressBar) findViewById(R.id.id_update_progress);
	}

	private void initView() {
		mNameTv.setText(getString(R.string.app_name));
		mAuthTv.setText(mApplication.getWorkerSp().readAuthorization());
		mTokenTv.setText(mApplication.getDeviceToken());
		mVersionTv.setText(getVersion());
		
		mHomeRl.setOnClickListener(this);
		mFeedbackRl.setOnClickListener(this);
		rl_service.setOnClickListener(this);
		rl_update.setOnClickListener(this);
		
		mHomeRl.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				gotoWebViewActivity(Config.URL_ABOUT, R.string.app_name);
			}
		});
		mFeedbackRl.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				gotoWebViewActivity(Config.URL_FEED_BACK, R.string.app_feedback);
			}
		});
		findViewById(R.id.id_icon).setOnClickListener(new View.OnClickListener() {
			
            @Override
            public void onClick(View v) {
                //实现连续点击方法
                //src 拷贝的源数组
                //srcPos 从源数组的那个位置开始拷贝.
                //dst 目标数组
                //dstPos 从目标数组的那个位子开始写数据
                //length 拷贝的元素的个数
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
                //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于2000，即连续点击
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();
                if (mHits[0] >= (SystemClock.uptimeMillis() - 2000)) {
                	if (mHideRl.getVisibility() == View.GONE) {
                		mHideRl.setVisibility(View.VISIBLE);
                	} else {
                		mHideRl.setVisibility(View.GONE);
                	}
                	mHits = new long[5];
                }
            }
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.layout_home:
			gotoWebViewActivity(Config.URL_ABOUT, R.string.app_name);
			break;
		case R.id.layout_service: // 在线咨询
			this.gotoActivity(ClientChatActivity.class);
			break;
		case R.id.layout_feedback: // 用户反馈
			this.gotoWebViewActivity(Config.URL_FEED_BACK, R.string.app_feedback);
			break;
		case R.id.layout_update: // 检查更新
			mVersionTv.setVisibility(View.GONE);
			mUpdateProgress.setVisibility(View.VISIBLE);
			int level = mApplication.getWorkerSp().readInt("update_level");
			if (level == 0) { // 查询获取使用哪家更新服务
				if (Config.ENABLE_UMENG_UPDATE) {
					UmengUpdateAgent.forceUpdate(this);
				}
				Intent i = new Intent(this, UpdateService.class);
				i.putExtra("check_manual", true);
				this.startService(i);
			} else if (level == 2) { // 采用友盟自动更新
				if (Config.ENABLE_UMENG_UPDATE) {
					UmengUpdateAgent.forceUpdate(this);
				} else { // 友盟更新关闭则只能采用自家更新
					Intent i = new Intent(this, UpdateService.class);
					i.putExtra("check_manual", true);
					this.startService(i);
				}
			} else if (level >= 3) { // 采用自家更新服务
				Intent i = new Intent(this, UpdateService.class);
				i.putExtra("check_manual", true);
				startService(i);
			}
//			if (Config.ENABLE_UMENG_UPDATE) {
//				UmengUpdateAgent.forceUpdate(this);
//			} else {
//				Intent i = new Intent(this, UpdateService.class);
//				this.startService(i);
//			}
			MobclickAgent.onEvent(this, "CHECK_UPDATE");
			
			break;
		}
	}

	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	public String getVersion() {
	    return this.getString(R.string.app_version_name) + mApplication.getVersion();
	}
	
	@Override
	protected void handleMessage(Message msg) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

	}
	
	/****** Update Broadcast receiver ******/
	class CheckUpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (null == intent) {
				return;
			}
			if (intent.getAction().equals(Config.ACTION_ON_UPDATE)) {
				Bundle bundle = intent.getExtras();
				int intent_type = bundle.getInt(Config.EXTRA_KEY_INTENT_TYPE);
				switch (intent_type) {
				case Config.EXTRA_TYPE_UP_ENABLE:
					// 显示确认更新对话框
					String version = bundle.getString("version");				
					String displayMessage = bundle.getString("displayMessage");
					Logger.i(TAG, "【新版特性】：" + displayMessage);
					
					mVersionTv.setVisibility(View.VISIBLE);
					mUpdateProgress.setVisibility(View.GONE);
					mVersionTv.setText(String.format(getString(R.string.has_new), version));
					
					//showUpdateInfoDialog(version, displayMessage);
					break;
					
				case Config.EXTRA_TYPE_UP_NO_NEWVERSION:
					mVersionTv.setVisibility(View.VISIBLE);
					mUpdateProgress.setVisibility(View.GONE);
					mVersionTv.setText(R.string.already_new);
					break;
				
				case Config.EXTRA_TYPE_UP_DOWNLOAD_FINISH:
					// 显示确认安装对话框
					//showInstallConfirmDialog();
					break;
					
				case Config.EXTRA_TYPE_UP_FAILED: // 更新失败
					mVersionTv.setVisibility(View.VISIBLE);
					mUpdateProgress.setVisibility(View.GONE);
					mVersionTv.setText(R.string.update_failed);
					break;
				}
			}
		}
	}

}
