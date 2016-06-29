package com.v5kf.client.lib.entity;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.v5kf.client.lib.V5Util;

public class V5VideoMessage extends V5Message {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3955469350716412848L;

	// video、short_video
	private String media_id;
	private String thumb_id;
	// video
	private String title;
	private String description;
	
//	private String format;
	private String url;
	private String filePath; // 本地路径
	private Bitmap coverFrame;
	
	public V5VideoMessage() {
		super();
		this.message_type = V5MessageDefine.MSG_TYPE_VIDEO;
	}
	
	/**
	 * 上传本地语音文件(暂未支持)
	 * @param filePath
	 */
	public V5VideoMessage(String filePath) {
		this.filePath = filePath;
		
		this.message_type = V5MessageDefine.MSG_TYPE_VIDEO;
		this.create_time = V5Util.getCurrentLongTime() / 1000;
		this.direction = V5MessageDefine.MSG_DIR_TO_WORKER;
	}
	
	public V5VideoMessage(String url, String thumb_id, String media_id) {
		this.url = url;
		this.media_id = media_id;
		this.thumb_id = thumb_id;
		
		this.message_type = V5MessageDefine.MSG_TYPE_VIDEO;
		this.create_time = V5Util.getCurrentLongTime() / 1000;
		this.direction = V5MessageDefine.MSG_DIR_TO_WORKER;
	}

	public V5VideoMessage(JSONObject json) throws NumberFormatException, JSONException {
		super(json);
		if (json.has(V5MessageDefine.MSG_MEDIA_ID)) {
			this.media_id = json.getString(V5MessageDefine.MSG_MEDIA_ID);
		}
		if (json.has(V5MessageDefine.MSG_THUMB_ID)) {
			this.thumb_id = json.getString(V5MessageDefine.MSG_THUMB_ID);
		}
		if (json.has(V5MessageDefine.MSG_URL)) {
			this.url = json.optString(V5MessageDefine.MSG_URL);
		}
		if (json.has(V5MessageDefine.MSG_TITLE)) {
			this.title = json.getString(V5MessageDefine.MSG_TITLE);
		}
		if (json.has(V5MessageDefine.MSG_DESCRIPTION)) {
			this.description = json.getString(V5MessageDefine.MSG_DESCRIPTION);
		}
	}

	@Override
	public String toJson() throws JSONException {
		JSONObject json = new JSONObject();
		toJSONObject(json);
		if (!TextUtils.isEmpty(this.url)) {
			json.put(V5MessageDefine.MSG_URL, this.url);
		}
		if (!TextUtils.isEmpty(this.media_id)) {
			json.put(V5MessageDefine.MSG_MEDIA_ID, this.media_id);
		}
		if (!TextUtils.isEmpty(this.thumb_id)) {
			json.put(V5MessageDefine.MSG_THUMB_ID, this.thumb_id);
		}
		if (!TextUtils.isEmpty(this.title)) {
			json.put(V5MessageDefine.MSG_TITLE, this.title);
		}
		if (!TextUtils.isEmpty(this.description)) {
			json.put(V5MessageDefine.MSG_DESCRIPTION, this.description);
		}
		return json.toString();
	}

	public String getMedia_id() {
		return media_id;
	}

	public void setMedia_id(String media_id) {
		this.media_id = media_id;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getThumb_id() {
		return thumb_id;
	}

	public void setThumb_id(String thumb_id) {
		this.thumb_id = thumb_id;
	}

	public Bitmap getCoverFrame() {
		return coverFrame;
	}

	public void setCoverFrame(Bitmap coverFrame) {
		this.coverFrame = coverFrame;
	}
}
