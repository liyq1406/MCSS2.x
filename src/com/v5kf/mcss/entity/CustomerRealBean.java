package com.v5kf.mcss.entity;

import org.json.JSONException;
import org.json.JSONObject;

import com.v5kf.mcss.config.QAODefine;

public class CustomerRealBean extends BaseBean {
	private long id; /*db*/
	// 还有: "gender":"0","id":"1","wb_account_id":"0","wx_account_id":"0","yx_account_id":"0"
	private String real_id;
	private String nickname;
	private String realname;
	private String address;
	private String city;
	private String province;
	private String company;
	private String country;
	private String email;
	private String phone;
	private String qq;
	private String weixin;
	private String active_time;
	private int gender;		// 性别：String -> int
	
	public CustomerRealBean() {
		// TODO Auto-generated constructor stub
	}
	
	public CustomerRealBean(JSONObject json) throws NumberFormatException, JSONException {
		// TODO Auto-generated constructor stub
		updateRealInfo(json);
	}
	
	public void updateRealInfo(JSONObject real) throws NumberFormatException, JSONException {
		if (real.has("error") && real.optInt("error") < 0) {
			return;
		}
		// 解析get_customer_info的virtual内容
		if (real.has(QAODefine.REAL_ID)) {
			real_id = real.getString(QAODefine.REAL_ID);
		}
		if (real.has("gender")) {
//			gender = Integer.parseInt(real.getString("gender"));
			gender = real.getInt("gender");
		}
		nickname = real.optString(QAODefine.NICKNAME);
		active_time = real.optString("active_time");
		realname = real.optString("realname");
		address = real.optString("address");
		city = real.optString("city");
		province = real.optString("province");
		company = real.optString("company");
		country = real.optString("country");
		email = real.optString("email");
		phone = real.optString("phone");
		qq = real.optString("qq");
		weixin = real.optString("weixin");
	}
	
	public String getReal_id() {
		return real_id;
	}
	
	public void setReal_id(String real_id) {
		this.real_id = real_id;
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
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
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
	
	public String getCompany() {
		return company;
	}
	
	public void setCompany(String company) {
		this.company = company;
	}
	
	public String getCountry() {
		return country;
	}
	
	public void setCountry(String country) {
		this.country = country;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPhone() {
		return phone;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getQq() {
		return qq;
	}
	
	public void setQq(String qq) {
		this.qq = qq;
	}
	
	public String getWeixin() {
		return weixin;
	}
	
	public void setWeixin(String weixin) {
		this.weixin = weixin;
	}
	
	public String getActive_time() {
		return active_time;
	}
	
	public void setActive_time(String active_time) {
		this.active_time = active_time;
	}
	
	public int getGender() {
		return gender;
	}
	
	public void setGender(int gender) {
		this.gender = gender;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
/*
	public void serialReal(JSONObject json) throws JSONException {
		if (null != realname && !realname.isEmpty()) {
			json.putOpt("realname", realname);
		}
		if (null != address && !address.isEmpty()) {
			json.putOpt("address", address);
		}
		if (null != city && !city.isEmpty()) {
			json.putOpt("city", city);
		}
		if (null != province && !province.isEmpty()) {
			json.putOpt("province", province);
		}
		if (null != company && !company.isEmpty()) {
			json.putOpt("company", company);
		}
		if (null != country && !country.isEmpty()) {
			json.putOpt("country", country);
		}
		if (null != email && !email.isEmpty()) {
			json.putOpt("email", email);
		}
		if (null != phone && !phone.isEmpty()) {
			json.putOpt("phone", phone);
		}
		if (null != qq && !qq.isEmpty()) {
			json.putOpt("qq", qq);
		}
		if (null != weixin && !weixin.isEmpty()) {
			json.putOpt("weixin", weixin);
		}
	}
*/
}
