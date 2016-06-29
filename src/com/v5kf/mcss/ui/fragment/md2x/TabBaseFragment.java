package com.v5kf.mcss.ui.fragment.md2x;

import java.lang.ref.WeakReference;

import org.simple.eventbus.EventBus;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.chyrain.fragment.LazyFragment;
import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.R;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.ui.activity.MainTabActivity;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;

public abstract class TabBaseFragment extends LazyFragment {

	protected MainTabActivity mParentActivity;
	protected CustomApplication mApplication;
	protected AppInfoKeeper mAppInfo;
	protected FragmentHandler mHandler;
	protected int mIndex;
	
	protected static final int HDL_STOP_PROGRESS = 10;
	protected static final int HDL_STOP_REFRESH = 11;
	protected static final int HDL_STOP_LOAD = 12;
	protected static final int HDL_UPDATE_UI = 13;
	protected static final int HDL_TIME_OUT = 14;
	
	/* [修改]抽象方法改为普通方法 */
	protected abstract void handleMessage(Message msg, ActivityBase baseActivity);
	
	public TabBaseFragment() {
		// TODO Auto-generated constructor stub
	}
	
	public TabBaseFragment(MainTabActivity activity, int index) {
		this.mParentActivity = activity;
		this.mIndex = index;
		this.mApplication = (CustomApplication) mParentActivity.getApplication();
		this.mAppInfo = mApplication.getAppInfo();
		this.mHandler = new FragmentHandler(this);
	}
	
	@Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		if (mParentActivity == null) {
			mParentActivity = (MainTabActivity)getActivity();
		}
		
		// 注册event对象
        EventBus.getDefault().register(this);
	}
	
	@Override
	protected void onDestroyViewLazy() {
		super.onDestroyViewLazy();
		
		// 注销event对象
        EventBus.getDefault().unregister(this);
	}
	
	/**
	 * 获取图片主题色作为Toolbar色调
	 * @param bitmap
	 */
	protected void setPaletteColorForToolbar(Bitmap bitmap) {
		if (this.mParentActivity == null) {
			return;
		}
		mParentActivity.setToolbarColor(bitmap);
	}
	
	protected void onRefreshTimeOut() {
		mParentActivity.ShowToast(R.string.on_refresh_time_out);
	}
	
	/**
	 * 使用Handler传递消息
	 * @param msg
	 */
	protected void sendHandlerMessage(Message msg) {
		mHandler.sendMessage(msg);
	}
	
	/**
	 * 静态Handler类
	 * @author V5KF_MBP
	 *
	 */
	static class FragmentHandler extends Handler {
		
		WeakReference<TabBaseFragment> mFragment;
		
		public FragmentHandler(TabBaseFragment fragment) {
			mFragment = new WeakReference<TabBaseFragment>(fragment);
		}
		
		public void handleMessage(Message msg) {
			super.handleMessage(msg);			
			mFragment.get().handleMessage(msg, mFragment.get().mParentActivity);
		};
	}
}
