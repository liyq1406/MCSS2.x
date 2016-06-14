package com.v5kf.mcss.ui.fragment.md2x;

import android.os.Bundle;
import android.os.Message;

import com.v5kf.mcss.R;
import com.v5kf.mcss.ui.activity.MainTabActivity;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.utils.Logger;

public class TabSecondFragment extends TabBaseFragment {

	private static final String TAG = "TabSecondFragment";

	public TabSecondFragment(MainTabActivity tabMainActivity, int index) {
		super(tabMainActivity, index);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		setContentView(R.layout.fragment_tabmain_2);

		Logger.d(TAG, "Fragment 将要创建View " + this);
	}

	@Override
	protected void onResumeLazy() {
		super.onResumeLazy();
		Logger.d(TAG, "Fragment所在的Activity onResume, onResumeLazy " + this);
	}

	@Override
	protected void onFragmentStartLazy() {
		super.onFragmentStartLazy();
		Logger.d(TAG, "Fragment 显示 " + this);
		//this.mParentActivity.hideToolbar();
//		this.mParentActivity.hideFab();
//		int color = UITools.getColor(android.R.color.holo_orange_light);
//		this.mParentActivity.setBarColor(color);
//		//
//		this.mParentActivity.setStatusbarColor(color);
	}

	@Override
	protected void onFragmentStopLazy() {
		super.onFragmentStopLazy();
		Logger.d(TAG, "Fragment 掩藏 " + this);
	}

	@Override
	protected void onPauseLazy() {
		super.onPauseLazy();
		Logger.d(TAG, "Fragment所在的Activity onPause, onPauseLazy " + this);
	}

	@Override
	protected void onDestroyViewLazy() {
		super.onDestroyViewLazy();
		Logger.d(TAG, "Fragment View将被销毁 " + this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Logger.d(TAG, "Fragment 所在的Activity onDestroy " + this);
	}

	@Override
	protected void handleMessage(Message msg, ActivityBase baseActivity) {
		// TODO Auto-generated method stub
		
	}

}
