package com.v5kf.mcss.ui.fragment;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import org.simple.eventbus.EventBus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;

import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.ui.activity.BaseActivity;
import com.v5kf.mcss.ui.activity.V51xTabActivity;
import com.v5kf.mcss.utils.Logger;

public abstract class BaseFragment extends Fragment {
	
	protected CustomApplication mApplication;
	protected AppInfoKeeper mAppInfo;
	protected BroadcastReadReceiver mReceiver;
	protected FragmentHandler mHandler;
	protected BaseActivity mActivity;
	
	protected static final int HDL_STOP_REFRESH = 11;
	protected static final int HDL_STOP_LOAD = 12;
	protected static final int HDL_UPDATE_UI = 13;
	
	/* [修改]抽象方法改为普通方法 */
	protected void handleMessage(Message msg, BaseActivity baseActivity){};
	protected void onReceive(Context context, Intent intent){};
	
	public BaseFragment() {
	}	
	
	@Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	if (activity instanceof V51xTabActivity) {
    		mActivity = (V51xTabActivity) activity;
    	} else {
    		mActivity = (BaseActivity) activity;
    		Logger.w("**BaseFragment**", "onAttach activity is not instanceof MainTabActivity and HomeTabActivity");
    	}
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mReceiver == null) {
			mReceiver = new BroadcastReadReceiver();
		}
		if (mHandler == null) {
			mHandler = new FragmentHandler(this);
		}
		mApplication = (CustomApplication) getActivity().getApplication();
		mAppInfo = mApplication.getAppInfo();
		initReceiver();
		
		// 注册event对象
        EventBus.getDefault().register(this);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	protected void initReceiver() {
		/* 注册广播接收 */
		IntentFilter filter=new IntentFilter();
		filter.addAction(Config.ACTION_ON_MAINTAB);
		LocalBroadcastManager.getInstance(mActivity).registerReceiver(mReceiver, filter);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	    try {
	    	Field childFragmentManager = Fragment.class
	    			.getDeclaredField("mChildFragmentManager");
	    	childFragmentManager.setAccessible(true);
	    	childFragmentManager.set(this, null);
	    } catch (NoSuchFieldException e) {
	    	throw new RuntimeException(e);
	    } catch (IllegalAccessException e) {
	    	throw new RuntimeException(e);
	    }
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mReceiver);
		
		// 注销event对象
        EventBus.getDefault().unregister(this);
	}
	
	public abstract void onFragmentSelected();
	
//	/**
//	 * 发送请求到Websocket
//	 * @param request字符串
//	 */
//	protected void sendRequest(String request) {
//		Intent intent = new Intent(Config.ACTION_ON_WBSOCKET);
//		intent.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_MSG_WRITE);
//		// 将请求内容放到字符串数组广播给websocketService
//		intent.putExtra(Config.EXTRA_KEY_MSG_WRITE, request);
//		getActivity().sendBroadcast(intent);
//	}
	
	
	public void sendHandlerMessage(Message msg) {
		if (mHandler == null) {
			Logger.w("BaseFragment", "mHandler == null !");
			mHandler = new FragmentHandler(this);
		}
		mHandler.sendMessage(msg);
	}
	
	
	static class FragmentHandler extends Handler {
		
		WeakReference<BaseFragment> mFragment;
		
		public FragmentHandler(BaseFragment fragment) {
			mFragment = new WeakReference<BaseFragment>(fragment);
		}
		
		public void handleMessage(Message msg) {
			super.handleMessage(msg);			
			mFragment.get().handleMessage(msg, (BaseActivity) mFragment.get().mActivity);
		};
	}
	
	
	class BroadcastReadReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			BaseFragment.this.onReceive(context, intent);
		}		
	}

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
}
