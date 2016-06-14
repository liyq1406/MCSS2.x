package com.v5kf.mcss.ui.fragment.md2x;

import java.io.File;

import org.simple.eventbus.EventBus;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;
import com.v5kf.client.lib.DBHelper;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.Config.AppStatus;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.ui.activity.MainTabActivity;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.ui.activity.md2x.WebViewActivity;
import com.v5kf.mcss.ui.widget.CheckboxDialog;
import com.v5kf.mcss.ui.widget.CheckboxDialog.CheckboxDialogListener;
import com.v5kf.mcss.ui.widget.SlideSwitchView;
import com.v5kf.mcss.ui.widget.SlideSwitchView.OnSwitchChangedListener;
import com.v5kf.mcss.ui.widget.WarningDialog;
import com.v5kf.mcss.ui.widget.WarningDialog.WarningDialogListener;
import com.v5kf.mcss.utils.DbUtil;
import com.v5kf.mcss.utils.FileUtil;
import com.v5kf.mcss.utils.IntentUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.SharePreferenceUtil;
import com.v5kf.mcss.utils.WorkerSP;
import com.v5kf.mcss.utils.cache.ImageLoader;
import com.v5kf.mcss.utils.cache.MediaLoader;
import com.v5kf.mcss.utils.cache.URLCache;

/**
 * @author chenhy
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-2 下午11:24:47
 * @package com.v5kf.mcss.ui.fragment.HistoryVisitorFragment.java
 * @description
 *
 */
public class TabMoreFragment extends TabBaseFragment implements OnClickListener, OnSwitchChangedListener {
	
	private static final String TAG = "TabMoreFragment";
	private static final int HDL_UPDATE_CACHE = 2;
	
	private View view1, view2; // view0, Switch开关所在Layout的两个分割线
	
	@SuppressWarnings("unused")
	private RelativeLayout rl_autoboot, rl_switch_voice, rl_switch_vibrate, 
		rl_feedback, rl_about, rl_update, rl_clearcache, rl_refresh; //rl_switch_notification_wait
	private SlideSwitchView  mSwitchAutoBoot, mSwitchAutoLogin, mSwitchNotification,
		mSwitchVoice, mSwitchVibrate; // mSwitchWaitNotification
	private TextView mVersionTv;
	private TextView mCacheSizeTv;
	private float mCacheSize;
	private ProgressBar mUpdateProgress;
	
	private SharePreferenceUtil mSharedUtil;
	private WorkerSP mWsp;
	
    public TabMoreFragment(MainTabActivity activity, int index) {
		super(activity, index);
	}

    @Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		setContentView(R.layout.fragment_more);

		Logger.d(TAG, TAG + " 将要创建View " + this);
		initData();
		findView();
		initView();
		initUmengUpdate();
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
    
	
	private void initUmengUpdate() {
//		UmengUpdateAgent.setUpdateAutoPopup(false);
		UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

			@Override
			public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
				Logger.i(TAG, "onUpdateReturned:" + updateStatus);
				switch (updateStatus) {
		        case UpdateStatus.Yes: // has update
					mVersionTv.setText(String.format(getString(R.string.has_new), updateInfo.version));
//		            UmengUpdateAgent.showUpdateDialog(SettingMoreActivity.this, updateInfo);
		            break;
		        case UpdateStatus.No: // has no update
		        	mVersionTv.setText(R.string.already_new);
		            break;
		        case UpdateStatus.NoneWifi: // none wifi
		        	mVersionTv.setText(R.string.has_new);
//		        	UmengUpdateAgent.showUpdateDialog(SettingMoreActivity.this, updateInfo);
		            break;
		        case UpdateStatus.Timeout: // time out
					mVersionTv.setText(R.string.update_failed);
		            break;
		        }
				mVersionTv.setVisibility(View.VISIBLE);
				mUpdateProgress.setVisibility(View.GONE);
			}
		});
	}
	
	private void initData() {
		mSharedUtil = mApplication.getSpUtil();
		mWsp = mApplication.getWorkerSp();
	}

	private void findView() {
//		rl_switch_notification_wait = (RelativeLayout) findViewById(R.id.rl_switch_notification_wait);
		rl_switch_voice = (RelativeLayout) findViewById(R.id.rl_switch_voice);
		rl_switch_vibrate = (RelativeLayout) findViewById(R.id.rl_switch_vibrate);
		rl_feedback = (RelativeLayout) findViewById(R.id.layout_feedback);
		rl_about = (RelativeLayout) findViewById(R.id.layout_about);
		rl_update = (RelativeLayout) findViewById(R.id.layout_update);
		rl_clearcache = (RelativeLayout) findViewById(R.id.layout_clear_cache);
		rl_refresh = (RelativeLayout) findViewById(R.id.layout_refresh);
		mVersionTv = (TextView) findViewById(R.id.id_update_tv);
		mCacheSizeTv = (TextView) findViewById(R.id.id_cache_size_tv);
		mUpdateProgress = (ProgressBar) findViewById(R.id.id_update_progress);

		mSwitchAutoBoot = (SlideSwitchView) findViewById(R.id.switch_auto_boot);
		mSwitchAutoLogin = (SlideSwitchView) findViewById(R.id.switch_auto_login);
//		mSwitchWaitNotification = (SlideSwitchView) findViewById(R.id.switch_notification_wait);
		mSwitchNotification = (SlideSwitchView) findViewById(R.id.switch_notification);
		mSwitchVoice = (SlideSwitchView) findViewById(R.id.switch_voice);
		mSwitchVibrate = (SlideSwitchView) findViewById(R.id.switch_vibrate);
		
		mSwitchAutoBoot.setOnChangeListener(this);
		mSwitchAutoLogin.setOnChangeListener(this);
		mSwitchNotification.setOnChangeListener(this);
//		mSwitchWaitNotification.setOnChangeListener(this);
		mSwitchVoice.setOnChangeListener(this);
		mSwitchVibrate.setOnChangeListener(this);
		
		rl_feedback.setOnClickListener(this);
		rl_about.setOnClickListener(this);
		rl_update.setOnClickListener(this);
		rl_clearcache.setOnClickListener(this);
		rl_refresh.setOnClickListener(this);
		
//		view0 = (View) findViewById(R.id.view0);
		view1 = (View) findViewById(R.id.view1);
		view2 = (View) findViewById(R.id.view2);
	}
	
	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	public String getVersion() {
	    try {
	        PackageManager manager = mParentActivity.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(mParentActivity.getPackageName(), 0);
	        String version = info.versionName;
	        return this.getString(R.string.app_version_name) + version;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return this.getString(R.string.can_not_find_version_name);
	    }
	}

	private void initView() {
		mVersionTv.setText(getVersion());
		updateCacheSize();
		
		// 初始化
		boolean isAllowAutoBoot = mSharedUtil.isAllowAutoboot();
		mSwitchAutoBoot.setChecked(isAllowAutoBoot);
		
		boolean isAllowAutoLogin = mWsp.readAutoLogin();
		mSwitchAutoLogin.setChecked(isAllowAutoLogin);
		
		boolean isAllowNotify = mSharedUtil.isAllowPushNotify();
		if (isAllowNotify) {
			mSwitchNotification.setChecked(true);
//			rl_switch_notification_wait.setVisibility(View.VISIBLE);
			rl_switch_vibrate.setVisibility(View.VISIBLE);
			rl_switch_voice.setVisibility(View.VISIBLE);
			view1.setVisibility(View.VISIBLE);
			view2.setVisibility(View.VISIBLE);
		} else {
			mSwitchNotification.setChecked(false);
//			rl_switch_notification_wait.setVisibility(View.GONE);
			rl_switch_vibrate.setVisibility(View.GONE);
			rl_switch_voice.setVisibility(View.GONE);
			view1.setVisibility(View.GONE);
			view2.setVisibility(View.GONE);
		}
		
//		boolean isAllowNotifyWait = mSharedUtil.isAllowPushWaitNotify();
//		mSwitchWaitNotification.setChecked(isAllowNotifyWait);
		
		boolean isAllowVoice = mSharedUtil.isAllowVoice();
		mSwitchVoice.setChecked(isAllowVoice);
		
		boolean isAllowVibrate = mSharedUtil.isAllowVibrate();
		mSwitchVibrate.setChecked(isAllowVibrate);
	}
	
	private void updateCacheSize() {
		new Thread(new Runnable() { // 获取缓存目录大小
			
			@Override
			public void run() {
				long folder1 = FileUtil.getFolderSize(new File(FileUtil.getOldImageCachePath(getApplicationContext())));
				long folder2 = FileUtil.getFolderSize(new File(FileUtil.getImageCachePath(getApplicationContext())));
				long folder3 = FileUtil.getFolderSize(new File(FileUtil.getMediaCachePath(getApplicationContext())));
				Logger.i(TAG, "Dir:" + FileUtil.getOldImageCachePath(getApplicationContext()) + " size:" + folder1);
				Logger.i(TAG, "Dir:" + FileUtil.getImageCachePath(getApplicationContext()) + " size:" + folder2);
				Logger.i(TAG, "Dir:" + FileUtil.getMediaCachePath(getApplicationContext()) + " size:" + folder3);
				mCacheSize = (folder1 + folder2 + folder3) / (1024.0f * 1024.0f);
				mHandler.obtainMessage(HDL_UPDATE_CACHE).sendToTarget();
			}
		}).start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.layout_feedback: // 用户反馈
			Intent iFb = IntentUtil.getStartWebViewIntent(mParentActivity, WebViewActivity.class, 
					Config.URL_FEED_BACK, R.string.app_feedback);
			mParentActivity.gotoActivity(iFb);
			break;
			
		case R.id.layout_about: // 关于
			Intent iAb = IntentUtil.getStartWebViewIntent(mParentActivity, WebViewActivity.class, 
					Config.URL_ABOUT, R.string.app_about);
			mParentActivity.gotoActivity(iAb);
			break;
			
		case R.id.layout_update: // 检查更新
			mVersionTv.setVisibility(View.GONE);
			mUpdateProgress.setVisibility(View.VISIBLE);
			UmengUpdateAgent.forceUpdate(mParentActivity);
			MobclickAgent.onEvent(mParentActivity, "CHECK_UPDATE");
			
			break;
			
		case R.id.layout_clear_cache: // 清空缓存
			CheckboxDialog checkDlg = new CheckboxDialog(mParentActivity);
			checkDlg.setCheckMode(CheckboxDialog.MODE_FOUR_BUTTON);
			checkDlg.setDefaultChecked(new boolean[]{true, true, false, false});
			int[] titles = new int[]{R.string.check_media_cache, R.string.check_memory_cache, R.string.check_db_cache, R.string.check_login_cache};
			checkDlg.setCheckTitle(titles);
			checkDlg.setOnClickListener(new CheckboxDialogListener() {
				@Override
				public void onClick(View view, final boolean[] checks) {
					MobclickAgent.onEvent(mParentActivity, "APP_CLEAR_CACHE");
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							if (checks[0]) {
								ImageLoader imgLoader = new ImageLoader(mApplication, true, 0, null);
								imgLoader.clearCache();
								MediaLoader mediaLoader = new MediaLoader(getApplicationContext());
								mediaLoader.clearCache();
								URLCache urlCache = new URLCache();
								urlCache.clear();
								
								// 删除old imageCache
								FileUtil.deleteFile(FileUtil.getOldImageCachePath(getApplicationContext()));
								// 删除mediaCache
								FileUtil.deleteFile(FileUtil.getMediaCachePath(getApplicationContext()));
								// 删除imageCache
								FileUtil.deleteFile(FileUtil.getImageCachePath(getApplicationContext()));
								Logger.d(TAG, "cache - clear img|media cache");
							}
							if (checks[1]) {
								mAppInfo.clearMemory();
								Logger.d(TAG, "cache - clear memory cache");
							}
							if (checks[2]) {
								DbUtil.clearDb(getApplicationContext());
								DBHelper dbHelper = new DBHelper(getApplicationContext());
								dbHelper.delAll();
								Logger.d(TAG, "cache - clear db cache");
							}
							if (checks[3]) {
								DbUtil.clearCache(getApplicationContext());
								mWsp.clearLoginCache();
								Logger.d(TAG, "cache - clear login cache");
							}
							updateCacheSize();
						}
					}).start();
				}
			});
			checkDlg.show();
			
//			showConfirmDialog(
//				R.string.confirm_clear_cache, 
//				WarningDialog.MODE_TWO_BUTTON, 
//				new WarningDialogListener() {
//					@Override
//					public void onClick(View view) {
//						switch (view.getId()) {
//						case R.id.btn_dialog_warning_left:
//							
//							break;
//						case R.id.btn_dialog_warning_right:
//							ImageLoader imgLoader = new ImageLoader(mApplication, true, 0);
//							imgLoader.clearCache();
//							DbUtil.clearCache(getApplicationContext());
//							dismissWarningDialog();
//							MobclickAgent.onEvent(SettingMoreActivity.this, "APP_CLEAR_CACHE");
//							break;
//						}
//					}
//				});
			break;
			
		case R.id.layout_refresh:
			mParentActivity.showConfirmDialog(
				R.string.confirm_refresh, 
				WarningDialog.MODE_TWO_BUTTON, 
				new WarningDialogListener() {
					@Override
					public void onClick(View view) {
						switch (view.getId()) {
						case R.id.btn_dialog_warning_left:
							
							break;
						case R.id.btn_dialog_warning_right:
							// [eventbus][强制重连]
							mAppInfo.clearRunTimeInfo();
							mApplication.setAppStatus(AppStatus.AppStatus_Init);
							mParentActivity.dismissWarningDialog();
							EventBus.getDefault().post(Boolean.valueOf(true), EventTag.ETAG_ON_LINE);
							MobclickAgent.onEvent(mParentActivity, "APP_REFRESH");
							break;
						}
					}
				});			
			break;
		}
	}

	@Override
	public void onSwitchChange(SlideSwitchView switchView, boolean isChecked) {
		switch (switchView.getId()) {
		case R.id.switch_notification:
			mSharedUtil.setPushNotifyEnable(isChecked);
			if (isChecked) {
//				rl_switch_notification_wait.setVisibility(View.VISIBLE);
				rl_switch_vibrate.setVisibility(View.VISIBLE);
				rl_switch_voice.setVisibility(View.VISIBLE);
				view1.setVisibility(View.VISIBLE);
				view2.setVisibility(View.VISIBLE);
			} else {
//				rl_switch_notification_wait.setVisibility(View.GONE);
				rl_switch_vibrate.setVisibility(View.GONE);
				rl_switch_voice.setVisibility(View.GONE);
				view1.setVisibility(View.GONE);
				view2.setVisibility(View.GONE);
			}
			break;
		
//		case R.id.switch_notification_wait:
//			mSharedUtil.setPushWaitNotifyEnable(isChecked);
//			break;
			
		case R.id.switch_voice:
			mSharedUtil.setAllowVoiceEnable(isChecked);
			break;
			
		case R.id.switch_vibrate:
			mSharedUtil.setAllowVibrateEnable(isChecked);
			break;
			
		case R.id.switch_auto_boot:
			mSharedUtil.setAllowAutoboot(isChecked);
			break;

		case R.id.switch_auto_login:
			mWsp.saveAutoLogin(isChecked);
			break;
		}
	}


	@Override
	protected void handleMessage(Message msg, ActivityBase baseActivity) {
		switch (msg.what) {
		case HDL_UPDATE_CACHE:
			mCacheSizeTv.setText(String.format("%.1fMB", mCacheSize));
			break;

		default:
			break;
		}
	}
	
/***** event *****/
	
//	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
//	private void connectionChange(Boolean isConnect) {
//		if (isConnect) {
//			
//		} else {
////			finishActivity();
//		}
//	}
	
}