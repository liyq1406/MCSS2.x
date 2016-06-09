package com.v5kf.mcss.service;

import java.lang.ref.WeakReference;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.tencent.android.tpush.XGPushManager;
import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.Config.ExitFlag;
import com.v5kf.mcss.utils.IntentUtil;
import com.v5kf.mcss.utils.Logger;

public class PushService extends Service {
	private static final String TAG = "PushService";
	Handler mHandler;
	
	/* receiver */
	private PushServiceReceiver mReceiver;
	
	@Override
	public void onCreate() {
		super.onCreate();
		if (Build.VERSION.SDK_INT < 18)
		      startForeground(-1213, new Notification());
		if (Config.DEBUG_TOAST) {
			Logger.i(TAG, "PushService onCreate...");
			Toast.makeText(PushService.this, "PushService onCreate...", Toast.LENGTH_SHORT).show();
		}

		mHandler = new ServiceHandler(this);
		initHeartBeatAlarm();
		initReceiver();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
		clearHeartBeatAlarm();
		
		if (CustomApplication.getInstance().getWorkerSp().readExitFlag() != ExitFlag.ExitFlag_NeedLogin) {
			startService(new Intent(getApplicationContext(), PushService.class));
		}
	}
	
	private void initReceiver() {
		/* 本服务广播 */
		mReceiver = new PushServiceReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Config.ACTION_ON_PUSH);
		registerReceiver(mReceiver, filter);
	}
	
	private void initHeartBeatAlarm() {
		Intent intent = new Intent(Config.ACTION_ON_PUSH);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 30 * 1000, 3 * 60 * 1000, pi);
		Logger.i(TAG, "[Alarm - start] -> " + Config.ACTION_ON_PUSH);
	}
	
	private void clearHeartBeatAlarm() {
		Intent intent2 = new Intent(Config.ACTION_ON_PUSH);
		PendingIntent pi2 = PendingIntent.getBroadcast(this, 0, intent2, 0);
		AlarmManager am2 = (AlarmManager)getSystemService(ALARM_SERVICE);
		am2.cancel(pi2);
		Logger.i(TAG, "[Alarm - cancel] -> " + Config.ACTION_ON_PUSH);
	}
	
	class PushServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (null == intent || !Config.ACTION_ON_PUSH.equals(intent.getAction())) {
				return;
			}
			keepCoreService();
		}
	}
	
	static class ServiceHandler extends Handler {
		
		WeakReference<PushService> mService;
		
		public ServiceHandler(PushService activity) {
			mService = new WeakReference<PushService>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			if (null == mService.get()) {
				Logger.w(TAG, "ServiceHandler对象已被回收");
			}
			switch (msg.what) {
			case CoreService.WHAT_KEEP_SERVICE:
				mService.get().keepCoreService();
				break;
			default:
				break;
			}
		}
	}

	public void keepCoreService() {
		if (!IntentUtil.isServiceWork(getApplicationContext(), 
				"com.tencent.android.tpush.service.XGPushService")) {
			XGPushManager.startPushService(getApplicationContext());
		}
	}
}

