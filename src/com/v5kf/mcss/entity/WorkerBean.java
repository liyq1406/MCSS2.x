package com.v5kf.mcss.entity;

import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;

import android.text.TextUtils;

import com.v5kf.client.lib.V5Util;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.eventbus.EventTag;

/**
 * 登录APP的坐席对象
 * @author Chenhy
 *
 */
public class WorkerBean extends BaseBean {
	private long id; /*db*/
	private String w_id;
	private String e_id;
	private String account_id;
	
	private short status; // 状态：0-离线 1-在线...(查看QAODefine)
	private short mode; // 0- 仅转接  1-自动接入
	private int accepts; // 自动接入数量
	private int connects; // 仅转接数量
	private boolean monitor; // 是否在监控
	
	private String org_id;
	private String nickname;
	private String realname;
	private String job;
	private String phone;
	private String photo;
	private String email;
	private long create_time;	//
	private long active_time;
	private String position;	//
	private String qq;
	private int gender;
	
	public WorkerBean() {
		
	}
	
	
	public void parse(JSONObject json) throws JSONException {
		// get_worker_info
		if (json.has("config")) {
			parseConfig(json.getJSONObject("config"));
		}
		if (json.has("id")) {
			parseId(json.getJSONObject("id"));
		}
	}
	
	
	private void parseConfig(JSONObject config) {
		mode = (short) config.optInt(QAODefine.WORKER_MODE);
		status = (short) config.optInt(QAODefine.WORKER_STATUS);
		connects = config.optInt(QAODefine.WORKER_CONNECTS);
		accepts = config.optInt(QAODefine.WORKER_ACCEPTS);
		if (config.has("monitor")) {
			monitor = config.optBoolean("monitor");
			EventBus.getDefault().post(this, EventTag.ETAG_MONITOR_STATE_CHANGE);
		}
	}
	
	private void parseId(JSONObject id) {
		if (id.has("error") && id.optInt("error") < 0) {
			return;
		}
		
		org_id = id.optString("org_id");
		nickname = id.optString("nickname");
		realname = id.optString("realname");
		job = id.optString("job");
		phone = id.optString("phone");
		photo = id.optString("photo");
		email = id.optString("email");
		qq = id.optString("qq");
		if (id.has("active_time")) {
			try {
				active_time = id.getLong("active_time");
			} catch (JSONException e) {
				active_time = V5Util.stringDateToLong(id.optString("active_time")) / 1000;
			}
		} else {
			active_time = V5Util.getCurrentLongTime() / 1000;
		}
		gender = id.optInt("gender");
		setCreate_time(id.optLong("create_time"));
	}
	
	
	public String getW_id() {
		return w_id;
	}
	
	public void setW_id(String w_id) {
		this.w_id = w_id;
	}
	
	public String getE_id() {
		return e_id;
	}
	
	public void setE_id(String e_id) {
		this.e_id = e_id;
	}
	
	public short getStatus() {
		return status;
	}
	
	public void setStatus(short status) {
		this.status = status;
	}
	
	public short getMode() {
		return mode;
	}
	
	public void setMode(short mode) {
		this.mode = mode;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public String getRealname() {
		return realname;
	}
	
	public void setRealname(String realname) {
		this.realname = realname;
	}
	
	public String getPhone() {
		return phone;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getPhoto() {
		return photo;
	}
	
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public long getActive_time() {
		return active_time;
	}
	
	public void setActive_time(long active_time) {
		this.active_time = active_time;
	}
	
	public String getPosition() {
		return position;
	}
	
	public void setPosition(String position) {
		this.position = position;
	}
	
	public String getQq() {
		return qq;
	}
	
	public void setQq(String qq) {
		this.qq = qq;
	}


	public String getJob() {
		return job;
	}


	public void setJob(String job) {
		this.job = job;
	}


	public int getGender() {
		return gender;
	}


	public void setGender(int gender) {
		this.gender = gender;
	}	
	
	public String getDefaultName() {
		if (nickname != null && !nickname.isEmpty()) {
			return nickname;
		} else {
			return realname;
		}
	}


	public int getAccepts() {
		return accepts;
	}


	public void setAccepts(int accepts) {
		this.accepts = accepts;
	}


	public int getConnects() {
		return connects;
	}


	public void setConnects(int connects) {
		this.connects = connects;
	}


	public String getOrg_id() {
		return org_id;
	}


	public void setOrg_id(String org_id) {
		this.org_id = org_id;
	}


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public String getAccount_id() {
		return account_id;
	}


	public void setAccount_id(String account_id) {
		this.account_id = account_id;
	}


	public long getCreate_time() {
		return create_time;
	}


	public void setCreate_time(long create_time) {
		this.create_time = create_time;
	}
	
	public void addPropertyToJSON(JSONObject json) throws JSONException {
		JSONObject id = new JSONObject();
		if (!TextUtils.isEmpty(nickname)) {
			id.put("nickname", nickname);
		}
		if (!TextUtils.isEmpty(nickname)) {
			id.put("photo", photo);
		}
		json.put("id", id);
	}

	public boolean isMonitor() {
		return monitor;
	}

	public void setMonitor(boolean monitor) {
		this.monitor = monitor;
	}
}
