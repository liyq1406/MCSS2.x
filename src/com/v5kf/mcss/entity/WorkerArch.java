package com.v5kf.mcss.entity;

import com.v5kf.mcss.ui.adapter.tree.TreeNodeId;
import com.v5kf.mcss.ui.adapter.tree.TreeNodeLabel;
import com.v5kf.mcss.ui.adapter.tree.TreeNodePid;
import com.v5kf.mcss.ui.adapter.tree.TreeNodeType;

/**
 * "organization"组织架构
 * @author Chenhy	
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-21 下午12:08:19
 * @package com.v5kf.mcss.entity of MCSS-Native
 * @file WorkerOrganization.java 
 *
 */
public class WorkerArch extends BaseBean {
	private long id; /*db*/
	/* type = organization */
	@TreeNodeLabel
	private String name;
	@TreeNodeId
	private long objId;								/* 组织id或者组id */
	@TreeNodePid
	private long parentId;							/* 组织父id */
	@TreeNodeType
	private String type;
	

	public WorkerArch(String name, long id, String type, WorkerArch parent) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.objId = id;
		this.type = type;
		if (parent != null) {
			this.parentId = parent.getObjId();
		}
	}

	public WorkerArch(String name, long id, String type, long parentId) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.objId = id;
		this.type = type;
		this.parentId = parentId;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public long getObjId() {
		return objId;
	}


	public void setObjId(long objId) {
		this.objId = objId;
	}


	public long getParentId() {
		return parentId;
	}


	public void setParentId(long parentId) {
		this.parentId = parentId;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
}
