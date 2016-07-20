package com.v5kf.mcss.ui.activity.md2x;

import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;

import com.umeng.analytics.MobclickAgent;
import com.v5kf.client.ui.keyboard.EmoticonsUtils;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.Config.ExitFlag;
import com.v5kf.mcss.config.Config.LoginStatus;
import com.v5kf.mcss.entity.WorkerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.service.CoreService;
import com.v5kf.mcss.service.NetworkManager;
import com.v5kf.mcss.utils.IntentUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.WorkerSP;

/**
 * 欢迎界面，执行应用数据初始化与必要加载项
 * @author Chyrain
 *
 */
public class WelcomeActivity extends BaseLoginActivity {
	private static final String TAG = "WelcomeActivity";
	private static final int TASK_LOGIN_OK = 1;
	private static final int TASK_UN_LOGIN = 2;
	private static final int TASK_TIME_OUT = 3;
	
	private static final int DALAY_MS = 500;
	
	private boolean login_flag = false;
	private WorkerSP mWSP;
	private boolean isForeground = false;
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		finish();
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP) 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		setSwipeBackEnable(false);

		MobclickAgent.onEvent(this,"APP_START");
		mWSP = mApplication.getWorkerSp();
		isForeground = true;
		initView();
		
		/* 初始网络状态 */
		mApplication.setNetworkState(NetworkManager.getNetworkState(this));
		
		/* 第一次打开应用时初始化表情数据库 */
		EmoticonsUtils.initEmoticonsDB(getApplicationContext());
		/* 创建本地数据库(unuse) */
		mApplication.createDB();
		
		// 设置虚拟按键导航栏颜色
		//setNavigationBarColor(UITools.getColor(R.color.v5_navigation_bar_bg));
		/* 在API19以上改变状态栏颜色 */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        	Window window = getWindow();
//			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
////							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(UITools.getColor(this, R.color.base_status_bar_color_welcome));
//            window.setStatusBarColor(Color.TRANSPARENT);
//            window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(true);
			
//			SystemBarTintManager tintManager = new SystemBarTintManager(this);
//			tintManager.setStatusBarTintEnabled(true);  
//			tintManager.setStatusBarTintResource(R.color.main_color_dark);
		}
		
		if (!mWSP.readBoolean("v5_inited")) {
			gotoActivityAndFinishThis(GuideActivity.class);
		} else {
			// 初始化任务
			new Thread(new Runnable() {
				@Override
				public void run() {
					initTask();
				}
			}).start();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		MobclickAgent.updateOnlineConfig(this);
//		MobclickAgent.openActivityDurationTrack(false);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		isForeground = false;
		mHandler.removeMessages(TASK_TIME_OUT);
		mHandler.removeMessages(TASK_UN_LOGIN);
	}

	
	private void initView() {
		// 
	}

	
	private void initTask() {
		if (mWSP.readAuthorization() == null || !mWSP.readAutoLogin() || mWSP.readPassWord().isEmpty()
				|| mWSP.readExitFlag() == ExitFlag.ExitFlag_NeedLogin) {
			mApplication.setLoginStatus(LoginStatus.LoginStatus_Unlogin);
			mHandler.sendEmptyMessageDelayed(TASK_UN_LOGIN, DALAY_MS);
			return;
		}
		
		/* 进行自动登录: 查询本地密码存储,进行自动登录,开启websocket服务 */
		try {
			Logger.i(TAG, "init-<><>doAutoLogin<><>");
			// doAutoLogin(); 
			// [修改]改为在MaintTabActivity自动登录
			mHandler.sendEmptyMessageDelayed(TASK_LOGIN_OK, 500);
		} catch (Exception e) {
			e.printStackTrace();
			mHandler.sendEmptyMessageDelayed(TASK_UN_LOGIN, DALAY_MS);
		}
	}
	
	
	/**
	 * 处理自动登录
	 * @throws InterruptedException
	 * @deprecated
	 */
	private void doAutoLogin() throws InterruptedException {
		WorkerBean worker = mApplication.getAppInfo().getUser();
		worker.setW_id(mWSP.readWorkerId());
		worker.setE_id(mWSP.readSiteId());
		
		Logger.i(TAG, "<><><><>CoreService start<><><><>");
		if (!IntentUtil.isServiceWork(this, Config.SERVICE_NAME_CS)) { // 开启服务并上线
			Intent localIntent = new Intent();
			localIntent.setAction(Config.ACTION_CORE);
			localIntent.setPackage("com.v5kf.mcss");
			localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        startService(localIntent);
		}
		
		int i = 0;
		/* 至少等待1.0s */
		while (!login_flag || i < 1000) {
			Thread.sleep(100);
			i += 100;
			
			/* 等待WS_TIME_OUT后发出警告 */
			if (i > Config.WS_TIME_OUT) {
				mHandler.sendEmptyMessage(TASK_TIME_OUT);
				break;
			}
		}
		
		if (mApplication.getLoginStatus() != LoginStatus.LoginStatus_Logged) {
			mHandler.sendEmptyMessage(TASK_UN_LOGIN);
		}
	}


	@Override
	protected void handleMessage(Message msg) {
		Logger.d(TAG, "handleMessage:" + msg.what + " loginStatus:" + mApplication.getLoginStatus());
		switch (msg.what) {
		case TASK_LOGIN_OK:
			gotoMainTabActivity();
			startUpdateService();
			break;
		case TASK_UN_LOGIN: {
			final int resId;
			if (!mWSP.readBoolean("v5_inited")) {
				gotoActivityAndFinishThis(GuideActivity.class);
				break;
			}
			switch (mApplication.getLoginStatus()) {
			case LoginStatus_Unlogin:
				stopService(new Intent(this, CoreService.class));
				if (mAlertDialog == null || !mAlertDialog.isShowing()) {
					gotoActivityAndFinishThis(CustomLoginActivity.class);
				}
				break;
			case LoginStatus_LogErr:
				resId = R.string.err_network_failed;
				alert(resId);
				break;
			case LoginStatus_Logging:
				resId = R.string.err_network_failed;
				alert(resId);
				break;
			case LoginStatus_LoginFailed:
				resId = R.string.err_account_failed;
				alert(resId);
				break;
			case LoginStatus_LoginLimit:
				resId = R.string.err_login_limit;
				alert(resId);
				break;
			case LoginStatus_AuthFailed:
				resId = R.string.err_login_token;
				alert(resId);
				break;
			default:
				resId = R.string.err_login_failed;
				alert(resId);
				break;
			}
			break;
		}
		case TASK_TIME_OUT: {
			Logger.d(TAG, "loginStatus = " + mApplication.getLoginStatus());
			if (isForeground) {
				alert(R.string.err_login_timeout);
			}
			break;
		}
		default:
			break;
		}
	}
	
	private void alert(int resId) {
//		if (NetworkManager.isConnected(WelcomeActivity.this)) {
//			showWarningDialog(resId, new WarningDialogListener() {
//				@Override
//				public void onClick(View view) {
//					dismissWarningDialog();
//					stopService(new Intent(WelcomeActivity.this, CoreService.class));
//					gotoActivityAndFinishThis(UserLoginActivity.class);
//				}
//			});
//		} else {
			stopService(new Intent(WelcomeActivity.this, CoreService.class));
			gotoActivityAndFinishThis(CustomLoginActivity.class);
//		}
	}
	
	
	/***** event *****/
	
	@Subscriber(tag = EventTag.ETAG_LOGIN_CHANGE, mode = ThreadMode.MAIN)
	private void loginChange(Integer error) {
		login_flag = true; // 登录返回
		switch (error) {
		case 0: // 登录成功				
			mHandler.sendEmptyMessage(TASK_LOGIN_OK);
			break;
		case 40001: // 无效参数
			mHandler.sendEmptyMessage(TASK_UN_LOGIN);
			break;
		case 40002: // 无效登陆设备
			mHandler.sendEmptyMessage(TASK_UN_LOGIN);
			break;
		case 40003: // 服务准备中（服务启动过程中、座席数据更新中）
			mHandler.sendEmptyMessage(TASK_UN_LOGIN);
			break;
		case 40004: // 达到最大登录限制
			mApplication.setLoginStatus(LoginStatus.LoginStatus_LoginLimit);
			mHandler.sendEmptyMessage(TASK_UN_LOGIN);
			break;
		default:
			mHandler.sendEmptyMessage(TASK_UN_LOGIN);
			break;
		}
	}
	
//	@Subscriber(tag = EventTag.ETAG_RELOGIN, mode = ThreadMode.MAIN)
//	private void gotoLogin(ReloginReason reason) {
//		Logger.e(TAG + "-eventbus", "eventbus -> ETAG_RELOGIN");
//		mApplication.setLoginStatus(LoginStatus.LoginStatus_AuthFailed);
//		mHandler.sendEmptyMessage(TASK_UN_LOGIN);
//	}
}
