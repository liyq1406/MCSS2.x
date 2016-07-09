package com.v5kf.client.lib.entity;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.v5kf.client.lib.V5Util;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.utils.UITools;

public class V5ImageMessage extends V5Message {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1560610123867478278L;
	public static final int MAX_PIC_W = 480;
	public static final int MAX_PIC_H = 600;
	private String pic_url;
	private String media_id;
//	private String thumbnail_url; // 缩略图
	
	// 素材名称
	private String title;

	private String filePath; // 本地路径
	private boolean upload;
	
	public V5ImageMessage() {
		// TODO Auto-generated constructor stub
		super();
		this.message_type = V5MessageDefine.MSG_TYPE_IMAGE;
	}
	
	/**
	 * 上传本地图片(暂未支持)
	 * @param filePath
	 */
	public V5ImageMessage(String filePath) {
		this.filePath = filePath;
		this.pic_url = null;
		this.media_id = null;
		this.message_type = V5MessageDefine.MSG_TYPE_IMAGE;
		this.create_time = V5Util.getCurrentLongTime() / 1000;
		this.direction = V5MessageDefine.MSG_DIR_TO_WORKER;
	}
	
	public V5ImageMessage(String pic_url, String media_id) {
		this.pic_url = pic_url;
		this.media_id = media_id;
		this.message_type = V5MessageDefine.MSG_TYPE_IMAGE;
		this.create_time = V5Util.getCurrentLongTime() / 1000;
		this.direction = V5MessageDefine.MSG_DIR_TO_WORKER;
	}

	public V5ImageMessage(JSONObject json) throws NumberFormatException, JSONException {
		super(json);
		this.pic_url = json.optString(V5MessageDefine.MSG_PIC_URL);
		this.media_id = json.optString(V5MessageDefine.MSG_MEDIA_ID);
	}

	@Override
	public String toJson() throws JSONException {
		JSONObject json = new JSONObject();
		toJSONObject(json);
		json.put(V5MessageDefine.MSG_PIC_URL, this.pic_url);
		if (null != this.media_id) {
			json.put(V5MessageDefine.MSG_MEDIA_ID, this.media_id);
		}
		return json.toString();
	}
	
	/**
	 * 默认原图路径(文件路径或者URL)
	 * @return
	 */
	public String getDefaultPicUrl() {
		if (!TextUtils.isEmpty(filePath)) {
			return filePath;
		} else {
			return pic_url;
		}
	}
	
	public String getThumbnailPicUrl() {
//		return String.format(Config.APP_RESOURCE_V5_FMT, Config.SITE_ID, getMessage_id());
		return UITools.getThumbnailUrlOfImage(this, Config.SITE_ID);
	}

	public String getPic_url() {
		return pic_url;
	}

	public void setPic_url(String pic_url) {
		this.pic_url = pic_url;
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


//	public String getThumbnail_url() {
//		if (thumbnail_url == null) {
//			if (pic_url != null && pic_url.contains("image.myqcloud.com")) {
//				thumbnail_url = pic_url + "?imageView2/2/w/" + MAX_PIC_W + "/h/" + MAX_PIC_H + "/q/85";
//			} else {
//				thumbnail_url = pic_url;
//			}
//		}
//		return thumbnail_url;
//	}

//	public void setThumbnail_url(String thumbnail_url) {
//		this.thumbnail_url = thumbnail_url;
//	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isUpload() {
		return upload;
	}

	public void setUpload(boolean upload) {
		this.upload = upload;
	}
}
