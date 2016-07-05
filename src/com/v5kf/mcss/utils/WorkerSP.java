package com.v5kf.mcss.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.v5kf.mcss.config.Config;

/**
 * @author chenhy
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-7 下午10:45:12
 * @package com.v5kf.mcss.utils.WorkerSP.java
 * @description 坐席信息管理
 *
 */
public class WorkerSP {
	public static final String SP_MONITOR_STATUS = "v5_monitor_status";
	public static final String SP_ENABLE_WORKER_LOG = "enable_worker_log";
	
	private static final String SP_EXIT_FLAG = "exit_flag_pref";
	private static final String SP_AUTO_LOGIN = "auto_login_pref";
	private static final String SP_WORKER_PHOTO = "worker_photo_pref";
	private static final String SP_WORKER_PWD = "worker_pwd_pref";
	private static final String SP_WORKER_ID = "worker_id_pref";
	private static final String SP_SITE_ID = "site_id_pref";
	private static final String SP_SITE_NAME = "site_name_pref";
//	private static final String SP_TOKEN = "token_pref";
	private static final String SP_AUTH = "authorization";
	private static final String SP_EXPIRES = "expires";
	private static final String SP_TIMESTAMP = "timestamp";
//	private static final String SP_NONCE = "nonce";
	private static final String SP_TOKEN_DATE = "token_date_pref";
	private static final String SP_WORKER_NAME = "worker_name_pref";
//	private static final String SP_WS_HOST = "websocket_host_pref";
	private static final String SP_KEY_BOARD_HEIGHT = "keyboard_height";
	
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEdit;
	
	public WorkerSP(Context context) {
		this.mSharedPreferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS);
	}
	
	/** 通用接口 **/
	public void remove(String key) {
		mEdit = mSharedPreferences.edit();
		mEdit.remove(key);
		mEdit.commit();
	}
	public void saveBoolean(String key, boolean val) {
		mEdit = mSharedPreferences.edit();
		mEdit.putBoolean(key, val);
		mEdit.commit();
	}
	public boolean readBoolean(String key) {
		boolean val = mSharedPreferences.getBoolean(key, false);
		return val;
	}
	public void saveString(String key, String val) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(key, val);
		mEdit.commit();
	}
	public String readString(String key) {
		String val = mSharedPreferences.getString(key, null);
		return val;
	}
	
	/**
	 * 保存w_id
	 * @param w_id
	 */
	public void saveWorkerId(String w_id) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(SP_WORKER_ID, w_id);
		mEdit.commit();
	}
	
	/**
	 * 读取w_id
	 * @return w_id
	 */
	public String readWorkerId() {
		String w_id = mSharedPreferences.getString(SP_WORKER_ID, null);
		return w_id;
	}

	/**
	 * 保存站点账号
	 * @param w_id
	 */
	public void saveSiteName(String siteName) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(SP_SITE_NAME, siteName);
		mEdit.commit();
	}
	
	/**
	 * 读取站点账号
	 * @return w_id
	 */
	public String readSiteName() {
		String siteName = mSharedPreferences.getString(SP_SITE_NAME, "");
		return siteName;
	}

	/**
	 * 保存WorkerPhoto
	 * @param WorkerPhoto String
	 */
	public void saveWorkerPhoto(String workerPhoto) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(SP_WORKER_PHOTO, workerPhoto);
		mEdit.commit();
	}
	
	/**
	 * 读取WorkerPhoto
	 * @return WorkerPhoto String
	 */
	public String readWorkerPhoto() {
		String workerPhoto = mSharedPreferences.getString(SP_WORKER_PHOTO, null);
		return workerPhoto;
	}
	
	/**
	 * 保存键盘高度
	 * @param saveKeyBoardHeight WorkerSP 
	 * @return void
	 * @param height
	 */
	public void saveKeyBoardHeight(int height) {
		mEdit = mSharedPreferences.edit();
		mEdit.putInt(SP_KEY_BOARD_HEIGHT, height);
		mEdit.commit();
	}
	
	/**
	 * 读取键盘高度
	 * @return num
	 */
	public int readKeyboardHeight() {
		int height = mSharedPreferences.getInt(SP_KEY_BOARD_HEIGHT, 600);
		return height;
	}
	
	/**
	 * 保存e_id
	 * @param e_id
	 */
	public void saveSiteId(String e_id) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(SP_SITE_ID, e_id);
		mEdit.commit();
	}
	
	/**
	 * 读取w_id
	 * @return w_id
	 */
	public String readSiteId() {
		String e_id = mSharedPreferences.getString(SP_SITE_ID, null);
		return e_id;
	}
	
	/**
	 * 保存用户名
	 * @param workerName
	 */
	public void saveWorkerName(String workerName) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(SP_WORKER_NAME, workerName);
		mEdit.commit();
	}
	
	/**
	 * 读取workerName
	 * @return workerName
	 */
	public String readWorkerName() {
		String serverIP = mSharedPreferences.getString(SP_WORKER_NAME, "");		
		return serverIP;
	}
	
	/**
	 * 保存密码
	 * @param passWord
	 */
	public void savePassWord(String passWord) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(SP_WORKER_PWD, MD5.encode(passWord)); // 保存加密过的密码
		mEdit.commit();
	}
	
	/**
	 * 读取密码
	 * @return 密码 
	 */
	public String readPassWord() {
		String passWord = mSharedPreferences.getString(SP_WORKER_PWD, "");
		return passWord;
	}

	/**
	 * 保存Authorization
	 * @param Authorization
	 */
	public void saveAuthorization(String auth) {
		mEdit = mSharedPreferences.edit();
		mEdit.putString(SP_AUTH, auth);
		mEdit.commit();
	}
	
	/**
	 * 读取Authorization
	 * @return Authorization
	 */
	public String readAuthorization() {
		String auth = mSharedPreferences.getString(SP_AUTH, null);
		long expires = readExpires();
		long timestamp = readTimestamp();
		long current = DateUtil.getCurrentLongTime() / 1000;
		if ((timestamp + expires) < current - 3) {
			return null;
		} else {
			return auth;
		}
	}
	
	/**
	 * 保存expires
	 * @param expires
	 */
	public void saveExpires(long expires) {
		mEdit = mSharedPreferences.edit();
		mEdit.putLong(SP_EXPIRES, expires);
		mEdit.commit();
	}
	
	/**
	 * 读取expires
	 * @return expires
	 */
	public long readExpires() {
		return mSharedPreferences.getLong(SP_EXPIRES, 0);
	}
	
	/**
	 * 保存timestamp
	 * @param timestamp
	 */
	public void saveTimestamp(long timestamp) {
		mEdit = mSharedPreferences.edit();
		mEdit.putLong(SP_TIMESTAMP, timestamp);
		mEdit.commit();
	}
	
	/**
	 * 读取timestamp
	 * @return timestamp
	 */
	public long readTimestamp() {
		return mSharedPreferences.getLong(SP_TIMESTAMP, 0);
	}
	
	/**
	 * 保存自动登录选型
	 * @param autoLogin
	 */
	public void saveAutoLogin(boolean autoLogin) {
		mEdit = mSharedPreferences.edit();
		mEdit.putBoolean(SP_AUTO_LOGIN, autoLogin);
		mEdit.commit();
	}
	
	/**
	 * 读取自动登录选型
	 * @return boolean
	 */
	public boolean readAutoLogin() {
		boolean autoLogin = mSharedPreferences.getBoolean(SP_AUTO_LOGIN, true);
		return autoLogin;
	}

	/**
	 * 保存主动退出标识
	 * @param exitFlag
	 */
	public void saveExitFlag(Config.ExitFlag exitFlag) {
		mEdit = mSharedPreferences.edit();
		mEdit.putInt(SP_EXIT_FLAG, exitFlag.ordinal());
		mEdit.commit();
	}
	
	/**
	 * 读取退出标识: 0-未退出的默认值 1-主动退出登录 2-其他退出原因
	 * @return boolean
	 */
	public Config.ExitFlag readExitFlag() {
		int exitFlag = mSharedPreferences.getInt(SP_EXIT_FLAG, 0);
		return Config.ExitFlag.values()[exitFlag];
	}
	
//	/**
//	 * 保存HOST
//	 * @param host
//	 */
//	public void saveWsHost(String host) {
//		mEdit = mSharedPreferences.edit();
//		mEdit.putString(SP_WS_HOST, host);
//		mEdit.commit();
//	}
//	
//	/**
//	 * 读取workerName
//	 * @return workerName
//	 */
//	public String readWsHost() {
//		String host = mSharedPreferences.getString(SP_WS_HOST, Config.WS_HOST);		
//		return host;
//	}
	
	/**
	 * 清空账户自动登录密码，自定登录状态不变
	 */
	public void clearAutoLogin() {
		mEdit = mSharedPreferences.edit();
		mEdit.remove(SP_WORKER_PWD);
		mEdit.commit();
	}

	/**
	 * 清空账户
	 * @param clearWorkerInfo WorkerSP 
	 * @return void
	 */
	public void clearLoginCache() {
		mEdit = mSharedPreferences.edit();
//		mEdit.clear();
		mEdit.remove(SP_AUTO_LOGIN);
		mEdit.remove(SP_WORKER_PHOTO);
		mEdit.remove(SP_WORKER_PWD);
		mEdit.remove(SP_WORKER_NAME);
		mEdit.remove(SP_WORKER_ID);
		mEdit.remove(SP_SITE_ID);
		mEdit.remove(SP_SITE_NAME);
		mEdit.remove(SP_AUTH);
		mEdit.putString(SP_WORKER_PWD, "");
		mEdit.commit();
	}

	public void clearAuthorization() {
		mEdit = mSharedPreferences.edit();
		mEdit.remove(SP_AUTH);
		mEdit.commit();
	}
	
	public void saveReadedListInfo(Map<String, Integer> datas) {
		saveInfo("readed_message_num_list", datas);
	}
	
	public Map<String, Integer> getReadedListInfo() {
		return getInfo("readed_message_num_list");
	}
	
	public void clearReadListInfo() {
		mEdit = mSharedPreferences.edit();
		mEdit.remove("readed_message_num_list");
		mEdit.commit();
	}
	
	public void saveInfo(String key, Map<String, Integer> datas) {
        Iterator<Entry<String, Integer>> iterator = datas.entrySet().iterator();
        JSONObject object = new JSONObject();
        while (iterator.hasNext()) {
            Entry<String, Integer> entry = iterator.next();
            try {
                object.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
            	e.printStackTrace();
            }
        }
	 
	    mEdit = mSharedPreferences.edit();
		mEdit.putString(key, object.toString());
		Logger.d("WorkerSP", "save: " + key + "->" + object.toString());
		mEdit.commit();
	}
	 
	public Map<String, Integer> getInfo(String key) {
	    Map<String, Integer> datas = new HashMap<String, Integer>();
	    String result = mSharedPreferences.getString(key, "");
	    Logger.d("WorkerSP", "read: " + key + "->" + result);
	    if (result.isEmpty()) {
	    	return datas;
	    }
	    try {
	    	JSONObject itemObject = new JSONObject(result);
            JSONArray names = itemObject.names();
            if (names != null) {
                for (int j = 0; j < names.length(); j++) {
                    String name = names.getString(j);
                    int value = itemObject.getInt(name);
                    datas.put(name, value);
                }
            }
	    } catch (JSONException e) {
	    	e.printStackTrace();
	    }
	    return datas;
	}
}
