package com.v5kf.mcss.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import android.text.TextUtils;

import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.CustomerRealBean;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;

public class CustomerBean extends BaseBean implements Serializable {
	public static final int TAG_VISITOR_FROM_ALIVE = 11;
	
	private long id; /*db*/
	public enum CustomerType {
		CustomerType_None,
		CustomerType_ServingAlive,
		CustomerType_WaitingAlive,
		CustomerType_Visitor,
		CustomerType_Monitor
	}
	
	/**
	 * 
	 */
	public static final long serialVersionUID = 7665698410349945844L;
	
	public static final int LAST_TYPE_OF_CJOIN = 1;
	public static final int LAST_TYPE_OF_CLIST = 2;
	public static final int LAST_TYPE_OF_CWAIT_LIST = 3;
	public static final int LAST_TYPE_OF_CWAIT_IN = 4;
	public static final int LAST_TYPE_OF_HISTORICAL_CSTM = 5;
	public static final int LAST_TYPE_OF_NOT = 0;
	
	/* 本地标识（客户端处理逻辑） */
	private int lastType;  /* 0-not last; 
							* 1-last customer_join_in;
							* 2-last get_customer_list; 
							* 3-last get_waiting_customer;
							* 4-last customer_waiting_in;
							*/
	
	/* from customer_join_in */
	private String c_id;		/* get_customer_list "id" */
	private String s_id;		/* get_customer_list "id" */
	private int channel;
	private int iface;
	private int service;
	private String nickname;	/* get_customer_list "name" */
	private String photo;		/* get_customer_list "picurl" */
	private int sex;			/* get_customer_list "sex" */
	private int vip;			/* get_customer_list/customer_join/wait_in[new] */
	private int status;			/* get_customer_list "status" */
	private String visitor_id;	/* 外层visitor_id(default) */
	private long queue_time;	/* 排队时间 */ 
	
	/* virtual get_customer_info->virtual & get_history_visitor_info */
	private CustomerVirtualBean virtual;
	/* real get_customer_info->real & get_history_info */
	private CustomerRealBean real;
	
	/* from get_historical_customer */
	private String accessable;
	private boolean online; // 是否在线[new]
	
	private String f_id;
	
	/* 最新会话结束时间 */
	private long last_time;
	private int queue_no; // 排队序号[new]
	
	// [修改]结构优化，加入session对象
	private SessionBean session;	// 当前会话
	private List<SessionBean> sessionArray; // 历史会话列表

	private int readedNumCache; // [新增]已读消息缓存
	private CustomerType cstmType;  /* 客户类型:3种 */
	private HashMap<String, String> custom_content; // magic自定义透传信息
	/**
	 * 0 - 原因不明
	 * 1 - 机器人指令
	 * 2 - 被转接
	 * 3 - 取消订阅
	 * 4 - 坐席主动结束
	 * 5 - 会话超时
	 * 6 - 微信多客服接管了
	 * 7 - 服务重启中断
	 */
	private int closingReason; // [新增]客户会话结束原因
	/**
	 * 待定
	 */
	private int joinReason; // [未新增]客户会话接入原因

	public CustomerBean() {
		this.setCstmType(CustomerType.CustomerType_None);
		this.lastType = LAST_TYPE_OF_NOT;
	}
	
	/**
	 * 在get_customer_info 或 waiting_in、join_in时初始化
	 * @param json
	 * @throws NumberFormatException
	 * @throws JSONException
	 */
	public CustomerBean(JSONObject json) throws NumberFormatException, JSONException {
		this.setCstmType(CustomerType.CustomerType_None);
		this.lastType = LAST_TYPE_OF_NOT;
		initCustomerInfo(json);
	}
	
//	/**
//	 * Visitor类型初始化
//	 * @param v_id
//	 */
//	public CustomerBean(String v_id, int iface) {
//		this.setCstmType(CustomerType.CustomerType_Visitor);
//		this.lastType = LAST_TYPE_OF_NOT;
//		this.visitor_id = v_id;
//
//		if (iface > 10) {
//			iface = iface / 256;
//		}
//		this.iface = iface;
//	}
//
//	/**
//	 * WaitingCustomer和Customer在get_waiting_list和get_customer_list时初始化
//	 * @param c_id
//	 * @param nickname
//	 */
//	public CustomerBean(String c_id, String nickname) {
//		this.setCstmType(CustomerType.CustomerType_ServingAlive);
//		this.lastType = LAST_TYPE_OF_NOT;
//		this.c_id = c_id;
//		this.nickname = nickname;
//	}
	
	public void initCustomerInfo(JSONObject json) throws NumberFormatException, JSONException {
		if (null == json) {
			return;
		}
		if (json.has(QAODefine.NICKNAME)) {
			nickname = json.getString(QAODefine.NICKNAME);
		}
		if (json.has("vip")) {
			vip = json.getInt("vip");
		}
		if (json.has("photo")) {
			photo = json.getString("photo");
		}
		if (json.has("queue_time")) {
			queue_time = json.getLong("queue_time");
		}
		if (json.has("queue_no")) {
			queue_no = json.getInt("queue_no");
		}
		if (json.has("accessable")) {
			accessable = json.getString("accessable");
		}
		if (json.has("online")) {
			online = json.getBoolean("online");
		}
		if (json.has(QAODefine.VISITOR_ID)) {
			visitor_id = json.getString(QAODefine.VISITOR_ID);
		}
		if (json.has(QAODefine.C_ID)) {
			c_id = json.getString(QAODefine.C_ID);
		}
		if (json.has(QAODefine.S_ID)) {
			setS_id(json.getString(QAODefine.S_ID));
		}
		if (json.has(QAODefine.F_ID)) {
			f_id = json.getString(QAODefine.F_ID);
		}
		if (json.has("interface")) {
			iface = json.getInt("interface");
			if (iface > 100) {
				iface = iface / 256;
			}
		}
		if (json.has("service")) {
			service = json.getInt("service");
		}
		if (json.has("channel")) {
			channel = json.getInt("channel");
		}
		if (json.has("sex")) {
			sex = json.getInt("sex");
		}
		if (json.has(QAODefine.S_ID)) {
			String sid = json.getString(QAODefine.S_ID);
			if (this.session == null) {
				SessionBean cacheSession = CustomApplication.getAppInfoInstance().getSessionBean(sid);
				if (cacheSession == null) {
					this.session = new SessionBean(sid, this.c_id, this.iface, this.channel, this.service);
					CustomApplication.getAppInfoInstance().addSession(this.session);
				} else {
					this.session = cacheSession;
				}
			} else {
				// update session info
			}
		}
		
		if (json.has(QAODefine.U_ID)) {
			if (null == visitor_id) {
				visitor_id = json.getString(QAODefine.U_ID);
			}
		}
		
		// 获取数据库内保存的客户信息
		getCustomerInfoFromDb();
	}
	
	public void updateCustomerInfo(JSONObject json) throws JSONException {
		if (json == null) {
			return;
		}
		JSONObject realInfo = json.optJSONObject("real");
		if (realInfo != null && realInfo.optInt("error") == 0) {
			updateRealInfo(realInfo);
		}
		
		JSONObject virtualInfo = json.optJSONObject("virtual");
		if (virtualInfo != null && virtualInfo.optInt("error") == 0) {
			updateVirtualInfo(virtualInfo);
		}
	}
	
	public void updateRealInfo(JSONObject json) throws NumberFormatException, JSONException {
		if (json == null) {
			return;
		}
		if (this.real == null) {
			this.real = new CustomerRealBean();
		}
		this.real.updateRealInfo(json);
	}
	
	public void updateVirtualInfo(JSONObject json) throws NumberFormatException, JSONException {
		if (json == null) {
			return;
		}
		if (this.virtual == null) {
			this.virtual = new CustomerVirtualBean();
		}
		this.virtual.updateVirtualInfo(json);
		if (iface == 0 && this.virtual.getCustomer_type() != null) {
			iface = UITools.intOfInterfaceType(this.virtual.getCustomer_type());
		}
		if (TextUtils.isEmpty(visitor_id) && this.virtual != null) {
			visitor_id = this.virtual.getVisitor_id();
		}
	}
	
	private void getCustomerInfoFromDb() {
		if (this.visitor_id != null) {
			List<CustomerVirtualBean> vlist = DataSupport.where("visitor_id = ?", this.visitor_id).find(CustomerVirtualBean.class);
			if (!vlist.isEmpty()) {
				Logger.d("CustomerBean", "[litepal] construct read visitor_id = " + visitor_id);
				if (this.virtual == null) {
					this.virtual = vlist.get(0);
				}
				String realId = this.virtual.getReal_id();
				if (realId != null && !realId.isEmpty()) {
					List<CustomerRealBean> rlist = DataSupport.where("real_id = ?", realId).find(CustomerRealBean.class);
					if (!rlist.isEmpty()) {
						Logger.d("CustomerBean", "[litepal] construct read real_id = " + realId);
						if (this.real == null) {
							this.real = rlist.get(0);
						}
					}
				}
			}
		}
	}

	public String getC_id() {
		return c_id;
	}

	public void setC_id(String c_id) {
		this.c_id = c_id;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getIface() {
		return iface;
	}

	public void setIface(int iface) {
		this.iface = iface;
	}
	
	/**
	 * 获取 interface接口 的字符串名称
	 * @return
	 */
	public String getIfaceString() {
		if (getVirtual() != null && !TextUtils.isEmpty(getVirtual().getCustomer_type())) {
			return getVirtual().getCustomer_type();
		} else {
			return UITools.stringOfInterface(getIface());
		}
	}

	public int getService() {
		return service;
	}

	public void setService(int service) {
		this.service = service;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public CustomerVirtualBean getVirtual() {
		if (virtual == null && this.visitor_id != null) {
			List<CustomerVirtualBean> list = DataSupport.where("visitor_id = ?", this.visitor_id).find(CustomerVirtualBean.class);
			if (!list.isEmpty()) {
				Logger.d("CustomerBean", "[litepal] read visitor_id = " + this.visitor_id);
				return list.get(0);
			}
		}
		return virtual;
	}

	public void setVirtual(CustomerVirtualBean virtual) {
		this.virtual = virtual;
	}

	public CustomerRealBean getReal() {
		if (real == null && virtual != null) {
			if (virtual.getReal_id() != null) {
				List<CustomerRealBean> list = DataSupport.where("real_id = ?", virtual.getReal_id()).find(CustomerRealBean.class);
				if (!list.isEmpty()) {
					Logger.d("CustomerBean", "[litepal] read real_id = " + virtual.getReal_id());
					return list.get(0);
				}
			}
		}
		return real;
	}

	public void setReal(CustomerRealBean real) {
		this.real = real;
	}

	public String getVisitor_id() {
		return visitor_id;
	}

	public void setVisitor_id(String visitor_id) {
		this.visitor_id = visitor_id;
	}
	
	
	public String getDefaultName() {
		if (real != null && !TextUtils.isEmpty(real.getRealname())) {
			return real.getRealname();
		} else if (nickname != null && !nickname.isEmpty()) {
			return nickname;
		} else if (real != null && !TextUtils.isEmpty(real.getNickname())) {
			return real.getNickname();
		}  else if (virtual != null && virtual.getNickname() != null &&
				!virtual.getNickname().isEmpty()) {
			return virtual.getNickname();
		}
		
		if (visitor_id != null && !visitor_id.isEmpty()) {
			//Logger.d("CustomerName", "visitor_id=" + visitor_id);
			String s = "" + visitor_id;
			if (s.length() >= 8) {
				return s.substring(0, 3) + getResString(R.string.default_prefix_name) + s.substring(s.length() - 5, s.length());
			} else {
				return s;
			}
		} else if (c_id != null && !c_id.isEmpty()) {
			//Logger.d("CustomerName", "c_id=" + c_id);
			String s = c_id;
			if (s.length() >= 8) {
				return s.substring(0, 3) + getResString(R.string.default_prefix_name) + s.substring(s.length() - 5, s.length());
			} else {
				return s;
			}
		}
		
		return "Null";
	}
	
	public String getDefaultPhoto () {
		if (photo != null && !photo.isEmpty()) {
			return photo;
		} else if (virtual != null) {
			return virtual.getPhoto();
		}
		
		return null;
	}

	public int getLastType() {
		return lastType;
	}

	public void setLastType(int lastType) {
		this.lastType = lastType;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String picurl) {
		this.photo = picurl;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public CustomerType getCstmType() {
		return cstmType;
	}

	public void setCstmType(CustomerType cstmType) {
		this.cstmType = cstmType;
	}
	

	public String getAccessable() {
		return accessable;
	}

	public void setAccessable(String accessable) {
		this.accessable = accessable;
	}

	public String getF_id() {
		if (f_id == null || f_id.isEmpty()) {
			long fid = (long)channel << 32;
			if (iface < 10) {
				fid |= ((long)iface << 24);
			} else {
				fid |= ((long)iface << 16);
			}
			fid |= (long)service;
			
			return fid + "";
		}
		
		return f_id;
	}

	public void setF_id(String f_id) {
		this.f_id = f_id;
	}
	
	public String getDefaultRealname() {
		if (real != null && real.getRealname() != null) {
			return real.getRealname();
		}		
		return null;
	}
	
	public String getDefaultPhone() {
		if (real != null && real.getPhone() != null) {
			return real.getPhone();
		}
		return null;
	}
	
	public String getDefaultEmail() {
		if (real != null && real.getEmail() != null) {
			return real.getEmail();
		}
		return null;
	}
	
	public String getDefaultWeixin() {
		if (real != null && real.getWeixin() != null) {
			return real.getWeixin();
		}
		return null;
	}
	
	public String getDefaultQQ() {
		if (real != null && real.getQq() != null) {
			return real.getQq();
		}
		return null;
	}
	
	public String getDefaultCountry() {
		if (real != null && real.getCountry() != null) {
			return real.getCountry();
		} else if (virtual != null && virtual.getCountry() != null) {
			return virtual.getCountry();
		}
		return null;
	}

	public String getDefaultProvince() {
		if (real != null && real.getProvince() != null) {
			return real.getProvince();
		} else if (virtual != null && virtual.getProvince() != null) {
			return virtual.getProvince();
		}
		return null;
	}
	
	public String getDefaultCity() {
		if (real != null && real.getCity() != null) {
			return real.getCity();
		} else if (virtual != null && virtual.getCity() != null) {
			return virtual.getCity();
		}
		return null;
	}
	
	public String getDefaultAddress() {
		if (real != null && real.getAddress() != null) {
			return real.getAddress();
		}
		return null;
	}
	
	public String getDefaultCompany() {
		if (real != null && real.getCompany() != null) {
			return real.getCompany();
		}
		return null;
	}
	
	/**
	 * 性别：0-未知 1-男 2-女
	 * @param getDefaultSex CustomerBean 
	 * @return int
	 * @return
	 */
	public int getDefaultSex() {
		if (real != null && real.getGender() != 0) {
			return real.getGender();
		}
		if (virtual != null && virtual.getGender() != 0) {
			return virtual.getGender();
		}
		return sex;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public SessionBean getSession() {
		return session;
	}

	public void setSession(SessionBean session) {
		this.session = session;
	}

	public List<SessionBean> getSessionArray() {
		return sessionArray;
	}

	public void setSessionArray(List<SessionBean> sessionArray) {
		this.sessionArray = sessionArray;
	}
	
	public void addSession(SessionBean session) {
		if (this.sessionArray == null) {
			this.sessionArray = new ArrayList<SessionBean>();
		}
		this.sessionArray.add(session);
	}

	public long getQueue_time() {
		return queue_time;
	}

	public void setQueue_time(long queue_time) {
		this.queue_time = queue_time;
	}

	public String getS_id() {
		return s_id;
	}

	public void setS_id(String s_id) {
		this.s_id = s_id;
	}

	public HashMap<String, String> getCustom_content() {
		if (custom_content == null) {
			custom_content = new HashMap<String, String>();
		}
		return custom_content;
	}

	public void setCustom_content(HashMap<String, String> custom_content) {
		this.custom_content = custom_content;
	}

	public int getReadedNumCache() {
		return readedNumCache;
	}

	public void setReadedNumCache(int readedNum) {
		this.readedNumCache = readedNum;
	}

	public int getClosingReason() {
		return closingReason;
	}

	public void setClosingReason(int closingReason) {
		this.closingReason = closingReason;
	}

	public long getLast_time() {
		return last_time;
	}

	public void setLast_time(long last_time) {
		this.last_time = last_time;
	}

	public String getDefaultAccountId() {
		if (virtual != null) {
			return virtual.getAccount_id();
		} else {
			return CustomApplication.getAppInfoInstance().getUser().getAccount_id();
		}
	}

	public long getLastestActiveTime() {
		V5Message msg = getLastestMessage();
		if (msg != null && msg.getCreate_time() > 0) {
			return msg.getCreate_time();
		}
		if (getSession() != null && getSession().getFirst_time() > 0) {
			return getSession().getFirst_time();
		}
		return 0;
	}
	
	public V5Message getLastestMessage() {
		if (null == getSession()) {
			return null;
		}
		List<V5Message> msgList = getSession().getMessageArray();
		if (msgList == null || msgList.isEmpty()) {
			return null;
		}
		V5Message message = msgList.get(0);
		if (null != message && message.getCandidate() != null && !message.getCandidate().isEmpty()) {
			V5Message msgContent = message.getCandidate().get(0);
			if (msgContent != null && (msgContent.getDirection() == QAODefine.MSG_DIR_FROM_ROBOT
					|| msgContent.getDirection() == QAODefine.MSG_DIR_FROM_WAITING 
//					|| msgContent.getDirection() == QAODefine.MSG_DIR_R2WM
			// || msgContent.getDirection() == QAODefine.MSG_DIR_R2CW
					)) {
				if (msgContent.getDefaultContent(CustomApplication.getContext()) == null || msgContent.getDefaultContent(CustomApplication.getContext()).isEmpty()) {
					// 排除机器人回答不上的空内容
					return message;
				}
				message = msgContent;
			}
		}
		return message;
	}

	public int getVip() {
		return vip;
	}

	public void setVip(int vip) {
		this.vip = vip;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public int getQueue_no() {
		return queue_no;
	}

	public void setQueue_no(int queue_no) {
		this.queue_no = queue_no;
	}
	
//	public void parseCustomerJoinIn(JSONObject json) throws NumberFormatException, JSONException {
//		if (json.has(QAODefine.C_ID)) {				// get_customer_info
//			c_id = json.getString(QAODefine.C_ID);
//			
//			// customer_join_in
//			nickname = json.getString("nickname");
//			channel = json.getInt("channel");
//			service = json.getInt("service");
//			iface = json.getInt("interface");
//			if (iface > 10) {
//				iface = iface / 256;
//			}
//				
//			if (json.has("photo")) {
//				photo = json.getString("photo");
//			}
//			if (json.has("sex")) {
//				sex = json.getInt("sex");
//			}
//			
//			if (json.has(QAODefine.VISITOR_ID) && null == visitor_id) {
//				visitor_id = json.getString(QAODefine.VISITOR_ID);
//			}
//		}		
//		getCustomerInfoFromDb();
//	}
//	
//
//	public void parseGetCustomerList(JSONObject json) throws JSONException {
//		nickname = json.getString("nickname");
//		if (json.has("photo")) {
//			photo = json.getString("photo");
//		}
//		if (json.has("sex")) {
//			sex = json.getInt("sex");
//		}
//		if (json.has("status")) {
//			status = json.getInt("status");
//		}
//		if (json.has("interface")) {
//			iface = json.getInt("interface");
//			if (iface > 10) {
//				iface = iface / 256;
//			}
//		}
//		if (json.has("service")) {
//			service = json.getInt("service");
//		}
//		if (json.has("channel")) {
//			channel = json.getInt("channel");
//		}
//		if (json.has(QAODefine.VISITOR_ID) && null == visitor_id) {
//			visitor_id = json.getString(QAODefine.VISITOR_ID);
//		}
//		getCustomerInfoFromDb();
//	}
//	
//	public void parseGetHistoricalCustomer(JSONObject json) throws JSONException {
//		accessable = json.getString("accessable");
//		f_id = json.getString("f_id");
//		if (json.has("interface")) {
//			iface = json.getInt("interface");
//			if (iface > 10) {
//				iface = iface / 256;
//			}
//		}
//		if (json.has("service")) {
//			service = json.getInt("service");
//		}
//		if (json.has("channel")) {
//			channel = json.getInt("channel");
//		}
//		if (json.has(QAODefine.VISITOR_ID) && null == visitor_id) {
//			visitor_id = json.getString(QAODefine.VISITOR_ID);
//		}
//		getCustomerInfoFromDb();
//	}
}
