package com.v5kf.mcss.utils;

import java.util.ArrayList;
import java.util.List;

import org.litepal.crud.DataSupport;

import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.entity.WorkerLogBean;

/**
 * 记录和查询坐席接日志，包括接入客户、会话结束
 * @author V5KF_Chyrain
 *
 */
public class WorkerLogUtil {
	private static final String TAG = "WorkerLogUtil";
	public static boolean ENABLE_LOG = false;
	
	public static void insertAppBackgroundLog() {
		if (!ENABLE_LOG) {
			return;
		}
		
		// [修改]不记录坐席退出应用记录
//		long time = DateUtil.getCurrentLongTime() / 1000;
//		String desc = DateUtil.longDateToString(time) + " [log] 退出应用，记录中断";
//		String e_id = Config.SITE_ID;
//		String w_id = CustomApplication.getAppInfoInstance().getUser().getW_id();
//		WorkerLogBean wlog = new WorkerLogBean();
//		wlog.setTime(time);
//		wlog.setDescription(desc);
//		wlog.setE_id(e_id);
//		wlog.setW_id(w_id);
//		wlog.setO_type("app_log");
//		wlog.setO_method("app_background");
//		wlog.save();
	}
	
	public static void insertAppForegroundLog() {
		if (!ENABLE_LOG) {
			return;
		}
		
		// [修改]不记录坐席退出应用记录
//		long time = DateUtil.getCurrentLongTime() / 1000;
//		String desc = DateUtil.longDateToString(time) + " [log] 启动应用，记录开始";
//		String e_id = Config.SITE_ID;
//		String w_id = CustomApplication.getAppInfoInstance().getUser().getW_id();
//		WorkerLogBean wlog = new WorkerLogBean();
//		wlog.setTime(time);
//		wlog.setDescription(desc);
//		wlog.setE_id(e_id);
//		wlog.setW_id(w_id);
//		wlog.setO_type("app_log");
//		wlog.setO_method("app_foreground");
//		wlog.save();
	}
	
	public static void insertCustomerJoinInLog(CustomerBean customer, int reason) {
		if (!ENABLE_LOG) {
			return;
		}
		
		if (null == customer) {
			Logger.e(TAG, "insertCustomerJoinInLog -> null customer");
			return;
		}
		String e_id = Config.SITE_ID;
		String w_id = CustomApplication.getAppInfoInstance().getUser().getW_id();
		String c_id = customer.getC_id();
		String s_id = customer.getSession() != null ? customer.getSession().getS_id() : customer.getS_id();
		String o_type = QAODefine.O_TYPE_WCSTM;
		String o_method = QAODefine.O_METHOD_CSTM_JOIN_IN;
		long time = DateUtil.getCurrentLongTime() / 1000;
		String ifaceStr = customer.getIfaceString();
		String desc = DateUtil.longDateToString(time) + " [" + ifaceStr + "] 客户\"" + customer.getDefaultName() + "\" 接入";
		WorkerLogBean wlog = new WorkerLogBean(time, e_id, w_id, c_id, s_id, o_type, o_method, 0, desc);
		wlog.save();
	}
	
	public static void insertCustomerJoinOutLog(CustomerBean customer, int reason) {
		if (!ENABLE_LOG) {
			return;
		}
		
		if (null == customer) {
			Logger.e(TAG, "insertCustomerJoinOutLog -> null customer");
			return;
		}
		// e_id, w_id, c_id, s_id, o_type, o_method, reason
		String e_id = Config.SITE_ID;
		String w_id = CustomApplication.getAppInfoInstance().getUser().getW_id();
		String c_id = customer.getC_id();
		String s_id = customer.getSession() != null ? customer.getSession().getS_id() : customer.getS_id();
		String o_type = QAODefine.O_TYPE_WCSTM;
		String o_method = QAODefine.O_METHOD_CSTM_JOIN_OUT;
		long time = DateUtil.getCurrentLongTime() / 1000;
		String ifaceStr = customer.getIfaceString();
		String desc = DateUtil.longDateToString(time) + " [" + ifaceStr + "] 客户\"" + customer.getDefaultName() + "\" 会话结束：" + UITools.stringOfEndReason(reason);
		WorkerLogBean wlog = new WorkerLogBean(time, e_id, w_id, c_id, s_id, o_type, o_method, reason, desc);
		wlog.save();
	}
	
//	public static void insertLog(long time, CustomerBean customer, String o_type, String o_method, int reason, String desc) {
//		// TODO e_id, w_id, c_id, s_id, o_type, o_method, 
//		
//	}
	
	/**
	 * 查询坐席日志
	 * @param offset
	 * @param size
	 * @return
	 */
	public static List<WorkerLogBean> queryLogs(int offset, int size) {
		String e_id = Config.SITE_ID;
		String w_id = CustomApplication.getAppInfoInstance().getUser().getW_id();
		List<WorkerLogBean> logs = DataSupport.where("e_id = ? and w_id = ?", e_id, w_id).order("time desc").offset(offset).limit(size).find(WorkerLogBean.class);
		return logs;
	}

	/**
	 * 查询坐席日志
	 * @param list
	 * @param offset
	 * @param size
	 * @return finish
	 */
	public static boolean queryLogs(List<WorkerLogBean> list, int offset, int size) {
		if (list == null) {
			list = new ArrayList<WorkerLogBean>();
		}
		String e_id = Config.SITE_ID;
		String w_id = CustomApplication.getAppInfoInstance().getUser().getW_id();
		List<WorkerLogBean> logs = DataSupport.where("e_id = ? and w_id = ?", e_id, w_id).order("time desc").offset(offset).limit(size).find(WorkerLogBean.class);
		list.addAll(logs);
		return logs.size() < size;
	}
}
