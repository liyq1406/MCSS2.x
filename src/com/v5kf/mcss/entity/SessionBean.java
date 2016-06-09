package com.v5kf.mcss.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.v5kf.client.lib.V5Util;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.utils.DateUtil;
import com.v5kf.mcss.utils.Logger;

/**
 * Created by moon.zhong on 2015/2/5.
 */
public class SessionBean extends BaseBean implements Serializable {
	private long id; /*db*/
	
	public static final long serialVersionUID = -7315603128870481404L;
	public static final String TAG = "SessionBean";
	
	protected boolean isActive;		// 是否进行中的会话
	protected boolean isInTrust;	// 托管状态
	protected int mUnreadMessageNum = 0;
	protected int readedNum; // [新增]已读消息缓存
	
	/* customer_join_in */
	private String c_id;
	private String s_id;
	private int channel;	//
	private int iface;		//
	private int service;	//
	private String nickname; //
	private int status;		//add
		
	/* get_worker_session */ // "entry_page": "0","entry_type": "0","finished_type": "0","id": "9264",
	private String visitor_id;
	private String site_id;
	private String worker_id;
	private String account_id;
//	private String create_time; // 已失效
	private long first_time;
	private long last_time;
	private String server_date; // "2016-04-29"
	private int score;
	private int total_times;
	private int wait_times;
	private int total_messages;
	private int miss_messages;
	private int robot_messages;
	private int visitor_messages;
	private int worker_messages;
	
	// [修改]添加消息列表
	private List<V5Message> messageArray;
	
	public SessionBean() {
		//
		this.messageArray = new ArrayList<V5Message>();
		this.server_date = DateUtil.getCurrentTime("yyyy-MM-dd");
	}
	
	public SessionBean(String s_id, String c_id, int iface, int channel, int service) {
		isInTrust = false;
		isActive = true;
		this.s_id = s_id;
		this.c_id = c_id;
		this.iface = iface;
		this.channel = channel;
		this.service = service;
	}
	
	public SessionBean(String s_id, String c_id) {
		isInTrust = false;
		isActive = true;
		this.s_id = s_id;
		this.c_id = c_id;
	}
	
	public SessionBean(JSONObject json) throws NumberFormatException, JSONException {
		isInTrust = false;
		updateSessionInfo(json);
	}

	public void updateSessionInfo(JSONObject json) throws NumberFormatException, JSONException {
		if (null == json) {
			return;
		}
		
		if (json.has(QAODefine.SESSION_ID)) {	// get_xxx_session
			isActive = false;
			account_id = json.optString(QAODefine.ACCOUNT_ID);
			s_id = json.getString(QAODefine.SESSION_ID);
			if (json.has(QAODefine.FIRST_TIME)) {
				try {
					first_time = json.getLong(QAODefine.FIRST_TIME);
				} catch (JSONException e) {
					first_time = V5Util.stringDateToLong(json.getString(QAODefine.FIRST_TIME)) / 1000;
				}
			} else {
				first_time = V5Util.getCurrentLongTime() / 1000;
			}
			if (json.has(QAODefine.LAST_TIME)) {
				try {
					last_time = json.getLong(QAODefine.LAST_TIME);
				} catch (JSONException e) {
					last_time = V5Util.stringDateToLong(json.getString(QAODefine.LAST_TIME)) / 1000;
				}
			} else {
				last_time = V5Util.getCurrentLongTime() / 1000;
			}
			if (json.has(QAODefine.CUSTOMER_ID)) { // 因为get_worker_session没有customer_id
				c_id = json.getString(QAODefine.CUSTOMER_ID);
			}
			if (json.has("server_date")) {
				server_date = json.getString("server_date");
			}
			visitor_id = json.optString(QAODefine.VISITOR_ID);
			worker_id = json.optString(QAODefine.WORKER_ID);
			site_id = json.optString("site_id");
			if (json.has(QAODefine.INTERFACE)) {
				iface = json.optInt(QAODefine.INTERFACE);
			}
			if (json.has(QAODefine.CHANNEL)) {
				channel = json.optInt(QAODefine.CHANNEL);
			}
			if (json.has(QAODefine.SERVICE)) {
				service = json.optInt(QAODefine.SERVICE);
			}
			if (json.has("score")) {
				score = json.optInt("score");
			}
			if (json.has("total_times")) {
				total_times = json.optInt("total_times");
			}
			if (json.has("wait_times")) {
				wait_times = json.optInt("wait_times");
			}
			if (json.has("total_messages")) {
				total_messages = json.optInt("total_messages");
			}
			if (json.has("robot_messages")) {
				robot_messages = json.optInt("robot_messages");
			}
			if (json.has("worker_messages")) {
				worker_messages = json.optInt("worker_messages");
			}
			if (json.has("visitor_messages")) {
				visitor_messages = json.optInt("visitor_messages");
			}
			if (json.has("miss_messages")) {
				miss_messages = json.optInt("miss_messages");
			}
		} else if(json.has(QAODefine.C_ID)) {	// customer_join_in
			isActive = true;
			c_id = json.getString(QAODefine.C_ID);
			s_id = json.getString(QAODefine.S_ID);
			channel = json.getInt(QAODefine.CHANNEL);
			iface = json.getInt(QAODefine.INTERFACE);
			service = json.getInt(QAODefine.SERVICE);
			nickname = json.optString(QAODefine.NICKNAME);
			if (json.has("status")) {
				this.status = json.getInt("status");
			}
		}
		
//		if (iface > 10) {
//			iface = iface / 256;
//		}
	}
	
	public String getSite_id() {
		return site_id;
	}

	public void setSite_id(String site_id) {
		this.site_id = site_id;
	}

	public long getFirst_time() {
		return first_time;
	}

	public void setFirst_time(long first_time) {
		this.first_time = first_time;
	}

	public long getLast_time() {
		return last_time;
	}

	public void setLast_time(long last_time) {
		this.last_time = last_time;
	}

	public String getServer_date() {
		return server_date;
	}

	public void setServer_date(String server_date) {
		this.server_date = server_date;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getTotal_times() {
		return total_times;
	}

	public void setTotal_times(int total_times) {
		this.total_times = total_times;
	}

	public int getWait_times() {
		return wait_times;
	}

	public void setWait_times(int wait_times) {
		this.wait_times = wait_times;
	}

	public int getTotal_messages() {
		return total_messages;
	}

	public void setTotal_messages(int total_messages) {
		this.total_messages = total_messages;
	}

	public int getMiss_messages() {
		return miss_messages;
	}

	public void setMiss_messages(int miss_messages) {
		this.miss_messages = miss_messages;
	}

	public int getRobot_messages() {
		return robot_messages;
	}

	public void setRobot_messages(int robot_messages) {
		this.robot_messages = robot_messages;
	}

	public int getVisitor_messages() {
		return visitor_messages;
	}

	public void setVisitor_messages(int visitor_messages) {
		this.visitor_messages = visitor_messages;
	}

	public int getWorker_messages() {
		return worker_messages;
	}

	public void setWorker_messages(int worker_messages) {
		this.worker_messages = worker_messages;
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

	public String getVisitor_id() {
		return visitor_id;
	}

	public void setVisitor_id(String visitor_id) {
		this.visitor_id = visitor_id;
	}

	public String getAccount_id() {
		return account_id;
	}

	public void setAccount_id(String account_id) {
		this.account_id = account_id;
	}

	public void setUnreadMessageNum(int mUnreadMessageNum) {
		this.mUnreadMessageNum = mUnreadMessageNum;
	}

	public String getC_id() {
		return c_id;
	}

	public void setC_id(String c_id) {
		this.c_id = c_id;
	}

	public String getS_id() {
		return s_id;
	}

	public void setS_id(String s_id) {
		this.s_id = s_id;
	}	

	public int getUnreadMessageNum() {
		return mUnreadMessageNum;
	}
	
	public void addUnreadMessageNum() {
		this.mUnreadMessageNum++;
	}
	
	public void clearUnreadMessageNum() {
		Logger.d(TAG, "--- clearUnreadMessageNum");
		this.mUnreadMessageNum = 0;
	}
	
	public int getReadedNum() {
		return readedNum;
	}

	public void setReadedNum(int readedNum) {
		this.readedNum = readedNum;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getWorker_id() {
		return worker_id;
	}

	public void setWorker_id(String worker_id) {
		this.worker_id = worker_id;
	}

	public boolean isInTrust() {
		return isInTrust;
	}

	public void setInTrust(boolean isInTrust) {
		this.isInTrust = isInTrust;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<V5Message> getMessageArray() {
		return messageArray;
	}

	public void setMessageArray(List<V5Message> messageArray) {
		this.messageArray = messageArray;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void addMessage(V5Message msg, boolean top) {
		if (this.messageArray == null) {
			this.messageArray = new ArrayList<V5Message>();
		}
		if (top) {
			this.messageArray.add(0, msg);
		} else {
			this.messageArray.add(msg);
		}
	}
	
	public void addMessage(V5Message msg) {
		addMessage(msg, false);
	}

	public long getDefaultTime() {
		if (this.first_time > 0) {
			return this.first_time;
		}
		if (this.messageArray != null && this.messageArray.size() > 0) {
			return this.messageArray.get(0).getCreate_time();
		}
		return DateUtil.getCurrentLongTime()/1000;
	}

}
