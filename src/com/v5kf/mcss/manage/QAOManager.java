package com.v5kf.mcss.manage;

import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;

import android.content.Context;
import android.os.Handler;

import com.umeng.analytics.MobclickAgent;
import com.v5kf.client.lib.entity.V5ArticlesMessage;
import com.v5kf.client.lib.entity.V5ControlMessage;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5JSONMessage;
import com.v5kf.client.lib.entity.V5LocationMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MessageDefine;
import com.v5kf.client.lib.entity.V5MusicMessage;
import com.v5kf.client.lib.entity.V5TextMessage;
import com.v5kf.client.lib.entity.V5VoiceMessage;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.MessageBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.qao.receive.QAOBase;
import com.v5kf.mcss.qao.receive.QAOCustomer;
import com.v5kf.mcss.qao.receive.QAOLogin;
import com.v5kf.mcss.qao.receive.QAOMessage;
import com.v5kf.mcss.qao.receive.QAOTicket;
import com.v5kf.mcss.qao.receive.QAOWorker;
import com.v5kf.mcss.utils.Logger;

public class QAOManager {
	private static final String TAG = "QAOManager";
	private Context mContext;
	private Handler mHandler;
	
	public QAOManager(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
	}
	
	public void receive(String message) throws JSONException {
		JSONObject jsonMsg = new JSONObject(message);
		String o_type = jsonMsg.getString(QAODefine.O_TYPE);
		if (jsonMsg.has(QAODefine.O_ERROR)) {
			int o_error = jsonMsg.getInt(QAODefine.O_ERROR);
			switch (o_error) {
			case 10003: // 坐席不存在
			case 10005: // 坐席无权限
				// [eventbus][强制重连]
				EventBus.getDefault().post(Boolean.valueOf(true), EventTag.ETAG_ON_LINE);
				return;
			}
		}
		
		QAOBase qao = null;
		switch (o_type) {
		case QAODefine.O_TYPE_MESSAGE:
			MobclickAgent.onEvent(mContext,"O_TYPE_MESSAGE");
			qao = new QAOMessage(jsonMsg, mContext, mHandler);
			break;
		case QAODefine.O_TYPE_WLOGIN:
			MobclickAgent.onEvent(mContext,"O_TYPE_LOGIN");
			qao = new QAOLogin(jsonMsg, mContext, mHandler);
			break;
		case QAODefine.O_TYPE_WWRKR:
			MobclickAgent.onEvent(mContext,"O_TYPE_WORKER");
			qao = new QAOWorker(jsonMsg, mContext, mHandler);
			break;
		case QAODefine.O_TYPE_WCSTM:
			MobclickAgent.onEvent(mContext,"O_TYPE_CUSTOMER");
			qao = new QAOCustomer(jsonMsg, mContext, mHandler);
			break;
		case QAODefine.O_TYPE_WTICKET:
			MobclickAgent.onEvent(mContext,"O_TYPE_TICKET");
			qao = new QAOTicket(jsonMsg, mContext, mHandler);
			break;
		default:
			throw new JSONException("Unknow o_type:" + o_type);	
		}
		
		try {
			qao.process();
		} catch (JSONException e) {
			Logger.e(
					TAG + "." + o_type + "." + jsonMsg.optString(QAODefine.O_METHOD), 
					"JSONException: " + e.getMessage());
		} catch (NumberFormatException e) {
			Logger.e(
					TAG + "." + o_type + "." + jsonMsg.optString(QAODefine.O_METHOD), 
					"NumberFormatException: " + e.getMessage());
		} catch (Exception e) {
			Logger.e(
					TAG + "." + o_type + "." + jsonMsg.optString(QAODefine.O_METHOD), 
					"Exception: " + e.getMessage());
		}
	}
	
	public static V5Message receiveMessage(MessageBean msgBean) throws NumberFormatException, JSONException {
		/* parse content according to message_type */
		int message_type = msgBean.getMessage_type();
		V5Message message = null;
		switch (message_type) {
		case V5MessageDefine.MSG_TYPE_TEXT:
			message = new V5TextMessage(msgBean.getText_content());
			break;
			
		case V5MessageDefine.MSG_TYPE_LOCATION:
			message = new V5LocationMessage(new JSONObject(msgBean.getJson_content()));
			break;
			
		case V5MessageDefine.MSG_TYPE_CONTROL:
			message = new V5ControlMessage(new JSONObject(msgBean.getJson_content()));
			break;
			
		case V5MessageDefine.MSG_TYPE_ARTICLES:
			message = new V5ArticlesMessage(new JSONObject(msgBean.getJson_content()));
			break;
			
		case V5MessageDefine.MSG_TYPE_IMAGE:
			message = new V5ImageMessage(new JSONObject(msgBean.getJson_content()));
			break;
			
		case V5MessageDefine.MSG_TYPE_VOICE:
			message = new V5VoiceMessage(new JSONObject(msgBean.getJson_content()));
			break;
		
		case V5MessageDefine.MSG_TYPE_MUSIC:
			message = new V5MusicMessage(new JSONObject(msgBean.getJson_content()));
			break;
			
		default: // 不支持消息统一为V5JSONMessage
			message = new V5JSONMessage(new JSONObject(msgBean.getJson_content()));
			break;
		}
		message.setMessage_type(message_type);
		message.setCreate_time(msgBean.getCreate_time());
		Logger.d(TAG, "receiveMessage create_time:" + message.getCreate_time());
		message.setDirection(msgBean.getDirection());
		message.setMessage_id(msgBean.getMessage_id());
		message.setS_id(msgBean.getSession_id());
		return message;
	}
}
