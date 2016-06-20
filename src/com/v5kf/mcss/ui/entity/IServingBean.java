package com.v5kf.mcss.ui.entity;

import com.v5kf.mcss.entity.CustomerBean;

import bupt.freeshare.swipelayoutlibrary.IRecyclerBean;

public class IServingBean extends IRecyclerBean {

	private CustomerBean mCustomer;
	
	public IServingBean(CustomerBean cstm) {
		mCustomer = cstm;
	}

	public CustomerBean getCustomer() {
		return mCustomer;
	}

	public void setCustomer(CustomerBean mCustomer) {
		this.mCustomer = mCustomer;
	}
}
