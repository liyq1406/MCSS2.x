package com.v5kf.mcss.service;

import java.io.File;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import com.v5kf.client.lib.V5HttpUtil;
import com.v5kf.client.lib.callback.HttpResponseHandler;
import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.manage.update.VersionInfo;
import com.v5kf.mcss.manage.update.XMLParserUtil;
import com.v5kf.mcss.ui.widget.AlertDialog;
import com.v5kf.mcss.utils.Logger;

public class UpdateService extends Service {

	private static final String TAG = "UpdateService";
	private static final String SERVER_VERSION_URL = "https://chat.v5kf.com/app/download/" + (Config.DEBUG ? "version_debug.xml" : "version.xml");
	private VersionInfo mVInfo;
	private long mReference;
	private UpdateServiceReceiver mReceiver;
	@SuppressWarnings("unused")
	private Handler mHandler;
	private boolean isDownloading;
	private boolean mCheckManual;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		Logger.v(TAG, "[onCreate]");
		mHandler = new Handler(Looper.getMainLooper());
		initReceiver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Logger.v(TAG, "[onStartCommand]");
		if (intent != null) {
			mCheckManual = intent.getBooleanExtra("check_manual", false);
		}
		checkUpdate();
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
		unregisterReceiver(mReceiver);
		Logger.v(TAG, "[onDestroy]");
	}

	private void initReceiver() {
		mReceiver = new UpdateServiceReceiver();
		
		/* 注册广播接收 */
		IntentFilter filter = new IntentFilter();
		filter.addAction(Config.ACTION_ON_UPDATE);
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, filter);
		
		IntentFilter downloadFilter =new IntentFilter();
		downloadFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		downloadFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
		registerReceiver(mReceiver, downloadFilter);
	}

	private void addDownloadTask(boolean onlyWifi) {
		if (null == mVInfo) {
			return;
		}
		
		File apkfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + mVInfo.getApkName());
		if (apkfile.exists()) { // 已存在安装包，直接提示安装
			Logger.i(TAG, "[addDownloadTask] 文件已下载");
			installApk();
			return;
		}
		
		if (isDownloading) {
			Logger.w(TAG, "[addDownloadTask] already in downloading");
			return;
		}
		isDownloading = true;
		
		String serviceString = Context.DOWNLOAD_SERVICE;
		DownloadManager downloadManager;  
		downloadManager = (DownloadManager)getSystemService(serviceString);  
		
		Uri uri = Uri.parse(mVInfo.getDownloadURL());
		DownloadManager.Request request = new Request(uri);
		request.setMimeType("application/vnd.android.package-archive");
		request.setVisibleInDownloadsUi(true);
		// 设置在什么网络情况下进行下载
		if (onlyWifi) {
			request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
		}
		// 设置通知栏
		request.setNotificationVisibility(Request.VISIBILITY_VISIBLE);
		request.setTitle(getString(R.string.app_name));
		request.setDescription(getString(R.string.app_name) + getString(R.string.on_download_description));
		request.setAllowedOverRoaming(false);
		// 设置文件存放目录
//		request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, mVInfo.getApkName());
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mVInfo.getApkName());
		mReference = downloadManager.enqueue(request);
		Logger.d(TAG, "[更新] 下载中。。。" + mReference);
	}
	
	
	/**
	 * 启动自动检测更新
	 * @param checkUpdate WelcomeActivity 
	 * @return void
	 */
	public void checkUpdate() {
		V5HttpUtil.get(SERVER_VERSION_URL, new HttpResponseHandler(getApplicationContext()) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				Logger.d(TAG, "responseString:" +  responseString);
				mVInfo = XMLParserUtil.getUpdateInfo(responseString);;
				mVInfo.setCheckManual(mCheckManual);
				if (mVInfo != null) {
					// xml解析
					Logger.v(TAG, "ApkName:" + mVInfo.getApkName());
					Logger.v(TAG, "Version:" + mVInfo.getVersion());
					Logger.v(TAG, "DisplayMessage:" + mVInfo.getDisplayMessage());
					Logger.v(TAG, "DownloadURL:" + mVInfo.getDownloadURL());
					Logger.v(TAG, "AppName:" + mVInfo.getAppName());
					Logger.v(TAG, "Title:" + mVInfo.getDisplayTitle());
					Logger.v(TAG, "ChannelURL:" + mVInfo.getChannelURL());
					Logger.v(TAG, "level:" + mVInfo.getLevel());
					CustomApplication.getInstance().getWorkerSp().saveInt("update_level", mVInfo.getLevel());
					if (mVInfo.getLevel() > 0) {
						if (checkVersionInfo(mVInfo)) {
							Logger.d(TAG, "gets update");
							Intent i = new Intent(Config.ACTION_ON_UPDATE);
							Bundle bundle = new Bundle();
							bundle.putInt(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_UP_ENABLE);
							bundle.putSerializable("versionInfo", mVInfo);
							i.putExtras(bundle);
							i.putExtra("version", mVInfo.getVersion());
							i.putExtra("displayMessage", mVInfo.getDisplayMessage());
							LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
						} else {
							Logger.d(TAG, "no update");
							// 没有新版本，仅手动点击更新处理此广播返回
							Intent i = new Intent(Config.ACTION_ON_UPDATE);
							i.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_UP_NO_NEWVERSION);
							LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
							stopSelf();
						}
					} else if (mCheckManual) {
						Logger.d(TAG, "mCheckManual not alert update");
						// 没有新版本，仅手动点击更新处理此广播返回
						Intent i = new Intent(Config.ACTION_ON_UPDATE);
						i.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_UP_NO_NEWVERSION);
						LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
						stopSelf();
					} else {
						Logger.d(TAG, "not alert update");
					}
				} else {
					// 检查更新失败，仅手动点击更新处理此广播返回
					Intent i = new Intent(Config.ACTION_ON_UPDATE);
					i.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_UP_FAILED);
					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
					stopSelf();
				}
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "Update failed. Code[" + statusCode + "]:" + responseString);
				// 检查更新失败，仅手动点击更新处理此广播返回
				Intent i = new Intent(Config.ACTION_ON_UPDATE);
				i.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_UP_FAILED);
				LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
				stopSelf();
			}
		});
		
	}
	
	protected void alertUpdateInfo(VersionInfo vInfo) {
		// [修改]显示确认更新对话框
		String title = TextUtils.isEmpty(vInfo.getDisplayTitle()) ? "【版本更新（" + vInfo.getVersion() + "）】" : vInfo.getDisplayTitle();
		AlertDialog alertDialog = new AlertDialog(this).builder()
			.setTitle(title)
			.setMsg(vInfo.getDisplayMessage())
			.setCancelable(false)
			.setWindowType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		if (vInfo.getLevel() == 5) { // 第5等级的level说明更新很重要，解决前一版本的重大bug 
			alertDialog.setNegativeButton("下载更新", new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// 允许下载
					Intent i = new Intent(Config.ACTION_ON_UPDATE);
					i.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_UP_DOWNLOAD);
					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
				}
			});
		} else {
			alertDialog.setPositiveButton("下载更新", new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// 允许下载
					Intent i = new Intent(Config.ACTION_ON_UPDATE);
					i.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_UP_DOWNLOAD);
					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
				}
			});
			alertDialog.setNegativeButton("下次再说", new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// 不下载，下次再试
					Intent i = new Intent(Config.ACTION_ON_UPDATE);
					i.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_UP_CANCEL);
					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
				}
			});
		}
		alertDialog.show();
	}

	public boolean checkVersionInfo(VersionInfo info) {
		// 获取当前软件包信息
		PackageInfo pi = null;
		try {
			pi = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		if (null == pi) {
			return false;
		}
		// 当前软件版本号
		int versionCode = pi.versionCode;
		if (versionCode < info.getVersionCode()) {
			// 如果当前版本号小于服务端版本号,则弹出提示更新对话框
			Logger.i(TAG, "<<有更新>>");			
			return true;
		} else {
			// 无更新
			Logger.i(TAG, "<<无更新>>");
			return false;
		}
	}
	
	
	/**
	 * 安装apk
	 */
	private void installApk() {
		isDownloading = false;
		
		Logger.d(TAG, "[installApk] 下载安装");
		// 获取当前sdcard存储路径
//		File apkfile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + mVInfo.getApkName());
		File apkfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + mVInfo.getApkName());
		if (!apkfile.exists()) {
			Logger.w(TAG, "[installApk] 文件不存在");
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		// 安装，如果签名不一致，可能出现程序未安装提示
		i.setDataAndType(Uri.fromFile(new File(apkfile.getAbsolutePath())), "application/vnd.android.package-archive"); 
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		
		// TODO判断安装是否完成，安装完成关闭更新服务
		stopSelf();
	}
	
	class UpdateServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (null == intent) {
				return;
			}
			Logger.d(TAG, "[onReceive] " + intent.getAction());
			if (intent.getAction().equals(Config.ACTION_ON_UPDATE)) {
				int type = intent.getIntExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_NULL);
				boolean onlyWifi = intent.getBooleanExtra(Config.EXTRA_KEY_DOWN_ONLYWIFI, false);
				if (type == Config.EXTRA_TYPE_UP_DOWNLOAD) { // 允许下载
					addDownloadTask(onlyWifi);
				} else if (type == Config.EXTRA_TYPE_UP_INSTALL) { // 允许安装
					installApk();
				} else if(type == Config.EXTRA_TYPE_UP_CANCEL) { // 取消安装
					stopSelf();
				}
			} else if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) { // 下载完成
				long refId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
				if (mReference == refId) {
					installApk();
//					Intent i = new Intent(Config.ACTION_ON_UPDATE);
//					i.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_UP_DOWNLOAD_FINISH);
//					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
//					
//					/* 显示通知栏下载完成消息 */
//					CustomApplication app = (CustomApplication)getApplication();
//					String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//							+ "/" + mVInfo.getApkName();
//					File apkfile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + mVInfo.getApkName());
//					if (!apkfile.exists()) {
//						Logger.w(TAG, "[installApk] 文件不存在");
//						return;
//					}
//					Intent ib = new Intent(Intent.ACTION_VIEW);
//					// 安装，如果签名不一致，可能出现程序未安装提示
//					ib.setDataAndType(Uri.fromFile(new File(apkfile.getAbsolutePath())), "application/vnd.android.package-archive"); 
//					ib.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					app.notifyMessage(intent, "下载完成(点击安装)", "保存到" + path, "新版本下载完成", 0, Config.NOTIFY_ID_UPDATE);
				}
			} else if (intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
				// TODO 点击下载通知
			}
		}
	}
	
}
