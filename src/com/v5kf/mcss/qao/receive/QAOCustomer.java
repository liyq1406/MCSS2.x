package com.v5kf.mcss.qao.receive;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import android.content.Context;
import android.os.Handler;

import com.umeng.analytics.MobclickAgent;
import com.v5kf.client.lib.V5MessageManager;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.entity.CustomerBean.CustomerType;
import com.v5kf.mcss.entity.CustomerRealBean;
import com.v5kf.mcss.entity.SessionBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.WorkerLogUtil;

public class QAOCustomer extends QAOBase {
	private static final String TAG = "QAOCustomer";
	
	/* get_customer_messages */
	public static boolean finish;
	public static int position;
	public static int num;

	public QAOCustomer(JSONObject json, Context context, Handler handler) throws JSONException {
		super(json, context, handler);
		this.o_type = QAODefine.O_TYPE_WCSTM;
		this.o_method = json.getString(QAODefine.O_METHOD);
	}
	
	@Override
	public void process() throws NumberFormatException, JSONException {
		if (o_error != 0) {
			Logger.e(TAG, o_method + ".o_errmsg:" + qao_data.optString(QAODefine.O_ERRMSG));
			errorHandle();
			return;
		}
		switch (o_method) {		
		case QAODefine.O_METHOD_GET_CUSTOMER_INFO:
			parseGetCustomerInfo();
			break;
		
		case QAODefine.O_METHOD_SET_CUSTOMER_INFO:
			// 无需处理
			break;
			
		case QAODefine.O_METHOD_GET_CUSTOMER_LIST:
			parseGetCustomerList();			
			break;
		
		case QAODefine.O_METHOD_GET_WAITING_CUSTOMER:
			parseGetWaitingCustomer();			
			break;
			
		case QAODefine.O_METHOD_GET_WAITING_NUM:
			//
			break;
			
		case QAODefine.O_METHOD_PICK_CUSTOMER:
			// 成功则触发customer_join_in
			postEvent(mAppInfo, EventTag.ETAG_PICK_CSTM_OK);
			MobclickAgent.onEvent(mContext,"PICK_CUSTOMER");
			break;
			
		case QAODefine.O_METHOD_SWITCH_CUSTOMER:
			// 成功则触发customer_join_out
			MobclickAgent.onEvent(mContext,"SWITCH_CUSTOMER");
			break;
			
		case QAODefine.O_METHOD_SET_IN_TRUST:
			// 无需处理，请求的同时进行get_in_trust检查
			break;
			
		case QAODefine.O_METHOD_GET_IN_TRUST: 
			parseGetInTrust();
			break;
			
		case QAODefine.O_METHOD_END_SESSION:
			// 成功则触发customer_join_out
			break;
		
		case QAODefine.O_METHOD_CSTM_JOIN_IN: 
			MobclickAgent.onEvent(mContext,"CSTM_JOIN_IN");
			parseCstmJoinIn();
			break;
		
		case QAODefine.O_METHOD_CSTM_JOIN_OUT: 
			parseCstmJoinOut();
			break;
		
		case QAODefine.O_METHOD_CSTM_WAIT_IN: 
			parseCstmWaitIn();
			break;
		
		case QAODefine.O_METHOD_CSTM_WAIT_OUT: 
			parseCstmWaitOut();
			break;
		
		case QAODefine.O_METHOD_GET_HISTORY_VISITOR_INFO: 
			parseGetHistoryVisitorInfo();
			break;
		
		case QAODefine.O_METHOD_GET_HISTORY_VISITOR_REAL: 
			parseGetHistoryVisitorReal();
			break;
		
		case QAODefine.O_METHOD_GET_CUSTOMER_MESSAGES: 
			parseGetCustomerMessages();
			break;
			
		case QAODefine.O_METHOD_UPDATE_CSTM_INFO:
			parseUpdateCustomerInfo();
			break;
			
		case QAODefine.O_METHOD_CSTM_ACCESSABLE_CHANGE:
			parseCustomerAccessableChange();
			break;
			
		case QAODefine.O_METHOD_CSTM_MONITOR_OUT:
			parseCustomerMonitorOut();
			break;
		
		default:
			throw new JSONException("Unknow o_method:" + o_method + " of o_type:" + o_type);
		}
		
	}

	private void parseCustomerMonitorOut() throws JSONException {
		String c_id = qao_data.getString(QAODefine.C_ID);
//		String s_id = qao_data.getString(QAODefine.S_ID);
		CustomerBean cstm = mAppInfo.getMonitorCustomer(c_id);
		if (cstm != null) {
			if (cstm.getCstmType() == CustomerType.CustomerType_Monitor) {
				mAppInfo.removeMonitorCustomer(cstm);
				postEvent(cstm, EventTag.ETAG_MONITOR_OUT);
			}
		}
	}

	private void parseCustomerAccessableChange() throws JSONException {
		String c_id = null;
		if (qao_data.has(QAODefine.C_ID)) {
			c_id = qao_data.getString(QAODefine.C_ID);
		}
		String visitor_id = null;
		if (qao_data.has(QAODefine.VISITOR_ID)) {
			visitor_id = qao_data.getString(QAODefine.VISITOR_ID);
		}
		if (c_id == null && visitor_id == null) {
			return;
		}
		String accessable = qao_data.optString("accessable");
		if (c_id != null) {
			CustomerBean customer = this.mAppInfo.getAliveCustomer(c_id);
			if (customer != null) {
				customer.setAccessable(accessable);
				Logger.d(TAG, "parseCustomerAccessableChange -> customer:" + customer.getDefaultName());
				if (customer.getCstmType() == CustomerType.CustomerType_ServingAlive) {
					postEvent(QAODefine.O_METHOD_CSTM_ACCESSABLE_CHANGE, EventTag.ETAG_SERVING_CSTM_CHANGE);
				} else if (customer.getCstmType() == CustomerType.CustomerType_WaitingAlive) {
					postEvent(mAppInfo, EventTag.ETAG_WAITING_CSTM_CHANGE);
				}
				postEvent(customer, EventTag.ETAG_ACCESSABLE_CHANGE);
				//postEvent(customer, EventTag.ETAG_VISITORS_CHANGE);
				//return;
			}
		}
		if (visitor_id != null) {
			CustomerBean customer = this.mAppInfo.getVisitor(visitor_id);
			if (customer != null) {
				customer.setAccessable(accessable);
				Logger.d(TAG, "parseCustomerAccessableChange -> visitor:" + customer.getDefaultName());
				// [eventbus]
				postEvent(customer, EventTag.ETAG_VISITORS_CHANGE);
				postEvent(customer, EventTag.ETAG_ACCESSABLE_CHANGE);
				//return;
			}
		}
		//Logger.e(TAG, "[parseCustomerAccessableChange] not match Customer");
	}

	/**
	 * 新客户信息（推送），新接口
	 */
	private void parseUpdateCustomerInfo() {
		// TODO parseUpdateCustomerInfo
		Logger.i(TAG, "[updateCustomerInfo] " + qao_data.toString());
		
		//////add
	}

	private void parseGetCustomerInfo() throws NumberFormatException, JSONException {
		String c_id = null, u_id = null;
		CustomerBean customer = null;
		if (qao_data.has(QAODefine.C_ID)) {
			c_id = qao_data.getString(QAODefine.C_ID);
			customer = mAppInfo.getAliveCustomer(c_id); // 找CustomerMap
		} else if (qao_data.has(QAODefine.U_ID)) {
			u_id = qao_data.getString(QAODefine.U_ID);
			customer = mAppInfo.getVisitor(u_id); // 找VisitorMap
		}
		
		if (null == customer) {
			customer = new CustomerBean();
		}
		customer.updateCustomerInfo(qao_data);

		// [修改]获得客户消息[通知界面]更新逻辑[eventbus]
		postEvent(customer, EventTag.ETAG_UPDATE_CSTM_INFO);
		if (customer.getCstmType() == CustomerType.CustomerType_Monitor) {
			postEvent(customer, EventTag.ETAG_MONITOR_IN);
		}
	}

	private void parseGetCustomerList() throws JSONException {
		// 读取已读消息缓存
		Map<String, Integer> readList = mApplication.getWorkerSp().getReadedListInfo();
		mApplication.getWorkerSp().clearReadListInfo();
		
		// 清空已有的列表
		for (CustomerBean cstm : mAppInfo.getCustomerMap().values()) {
			if (cstm.getCstmType() == CustomerType.CustomerType_ServingAlive) {
				// 全部未读消息
				if (cstm.getSession() != null) {
					int totalNum = mAppInfo.getTotalUnreplyMessageNum(cstm);
					int readedNum = totalNum - cstm.getSession().getUnreadMessageNum();
					Logger.d(TAG, "totalNum:" + totalNum + " readedNum:" + readedNum);
					cstm.getSession().setReadedNum(readedNum);
				}
				mAppInfo.removeCustomer(cstm);
			}
		}
		postEvent(QAODefine.O_METHOD_GET_CUSTOMER_LIST, EventTag.ETAG_SERVING_CSTM_CHANGE);
		
		/* 应答对应登录成功后需进行仅此一次该请求 */
		if (qao_data.has(QAODefine.CUSTOMER)) {
			JSONArray customer_array = qao_data.getJSONArray(QAODefine.CUSTOMER);
			String c_id = null;
			
			for (int i = 0; i < customer_array.length(); i++) {
				JSONObject item = customer_array.getJSONObject(i);
				if (item.has("c_id")) {
					c_id = item.getString("c_id");
				} else if (item.has("id")) {
					c_id = item.getString("id");
				}
				CustomerBean cstm = mAppInfo.getCustomerBean(c_id);
				if (null == cstm) {
					cstm = new CustomerBean();
					cstm.initCustomerInfo(item);
					cstm.setCstmType(CustomerType.CustomerType_ServingAlive);
					mAppInfo.addCustomer(cstm);
				} else {
					cstm.initCustomerInfo(item);
				}
				if (readList.containsKey(c_id)) {
					cstm.setReadedNumCache(readList.get("c_id") == null ? 0 : readList.get("c_id"));
				}
				
				CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(o_type, mContext);
				cReq.getCustomerInfo(c_id);
				cReq.getCustomerMessages(c_id); //, position, CustomerRequest.numPerPage);
				cReq.getInTrust(c_id);

				/* [修改]get_customer_list不显示通知 */
//				if (mApplication.isNotifyNotJoin()) {
//					String sid = mAppInfo.getCSMap().get(c_id);
//					showNitification(
//							cstm, 
//							sid,
//							mContext.getString(R.string.notify_join_in),
//							Config.EXTRA_NOTIFY_TYPE_JOIN_IN,
//							mApplication.getNotificationId(cstm.getC_id()));
//				}
				
				Logger.d(TAG, "GET_CUSTOMER_LIST sendRequest i=" + i);
				/* 最后一个请求 */
				if (i == (customer_array.length() - 1)) {
					cstm.setLastType(CustomerBean.LAST_TYPE_OF_CLIST);
				}
			}
			//[修改][等待getCustomerMessages]postEvent(mAppInfo, EventTag.ETAG_SERVING_CSTM_CHANGE);
		}
	}

	private void parseGetWaitingCustomer() throws JSONException {
		// 清空已有的列表
		for (CustomerBean cstm : mAppInfo.getCustomerMap().values()) {
			if (cstm.getCstmType() == CustomerType.CustomerType_WaitingAlive) {
				mAppInfo.removeCustomer(cstm);
			}
		}
		postEvent(mAppInfo, EventTag.ETAG_WAITING_CSTM_CHANGE);
		
		/* 查询等待顾客 */
		JSONArray serviceArray = qao_data.optJSONArray("services");
		if (null == serviceArray || serviceArray.length() < 1) {
			return;
		}
		
		for (int i = 0; i < serviceArray.length(); i++) {
			parseCustomersOfService(serviceArray.getJSONObject(i), i);
		}
		// [eventbus]
		//[修改][等待getCustomerMessages]postEvent(mAppInfo, EventTag.ETAG_WAITING_CSTM_CHANGE);
	}

	private void parseCustomersOfService(JSONObject json, int service) throws JSONException {
		if (json.optInt(QAODefine.CUSTOMERS) > 0) {
			JSONArray customer_array = json.getJSONArray(QAODefine.CUSTOMER);
			String c_id = null;
			for (int i = 0; i < customer_array.length(); i++) {
				JSONObject item = customer_array.getJSONObject(i);
				if (item.has("c_id")) {
					c_id = item.getString("c_id");
				} else if (item.has("id")) {
					c_id = item.getString("id");
				}
				if (c_id == null) {
					continue;
				}
				CustomerBean cstm = mAppInfo.getCustomerBean(c_id);
				if (null == cstm) {
					cstm = new CustomerBean();
					cstm.initCustomerInfo(item);
					mAppInfo.addCustomer(cstm);
				} else {
					cstm.initCustomerInfo(item);
				}
				cstm.setCstmType(CustomerType.CustomerType_WaitingAlive);
				cstm.setService(service);
				CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(o_type, mContext);
				cReq.getCustomerInfo(c_id);
				cReq.getCustomerMessages(c_id); //, position, CustomerRequest.numPerPage);
				/* [修改]get_waiting_customer不显示通知 */
//				if (mApplication.isNotifyNotJoin()) {
//					String sid = mAppInfo.getCSMap().get(c_id);
//					showNitification(
//							cstm, 
//							sid, 
//							"[" + (i + 1) + "个客户正在排队]",
//							Config.EXTRA_NOTIFY_TYPE_WAIT_IN,
//							Config.NOTIFY_ID_WAIT_IN);
//				}
				
				/* 最后一个请求 */
				if (i == (customer_array.length() - 1)) {
					cstm.setLastType(CustomerBean.LAST_TYPE_OF_CWAIT_LIST);
				}
			}
		}
	}

	private void parseGetInTrust() throws NumberFormatException, JSONException {
		String s_id = qao_data.getString(QAODefine.S_ID);
		int status = qao_data.getInt("status");
		mAppInfo.getSessionBean(s_id).setInTrust(status == 1 ? true : false);
		
		// 通知界面更新[eventbus]
		postEvent(QAODefine.O_METHOD_GET_IN_TRUST, EventTag.ETAG_SERVING_CSTM_CHANGE);
		postEvent(mAppInfo.getSessionBean(s_id), EventTag.ETAG_IN_TRUST_CHANGE);
	}

	private void parseCstmJoinIn() throws NumberFormatException, JSONException {
		if (!qao_data.has(QAODefine.C_ID)) {
			return;
		}
		String c_id = qao_data.getString(QAODefine.C_ID);
//		String s_id = qao_data.getString(QAODefine.S_ID);
		int reason = qao_data.optInt("reason"); // [待新增]会话结束原因
		CustomerBean cstm = mAppInfo.getCustomerBean(c_id);
		if (cstm == null) {
			cstm = new CustomerBean();
			cstm.initCustomerInfo(qao_data);
			mAppInfo.addCustomer(cstm);
		} else {
			cstm.initCustomerInfo(qao_data);
		}
		cstm.setCstmType(CustomerType.CustomerType_ServingAlive);
		
		// [新增]坐席日志记录
		WorkerLogUtil.insertCustomerJoinInLog(cstm, reason);
				
		// 通知[界面更新][eventbus]
		//[修改][等待getCustomerMessages]postEvent(mAppInfo, EventTag.ETAG_SERVING_CSTM_CHANGE);
		
		// 请求客户详细信息和当前会话消息
		CustomerRequest customerRequest = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, mContext);
		customerRequest.getCustomerInfo(c_id);
		customerRequest.getCustomerMessages(c_id);

		/* [修改]震动&铃声 非通知栏提示 */
		mApplication.noticeMessage("有客户接入服务");
	}

	private void parseCstmJoinOut() throws NumberFormatException, JSONException {
		if (!qao_data.has(QAODefine.C_ID)) {
			return;
		}
		String c_id = qao_data.getString(QAODefine.C_ID);
		int reason = qao_data.optInt("reason"); // [新增]会话结束原因
		CustomerBean customer = mAppInfo.getCustomerBean(c_id);
		if (customer != null) {
			customer.setClosingReason(reason);
		}
		// [新增]坐席日志记录
		WorkerLogUtil.insertCustomerJoinOutLog(customer, reason);
		
		mAppInfo.removeCustomer(customer);
		mAppInfo.clearCustomerSession(customer);
		
		// 通知界面更新[eventbus]
		postEvent(QAODefine.O_METHOD_CSTM_JOIN_OUT, EventTag.ETAG_SERVING_CSTM_CHANGE);
		postEvent(customer, EventTag.ETAG_CSTM_OUT);
		postEvent(customer, EventTag.ETAG_VISITORS_IN);
	}

	private void parseCstmWaitIn() throws NumberFormatException, JSONException {
		if (!qao_data.has(QAODefine.C_ID)) {
			return;
		}
		String c_id = qao_data.getString(QAODefine.C_ID);
//		String s_id = qao_data.getString(QAODefine.S_ID);
		CustomerBean cstm = mAppInfo.getCustomerBean(c_id);
		if (cstm == null) {
			cstm = new CustomerBean();
			cstm.initCustomerInfo(qao_data);
			mAppInfo.addCustomer(cstm);
		} else {
			cstm.initCustomerInfo(qao_data);
		}
		cstm.setCstmType(CustomerType.CustomerType_WaitingAlive);
		cstm.setLastType(CustomerBean.LAST_TYPE_OF_CWAIT_IN);
		
		// 通知界面更新[eventbus]
		//[修改][等待getCustomerMessages]postEvent(mAppInfo, EventTag.ETAG_WAITING_CSTM_CHANGE);
		
		// 请求客户详细信息和当前会话消息
		CustomerRequest customerRequest = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, mContext);
		customerRequest.getCustomerInfo(c_id);
		customerRequest.getCustomerMessages(c_id);
		
		/* [修改]震动&铃声 非通知栏提示 */
		mApplication.noticeMessage("有客户等待服务");
//		showNitification(
//				cstm, 
//				session.getS_id(), 
//				mContext.getString(R.string.notify_wait_in),
//				Config.EXTRA_NOTIFY_TYPE_WAIT_IN,
//				Config.NOTIFY_ID_WAIT_IN);
	}

	private void parseCstmWaitOut() throws NumberFormatException, JSONException {
		if (!qao_data.has(QAODefine.C_ID)) {
			return;
		}
		String c_id = qao_data.getString(QAODefine.C_ID);
		CustomerBean customer = mAppInfo.getCustomerBean(c_id);
		mAppInfo.removeCustomer(customer);
		mAppInfo.clearCustomerSession(customer);
		// [通知界面]更新[eventbus]
		postEvent(mAppInfo, EventTag.ETAG_WAITING_CSTM_CHANGE);
		postEvent(customer, EventTag.ETAG_CSTM_WAIT_OUT);
	}

	private void parseGetHistoryVisitorInfo() throws NumberFormatException, JSONException {
		String visitor_id = qao_data.optString(QAODefine.VISITOR_ID);
		JSONObject virtualInfo = qao_data.optJSONObject("virtual");
		if (!visitor_id.isEmpty() && virtualInfo != null) {
			CustomerBean customer = mAppInfo.getVisitor(visitor_id);
			if (customer != null) {
				customer.updateVirtualInfo(virtualInfo);
				if (customer.getVirtual() != null) {
					String real_id = customer.getVirtual().getReal_id();
					if (null != real_id && !real_id.isEmpty()) {
						List<CustomerRealBean> list = DataSupport.where("real_id = ?", real_id).find(CustomerRealBean.class);
						Logger.d("QAOTicket", "[litepal] read real_id.isEmpty = " + list.isEmpty());
						if (list.isEmpty()) {
							CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, mContext);
							cReq.getHistoryVisitorReal(real_id, customer.getVisitor_id());
						}
					} else {
						postEvent(customer, EventTag.ETAG_UPDATE_CSTM_INFO);
					}
				}
			}
		}
		// [通知界面][eventbus]
	}

	private void parseGetHistoryVisitorReal() throws NumberFormatException, JSONException {
		String visitor_id = qao_data.optString(QAODefine.VISITOR_ID);
		JSONObject realInfo = qao_data.optJSONObject("real");
		if (!visitor_id.isEmpty() && realInfo != null) {
			CustomerBean customer = mAppInfo.getVisitor(visitor_id);
			if (null == customer) {
				return;
			}
			customer.updateRealInfo(realInfo);
			// [通知界面][eventbus]
			postEvent(customer, EventTag.ETAG_UPDATE_CSTM_INFO);
		}
	}

	private void parseGetCustomerMessages() throws NumberFormatException, JSONException {
		if (!qao_data.has(QAODefine.C_ID)) {
			return;
		}
		String c_id = qao_data.getString(QAODefine.C_ID);
		finish = qao_data.optBoolean("finish");
		position = qao_data.optInt("position");
		num = qao_data.optInt("num");
		CustomerBean customer = mAppInfo.getCustomerBean(c_id);
		if (customer == null) {
			return;
		}
		if (qao_data.has("session")) {
			String s_id = qao_data.getJSONObject("session").getString(QAODefine.S_ID);
			SessionBean cacheSession = mAppInfo.getSessionBean(s_id);
			if (customer.getSession() == null && cacheSession != null) {
				customer.setSession(cacheSession);
			} else if (customer.getSession() == null) {
				customer.setSession(new SessionBean());
			}
			customer.getSession().updateSessionInfo(qao_data.getJSONObject("session"));
			mAppInfo.addSession(customer.getSession());
		}
		if (qao_data.has("messages") && num > 0) {
			SessionBean session = customer.getSession();
			if (session != null) {
				JSONArray msg_array = qao_data.getJSONArray("messages");
				if (null == msg_array) {
					Logger.e(TAG, "GET_CUSTOMER_MESSAGES get Messages array null");
					return;
				}
				if (session.getMessageArray() == null) {
					session.setMessageArray(new ArrayList<V5Message>());
//					session.setUnreadMessageNum(0);// -1
				} else {
					session.getMessageArray().clear();
//					if (session.getUnreadMessageNum() < 0) {
//						session.setUnreadMessageNum(0);
//					}
				}
				// 逆序获取消息列表
				for (int i = msg_array.length() - 1; i >= 0; i--) {
					V5Message message = V5MessageManager.receiveMessage(msg_array.getJSONObject(i));
					//MessageBean message = new MessageBean(msg_array.getJSONObject(i));
					session.addMessage(message);
					
					// [新增]获取message中的custom_content
					if (message.getCustom_content() != null) {
						Iterator<String> it = message.getCustom_content().keys();  
			            while (it.hasNext()) {  
			                String key = (String) it.next();
			                String value = message.getCustom_content().getString(key);
			                customer.getCustom_content().put(key, value);
			            }
					}
				}
//				if (session.getUnreadMessageNum() == -1) {
//					session.setUnreadMessageNum(session.getMessageArray().size());
//				} else if (session.getUnreadMessageNum() == 0) {
//					session.setUnreadMessageNum(-1);
//				}
				Logger.d(TAG, "[parseGetCustomerMessage] titalNum:" + mAppInfo.getTotalUnreplyMessageNum(customer) + 
						" readedNum:" + session.getReadedNum());
				if (customer.getReadedNumCache() > 0) {
					session.setReadedNum(customer.getReadedNumCache());
					customer.setReadedNumCache(0);
				}
				
				int unreadNum = 0;
				int totalUnreply = mAppInfo.getTotalUnreplyMessageNum(customer);
				if (session.getReadedNum() > 0 && session.getReadedNum() <= totalUnreply) {
					unreadNum = totalUnreply - session.getReadedNum();
				} else {
					unreadNum = totalUnreply;
				}
				session.setUnreadMessageNum(unreadNum);
				
				postEvent(session, EventTag.ETAG_MESSAGE_ARRAY_CHANGE);
			} else {
				Logger.e(TAG, "parseGetCustomerMessages -> customer.getSession null");
			}
		}
		
//		// [修改]等待全部会话获取完成 -> [取消修改]
//		for (CustomerBean cstm : mAppInfo.getCustomerMap().values()) {
//			if (cstm.getSession() == null) {
//				return;
//			}
//		}
		
		// [通知界面][eventbus]
		if (customer.getCstmType() == CustomerType.CustomerType_ServingAlive) {
			postEvent(QAODefine.O_METHOD_GET_CUSTOMER_MESSAGES, EventTag.ETAG_SERVING_CSTM_CHANGE);
		} else if (customer.getCstmType() == CustomerType.CustomerType_WaitingAlive) {
			postEvent(mAppInfo, EventTag.ETAG_WAITING_CSTM_CHANGE);
		}
	}

	@Override
	protected void errorHandle() {
		if (o_method.equals(QAODefine.O_METHOD_PICK_CUSTOMER)) { // 主动接入失败
			postEvent(mAppInfo, EventTag.ETAG_PICK_CSTM_ERROR);
		}
//		switch (o_method) {
//		case QAODefine.O_METHOD_SET_CUSTOMER_INFO:
//			// 提示设置信息失败，重新获得座席信息
//			showToast(mContext.getString(R.string.err_set_customer_info) + ": " + o_errmsg);
//			break;
//		case QAODefine.O_METHOD_PICK_CUSTOMER:
//			showToast(mContext.getString(R.string.err_pick_customer) + ": " + o_errmsg);
//			break;
//		case QAODefine.O_METHOD_SWITCH_CUSTOMER:
//			showToast(mContext.getString(R.string.err_switch_customer) + ": " + o_errmsg);
//			break;
//		case QAODefine.O_METHOD_SET_IN_TRUST:
//			showToast(mContext.getString(R.string.err_set_in_trust) + ": " + o_errmsg);
//			break;
//		case QAODefine.O_METHOD_END_SESSION:
//			showToast(mContext.getString(R.string.err_end_session) + ": " + o_errmsg);
//			break;
//		case QAODefine.O_METHOD_GET_CUSTOMER_MESSAGES:
//			showToast(mContext.getString(R.string.err_get_customer_messages) + ": " + o_errmsg);
//			break;
//		default:
//			showToast(o_type + "." + o_method + ": " + o_errmsg);
//			break;
//		}
	}
}
