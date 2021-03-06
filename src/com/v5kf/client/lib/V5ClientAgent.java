package com.v5kf.client.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.v5kf.client.lib.V5KFException.V5ExceptionStatus;
import com.v5kf.client.lib.callback.HttpResponseHandler;
import com.v5kf.client.lib.callback.MessageSendCallback;
import com.v5kf.client.lib.callback.OnGetMessagesCallback;
import com.v5kf.client.lib.callback.OnLocationMapClickListener;
import com.v5kf.client.lib.callback.OnURLClickListener;
import com.v5kf.client.lib.callback.V5InitCallback;
import com.v5kf.client.lib.callback.V5MessageListener;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5JSONMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MessageDefine;
import com.v5kf.client.lib.entity.V5TextMessage;
import com.v5kf.client.lib.entity.V5VoiceMessage;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.cache.MediaLoader;

public class V5ClientAgent {
	public static final String TAG = "ClientAgent";
	public static final long OPEN_QUES_MAX_ID = 9999999999L;
	
	public final static String VERSION = "1.0.4";
	private static boolean isSDKInit = false;
	private int isForeground = 0;
	
	private Context mContext;
	private V5MessageListener mMessageListener;
//	private OnGetMessagesCallback mGetMessagesCallback;
	private OnURLClickListener mURLClickListener;
	private OnLocationMapClickListener mLocationMapClickListener;
	private Handler mHandler;
	private DBHelper mDBHelper;
	private V5ConfigSP mConfigSP;
	private long mSessionStart = 0;
	protected boolean cacheLocalMsg;
//	public ClientOpenMode mOpenMode;
//	public String mOpenQuestion;
	protected long mMsgIdCount = 0;	// 开场问题Id
	
	public enum ClientOpenMode {
		clientOpenModeDefault,	// 默认开场白方式（无消息记录显示默认开场白）
		clientOpenModeQuestion,	// 自定义开场白，设置开场问题获得对应开场白（此模式不建议与自动转人工客服同用，一旦问题转到人工客服则需要客服人工回复消息）
		clientOpenModeNone		// 无开场白方式，有则显示历史消息
	};
	
	private static class SingletonHolder {
		private static final V5ClientAgent singletonHolder = new V5ClientAgent();
	}
	
	private V5ClientAgent() {
//		mOpenMode = ClientOpenMode.clientOpenModeDefault;
		Logger.w(TAG, "V5ClientAgent instance");
		if (Looper.myLooper() != null) {
			mHandler = new Handler(Looper.myLooper());
			Logger.i(TAG, "The callbak method of MessageListener will run in the current UI thread");
		} else {
			Logger.i(TAG, "The callbak method of MessageListener will run in another thread");
		}
	}
	
	public static V5ClientAgent getInstance() {
		return SingletonHolder.singletonHolder;
	}
	
	public static String getVersion() {
		return VERSION;
	}
	
	/**
	 * 在Application的onCreate中进行初始化，从AndroidManifest.xml读取配置信息
	 * @param conetxt
	 * @param callback
	 */
	public static void init(Context context, V5InitCallback callback) {
		if (context == null) {
			if (callback != null) {
				callback.onFailure("SDK auth failed: context null");
			}
			isSDKInit = false;
			return;
		}
		
		V5ClientConfig config = V5ClientConfig.getInstance(context);
		String siteId = config.getSiteId();
		String siteAccount = config.getSiteAccount();
		V5ConfigSP configSP = new V5ConfigSP(context);
		if (configSP.readSDKAuthFlag()) {
			if (configSP.readSiteId() != null && 
					!configSP.readSiteId().equals(siteId)) {
				configSP.removeSDKAuthFlag();
				configSP.removeUid();
				String vid = configSP.readVisitorId();
				if (vid != null) {
					configSP.removeAuthorization(vid);
					configSP.removeVisitorId();
				}
				isSDKInit = false;
			} else {
				config.setSiteId(siteId);
				config.setSiteAccount(siteAccount);
				isSDKInit = true;
			}
		}
		if (!isSDKInit) {
			configSP.saveSiteId(siteId);
			configSP.saveSiteAccount(siteAccount);
			try {
				doSDKAuth(context, siteId, siteAccount, callback);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 在Application的onCreate中进行初始化，通过代码填写配置信息
	 * @param conetxt
	 * @param appkey
	 * @param siteId
	 * @param siteAccount
	 * @param callback
	 */
	public static void init(Context context, String siteId, String siteAccount, V5InitCallback callback) {
		if (null == siteId || null == siteAccount || context == null) {
			if (callback != null) {
				callback.onFailure("SDK auth failed: param null");
			}
			isSDKInit = false;
			return;
		}
		
		V5ConfigSP configSP = new V5ConfigSP(context);
		V5ClientConfig config = V5ClientConfig.getInstance(context);
		if (configSP.readSDKAuthFlag()) {
			if (configSP.readSiteId() != null && 
					!configSP.readSiteId().equals(siteId)) {
				configSP.removeSDKAuthFlag();
				configSP.removeUid();
				String vid = configSP.readVisitorId();
				if (vid != null) {
					configSP.removeAuthorization(vid);
					configSP.removeVisitorId();
				}
				isSDKInit = false;
			} else {
				config.setSiteId(siteId);
				config.setSiteAccount(siteAccount);
				isSDKInit = true;
			}
		}
		if (!isSDKInit) {
			config.setSiteId(siteId);
			config.setSiteAccount(siteAccount);
			try {
				doSDKAuth(context, siteId, siteAccount, callback);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * SDK授权认证
	 * @param appkey
	 * @param siteId
	 * @param siteAccount
	 * @param callback
	 */
	private static void doSDKAuth(final Context context, String siteId,
			String siteAccount, final V5InitCallback callback) throws JSONException {
		// 表情模块初始化
		//EmoticonsUtils.initEmoticonsDB(context);
				
		if (null == siteId || null == siteAccount) {
			if (callback != null) {
				callback.onFailure("SDK auth failed: param invalid");
			}
			isSDKInit = false;
			return;
		}
		
		JSONObject json = new JSONObject();
		json.put("site_id", siteId);
		json.put("account", siteAccount); // 获得用户唯一ID
		json.put("platform", "android");
		Logger.d(TAG, "<Init request>: " + json.toString());
		V5HttpUtil.post(
				V5ClientConfig.SDK_INIT_URL, 
				json.toString(), 
				new HttpResponseHandler(context) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				responseString = V5Util.decodeUnicode(responseString);
				Logger.d(TAG, "<Init response>: " + responseString);
				try {
					JSONObject js = new JSONObject(responseString);
					int o_error = js.getInt("o_error");
					if (o_error == 0) { // 成功返回
						isSDKInit = true;
						V5ConfigSP configSP = new V5ConfigSP(context);
						configSP.saveSDKAuthFlag(true);
						int appPush = js.optInt("app_push");
						String appTitle = js.optString("app_title");
						String version = js.optString("version");
						String versionInfo = js.optString("version_info");

						configSP.saveAppPush(appPush);
						if (null != appTitle && !appTitle.isEmpty()) {
							configSP.saveNotificationTitle(appTitle);
						}
						if (VERSION.compareTo(version) < 0) {
							Logger.w(TAG, "V5 SDK info:" + versionInfo);
						}
						if (callback != null) {
							callback.onSuccess("SDK auth success");
						}
					} else if (js.has("o_errmsg")) {
						String errmsg = js.getString("o_errmsg");
						Logger.e(TAG, "V5 SDK init failed(code:" + o_error + "):" + errmsg);
						if (callback != null) {
							callback.onFailure("SDK auth failed: " + errmsg);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "V5 SDK init failed(code:" + statusCode + "):" + responseString);
				if (callback != null) {
					callback.onFailure("SDK auth failed: " + responseString);
				}
			}
		});
	}

	/**
	 * 是否连接
	 * @return
	 */
	public static boolean isConnected() {
		return V5ClientService.isConnected();
	}

	/**
	 * 上线。服务初始化
	 * @param context
	 */
	public void start(Context context, V5MessageListener listener) {
		if (null == context || null == listener) {
			Logger.e(TAG, "[V5ClientAgent->start] param null");
			return;
		}
		setMessageListener(listener);
		setContext(context);
		V5ClientConfig config = V5ClientConfig.getInstance(mContext);
		if (mDBHelper == null) {
			mDBHelper = new DBHelper(context);
			mDBHelper.setTableName("v5_message_" + config.getV5VisitorId());
		}
		if (mConfigSP == null) {
			mConfigSP = new V5ConfigSP(context);
		}
		cacheLocalMsg = mConfigSP.readLocalDbFlag();
//		if (mSessionStart == 0) {
//			mSessionStart = mConfigSP.readSessionStart();
//		}
		if (mConfigSP.readAuthorization(config.getV5VisitorId()) == null) {
			Logger.d(TAG, "[start] initialization - should do auth");		
			try {
				doAccountAuth();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			Logger.d(TAG, "[start] already auth - start client");
			onClientStart();
		}
	}

	/**
	 * 重连websocket
	 */
	public void reconnect() {
		if (null != mContext) {
			V5ClientService.reConnect(mContext);
		} else {
			Logger.e(TAG, "V5ClientAgent got null context! Please do V5ClientAgent.getInstance().start()");
		}
	}
	
	/**
	 * 服务退出判断
	 * @return
	 */
	protected boolean isExit() {
		if (mMessageListener == null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 账号认证
	 * @param context
	 * @throws JSONException
	 */
	protected void doAccountAuth() throws JSONException {
		JSONObject json = new JSONObject();
		V5ClientConfig config = V5ClientConfig.getInstance(mContext);
		json.put("site", config.getSiteId());
		json.put("account", config.getSiteAccount());
		json.put("visitor", config.getV5VisitorId()); // 获得用户唯一ID
		json.put("device", "android");
		String device_token = config.getDeviceToken();
		if (device_token != null) {
			json.put("dev_id", device_token);
		} else {
			Logger.w(TAG, "device_token not set!");
		}
		json.put("expires", 604800); // 一次Auth为7天有效期
		if (null != config.getNickname()) {
			json.put("nickname", config.getNickname());
		}
		if (0 != config.getGender()) {
			json.put("gender", config.getGender());
		}
		if (null != config.getAvatar()) {
			json.put("avatar", config.getAvatar());
		}
		if (mDBHelper != null) { // 更新表名：v5_message_[visitor_id]
			mDBHelper.setTableName("v5_message_" + config.getV5VisitorId());
		}
		Logger.d(TAG, "Auth:" + json.toString());
		V5HttpUtil.post(V5ClientConfig.SDK_AUTH_URL, json.toString(), new HttpResponseHandler(mContext) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				Logger.d(TAG, "[auth] statusCode=" + statusCode + " responseString=" + responseString);
				if (statusCode == 200) {
					try {
						JSONObject js = new JSONObject(responseString);
						if (js.has("authorization")) { // 成功返回authorization
							long expires = js.getLong("expires");
							long timestamp = js.getLong("timestamp");
							V5ClientConfig config = V5ClientConfig.getInstance(mContext);
							config.setExpires(expires);
							config.setTimestamp(timestamp);
							String auth = js.optString("authorization");
							if (auth != null && !auth.isEmpty()) {
								config.setAuthorization(auth);
							}
							onClientStart();
						} else if (js.has("o_error")) {
							int code = js.getInt("o_error");
							String desc = js.optString("o_errmsg");
							if (isSDKInit) {
								errorHandle(new V5KFException(V5ExceptionStatus.ExceptionAccountFailed, "[" + code + "]" + desc));
							} else {
								Logger.e(TAG, "start(): init SDK not success, please check the initialization");
								errorHandle(new V5KFException(V5ExceptionStatus.ExceptionNotInitialized, "init not success"));
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
						errorHandle(new V5KFException(V5ExceptionStatus.ExceptionAccountFailed, "JSONException:" + e.getMessage()));
					}
				} else {
					errorHandle(new V5KFException(V5ExceptionStatus.ExceptionAccountFailed, "Connect error, Auth failed."));
				}
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.w(TAG, "doAuth->onFailure(" + statusCode + "): " + responseString);
				errorHandle(new V5KFException(V5ExceptionStatus.ExceptionAccountFailed, "Connect error, Auth failed."));
			}
		});
	}
	
	/**
	 * 接口开启
	 * @param context
	 */
	private void onClientStart() {
		startClientService();
	}

//	/**
//	 * 获得会话初始标志
//	 * @return
//	 */
//	protected boolean isSessionStart() {
//		if (mContext == null) {
//			errorHandle(new V5KFException(V5KFException.ERR_CODE_NOT_START, "Client not start, Please start by V5ClientAgent.getInstance().start"));
//			return false;
//		}
//		if (mConfigSP == null) {
//			mConfigSP = new V5ConfigSP(mContext);
//		}
//		return mConfigSP.readSessionFlag();
//	}
//	
//	/**
//	 * 保存会话初始标志
//	 * @param flg
//	 */
//	protected void setSessionStart(boolean flg) {
//		if (mContext == null) {
//			errorHandle(new V5KFException(V5KFException.ERR_CODE_NOT_START, "Client not start, please start by V5ClientAgent.getInstance().start"));
//			return;
//		}
//		if (mConfigSP == null) {
//			mConfigSP = new V5ConfigSP(mContext);
//		}
//		mConfigSP.saveSessionFlag(flg);
//	}
	
	protected long getCurrentSessionStart() {
		return mSessionStart;
	}
	
	/**
	 * 获得开场消息
	 * @param mode
	 * @param param
	 */
	public void getOpeningMessage(ClientOpenMode mode, String param) {
		if (mode == ClientOpenMode.clientOpenModeQuestion
				&& param != null) {
			V5TextMessage openQuestion = V5MessageManager.obtainTextMessage(param);
			mMsgIdCount++;
			openQuestion.setMsg_id(V5Util.getCurrentLongTime()/1000);
			V5ClientAgent.getInstance().sendOpeningQuestion(openQuestion);
		} else if (mode == ClientOpenMode.clientOpenModeDefault) {
			// 没有获得会话消息则获取开场白
			V5ClientAgent.getInstance().getPrologue();
		}
	}
	
	/**
	 * 获得站点信息
	 * @param context
	 */
	private void getPrologue() {
		if (mContext == null) {
			Logger.e(TAG, "Client not start, please start by V5ClientAgent.getInstance().start");
			//errorHandle(new V5KFException(V5ExceptionStatus.ExceptionUnknownError, "Client not start, please start by V5ClientAgent.getInstance().start"));
			return;
		}
		V5HttpUtil.get(V5Util.getSiteInfoUrl(mContext), new HttpResponseHandler(mContext) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				Logger.i(TAG, "responseString:" + V5Util.decodeUnicode(responseString));
				try {
					JSONObject json = new JSONObject(V5Util.decodeUnicode(responseString));
					if (json.getString("state").equals("ok")) {
						JSONObject robot = json.getJSONObject("robot");
						String prologue = robot.getString("intro");							
						if (null != prologue) {
							V5TextMessage msg = V5MessageManager.obtainTextMessage(prologue);
							msg.setDirection(V5MessageDefine.MSG_DIR_FROM_ROBOT);
							msg.setSession_start(mSessionStart);
							msg.setMsg_id(V5Util.getCurrentLongTime()/1000);
//							if (null != mDBHelper && cacheLocalMsg) { // 保存开场白
//								mDBHelper.insert(msg);
//							}
							if (null != mHandler) { // 将开场白回调到message消息接口
								mHandler.post(new OnMessageRunnable(msg));
							} else if (getMessageListener() != null) {
								getMessageListener().onMessage(msg);
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "statusCode:" + statusCode + " responseString:" + responseString);
			}
		});
	}
	
	/**
	 * 启动WS服务
	 */
	private void startClientService() {
		if (mContext == null) {
			Logger.e(TAG, "Client not start, please start by V5ClientAgent.getInstance().start");
			//errorHandle(new V5KFException(V5ExceptionStatus.ExceptionUnknownError, "Client not start, please start by V5ClientAgent.getInstance().start"));
			return;
		}
		Intent i = new Intent(mContext, V5ClientService.class);
		mContext.startService(i);
	}
	
	public boolean isForeground() {
		return isForeground > 0;
	}
	
	/**
	 * 调用服务的Activity的onStart方法中调用(用于通知发送判断)
	 */
	public void onStart() {
		isForeground++;
		Logger.i(TAG, "<onStart> isForeground:" + isForeground);
		if (isForeground > 1) {
			return;
		} else if (isForeground < 1) {
			Logger.e(TAG, "V5CientAgent -> onStop() not match onStart()");
			return;
		}
		
		V5ClientConfig.NOTIFICATION_SHOW = false;
		if (mContext == null) {
			Logger.e(TAG, "Client not start, please start by V5ClientAgent.getInstance().start");
			//errorHandle(new V5KFException(V5ExceptionStatus.ExceptionUnknownError, "Client not start, please start by V5ClientAgent.getInstance().start"));
			return;
		}
		// 清除本应用的通知
		V5ClientConfig.getNotificationManager(mContext).cancel(V5ClientConfig.getNotifyId(mContext));
		if (V5ClientService.isConnected()) {
			sendOnLineMessage();
		}
		// [修改]点击通知 重新获取消息
		/* onStart -> 刷新最新会话数据 */
		this.updateMessages();
	}

	/**
	 * 调用服务的Activity的onStop方法中调用(用于通知发送判断)
	 */
	public void onStop() {
		isForeground--;
		Logger.i(TAG, "<onStop> isForeground:" + isForeground);
		if (isForeground > 0) {
			return;
		} else if (isForeground < 0) {
			Logger.e(TAG, "V5CientAgent -> onStop() not match onStart()");
			return;
		}
		
		V5ClientConfig.NOTIFICATION_SHOW = true;
		if (mConfigSP != null && mConfigSP.readAppPush() == 0) {
			return;
		} else {
			sendOffLineMessage();
		}
	}
	
	/**
	 * 下线。退出服务，停止接收消息，通知服务端消息发到对方推送服务器
	 */
	public void onDestroy() {
		// 发送下线
		sendOffLineMessage();
//		mMsgIdCount = 0;
//		setSessionStart(true); // 恢复会话初始状态
//		if (mConfigSP != null) {
//			mConfigSP.saveSessionStart(0);
//		}
		
		mSessionStart = 0;
		mMessageListener = null;
		
		if (mContext != null) {
			Intent i = new Intent(mContext, V5ClientService.class);
			mContext.stopService(i);
		}
	}
	
	/**
	 * 发送消息
	 * @param message
	 */
	public void sendMessage(V5Message message, MessageSendCallback handler) {
		if (mContext == null) {
			Logger.e(TAG, "Client not start, please start by V5ClientAgent.getInstance().start");
			//errorHandle(new V5KFException(V5ExceptionStatus.ExceptionUnknownError, "Client not start, please start by V5ClientAgent.getInstance().start"));
			return;
		}
		if (message.getMsg_id() == 0) {
			message.setMsg_id(V5Util.getCurrentLongTime());
		}
		message.setState(V5Message.STATE_SENDING);
		if (message.getMessage_type() == V5MessageDefine.MSG_TYPE_IMAGE) {
			// 发送图片
			V5ImageMessage imageMessage = (V5ImageMessage) message;
			if (imageMessage.getPic_url() == null && imageMessage.getFilePath() != null) {
				V5ClientConfig config = V5ClientConfig.getInstance(mContext);
				if (null != config && null != config.getAuthorization()) {
					String url = V5ClientConfig.SDK_PIC_AUTH_URL + config.getAuthorization();
					getPictureService(url, config.getAuthorization(), imageMessage, handler);
				} else {
					sendFailedHandle(handler, message, V5ExceptionStatus.ExceptionWSAuthFailed, "Authorization null, can't upload image");
				}
				return;
			}
		} else if (message.getMessage_type() == V5MessageDefine.MSG_TYPE_VOICE) {
			// 发送语音
			V5VoiceMessage voiceMessage = (V5VoiceMessage) message;
			if (voiceMessage.getUrl() == null && voiceMessage.getFilePath() != null) {
				V5ClientConfig config = V5ClientConfig.getInstance(mContext);
				if (null != config && null != config.getAuthorization()) {
					String url = V5ClientConfig.SDK_MEDIA_AUTH_URL + config.getAuthorization();
					getPictureService(url, config.getAuthorization(), voiceMessage, handler);
				} else {
					sendFailedHandle(handler, message, V5ExceptionStatus.ExceptionWSAuthFailed, "Authorization null, can't upload voice");
				}				
				return;
			}
		}
		try {			
			String json = message.toJson();
			sendMessage(json);
			
			// 根据连接状态判断是否发送成功
			if (V5ClientService.isConnected()) {
				sendSuccessHandle(handler, message);
			} else {
				sendFailedHandle(handler, message, V5ExceptionStatus.ExceptionMessageSendFailed, "connection closed");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void sendOpeningQuestion(V5Message openQues) {
		try {
			this.sendMessage(openQues.toJson());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void sendMessage(String json) {
		Logger.d(TAG, "sendMessage:" + json);
		Intent sendIntent = new Intent();
		sendIntent.putExtra("v5_message", json);
		sendIntent.setAction(V5ClientService.ACTION_SEND);
		// 通过广播发送给ws服务
		LocalBroadcastManager.getInstance(mContext).sendBroadcast(sendIntent);
	}
	
	private void getPictureService(String url, String auth, final V5Message message, final MessageSendCallback handler) {
		V5HttpUtil.getPicService(url, auth, new HttpResponseHandler(mContext) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				Logger.i(TAG, "getPictureService responseString:" + responseString);
				if (statusCode == 200) {
					try {
						JSONObject js = new JSONObject(responseString);
						if (js.has("url")) { // 成功返回signature
							String authorization = js.getString("authorization");
							String url = js.getString("url");
							String magicContext = js.optString("magic_content");
//							V5ClientConfig config = V5ClientConfig.getInstance(mContext);
//							config.setWxyturl(url);
//							config.setWxytauth(authorization);
							
							if (message.getMessage_type() == V5MessageDefine.MSG_TYPE_IMAGE) {
								postImageAndSend((V5ImageMessage)message, url, authorization, handler);
							} else if (message.getMessage_type() == V5MessageDefine.MSG_TYPE_VOICE) {
								postMediaAndSend(message, url, authorization, magicContext, handler);
							} else {
								sendFailedHandle(
										handler, 
										message, 
										V5ExceptionStatus.ExceptionMessageSendFailed, 
										"Image/Voice upload error: unsupport media type");
							}
							return;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					sendFailedHandle(
							handler, 
							message, 
							V5ExceptionStatus.ExceptionMessageSendFailed, 
							"Media/Image upload error: get upload url failed");
				}
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "getPictureService statusCode:" + statusCode + " responseString:" + responseString);
				sendFailedHandle(
						handler, 
						message, 
						V5ExceptionStatus.ExceptionMessageSendFailed, 
						"Media/Image upload error: get upload url failed");
			}
		});
	}

	private void postImageAndSend(final V5ImageMessage imageMessage, String url, String authorization, 
			final MessageSendCallback handler) {
//		V5ClientConfig config = V5ClientConfig.getInstance(mContext);
//		String url = config.getWxyturl();
//		String authorization = config.getWxytauth();
		String filePath = imageMessage.getFilePath();
		if (null == url || null == authorization) {
			sendFailedHandle(
					handler, 
					imageMessage, 
					V5ExceptionStatus.ExceptionImageUploadFailed, 
					"Image upload error: no url or authorization");
			return;
		}
		V5HttpUtil.postLocalImage(url, filePath, authorization, null, new HttpResponseHandler(mContext) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				// 解析图片地址
				Logger.i(TAG, "[postLocalImage] success responseString:" + responseString);
				try {
					JSONObject js = new JSONObject(responseString);
					int code = js.getInt("code");
//					String message = js.optString("message");
					JSONObject data = js.optJSONObject("data");
					if (null != data && 0 == code) {
//						String url = data.optString("url");
						String download_url = data.optString("download_url");
						if (null != download_url) {
							imageMessage.setPic_url(download_url);
							sendMessage(imageMessage, handler);
							return;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				sendFailedHandle(
						handler, 
						imageMessage, 
						V5ExceptionStatus.ExceptionImageUploadFailed, 
						"Image upload error: response error");
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "[postLocalImage] failure(" + statusCode + 
						") responseString:" + responseString);
				sendFailedHandle(
						handler, 
						imageMessage, 
						V5ExceptionStatus.ExceptionImageUploadFailed, 
						responseString);
			}
		});
	}
	
	protected void postMediaAndSend(final V5Message message, String url,
			String authorization, String magicContext, final MessageSendCallback handler) {
		String filePath = null;
		if (message.getMessage_type() == QAODefine.MSG_TYPE_VOICE) {
			filePath = ((V5VoiceMessage)message).getFilePath();
		}
		final File file = new File(filePath);
		V5HttpUtil.postLocalMedia(message, filePath, url, authorization, magicContext, new HttpResponseHandler(mContext) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				// TODO Auto-generated method stub
				// 解析地址
				Logger.i(TAG, "[postLocalMedia] success responseString:" + responseString);
				try {
					JSONObject js = new JSONObject(responseString);
					int code = js.getInt("code");
//					String message = js.optString("message");
					JSONObject data = js.optJSONObject("data");
					if (null != data && 0 == code) {
//						String url = data.optString("url");
						String access_url = data.optString("access_url");
						if (!TextUtils.isEmpty(access_url)) {
							if (message.getMessage_type() == QAODefine.MSG_TYPE_VOICE) {
								V5VoiceMessage voiceMessage = (V5VoiceMessage)message;
								voiceMessage.setUrl(access_url);
								voiceMessage.setUpload(true);
								// 删除临时语音文件，重命名
								MediaLoader.copyPathToFileCche(getContext(), file, access_url);
								// 非微信接口的直接发送
								Logger.i(TAG, "sendMessage -> VoiceMessage(voiceMessage, handler) " + message.getState());
								sendMessage(voiceMessage, handler);
							} else {
								// 其他文件类型
								sendFailedHandle(
										handler, 
										message, 
										V5ExceptionStatus.ExceptionMessageSendFailed, 
										"Media upload error: unsupport type");
							}
							return;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				sendFailedHandle(
						handler, 
						message, 
						V5ExceptionStatus.ExceptionMessageSendFailed, 
						"Media upload error: response error");
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "[postLocalImage] failure(" + statusCode + 
						") responseString:" + responseString);
				sendFailedHandle(
						handler, 
						message, 
						V5ExceptionStatus.ExceptionMessageSendFailed, 
						responseString);
			}
		});
		
	}

	/**
	 * 转人工客服
	 */
	public void switchToArtificialService(MessageSendCallback handler) {
		V5Message msg = V5MessageManager.obtainControlMessage(1, 0, null);
		sendMessage(msg, handler);
	}
	
	/**
	 * 发上线消息
	 */
	protected void sendOnLineMessage() {
		V5Message msg = V5MessageManager.obtainControlMessage(100, 0, null);
		try {
			sendMessage(msg.toJson());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发下线消息
	 */
	protected void sendOffLineMessage() {
		V5Message msg = V5MessageManager.obtainControlMessage(101, 0, null);
		try {
			sendMessage(msg.toJson());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 按照指定位置请求指定消息数量历史消息
	 * @param context
	 * @param offset 请求起始位置
	 * @param size	最多返回消息数
	 * @param callback 返回消息回调
	 */
	public void getMessages(Context context, int offset, int size, OnGetMessagesCallback callback) {
		if (mDBHelper == null) {
			mDBHelper = new DBHelper(context);
		}
		new Thread(new GetLocalMessageRunnable(offset, size, callback)).start();
	}
	
	/**
	 * 更新会话消息，重新连接后调用，获得新的消息后会回调onConnect
	 */
	public void updateMessages() {
		this.getCurrentMessages(0, 0, null);
	}
	
	/**
	 * 获取会话消息
	 * @param offset
	 * @param size
	 * @param callback
	 */
	protected void getCurrentMessages(int offset, int size, OnGetMessagesCallback callback) {
		Logger.i(TAG, "[updateMessages -> getCurrentMessages]");
		//setGetMessagesCallback(callback);
		try {
			JSONObject json = new JSONObject();
			json.put("o_type", "session");
			json.put("o_method", "get_messages");
			json.put("size", size);
			json.put("offset", offset);
			V5JSONMessage jsonMsg = new V5JSONMessage(json);
			sendMessage(jsonMsg.toJson());
//			sendMessage(jsonMsg, new MessageSendCallback() {
//				
//				@Override
//				public void onSuccess(V5Message message) {
//					
//				}
//				
//				@Override
//				public void onFailure(V5Message message, int statusCode, String desc) {
//					errorHandle(new V5KFException(statusCode, desc));
//				}
//			});
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 清空本地历史消息记录
	 * @param context not null
	 */
	public void clearLocalHistoricalMessages(Context context) {
		if (mDBHelper == null) {
			mDBHelper = new DBHelper(context);
		}
		mDBHelper.delAll();
	}
	
	public static void clearCache(Context context) {
		V5ConfigSP csp = new V5ConfigSP(context);
		csp.clearCache();
	}
	
	protected Context getContext() {
		return mContext;
	}

	protected void setContext(Context context) {
		mContext = context;
	}
	
	protected Handler getHandler() {
		return mHandler;
	}
	
	protected V5MessageListener getMessageListener() {
		return mMessageListener;
	}

	public void setMessageListener(V5MessageListener mListener) {
		mMessageListener = mListener;
	}
	
	/**
	 * 错误处理
	 * @param err_code
	 */
	protected void errorHandle(V5KFException ex) {
		if (null != mHandler) {
			mHandler.post(new OnErrorRunnable(ex));
		} else if (null != getMessageListener()) {
			getMessageListener().onError(ex);
		}
	}
//	
//	/**
//	 * poll返回错误码处理
//	 * @param code
//	 */
//	protected void wsErrorHandler(int code, String desc) {
//		if (code == 0) { // 正常返回
//			return;
//		}
//		if (mContext == null) {
//			Logger.e(TAG, "V5ClientAgent got null context! Please do start again");
//			return;
//		}
//		
//		/* 非0为有错误 */
//		switch (code) {
//		case 50004:		// account disable
//		case 50005: 	// account failed
//		case 50010: {	// session closed
//			// URL失效、会话结束都需要重新认证获取会话URL
//			if (mConfigSP == null) {
//				mConfigSP = new V5ConfigSP(mContext);
//			}
//			mConfigSP.saveSessionStart(0);
//			try {
//				doAccountAuth();
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//			break;
//		}
//		case 50008:		// request timeout
//			// 超时则进行下一轮请求，无需处理
//			return;
//		}
//			
//		if (null != mHandler) {
//			mHandler.post(new OnErrorRunnable(new V5KFException(code, desc)));
//		} else if (getMessageListener() != null) {
//			getMessageListener().onError(new V5KFException(code, desc));
//		}
//	}
//
//	/**
//	 * 发送消息返回错误码处理
//	 * @param code
//	 * @param desc
//	 */
//	protected void sendEerrorHandler(V5Message message, int code, String desc) {
//		if (code == 0) { // 正常返回
//			return;
//		}
//		if (mContext == null) {
//			Logger.e(TAG, "V5ClientAgent got null context! Please do start again");
//			return;
//		}
//		
//		/* 非0为有错误 */
//		switch (code) {
//		case 50004: 	// account failed
//		case 50007:		// URL错误
//		case 50011:		// 无效会话ID
//		case 50010: {	// session closed
//			// URL失效、会话结束都需要重新认证获取会话URL
//			if (mConfigSP == null) {
//				mConfigSP = new V5ConfigSP(mContext);
//			}
//			mConfigSP.saveSessionStart(0);
//			try {
//				doAccountAuth();
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//			break;
//		}
//		case 50008:	// request timeout
//			break;
//		case 50005: // 错误的请求域
//			
//			break;
//		case 50006: // 内部错误
//		
//			break;
//		}
//		
//		// 无需回调到onError接口，已回调到MessageSendHandler
////		if (null != mHandler) {
////			mHandler.post(new OnErrorRunnable(new V5KFException(code, desc)));
////		} else if (getMessageListener() != null) {
////			getMessageListener().onError(new V5KFException(code, desc));
////		}
//	}
//
//	/**
//	 * 获取认证URL返回错误码处理
//	 * @param code
//	 * @param desc
//	 */
//	protected void authEerrorHandler(int code, String desc) {
//		if (code == 0) { // 正常返回
//			return;
//		}
//		
//		/* 非0为有错误 */
//		switch (code) {
//		case 50005: 	// account failed
//			break;
//		case 50004:			
//			break;
//		case 50010: // session closed			
//			break;
//		}
//		
//		if (null != mHandler) {
//			mHandler.post(new OnErrorRunnable(new V5KFException(code, desc)));
//		} else if (getMessageListener() != null) {
//			getMessageListener().onError(new V5KFException(code, desc));
//		}
//	}
	
	/**
	 * 消息发送成功处理
	 * @param handler
	 * @param code
	 * @param description
	 */
	private void sendSuccessHandle(MessageSendCallback handler, V5Message message) {
		if (message != null) {
			message.setState(V5Message.STATE_ARRIVED);
			message.setSession_start(mSessionStart);
			// 保存消息
			if (null != mDBHelper && cacheLocalMsg) {
				mDBHelper.insert(message);
			}
			// 回调
			if (null != handler) {
				if (mHandler == null) {
					handler.onSuccess(message);
				} else {
					mHandler.post(new MessageSendSuccessRunnable(handler, message));
				}
			}
		}
	}
	
	/**
	 * 消息发送失败处理
	 * @param handler
	 * @param code
	 * @param desc
	 */
	private void sendFailedHandle(MessageSendCallback handler, V5Message message, V5ExceptionStatus code, String desc) {
		if (message != null) {
			message.setState(V5Message.STATE_FAILURE);
		}
		if (null != handler) {
			if (mHandler == null) {
				handler.onFailure(message, code, desc);
			} else {
				mHandler.post(new MessageSendFailureRunnable(handler, message, code, desc));
			}
		}
	}
	
	public OnURLClickListener getURLClickListener() {
		return mURLClickListener;
	}

	public void setURLClickListener(OnURLClickListener mURLClickListener) {
		this.mURLClickListener = mURLClickListener;
	}

	public OnLocationMapClickListener getLocationMapClickListener() {
		return mLocationMapClickListener;
	}

	public void setLocationMapClickListener(OnLocationMapClickListener mLocationMapClickListener) {
		this.mLocationMapClickListener = mLocationMapClickListener;
	}

	protected static class OnMessageRunnable implements Runnable {
		
		private String msgString;
		private V5Message msgBean;
		
		public OnMessageRunnable(String msg) {
			msgString = msg;
		}
		
		public OnMessageRunnable(V5Message msg) {
			msgBean = msg;
		}

		@Override
		public void run() {
			if (V5ClientAgent.getInstance().getMessageListener() != null) {
				if (null != msgBean) {
					V5ClientAgent.getInstance().getMessageListener().onMessage(msgBean);
				}
				if (null != msgString) {
					V5ClientAgent.getInstance().getMessageListener().onMessage(msgString);
				}
			}
		}
	}
	
	class OnErrorRunnable implements Runnable {

		private V5KFException exception;
		
		public OnErrorRunnable(V5KFException ex) {
			exception = ex;
		}
		
		@Override
		public void run() {
			if (getMessageListener() != null) {
				getMessageListener().onError(exception);
			}
		}
	}
	
	class MessageSendSuccessRunnable implements Runnable {
		
		private MessageSendCallback sendHandler;
		private V5Message message;

		public MessageSendSuccessRunnable(MessageSendCallback handler, V5Message msg) {
			this.sendHandler = handler;
			this.message = msg;
		}
		
		@Override
		public void run() {
			if (null != sendHandler) {
				sendHandler.onSuccess(message);
			}
		}
	}

	class MessageSendFailureRunnable implements Runnable {
		
		private MessageSendCallback sendHandler;
		private V5Message message;
		private V5ExceptionStatus statusCode;
		private String description;
		
		public MessageSendFailureRunnable(
				MessageSendCallback handler, 
				V5Message msg,
				V5ExceptionStatus code,
				String desc) {
			this.sendHandler = handler;
			this.message = msg;
			this.statusCode = code;
			this.description = desc;
		}
		
		@Override
		public void run() {
			if (null != sendHandler) {
				sendHandler.onFailure(message, statusCode, description);
			}
		}
	}
	
	class GetLocalMessageRunnable implements Runnable {

		private int offset;
		private int size;
		private OnGetMessagesCallback callback;
		
		public GetLocalMessageRunnable(int offset, int size, OnGetMessagesCallback callback) {
			this.offset = offset;
			this.size = size;
			this.callback = callback;
		}
		
		@Override
		public void run() {
			final List<V5Message> msgList = new ArrayList<V5Message>();
			final boolean finish = mDBHelper.querySession(msgList, offset, size);
			size = msgList.size();
			if (callback != null) {
				if (mHandler != null) {
					mHandler.post(new Runnable() {							
						@Override
						public void run() {
							callback.complete(msgList, offset, size, finish);
						}
					});
				} else {
					callback.complete(msgList, offset, size, finish);
				}
			}
		}		
	}

//	protected OnGetMessagesCallback getGetMessagesCallback() {
//		return mGetMessagesCallback;
//	}
//
//	protected void setGetMessagesCallback(OnGetMessagesCallback mGetMessagesCallback) {
//		this.mGetMessagesCallback = mGetMessagesCallback;
//	}
}
