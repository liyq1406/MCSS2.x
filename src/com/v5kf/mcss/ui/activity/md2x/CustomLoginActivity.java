package com.v5kf.mcss.ui.activity.md2x;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.v5kf.client.lib.V5HttpUtil;
import com.v5kf.client.lib.callback.HttpResponseHandler;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.Config.AppStatus;
import com.v5kf.mcss.config.Config.ExitFlag;
import com.v5kf.mcss.config.Config.LoginStatus;
import com.v5kf.mcss.config.Config.ReloginReason;
import com.v5kf.mcss.entity.WorkerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.service.CoreService;
import com.v5kf.mcss.service.NetworkManager;
import com.v5kf.mcss.service.NetworkManager.NetworkListener;
import com.v5kf.mcss.ui.widget.CircleImageView;
import com.v5kf.mcss.utils.DbUtil;
import com.v5kf.mcss.utils.IntentUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.MD5;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.WorkerSP;
import com.v5kf.mcss.utils.cache.ImageLoader;

public class CustomLoginActivity extends BaseLoginActivity implements NetworkListener {
	private static final String TAG = "UserLoginActivity";
	
	private static final int TASK_LOGIN_OK = 1;
	private static final int TASK_UN_LOGIN = 2;
	private static final int TASK_TIME_OUT = 3;
	private boolean login_flag = false;
	private WorkerSP mWsp;
	
	private EditText mSiteNameEt, mUserNameEt, mUserPwdEt;
	private CircleImageView mWorkerPhoto;
	private TextView mTipsTv;
	
	private NetworkManager mNetReceiver;
	
	@Override
	public void onBackPressed() {
		finish();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_md2x_user_login);
		// 取消滑动返回
		setSwipeBackEnable(false);
		
		handleIntent(getIntent());
		
		mWsp = mApplication.getWorkerSp();
		findView();
		initView();
		startUpdateService();
		
		/* 添加网络监听 */
		mNetReceiver = new NetworkManager();
		registerReceiver(mNetReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		NetworkManager.addNetworkListener(this);
		
		// 设置初始状态
//		mApplication.setAppStatus(AppStatus.AppStatus_Exit);
//		mWsp.saveExitFlag(ExitFlag.ExitFlag_NeedLogin);
//		mWsp.clearAuthorization();
		
		// 设置状态栏颜色 -> [修改]不设置，让view顶到头
		/* 在API19以上改变状态栏颜色 */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        	Window window = getWindow();
//			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
////							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            //window.setStatusBarColor(UITools.getColor(this, R.color.base_status_bar_bg));
////            window.setStatusBarColor(Color.TRANSPARENT);
////            window.setNavigationBarColor(Color.TRANSPARENT);
        }
		
		Logger.w(TAG, "android.os.Build.MODEL = " + android.os.Build.MODEL);
		//setNavigationBarColor(UITools.getColor(R.color.v5_navigation_bar_bg));
		//setStatusbarColor(UITools.getColor(R.color.transparent, this));
		if (android.os.Build.VERSION.SDK_INT >= 23){ // Build.VERSION_CODES.M
		    //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
		} else {
			if (android.os.Build.MODEL.contains("MIUI")) {
				UITools.setStatusBarDarkModeOfMIUI(true, this);
			} else if (android.os.Build.MODEL.contains("Flyme")) {
				UITools.setStatusBarDarkIconOfFlyme(getWindow(), true);
			}
		}
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	private void handleIntent(Intent intent) {
		ReloginReason reason = ReloginReason.values()[intent.getIntExtra(EventTag.ETAG_RELOGIN, 0)];
		if (reason == ReloginReason.ReloginReason_Code1000) {
//			showWarningDialog(R.string.on_other_dev_login_err, null);
			showAlertDialog(R.string.on_other_dev_login_err);
		}
		mApplication.stopOtherActivities(this);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mNetReceiver);
		NetworkManager.removeNetworkListener(this);
		super.onDestroy();
		mHandler.removeMessages(TASK_TIME_OUT);
		mHandler.removeMessages(TASK_UN_LOGIN);
	}
	
	
	private void findView() {
		mUserNameEt = (EditText) findViewById(R.id.user_name_et);
		mUserPwdEt = (EditText) findViewById(R.id.user_pwd_et);
		mSiteNameEt = (EditText) findViewById(R.id.site_name_et);
		mWorkerPhoto = (CircleImageView) findViewById(R.id.login_worker_photo);
		mTipsTv = (TextView) findViewById(R.id.account_tips);
	}
	

	private void initView() {
		mUserNameEt.setText(String.valueOf(mWsp.readWorkerName()));
		mSiteNameEt.setText(String.valueOf(mWsp.readSiteName()));
		
		/* 显示头像 */
		if (mWsp.readWorkerPhoto() != null) {
			ImageLoader imageLoader = new ImageLoader(this, true, R.drawable.v5_photo_default);
			imageLoader.DisplayImage(mWsp.readWorkerPhoto(), mWorkerPhoto);
		}
		
		mTipsTv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				gotoWebViewActivity(Config.V5KF_COM, 0); //R.string.app_name
			}
		});
	}

	@Override
	public boolean onTouchEvent(android.view.MotionEvent event) {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		return imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
	}
	
	
	/**
	 * 登录按钮点击处理
	 * @param v
	 */
	public void onUserLogin(final View v) {
		Logger.d(TAG, "onUserLogin");
		if (TextUtils.isEmpty(mUserNameEt.getText().toString().trim()) || 
				TextUtils.isEmpty(mSiteNameEt.getText().toString().trim()) ||
				TextUtils.isEmpty(mUserPwdEt.getText().toString().trim())) {
//			showWarningDialog(R.string.username_or_pwd_empty_tip, null);
			showAlertDialog(R.string.username_or_pwd_empty_tip);
			return;
		}
		login_flag = false;
		showProgressDialog();
		
		Logger.i(TAG, "ServiceRunning:" + IntentUtil.isServiceWork(this, Config.SERVICE_NAME_CS) 
				+ " AppStatus:" + mApplication.getLoginStatus());
		if (IntentUtil.isServiceWork(this, Config.SERVICE_NAME_CS) 
				&& LoginStatus.LoginStatus_Logged == mApplication.getLoginStatus()) {
			Logger.i(TAG, "[onUserLogin]<>CoreService already running<><>");
			mHandler.sendEmptyMessage(TASK_LOGIN_OK);
			return;
		}
		mApplication.setAppStatus(AppStatus.AppStatus_Init);
		mApplication.getWorkerSp().saveExitFlag(ExitFlag.ExitFlag_None);
		
		String siteName = mSiteNameEt.getText().toString().trim();
		String userName = mUserNameEt.getText().toString().trim();
		String passWord = mUserPwdEt.getText().toString().trim();
//		if (!isExpired(siteName, userName, passWord)) {			
//			doLogin();
//			return;
//		}
		passWord = MD5.encode(passWord);
		
		String url = getAccountAuthUrl(siteName, userName, passWord);
		V5HttpUtil.get(url, new HttpResponseHandler(mApplication) {
			public void onSuccess(int statusCode, String responseString) {
				Logger.d(TAG, "[AccountAuth]->responseString:" + responseString);
				if (statusCode == 200) {
					try {
						JSONObject json = new JSONObject(responseString);
						if (json.has("authorization")) {
							parseAndSave(json);
						} else {
							mApplication.setLoginStatus(LoginStatus.LoginStatus_LoginFailed);
							mHandler.sendEmptyMessage(TASK_UN_LOGIN);
						}
					} catch (JSONException e) {
						e.printStackTrace();
						
						mApplication.setLoginStatus(LoginStatus.LoginStatus_LoginFailed);
						mHandler.sendEmptyMessage(TASK_UN_LOGIN);
					}
				} else {
					Logger.e(TAG, "User login fail, code:" + statusCode);
					mApplication.setLoginStatus(LoginStatus.LoginStatus_LogErr);
					mHandler.sendEmptyMessage(TASK_UN_LOGIN);
				}
			}
			
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "User login fail, code:" + statusCode + ", resp:" + responseString);
				mApplication.setLoginStatus(LoginStatus.LoginStatus_LogErr);
				mHandler.sendEmptyMessage(TASK_UN_LOGIN);
			}
		});
		
//		SigninHttpClient.get(
//				siteName, 
//				userName, 
//				passWord, 
//			new TextHttpResponseHandler() {
//				@Override
//				public void onSuccess(int statusCode, Header[] headers,
//						String responseString) {
//					Logger.d(TAG, "[SigninHttpClient]->responseString:" + responseString);
//					if (statusCode == 200) {
//						try {
//							JSONObject json = new JSONObject(responseString);
//							if (json.has("authorization")) {
//								parseAndSave(json);
//							} else {
//								mApplication.setLoginStatus(LoginStatus.LoginStatus_LoginFailed);
//								mHandler.sendEmptyMessage(TASK_UN_LOGIN);
//							}
//						} catch (JSONException e) {
//							e.printStackTrace();
//						}
//					} else {
//						Logger.e(TAG, "User login fail, code:" + statusCode);
//						mApplication.setLoginStatus(LoginStatus.LoginStatus_LogErr);
//						mHandler.sendEmptyMessage(TASK_UN_LOGIN);
//					}
//				}
//				
//				@Override
//				public void onFailure(int statusCode, Header[] headers,
//						String responseString, Throwable throwable) {
//					Logger.e(TAG, "User login fail, code:" + statusCode + ", resp:" + responseString);
//					mApplication.setLoginStatus(LoginStatus.LoginStatus_LogErr);
//					mHandler.sendEmptyMessage(TASK_UN_LOGIN);
//				}
//			});
	}
	
	private static String getAccountAuthUrl(String siteName, String userName, String pwd) {
		int expires = 24 * 3600 * 30; // 有效期30天
		String path = Config.LOGIN_URL + "sitename=" + siteName + "&username=" + userName + 
			"&sign=" + pwd + "&expires=" + expires;
		
		Logger.d(TAG, "getAccountAuthUrl:" + path);
		return path;
	}
	
	/**
	 * 判断当前登录是否过期（需为同一用户）
	 * @param siteName
	 * @param userName
	 * @param passWord
	 * @return
	 */
//	private boolean isExpired(String siteName, String userName, String passWord) {
//		if (null == siteName || null == userName || null == passWord) {
//			return true;
//		}
//		passWord = MD5.encode(passWord);
//		if (siteName.equals(mWsp.readSiteName()) && userName.equals(mWsp.readWorkerName())
//				&& passWord.equals(mWsp.readPassWord())) {
//			if (mWsp.readAuthorization() == null) {
//				return true;
//			} else {
//				return false;
//			}
//		} else {
//			return true;
//		}		
//	}

	protected void doLogin() {
		Logger.i(TAG, "<><>CoreService start new instance<><>");			
		/* 启动WS服务 */
		Intent intent = new Intent();
		intent.setAction(Config.ACTION_CORE);
		intent.setPackage("com.v5kf.mcss");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startService(intent);
		doLoginTask();
	}


	private void doLoginTask() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				int i = 0;
				
				try {
					while (!login_flag) {
						Thread.sleep(50);
						i += 50;
						
						/* 等待超时后发出警告 */
						if (i > Config.WS_TIME_OUT) {
							mHandler.sendEmptyMessage(TASK_TIME_OUT);
							return;
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(TASK_UN_LOGIN);
				}
				
//				if (Config.LOGGED == mApplication.getLoginStatus()) {
//					mHandler.sendEmptyMessage(TASK_LOGIN_OK);
//				} else {
//					mHandler.sendEmptyMessage(TASK_UN_LOGIN);
//				}
			}
		}).start();
	}
	
	private void parseAndSave(JSONObject json) throws JSONException {
		String siteName = mSiteNameEt.getText().toString().trim();
		String userName = mUserNameEt.getText().toString().trim();
		String passWord = mUserPwdEt.getText().toString().trim();
		String w_id = json.getString("worker_id");
		String e_id = json.getString("site_id");
		String authorization = json.optString("authorization");
		long timestamp = json.getLong("timestamp");
		long expires = json.getLong("expires");
		
		if (authorization.isEmpty()) {
			mApplication.setLoginStatus(LoginStatus.LoginStatus_LoginFailed);
			mHandler.sendEmptyMessage(TASK_UN_LOGIN);
			return;
		}
		
		List<WorkerBean> users = DataSupport.findAll(WorkerBean.class);
		if (users != null && !users.isEmpty() && users.get(0).getW_id() != null && !users.get(0).getW_id().equals(w_id)) { // 更换用户登录时清空数据库
			DbUtil.clearCache(getApplicationContext());
		}
		
		WorkerSP wsp = mApplication.getWorkerSp();
		wsp.saveSiteName(siteName);
		wsp.saveWorkerName(userName);
		wsp.savePassWord(passWord);
		wsp.saveSiteId(e_id);
		wsp.saveWorkerId(w_id);
		mAppInfo.getUser().setW_id(w_id);
		mAppInfo.getUser().setE_id(e_id);
		wsp.saveAuthorization(authorization);
		wsp.saveTimestamp(timestamp);
		wsp.saveExpires(expires);
		
		doLogin();
	}
	

	@Override
	protected void handleMessage(Message msg) {
		Logger.v(TAG, "handleMessage:" + msg.what);
		switch (msg.what) {
		case TASK_LOGIN_OK:
			dismissProgressDialog();
			LoginStatus loginStatus = mApplication.getLoginStatus();
			if (LoginStatus.LoginStatus_Logged == loginStatus) {
				gotoMainTabActivity();
			} else {
				if (LoginStatus.LoginStatus_LoginFailed == loginStatus) {
					stopService(new Intent(this, CoreService.class));
//					showWarningDialog(R.string.err_account_failed, null);
					showAlertDialog(R.string.err_account_failed);
				}
			}
			break;

		case TASK_TIME_OUT:
			dismissProgressDialog();
			if (isDialogShow()) {
				return;
			}
			
		case TASK_UN_LOGIN:
			stopService(new Intent(this, CoreService.class));
			dismissProgressDialog();
			Logger.d(TAG, "loginStatus = " + mApplication.getLoginStatus());
			int resId = 0;
			switch (mApplication.getLoginStatus()) {
			case LoginStatus_LogErr:
				resId = R.string.err_network_failed;
				break;
//			case LoginStatus_Logging:
//				resId = R.string.err_network_failed;
//				break;
			case LoginStatus_LoginFailed:
				resId = R.string.err_account_failed;
				break;
			case LoginStatus_LoginLimit:
				resId = R.string.err_login_limit;
				break;
			case LoginStatus_AuthFailed:
				resId = R.string.err_login_token;
				break;
			default:
				resId = R.string.err_login_failed;
				break;
			}
//			showWarningDialog(resId, null);
			showAlertDialog(resId);
			break;
		}
	}

	@Override
	public void onNetworkStatusChange(int netStatus, int oldStatus) {
		Logger.d(TAG, "[onNetworkStatusChange] -> " + netStatus);
		switch (netStatus) {
		case NetworkManager.NETWORK_MOBILE:
		case NetworkManager.NETWORK_WIFI:
			break;
		case NetworkManager.NETWORK_NONE:
			break;
		}
	}
	
	/***** event *****/
	
	@Subscriber(tag = EventTag.ETAG_LOGIN_CHANGE, mode = ThreadMode.MAIN)
	private void loginChange(Integer error) {
		login_flag = true; // 登录返回
		switch (error) {
		case 0: // 登录成功				
			mHandler.sendEmptyMessage(TASK_LOGIN_OK);
			//mHandler.sendEmptyMessageDelayed(TASK_LOGIN_OK, 3000);
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
	
	@Subscriber(tag = EventTag.ETAG_RELOGIN, mode = ThreadMode.MAIN)
	private void gotoLogin(ReloginReason reason) {
		Logger.e(TAG + "-eventbus", "eventbus -> ETAG_RELOGIN");
		mWsp.clearAuthorization();
		mApplication.setLoginStatus(LoginStatus.LoginStatus_AuthFailed);
		if (reason == ReloginReason.ReloginReason_AuthFailed) {
			mHandler.sendEmptyMessage(TASK_UN_LOGIN);
		}
		
//		showConfirmDialog(
//		R.string.on_other_dev_login_re_token, 
//		WarningDialog.MODE_TWO_BUTTON, 
//		new WarningDialogListener() {
//	@Override
//	public void onClick(View view) {
//		switch (view.getId()) {
//		case R.id.btn_dialog_warning_left: // 取消								
//			break;
//		case R.id.btn_dialog_warning_right: // 确认
//			dismissWarningDialog();
//			mApplication.terminate();
//			gotoActivityAndFinishThis(UserLoginActivity.class);							
//			break;
//		}
//	}
//});
	}

}
