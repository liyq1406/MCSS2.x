package com.v5kf.mcss.ui.activity;

import java.lang.ref.WeakReference;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.SwipeBackLayout.SwipeListener;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import org.simple.eventbus.EventBus;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.ui.activity.md2x.ShowImageActivity;
import com.v5kf.mcss.ui.view.SystemBarTintManager;
import com.v5kf.mcss.ui.widget.AlertDialog;
import com.v5kf.mcss.ui.widget.CustomOptionDialog;
import com.v5kf.mcss.ui.widget.CustomOptionDialog.OptionDialogListener;
import com.v5kf.mcss.ui.widget.CustomProgressDialog;
import com.v5kf.mcss.ui.widget.WarningDialog;
import com.v5kf.mcss.ui.widget.WarningDialog.WarningDialogListener;
import com.v5kf.mcss.utils.Logger;

public abstract class ActivityBase extends SwipeBackActivity {
	
	protected CustomApplication mApplication;
	protected AppInfoKeeper mAppInfo;
	protected BaseHandler mHandler;
	protected WebsocktReadReceiver mReceiver;
	protected AlertDialog mAlertDialog;
	protected WarningDialog mWarningDialog;
	protected CustomOptionDialog mOptionDialog;
	protected CustomProgressDialog mProgressDialog;
	protected Toast mToast;

	/* Layout动画 */
	protected Animation mSlideIn; // Y轴出现
	protected Animation mSlideOut; // Y轴消失
	
	/* [修改]抽象方法改为普通方法，子类选择性实现即可 */
	protected abstract void handleMessage(Message msg);
	protected void onReceive(Context context, Intent intent){};
	
	@Override
    public void onBackPressed() {
		Logger.i("ActivityBase", "onBackPressed");
		finishActivity();
    }
	
	protected void onSwipeBackDone() {
		// TODO
	}
	
	public BaseHandler getHandler() {
		return mHandler;
	}
	
	protected void initReceiver() {
		/* 注册广播接收 */
		IntentFilter filter=new IntentFilter();
		filter.addAction(Config.ACTION_ON_MAINTAB);
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
	}
	
	public void sendLocalBroadcast(Intent intent) {
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP) @Override
	protected void onCreate(Bundle savedInstanceState) {
		Logger.d("LifeCycle", "---onCreate---");
		super.onCreate(savedInstanceState);
//		setSwipeBackEnable(false);
		
		mApplication = (CustomApplication)getApplication();
		mAppInfo = mApplication.getAppInfo();		
		mHandler = new BaseHandler(this);
		mReceiver = new WebsocktReadReceiver();
		
		/* 注册广播接收 */
		initReceiver();
		mApplication.addActivity(this);
		
		/* 在API19以上改变状态栏颜色 */
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        	Window window = getWindow();
//			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
////							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(UITools.getColor(this, R.color.base_status_bar_bg));
////            window.setStatusBarColor(Color.TRANSPARENT);
////            window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(true);
			
			SystemBarTintManager tintManager = new SystemBarTintManager(this);  
			tintManager.setStatusBarTintEnabled(true);  
			tintManager.setStatusBarTintResource(R.color.base_status_bar_bg);
		}
		
		// TODO 取消滑动返回
        getSwipeBackLayout().addSwipeListener(new SwipeListener() {
			@Override
			public void onScrollStateChange(int state, float scrollPercent) {
				if (state == SwipeBackLayout.STATE_SETTLING) {
					onSwipeBackDone();
				} else if (state == SwipeBackLayout.STATE_DRAGGING) { // 开始拖动
					
				} else if (state == SwipeBackLayout.STATE_IDLE) {
					
				}
			}
			
			@Override
			public void onScrollOverThreshold() {
				
			}
			
			@Override
			public void onEdgeTouch(int edgeFlag) {
				
			}
		});
        
     // 注册event对象
        EventBus.getDefault().register(this);
	}
	
	
	@Override
	protected void onStart() {
		Logger.d("LifeCycle", "onStart");
		mApplication.setAppForeground();
		super.onStart();		
	}
	
	
	@Override
	protected void onResume() {
		Logger.v("LifeCycle", "onResume");
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
	
	@Override
	protected void onPause() {
		Logger.v("LifeCycle", "onPause");
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	
	@Override
	protected void onStop() {
		Logger.v("LifeCycle", "onStop");
		mApplication.setAppBackground();
		super.onStop();
	}
	
	
	@Override
	protected void onRestart() {
		Logger.v("LifeCycle", "onRestart");
		super.onRestart();
	}
	
	
	@Override
	protected void onDestroy() {
		Logger.d("LifeCycle", "---onDestroy---");
		super.onDestroy();
		mApplication.removeActivity(this);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
		
		// 注销event对象
        EventBus.getDefault().unregister(this);
	}
	
	
	@TargetApi(19) 
	protected void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}
	

	public void ShowToast(final String text) {
		if (!TextUtils.isEmpty(text)) {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if (mToast == null) {
						mToast = Toast.makeText(getApplicationContext(), text,
								Toast.LENGTH_LONG);
					} else {
						mToast.setText(text);
					}
					mToast.show();
				}
			});
			
		}
	}

	public void ShowToast(final int resId) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (mToast == null) {
					mToast = Toast.makeText(ActivityBase.this.getApplicationContext(), resId,
							Toast.LENGTH_LONG);
				} else {
					mToast.setText(resId);
				}
				mToast.show();
			}
		});
	}
	public void ShowShortToast(final int resId) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (mToast == null) {
					mToast = Toast.makeText(ActivityBase.this.getApplicationContext(), resId,
							Toast.LENGTH_SHORT);
				} else {
					mToast.setText(resId);
				}
				mToast.show();
			}
		});
	}

	public void ShowShortToast(final String text) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (mToast == null) {
					mToast = Toast.makeText(ActivityBase.this.getApplicationContext(), text,
							Toast.LENGTH_SHORT);
				} else {
					mToast.setText(text);
				}
				mToast.show();
			}
		});
	}
	
	public void showWarningDialog(int contentResId, WarningDialogListener listener) {
		if(null == mWarningDialog) {
			mWarningDialog = new WarningDialog(this);
		}
		mWarningDialog.setDialogMode(WarningDialog.MODE_ONE_BUTTON);
		mWarningDialog.setContent(contentResId);
		mWarningDialog.setContentViewGravity(Gravity.CENTER);
		mWarningDialog.setOnClickListener(listener);
		
		mWarningDialog.show();
	}
	
	public void showConfirmDialog(int contentResId, int mode, WarningDialogListener listener) {
		if(null == mWarningDialog) {
			mWarningDialog = new WarningDialog(this);
		}
		mWarningDialog.setDialogMode(mode);
		mWarningDialog.setContent(contentResId);
		mWarningDialog.setContentViewGravity(Gravity.CENTER);
		mWarningDialog.setOnClickListener(listener);
		
		mWarningDialog.show();
	}
	
	
	public void dismissWarningDialog() {
		if(mWarningDialog != null && mWarningDialog.isShowing()) {
			mWarningDialog.dismiss();
		}
	}
	
	
	public void showOptionDialogInSession(boolean inTrust, OptionDialogListener listener) {
		if(null == mOptionDialog) {
			mOptionDialog = new CustomOptionDialog(this);
		}
		mOptionDialog.setDisplayTypeServing(inTrust);
		mOptionDialog.setOnClickListener(listener);
		
		mOptionDialog.show();
		
	}
	
	public void showOptionDialogInWaiting(OptionDialogListener listener) {
		if(null == mOptionDialog) {
			mOptionDialog = new CustomOptionDialog(this);
		}
		mOptionDialog.setDisplayTypeWaiting();
		mOptionDialog.setOnClickListener(listener);
		
		mOptionDialog.show();
	}
	
	public void showOptionDialogInChat(int optionType, OptionDialogListener listener) {
		showOptionDialog(optionType, listener);
	}
	
	public void showOptionDialog(int type, OptionDialogListener listener) {
		if(null == mOptionDialog) {
			mOptionDialog = new CustomOptionDialog(this);
		}
		mOptionDialog.setDisplayType(type);
		mOptionDialog.setOnClickListener(listener);
		
		mOptionDialog.show();
	}
	
	
	public void dismissOptionDialog() {
		if(mOptionDialog != null && mOptionDialog.isShowing()) {
			mOptionDialog.dismiss();
		}
	}
	

	public void showProgressDialog() {
		if (null == mProgressDialog) {
			mProgressDialog = CustomProgressDialog.createDialog(this, false);
		}
		mProgressDialog.show();
	}
	
	public void showProgressDialogWithTitle(int titleResId) {
		if (null == mProgressDialog) {
			mProgressDialog = CustomProgressDialog.createDialog(this, titleResId != 0);
		}
		mProgressDialog.setMessage(titleResId).show();
	}
	
	public void dismissProgressDialog() {
		if(mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}
	
	protected boolean isDialogShow() {
		if(mProgressDialog != null && mProgressDialog.isShowing()) {
			return true;
		}
		if(mWarningDialog != null && mWarningDialog.isShowing()) {
			return true;
		}
		
		return false;
	}
	
	public void gotoActivity(Class<?> cls) {
		Intent intent = new Intent(this, cls);
		startActivity(intent);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	public void gotoActivity(Intent intent) {
		startActivity(intent);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}
	
	public void gotoImageActivity(Intent intent) {
		startActivity(intent);
	}

	public void gotoImageActivity(String pic_url) {
		Intent intent = new Intent(this, ShowImageActivity.class);
		intent.putExtra("pic_url", pic_url);
		startActivity(intent);
		//overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
		// TODO
	}
	public void finishImageActivity() {
		finish();
		//overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
		// TODO
	}
	
	public void gotoActivityAndFinishThis(Class<?> cls) {
		Intent intent = new Intent(this, cls);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);	
	}
	
	public void finishActivity() {
//		scrollToFinishActivity();
		finish();
		overridePendingTransition(R.anim.finish_in_from_left, R.anim.finish_out_to_right);
	}
	
	
	public void sendHandlerMessage(Message msg) {
		mHandler.sendMessage(msg);
	}
	
	public static class BaseHandler extends Handler {
		
		WeakReference<ActivityBase> mActivity;
		
		public BaseHandler(ActivityBase activity) {
			mActivity = new WeakReference<ActivityBase>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			mActivity.get().handleMessage(msg);
		}
	}
	
	
	class WebsocktReadReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			ActivityBase.this.onReceive(context, intent);
		}		
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
	}
	
//	/**
//	 * Websocket开启时进行主动请求
//	 */
//	public void onWebsocketLogin() {
//		Logger.i("ActivityBase", "***--- onWebsocketLogin ---***");
//		Config.USER_ID = mAppInfo.getUser().getW_id() + "of" + mAppInfo.getUser().getE_id();
//		Config.SITE_ID = mAppInfo.getUser().getE_id();
//		/*
//		 * 初始请求内容：
//		 * 1. 查询座席今天的历史会话	get_worker_session
//		 * 2. 查询座席服务中的客户		get_customer_list
//		 * 3. 查询等待中客户列表		get_waiting_customer
//		 * 4. 查询座席信息			get_worker_info
//		 *   
//		 */
//		try {
//			if (mApplication.getAppStatus() != Config.LOADED_PART) {
//				// get_customer_list & get_waiting_customer
//				CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, this);
//				cReq.getCustomerList();
//				cReq.getWaitingCustomer();
//			}
//			
//			// get_worker_info
//			WorkerRequest wReq = (WorkerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WWRKR, this);
//			wReq.getArchWorkers();
//			List<WorkerBean> users = DataSupport.findAll(WorkerBean.class);
//			if (users == null || users.isEmpty()) {
//				wReq.getWorkerInfo();
//			} else {
//				mAppInfo.setUser(users.get(0));
//				wReq.getWorkerStatus();
//			}
//			
//			mApplication.setAppStatus(Config.LOADED);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}
	
	

	public Animation getAnimationY(boolean show) {
		if (show) {
			if (null == mSlideIn) {
				mSlideIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.layout_in);
			}			
			return mSlideIn;
		} else {
			if (null == mSlideOut) {
				mSlideOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.layout_out);
			}
			return mSlideOut;
		}
	}
	
	public void showViewWithAnimation(final View view, Animation anim) {
		if (view.getVisibility() == View.VISIBLE) {
			return;
		}
		view.startAnimation(anim);
		anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
//            	view.setVisibility(View.VISIBLE);
            }
             
            @Override
            public void onAnimationRepeat(Animation arg0) {}
             
            @Override
            public void onAnimationEnd(Animation arg0) {
            	view.setVisibility(View.VISIBLE);
            }
        });
	}
	
	public void dismissViewWithAnimation(final View view, Animation anim) {
		if (view.getVisibility() != View.VISIBLE) {
			return;
		}
		view.startAnimation(anim);
		anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {}
             
            @Override
            public void onAnimationRepeat(Animation arg0) {}
             
            @Override
            public void onAnimationEnd(Animation arg0) {
            	view.setVisibility(View.GONE);
            }
        });
	}
	
	/**
	 * 发布事件
	 * @param obj
	 * @param tag
	 */
	protected void postEvent(Object obj, String tag) {
		EventBus.getDefault().post(obj, tag);
	}

	
	/*** MD2.x ***/
	
	@TargetApi(21)
	public void setStatusbarColor(int color) { // 显示颜色为window背景色
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			//getWindow().setStatusBarColor(colorBurn(color)); // 加深
			getWindow().setStatusBarColor(color); // 不加深
		}
	}

	@TargetApi(21)
	public void setNavigationBarColor(int color) {
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			getWindow().setNavigationBarColor(color);
		}
	}

	public void setWindowBackground(int color) {
		// [修改]不设置窗口背景，改为默认状态栏遮罩
//		if (Build.VERSION.SDK_INT >= 21) {
//			getWindow().setBackgroundDrawable(new ColorDrawable(colorBurn(color)));
//		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//			setTranslucentStatus(true);
//			
//			SystemBarTintManager tintManager = new SystemBarTintManager(this);  
//			tintManager.setStatusBarTintEnabled(true);  
//			tintManager.setStatusBarTintColor(color);
//		}
	}
	
	/**
	 * 颜色加深处理
	 * 
	 * @param RGBValues
	 *            RGB的值，由alpha（透明度）、red（红）、green（绿）、blue（蓝）构成，
	 *            Android中我们一般使用它的16进制，
	 *            例如："#FFAABBCC",最左边到最右每两个字母就是代表alpha（透明度）、
	 *            red（红）、green（绿）、blue（蓝）。每种颜色值占一个字节(8位)，值域0~255
	 *            所以下面使用移位的方法可以得到每种颜色的值，然后每种颜色值减小一下，在合成RGB颜色，颜色就会看起来深一些了
	 * @return
	 */
	protected int colorBurn(int RGBValues) {
		//int alpha = RGBValues >> 24;
		int red = RGBValues >> 16 & 0xFF;
		int green = RGBValues >> 8 & 0xFF;
		int blue = RGBValues & 0xFF;
		red = (int) Math.floor(red * (1 - 0.1));
		green = (int) Math.floor(green * (1 - 0.1));
		blue = (int) Math.floor(blue * (1 - 0.1));
		return Color.rgb(red, green, blue);
	}
	
	public void showSnackbar(View view, String text) {
		Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show();
	}

	public void showSnackbar(View view, int textId) {
		Snackbar.make(view, textId, Snackbar.LENGTH_SHORT).show();
	}

	public void showSnackbar(View view, String text, OnClickListener listener) {
		Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
				.setAction(R.string.cancel, listener)
				.show();
	}

	public void showSnackbar(View view, int textId, OnClickListener listener) {
		Snackbar.make(view, textId, Snackbar.LENGTH_SHORT)
		.setAction(R.string.cancel, listener)
		.show();
	}
	
	public void showAlertDialog(int titleId, int contentId, int positiveBtnText, int negativeBtnText,
			OnClickListener positiveBtnListener, OnClickListener negativeBtnListener) {
		mAlertDialog = 
		new AlertDialog(this).builder()
			.setTitle(titleId)
			.setMsg(contentId)
			.setCancelable(false)
			.setPositiveButton(positiveBtnText, positiveBtnListener)
			.setNegativeButton(negativeBtnText, negativeBtnListener);
		mAlertDialog.show();
	}
	
	public void showAlertDialog(int titleId, int contentId) {
		mAlertDialog = 
		new AlertDialog(this).builder()
		.setTitle(titleId)
		.setMsg(contentId)
		.setCancelable(false)
		.setNegativeButton("确定", new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		mAlertDialog.show();
	}
	
	public void showAlertDialog(int titleId, int contentId, OnClickListener positiveBtnListener, OnClickListener negativeBtnListener) {
		mAlertDialog = 
		new AlertDialog(this).builder()
		.setTitle(titleId)
		.setMsg(contentId)
		.setCancelable(false)
		.setPositiveButton("确定", positiveBtnListener)
		.setNegativeButton("取消", negativeBtnListener);
		mAlertDialog.show();
	}

	public void showAlertDialog(int contentId, OnClickListener positiveBtnListener, OnClickListener negativeBtnListener) {
		mAlertDialog = 
				new AlertDialog(this).builder()
				.setTitle("提示")
				.setMsg(contentId)
				.setCancelable(false)
				.setPositiveButton("确定", positiveBtnListener)
				.setNegativeButton("取消", negativeBtnListener);
		mAlertDialog.show();
	}
	
	public void showAlertDialog(int contentId) {
		mAlertDialog = 
		new AlertDialog(this).builder()
		.setTitle("提示")
		.setMsg(contentId)
		.setCancelable(false)
		.setNegativeButton("确定", new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		mAlertDialog.show();
	}
	
	public void dismissAlertDialog() {
		if (mAlertDialog != null) {
			mAlertDialog.dismiss();
			mAlertDialog = null;
		}
	}
}
