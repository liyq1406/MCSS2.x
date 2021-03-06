package com.v5kf.mcss.ui.entity;

import java.io.Serializable;

import android.content.Context;

import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.utils.DateUtil;
import com.v5kf.mcss.utils.Logger;

public class ChatRecyclerBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 699274878680464827L;

	private V5Message mMessage;
	
	private String name;	// Customer Name
	private String time;	// from msgContent->create_time
	
	// [添加]语音相关，播放状态
	private boolean isPlaying;
	
	// 是否会话第一句话(与Adapter是否显示日期相关)
	private boolean isSessionStart;
	
	// 是否被选中(机器人推荐列表相关)
	private boolean isSelected;
	
	// 媒体资源访问错误
	private boolean badUrl;
	
	// 是否加载过媒体资源
	private boolean loaded;
		
	public ChatRecyclerBean(V5Message msg) {
		mMessage = msg;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getDir() {
		if (null != mMessage) {
//			if (mMessage.getCandidate() != null) { // 客户消息
//				return QAODefine.MSG_DIR_TO_WORKER;
//			}
//			// 现可确定消息方向，无需判断candidate
			return mMessage.getDirection();
		}
		
		Logger.w("ChatRecyclerBean", "[getDir] Message is null");
		return QAODefine.MSG_DIR_TO_CUSTOMER;
	}
	
	public String getDefaultTime() {
		if (time != null && !time.isEmpty()) {
			return time;
		}
		
		if (mMessage != null) {
			return DateUtil.longDateToString(mMessage.getCreate_time());
		}
		
		return DateUtil.getCurrentTime();
	}

	public String getM_id() {
		if (mMessage != null) {
			return mMessage.getMessage_id();
		}
		
		Logger.w("ChatRecyclerBean", "[getM_id] Message is null");
		return null;
	}

	public boolean isSessionStart() {
		return isSessionStart;
	}

	public void setSessionStart(boolean isSessionStart) {
		this.isSessionStart = isSessionStart;
	}
	
	public String getDefaultContent(Context context) {
		return mMessage.getDefaultContent(context);
	}

	public String getDefaultCandidate(Context context) {
		if (mMessage != null && mMessage.getCandidate() != null 
				&& !mMessage.getCandidate().isEmpty()) {
			return mMessage.getCandidate().get(0).getDefaultContent(context);
		}
		
		return null;
	}

	public V5Message getMessage() {
		return mMessage;
	}

	public void setMessage(V5Message mMessage) {
		this.mMessage = mMessage;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public void setPlaying(boolean playing) {
		this.isPlaying = playing;
	}

	public boolean isBadUrl() {
		return badUrl;
	}

	public void setBadUrl(boolean badUrl) {
		this.badUrl = badUrl;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

}
