package com.v5kf.mcss.eventbus;


/**
 * 事件总线的
 * @author Chenhy
 *
 */
public class EventTag {
	
	public static final String ETAG_SEND_MSG = "send_message_tag"; // string
	public static final String ETAG_UPDATE_USER_INFO = "update_user_tag"; // worker|user
	public static final String ETAG_UPDATE_CSTM_INFO = "update_customer_tag"; // customer
//	public static final String ETAG_UPDATE_VISITOR_INFO = "update_visitor_tag"; // visitor
//	public static final String ETAG_UPDATE_WORKER_INFO = "update_worker_tag"; // COWorker
	public static final String ETAG_CSTM_OUT = "customer_out_tag"; // customer
	public static final String ETAG_CSTM_WAIT_OUT = "customer_wait_out_tag"; // customer
	public static final String ETAG_SESSION_ARRAY_CHANGE = "session_array_change_tag"; // customer
	public static final String ETAG_MESSAGE_ARRAY_CHANGE = "message_array_change_tag"; // session
	public static final String ETAG_SERVING_CSTM_CHANGE = "serving_customer_change_tag"; // 服务中列表
	public static final String ETAG_WAITING_CSTM_CHANGE = "waiting_customer_change_tag"; // 排队列表
	public static final String ETAG_VISITORS_CHANGE = "visitors_change_tag"; // 访客列表
	public static final String ETAG_ARCH_WORKER_CHANGE = "arch_worker_change_tag"; // 坐席列表
	public static final String ETAG_PICK_CSTM_ERROR = "pick_customer_error_tag"; // 主动接入失败
//	public static final String ETAG_LOGIN_OK = "login_ok_tag"; // 登入成功
//	public static final String ETAG_LOGOUT_OK = "logout_ok_tag"; // 登出成功
	public static final String ETAG_ROBOT_ANSWER = "robot_answer_tag"; // 机器人应答
	public static final String ETAG_NEW_MESSAGE = "new_message_tag"; // 新消息
	public static final String ETAG_IN_TRUST_CHANGE = "intrust_change_tag"; // 新消息
	public static final String ETAG_ACCESSABLE_CHANGE = "cstm_accessable_change"; // 新消息
	
	public static final String ETAG_ON_LINE = "on_line_tag"; // 上线
	public static final String ETAG_OFF_LINE = "off_line_tag"; // 下线
	public static final String ETAG_RELOGIN = "re_login_tag"; // 需要重新登录
	public static final String ETAG_CONNECTION_CHANGE = "connection_change_tag"; // 连接状态改变 mClient
	public static final String ETAG_CONNECTION_START = "connection_start_tag"; // 开始连接
	
	public static final String ETAG_LOGIN_CHANGE = "login_change_tag"; // 登入事件
	public static final String ETAG_LOGOUT_CHANGE = "logout_change_tag"; // 登出事件
	
}
