package com.v5kf.mcss.entity;

import java.io.Serializable;

public class WorkerLogBean extends BaseBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1499822920201430337L;
	private long id; /*db*/
	private long time;
	private String e_id; 
	private String w_id; 
	private String c_id;
	private String s_id; 
	private String o_type; 
	private String o_method; 
	private int reason;
	private String description;
	
	public WorkerLogBean() {
	}
	
	public WorkerLogBean(long time, String eid, String wid, String cid, String sid, String otype, String omethod, int _reason, String desc) {
		this.time = time;
		this.e_id = eid;
		this.c_id = cid;
		this.w_id = wid;
		this.s_id = sid;
		this.o_type = otype;
		this.o_method = omethod;
		this.reason = _reason;
		this.setDescription(desc);
	}
	
	public String getE_id() {
		return e_id;
	}
	
	public void setE_id(String e_id) {
		this.e_id = e_id;
	}
	
	public String getW_id() {
		return w_id;
	}
	
	public void setW_id(String w_id) {
		this.w_id = w_id;
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
	
	public String getO_type() {
		return o_type;
	}
	
	public void setO_type(String o_type) {
		this.o_type = o_type;
	}
	
	public String getO_method() {
		return o_method;
	}
	
	public void setO_method(String o_method) {
		this.o_method = o_method;
	}
	
	public int getReason() {
		return reason;
	}
	
	public void setReason(int reason) {
		this.reason = reason;
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	} 
}
