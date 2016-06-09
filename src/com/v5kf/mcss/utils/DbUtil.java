package com.v5kf.mcss.utils;

import org.litepal.crud.DataSupport;

import android.content.Context;

import com.v5kf.mcss.entity.ArchWorkerBean;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.entity.CustomerRealBean;
import com.v5kf.mcss.entity.CustomerVirtualBean;
import com.v5kf.mcss.entity.MessageBean;
import com.v5kf.mcss.entity.SessionBean;
import com.v5kf.mcss.entity.WorkerArch;
import com.v5kf.mcss.entity.WorkerBean;
import com.v5kf.mcss.entity.WorkerLogBean;

public class DbUtil {

	public static void clearDb(Context context) {
		DataSupport.deleteAll(WorkerLogBean.class);
		DataSupport.deleteAll(ArchWorkerBean.class);
		DataSupport.deleteAll(WorkerArch.class);
		DataSupport.deleteAll(SessionBean.class);
		DataSupport.deleteAll(MessageBean.class);
		DataSupport.deleteAll(WorkerBean.class);
		DataSupport.deleteAll(CustomerBean.class);
		DataSupport.deleteAll(CustomerRealBean.class);
		DataSupport.deleteAll(CustomerVirtualBean.class);
	}
	
	public static void clearCache(Context context) {
		DataSupport.deleteAll(WorkerBean.class);
	}
	
	public static void clearTable(Class<?> modelClass) {
		DataSupport.deleteAll(modelClass);
	}
}
