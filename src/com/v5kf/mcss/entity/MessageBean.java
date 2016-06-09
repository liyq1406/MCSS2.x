package com.v5kf.mcss.entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.v5kf.client.lib.V5Util;
import com.v5kf.client.lib.entity.V5MessageDefine;
import com.v5kf.mcss.config.QAODefine;

/**
 * 消息保存数据库使用
 * @author V5KF_MBP
 *
 */
public class MessageBean extends BaseBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3895987892283108452L;
	private int direction;
	private String message_id;
	private String session_id;
	private int message_type;
	private long create_time;
	private String text_content; // 文本
	private String json_content; // 其他类型
	
	public MessageBean() {
		// TODO Auto-generated constructor stub
	}
	
	public MessageBean(JSONObject json) throws JSONException {
		this.setMessage_type(json.getInt(QAODefine.MESSAGE_TYPE));
		if (json.has(V5MessageDefine.CREATE_TIME)) {
			try {
				create_time = json.getLong(V5MessageDefine.CREATE_TIME);
			} catch (JSONException e) {
				// 时间字符串时区为北京时间，需要转换成标准时间
				create_time = V5Util.stringDateToLong(json.getString(V5MessageDefine.CREATE_TIME)) / 1000;
			}
		} else {
			create_time = V5Util.getCurrentLongTime() / 1000;
		}
		this.setDirection(json.getInt(QAODefine.DIRECTION));
		this.setMessage_id(json.getString(QAODefine.MESSAGE_ID));
		this.setSession_id(json.getString(QAODefine.SESSION_ID));
		if (json.has("text_content")) {
			this.setText_content(json.getString("text_content"));
		}
		if (json.has("json_content")) {
			this.setJson_content(json.getString("json_content"));
		}
	}
	
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}
	public String getMessage_id() {
		return message_id;
	}
	public void setMessage_id(String message_id) {
		this.message_id = message_id;
	}
	public String getSession_id() {
		return session_id;
	}
	public void setSession_id(String session_id) {
		this.session_id = session_id;
	}
	public int getMessage_type() {
		return message_type;
	}
	public void setMessage_type(int message_type) {
		this.message_type = message_type;
	}
	public long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(long create_time) {
		this.create_time = create_time;
	}
	public String getText_content() {
		return text_content;
	}
	public void setText_content(String text_content) {
		this.text_content = text_content;
	}

	public String getJson_content() {
		return json_content;
	}

	public void setJson_content(String json_content) {
		this.json_content = json_content;
	}
}
