package com.v5kf.mcss.qao.receive;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.Config.AppStatus;
import com.v5kf.mcss.config.Config.ExitFlag;
import com.v5kf.mcss.config.Config.LoginStatus;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.utils.Logger;

public class QAOLogin extends QAOBase {
	public static final String TAG = "QAOLogin";
	
	public QAOLogin(JSONObject jsonMsg, Context context, Handler mHandler) throws JSONException {
		super(jsonMsg, context, mHandler);
		this.o_type = QAODefine.O_TYPE_WLOGIN;
		this.o_method = jsonMsg.getString(QAODefine.O_METHOD);
	}

	@Override
	public void process() throws JSONException {
		int client = qao_data.optInt("client");
		if (client == 0 || client == 1) {
			return;
		}
		
		switch (o_method) {
		case QAODefine.O_METHOD_LOGIN: {
			if (o_error == 0) {
				onWebsocketLogin();
//				// [通知更新][eventbus]
//				postEvent(mAppInfo, EventTag.ETAG_LOGIN_OK);
				CustomApplication.getInstance().setLoginStatus(LoginStatus.LoginStatus_Logged);
			} else {
				mApplication.setAppStatus(AppStatus.AppStatus_Exit);
				mApplication.getWorkerSp().saveExitFlag(ExitFlag.ExitFlag_NeedLogin);
				CustomApplication.getInstance().setLoginStatus(LoginStatus.LoginStatus_LoginFailed);
			}
			postEvent(Integer.valueOf(o_error), EventTag.ETAG_LOGIN_CHANGE);
//			Intent intent = new Intent(Config.ACTION_ON_LOGIN);
//			intent.putExtra(Config.EXTRA_KEY_INTENT_TYPE, o_error);
//			LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
//			Logger.d(TAG + "-Broadcast", "o_error:" + o_error + "<<<" + Config.ACTION_ON_LOGIN);
			break;
		}
		case QAODefine.O_METHOD_LOGOUT:
			if (o_error == 0) {
				// [通知更新][eventbus]
				CustomApplication.getInstance().setLoginStatus(LoginStatus.LoginStatus_Logout);
			} else {
				// [通知更新][eventbus]
				CustomApplication.getInstance().setLoginStatus(LoginStatus.LoginStatus_LogoutFailed);
			}
			postEvent(Integer.valueOf(o_error), EventTag.ETAG_LOGOUT_CHANGE);
			break;
		default:
			throw new JSONException("Unknow o_method:" + o_method + " of o_type:" + o_type);
		}
	}

	@Override
	protected void errorHandle() {
		
	}
	
}
