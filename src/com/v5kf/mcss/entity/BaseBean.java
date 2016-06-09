package com.v5kf.mcss.entity;

import org.litepal.crud.DataSupport;

import com.v5kf.mcss.CustomApplication;

public class BaseBean extends DataSupport {
	

	// 自定义标签tag
	private int tag;

	protected String getResString(int resId) {
		return CustomApplication.getInstance().getString(resId);
	}

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}
}
