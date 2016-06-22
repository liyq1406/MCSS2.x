package com.v5kf.mcss.qao.receive;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;

import com.umeng.analytics.MobclickAgent;
import com.v5kf.client.lib.V5MessageManager;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.entity.CustomerBean.CustomerType;
import com.v5kf.mcss.entity.SessionBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.utils.Logger;

public class QAOMessage extends QAOBase {
	private static final String TAG = "QAOMessage";
//	private MessageBean mMessage;
//	private SharePreferenceUtil mSharedUtil;

	public QAOMessage(JSONObject json, Context context, Handler mHandler) throws JSONException {
		super(json, context, mHandler);
		this.o_type = QAODefine.O_TYPE_MESSAGE;
//		this.mMessage = new MessageBean(json);
//		this.mSharedUtil = mApplication.getSpUtil();
	}

	@Override
	public void process() throws JSONException {
		if (o_error != 0) {
			Logger.e(TAG, "o_errmsg:" + qao_data.optString(QAODefine.O_ERRMSG));
			errorHandle();
			return;
		}
		MobclickAgent.onEvent(mContext,"NEW_MESSAGE");
		Logger.d(TAG, "mApplication.isAppForeground()=" + mApplication.isAppForeground());
		V5Message message = V5MessageManager.receiveMessage(qao_data);
		CustomerBean customer = null;
		
		if (message.getDirection() == QAODefine.MSG_DIR_R2WM) { // 监控消息
			customer = mAppInfo.getMonitorCustomer(message.getC_id());
			if (customer == null) {
				customer = new CustomerBean();
				customer.setC_id(message.getC_id());
				customer.setCstmType(CustomerType.CustomerType_Monitor);
				mAppInfo.addMonitorCustomer(customer);
				SessionBean mSession = new SessionBean(message.getS_id(), message.getC_id());
				customer.setSession(mSession);
				mAppInfo.addSession(mSession);
				
				// 获取客户信息
				CustomerRequest cReq = (CustomerRequest)RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, mContext);
				cReq.getCustomerInfo(message.getC_id());
			}
		} else { // 排队和服务中客户消息
			customer = mAppInfo.getCustomerBean(message.getC_id());
		}
		
		if (customer != null && customer.getSession() != null) {
			// [新增]magic获取message中的custom_content
			if (message.getCustom_content() != null) {
				Iterator<String> it = message.getCustom_content().keys();  
	            while (it.hasNext()) {
	                String key = (String) it.next();
	                String value = message.getCustom_content().getString(key);
	                customer.getCustom_content().put(key, value);
	            }
			}
			
			switch (message.getDirection()) {
			case QAODefine.MSG_DIR_TO_CUSTOMER:
			case QAODefine.MSG_DIR_TO_WORKER:
			case QAODefine.MSG_DIR_FROM_ROBOT:
			case QAODefine.MSG_DIR_FROM_WAITING:
			case QAODefine.MSG_DIR_R2CW:
			case QAODefine.MSG_DIR_R2CQ:
				customer.getSession().addMessage(message, true);
				
				// 未读消息数量
				if (customer.getSession().getUnreadMessageNum() < 0) {
					customer.getSession().setUnreadMessageNum(0);
				}
				if (message.getDirection() == QAODefine.MSG_DIR_TO_WORKER &&
						message.getMessage_type() != QAODefine.MSG_TYPE_CONTROL) {
					if (message.getCandidate() != null && message.getCandidate().size() > 0 
							&& message.getCandidate().get(0).getDirection() == QAODefine.MSG_DIR_FROM_ROBOT) {
						// 机器人已回复不计入数量
					} else {
						customer.getSession().addUnreadMessageNum();
					}
				}
				
				// [通知更新][eventbus]
				if (customer.getCstmType() == CustomerType.CustomerType_ServingAlive) {
					postEvent(o_type, EventTag.ETAG_SERVING_CSTM_CHANGE);
				} else if (customer.getCstmType() == CustomerType.CustomerType_WaitingAlive) {
					postEvent(mAppInfo, EventTag.ETAG_WAITING_CSTM_CHANGE);
				}
				postEvent(message, EventTag.ETAG_NEW_MESSAGE);
				
				/* 有正在排队中的新消息 */
				if (mApplication.getSpUtil().isAllowPushNotify()) {
					if (customer.getCstmType() == CustomerType.CustomerType_WaitingAlive) {
						if (mApplication.getSpUtil().isAllowPushWaitNotify()) {
							/* [修改]震动&铃声 非通知栏提示 */
							//mApplication.noticeMessage(message.getDefaultContent(mContext));
						}
					} else {
						if (message.getDirection() == QAODefine.MSG_DIR_TO_WORKER) {
							mApplication.noticeMessage(message.getDefaultContent(mContext));
						}
					}
				}
				break;
			case QAODefine.MSG_DIR_R2VC: // 有价值客户的消息
				break;
			case QAODefine.MSG_DIR_W2R: // 提问机器人
			case QAODefine.MSG_DIR_R2W: // 机器人回复
				mAppInfo.setRobotAnswer(message);
				postEvent(message, EventTag.ETAG_ROBOT_ANSWER);
				break;
			case QAODefine.MSG_DIR_R2WM: // 监控消息
				customer.getSession().addMessage(message, true);
				postEvent(message, EventTag.ETAG_MONITOR_MESSAGE);
				break;
			}
		}
		
//		mAppInfo.addMessage(mMessage, true);
//		if (mApplication.getSpUtil().isAllowPushNotify()) {
//			if ((!mApplication.isOnChat() || !mMessage.getC_id().equals(mApplication.getOnChatCustomer()))
//					&& mMessage.getMsg_dir() == QAODefine.MSG_DIR_TO_WORKER) {
//				/* [修改]震动&铃声 非通知栏提示 */
//				noticeMessage();
//			} else if (mMessage.getMsg_dir() == QAODefine.MSG_DIR_R2W || 
//					mMessage.getMsg_dir() == QAODefine.MSG_DIR_W2R) {
//				// 提问机器人的回复消息
//			} else if (mMessage.getMsg_dir() != QAODefine.MSG_DIR_TO_CUSTOMER) {
//				if (mMessage.getMsg_dir() == QAODefine.MSG_DIR_FROM_WAITING) {
//					/* 有正在排队中的新消息 */
//					if (mApplication.getSpUtil().isAllowPushWaitNotify()) {
//						/* [修改]震动&铃声 非通知栏提示 */
//						noticeMessage();
//					}
//				} else {
//					/* 震动&铃声 非通知栏提示 */
//					noticeMessage();
//				}
//			}
//		}
//		Logger.d(TAG, "Finish notify &&&");
//		Bundle bundle = null;
//		if (message.getMsg_dir() == QAODefine.MSG_DIR_FROM_WAITING) {
//			bundle = BundleManager.getBundleOfWaitingMessage(message.getC_id(), message.getS_id());
//		} else if (message.getMsg_dir() == QAODefine.MSG_DIR_R2W ||
//				message.getMsg_dir() == QAODefine.MSG_DIR_W2R) {
//			bundle = BundleManager.getBundleOfRobotMessage(message.getC_id(), message.getS_id());
//		} else {
//			bundle = BundleManager.getBundleOfNewMessage(message.getC_id(), message.getS_id());
//		}
//		sendBroadcast(Config.ACTION_ON_MAINTAB, bundle);
	}

	@Override
	protected void errorHandle() {
		// TODO Auto-generated method stub
		
	}
}
