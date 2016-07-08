package com.v5kf.client.lib.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import android.content.Context;

import com.v5kf.client.lib.V5MessageManager;
import com.v5kf.client.lib.V5Util;
import com.v5kf.mcss.config.QAODefine;


public abstract class V5Message extends DataSupport implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -183946488182040945L;
	public static final int STATE_UNKNOW = 0;
	public static final int STATE_ARRIVED = 1;
	public static final int STATE_FAILURE = 2;
	public static final int STATE_SENDING = 3;
	public static final int HIT_FAILURE = 0;
	public static final int HIT_SUCCESS = 1;
	
	private long id; // 数据库存储id
	private int state; // 发送状态 0-默认未发送 1-发送成功 2-发送失败 3-发送中
	private long session_start; // 会话开始时间，本地记录会话所有消息	
	
	protected long msg_id;
	protected String message_id;
	protected int message_type;
	protected int direction;

	protected long create_time;
	protected int hit; // 问题命中与否：0-未命中 1-命中
	private List<V5Message> candidate;
	protected JSONObject custom_content; // 自定义magic参数，键值对数组形式[{"key":"1","val":"a"},{"key":"2","val":"b"}]
	
	private String c_id;
	private String s_id;
	private long p_id; /*add*/
	
	public V5Message() {
		this.create_time = V5Util.getCurrentLongTime() / 1000;
		this.direction = V5MessageDefine.MSG_DIR_TO_WORKER;
	}
	
	public V5Message(JSONObject jsonMsg) throws NumberFormatException, JSONException {
		if (jsonMsg.has(QAODefine.MESSAGE_TYPE)) {
			message_type = jsonMsg.getInt(V5MessageDefine.MESSAGE_TYPE);
		}
		if (jsonMsg.has(QAODefine.C_ID)) {
			c_id = jsonMsg.getString(QAODefine.C_ID);
		}
		if (jsonMsg.has(QAODefine.S_ID)) {
			s_id = jsonMsg.getString(QAODefine.S_ID);
		} else if (jsonMsg.has(QAODefine.SESSION_ID)) {
			s_id = jsonMsg.getString(QAODefine.SESSION_ID);
		}
		if (jsonMsg.has(QAODefine.P_ID)) {
			p_id = jsonMsg.getLong(QAODefine.P_ID);
		}
		if (jsonMsg.has(V5MessageDefine.DIRECTION)) {
			direction = Integer.parseInt(jsonMsg.getString(V5MessageDefine.DIRECTION));
		}
		if (jsonMsg.has(V5MessageDefine.CREATE_TIME)) {
			try {
				try {
					create_time = jsonMsg.getLong(V5MessageDefine.CREATE_TIME);
				} catch (JSONException e) {
					create_time = V5Util.stringDateToLong(jsonMsg.getString(V5MessageDefine.CREATE_TIME)) / 1000;
				}
			} catch (Exception e) {
				create_time = V5Util.getCurrentLongTime() / 1000;
				e.printStackTrace();
			}
		} else {
			create_time = V5Util.getCurrentLongTime() / 1000;
		}
		if (jsonMsg.has(V5MessageDefine.HIT)) {
			hit = jsonMsg.getInt(V5MessageDefine.HIT);
		}
		if (jsonMsg.has(V5MessageDefine.MESSAGE_ID)) {
			message_id = jsonMsg.getString(V5MessageDefine.MESSAGE_ID);
		}
		if (jsonMsg.has(V5MessageDefine.MSG_ID)) {
			msg_id = jsonMsg.getLong(V5MessageDefine.MSG_ID);
		}
		if (jsonMsg.has(V5MessageDefine.CANDIDATE)) {
			JSONArray array = jsonMsg.optJSONArray(V5MessageDefine.CANDIDATE);
			if (array != null && array.length() > 0) {
				if (null == candidate) {
					candidate = new ArrayList<V5Message>();
				}
				for (int i = 0; i < array.length(); i++) {
					V5Message msg = V5MessageManager.receiveMessage(array.getJSONObject(i));
					candidate.add(msg);
				}
			}
		}
		if (jsonMsg.has(V5MessageDefine.CUSTOM_CONTENT)) {
			JSONArray array = jsonMsg.optJSONArray(V5MessageDefine.CUSTOM_CONTENT);
			if (array != null && array.length() > 0) {
				if (custom_content == null) {
					custom_content = new JSONObject();
				}
				for (int i = 0; i < array.length(); i++) {
					JSONObject item = array.getJSONObject(i);
					if (item != null) {
						custom_content.put(item.getString("key"), item.getString("val"));
					}
				}
			}
		}
	}
	
	public int getMessage_type() {
		return message_type;
	}
	
	public void setMessage_type(int message_type) {
		this.message_type = message_type;
	}
	
	public int getDirection() {
		return direction;
	}
	
	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	public long getCreate_time() {
		return create_time;
	}
	
	public void setCreate_time(long create_time) {
		this.create_time = create_time;
	}
	
	public int getHit() {
		return hit;
	}
	
	public void setHit(int hit) {
		this.hit = hit;
	}
	
	public List<V5Message> getCandidate() {
		return candidate;
	}

	public void setCandidate(List<V5Message> candidate) {
		this.candidate = candidate;
	}
	
	public String getDefaultContent(Context context) {
		String defContentId = null;
		switch (this.message_type) {
		case V5MessageDefine.MSG_TYPE_TIPS:
		case V5MessageDefine.MSG_TYPE_TEXT:
			return ((V5TextMessage)this).getContent();
			
		case V5MessageDefine.MSG_TYPE_IMAGE:
			defContentId = "v5_def_content_image";
			break;
			
		case V5MessageDefine.MSG_TYPE_LOCATION:
			defContentId = "v5_def_content_location";
			break;
			
		case V5MessageDefine.MSG_TYPE_LINK:
			defContentId = "v5_def_content_link";
			break;

		case V5MessageDefine.MSG_TYPE_EVENT:
			defContentId = "v5_def_content_event";
			break;

		case V5MessageDefine.MSG_TYPE_VOICE:
			defContentId = "v5_def_content_voice";
			break;

		case V5MessageDefine.MSG_TYPE_VIDEO:
			defContentId = "v5_def_content_vedio";
			break;

		case V5MessageDefine.MSG_TYPE_SHORT_VIDEO:
			defContentId = "v5_def_content_short_vedio";
			break;

		case V5MessageDefine.MSG_TYPE_ARTICLES:
			defContentId = "v5_def_content_articles";
			break;

		case V5MessageDefine.MSG_TYPE_MUSIC:
			defContentId = "v5_def_content_music";
			break;
			
		case V5MessageDefine.MSG_TYPE_NOTE:
			defContentId = "v5_def_content_note";
			break;
			
		case V5MessageDefine.MSG_TYPE_CONTROL: // 转接人工客服或多客服消息
			if (((V5ControlMessage)this).getCode() == 1 || ((V5ControlMessage)this).getCode() == 3) {
				defContentId = "v5_def_content_switch_worker";
			} else {
				//defContentId = "v5_def_content_control";
				defContentId = "v5_def_content_switch_worker";
			}
			break;
			
		case V5MessageDefine.MSG_TYPE_COMMENT: // 客户提交评价
			defContentId = "v5_def_content_comment";
			break;
			
		case V5MessageDefine.MSG_TYPE_WXCS:
			defContentId = "v5_def_content_wxcs";
			break;
		
		default:
			defContentId = "v5_def_content_unsupport";
			break;		
		}
		return context.getString(V5Util.getIdByName(context, "string", defContentId));
	}
	
	protected void toJSONObject(JSONObject json) throws JSONException {
		if (json == null) {
			return;
		}
		json.put("o_type", "message");
		if (this.msg_id != 0) { // [修改]添加msg_id到发送字符串
			json.put(V5MessageDefine.MSG_ID, this.msg_id);
		}
		if (this.p_id != 0) { // [修改]添加msg_id到发送字符串
			json.put(V5MessageDefine.P_ID, this.p_id);
		}
		if (this.s_id != null) { // [修改]添加s_id、c_id
			json.put(QAODefine.S_ID, this.s_id);
		}
		if (this.c_id != null) {
			json.put(QAODefine.C_ID, this.c_id);
		}
		json.put(V5MessageDefine.MESSAGE_TYPE, this.message_type);
		json.put(V5MessageDefine.DIRECTION, this.direction);
		if (this.custom_content != null && this.custom_content.length() > 0) {
			JSONArray customArray = new JSONArray();
            Iterator<String> it = this.custom_content.keys();  
            while (it.hasNext()) {  
                String key = (String) it.next();  
                String value = this.custom_content.getString(key);
                JSONObject item = new JSONObject();
                item.putOpt("key", key);
                item.putOpt("val", value);
                customArray.put(item);
            }
            json.put(V5MessageDefine.CUSTOM_CONTENT, customArray);
		}
//		if (this.create_time != 0) {
//			json.put(V5MessageDefine.CREATE_TIME, this.create_time);
//		}
	}
		
	public abstract String toJson() throws JSONException;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getSession_start() {
		return session_start;
	}

	public void setSession_start(long session_start) {
		this.session_start = session_start;
	}

	public long getMsg_id() {
		return msg_id;
	}

	public void setMsg_id(long msg_id) {
		this.msg_id = msg_id;
	}

	public String getMessage_id() {
		return message_id;
	}

	public void setMessage_id(String message_id) {
		this.message_id = message_id;
	}

	public JSONObject getCustom_content() {
		return custom_content;
	}

	public void setCustom_content(JSONObject custom_content) {
		this.custom_content = custom_content;
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

	public long getP_id() {
		return p_id;
	}

	public void setP_id(long p_id) {
		this.p_id = p_id;
	}

	public V5Message cloneDefaultRobotMessage() {
		V5Message robotMsg = this.getCandidate().get(0);
		robotMsg.setS_id(this.getS_id());
		robotMsg.setC_id(this.getC_id());
		if (this.p_id != 0) {
			robotMsg.setP_id(p_id);
		}
		return robotMsg;
	}

	public V5Message cloneMessage() {
		V5Message msg = null;
		try {
			msg = V5MessageManager.receiveMessage(new JSONObject(this.toJson()));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msg;
	}
}
