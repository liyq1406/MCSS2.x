package com.v5kf.mcss.qao.request;

import org.json.JSONException;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;
import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.WorkerBean;
import com.v5kf.mcss.utils.Logger;


public class WorkerRequest extends BaseRequest {
	
	public WorkerRequest(String o_type, Context handler) throws JSONException {
		// TODO Auto-generated constructor stub
		super(handler);
		mRequestJson.put(QAODefine.O_TYPE, o_type);
	}
	
	public void setWorkerInfo(WorkerBean worker) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_SET_WORKER_INFO);
		/**/
		worker.addPropertyToJSON(mRequestJson);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_SET_WORKER_INFO");
	}
	
	public void getWorkerInfo() throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_WORKER_INFO);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_WORKER_INFO");
	}

	public void getArchWorkers() throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_ARCH_WORKERS);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_ARCH_WORKERS");
	}
	
	public void getListWorkers() throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_LIST_WORKERS);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_LIST_WORKERS");
	}
	
	public void getWorkerMode() throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_WORKER_MODE);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_WORKER_MODE");
	}
	
	public void setWorkerMode(int mode, int num) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_SET_WORKER_MODE);
		if (QAODefine.MODE_AUTO == mode) {
			mRequestJson.put("accepts", num);
		} else if (QAODefine.MODE_SWITCH_ONLY == mode) {
			mRequestJson.put("connects", num);
		}
		mRequestJson.put("mode", mode);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_SET_WORKER_MODE");
	}
	
	public void getWorkerStatus() throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_WORKER_STATUS);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_GET_WORKER_STATUS");
	}
	
	public void setWorkerStatus(int status) throws JSONException {
		clear();
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_SET_WORKER_STATUS);
		mRequestJson.put("status", status);
		sendRequest(mRequestJson.toString());
		MobclickAgent.onEvent(mContext,"REQ_SET_WORKER_STATUS");
	}
	
	public void setWorkerMonitor(int monitor) throws JSONException {
		clear();
		CustomApplication.getAppInfoInstance().getUser().setMonitor(monitor != 0);
		if (monitor == 0) {
			CustomApplication.getAppInfoInstance().clearMonitorMap();
		}
		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_SET_WORKER_MONITOR);
		mRequestJson.put("monitor", monitor);
		sendRequest(mRequestJson.toString());
		Logger.w("WorkerRequest", "setWorkerMonitor:" + monitor + " " + CustomApplication.getAppInfoInstance().getUser().isMonitor());
		MobclickAgent.onEvent(mContext,"REQ_SET_WORKER_MONITOR");
	}
	
//	public void getWorkerMonitor() throws JSONException {
//		clear();
//		mRequestJson.put(QAODefine.O_METHOD, QAODefine.O_METHOD_GET_WORKER_MONITOR);
//		sendRequest(mRequestJson.toString());
//		MobclickAgent.onEvent(mContext,"REQ_GET_WORKER_MONITOR");
//	}
	
	@Override
	public String toString() {
		return mRequestJson.toString();
	}

	private void clear() {
		mRequestJson.remove("status");
		mRequestJson.remove("mode");
		mRequestJson.remove("connects");
		mRequestJson.remove("accepts");
		mRequestJson.remove("connects");
		mRequestJson.remove("monitor");
	}

}
