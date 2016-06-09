package com.v5kf.mcss.qao.request;

import org.json.JSONObject;
import org.simple.eventbus.EventBus;

import android.content.Context;

import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.utils.Logger;

public abstract class BaseRequest {
	protected JSONObject mRequestJson;
	protected Context mContext;	// WebsocketService
	
	public BaseRequest(Context context) {
		mRequestJson = new JSONObject();
		mContext = context;
	}
	
	/**
	 * 请求发送出口
	 * @param sendRequest BaseRequest 
	 * @return void
	 * @param request
	 */
	protected void sendRequest(String request) {
		// [eventbus]
		EventBus.getDefault().post(request, EventTag.ETAG_SEND_MSG);
	}
	
	/**
	 * 发布事件
	 * @param obj
	 * @param tag
	 */
	protected void postEvent(Object obj, String tag) {
		Logger.d("QAOBase", "postEvent:" + tag);
		EventBus.getDefault().post(obj, tag);
	}
	
	@Override
	public abstract String toString();
}
