package com.v5kf.mcss.entity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.CustomerBean.CustomerType;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.WorkerRequest;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.cache.ImageLoader;

/**
 * @author chenhy
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-6 下午11:45:30
 * @package com.v5kf.mcss.entity.AppInfoKeeper.java
 * @description 保存会话Map、客户列表等信息
 *
 */
public class AppInfoKeeper {
	private static final String TAG = "AppInfoKeeper";
	private Context mContext;
	
	/* 转发消息临时存储对象 */
	private V5Message mTempMessage;
	private V5Message robotAnswer;
	private WorkerBean mUser;
	private SiteInfo mSiteInfo;
	
	// 下面连个部分常驻内存，历史访客消息用完需清理	
	/* 两个共用Map key: session_id */
	private Map<String, SessionBean> mSessionMap;					
	/* key: customer_id 当前服务中顾客 */
	private Map<String, CustomerBean> mCustomerMap;	
	/* key: visitor_id 历史访客，离线状态 */
	private Map<String, CustomerBean> mVisitorMap;	
	/* key: w_id 企业架构，坐席列表 */
	private Map<String, ArchWorkerBean> mWorkerMap;
	private List<WorkerArch> mWorkerArchs;
	
	/**
	 * 构造函数内进行成员初始化
	 */
	public AppInfoKeeper(Context context) {
		mContext = context;
		mSessionMap = new ConcurrentHashMap<String, SessionBean>();
		mCustomerMap = new ConcurrentHashMap<String, CustomerBean>();
//		mSessionMessageMap = new ConcurrentHashMap<String, List<MessageBean>>();
//		mCSMap = new ConcurrentHashMap<String, String>();
//		mVSListMap = new ConcurrentHashMap<String, List<String>>();
		mVisitorMap= new ConcurrentHashMap<String, CustomerBean>();
	}
	
	public Map<String, CustomerBean> getVisitorMap() {
		return mVisitorMap;
	}
	
	public CustomerBean getVisitor(String v_id) {
		if (null == v_id) {
			Logger.w(TAG, "[getVisitor] null key");
			return null;
		}
		return mVisitorMap.get(v_id);
	}
	
	public boolean hasVisitor(String v_id) {
		if (null == v_id) {
			Logger.w(TAG, "[hasVisitor] null key");
			return false;
		}
		return (mVisitorMap.get(v_id) != null);
	}
	
	
	public void addVisitor(CustomerBean bean) {
		if (null == bean) {
			Logger.e(TAG, "Visitor CustomerBean null");
			return;
		}
		
		String v_id = bean.getVisitor_id();
		if (null == v_id) {
			Logger.w(TAG, "[addVisitor] null key");
			return;
		}
		if (mVisitorMap.containsKey(v_id)) {
			Logger.w(TAG, "Tips: VisitorMap already contains this v_id");
		} else {
			
		}
		mVisitorMap.put(v_id, bean);
	}
	
	public void addCustomer(CustomerBean bean) {
		if (null == bean) {
			Logger.e(TAG, "CustomerBean null");
			return;
		}
		
		String c_id = bean.getC_id();
		if (mCustomerMap.containsKey(c_id)) {
//			Logger.w(TAG, "Tips: CustomerMap already contains this c_id");
//			customerUpdate(bean, mCustomerMap.get(c_id));
		} else {
			
		}
		mCustomerMap.put(c_id, bean);
	}

	/**
	 * 移除正在服务的客户与会话Map，并添加到访客VisitorMap和映射
	 * @param removeCustomer AppInfoKeeper 
	 * @return void
	 * @param c_id
	 */
	public void removeCustomer(CustomerBean cstm) {
		if (null == cstm) {
			Logger.w(TAG, "[removeCustomer] null cstm");
			return;
		}
		mCustomerMap.remove(cstm.getC_id());
		if (cstm.getCstmType() == CustomerType.CustomerType_ServingAlive) {
			// 退出的客户变成历史客户
			cstm.setCstmType(CustomerType.CustomerType_Visitor);
			addVisitor(cstm);
			//clearCustomerSession(cstm);
			// [修改][通知界面]更新历史访客[eventbus]
			
		} else {
			//clearCustomerSession(cstm);
		}
	}

	public SessionBean getSessionBean(String s_id) {
		if (null == s_id) {
			Logger.w(TAG, "[getSessionBean] null key");
			return null;
		}
		return mSessionMap.get(s_id);
	}
	
	public void addSession(SessionBean session) {
		if (null == session) {
			Logger.e(TAG, "SessionBean null");
			return;
		}
		
		String s_id = session.getS_id();
		if (null == s_id) {
			Logger.w(TAG, "[addSession] null s_id");
			return;
		}
		if (mSessionMap.containsKey(s_id)) {
			Logger.w(TAG, "Tips: SessionMap already contains this s_id");
		} else {
			
		}
		mSessionMap.put(s_id, session);
	}
	
	public Map<String, SessionBean> getSessionMap() {
		return mSessionMap;
	}

	
	public Map<String, CustomerBean> getCustomerMap() {
		return mCustomerMap;
	}
	
	public WorkerBean getUser() {
		if (mUser == null) {
			try {
				WorkerRequest wReq = (WorkerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WWRKR, mContext);
//				List<WorkerBean> users = DataSupport.findAll(WorkerBean.class);
//				if (users == null || users.isEmpty()) {
//					wReq.getWorkerInfo();
//				} else {
//					setUser(users.get(0));
//					wReq.getWorkerStatus();
//				}
				wReq.getWorkerInfo();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if (mUser == null) {
			mUser = new WorkerBean();
		}
		
		return mUser;
	}
	
	public void setUser(WorkerBean user) {
		this.mUser = user;
	}
	
	public CustomerBean getCustomerBean(String c_id) {
		if (null == c_id) {
			Logger.w(TAG, "[getCustomerBean] null key");
			return null;
		}
		return mCustomerMap.get(c_id);
	}
	
	/**
	 * 获得统计会话未读消息数量(显示于通知栏上的消息)
	 * @param getUnreadMessageNumOfSession AppInfoKeeper 
	 * @return int
	 * @param s_id
	 * @return
	 */
	public int getUnreadMessageNumOfSession(String s_id) {
		if (null == s_id) {
			Logger.w(TAG, "[getUnreadMessageNumOfSession] null key");
			return 0;
		}
		SessionBean session = getSessionBean(s_id);
		if (null != session) {
			return session.getUnreadMessageNum();
		} else {
			return 0;
		}
	}
	
	/**
	 * 清空统计会话未读消息数量(显示于通知栏上的消息)
	 * @param clearUnreadMessageNumOfSession AppInfoKeeper 
	 * @return boolean
	 * @param s_id
	 * @return
	 */
	public boolean clearUnreadMessageNumOfSession(SessionBean session) {
		if (null != session) {
			session.clearUnreadMessageNum();
			return true;
		} else {
			return false;
		}
	}
	
	public V5Message getLastestMessageOfCustomer(CustomerBean cstm) {
		if (null == cstm) {
			Logger.w(TAG, "[getLastestMessageOfCustomer] null cstm");
			return null;
		}
		if (null == cstm.getSession()) {
			return null;
		}
		List<V5Message> msgList = cstm.getSession().getMessageArray();
		Logger.d(TAG, "getLastestMessageOfCustomer c_id:" + cstm.getC_id() + " s_id:" + cstm.getSession().getS_id());
		if (msgList == null || msgList.isEmpty()) {
			return null;
		}
		V5Message message = msgList.get(0);
		if (null != message && message.getCandidate() != null && !message.getCandidate().isEmpty()) {
			V5Message msgContent = message.getCandidate().get(0);
			if (msgContent != null && (msgContent.getDirection() == QAODefine.MSG_DIR_FROM_ROBOT
					|| msgContent.getDirection() == QAODefine.MSG_DIR_FROM_WAITING
			// || msgContent.getDirection() == QAODefine.MSG_DIR_R2CW
					)) {
				if (msgContent.getDefaultContent(mContext) == null || msgContent.getDefaultContent(mContext).isEmpty()) {
					// 排除机器人回答不上的空内容
					return message;
				}
				message = msgContent;
			}
		}
		return message;
	}
	

//	public String getCreateTimeOfSession(SessionBean session) {
//		if (null == session) {
//			Logger.w(TAG, "[getCreateTimeOfSession] null session");
//			return null;
//		}
//		if (session != null && session.getFirst_time() != null) {
//			if (!session.getFirst_time().isEmpty()) {
//				return session.getFirst_time();
//			}
//		}
//		
//		List<V5Message> msgList = session.getMessageArray();
//		if (msgList == null || msgList.isEmpty()) {
//			Logger.d(TAG, "[getCreateTimeOfSession] 会话消息列表为空");
//			return null;
//		}
//		return DateUtil.longDateToString(msgList.get(0).getCreate_time());
//	}
	
	public ArchWorkerBean addWorker(JSONObject json) throws NumberFormatException, JSONException {
		if (null == json) {
			Logger.e(TAG, "ArchWorkerBean json null");
			return null;
		}		
		if (null == mWorkerMap) {
			mWorkerMap = new ConcurrentHashMap<String, ArchWorkerBean>();
		}
		
		String w_id = String.valueOf(json.getString("id"));
		ArchWorkerBean coWorker = mWorkerMap.get(w_id);
		try {
			if (coWorker == null) {
				coWorker = new ArchWorkerBean(json);
				mWorkerMap.put(w_id, coWorker);
			} else {
				coWorker.parser(json);
			}
		} catch (Exception e) {
			Logger.e(TAG, "<<<Exception>>>: " + e.getMessage());
			e.printStackTrace();
		}
		return coWorker;
	}

	public ArchWorkerBean getCoWorker(String w_id) {
		if (null == w_id || mWorkerMap == null) {
			Logger.w(TAG, "[getCoWorker] null key");
			return null;
		}
		return mWorkerMap.get(w_id);
	}

	public Map<String, ArchWorkerBean> getWorkerMap() {
		if (null == mWorkerMap) {
			mWorkerMap = new ConcurrentHashMap<String, ArchWorkerBean>();
		}
		return mWorkerMap;
	}

	/**
	 * 重置坐席列表状态全为离线使用
	 * @param status
	 */
	public void resetCoWorkerStatus(short status) {
		if (mWorkerMap == null) {
			return;
		}
		for (ArchWorkerBean worker : mWorkerMap.values()) {
			worker.setStatus(status);
		}
	}

	
	/**
	 * 服务中客户数量
	 * @return
	 */
	public int getServingCustomerCount() {
		return mCustomerMap.size() - getWaitingCustomerCount();
	}

	/**
	 * 排队等待中客户数量
	 * @return
	 */
	public int getWaitingCustomerCount() {
		int n = 0;
		for (CustomerBean bean : mCustomerMap.values()) {
			if (bean.getCstmType() == CustomerType.CustomerType_WaitingAlive) {
				n++;
			}
		}
		return n;
	}
	
	public List<SessionBean> getSessionsOnDayOfVisitor(long day, CustomerBean visitor) {
		long dayEnd = day + 24 * 3600;
		Logger.i(TAG, "[getSessionsOnDayOfVisitor] dateStart=" + day + " dateEnd=" + dayEnd);
		
		List<SessionBean> s_list = visitor.getSessionArray();
		if (null == s_list || s_list.isEmpty()) {
			Logger.w(TAG, "Null session list of visitor:" + visitor.getVisitor_id());
			return null;
		}
		
		List<SessionBean> sids = new ArrayList<SessionBean>();
		for (SessionBean session : s_list) {
			long s_id = (Long.parseLong(session.getS_id()) >> 32) * 60;
			if (s_id > day && s_id < dayEnd) {
				sids.add(session);
			}
		}
		
		return sids;
	}
	
	/**
	 * 清空实时客户的所有数据，慎用
	 * @return void
	 */
	public void clearRunTimeInfo() {
		for (CustomerBean cstm : mCustomerMap.values()) {
			removeCustomer(cstm);
			clearCustomerSession(cstm);
		}
		mCustomerMap.clear();
		mSessionMap.clear();
		clearWorkerMap();
		clearWorkerArchs();
		ImageLoader.clearMemoryCache();
		Logger.i(TAG, "清空运行时全部信息");
	}
	
//	/**
//	 * 清空实时会话数据
//	 * @param clearRunTimeSession AppInfoKeeper 
//	 * @return void
//	 */
//	private void clearRunTimeSession() {
//		for (CustomerBean cstm : mCustomerMap.values()) {
//			removeCustomer(cstm);	
//		}
//		mCustomerMap.clear();
//		ImageLoader imgLoader = new ImageLoader(mContext, true, 0);
//		imgLoader.clearMemoryCache();
//		Logger.i(TAG, "clearRunTimeSession清空实时会话信息");
//	}
	
	public void clearCustomerSession(CustomerBean cstm) {
		if (cstm == null) {
			return;
		}
		if (cstm.getSession() != null && cstm.getCstmType() != CustomerType.CustomerType_ServingAlive
				&& cstm.getCstmType() != CustomerType.CustomerType_WaitingAlive) { // 不清空正在会话的消息
			if (cstm.getSession().getMessageArray() != null) {
				cstm.getSession().getMessageArray().clear();
			}
			mSessionMap.remove(cstm.getSession().getS_id());
		}
		if (cstm.getSessionArray() != null) {
			for (SessionBean session : cstm.getSessionArray()) {
				if (session.getMessageArray() != null) {
					session.getMessageArray().clear();
				}
				mSessionMap.remove(session.getS_id());
			}
			cstm.getSessionArray().clear();
		}
	}
	
	/**
	 * 清空历史客户及会话对应消息
	 * @param clearHistorySession AppInfoKeeper 
	 * @return void
	 */
	public void clearHistoryVisitor() {
		for (CustomerBean cstm : mVisitorMap.values()) {
			clearCustomerSession(cstm);
		}
		mVisitorMap.clear();
		Logger.d(TAG, "清空历史客户");
	}
	
	/**
	 * 释放非实时会话数据内存
	 * @param clearMemory AppInfoKeeper 
	 * @return void
	 */
	public void clearMemory() {
		clearHistoryVisitorSession();		
		ImageLoader.clearMemoryCache();
		Logger.i(TAG, "释放部分不常用内存");
	}

	private void clearHistoryVisitorSession() {
		for (CustomerBean cstm : mVisitorMap.values()) {
			clearCustomerSession(cstm);
		}
		Logger.d(TAG, "清空历史客户会话");
	}

	public V5Message getTempMessage() {
		return mTempMessage;
	}


	public void setTempMessage(V5Message tempMessage) {
		this.mTempMessage = tempMessage;
	}

	public List<WorkerArch> getWorkerArchs() {
		return mWorkerArchs;
	}


	public void setWorkerArchs(List<WorkerArch> workerArchs) {
		this.mWorkerArchs = workerArchs;
	}
	
	public void addWorkerArch(WorkerArch arch) {
		Logger.d(TAG, "[addWorkerArch] Type:" + arch.getType()  + " arch:" + arch.getName());
		if (null == mWorkerArchs) {
			mWorkerArchs = new LinkedList<WorkerArch>();
		}
		mWorkerArchs.add(arch);
		Logger.d(TAG, "[addWorkerArch] Size:" + mWorkerArchs.size());
	}
	
	public void clearWorkerArchs() {
		if (null != mWorkerArchs) {
			mWorkerArchs.clear();
		}
	}
	
	public void clearWorkerMap() {
		if (null != mWorkerMap) {
			mWorkerMap.clear();
			mWorkerMap = null;
		}
	}

	public V5Message getRobotAnswer() {
		return robotAnswer;
	}

	public void setRobotAnswer(V5Message robotAnswer) {
		this.robotAnswer = robotAnswer;
	}

	public SiteInfo getSiteInfo() {
		return mSiteInfo;
	}

	public void setSiteInfo(SiteInfo mSiteInfo) {
		this.mSiteInfo = mSiteInfo;
	}

	// 消息列表为逆序，则需顺序读取
	public int getTotalUnreplyMessageNum(CustomerBean cstm) {
		int totalNum = 0;
		if (cstm.getSession() == null || cstm.getSession().getMessageArray() == null) {
			return totalNum;
		}
		for (int i = 0; i < cstm.getSession().getMessageArray().size(); i++) {
			V5Message msg = cstm.getSession().getMessageArray().get(i);
			if (msg.getDirection() == QAODefine.MSG_DIR_TO_CUSTOMER && 
					msg.getMessage_type() != QAODefine.MSG_TYPE_CONTROL) {
				break;
			}
			if (msg.getDirection() == QAODefine.MSG_DIR_TO_WORKER 
					&& msg.getMessage_type() != QAODefine.MSG_TYPE_CONTROL) {
				if (msg.getCandidate() != null && msg.getCandidate().size() > 0
						&& msg.getCandidate().get(0).getDirection() == QAODefine.MSG_DIR_FROM_ROBOT) {
					break;
				}
				Logger.i(TAG, "candidate:" + msg.getCandidate() != null ? msg.getCandidate().get(0).getDefaultContent(mContext) : "null" + " getTotalUnreplyMessageNum of msg:" + msg.getDefaultContent(mContext));
				totalNum++;
			}
		}
		return totalNum;
	}
	// TODO
	// TODO
	// TODO

}
