package com.v5kf.mcss.config;

public class QAODefine {
	/* QAO Base */
	public static final String O_TYPE = "o_type";
	public static final String O_METHOD = "o_method";
	public static final String O_ERROR = "o_error";
	public static final String O_ERRMSG = "o_errmsg";
	
	/* QAO o_type */
	public static final String O_TYPE_MESSAGE = "message";
	public static final String O_TYPE_WLOGIN = "wservice_login";
	public static final String O_TYPE_WWRKR = "wservice_worker";
	public static final String O_TYPE_WCSTM = "wservice_customer";
	public static final String O_TYPE_WTICKET = "wservice_ticket";
	
	/* QAO Message_Dir */
	public static final int MSG_DIR_TO_CUSTOMER = 0;
	public static final int MSG_DIR_TO_WORKER = 1;
	public static final int MSG_DIR_FROM_ROBOT = 2;
	public static final int MSG_DIR_R2CW = 3; /* 机器人推荐回复 */
	public static final int MSG_DIR_FROM_WAITING = 4; /* 排队客户消息 */
	public static final int MSG_DIR_R2VC = 5; /* 有价值客户 */
	public static final int MSG_DIR_W2R = 6; /* 向机器人提问 */
	public static final int MSG_DIR_R2W = 7; /* 机器人回复 */
	public static final int MSG_DIR_RELATIVE_QUESTION = 8; /* 相关问题 */
	public static final int MSG_DIR_R2CQ = 9; /* 机器人发给客户的问卷 */
	public static final int MSG_DIR_R2WM = 10; /* 坐席监控客户与机器人会话 */
	
	/* QAO worker mode */
	public static final int MODE_SWITCH_ONLY = 0;
	public static final int MODE_AUTO = 1;
	
	/* QAO worker status */
	public static final short STATUS_OFFLINE = 0;	/* 离线 */
	public static final short STATUS_ONLINE = 1;	/* 在线 */
	public static final short STATUS_HIDE = 2;		/* 隐身 */
	public static final short STATUS_LEAVE = 3;		/* 离开 */
	public static final short STATUS_BUSY = 4;		/* 忙碌 */
	
	/* QAO customer interface */
	public static final int CSTM_IF_NULL = 0;
	public static final int CSTM_IF_WEIXIN = 1;
	public static final int CSTM_IF_YIXIN = 2;
	public static final int CSTM_IF_WEB = 3;
	public static final int CSTM_IF_OPEN = 4;
	public static final int CSTM_IF_WXQY = 5; // 企业号 WXQY
	public static final int CSTM_IF_SINA = 6; // 待定
	public static final int CSTM_IF_QQ = 9; // 待定
	public static final int CSTM_IF_ALIPAY = 7; // 支付宝平台
	public static final int CSTM_IF_APP = 8; // APP接口
	public static final int CSTM_IF_MAGIC = 11; // Magic接口（硬件接口）
	
	/* QAO wservice_login o_method */
	public static final String O_METHOD_LOGIN = "login";
	public static final String O_METHOD_LOGOUT = "logout";
	
	/* QAO Message */
	public static final String MSG_DIR = "msg_dir"; //
	public static final String C_ID = "c_id";
	public static final String S_ID = "s_id";
	public static final String M_ID = "m_id";
	public static final String W_ID = "w_id";
	public static final String U_ID = "u_id";
	public static final String F_ID = "f_id";
	public static final String P_ID = "p_id";
	public static final String CREATE_TIME = "create_time";
	public static final String FIRST_TIME = "first_time";
	public static final String LAST_TIME = "last_time";
	public static final String CANDIDATE = "candidate";	/* 候选者 */
	// get_messages
	public static final String MESSAGE_ID = "message_id";
	public static final String DIRECTION = "direction";
	public static final String MESSAGE_TYPE = "message_type";
	
	/* QAO Message_Content */
	public static final String MSG_TYPE = "message_type";
	public static final String MSG_CONTENT = "content";
	public static final String MSG_MEDIA_ID = "media_id";
	public static final String MSG_RECOGNITION = "recognition";
	public static final String MSG_FORMAT = "format";
	public static final String MSG_THUMB_ID = "thumb_id";
	public static final String MSG_X = "x";
	public static final String MSG_Y = "y";
	public static final String MSG_SCALE = "scale";
	public static final String MSG_LABEL = "label";	
	public static final String MSG_TITLE = "title";
	public static final String MSG_PIC_URL = "pic_url";
	public static final String MSG_MUSIC_URL = "music_url";
	public static final String MSG_HQ_MUSIC_URL = "hq_music_url";
	public static final String MSG_URL = "url";
	public static final String MSG_TOKEN = "token";
	public static final String MSG_DESCRIPTION = "description";
	public static final String MSG_MULTI_ARTICLE = "articles";
	public static final String MSG_ARTICLES = "articles";
	public static final String MSG_ARTICLE = "article";
	public static final String MSG_CODE = "code";
	public static final String MSG_ARGC = "argc";
	public static final String MSG_ARGV = "argv";
	public static final String MSG_NAME = "name";
	public static final String MSG_PHONE = "phone";
	public static final String MSG_EMAIL = "email";
	public static final String MSG_QQ = "qq";
	public static final String MSG_TEXT = "text";
	public static final String MSG_SUMMARY = "summary";
	
	
	/* QAO Message_Type */
	public static final int MSG_TYPE_NULL = 0;
	public static final int MSG_TYPE_TEXT = 1;
	public static final int MSG_TYPE_IMAGE = 2;
	public static final int MSG_TYPE_LOCATION = 3;
	public static final int MSG_TYPE_LINK = 4;
	public static final int MSG_TYPE_EVENT = 5;
	public static final int MSG_TYPE_VOICE = 6;
	public static final int MSG_TYPE_VIDEO = 7;
	public static final int MSG_TYPE_SHORT_VIDEO = 8;
	public static final int MSG_TYPE_NEWS = 9; /* 单图文、多图文 统一到此类型 */
	public static final int MSG_TYPE_MUSIC = 10;	
	public static final int MSG_TYPE_WXCS = 11;	/* 切换到多客服 */
	public static final int MSG_TYPE_RES = 20;
	public static final int MSG_TYPE_VS = 21;
	public static final int MSG_TYPE_APP_URL = 22;	/* 第三方URL */
	public static final int MSG_TYPE_COMMENT = 23;	/* 评价: code: 0 1 2 3 */
	public static final int MSG_TYPE_NOTE = 24;		/* 留言 */
	public static final int MSG_TYPE_CONTROL = 25;	/* 转人工客服：code:1 */
//	public static final int MSG_TYPE_ARTICLE = 13;
//	public static final int MSG_TYPE_CMD = 13;
//	public static final int MSG_TYPE_MULTI_ARTICLE = 14;
	
	
	/* QAO wservice_worker o_method */
	public static final String O_METHOD_GET_WORKER_INFO = "get_worker_info";
	public static final String O_METHOD_SET_WORKER_INFO = "set_worker_info";
	public static final String O_METHOD_GET_ARCH_WORKERS = "get_arch_workers";
	public static final String O_METHOD_GET_LIST_WORKERS = "get_list_workers";
	public static final String O_METHOD_GET_WORKER_MODE = "get_worker_mode";
	public static final String O_METHOD_SET_WORKER_MODE = "set_worker_mode";
	public static final String O_METHOD_GET_WORKER_STATUS = "get_worker_status";
	public static final String O_METHOD_SET_WORKER_STATUS = "set_worker_status";
	public static final String O_METHOD_WORKER_STATUS_PUSH = "worker_status_push";
	public static final String O_METHOD_SET_WORKER_MONITOR = "set_worker_monitor";
	public static final String O_METHOD_GET_WORKER_MONITOR = "get_worker_monitor";

	/* QAO wservice_customer o_method */
	public static final String CUSTOMER = "customer";
	public static final String CUSTOMERS = "customers";
	public static final String O_METHOD_GET_CUSTOMER_INFO = "get_customer_info";
	public static final String O_METHOD_SET_CUSTOMER_INFO = "set_customer_info";
	public static final String O_METHOD_GET_CUSTOMER_LIST = "get_customer_list";
	public static final String O_METHOD_GET_WAITING_CUSTOMER = "get_waiting_customer";
	public static final String O_METHOD_GET_WAITING_NUM = "get_waiting_num";
	public static final String O_METHOD_PICK_CUSTOMER = "pick_customer";
	public static final String O_METHOD_SWITCH_CUSTOMER = "switch_customer";
	public static final String O_METHOD_SET_IN_TRUST = "set_in_trust";
	public static final String O_METHOD_GET_IN_TRUST = "get_in_trust";
	public static final String O_METHOD_END_SESSION = "end_session";
	public static final String O_METHOD_CSTM_JOIN_IN = "customer_join_in";
	public static final String O_METHOD_CSTM_JOIN_OUT = "customer_join_out";
	public static final String O_METHOD_CSTM_WAIT_IN = "customer_waiting_in";
	public static final String O_METHOD_CSTM_WAIT_OUT = "customer_waiting_out";
	public static final String O_METHOD_GET_HISTORY_VISITOR_INFO = "get_history_visitor_info";
	public static final String O_METHOD_GET_HISTORY_VISITOR_REAL = "get_history_visitor_real";
	public static final String O_METHOD_GET_CUSTOMER_MESSAGES = "get_customer_messages";
	public static final String O_METHOD_GET_FAVOR_CUSTOMER = "get_favor_customer";
	public static final String O_METHOD_SET_FAVOR_CUSTOMER = "set_favor_customer";
	public static final String O_METHOD_UPDATE_CSTM_INFO = "update_customer_info";
	public static final String O_METHOD_CSTM_ACCESSABLE_CHANGE = "customer_accessable_change";
	public static final String O_METHOD_CSTM_MONITOR_OUT = "customer_monitor_out";
//	public static final String O_METHOD_CSTM_MONITOR_IN = "customer_monitor_IN";
	
	/* QAO wservice_ticket o_method */
	public static final String O_METHOD_GET_CUSTOMER_SESSION = "get_customer_session";
	public static final String O_METHOD_GET_HISTORICAL_CUSTOMER = "get_historical_customer";
	public static final String O_METHOD_GET_WORKER_SESSION = "get_worker_session";
	public static final String O_METHOD_GET_MESSAGES = "get_messages";
	
	/* QAO wservice_ticket get session */
	public static final String CUSTOMER_ID = "customer_id";
	public static final String SESSION_ID = "session_id";
	public static final String ACCOUNT_ID = "account_id";
	public static final String VISITOR_ID = "visitor_id"; // u_id
	public static final String WORKER_ID = "worker_id";
	public static final String CHANNEL = "channel";
	public static final String INTERFACE = "interface";
	public static final String SERVICE = "service";
	public static final String NICKNAME = "nickname";
	
	/* QAO wservice_customer */
	public static final String CSTM_REAL = "real";
	public static final String REAL_ID = "real_id";
	
	public static final String CSTM_VIRTUAL = "virtual";
	public static final String CSTM_CUSTOMER_TYPE = "customer_type";
	public static final String CSTM_CUSTOMER_TYPE_WEIXIN = "weixin";
	public static final String CSTM_CUSTOMER_TYPE_WEB = "web";
	public static final String CSTM_CUSTOMER_TYPE_WXQY = "wxqy"; // 企业号
	
	/* QAO worker config */
	public static final String WORKER_MODE = "mode";
	public static final String WORKER_CONNECTS = "connects";
	public static final String WORKER_ACCEPTS = "accepts";
	public static final String WORKER_STATUS = "status";
	
	/* QAO worker 坐席架构 */
	public static final String TYPE = "type"; // 组织架构中的类型
	public static final String ORGANIZATION = "organization";
	public static final String GROUP = "group";
	public static final String WORKER = "worker";
	public static final String ID = "id";
	
	public static final String LIST = "list";
	
	/* Wservice Ticket get_historical_customer */
	public static final String ACCESSABLE_IDLE = "idle"; // 可以接入
	public static final String ACCESSABLE_JOIN = "join"; // 已经接入
	public static final String ACCESSABLE_AWAY = "away"; // 不可以接入
	
	
	/* ControlMessage code */
	public static final int CTR_CODE_1 = 1; // 转人工客服 可加参数argc=2,argv="组id 坐席id"
	public static final int CTR_CODE_2 = 2; // 转机器人
	
}
