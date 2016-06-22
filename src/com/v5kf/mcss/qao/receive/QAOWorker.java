package com.v5kf.mcss.qao.receive;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import android.content.Context;
import android.os.Handler;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.ArchWorkerBean;
import com.v5kf.mcss.entity.WorkerArch;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.WorkerRequest;
import com.v5kf.mcss.utils.Logger;

public class QAOWorker extends QAOBase {
	private static final String TAG = "QAOWorker";
	
	public QAOWorker(JSONObject jsonMsg, Context context, Handler mHandler) throws JSONException {
		super(jsonMsg, context, mHandler);
		this.o_type = QAODefine.O_TYPE_WWRKR;
		this.o_method = jsonMsg.getString(QAODefine.O_METHOD);
	}

	@Override
	public void process() throws NumberFormatException, JSONException  {
		if (o_error != 0) {
			Logger.e(TAG, o_method + ".o_errmsg: " + o_errmsg);
			errorHandle();
			return;
		}
		switch (o_method) {
		case QAODefine.O_METHOD_GET_WORKER_INFO: {
			mAppInfo.getUser().parse(qao_data);
			if (mAppInfo.getUser().getPhoto() != null && !mAppInfo.getUser().getPhoto().isEmpty()) {
				mApplication.getWorkerSp().saveWorkerPhoto(mAppInfo.getUser().getPhoto());
			}
//			List<WorkerBean> users = DataSupport.findAll(WorkerBean.class);
//			if (users == null || users.isEmpty()) {
//				mAppInfo.getUser().save();
//			} else {
//				users.get(0).delete();
//				mAppInfo.getUser().save();
//			}
			// [更新界面][eventbus]
			postEvent(mAppInfo.getUser(), EventTag.ETAG_UPDATE_USER_INFO);
			break;
		}
		
		case QAODefine.O_METHOD_SET_WORKER_INFO:
			// 失败时处理
			break;
			
		case QAODefine.O_METHOD_GET_ARCH_WORKERS:
			parseGetArchWorkers();
			break;
			
		case QAODefine.O_METHOD_GET_LIST_WORKERS:
			parseGetListWorkers();			
			break;
			
		case QAODefine.O_METHOD_GET_WORKER_MODE: {
			mAppInfo.getUser().setStatus((short) qao_data.optInt(QAODefine.WORKER_STATUS));
			mAppInfo.getUser().setMode((short) qao_data.optInt(QAODefine.WORKER_MODE));
			mAppInfo.getUser().setConnects(qao_data.optInt(QAODefine.WORKER_CONNECTS));
			mAppInfo.getUser().setAccepts(qao_data.optInt(QAODefine.WORKER_ACCEPTS));
			// [更新界面][eventbus]
			postEvent(mAppInfo, EventTag.ETAG_ARCH_WORKER_CHANGE);
			postEvent(mAppInfo.getUser(), EventTag.ETAG_UPDATE_USER_INFO);
			break;
		}
		
		case QAODefine.O_METHOD_SET_WORKER_MODE: {
			// 成功设置模式
			break;
		}
		
		case QAODefine.O_METHOD_GET_WORKER_STATUS: {
			mAppInfo.getUser().setStatus((short) qao_data.optInt(QAODefine.WORKER_STATUS));
			mAppInfo.getUser().setMode((short) qao_data.optInt(QAODefine.WORKER_MODE));
			mAppInfo.getUser().setConnects(qao_data.optInt(QAODefine.WORKER_CONNECTS));
			mAppInfo.getUser().setAccepts(qao_data.optInt(QAODefine.WORKER_ACCEPTS));
			// [更新界面][eventbus]
			postEvent(mAppInfo, EventTag.ETAG_ARCH_WORKER_CHANGE);
			postEvent(mAppInfo.getUser(), EventTag.ETAG_UPDATE_USER_INFO);
			break;
		}
		
		case QAODefine.O_METHOD_SET_WORKER_STATUS: {
			// 成功设置状态
			break;
		}
		
		case QAODefine.O_METHOD_WORKER_STATUS_PUSH:
			parseWorkerStatusPush();
			break;
		case QAODefine.O_METHOD_SET_WORKER_MONITOR:
			postEvent(mAppInfo.getUser(), EventTag.ETAG_MONITOR_STATE_CHANGE);
			break;
//		case QAODefine.O_METHOD_GET_WORKER_MONITOR:
//			mAppInfo.getUser().setMonitor(qao_data.getBoolean("monitor"));
//			postEvent(mAppInfo, EventTag.ETAG_MONITOR_STATE_CHANGE);
//			break;
		
		default:
			throw new JSONException("Unknow o_method:" + o_method + " of o_type:" + o_type);
		}
		
	}

	private void parseWorkerStatusPush() throws NumberFormatException, JSONException {
		String w_id = qao_data.getString(QAODefine.W_ID);
		int status = qao_data.getInt(QAODefine.WORKER_STATUS);
		if (Config.USE_DB) {
			List<ArchWorkerBean> list = DataSupport.where("w_id = ?", w_id).find(ArchWorkerBean.class);
			if (list != null && !list.isEmpty()) {
				ArchWorkerBean coWorker = new ArchWorkerBean();
				if (status == 0) {
					coWorker.setToDefault("status");
					coWorker.updateAll("w_id = ?", w_id);
				} else {
					coWorker.setStatus((short)status);
					coWorker.updateAll("w_id = ?", w_id);
				}
				// [更新界面][eventbus]
				postEvent(mAppInfo, EventTag.ETAG_ARCH_WORKER_CHANGE);
				if (w_id.equals(mAppInfo.getUser().getW_id())) {
					postEvent(mAppInfo.getUser(), EventTag.ETAG_UPDATE_USER_INFO);
				}
			}
		} else {
			ArchWorkerBean coWorker = mAppInfo.getCoWorker(w_id);
			if (null != coWorker) {
				coWorker.setStatus((short) status);
				if (qao_data.has(QAODefine.WORKER_MODE)) {
					coWorker.setMode((short) qao_data.optInt(QAODefine.WORKER_MODE));
				}
				if (qao_data.has(QAODefine.WORKER_CONNECTS)) {
					coWorker.setConnects(qao_data.optInt(QAODefine.WORKER_CONNECTS));
				}
				if (qao_data.has(QAODefine.WORKER_ACCEPTS)) {
					coWorker.setAccepts(qao_data.optInt(QAODefine.WORKER_ACCEPTS));
				}
				// [更新界面][eventbus]
				postEvent(mAppInfo, EventTag.ETAG_ARCH_WORKER_CHANGE);
				if (w_id.equals(mAppInfo.getUser().getW_id())) {
					postEvent(mAppInfo.getUser(), EventTag.ETAG_UPDATE_USER_INFO);
				}
			} else {/* 坐席不在列表中，请求坐席列表数据 */
				if (w_id.equals(mAppInfo.getUser().getW_id())) {
					postEvent(mAppInfo.getUser(), EventTag.ETAG_UPDATE_USER_INFO);
				}
//				try {
//					WorkerRequest wReq = (WorkerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WWRKR, mContext);
//					wReq.getArchWorkers();
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
			}
		}
	}

	private void parseGetArchWorkers() throws NumberFormatException, JSONException {
		JSONObject workers = qao_data.getJSONObject("workers");
		if (workers.has(QAODefine.LIST)) {
			if (Config.USE_DB) { /*db*/
				if (DataSupport.count(WorkerArch.class) > 0) {
					DataSupport.deleteAll(WorkerArch.class);
					DataSupport.deleteAll(ArchWorkerBean.class);
					Logger.d(TAG, "[WorkerArch] delete CoWorker");
				}
			} else {
				mAppInfo.clearWorkerArchs();
				mAppInfo.clearWorkerMap();
			}
			parseArch(workers.getJSONArray(QAODefine.LIST), null); /*db*/
			if (Config.USE_DB) {
				DataSupport.saveAll(mAppInfo.getWorkerArchs());
				DataSupport.saveAll(mAppInfo.getWorkerMap().values());
				mAppInfo.clearWorkerArchs();
				mAppInfo.clearWorkerMap();
			}
		}
		// [更新界面][eventbus]
		postEvent(mAppInfo, EventTag.ETAG_ARCH_WORKER_CHANGE);
	}

	/* 数据库相关 */
	private void parseArch(JSONArray list, WorkerArch parent) throws JSONException {
		if (list != null && list.length() > 0) {
			for (int i = 0; i < list.length(); i++) {
				JSONObject item = list.getJSONObject(i);
				String type = item.getString("type");
				if (type.equals("organization")) {
					long org_id = item.getLong("id");
					String name = item.getString("name");
					WorkerArch arch = new WorkerArch(name, org_id, type, parent);
					mAppInfo.addWorkerArch(arch);
					parseArch(item.optJSONArray(QAODefine.LIST), arch);
				} else if (type.equals("group")) {
					long org_id = item.getLong("id");
					String name = item.getString("name");
					WorkerArch arch = new WorkerArch(name, org_id, type, parent);
					mAppInfo.addWorkerArch(arch);
					parseWorkerArch(item, arch); // 获得Worker_List
				}
			} /* end of for */
		}
	}

	/* worker列表 */
	private void parseWorkerArch(JSONObject json, WorkerArch worker_org) throws JSONException, NumberFormatException {
		JSONArray workers = json.optJSONArray(QAODefine.LIST);
		if (null != workers && workers.length() > 0) {
			for (int i = 0; i < workers.length(); i++) {
//				if (workers.getJSONObject(i).get("id").equals(mAppInfo.getUser().getW_id())) {
//					continue; // 联系人列表排除客服自己
//				}
				ArchWorkerBean worker = mAppInfo.addWorker(workers.getJSONObject(i));
				worker.setGroup_id(worker_org.getObjId());
				worker.setGroup_name(worker_org.getName());
				WorkerArch arch = new WorkerArch(worker.getDefaultName(), Long.parseLong(worker.getW_id()), "worker", worker_org);
				mAppInfo.addWorkerArch(arch);
			}
		}
	}

	private void parseGetListWorkers() throws NumberFormatException, JSONException {
		JSONObject workers = qao_data.getJSONObject("workers");
		if (null != workers && workers.getInt("workers") > 0) {
			JSONArray workerArray = workers.getJSONArray("worker");
			for (int i = 0; i < workerArray.length(); i++) {
				mAppInfo.addWorker(workerArray.getJSONObject(i));
			}
		}
	}

	@Override
	protected void errorHandle() throws JSONException {
		switch (o_method) {
		case QAODefine.O_METHOD_SET_WORKER_INFO: {
			// 提示设置信息失败，重新获得座席信息
			showToast(mContext.getString(R.string.err_set_worker_info) + ": " + o_errmsg);
			WorkerRequest wReq = (WorkerRequest) RequestManager.getRequest(o_type, mContext);
			wReq.getWorkerInfo();
		}
			break;
		case QAODefine.O_METHOD_SET_WORKER_MODE:
			showToast(mContext.getString(R.string.err_set_worker_mode) + ": " + o_errmsg);
			break;
		case QAODefine.O_METHOD_SET_WORKER_STATUS:
			showToast(mContext.getString(R.string.err_set_worker_status) + ": " + o_errmsg);
			break;
		case QAODefine.O_METHOD_SET_WORKER_MONITOR: {
//			WorkerRequest wReq = (WorkerRequest) RequestManager.getRequest(o_type, mContext);
//			wReq.getWorkerMonitor();
			showToast(o_type + "." + o_method + ": " + o_errmsg);
		}
			break;
		default:
			showToast(o_type + "." + o_method + ": " + o_errmsg);
			break;
		}
	}

}
