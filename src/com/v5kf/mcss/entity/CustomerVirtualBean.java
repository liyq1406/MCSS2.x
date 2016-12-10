package com.v5kf.mcss.entity;

import org.json.JSONException;
import org.json.JSONObject;

import com.v5kf.client.lib.V5Util;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.utils.Logger;

/**
 * get_history_visitor_info获得的客户信息
 * @author Chenhy	
 * @e-mail chenhy@v5kf.com
 * @version v1.0 2015-7-11 下午3:06:56
 * @package com.v5kf.mcss.entity of MCSS-Native
 * @file CustomerVirtualBean.java 
 *
 */
public class CustomerVirtualBean extends BaseBean {
	private long id; /*db*/
	// "id": "1",
	private String account_id;
	private String visitor_id;
	private String real_id;
	private String open_id;
	private String nickname; /*  */
	private String customer_type;
	private long create_time;
	private long active_time;
	private String city;
	private String province;
	private String country;
	private String photo;	/*  */
	private String remark;
	private String union_id;
	private int gender; 	// 性别：String -> int
	private String group_id;	// String -> int
	private int auth;		// String -> int 认证状态：-1-不合法，0-未认证，1-关注，2-取消关注
	private int chats;		// String -> int 对话总次数
	private int visits;		// String -> int 访问总次数
	
	private int state;		// String -> int 状态：默认0(-1:黑名单，0:正常，1:星标，2:取消关注)
	
	/* 新增 */
	private long last_service;
	private String last_worker;
	private int os;
	private int vip;
	
	public CustomerVirtualBean() {
		// Auto-generated constructor stub
	}
	
	public CustomerVirtualBean(JSONObject json) throws NumberFormatException, JSONException {
		// Auto-generated constructor stub
		updateVirtualInfo(json);
	}
	
	public void updateVirtualInfo(JSONObject virtual) throws NumberFormatException, JSONException {
		if (virtual.optInt("error") < 0) {
			return;
		}
		
		// 解析get_customer_info的virtual内容
		if (virtual.has(QAODefine.VISITOR_ID)) {
			visitor_id = virtual.getString(QAODefine.VISITOR_ID);
		}
		if (virtual.has(QAODefine.ACCOUNT_ID)) {
			account_id = virtual.getString(QAODefine.ACCOUNT_ID);
			Logger.d("CVB-updateVirtualInfo", "account_id =" + account_id);
		}
		if (virtual.has(QAODefine.REAL_ID)) {
			real_id = virtual.getString(QAODefine.REAL_ID);
		}
		customer_type = virtual.optString(QAODefine.CSTM_CUSTOMER_TYPE);
		nickname = virtual.optString(QAODefine.NICKNAME);
		if (virtual.has("active_time")) {
			try {
				active_time = virtual.getLong("active_time");
			} catch (JSONException e) {
				active_time = V5Util.stringDateToLong(virtual.getString("active_time")) / 1000;
			}
		} else {
			active_time = V5Util.getCurrentLongTime() / 1000;
		}
		if (virtual.has("create_time")) {
			try {
				create_time = virtual.getLong("create_time");
			} catch (JSONException e) {
				create_time = V5Util.stringDateToLong(virtual.getString("create_time")) / 1000;
			}
		} else {
			create_time = V5Util.getCurrentLongTime() / 1000;
		}
		city = virtual.optString("city");
		province = virtual.optString("province");
		country = virtual.optString("country");
		open_id = virtual.optString("open_id");
		photo = virtual.optString("photo");
		remark = virtual.optString("remark");
		union_id = virtual.optString("union_id");
		group_id= virtual.optString("group_id");
		gender= virtual.optInt("gender");
		auth= virtual.optInt("auth");
		chats= virtual.optInt("chats");
		visits= virtual.optInt("visits");
		state= virtual.optInt("state");
		setVip(virtual.optInt("vip"));
		setOs(virtual.optInt("os"));
		setLast_worker(virtual.optString("last_worker"));
		if (virtual.has("last_service")) {
			try {
				setLast_service(virtual.getLong("last_service"));
			} catch (JSONException e) {
				//setLast_service(V5Util.stringDateToLong(virtual.getString("last_service")) / 1000);
			}
		}
	}
	
	public String getAccount_id() {
		return account_id;
	}

	public void setAccount_id(String account_id) {
		this.account_id = account_id;
	}

	public String getVisitor_id() {
		return visitor_id;
	}

	public void setVisitor_id(String visitor_id) {
		this.visitor_id = visitor_id;
	}

	public String getReal_id() {
		return real_id;
	}

	public void setReal_id(String real_id) {
		this.real_id = real_id;
	}

	public String getOpen_id() {
		return open_id;
	}

	public void setOpen_id(String open_id) {
		this.open_id = open_id;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getCustomer_type() {
		return customer_type;
	}

	public void setCustomer_type(String customer_type) {
		this.customer_type = customer_type;
	}

	public long getCreate_time() {
		return create_time;
	}

	public void setCreate_time(long create_time) {
		this.create_time = create_time;
	}

	public long getActive_time() {
		return active_time;
	}

	public void setActive_time(long active_time) {
		this.active_time = active_time;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getUnion_id() {
		return union_id;
	}

	public void setUnion_id(String union_id) {
		this.union_id = union_id;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getGroup_id() {
		return group_id;
	}

	public void setGroup_id(String group_id) {
		this.group_id = group_id;
	}

	public int getAuth() {
		return auth;
	}

	public void setAuth(int auth) {
		this.auth = auth;
	}

	public int getChats() {
		return chats;
	}

	public void setChats(int chats) {
		this.chats = chats;
	}

	public int getVisits() {
		return visits;
	}

	public void setVisits(int visits) {
		this.visits = visits;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getLast_service() {
		return last_service;
	}

	public void setLast_service(long last_service) {
		this.last_service = last_service;
	}

	public String getLast_worker() {
		return last_worker;
	}

	public void setLast_worker(String last_worker) {
		this.last_worker = last_worker;
	}

	public int getOs() {
		return os;
	}

	public void setOs(int os) {
		this.os = os;
	}

	public int getVip() {
		return vip;
	}

	public void setVip(int vip) {
		this.vip = vip;
	}
	
	
}
