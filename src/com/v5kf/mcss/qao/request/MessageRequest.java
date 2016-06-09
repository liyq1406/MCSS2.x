package com.v5kf.mcss.qao.request;

import org.json.JSONException;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.mcss.config.QAODefine;

public class MessageRequest extends BaseRequest {
	
	public MessageRequest(String o_type, Context handler) throws JSONException {
		// TODO Auto-generated constructor stub
		super(handler);
		mRequestJson.put(QAODefine.O_TYPE, o_type);
	}
	
	public void sendMessage(V5Message message) throws JSONException {
//		mRequestJson.put(QAODefine.C_ID, message.getC_id());
//		mRequestJson.put(QAODefine.S_ID, message.getS_id());
//		mRequestJson.put(QAODefine.M_ID, message.getMessage_id());
//		mRequestJson.put(QAODefine.MSG_TYPE, message.getMessage_type());
//		mRequestJson.put(QAODefine.DIRECTION, message.getDirection());
//		message.getMsg_content().toJson(mRequestJson);	
		
		sendRequest(message.toJson());
		MobclickAgent.onEvent(mContext,"REQ_SEND_MESSAGE");
	}

	public void sendMessage(V5Message message, long pid) throws JSONException {
//		mRequestJson.put(QAODefine.C_ID, message.getC_id());
//		mRequestJson.put(QAODefine.S_ID, message.getS_id());
//		mRequestJson.put(QAODefine.M_ID, message.getM_id());
//		mRequestJson.put(QAODefine.P_ID, pid);
//		mRequestJson.put(QAODefine.MSG_TYPE, message.getMessage_type());
//		mRequestJson.put(QAODefine.DIRECTION, message.getMsg_dir());
//		message.getMsg_content().toJson(mRequestJson);	
		
		sendRequest(message.toJson());
		MobclickAgent.onEvent(mContext,"REQ_SEND_MESSAGE");
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return mRequestJson.toString();
	}

}
