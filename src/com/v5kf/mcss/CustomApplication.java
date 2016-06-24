package com.v5kf.mcss;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.JSONException;
import org.litepal.LitePalApplication;
import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;
import org.simple.eventbus.EventBus;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Process;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.v5kf.client.lib.V5ClientAgent;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.Config.AppStatus;
import com.v5kf.mcss.config.Config.ExitFlag;
import com.v5kf.mcss.config.Config.LoginStatus;
import com.v5kf.mcss.config.Config.ReloginReason;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.entity.WorkerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.qao.request.LoginRequest;
import com.v5kf.mcss.qao.request.WorkerRequest;
import com.v5kf.mcss.service.PushService;
import com.v5kf.mcss.utils.IntentUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.SharePreferenceUtil;
import com.v5kf.mcss.utils.WorkerLogUtil;
import com.v5kf.mcss.utils.WorkerSP;

/**
 * 自定义全局Application类
 * @ClassName: CustomApplcation
 * @author Chen Haiyong
 * @date 2015-6-26	09:50
 */
public class CustomApplication extends LitePalApplication {

	private static final String TAG = "CustomApplication";

	private static CustomApplication mInstance;
	
	private int mNetworkState;
	private int mOldNetworkState;
	private LoginStatus mLoginStatus;
	private AppStatus mAppStatus;
	private AppInfoKeeper mAppInfo;
	
	private int mAppForeground = 0;
	private boolean isOnChat = false;
	private String mOnChatCustomer = null;
	
//	private int mNotificationId = 0;
//	private Map<String, Integer> mNotificationIdMap;
	
	private NotificationManager mNotificationManager;
	
	/* get_wait_customer/get_list_customer(非join_in)是否发通知标识 */
//	private boolean mNotifyNotJoin = true;
	
	/* App所有Activity管理 */
	private List<WeakReference<Activity>> mActivitiePrefs;
	
	/**
	 * OnCreate中调用
	 * @param addActivity CustomApplication 
	 * @return void
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		mActivitiePrefs.add(new WeakReference<Activity>(activity));
	}
	
	/**
	 * OnDestory中调用
	 * @param removeActivity CustomApplication 
	 * @return void
	 * @param activity
	 */
	public void removeActivity(Activity activity) {
		for (WeakReference<Activity> activitiPref : mActivitiePrefs) {
			if (activitiPref.get() == activity) {
				mActivitiePrefs.remove(activitiPref);
			}
		}
	}
	
	/**
	 * 退出应用(退出登录、结束服务)
	 * @param terminate CustomApplication 
	 * @return void
	 */
	public void terminate() {
//		stopActivities();
		
		getWorkerSp().saveExitFlag(ExitFlag.ExitFlag_NeedLogin); // 主动退出
		getWorkerSp().remove(WorkerSP.SP_MONITOR_STATUS);
		mAppStatus = AppStatus.AppStatus_Exit;
		mLoginStatus = LoginStatus.LoginStatus_Unlogin;
		
		/* 通知CoreService退出 */
		EventBus.getDefault().post(Boolean.valueOf(true), EventTag.ETAG_OFF_LINE);
		
		// 停止PushService
		stopService(new Intent(this, PushService.class));
		
		// 清空自动登录密码
		mWorkerSp.clearAutoLogin();
		// 清空运行时内存
		mAppInfo.clearHistoryVisitor();
		mAppInfo.clearRunTimeInfo();
		
//		XGPushManager.unregisterPush(getApplicationContext(), new XGIOperateCallback() {
//			
//			@Override
//			public void onSuccess(Object arg0, int arg1) {
//				Logger.d(TAG, "[XGPush] 信鸽注销成功");
//			}
//			
//			@Override
//			public void onFail(Object arg0, int arg1, String arg2) {
//				Logger.d(TAG, "[XGPush] 信鸽注销失败");
//			}
//		});
//		Logger.d(TAG, "[XGPush] 信鸽注册状态：" + XGPushManager.getServiceStatus(getApplicationContext()));
	}

	/**
	 * 结束所有Activity,退回后台
	 * @param stopActivities CustomApplication 
	 * @return void
	 */
//	public void stopActivities() {
//		Logger.w(TAG, "[stopActivities] - 清理内存");
//		for (WeakReference<FragmentActivity> activitiPref : mActivitiePrefs) {
//			if (null != activitiPref.get()) {
//				activitiPref.get().finish();
//			}
//		}
//		
//		if (mAppInfo.getCustomerMap().size() == 0) {
//			mAppStatus = AppStatus.AppStatus_Init;
//			mAppInfo.clearRunTimeInfo();
//		} else {
//			mAppStatus = AppStatus.AppStatus_Loaded;
//			mAppInfo.clearMemory();
//		}
//	}

	/**
	 * 关闭除指定activity外的其他activity
	 * @param activity
	 */
	public void stopOtherActivities(Activity activity) {
		Logger.w(TAG, "[stopActivities] - 清理内存");
		for (WeakReference<Activity> activitiPref : mActivitiePrefs) {
			if (null != activitiPref.get() && activitiPref.get() != activity) {
				activitiPref.get().finish();
			}
		}
		
		if (mAppInfo.getCustomerMap().size() == 0) {
			mAppStatus = AppStatus.AppStatus_Init;
			mAppInfo.clearRunTimeInfo();
		} else {
			mAppStatus = AppStatus.AppStatus_Loaded;
			mAppInfo.clearMemory();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Logger.i("CustomApplication", "<<<<<<App create[PID:" + Process.myPid() + "]("+Thread.currentThread().getName()+")>>>>>>");
		mInstance = this;
		mAppInfo = new AppInfoKeeper(this);
		mActivitiePrefs = new CopyOnWriteArrayList<WeakReference<Activity>>();
//		mNotificationIdMap = new ConcurrentHashMap<String, Integer>();
		initApplication();	
	}

	/*
	 * 应用初始化
	 */
	private void initApplication() {
		mAppStatus =AppStatus.AppStatus_Init;
		mLoginStatus = LoginStatus.LoginStatus_Unlogin;
//		mNetworkState = NetworkManager.getNetworkState(this); // 延迟获取，避免权限未加载
//		CrashHandler.getInstance().init(this);
		
		if (getWorkerSp().readAutoLogin()) {
			mAppInfo.getUser().setW_id(getWorkerSp().readWorkerId());
			mAppInfo.getUser().setE_id(getWorkerSp().readSiteId());
		}
		
		V5ClientAgent.init(this, null);
		// 开启logcat输出，方便debug，发布时请关闭
		 XGPushConfig.enableDebug(this, Config.DEBUG);
		// 如果需要知道注册是否成功，请使用registerPush(getApplicationContext(), XGIOperateCallback)带callback版本
		// 如果需要绑定账号，请使用registerPush(getApplicationContext(),account)版本
		// 具体可参考详细的开发指南
		// 传递的参数为ApplicationContext
		Context context = getApplicationContext();
		Logger.d(TAG, "[XGPush] 信鸽注册状态：" + XGPushManager.getServiceStatus(getApplicationContext()));
		
		XGPushManager.registerPush(context, "v5kf2015", new XGIOperateCallback() {
			
			@Override
			public void onSuccess(Object arg0, int arg1) {
				Logger.i(TAG, "信鸽注册成功token：" + (String)arg0);
			}
			
			@Override
			public void onFail(Object arg0, int arg1, String arg2) {
				Logger.e(TAG, "信鸽注册失败");
			}
		});
	}

	public static CustomApplication getInstance() {
		return mInstance;
	}
	
	/**
	 * 首次打开应用创建数据库，litePal本地化数据库
	 */
	public void createDB() {
		Connector.getDatabase();
	}
	
	public static AppInfoKeeper getAppInfoInstance() {
		if (null == mInstance.mAppInfo) {
			mInstance.mAppInfo = new AppInfoKeeper(mInstance);
		}
		return mInstance.mAppInfo;
	}

	public AppInfoKeeper getAppInfo() {
		if (null == mAppInfo) {
			mAppInfo = new AppInfoKeeper(this);
		}
		return mAppInfo;
	}

	public LoginStatus getLoginStatus() {
		return mLoginStatus;
	}

	public void setLoginStatus(LoginStatus loginStatus) {
		this.mLoginStatus = loginStatus;
	}
	
	public NotificationManager getNotificationManager() {
		if (mNotificationManager == null)
			mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		return mNotificationManager;
	}
	
	/**
	 * 退出登录,需清空缓存数据
	 */
	public void logout() {
		try {
			LoginRequest lr = (LoginRequest) RequestManager.getRequest(QAODefine.O_TYPE_WLOGIN, this);
			lr.logout(mAppInfo.getUser().getE_id(), mAppInfo.getUser().getW_id());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		terminate();
	}

	public boolean isAppForeground() {
		return mAppForeground > 0 ? true : false;
	}
	
	private void onAppEnterForeground() {
		Logger.w(TAG, "{Application on foreground}");
		XGPushManager.cancelAllNotifaction(getContext());
		/* 初始化登录数据 */
        Logger.d(TAG, "初始化条件：AppStatus:" + getAppStatus() + 
        		" ExitFlag:" + getWorkerSp().readExitFlag()
        		+ " LoginStatus:" + getLoginStatus() + " Auth:" + getWorkerSp().readAuthorization());
		if (getWorkerSp().readAuthorization() == null) {
			return;
		}
		
		// 读取监控状态
		if (mAppInfo.getUser() != null) {
			mAppInfo.getUser().setMonitor(getWorkerSp().readBoolean(WorkerSP.SP_MONITOR_STATUS));
		}
		
		if (getWorkerSp().readExitFlag() == ExitFlag.ExitFlag_NeedLogin) {
			// 通知MainTabActivity
			EventBus.getDefault().post(ReloginReason.ReloginReason_None, EventTag.ETAG_RELOGIN);
		} else if (getWorkerSp().readExitFlag() == ExitFlag.ExitFlag_AutoLogin) { // 恢复启动消息服务
			getWorkerSp().saveExitFlag(ExitFlag.ExitFlag_AutoLogin);
			if (!IntentUtil.isServiceWork(this, Config.SERVICE_NAME_CS)) { // 开启服务并上线
				Intent localIntent = new Intent();
				localIntent.setAction(Config.ACTION_CORE);
				localIntent.setPackage("com.v5kf.mcss");
				localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        startService(localIntent);
			} else { // 发上线通知
				EventBus.getDefault().post(Boolean.valueOf(true), EventTag.ETAG_ON_LINE);
			}
			
			/* 取消onWebsocketLogin，onConnect后自动发送 */
//	        if (getAppStatus() != AppStatus.AppStatus_Loaded && getLoginStatus() == LoginStatus.LoginStatus_Logged) {
//				/* App在后台被kill调后重新打开App初始化数据 */
//				Logger.w(TAG, "DO onWebsocketLogin to restore data");
//				onWebsocketLogin();
//			}
		}
		
		// [日志]记录
		WorkerLogUtil.insertAppForegroundLog();
	}
	
	private void onAppEnterBackground() {
		Logger.w(TAG, "{Application on background}");
		// 缓存未读消息数量
		Map<String, Integer> readMap = new HashMap<String,Integer>();
		for (CustomerBean cstm : mAppInfo.getCustomerMap().values()) {
			// 全部未读消息
			int totalNum = mAppInfo.getTotalUnreplyMessageNum(cstm);
			int readedNum = totalNum - ((cstm.getSession() != null) ? cstm.getSession().getUnreadMessageNum() : 0);
			Logger.d(TAG, "totalNum:" + totalNum + " readedNum:" + readedNum);
			// cache num
			if (readedNum > 0) {
				readMap.put(cstm.getC_id(), readedNum);
			}
		}
		if (readMap.size() > 0) {
			mWorkerSp.saveReadedListInfo(readMap);
		}
		
		// 保存监控状态
		boolean monitor = mAppInfo.getUser().isMonitor();
		if (monitor) {
			mAppInfo.stopMonitor();
			getWorkerSp().saveBoolean(WorkerSP.SP_MONITOR_STATUS, monitor);
		}
		
		// [日志]记录
		WorkerLogUtil.insertAppBackgroundLog();
		
		EventBus.getDefault().post(Boolean.valueOf(false), EventTag.ETAG_OFF_LINE);
		//stopService(new Intent(this, CoreService.class));
		//mAppInfo.getCustomerMap().clear();
	}

	public void setAppForeground() {
		this.mAppForeground++;
		if (this.mAppForeground == 1) { // [修改]回到前台，建立连接
			this.onAppEnterForeground();
		}
	}
	
	public void setAppBackground() {
		this.mAppForeground--;
		if (this.mAppForeground == 0) { // [修改]退出到后台，使用推送
			this.onAppEnterBackground();
		}
	}
	
	/**
	 * Websocket开启时进行主动请求
	 */
	public void onWebsocketLogin() {
		Logger.i(TAG, "***--- onWebsocketLogin ---***");
		Config.USER_ID = mAppInfo.getUser().getW_id() + "of" + mAppInfo.getUser().getE_id();
		Config.SITE_ID = mAppInfo.getUser().getE_id();
		/*
		 * 初始请求内容：
		 * 1. 查询座席今天的历史会话	get_worker_session
		 * 2. 查询座席服务中的客户		get_customer_list
		 * 3. 查询等待中客户列表		get_waiting_customer
		 * 4. 查询座席信息			get_worker_info
		 *   
		 */
		try {
			if (getAppStatus() != AppStatus.AppStatus_Loaded) {
				// get_customer_list & get_waiting_customer
				CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, this);
				cReq.getCustomerList();
				cReq.getWaitingCustomer();
				// get_worker_info
				WorkerRequest wReq = (WorkerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WWRKR, this);
				wReq.getArchWorkers();
				List<WorkerBean> users = DataSupport.findAll(WorkerBean.class);
				if (users == null || users.isEmpty()) {
					wReq.getWorkerInfo();
				} else {
					mAppInfo.setUser(users.get(0));
					wReq.getWorkerStatus();
				}
				setAppStatus(AppStatus.AppStatus_Loaded);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
//	public void sendRequest(String request) {
//		Intent intent = new Intent(Config.ACTION_ON_WBSOCKET);
//		intent.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_MSG_WRITE);
//		// 将请求内容放到字符串数组广播给websocketService
//		intent.putExtra(Config.EXTRA_KEY_MSG_WRITE, request);
//		sendBroadcast(intent);
//	}
//	
//	public void notifyMessage(Intent intent, String title, String text, 
//			String ticker, int num) {
//		notifyMessage(intent, title, text, ticker, num, Config.NOTIFY_ID_MESSAGE, 0, 0);
//	}
//	
//	public void notifyMessage(Intent intent, String title, String text, 
//			String ticker, int num, int id) {
//		notifyMessage(intent, title, text, ticker, num, id, 0, 0);
//	}
//	
//	public void notifyMessage(Intent intent, String title, String text, 
//			String ticker, int num, int id, int largeIcon, int smallIcon) {
//		Logger.w("notifyMessage", "--- notifyMessage --- id:" + id + 
//				" notifyType:" + intent.getIntExtra(Config.EXTRA_KEY_NOTIFY_TYPE, 0));
//		// 此Builder为android.support.v4.app.NotificationCompat.Builder中的，下同。
//		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
//        
//		// 系统收到通知时，通知栏上面滚动显示的文字。
//		if (null != ticker) {
//			mBuilder.setTicker(ticker);
//		}
//		
//		// 设置点击行为
//		if (intent != null) {
//			if (id == Config.NOTIFY_ID_UPDATE) {
//				PendingIntent contextIntent = PendingIntent.getActivity(this, 0, intent, 0);
//				mBuilder.setContentIntent(contextIntent);
//			} else {
//				//点击通知之后需要跳转的页面
//		        PendingIntent pIntent = PendingIntent.getActivity(
//		        		this, 
//		        		Config.REQUEST_CODE_SERVING_SESSION_FRAGMENT, 
//		        		intent, 
//		        		PendingIntent.FLAG_UPDATE_CURRENT);
//		        mBuilder.setContentIntent(pIntent);
//			}
//		}
//        
//        // 通知标题
//        mBuilder.setContentTitle(title);
//        // 通知内容
//        mBuilder.setContentText(text);
// 
//        // 显示在通知栏上的小图标
//        if (smallIcon != 0) {
//        	mBuilder.setSmallIcon(smallIcon);
//        } else {
//        	mBuilder.setSmallIcon(R.drawable.v5_iface_weixin);
//        }
//        
//        // 设置大图标，即通知条上左侧的图片（如果只设置了小图标，则此处会显示小图标）
//        if (largeIcon != 0) {
//        	mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), largeIcon));
//        } else {
//        	mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
//        }
//        
//        // 显示在小图标左侧的数字
//        mBuilder.setNumber(num);
// 
//        // 设置为不可清除模式
//        mBuilder.setOngoing(false);
//        
//        // 点击自动消失 
//        mBuilder.setAutoCancel(true);
//                
//        // 设置铃声、震动、LED灯提醒-默认 
////		mBuilder.setDefaults(Notification.DEFAULT_ALL);
//        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
//        
//        if (getSpUtil().isAllowVibrate()) {
//        	mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
//        }
//        if (getSpUtil().isAllowVoice()) {
//        	mBuilder.setDefaults(Notification.DEFAULT_SOUND);
//        }
//        
//        
//        Notification notification = mBuilder.build();
//        Intent ii = new Intent(Config.ACTION_ON_WBSOCKET);
//        ii.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_NOTIFY_WAKE);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(ii);
//        Logger.d("notifyMessage", "notifyMessage send <<<");
//        // 显示通知，id必须不重复，否则新的通知会覆盖旧的通知（利用这一特性，可以对通知进行更新）
//        getNotificationManager().notify(id, notification);
//	}
	
	
	public boolean isOnChat() {
		return isOnChat;
	}

	public void setOnChat(boolean isOnChat) {
		this.isOnChat = isOnChat;
	}
	
	public String getOnChatCustomer() {
		return mOnChatCustomer;
	}

	/**
	 * 正在对话界面的s_id
	 * @param setOnChatSession CustomApplication 
	 * @return void
	 * @param mOnChatSession
	 */
	public void setOnChatCustomer(String c_id) {
		this.mOnChatCustomer = c_id;
	}

	public AppStatus getAppStatus() {
		return mAppStatus;
	}

	public void setAppStatus(AppStatus mAppStatus) {
		this.mAppStatus = mAppStatus;
	}
	

	private SharePreferenceUtil mSpUtil;
	public static final String PREFERENCE_NAME = "_sharedinfo";
	// 程序设置信息
	public synchronized SharePreferenceUtil getSpUtil() {
		if (mSpUtil == null) {
			String sharedName = getWorkerSp().readWorkerId() + PREFERENCE_NAME;
			mSpUtil = new SharePreferenceUtil(this, sharedName);
		}
		return mSpUtil;
	}
	
	private WorkerSP mWorkerSp;
	public static final String WORKER_PREFERENCE_NAME = "_workerinfo";
	// Worker信息
	public synchronized WorkerSP getWorkerSp() {
		if (mWorkerSp == null) {
			mWorkerSp = new WorkerSP(this);
		}
		return mWorkerSp;
	}

	public int getNetworkState() {
		return mNetworkState;
	}

	public void setNetworkState(int mNetworkState) {
		this.mNetworkState = mNetworkState;
	}

//	public boolean isNotifyNotJoin() {
//		return mNotifyNotJoin;
//	}
//
//	public void setNotifyNotJoin(boolean mNotifyNotJoin) {
//		this.mNotifyNotJoin = mNotifyNotJoin;
//	}

	public int getOldNetworkState() {
		return mOldNetworkState;
	}

	public void setOldNetworkState(int mOldNetworkState) {
		this.mOldNetworkState = mOldNetworkState;
	}
	
	/**
	 * 不显示在通知栏的新消息通知(仅震动、铃声)
	 * @param noticeMessage CustomApplication 
	 * @return void
	 */
//	public void noticeMessage() {
//		// TODO
//		Logger.d("noticeMessage", "--- noticeMessage ---");
//		// 此Builder为android.support.v4.app.NotificationCompat.Builder中的，下同。
//		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
//        
//		// 设置铃声、震动、LED灯提醒-默认 
//		int defaults = Notification.DEFAULT_LIGHTS;
//		     
//        if (getSpUtil().isAllowVibrate()) {
//        	defaults |= (Notification.DEFAULT_VIBRATE);
//        }
//        if (getSpUtil().isAllowVoice()) {
//        	defaults |= (Notification.DEFAULT_SOUND);
//        }
//        mBuilder.setDefaults(defaults);
//        mBuilder.setSmallIcon(R.drawable.ic_launcher);
////        mBuilder.setLargeIcon();
//        
//        Notification notification = mBuilder.build();
//        
//        Logger.d("notifyMessage", "notifyMessage send <<<");
//        try {
//	        // 显示通知，id必须不重复，否则新的通知会覆盖旧的通知（利用这一特性，可以对通知进行更新）
//	        getNotificationManager().notify(0, notification);
//        } catch (Exception e) {
//        	e.printStackTrace();
//        }
//        //clearNotification(0);
//	}
	
	public void noticeMessage(String text) {
		// [修改]使用铃声提示而非通知
		innerNotice();

//		try {
//	        // 显示通知，id必须不重复，否则新的通知会覆盖旧的通知（利用这一特性，可以对通知进行更新）
//	        getNotificationManager().notify(0, getNotification(text));
//        } catch (Exception e) {
//        	e.printStackTrace();
//        }
	}

	private void innerNotice() {
		if (getSpUtil().isAllowPushNotify()) {
			if (getSpUtil().isAllowVibrate()) {
				Vibrator vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);  
	            // 等待3秒，震动3秒，从第0个索引开始，一直循环  
	            vibrator.vibrate(new long[]{200, 200, 200, 200}, -1);
			}
			if (getSpUtil().isAllowVoice()) {
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		 		Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
		 		r.play();
			}
		}
	}

	public void noticeMessage(String c_id, String text) {
		if (c_id != null && isOnChat() && c_id.equals(mOnChatCustomer)) { // 在对话界面中，不发通知
			return;
		}
		innerNotice();
		
//		try {
//			// 显示通知，id必须不重复，否则新的通知会覆盖旧的通知（利用这一特性，可以对通知进行更新）
//			getNotificationManager().notify(getNotificationId(c_id), getNotification(text));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	protected Notification getNotification(String text) {
		// 此Builder为android.support.v4.app.NotificationCompat.Builder中的，下同。
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        
		// 系统收到通知时，通知栏上面滚动显示的文字。
		mBuilder.setTicker(text);
		
		Intent appIntent = new Intent(Intent.ACTION_MAIN);
		appIntent.setAction(Intent.ACTION_MAIN);
		appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		appIntent.setComponent(new ComponentName(this.getPackageName(), 
				"com.v5kf.mcss.ui.activity.MainTabActivity"));
		appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| 
		Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);//关键的一步，设置启动模式
		// 点击通知之后需要跳转的页面
        PendingIntent pIntent = PendingIntent.getActivity(
        		this, 
        		0, 
        		appIntent, 
        		PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pIntent);
        
        // 通知标题
        String notifTitle = getContext().getString(R.string.app_name);
        mBuilder.setContentTitle(notifTitle);
        // 通知内容
        mBuilder.setContentText(text);
 
        // 显示在通知栏上的小图标
        mBuilder.setSmallIcon(getApplicationInfo().icon);
        
        // 设置大图标，即通知条上左侧的图片（如果只设置了小图标，则此处会显示小图标）
        mBuilder.setLargeIcon(
    			BitmapFactory.decodeResource(getResources(),
    					getApplicationInfo().icon));
 
        // 设置为可清除模式
        mBuilder.setOngoing(false);
        
        // 点击自动消失 
        mBuilder.setAutoCancel(true);
                
//        // 设置铃声、震动、LED灯提醒-默认 
//        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        // 设置铃声、震动、LED灯提醒-默认
 		int defaults = Notification.DEFAULT_LIGHTS;
 		     
         if (getSpUtil().isAllowVibrate()) {
         	defaults |= (Notification.DEFAULT_VIBRATE);
         }
         if (getSpUtil().isAllowVoice()) {
         	defaults |= (Notification.DEFAULT_SOUND);
         }
         mBuilder.setDefaults(defaults);
        
        Notification notification = mBuilder.build();
        return notification;
	}
	
	public int getNotificationId(String c_id) {
		if (null == c_id || c_id.isEmpty()) {
			return 0;
		}
//		Integer id;
//		if (mNotificationIdMap.containsKey(c_id)) {
//			id = mNotificationIdMap.get(c_id);
//			if (null == id) {
//				return 10; // 从10开始, 0-9用作其他用途
//			}
//			return id;
//		}
//
//		id = ++mNotificationId;
//		mNotificationIdMap.put(c_id, id);
		return c_id.hashCode();
	}
	
	public void clearNotification(String c_id) {
		if (null == c_id) {
			Logger.w(TAG, "[clearNotification] null key");
			return;
		}
		// 清空对应会话的通知栏消息 
		Logger.d(TAG, "--- clearNotification of c_id:" + c_id);
		getNotificationManager().cancel(getNotificationId(c_id));
//		mNotificationIdMap.remove(c_id);
	}

	public void clearNotification(int id) {
		getNotificationManager().cancel(id);
	}
}
