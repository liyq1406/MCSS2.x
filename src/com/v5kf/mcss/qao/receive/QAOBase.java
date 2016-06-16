package com.v5kf.mcss.qao.receive;

import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.Config.AppStatus;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.qao.request.WorkerRequest;
import com.v5kf.mcss.service.CoreService;
import com.v5kf.mcss.utils.Logger;

/**
 * 接收数据模型处理-基类
 * @author Chenhy
 *
 */
public abstract class QAOBase {
	protected Context mContext;
	protected Handler mHandler;
	protected AppInfoKeeper mAppInfo;
	protected CustomApplication mApplication;

	protected JSONObject qao_data;
	protected String o_type;
	protected String o_method;
	protected int o_error;
	protected String o_errmsg;
	
	public QAOBase(JSONObject json, Context context, Handler handler) {
		this.mContext = context;
		this.mHandler = handler;
		this.qao_data = json;
		this.o_error = json.optInt(QAODefine.O_ERROR);
		this.o_errmsg = json.optString(QAODefine.O_ERRMSG);
		this.mApplication = CustomApplication.getInstance();
		this.mAppInfo = CustomApplication.getAppInfoInstance();
	}
	
	/* json处理 */
	public abstract void process() throws JSONException;
	
	/**
	 * 错误处理:接收到o_error < 0时进行的处理
	 * @param errorHandle QAOBase 
	 * @return void
	 * @throws JSONException 
	 */
	protected abstract void errorHandle() throws JSONException;
	
	/**
	 * 显示Toast提示信息
	 * @param showToast QAOBase 
	 * @return void
	 * @param resId
	 */
	protected void showToast(int resId) {
		showToast(mApplication.getString(resId));
	}
	
	/**
	 * 显示Toast提示信息
	 * @param showToast QAOBase 
	 * @return void
	 * @param text
	 */
	protected void showToast(String text) {
		Message msg = new Message();
		msg.what = CoreService.WHAT_SHOW_TOAST;
		Bundle data = new Bundle();
		data.putString(CoreService.MSG_KEY_TOAST_TEXT, text);
		msg.setData(data);
		mHandler.sendMessage(msg);
	}
	
	/**
	 * Websocket开启时进行主动请求
	 */
	protected void onWebsocketLogin() {
		Logger.i("QAOBase", "***--- onWebsocketLogin ---***");
		Config.USER_ID = mAppInfo.getUser().getW_id() + "of" + mAppInfo.getUser().getE_id();
		Config.SITE_ID = mAppInfo.getUser().getE_id();
		/*
		 * 初始请求内容：
		 * 1. 查询座席今天的历史会话	get_worker_session
		 * 2. 查询座席服务中的客户		get_customer_list
		 * 3. 查询等待中客户列表		get_waiting_customer
		 * 4. 查询座席信息			get_worker_info
		 *   
		 */
		try {
			// get_customer_list & get_waiting_customer
			CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, mContext);
			cReq.getCustomerList();
			cReq.getWaitingCustomer();
			
			// get_worker_info & get_arch_workers
			WorkerRequest wReq = (WorkerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WWRKR, mContext);
			wReq.getArchWorkers();
//			List<WorkerBean> users = DataSupport.findAll(WorkerBean.class);
//			if (users == null || users.isEmpty()) {
//				wReq.getWorkerInfo();
//			} else {
//				mAppInfo.setUser(users.get(0));
//				wReq.getWorkerStatus();
//			}
			wReq.getWorkerInfo();
			mApplication.setAppStatus(AppStatus.AppStatus_Loaded);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 发布事件
	 * @param obj
	 * @param tag
	 */
	protected void postEvent(Object obj, String tag) {
		Logger.d("QAOBase", "postEvent:" + tag);
		EventBus.getDefault().post(obj, tag);
	}
}
