package com.v5kf.mcss.ui.activity.md2x;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import com.umeng.update.UmengUpdateAgent;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.manage.update.VersionInfo;
import com.v5kf.mcss.service.UpdateService;
import com.v5kf.mcss.ui.activity.MainTabActivity;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;

public abstract class BaseLoginActivity extends ActivityBase {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.AppTheme_Normal);
		super.onCreate(savedInstanceState);
		
		setNavigationBarColor(UITools.getColor(R.color.v5_navigation_bar_bg));
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	protected void gotoMainTabActivity() {
		// TODO 改成TabMainActivity
//		gotoActivityAndFinishThis(MainTabActivity.class);		
		gotoActivityAndFinishThis(MainTabActivity.class);		
	}
	
	/**
	 * 开启友盟自动更新
	 */
	protected void startUpdateService() {
		int level = mApplication.getWorkerSp().readInt("update_level");
		Logger.d("BaseLoginActivity", "Update level:" + level);
		if (level == 0) { // 查询获取使用哪家更新服务 默认0，1则不自动更新
			if (Config.ENABLE_UMENG_UPDATE) {
				UmengUpdateAgent.update(this); // 改用友盟自动更新SDK
				UmengUpdateAgent.setDeltaUpdate(false);
				UmengUpdateAgent.setUpdateCheckConfig(true);
			}
			Intent i = new Intent(this, UpdateService.class);
			startService(i);
		} else if (level == 2) { // 采用友盟自动更新
			if (Config.ENABLE_UMENG_UPDATE) {
				UmengUpdateAgent.update(this); // 改用友盟自动更新SDK
				UmengUpdateAgent.setDeltaUpdate(false);
				UmengUpdateAgent.setUpdateCheckConfig(true);
			} else { // 友盟更新关闭则只能采用自家更新
				Intent i = new Intent(this, UpdateService.class);
				startService(i);
			}
		} else if (level >= 3) { // 采用自家更新服务
			Intent i = new Intent(this, UpdateService.class);
			startService(i);
		}
	}
	
	@Override
	protected abstract void handleMessage(Message msg);

	@Override
	protected void onReceive(Context context, Intent intent) {
		if (intent == null) {
			return;
		}
		Logger.d("BaseLoginActivity", "[onReceive] " + intent.getAction());
		if (intent.getAction().equals(Config.ACTION_ON_UPDATE)) {
			Bundle bundle = intent.getExtras();
			int intent_type = bundle.getInt(Config.EXTRA_KEY_INTENT_TYPE);
			switch (intent_type) {
			case Config.EXTRA_TYPE_UP_ENABLE:
				// 显示确认更新对话框
//				String version = bundle.getString("version");				
//				String displayMessage = bundle.getString("displayMessage");
//				Logger.i(TAG, "【新版特性】：" + displayMessage);
				VersionInfo versionInfo = (VersionInfo) bundle.getSerializable("versionInfo");
				if (isForeground) {
					alertUpdateInfo(versionInfo);
				}
				break;
			}
		}		
	}

}
