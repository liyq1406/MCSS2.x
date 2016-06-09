package com.v5kf.mcss.ui.entity;

import java.util.List;


public class SessionRecyclerBean {

	private String s_id;
	private String c_id; // v_id
	private String title;
	private String content;
	private String pic;
	private int iface;
	private int service;	/* none */
	private String date;
	
	/* 服务中客户会话属性 */
	private int unreadNum;
	private boolean inTrust;
	
	/* 客户是否可接入 */
	private String accessable;

	/* 历史顾客会话列表(HistoryVisitorFragment) */
	private List<String> session_list;
	
	public SessionRecyclerBean() {
		// TODO Auto-generated constructor stub
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public int getIface() {
		return iface;
	}

	public void setIface(int iface) {
		this.iface = iface;
	}

	public int getUnreadNum() {
		return unreadNum;
	}

	public void setUnreadNum(int unreadNum) {
		this.unreadNum = unreadNum;
	}

	public String getS_id() {
		return s_id;
	}

	public void setS_id(String s_id) {
		this.s_id = s_id;
	}

	public String getC_id() {
		return c_id;
	}

	public void setC_id(String c_id) {
		this.c_id = c_id;
	}
	
	public void addUnread() {
		this.unreadNum++;
	}
	
	public void clearUnread() {
		this.unreadNum = 0;
	}

	public boolean isInTrust() {
		return inTrust;
	}

	public void setInTrust(boolean inTrust) {
		this.inTrust = inTrust;
	}

	public int getService() {
		return service;
	}

	public void setService(int service) {
		this.service = service;
	}

	public List<String> getSession_list() {
		return session_list;
	}

	public void setSession_list(List<String> session_list) {
		this.session_list = session_list;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public String getAccessable() {
		return accessable;
	}

	public void setAccessable(String accessable) {
		this.accessable = accessable;
	}
}
