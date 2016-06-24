package com.v5kf.mcss.ui.activity.md2x;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import com.umeng.update.UmengUpdateAgent;
import com.v5kf.mcss.R;
import com.v5kf.mcss.ui.activity.MainTabActivity;

public abstract class BaseLoginActivity extends ActivityBase {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		setTheme(R.style.AppTheme_Normal);
		super.onCreate(savedInstanceState);
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
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
		UmengUpdateAgent.update(this); // 改用友盟自动更新SDK
		UmengUpdateAgent.setDeltaUpdate(false);
		UmengUpdateAgent.setUpdateCheckConfig(true);
	}
	
	@Override
	protected abstract void handleMessage(Message msg);

	@Override
	protected void onReceive(Context context, Intent intent) {}

}
