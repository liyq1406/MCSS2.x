package com.v5kf.mcss.qao.receive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import android.content.Context;
import android.os.Handler;

import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.entity.CustomerBean.CustomerType;
import com.v5kf.mcss.entity.CustomerVirtualBean;
import com.v5kf.mcss.entity.MessageBean;
import com.v5kf.mcss.entity.SessionBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.QAOManager;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.utils.Logger;

public class QAOTicket extends QAOBase {
	private static final String TAG = "QAOTicket";
	/* messageArrayMap用于一次性保存历史消息到数据库 */
	private static Map<String, List<MessageBean>>  messageArrayMap = new HashMap<String, List<MessageBean>>();
	
	public QAOTicket(JSONObject jsonMsg, Context context, Handler mHandler) throws JSONException {
		super(jsonMsg, context, mHandler);
		this.o_type = QAODefine.O_TYPE_WTICKET;
		this.o_method = jsonMsg.getString(QAODefine.O_METHOD);
	}

	@Override
	public void process() throws JSONException {
		if (o_error != 0) {
			Logger.e(TAG, o_method + ".o_errmsg:" + qao_data.optString(QAODefine.O_ERRMSG));
			return;
		}
		switch (o_method) {
		case QAODefine.O_METHOD_GET_CUSTOMER_SESSION: 
			parseGetCustomerSession();
			break;

		case QAODefine.O_METHOD_GET_WORKER_SESSION: 
			parseGetWorkerSession();
			break;

		case QAODefine.O_METHOD_GET_MESSAGES: 
			parseGetMessages();
			break;
			
		case QAODefine.O_METHOD_GET_HISTORICAL_CUSTOMER:
			parseGetHistoricalCustomer();
			break;

		default:
			throw new JSONException("Unknow o_method:" + o_method + " of o_type:" + o_type);
		}
	}

	private void parseGetHistoricalCustomer() throws JSONException {
		if (qao_data.getLong("o_sequence") >= 2147483648L) {
			// [更新界面][eventbus]
			postEvent(Integer.valueOf((int) (qao_data.getLong("o_sequence") - 2147483648L)), EventTag.ETAG_VISITORS_CHANGE);
			return;
		}
		String v_id = qao_data.getString(QAODefine.VISITOR_ID);
		int iface = qao_data.getInt(QAODefine.INTERFACE);
		CustomerBean visitor = mAppInfo.getVisitor(v_id);
		if (visitor == null) {
			visitor = new CustomerBean();
			visitor.setCstmType(CustomerType.CustomerType_Visitor);
			visitor.initCustomerInfo(qao_data);
			mAppInfo.addVisitor(visitor);
			// [修改]不需要立即请求getCustomerInfo，已携带nickname、photo等基本信息
			/* 不存在visitor则请求visitor_info */
			List<CustomerVirtualBean> list = DataSupport.where("visitor_id = ?", v_id).find(CustomerVirtualBean.class);
			Logger.d("QAOTicket", "[litepal] read visitor_id.isEmpty = " + list.isEmpty());
			if (list.isEmpty()) {
				CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, mContext);
				cReq.getHistoryVisitorInfo(v_id, iface);
			}
//			cReq.getCustomerInfo(v_id, iface, visitor.getChannel(), visitor.getService());
			visitor.setLastType(CustomerBean.LAST_TYPE_OF_HISTORICAL_CSTM);
		} else {
			visitor.initCustomerInfo(qao_data);
		}
		if (qao_data.has("last_time")) { // 最新会话结束时间
			if (visitor.getLast_time() < qao_data.getLong("last_time")) {
				visitor.setLast_time(qao_data.getLong("last_time"));
			}
		}
	}

	private void parseGetCustomerSession() throws NumberFormatException, JSONException {
		String visitor_id = qao_data.getString(QAODefine.VISITOR_ID);
		CustomerBean customer = mAppInfo.getVisitor(visitor_id);
		if (customer == null) {
			for (CustomerBean cstm : mAppInfo.getCustomerMap().values()) { // [修改]历史列表和客户列表不统一问题
				if (cstm.getVisitor_id() != null && cstm.getVisitor_id().equals(visitor_id)) {
					customer = cstm;
				}
			}
			if (customer == null) {
				for (CustomerBean cstm : mAppInfo.getMonitorMap().values()) { // [修改]历史列表和客户列表不统一问题
					if (cstm.getVisitor_id() != null && cstm.getVisitor_id().equals(visitor_id)) {
						customer = cstm;
					}
				}
			}
		}
		if (customer == null) {
			Logger.e(TAG, "[parseGetCustomerSession] null customer");
		}
		if (qao_data.getLong("o_sequence") >= 2147483648L) {
//			Bundle bundle = BundleManager.getBundleOfCustomerSession(qao_data.getString(QAODefine.CUSTOMER_ID));
			// [通知更新][eventbus]
			postEvent(customer, EventTag.ETAG_SESSION_ARRAY_CHANGE);
			return;
		}
		String session_id = qao_data.getString(QAODefine.SESSION_ID);
		SessionBean session = mAppInfo.getSessionBean(session_id); // [修改]正在进行的会话未退出mSessionMap导致已存在该会话，历史会话无法加入
		if (session == null) {
			session = new SessionBean();
			if (customer != null) {
				if (customer.getSessionArray() == null) {
					customer.setSessionArray(new ArrayList<SessionBean>());
				}
				customer.getSessionArray().add(session);
			}
		} else {
			if (customer != null) {
				if (customer.getSessionArray() == null) {
					customer.setSessionArray(new ArrayList<SessionBean>());
				}
				if (!customer.getSessionArray().contains(session)) {
					if (session.getMessageArray() != null) {
						session.getMessageArray().clear(); // 防止消息重复
					}
					customer.getSessionArray().add(session);
				}
			}
		}
		session.updateSessionInfo(qao_data);
		mAppInfo.addSession(session);
//		List<SessionBean> list = DataSupport.where("s_id = ?", session.getS_id()).find(SessionBean.class);
//		if (!list.isEmpty()) {
//			session.updateAll("s_id = ?", session.getS_id());
//			Logger.d("QAOTicket", "[litepal] update s_id = " + session.getS_id());
//		} else {
//			session.save();
//			Logger.d("QAOTicket", "[litepal] save s_id = " + session.getS_id());
//		}
		
		/* 取消自动查询会话消息，改为手动在需要时查询 */
//		TicketRequest tReq = (TicketRequest) RequestManager.getRequest(QAODefine.O_TYPE_WTICKET, mContext);
//		tReq.getMessages(session.getS_id());
	}

	/**
	 * @deprecated
	 * @throws JSONException
	 */
	private void parseGetWorkerSession() throws JSONException {
		if (qao_data.getLong("o_sequence") >= 2147483648L) {
			// [eventbus][通知界面]
			return;
		}
		String session_id = qao_data.getString(QAODefine.SESSION_ID);
		SessionBean session = mAppInfo.getSessionBean(session_id);
		if (session == null) {
			session = new SessionBean();
		}
		session.updateSessionInfo(qao_data);
		//String s_id = session.getS_id();
		String v_id = session.getVisitor_id();
		int iface = qao_data.getInt(QAODefine.INTERFACE);
		//int channel = qao_data.getInt(QAODefine.CHANNEL);
		//int service = qao_data.getInt(QAODefine.SERVICE);
		if (!mAppInfo.hasVisitor(v_id)) {
			CustomerBean visitor = new CustomerBean();
			visitor.initCustomerInfo(qao_data);
			mAppInfo.addVisitor(visitor);
			/* 不存在visitor则请求visitor_info */
			List<CustomerVirtualBean> list = DataSupport.where("visitor_id = ?", v_id).find(CustomerVirtualBean.class);
			if (list.isEmpty()) {
				CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, mContext);
//				cReq.getCustomerInfo(v_id, iface, channel, service);
				cReq.getHistoryVisitorInfo(v_id, iface);
			}
			visitor.setLastType(CustomerBean.LAST_TYPE_OF_HISTORICAL_CSTM);
		}
//		TicketRequest tReq = (TicketRequest) RequestManager.getRequest(QAODefine.O_TYPE_WTICKET, mContext);
//		tReq.getMessages(s_id);
	}

	private void parseGetMessages() throws NumberFormatException, JSONException {
		String sid = qao_data.getString(QAODefine.SESSION_ID);
		SessionBean session = mAppInfo.getSessionBean(sid);
		if (session == null) {
			Logger.e(TAG, "parseGetMessages -> null session");
			return;
		}
		if (session.getMessageArray() == null) {
			session.setMessageArray(new ArrayList<V5Message>());
		}
		if (qao_data.getLong("o_sequence") >= 2147483648L) {
			if (sid != null) {
				// 保存历史消息到litepal数据库
				List<MessageBean> list = DataSupport.where("session_id = ?", sid).find(MessageBean.class);
				if (list.isEmpty()) {
					List<MessageBean> msgs = messageArrayMap.get(sid);
					if (null != msgs && !msgs.isEmpty()) {
						DataSupport.saveAll(msgs);
						Logger.d("QAOTicket", "[litepal] MessageBean.save session_id=" + sid);
					}
				} else {
					Logger.d("QAOTicket", "[litepal] MessageBean.has session_id=" + sid);
				}
				if (messageArrayMap.get(sid) != null) {
					messageArrayMap.get(sid).clear();
					messageArrayMap.remove(sid);
				}
			}
			// [更新界面][eventbus]
			postEvent(session, EventTag.ETAG_MESSAGE_ARRAY_CHANGE);
			return;
		}
		MessageBean msgBean = new MessageBean(qao_data);
		if (messageArrayMap.get(sid) == null) {
			messageArrayMap.put(sid, new ArrayList<MessageBean>());
		}
		messageArrayMap.get(sid).add(msgBean);
		Logger.w(TAG, "MessageBean -> text_content:" + msgBean.getText_content() +
				" json_content:" + msgBean.getJson_content());
		
		V5Message message = QAOManager.receiveMessage(msgBean);
		session.getMessageArray().add(message);
	}

	@Override
	protected void errorHandle() {
		//
	}
}
