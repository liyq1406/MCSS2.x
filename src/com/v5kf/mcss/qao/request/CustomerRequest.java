package com.v5kf.mcss.qao.request;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;
import com.v5kf.mcss.config.QAODefine;

public class CustomerRequest extends BaseRequest {
	/* get_customer_messages */
	public static final int numPerPage = 18;

	public CustomerRequest(String o_type, Context context) throws JSONException {
		super(context);
		// TODO Auto-generated constructor stub
		mRequestJson.put(QAODefine.O_TYPE, o_type);
	}
	
	public void setCustomerInfo(JSONObject real, String c_id) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_SET_CUSTOMER_INFO);
		mRequestJson.put(QAODefine.C_ID, c_id);
		/**/
		mRequestJson.put("real", real);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_SET_CUSTOMER_INFO");
	}
	
	public void getCustomerInfo(String c_id) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_CUSTOMER_INFO);
		mRequestJson.put(QAODefine.C_ID, c_id);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_CUSTOMER_INFO");
	}

	public void getCustomerInfo(String u_id, int iface, int channel, int service) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_CUSTOMER_INFO);
		mRequestJson.put(QAODefine.U_ID, u_id);
		if (iface < 10) {
			iface = iface << 8;
		}
		mRequestJson.put(QAODefine.INTERFACE, iface);
		mRequestJson.put(QAODefine.CHANNEL, channel);
		mRequestJson.put(QAODefine.SERVICE, service);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_CUSTOMER_INFO");
	}

	public void getCustomerInfo(String u_id, String f_id) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_CUSTOMER_INFO);
		mRequestJson.put(QAODefine.U_ID, u_id);
		mRequestJson.put(QAODefine.F_ID, f_id);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_CUSTOMER_INFO");
	}
	
	public void getHistoryVisitorInfo(String v_id, int iface) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_HISTORY_VISITOR_INFO);
		mRequestJson.put(QAODefine.VISITOR_ID, v_id);
		mRequestJson.put(QAODefine.INTERFACE, iface);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_CUSTOMER_INFO");
	}
	
	public void getHistoryVisitorReal(String r_id, String v_id) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_HISTORY_VISITOR_REAL);
		mRequestJson.put(QAODefine.VISITOR_ID, v_id);
		mRequestJson.put(QAODefine.REAL_ID, r_id);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_HISTORY_REAL");
	}
	
	public void getCustomerList() throws JSONException {
		// 清空现有的serving列表[改到获取到的时候移除，减少UI闪动情况]
//		AppInfoKeeper appInfo = CustomApplication.getAppInfoInstance();
//		for (CustomerBean cstm : appInfo.getCustomerMap().values()) {
//			if (cstm.getCstmType() == CustomerType.CustomerType_ServingAlive) {
//				appInfo.removeCustomer(cstm);
//			}
//		}
//		postEvent(appInfo, EventTag.ETAG_SERVING_CSTM_CHANGE);
		
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_CUSTOMER_LIST);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_CUSTOMER_REAL");
	}
	
	public void getWaitingCustomer() throws JSONException {
		// 清空现有的waiting列表[改到获取到的时候移除，减少UI闪动情况]
//		AppInfoKeeper appInfo = CustomApplication.getAppInfoInstance();
//		for (CustomerBean cstm : appInfo.getCustomerMap().values()) {
//			if (cstm.getCstmType() == CustomerType.CustomerType_WaitingAlive) {
//				appInfo.removeCustomer(cstm);
//			}
//		}
//		postEvent(appInfo, EventTag.ETAG_WAITING_CSTM_CHANGE);
		
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_WAITING_CUSTOMER);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_WAITING_CUSTOMER");
	}
	
	public void getWaitingNum() throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_WAITING_NUM);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_WAITING_NUM");
	}
	
	public void pickCustomer(String c_id) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_PICK_CUSTOMER);
		mRequestJson.put(QAODefine.C_ID, c_id);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_PICK_CUSTOMER");
	}

	public void pickCustomer(String u_id, String f_id) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_PICK_CUSTOMER);
		mRequestJson.put(QAODefine.U_ID, u_id);
		mRequestJson.put(QAODefine.F_ID, f_id);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_PICK_CUSTOMER");
	}

	public void pickCustomer(String u_id, int iface, int channel, int service) throws JSONException {
		clear();		
		if (iface < 10) {
			iface = iface << 8;
		}
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_PICK_CUSTOMER);
		mRequestJson.put(QAODefine.U_ID, u_id);
		mRequestJson.put(QAODefine.INTERFACE, iface);
		mRequestJson.put(QAODefine.CHANNEL, channel);
		mRequestJson.put(QAODefine.SERVICE, service);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_PICK_CUSTOMER");
	}
	
	public void switchCustomerToWorker(String c_id, String w_id) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_SWITCH_CUSTOMER);
		mRequestJson.put(QAODefine.C_ID, c_id);
		mRequestJson.put(QAODefine.ID, w_id);
		mRequestJson.put(QAODefine.TYPE, QAODefine.WORKER);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_SWITCH_CUSTOMER");
	}

	public void switchCustomerToGroup(String c_id, long g_id) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_SWITCH_CUSTOMER);
		mRequestJson.put(QAODefine.C_ID, c_id);
		mRequestJson.put(QAODefine.ID, g_id);
		mRequestJson.put(QAODefine.TYPE, QAODefine.GROUP);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_SWITCH_CUSTOMER_G");
	}
	
	public void getInTrust(String c_id) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_IN_TRUST);
		mRequestJson.put(QAODefine.C_ID, c_id);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_IN_TRUST");
	}
	
	public void setInTrust(String c_id, int status) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_SET_IN_TRUST);
		mRequestJson.put("status", status);
		mRequestJson.put(QAODefine.C_ID, c_id);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_SET_IN_TRUST");
	}
	
	public void endSession(String c_id) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_END_SESSION);
		mRequestJson.put(QAODefine.C_ID, c_id);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_END_SESSION");
	}

	public void getCustomerMessages(String c_id, int position, int num) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_CUSTOMER_MESSAGES);
		mRequestJson.put(QAODefine.C_ID, c_id);
		mRequestJson.put("position", position);
		mRequestJson.put("num", num);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_CUSTOMER_MESSAGES");
	}
	
	/**
	 * 查询当前会话全部消息
	 * @param c_id
	 * @throws JSONException
	 */
	public void getCustomerMessages(String c_id) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_CUSTOMER_MESSAGES);
		mRequestJson.put(QAODefine.C_ID, c_id);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_CUSTOMER_MESSAGES");
	}
	
	public void getFavorCustomer() throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_FAVOR_CUSTOMER);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_FAVOR_CUSTOMER");
	}
	
	/**
	 * @param option 1-收藏 0-取消收藏
	 * @param visitor_id 
	 * @param iface 
	 */
	public void setFavorCustomer(int option, String visitor_id, int iface) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_SET_FAVOR_CUSTOMER);
		mRequestJson.put("option", option);
		mRequestJson.put("visitor_id", visitor_id);
		mRequestJson.put("interface", iface);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_SET_FAVOR_CUSTOMER");		
	}
	
	
	/**
	 * @param c_id
	 * @param option 1-收藏 0-取消收藏
	 */
	public void setFavorCustomer(int option, String c_id) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_SET_FAVOR_CUSTOMER);
		mRequestJson.put("option", option);
		mRequestJson.put("c_id", c_id);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_SET_FAVOR_CUSTOMER");		
	}
	
	private void clear() {
		mRequestJson.remove(QAODefine.C_ID);
		mRequestJson.remove(QAODefine.W_ID);
		mRequestJson.remove("status");
		mRequestJson.remove("real");
		mRequestJson.remove("position");
		mRequestJson.remove("num");
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return mRequestJson.toString();
	}

}
