package com.v5kf.mcss.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.ui.activity.V51xTabActivity;
import com.v5kf.mcss.ui.activity.MainTabActivity;

public class IntentUtil {
	
	public static final String WLOCK_TAG = "com.v5kf.mcss.service";
	
	public static Intent getNotificationIntent(Context context, String c_id, String s_id, int notify_type, int id) {
		// TODO 改为md2x TabMainActivity
//		Intent resultIntent = new Intent(context, MainTabActivity.class);
		Intent resultIntent = new Intent(context, MainTabActivity.class);
        resultIntent.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_NOTIFY);
        resultIntent.putExtra(Config.EXTRA_KEY_NOTIFY_TYPE, notify_type);
        resultIntent.putExtra(Config.EXTRA_KEY_NOTIFY_ID, id);
        resultIntent.putExtra(Config.EXTRA_KEY_S_ID, s_id);
        resultIntent.putExtra(Config.EXTRA_KEY_C_ID, c_id);
        
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
        		|Intent.FLAG_ACTIVITY_NEW_TASK);
        
        return resultIntent;
	}

	public static Intent getStartActivityIntent(Context packageContext, Class<?> cls, String c_id, String s_id) {
		Bundle bundle = new Bundle();
		bundle.putInt(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_ACTIVITY_START);
		bundle.putString(Config.EXTRA_KEY_S_ID, s_id);
		bundle.putString(Config.EXTRA_KEY_C_ID, c_id);
		
		Intent intent = new Intent(packageContext, cls);
		intent.putExtras(bundle);		
		return intent;
	}

	public static Intent getStartActivityIntent(Context packageContext, Class<?> cls, String c_id, String s_id, int service) {
		Bundle bundle = new Bundle();
		bundle.putInt(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_ACTIVITY_START);
		bundle.putString(Config.EXTRA_KEY_S_ID, s_id);
		bundle.putString(Config.EXTRA_KEY_C_ID, c_id);
		bundle.putInt(Config.EXTRA_KEY_SERVICE, service);
		
		Intent intent = new Intent(packageContext, cls);
		intent.putExtras(bundle);
		return intent;
	}

	public static Intent getStartActivityIntent(Context packageContext, Class<?> cls, String c_id) {
		Bundle bundle = new Bundle();
		bundle.putInt(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_ACTIVITY_START);
		bundle.putString(Config.EXTRA_KEY_C_ID, c_id);
		
		Intent intent = new Intent(packageContext, cls);
		intent.putExtras(bundle);
		return intent;
	}

	public static Intent getStartWebViewIntent(Context packageContext, Class<?> cls, String url, int resId) {
		Bundle bundle = new Bundle();
		bundle.putInt(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_ACTIVITY_START);
		bundle.putString("url", url);
		bundle.putInt("title", resId);
		
		Intent intent = new Intent(packageContext, cls);
		intent.putExtras(bundle);
		return intent;
	}
	
	/** 
	 * 判断某个服务是否正在运行的方法 
	 *  
	 * @param mContext 
	 * @param serviceName 
	 *            是包名+服务的类名（例如：com.v5kf.mcss.service.WebsocketService） 
	 * @return true代表正在运行，false代表服务没有正在运行 
	 */  
	public static boolean isServiceWork(Context mContext, String serviceName) {  
	    boolean isWork = false;  
	    ActivityManager myAM = (ActivityManager) mContext  
	            .getSystemService(Context.ACTIVITY_SERVICE);  
	    List<RunningServiceInfo> myList = myAM.getRunningServices(Integer.MAX_VALUE);  
	    if (myList.size() <= 0) {
	        return false;  
	    }  
	    for (int i = 0; i < myList.size(); i++) {  
	        String mName = myList.get(i).service.getClassName().toString();  
	        if (mName.equals(serviceName)) {  
	            isWork = true;  
	            break;  
	        }  
	    }  
	    return isWork;  
	}
	
	/**
	 * 判断进程是否运行
	 * @return
	 */
	public static boolean isProessRunning(Context context, String proessName) {

		boolean isRunning = false;
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		List<RunningAppProcessInfo> lists = am.getRunningAppProcesses();
		for (RunningAppProcessInfo info : lists) {
			if (info.processName.equals(proessName)) {
				isRunning = true;
			}
		}

		return isRunning;
	}
	
	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	public static String getVersion(Context context) {
	    try {
	        PackageManager manager = context.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
	        String version = info.versionName;
	        return version;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}
}
