package com.v5kf.mcss.qao.request;

import org.json.JSONException;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;
import com.v5kf.mcss.config.QAODefine;

public class LoginRequest extends BaseRequest {
	
	public LoginRequest(String o_type, Context handler) throws JSONException {
		// TODO Auto-generated constructor stub
		super(handler);
		mRequestJson.put(QAODefine.O_TYPE, o_type);
	}
	
	public void login(String e_id, String w_id) throws JSONException {
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_LOGIN);
		mRequestJson.put("e_id", e_id);
		mRequestJson.put("w_id", w_id);
		mRequestJson.put("client", 2); // 移动端
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_LOGIN");
	}

	public void logout(String e_id, String w_id) throws JSONException {
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_LOGOUT);
		mRequestJson.put("e_id", e_id);
		mRequestJson.put("w_id", w_id);
		mRequestJson.put("client", 2); // 移动端
//		mRequestJson.put("fault", false); // 登出（区别于掉线状态）
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_LOGOUT");
	}
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return mRequestJson.toString();
	}

}
