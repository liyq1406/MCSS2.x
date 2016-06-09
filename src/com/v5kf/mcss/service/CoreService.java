package com.v5kf.mcss.service;

import java.lang.ref.WeakReference;
import java.net.URI;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.tencent.android.tpush.XGPushConfig;
import com.umeng.analytics.MobclickAgent;
import com.v5kf.client.lib.V5HttpUtil;
import com.v5kf.client.lib.callback.HttpResponseHandler;
import com.v5kf.client.lib.websocket.WebSocketClient;
import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.Config.AppStatus;
import com.v5kf.mcss.config.Config.ExitFlag;
import com.v5kf.mcss.config.Config.LoginStatus;
import com.v5kf.mcss.config.Config.ReloginReason;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.entity.ArchWorkerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.QAOManager;
import com.v5kf.mcss.service.NetworkManager.NetworkListener;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.WakeUtil;
import com.v5kf.mcss.utils.WorkerSP;

public class CoreService extends Service implements WebSocketClient.Listener, NetworkListener {
	private static final String TAG = "CoreService";
	public static final int WHAT_SHOW_TOAST = 1;
	public static final int WHAT_SEND_REQUEST = 3;
	public static final int WHAT_GET_TOKEN = 4;
	public static final int WHAT_WS_CONNECT = 5;
	public static final int WHAT_KEEP_SERVICE = 6;
	public static final int WHAT_WS_RECONNECT = 7;
	public static final int WHAT_WAKE_RELEASE = 8;
	public static final String MSG_KEY_TOAST_TEXT = "text";
	public static final String MSG_KEY_REQUEST = "request";
	public static final int WHAT_NOTIFY_DISCON = 9;
	public static final int WHAT_NOTIFY_RECON = 10;
	
	private CoreServiceReceiver mReceiver;
	private NetworkManager mNetReceiver;
	
	String mUri;
	static CustomApplication mApplication;
	static AppInfoKeeper mAppInfo;
	static WebSocketClient mClient;
	WorkerSP mWSP;
	Handler mHandler;
	private boolean mReconFlag = false; // 重连标识
	private boolean connecting = false; // 防止重复调用connect，多线程去重
	private int mTotalSend = 0;
	
	private IBinder binder = new MyBinder();
	public class MyBinder extends Binder {
		public CoreService getService() {
			return CoreService.this;
		}
	}
	
	public static boolean isConnected() {
		if (mClient != null && mClient.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	public static void reConnect(Context context) {
		if (mClient != null) {
			/* 注册Alarm广播接收 */
			Intent intent = new Intent(Config.ACTION_ON_WS_HEARTBEAT);
			context.sendBroadcast(intent);
		} else {
			Intent i = new Intent(context, CoreService.class);
			context.startService(i);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Logger.w(TAG, "[onCreate] -> startCoreService");
		if (Build.VERSION.SDK_INT < 18)
		      startForeground(-1213, new Notification());
		if (Config.DEBUG_TOAST) {
			Logger.i(TAG, "CoreService onCreate...");
			Toast.makeText(CoreService.this, "CoreService onCreate...", Toast.LENGTH_SHORT).show();
		}
		init();
		
		// 注册对象
        EventBus.getDefault().register(this);
        // 开启PushService
        startService(new Intent(this, PushService.class));
	}
	
	private void init() {
		mHandler = new ServiceHandler(this);
		mWSP = new WorkerSP(this);
		mApplication = (CustomApplication)getApplication();
		mAppInfo = mApplication.getAppInfo();
		NetworkManager.addNetworkListener(this);
		
		initHeartBeatAlarm();
		initReceiver();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Logger.d(TAG, "onBind");
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.d(TAG, "onStartCommand -> [connectWebsocket]");
		connectWebsocket(true);
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		Logger.d(TAG, "onUnbind");
		return super.onUnbind(intent);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
		unregisterReceiver(mNetReceiver);
//		IntentUtil.releaseWakeLock(this);
		Logger.w(TAG, "[onDestroy] -> stopCoreService");
		NetworkManager.removeNetworkListener(this);
		clearHeartBeatAlarm();
		
		mApplication.setAppStatus(AppStatus.AppStatus_Init);
		if (mClient!= null && mClient.isConnected()) {
			mClient.disconnect();
		}
		
		if (mWSP.readExitFlag() == ExitFlag.ExitFlag_None) {
			Intent localIntent = new Intent();
			localIntent.setAction(Config.ACTION_CORE);
			localIntent.setPackage("com.v5kf.mcss");
			localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        startService(localIntent);
		}
		
		// 不要忘记注销！！！！
        EventBus.getDefault().unregister(this);
	}
	
	private synchronized void connectWebsocket() {
		connectWebsocket(false);
	}
	
	/**
	 * 连接Websocket(多线程访问问题)
	 * @param connectWebsocket CoreService 
	 * @return void
	 */
	private synchronized void connectWebsocket(boolean newClient) {
		Logger.i(TAG, "[connectWebsocket] isNew:" + newClient + " connecting:" + connecting);
		if (connecting) {
			Logger.w(TAG, "[connectWebsocket] is already connecting");
			return;
		}
		connecting = true;
		if (mWSP.readExitFlag() == ExitFlag.ExitFlag_NeedLogin) { //|| mApplication.getAppStatus() == Config.EXIT
			Logger.d(TAG, "[connectWebsocket] EXIT appStatus:" + mApplication.getAppStatus()
					+ " exitflag:" + mWSP.readExitFlag());
			stopSelf();
			return;
		}
		String id = mWSP.readWorkerId();
		String site = mWSP.readSiteId();
		String auth = mWSP.readAuthorization();
		if (auth == null) {
			Logger.i(TAG, "Auth已过期，重新请求authorization...");
			getAccessToken();
		} else {
			if (null != mClient && mClient.isConnected() && newClient) {
//				mClient.disconnect(10, "Stop and new client");
//				connecting = false;
//				Logger.i(TAG, "[connectWebsocket] mClient isConnected and force disconnect");
//				return;
				mClient.disconnect(-1, "Normal disconnect");
				mClient = null;
				mUri = Config.WS_PROTOCOL + "://" + Config.WS_HOST + Config.WS_PATH + "?id=" + id + "&site=" 
						+ site + "&dev=android&client=1026&device_token=" + XGPushConfig.getToken(getApplicationContext()) + "&auth=" + auth;
				Logger.i(TAG, "uri:" + mUri);
					
				mClient = new WebSocketClient(URI.create(mUri), this, null);
			} else if (mClient != null && mClient.isConnected()) {
				Logger.i(TAG, "[connectWebsocket] mClient isConnected and return");
				return;
			} else if (mClient != null) {
				if (newClient) {
					Logger.i(TAG, "forceNew client");
					mClient.disconnect(-1, "Normal disconnect");
					mClient = null;
					mUri = Config.WS_PROTOCOL + "://" + Config.WS_HOST + Config.WS_PATH + "?id=" + id + "&site=" 
							+ site + "&dev=android&client=1026&device_token=" + XGPushConfig.getToken(getApplicationContext()) + "&auth=" + auth;
					Logger.i(TAG, "uri:" + mUri);
						
					mClient = new WebSocketClient(URI.create(mUri), this, null);
				}			
			} else {
				mUri = Config.WS_PROTOCOL + "://" + Config.WS_HOST + Config.WS_PATH + "?id=" + id + "&site=" 
						+ site + "&dev=android&client=1026&device_token=" + XGPushConfig.getToken(getApplicationContext()) + "&auth=" + auth;
				Logger.i(TAG, "uri:" + mUri);
					
				mClient = new WebSocketClient(URI.create(mUri), this, null);
				Logger.i(TAG, "[connectWebsocket] mClient.connect()");
			}
			mClient.connect();
		}
	}
	
	private static String getAccountAuthUrl(String siteName, String userName, String pwd) {
		int expires = 24 * 3600 * 30; // 有效期30天
		String path = Config.LOGIN_URL + "sitename=" + siteName + "&username=" + userName + 
			"&sign=" + pwd + "&expires=" + expires;
		
		Logger.d("getAccountAuthUrl", path);
		return path;
	}
	
	/* 重新获取Token */
	private void getAccessToken() {
		String url = getAccountAuthUrl(mWSP.readSiteName(), mWSP.readWorkerName(), mWSP.readPassWord());
		V5HttpUtil.get(url, new HttpResponseHandler(mApplication) {
			public void onSuccess(int statusCode, String responseString) {
				if (statusCode == 200) {
					Logger.d(TAG, "[getAccessToken]responseString：" + responseString);
					try {
						JSONObject json = new JSONObject(responseString);
						String auth = json.optString("authorization");
						if ((auth == null || auth.isEmpty())) {
							Logger.e(TAG, "User login fail, token/auth is empty, code:" + statusCode + " text:"
									+ json.optString("text"));
//							showToast(json.optString("text"));
							return;
						}
						long timestamp = json.getLong("timestamp");
						long expires = json.getLong("expires");
						mWSP.saveTimestamp(timestamp);
						mWSP.saveExpires(expires);
						mWSP.saveAuthorization(auth);
						// 连接
						mHandler.sendEmptyMessage(WHAT_WS_CONNECT);
					} catch (JSONException e) {
						Logger.e(TAG, "User login fail(JSON parse exception), code:" + statusCode);
						e.printStackTrace();
					}
				}
			}
			
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "User login fail, code:" + statusCode);
				showToast(R.string.on_account_fail);
			}
		});
		
//		SigninHttpClient.get(
//			mWSP.readSiteName(), 
//			mWSP.readWorkerName(), 
//			mWSP.readPassWord(), 
//			new TextHttpResponseHandler() {
//				@Override
//				public void onSuccess(int statusCode, Header[] headers,
//						String responseString) {
//					if (statusCode == 200) {
//						Logger.d(TAG, "[getAccessToken]responseString：" + responseString);
//						try {
//							JSONObject json = new JSONObject(responseString);
//							String auth = json.optString("authorization");
//							if ((auth == null || auth.isEmpty())) {
//								Logger.e(TAG, "User login fail, token/auth is empty, code:" + statusCode + " text:"
//										+ json.optString("text"));
////								showToast(json.optString("text"));
//								return;
//							}
//							long timestamp = json.getLong("timestamp");
//							long expires = json.getLong("expires");
//							mWSP.saveTimestamp(timestamp);
//							mWSP.saveExpires(expires);
//							mWSP.saveAuthorization(auth);
//							// 连接
//							mHandler.sendEmptyMessage(WHAT_WS_CONNECT);
//						} catch (JSONException e) {
//							Logger.e(TAG, "User login fail(JSON parse exception), code:" + statusCode);
//							e.printStackTrace();
//						}
//					}
//				}
//				
//				@Override
//				public void onFailure(int statusCode, Header[] headers,
//						String responseString, Throwable throwable) {
//					Logger.e(TAG, "User login fail, code:" + statusCode);
//					showToast(R.string.on_account_fail);
//				}
//			});
	}
	
	private void initHeartBeatAlarm() {
		Intent intent = new Intent(Config.ACTION_ON_WS_HEARTBEAT);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30 * 1000, 60 * 1000, pi);
		Logger.i(TAG, "[Alarm - start] -> " + Config.ACTION_ON_WS_HEARTBEAT);
	}
	
	private void clearHeartBeatAlarm() {
		Intent intent2 = new Intent(Config.ACTION_ON_WS_HEARTBEAT);
		PendingIntent pi2 = PendingIntent.getBroadcast(this, 0, intent2, 0);
		AlarmManager am2 = (AlarmManager)getSystemService(ALARM_SERVICE);
		am2.cancel(pi2);
		Logger.i(TAG, "[Alarm - cancel] -> " + Config.ACTION_ON_WS_HEARTBEAT);
	}

	private void initReceiver() {
		mReceiver = new CoreServiceReceiver();
		mNetReceiver = new NetworkManager();
		
		/* 注册Alarm广播接收 */
		IntentFilter filter=new IntentFilter();
		filter.addAction(Config.ACTION_ON_WS_HEARTBEAT);
		registerReceiver(mReceiver, filter);
		
		registerReceiver(mNetReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	public void onLowMemory() {
		Logger.w(TAG, "[onLowMemory]");
		mApplication.stopOtherActivities(null);
	}
	
	@Override
	public void onTrimMemory(int level){
		Logger.w(TAG, "[onTrimMemory] " + level);
		if (Config.DEBUG_TOAST) {
			Toast.makeText(getBaseContext(), "CS onTrimMemory..."+level, Toast.LENGTH_SHORT).show();
		}
		switch (level) {
		case TRIM_MEMORY_UI_HIDDEN:
			// UI不可见
			break;
			
		case TRIM_MEMORY_BACKGROUND:
		case TRIM_MEMORY_MODERATE:
			
			break;
			
		case TRIM_MEMORY_COMPLETE:
			// 马上要被清理
			mApplication.getAppInfo().clearMemory();
			break;
			
		case TRIM_MEMORY_RUNNING_CRITICAL:
		case TRIM_MEMORY_RUNNING_LOW:
		case TRIM_MEMORY_RUNNING_MODERATE:
			// 内存不足，清理内存	
			//mApplication.getAppInfo().clearMemory();
			break;
		}
		//keepCoreService(); //[修改]注释此行
	}
	
	private void showToast(int resId) {
		if (Config.DEBUG_TOAST) {
			showToast(getString(resId));
		}
	}
	
	private void showToast(String text) {
		if (Config.DEBUG_TOAST) {
			Logger.d(TAG, "[showToast]");
			Message msg = new Message();
			msg.what = WHAT_SHOW_TOAST;
			Bundle data = new Bundle();
			data.putString(MSG_KEY_TOAST_TEXT, text);
			msg.setData(data);
			if (mHandler != null) {
				mHandler.sendMessage(msg);
			}
		}
	}
	
	
	private void keepCoreService() {
		keepCoreService(false);
	}
	
	/**
	 * 判断push是否还在运行，如果不是则启动PushService，并检查自身连接是否建立
	 */
	private void keepCoreService(boolean ping) {
		Logger.d(TAG, "[keepCoreService] net:" + NetworkManager.isConnected(this) + " exit:" + mWSP.readExitFlag());
		if (mWSP.readExitFlag() == ExitFlag.ExitFlag_AutoLogin) {
			return;
		} else if (mWSP.readExitFlag() == ExitFlag.ExitFlag_NeedLogin) {
			EventBus.getDefault().post(ReloginReason.ReloginReason_None, EventTag.ETAG_RELOGIN);
			return;
		}
		
		if (NetworkManager.isConnected(this)) { // 判断是否连接
			if (mClient == null) {
				Logger.i(TAG, "[keepCoreService] -> mClient is null -> new client");
				Logger.d(TAG, "keepCoreService -> [connectWebsocket]");
				connectWebsocket();
			} else if (!mClient.isConnected()) {
				Logger.i(TAG, "[keepCoreService] -> not connect -> try mClient.connect()");
				mClient.disconnect();
				mClient.connect();
			} else {
				if (ping) {
					Logger.i(TAG, "[keepCoreService] -> connected -> ping");
					mClient.ping();
				}
			}
		} else {
			Logger.i(TAG, "[keepCoreService] -> Network not connect");
		}		
	}
	
	class CoreServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (null == intent) {
				return;
			}
			if (intent.getAction().equals(Config.ACTION_ON_WS_HEARTBEAT)) {
				if (Config.DEBUG_TOAST) {
					showToast("WS-30s检查-coreservice:" + mClient.isConnected());
				}
				Logger.i(TAG, "WS_HEARTBEAT -- keepWebsocketService - ping");
				keepCoreService(true); // 发送ping
			}
		}
	}
	
	
	static class ServiceHandler extends Handler {
		
		WeakReference<CoreService> mService;
		
		public ServiceHandler(CoreService activity) {
			mService = new WeakReference<CoreService>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			if (null == mService.get()) {
				Logger.w(TAG, "ServiceHandler对象已被回收");
			}
			switch (msg.what) {
			case WHAT_SHOW_TOAST:
				String text = msg.getData().getString(MSG_KEY_TOAST_TEXT);
				Toast mToast = Toast.makeText(mService.get(), text, Toast.LENGTH_SHORT);
				mToast.show();
				break;
			case WHAT_SEND_REQUEST:
				String request = msg.getData().getString(MSG_KEY_REQUEST);
				mService.get().sendMessage(request);
				break;
			case WHAT_GET_TOKEN:
				mService.get().getAccessToken();
				break;
			case WHAT_WS_CONNECT:
				if (mService.get() != null && NetworkManager.isConnected(mService.get())) {
					Logger.d(TAG, "WHAT_WS_CONNECT -> [connectWebsocket]");
					mService.get().connectWebsocket(true);
				}
				break;
			case WHAT_WS_RECONNECT:
				Logger.d(TAG, "WHAT_WS_RECONNECT >>> mService.get()=" + mService.get());
				if (mService.get() != null) {
					Logger.d(TAG, "WHAT_WS_RECONNECT >>> NetworkManager.isConnected(mService.get())=" + NetworkManager.isConnected(mService.get()) + " mService.get().mWSP.readExitFlag()=" + mService.get().mWSP.readExitFlag());
				}
				if (mService.get() != null && NetworkManager.isConnected(mService.get()) && mService.get().mWSP.readExitFlag() == ExitFlag.ExitFlag_None) {
					Logger.i(TAG, "[onReceive] <WS_RECONNECT>mClient.connect()");
					mService.get().connectWebsocket();
				} else if (mService.get() != null && mService.get().mWSP.readExitFlag() == ExitFlag.ExitFlag_NeedLogin) {
					EventBus.getDefault().post(ReloginReason.ReloginReason_None, EventTag.ETAG_RELOGIN);
				}
				break;
			case WHAT_KEEP_SERVICE:
				mService.get().keepCoreService();
				break;
			case WHAT_WAKE_RELEASE:
				WakeUtil.releaseWakeLock(mService.get().getBaseContext());
				break;
			default:
				break;
			}
		}
	}
	

	public void sendMessage(String data) {
		if (mClient == null) {
			Logger.i(TAG, "[sendMessage] -> mClient is null return");
			if (Config.DEBUG_TOAST) {
				showToast("[sendMessage] websocket client null!");
			}
			mHandler.sendEmptyMessage(WHAT_WS_RECONNECT);
			return;
		} else if (!mClient.isConnected()) {
			Logger.i(TAG, "[sendMessage] -> not connect -> try connect");
			if (Config.DEBUG_TOAST) {
				showToast("[sendMessage] websocket not connected!");
			}
			mHandler.sendEmptyMessage(WHAT_WS_RECONNECT);
			return;
		}
		
		MobclickAgent.onEvent(this, "ALL_SEND_MESSAGE");
		Logger.i(TAG, "sendMessage(" + mTotalSend + ")>>>:" + data);
		mClient.send(data);
		mTotalSend++;
	}


	@Override
	public void onConnect() {
		Logger.i(TAG, ">>>onConnect<<<");
		connecting = false;
		mTotalSend = 0;
		
		if (mReconFlag) {
			Logger.i(TAG, "^^^onConnect^^^ mReconFlag");
			if (Config.LOG_LEVEL >= 1 && mApplication.isAppForeground()) {
				showToast(R.string.on_websocket_connect);
			}
			// 发送重连通知
			Logger.d(TAG, "eventbus -> postEvent ETAG_CONNECTION_CHANGE: true");
			EventBus.getDefault().post(Boolean.valueOf(true), EventTag.ETAG_CONNECTION_CHANGE);
		}
		mReconFlag = true;
	}

	@Override
	public void onMessage(final String message) {
		Logger.i(TAG, ">>>onMessage<<<:" + message);
		MobclickAgent.onEvent(this,"ALL_RECV_MESSAGE");
		
		QAOManager qaoManager = new QAOManager(getApplicationContext(), mHandler);
		try {
			qaoManager.receive(message);
		} catch (JSONException e) {
			Logger.e(TAG, "JSONException:" + e.toString());
			e.printStackTrace();
		} catch (NumberFormatException e) {
			Logger.e(TAG, "NumberFormatException:" + e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(byte[] data) {
		Logger.d(TAG, ">>>onMessage[byte]<<<" + data);
	}

	@Override
	public void onDisconnect(int code, String reason) {
		Logger.w(TAG, ">>>onDisconnect<<< [code:" + code + "]: " + reason);
		offline();
		
//		if (mWSP.readExitFlag() != ExitFlag.ExitFlag_None) { /* 主动退出检查 */
//			Logger.d(TAG, "[onDisconnect] -- Websocket连接主动退出 --");
//			stopSelf();
//			return;
//		}
		
		/* 判断断线原因并处理 */
		switch (code) {
		case -1:
			return;
		
		case 10: // 强制断开重连
			mClient = null;
			connectWebsocket();
			return;
		
		case 1: // off line
			mApplication.setAppStatus(AppStatus.AppStatus_Exit);
			mWSP.saveExitFlag(ExitFlag.ExitFlag_AutoLogin);
			stopSelf();
			return;
		case 1000: // 重复登录
			mApplication.setAppStatus(AppStatus.AppStatus_Exit);
			mWSP.saveExitFlag(ExitFlag.ExitFlag_NeedLogin);
			stopSelf();
			// 需要重新登录
			mApplication.getWorkerSp().clearAutoLogin();
			mApplication.terminate(); // 被迫退出
			MobclickAgent.onEvent(this, "APP_LOGOUT");
			EventBus.getDefault().post(ReloginReason.ReloginReason_Code1000, EventTag.ETAG_RELOGIN);
			return;
		default:
			break;
		}
		
		// 通知连接断开
		if (mApplication.isAppForeground()) {
			EventBus.getDefault().post(Boolean.valueOf(false), EventTag.ETAG_CONNECTION_CHANGE);
		}
		if (mApplication.isAppForeground()) {
			showToast(R.string.on_websocket_disconnect);
		}
		
		// 网络原因则尝试重连
		if (NetworkManager.isConnected(this) && mWSP.readExitFlag() == ExitFlag.ExitFlag_None) {
			mHandler.sendEmptyMessage(WHAT_WS_RECONNECT);
		}
	}

	@Override
	public void onError(Exception error) {
		Logger.e(TAG, ">>>onError<<<[" + mClient.getStatusCode() + "]: " + error.getMessage());
		connecting = false;
		offline();
		/* 通知连接断开 */
		if (mApplication.isAppForeground()) {
			EventBus.getDefault().post(Boolean.valueOf(false), EventTag.ETAG_CONNECTION_CHANGE);
		}
		if (mApplication.isAppForeground()) {
			showToast(R.string.on_websocket_disconnect);
		}
		
//		/* 主动退出检查 */
//		if (mWSP.readExitFlag() != ExitFlag.ExitFlag_None) {
//			Logger.i(TAG, "[onError] -- Websocket连接主动退出 --");
//			if (mClient != null && mClient.isConnected()) {
//				mClient.disconnect();
//			}
//			stopSelf();
//			return;
//		}
		
		MobclickAgent.onEvent(this, "WEBSOCKET_ONERROR");

		if (mClient.isConnected()) {
			mClient.disconnect(-6, error.getMessage());
		}
		 
		/* 连接建立失败处理 */
		if (mClient.getStatusCode() != HttpStatus.SC_SWITCHING_PROTOCOLS) {
			switch (mClient.getStatusCode()) {
			case 406: // authorization失效
			case 404: // url错误
				/* 需重新登录 */
				// [修改]需进入UserLoginActivity
				mApplication.setAppStatus(AppStatus.AppStatus_Exit);
				mWSP.saveExitFlag(ExitFlag.ExitFlag_NeedLogin);
				stopSelf();
				// 需要重新登录
				EventBus.getDefault().post(ReloginReason.ReloginReason_AuthFailed, EventTag.ETAG_RELOGIN);
				break;
			}
			return;
		}
		
		/* 判断是否登录失败(非登录失败需重连) */
		if (mApplication.getLoginStatus() == LoginStatus.LoginStatus_LoginFailed) { // 失败重试条件:非认证失败，(一般是网络切换、断开)
			Logger.e(TAG, "[onError] LoginStatus_LoginFailed, not retry");
			return;
		}
		
		/* 网络原因则尝试重连 */
		if (NetworkManager.isConnected(this) && mWSP.readExitFlag() == ExitFlag.ExitFlag_None) {
			mHandler.sendEmptyMessage(WHAT_WS_RECONNECT);
		}
	}
	
	private void offline() {
		mApplication.getAppInfo().getCustomerMap().clear();
		mApplication.getAppInfo().getUser().setStatus(QAODefine.STATUS_OFFLINE);
		for (ArchWorkerBean worker : mApplication.getAppInfo().getWorkerMap().values()) {
			worker.setStatus(QAODefine.STATUS_OFFLINE);
		}
		mApplication.getAppInfo().getCustomerMap().clear();
	}
	
	@Override
	public void onNetworkStatusChange(int netStatus, int oldStatus) {
		Logger.i(TAG, "[onNetworkStatusChange] -> " + netStatus);
		switch (netStatus) {
		case NetworkManager.NETWORK_MOBILE:
		case NetworkManager.NETWORK_WIFI:
			connectWebsocket(true); // 重连
			break;
			
		case NetworkManager.NETWORK_NONE:
			
			break;
		}
	}
	
	/***** event *****/
	
	@Subscriber(tag = EventTag.ETAG_SEND_MSG, mode=ThreadMode.MAIN)
	private void receiveMessage(String msgStr){
		if (null != msgStr) {
			sendMessage(msgStr);
		}
	}
	
	@Subscriber(tag = EventTag.ETAG_ON_LINE, mode=ThreadMode.MAIN)
	private void online(Boolean forceCon){ // 参数表示是否强制重连
		Logger.d(TAG + "-eventbus", "eventbus -> ETAG_ON_LINE");
		connectWebsocket(forceCon);
	}
	
	@Subscriber(tag = EventTag.ETAG_OFF_LINE, mode=ThreadMode.MAIN)
	private void offline(Boolean quit){ // 参数表示是否退出登录
		Logger.d(TAG + "-eventbus", "eventbus -> ETAG_OFF_LINE");
		if (mClient != null) {
			//mClient.close(1000, "Normal close");
			if (quit) {
				mClient.disconnect(2, "Logout close"); // [修改]退出登录，需要重新登录
			} else {
				mClient.disconnect(1, "Off line"); // [修改]退出到后台OffLine
			}
		}
		mApplication.setAppStatus(AppStatus.AppStatus_Exit);
		if (quit) {
			mWSP.saveExitFlag(ExitFlag.ExitFlag_NeedLogin);
		} else {
			mWSP.saveExitFlag(ExitFlag.ExitFlag_AutoLogin);
		}
		stopSelf();
	}
}
