package com.v5kf.client.lib;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

import com.v5kf.client.lib.NetworkManager.NetworkListener;
import com.v5kf.client.lib.V5ClientAgent.OnMessageRunnable;
import com.v5kf.client.lib.V5KFException.V5ExceptionStatus;
import com.v5kf.client.lib.V5WebSocketHelper.WebsocketListener;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MessageDefine;

public class V5ClientService extends Service implements NetworkListener, WebsocketListener {

	public static final String TAG = "V5ClientService";
	private static final String ACTION_ALARM = "com.v5kf.client.alarm"; 
	public static final String ACTION_SEND = "com.v5kf.client.send"; 

	private ServiceReceiver mMsgReceiver;
	private ServiceReceiver mAlarmReceiver;
	private NetworkManager mNetReceiver;
	Handler mHandler;
	private String mUrl;
	private static V5WebSocketHelper mClient;
	
	private DBHelper mDBHelper;
	private V5ConfigSP mConfigSP;
	private boolean cacheLocalMsg;
	private long mSessionStart;
	
	private int mReAuthCount = 0;
	private boolean _block = false;
	
	protected static boolean isConnected() {
		if (mClient != null && mClient.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	protected static void reConnect(Context context) {
//		if (mClient != null) {
//			mClient.connect();
//		} else {
			Intent i = new Intent(context, V5ClientService.class);
			context.startService(i);
//		}
	}

	protected static void close() {
		if (mClient != null) {
			mClient.close(1000, "Normal close");
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		if (Build.VERSION.SDK_INT < 18)
			startForeground(-1213, new Notification());
//		else 
//			startForeground(0, new Notification());
		initService();
	}
	
	private void initService() {
		mHandler = new ServiceHandler(this);
		
		mMsgReceiver = new ServiceReceiver();
		IntentFilter filter = new IntentFilter();
		//filter.addAction(ACTION_ALARM);
		filter.addAction(ACTION_SEND);
		LocalBroadcastManager.getInstance(this).registerReceiver(mMsgReceiver, filter);
		
		mAlarmReceiver = new ServiceReceiver();
		registerReceiver(mAlarmReceiver, new IntentFilter(ACTION_ALARM));
		
		mNetReceiver = new NetworkManager();		
		registerReceiver(
				mNetReceiver, 
				new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

		NetworkManager.addNetworkListener(this);
		initAlarm();
	}
	
	/**
	 * 定时任务初始化
	 */
	private void initAlarm() {
		Intent intent = new Intent(ACTION_ALARM);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 20 * 1000, 30 * 1000, pi);
		Logger.i(TAG, "[Alarm - start] -> com.v5kf.client.alarm");
	}
	
	/**
	 * 取消定时任务
	 */
	private void cancelAlarm() {
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);  
        Intent intent = new Intent(ACTION_ALARM);  
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        if (pi != null){  
            am.cancel(pi);
        }
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mDBHelper == null) {
			mDBHelper = new DBHelper(this);
		}
		if (mConfigSP == null) {
			mConfigSP = new V5ConfigSP(this);
		}
		cacheLocalMsg = mConfigSP.readLocalDbFlag();
		mSessionStart = V5ClientAgent.getInstance().getCurrentSessionStart();
		
		/* 开启service前需确保认证授权authorization不为空 */
		connectWebsocket(true);
		return super.onStartCommand(intent, flags, startId);
	}
	
	private synchronized void connectWebsocket() {
		connectWebsocket(false);
	}
	
	private synchronized void connectWebsocket(boolean forceNew) {
		if (_block) {
			Logger.w(TAG, "[connectWebsocket] _block return");
			return;
		}
		if (mClient != null && mClient.isConnected()) {
			Logger.w(TAG, "[connectWebsocket] isConnected return");
			return;
		}
		_block = true;
		Logger.w(TAG, "[connectWebsocket] auth:" +  V5ClientConfig.getInstance(this).getAuthorization());
		V5ClientConfig config = V5ClientConfig.getInstance(this);
		if (mClient != null) { // 已连接
			mClient.disconnect();
			mClient = null;
		}
		
//		if (mClient != null && mClient.isConnected()) { // 已连接
//			Logger.w(TAG, "[connectWebsocket] -> mClient != null && mClient.isConnected() = true");
//			if (forceNew) {
//				Logger.i(TAG, "disconnect and then forceNew client");
//				mClient.disconnect(1, "Stop and new client");
//			} else {
//				return;
//			}
//		} else if (mClient != null) {
//			Logger.w(TAG, "[connectWebsocket] -> mClient != null && mClient.isConnected() = false");
//			if (forceNew) {
//				Logger.i(TAG, "forceNew client");
//				mClient = null;
//				mUrl = String.format(Locale.CHINA, V5ClientConfig.WS_URL_FMT, config.getAuthorization());
//				mClient = new V5WebSocketHelper(URI.create(mUrl), this, null);
//			}			
//			mClient.connect();
//			if (mUrl != null) {
//				Logger.i(TAG, "url:" + mUrl);
//			}
//		} else {
//			Logger.w(TAG, "[connectWebsocket] -> mClient == null");
			if (config.getAuthorization() == null) {
				//[修改]认证过期或者失效
				if (mReAuthCount < 3) {
					try {
						V5ClientAgent.getInstance().doAccountAuth();
						mReAuthCount++;
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					V5ClientConfig.getInstance(this).shouldUpdateUserInfo();
					// [修改]通知界面是否重试
					V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionWSAuthFailed, "authorization failed"));
				}
				_block = false;
				return;
			}
			mUrl = String.format(Locale.CHINA, V5ClientConfig.WS_URL_FMT, config.getAuthorization());
			mClient = new V5WebSocketHelper(URI.create(mUrl), this, null);
			Logger.i(TAG, "visitor_id:" + config.getV5VisitorId());
			mClient.connect();
			if (mUrl != null) {
				Logger.i(TAG, "url:" + mUrl);
			}
//		}
	}
	
//	private synchronized void connectWebsocket() {
//		Logger.d(TAG, "[connectWebsocket]");
//		if (mClient != null && mClient.isConnected()) { // 已连接
//			Logger.e(TAG, "[connectWebsocket] -> mClient != null && mClient.isConnected()");
//			return;
//		}
//		if (mClient == null) {
//			V5ClientConfig config = V5ClientConfig.getInstance(this);
//			if (config.getAuthorization() == null) {
//				//[修改]认证过期或者失效
//				if (mReAuthCount < 3) {
//					try {
//						V5ClientAgent.getInstance().doAccountAuth();
//						mReAuthCount++;
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//				} else {
//					mClient = null;
//					V5ClientConfig.getInstance(this).shouldUpdateUserInfo();
//					// [修改]通知界面是否重试
//					V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionWSAuthFailed, "authorization failed"));
//				}
//				return;
//			}
//			mUrl = String.format(Locale.CHINA, V5ClientConfig.WS_URL_FMT, config.getAuthorization()); 
//			mClient = new WebSocketClient(URI.create(mUrl), this, null);
//			Logger.i(TAG, "visitor_id:" + config.getV5VisitorId());
//		}
//		mClient.connect();
//		if (mUrl != null) {
//			Logger.i(TAG, "url:" + mUrl);
//		}
//	}
	
	private void keepService() {
		Logger.d(TAG, "[keepService] net:" + NetworkManager.isConnected(this));
		if (NetworkManager.isConnected(this)) { // 判断是否连接
			if (mClient == null) {
				Logger.i(TAG, "[keepService] -> mClient is null -> new client");
				connectWebsocket();
			} else if (!mClient.isConnected()) {
				Logger.i(TAG, "[keepService] -> not connect -> try connect");
				mClient.connect();
			} else {
				Logger.i(TAG, "[keepService] -> connected");
				mClient.ping();
			}
		} else {
			Logger.i(TAG, "[keepService] -> Network not connect");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Logger.i(TAG, "V5ClientService -> onDestroy");
		if (mClient != null) {
			// 发送断连帧
			mClient.close(1000, "Normal close");
		}
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMsgReceiver);
		unregisterReceiver(mAlarmReceiver);
		unregisterReceiver(mNetReceiver);
		NetworkManager.removeNetworkListener(this);
		cancelAlarm();
		
//		if (!V5ClientAgent.getInstance().isExit()) {
//			Intent restartIntent = new Intent(getApplicationContext(), V5ClientService.class);
//			startService(restartIntent);
//		}
	}
	
	
	static class ServiceHandler extends Handler {
		
		WeakReference<V5ClientService> mService;
		
		public ServiceHandler(V5ClientService activity) {
			mService = new WeakReference<V5ClientService>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			if (null == mService.get()) {
				Logger.w(TAG, "ServiceHandler has bean GC");
			}
			switch (msg.what) {
			
			default:
				break;
			}
		}
	}
	
	class ServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (null == intent) {
				return;
			}
			Logger.d(TAG, "onReceiver:" + intent.getAction());
			if (intent.getAction().equals(ACTION_ALARM)) {
				// 定时检查
				keepService();
			} else if (intent.getAction().equals(ACTION_SEND)) {
				// 发送
				String msg = intent.getStringExtra("v5_message");
				if (null != msg) {
					sendMessage(msg);
				}
			}
		}
	}


	@Override
	public void onNetworkStatusChange(int netStatus, int oldStatus) {
		Logger.i(TAG, "[onNetworkStatusChange] -> " + netStatus);
		switch (netStatus) {
		case NetworkManager.NETWORK_MOBILE:
		case NetworkManager.NETWORK_WIFI:
			connectWebsocket();
			break;
			
		case NetworkManager.NETWORK_NONE:
			
			break;
		}
	}

	public void sendMessage(String msg) {
		Logger.i(TAG, ">>>sendMessage<<<");
		if (mClient != null && mClient.isConnected()) {
			mClient.send(msg);
			Logger.i(TAG, ">>>sendMessage<<<:" + msg);
		} else {
			V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionNotConnected, "connection closed"));
		}
	}

	@Override
	public void onConnect() { // 连接成功后才可以开始调用消息接口
		Logger.i(TAG, ">>>onConnect<<< URL:" + mUrl);
		mReAuthCount = 0;
		// onConnect回调 [修改]get_messages完成后回调
//		if (null != V5ClientAgent.getInstance().getHandler()) { 
//			V5ClientAgent.getInstance().getHandler().post(new Runnable() {
//				
//				@Override
//				public void run() {
//					if (V5ClientAgent.getInstance().getMessageListener() != null) {
//						V5ClientAgent.getInstance().getMessageListener().onConnect();
//					}
//				}
//			});
//		} else if (V5ClientAgent.getInstance().getMessageListener() != null) {
//			V5ClientAgent.getInstance().getMessageListener().onConnect();
//		}
		
		V5ClientAgent.getInstance().sendOnLineMessage(); // 发上线消息
		if (V5ClientConfig.AUTO_WORKER_SERVICE) { // 自动转人工客服
			V5ClientAgent.getInstance().switchToArtificialService(null);
		}
		V5ClientAgent.getInstance().updateMessages();
//		else if (V5ClientAgent.getInstance().isSessionStart()) { // 显示开场白判断
//			V5ClientAgent.getInstance().getSiteInfo(this);
//			V5ClientAgent.getInstance().setSessionStart(false);
//		}
		
		_block = false;
	}

	@Override
	public void onMessage(String message) {
		Logger.i(TAG, ">>>onMessage<<<:" + message);
		try {
			JSONObject json = new JSONObject(message);
			if (json.optString("o_type").equals("message")) {
				V5Message messageBean = V5MessageManager.receiveMessage(json);
				messageBean.setSession_start(mSessionStart);
				if (messageBean.getMsg_id() > 0 && messageBean.getMsg_id() < V5ClientAgent.OPEN_QUES_MAX_ID) {
					// 开场问题的答案，在Activity里缓存
				} else if (null != mDBHelper && cacheLocalMsg) {
					mDBHelper.insert(messageBean);
				}
				if (null != V5ClientAgent.getInstance().getHandler()) { // 回调到Message消息接口
					V5ClientAgent.getInstance().getHandler().post(new OnMessageRunnable(messageBean));
				} else if (V5ClientAgent.getInstance().getMessageListener() != null) {
					V5ClientAgent.getInstance().getMessageListener().onMessage(messageBean);
				}
				if (V5ClientConfig.NOTIFICATION_SHOW) { // 显示通知(自定义通知添加到此处)
					Notification notification = V5ClientConfig.getNotification(getApplicationContext(), 
							messageBean);
					NotificationManager nm = V5ClientConfig.getNotificationManager(getApplicationContext());
					nm.notify(V5ClientConfig.getNotifyId(getApplicationContext()), notification);
					
					// 发出消息广播
					Intent intent = new Intent(V5ClientConfig.ACTION_NEW_MESSAGE);
					Bundle bundle = new Bundle();
					bundle.putSerializable("v5_message", messageBean);
					intent.putExtras(bundle);
					sendBroadcast(intent);
				}
			} else if (json.optString("o_type").equals("session")) {
				if (json.optString("o_method").equals("get_status")) { // 对话状态信息：机器人或者客服信息
					
				} else if (json.optString("o_method").equals("get_messages")) {
					//boolean finish = json.optBoolean("finish");
//					int offset = json.optInt("offset");
//					int size = json.optInt("size");
					List<V5Message> msgs = new ArrayList<V5Message>();
					JSONArray messages = json.optJSONArray("messages");
					if (null != messages && messages.length() > 0) {
						for (int i = 0; i < messages.length(); i++) {
							JSONObject item = messages.getJSONObject(i);
							V5Message msg = V5MessageManager.receiveMessage(item);
							if (msg.getMsg_id() > 0 && msg.getMsg_id() < V5ClientAgent.OPEN_QUES_MAX_ID) {
								// 排除开场问题
							} else {
								msgs.add(0, msg);
							}
							V5Message candidate = null;
							if (msg.getCandidate() != null && msg.getCandidate().size() > 0) {
								candidate = msg.getCandidate().get(0);
								if (candidate.getDirection() == V5MessageDefine.MSG_DIR_FROM_ROBOT) {
									msgs.add(0, candidate);
									if (null != mDBHelper && cacheLocalMsg) {
										mDBHelper.insert(candidate);
									}
								}
								msg.setCandidate(null);
							}
							if (null != mDBHelper && cacheLocalMsg) {
								mDBHelper.insert(msg);
							}
						}
					}
					if (V5ClientAgent.getInstance().mMsgIdCount == 0) {
						V5ClientAgent.getInstance().mMsgIdCount = msgs.size() + 1; //+V5Util.getCurrentLongTime()%100
					}
//					
//					if (offset == 0 && size == 0 && V5ClientAgent.getInstance().mOpenMode == ClientOpenMode.clientOpenModeQuestion
//							&& V5ClientAgent.getInstance().mOpenQuestion != null) {
//						V5TextMessage openQuestion = V5MessageManager.getInstance().obtainTextMessage(V5ClientAgent.getInstance().mOpenQuestion);
//						openQuestion.setMsg_id(mMsgIdCount++);
//						this.sendMessage(openQuestion.toJson());
//					} else if (offset == 0 && size == 0 && 
//							V5ClientAgent.getInstance().mOpenMode == ClientOpenMode.clientOpenModeDefault) {
//						// 没有获得会话消息则获取开场白
//						V5ClientAgent.getInstance().getPrologue();
//					}
					
					// [修改]取消GetMessagesCallback回调
//					if (V5ClientAgent.getInstance().getGetMessagesCallback() != null) {
//						if (mHandler != null) {
//							mHandler.post(new Runnable() {							
//								@Override
//								public void run() {
//									V5ClientAgent.getInstance().getGetMessagesCallback().complete(msgs, offset, size, finish);
//									V5ClientAgent.getInstance().setGetMessagesCallback(null);
//								}
//							});
//						} else {
//							V5ClientAgent.getInstance().getGetMessagesCallback().complete(msgs, offset, size, finish);
//							V5ClientAgent.getInstance().setGetMessagesCallback(null);
//						}
//					}
					// onConnect回调
					if (null != V5ClientAgent.getInstance().getHandler()) { 
						V5ClientAgent.getInstance().getHandler().post(new Runnable() {
							
							@Override
							public void run() {
								if (V5ClientAgent.getInstance().getMessageListener() != null) {
									V5ClientAgent.getInstance().getMessageListener().onConnect();
								}
							}
						});
					} else if (V5ClientAgent.getInstance().getMessageListener() != null) {
						V5ClientAgent.getInstance().getMessageListener().onConnect();
					}
				}
			} else if (json.has("o_error")) {
				int code = json.getInt("o_error");
				if (code != 0) {
					String desc = json.optString("o_errmsg");
					V5ClientAgent.getInstance().errorHandle(new V5KFException(
							V5ExceptionStatus.ExceptionServerResponse, "[" + code + "]" + desc));//认证错误
				}
			} else {
				if (null != V5ClientAgent.getInstance().getHandler()) { // 回调到字符串消息接口
					V5ClientAgent.getInstance().getHandler().post(new OnMessageRunnable(message));
				} else if (V5ClientAgent.getInstance().getMessageListener() != null) {
					V5ClientAgent.getInstance().getMessageListener().onMessage(message);
				}
			}
		} catch (JSONException e) {
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
		// 断开重连判断
		if (V5ClientAgent.getInstance().isExit()) {
			// 不重连
			mClient = null;
			stopSelf();
			Logger.w(TAG, "[onDisconnect] stop service");
		} else {
			// 因网络原因重连
			if (NetworkManager.isConnected(this)) {
				switch (code) {
				case -1: // 正常关闭，不重连
					break;
				case 1:
					mClient = null;
					connectWebsocket();
					break;
				case 1000: // 同一uid重复登录
					V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionConnectRepeat, "connection is cut off by same u_id"));
					break;
				case 1005: //
				case 1006: // (websocket关闭前一连接abnormal)
					break;
				default:
//					connectWebsocket();
//					V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionConnectionError, "[" + code + "]" + reason));
					break;
				}
			} else {
				V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionNoNetwork, "no network"));
			}
		}
		_block = false;
	}

	@Override
	public void onError(Exception error) {
		Logger.e(TAG, ">>>onError<<<status code:" + mClient.getStatusCode() + " " + error.getMessage());
		if (mClient != null && mClient.isConnected()) {
			mClient.disconnect();
		}
		
		if (V5ClientAgent.getInstance().isExit()) {
			// 不重连
			stopSelf();
		} else {
			if (!NetworkManager.isConnected(this)) {
				V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionNoNetwork, "no network"));
			} else {
				if (mClient.getStatusCode() == 406 || mClient.getStatusCode() == 404) {
					Logger.d(TAG, "onError mReAuthCount:" + mReAuthCount);
					if (mReAuthCount < 3) {
						try {
							V5ClientAgent.getInstance().doAccountAuth();
							mReAuthCount++;
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else {
						mClient = null;
						V5ClientConfig.getInstance(this).shouldUpdateUserInfo();
						V5ClientAgent.getInstance().errorHandle(new V5KFException(V5ExceptionStatus.ExceptionWSAuthFailed, "authorization failed"));
					}
				} else {
					connectWebsocket();
					V5ClientAgent.getInstance().errorHandle(new V5KFException(
							V5ExceptionStatus.ExceptionConnectionError, "[" + mClient.getStatusCode() + "]" + error.getMessage()));
				}
			}
		}
		_block = false;
	}
}
