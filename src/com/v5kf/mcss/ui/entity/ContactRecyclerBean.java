package com.v5kf.mcss.ui.entity;

import com.v5kf.mcss.entity.ArchWorkerBean;

public class ContactRecyclerBean {

	public static final int ITEM = 0;
	public static final int SECTION = 1;

	private ArchWorkerBean coWorker;			/* 座席信息 */

	public final int type;					/* 类型：列表段还是列表项 */

	public final String orgpath;			/* 列表段名称集合 */

	public int sectionPosition;
	public int listPosition;

	public ContactRecyclerBean(int type, String paths, ArchWorkerBean worker) {
	    this.type = type;
	    this.orgpath = paths;
	    this.coWorker = worker;
	}

	public String getPath() {
		return orgpath;
	}
	

	public ArchWorkerBean getWorker() {
		return coWorker;
	}

	public void setWorker(ArchWorkerBean mWorker) {
		this.coWorker = mWorker;
	}
	
	public int getType() {
		return type;
	}

}
