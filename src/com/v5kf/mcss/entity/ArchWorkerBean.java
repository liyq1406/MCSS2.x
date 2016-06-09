package com.v5kf.mcss.entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.v5kf.mcss.config.QAODefine;

/**
 * "organization"组织架构下的坐席worker
 * @author Chenhy
 *
 */
public class ArchWorkerBean extends BaseBean implements Serializable {
	
	private long id; /*db*/

	/**
	 * 
	 */
	public static final long serialVersionUID = 1L;
	/* get_arch_workers */
	private String w_id;
	private short status;
	private short mode;
	private String name;
	private String photo;
	private String realname;
	private int sex;
	private int accepts;
	private int connects;
	private long create_time;
	
	/* Arch */
//	private int depth;
//	private String parent_type;
//	private long org_id;
	private long group_id; //  
	private String group_name; //  
	
	public ArchWorkerBean() {
		// TODO Auto-generated constructor stub
	}
	
	public ArchWorkerBean(JSONObject json) throws NumberFormatException, JSONException {
		parser(json);
	}

	public void parser(JSONObject json) throws NumberFormatException, JSONException {
		w_id = json.getString("id");
		status = (short) json.optInt(QAODefine.WORKER_STATUS);
		mode = (short) json.optInt(QAODefine.WORKER_MODE);
		name = json.optString("name");
		setPhoto(json.optString("photo"));///**/
		realname = json.optString("realname");///**/
		sex = json.optInt("sex");///**/
		create_time = json.optLong("long");///**/
		connects = json.optInt(QAODefine.WORKER_CONNECTS);
		accepts = json.optInt(QAODefine.WORKER_ACCEPTS);
	}
	
	public String getW_id() {
		return w_id;
	}
	
	public void setW_id(String id) {
		this.w_id = id;
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
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}
	
	public String getDefaultName() {
		if (name != null && !name.isEmpty()) {
			return name;
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
	
	public long getGroup_id() {
		return group_id;
	}

	public void setGroup_id(long group_id) {
		this.group_id = group_id;
	}

	public String getGroup_name() {
		return group_name;
	}

	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public long getCreate_time() {
		return create_time;
	}

	public void setCreate_time(long create_time) {
		this.create_time = create_time;
	}

}
