package com.v5kf.mcss.utils.cache;

import android.graphics.Bitmap;


public class MediaCache {
	private String localPath;	// 本地路径
	private long duration;		// 秒
	private Bitmap coverFrame;  // 视频帧
	
	public String getLocalPath() {
		return localPath;
	}
	
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public Bitmap getCoverFrame() {
		return coverFrame;
	}

	public void setCoverFrame(Bitmap coverFrame) {
		this.coverFrame = coverFrame;
	}
	
}

