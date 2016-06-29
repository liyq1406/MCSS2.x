package com.v5kf.mcss.qao.request;

import org.json.JSONException;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;
import com.v5kf.mcss.config.QAODefine;

public class TicketRequest extends BaseRequest {
	
	public TicketRequest(String o_type, Context handler) throws JSONException {
		// TODO Auto-generated constructor stub
		super(handler);
		mRequestJson.put(QAODefine.O_TYPE, o_type);
	}
	
	public void getWorkerSession(int year, int month, int day) throws JSONException {
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_WORKER_SESSION);
		mRequestJson.put("year", year);
		mRequestJson.put("month", month);
		mRequestJson.put("day", day);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_WORKER_SESSION");
	}
	
	public void getCustomerSession(int year, int month, int day, String c_id) throws JSONException {
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_CUSTOMER_SESSION);
		mRequestJson.put("year", year);
		mRequestJson.put("month", month);
		mRequestJson.put("day", day);
		mRequestJson.put(QAODefine.C_ID, c_id);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_CUSTOMER_SESSION");
	}

	public void getCustomerSession(int year, int month, int day, String u_id, String f_id) throws JSONException {
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_CUSTOMER_SESSION);
		mRequestJson.put("year", year);
		mRequestJson.put("month", month);
		mRequestJson.put("day", day);
		mRequestJson.put(QAODefine.U_ID, u_id);
		mRequestJson.put(QAODefine.F_ID, f_id);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_CUSTOMER_SESSION");
	}

	public void getCustomerSession(int year, int month, int day, String u_id, 
			int iface, int channel, int service) throws JSONException {
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_CUSTOMER_SESSION);
		mRequestJson.put("year", year);
		mRequestJson.put("month", month);
		mRequestJson.put("day", day);
		mRequestJson.put(QAODefine.U_ID, u_id);
		mRequestJson.put(QAODefine.INTERFACE, iface);
		mRequestJson.put(QAODefine.CHANNEL, channel);
		mRequestJson.put(QAODefine.SERVICE, service);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_CUSTOMER_SESSION");
	}
	
	public void getMessages(String s_id) throws JSONException {
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_MESSAGES);
		mRequestJson.put(QAODefine.S_ID, s_id);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_MESSAGES");
	}
	
	public void getHistoricalCustomer(int year, int month, int day) throws JSONException {
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_HISTORICAL_CUSTOMER);
		mRequestJson.put("year", year);
		mRequestJson.put("month", month);
		mRequestJson.put("day", day);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_HISTORICAL_CUSTOMER");
	}

	/**
	 * 
	 * @param utc
	 * @param size
	 * @param before 加载时为true，刷新时为false
	 * @throws JSONException
	 */
	public void getHistoricalCustomer(String s_id, int size, boolean before) throws JSONException {
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_HISTORICAL_CUSTOMER);
		if (s_id == null) {
			mRequestJson.put("s_id", 0);
		} else {
			mRequestJson.put("s_id", s_id);
		}
		mRequestJson.put("before", before);
		if (!before) {
			size = 0;
		}
		mRequestJson.put("size", size);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_HISTORICAL_CUSTOMER");
	}

	public void getHistoricalCustomer(long utc, int size, boolean before) throws JSONException {
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_HISTORICAL_CUSTOMER);
		mRequestJson.put("utc", utc);
		mRequestJson.put("size", size);
		mRequestJson.put("before", before);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_HISTORICAL_CUSTOMER");
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return mRequestJson.toString();
	}

}
