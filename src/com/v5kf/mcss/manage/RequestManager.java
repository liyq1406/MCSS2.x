package com.v5kf.mcss.manage;

import org.json.JSONException;

import android.content.Context;

import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.qao.request.BaseRequest;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.qao.request.LoginRequest;
import com.v5kf.mcss.qao.request.MessageRequest;
import com.v5kf.mcss.qao.request.TicketRequest;
import com.v5kf.mcss.qao.request.WorkerRequest;

public class RequestManager {
		
	/* 请求参数：
	 * 	c_id
	 *  mode
	 *  status
	 *  
	 */

	public RequestManager() {
		// TODO Auto-generated constructor stub
	}
	
	public static BaseRequest getRequest(String o_type, Context context) throws JSONException {
		BaseRequest request = null;
		switch (o_type) {
		case QAODefine.O_TYPE_WWRKR:
			request = new WorkerRequest(o_type, context);
			break;
		case QAODefine.O_TYPE_WCSTM:
			request = new CustomerRequest(o_type, context);
			break;
		case QAODefine.O_TYPE_WLOGIN:
			request = new LoginRequest(o_type, context);
			break;
		case QAODefine.O_TYPE_WTICKET:
			request = new TicketRequest(o_type, context);
			break;
		case QAODefine.O_TYPE_MESSAGE:
			request = new MessageRequest(o_type, context);
			break;
		default:
			break;
		}
		
		return request;
	}
}
