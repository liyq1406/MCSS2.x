package com.v5kf.mcss.config;

import com.v5kf.mcss.utils.Logger;


/**
 * 
 *
 * @author Chenhy	
 * @email chenhy@v5kf.com
 * @version 2015-7-1 下午6:59:44
 * @package com.v5kf.mcss.config of MCSS-Native
 * @file Config.java 
 *
 */
public class Config {
	public static final boolean DEBUG_TOAST = false; // 是否显示Toast
	public static final boolean DEBUG = false; //BuildConfig.DEBUG; // 开发过程DEBUG模式，连接debug服务器
	public static final boolean USE_DB = false; // 是否使用数据库保存坐席
	
	public static final int VERSION = 3;
	public static boolean REQ_HTTPS = false; // 加密请求
	public static boolean WEB_HTTPS = false; // 网页使用https
	public static final boolean ENABLE_WORKER_LOG = true;
	public static final boolean ENABLE_UMENG_UPDATE = false; // 友盟自动更新
	public static final boolean ENABLE_CSTM_OFFLINE = true; // 显示客户离线
	public static final int LOG_LEVEL = Logger.VERBOS;
	public static final boolean USE_THUMBNAIL = true; // 使用缩略图
	public static final boolean ENABLE_PLAY_VEDIO_IN_LIST = false; // 是否允许对话列表播放视频
	public static final boolean SHOW_VID_OID = true; // 是否显示客户信息的v_id、o_id
	public static final String V5KF_COM = (WEB_HTTPS ? "https" : "http") + "://www.v5kf.com/public/website/page.html?sid=10000&id=14396&uid=[vs_user=wxkey]&first=1&ref=link&v5kf=mp.weixin.qq.com";
	/** 
	 * App Status 应用状态
	 * @author Chenhy
	 */
	public enum AppStatus {
		AppStatus_Init,			// 应用初始状态
		AppStatus_Loaded,		// 应用获取基础数据后
		AppStatus_Exit			// 应用退出状态
	}
	
	/* Login Status WS登录状态 */
	public enum LoginStatus {
		LoginStatus_Unlogin,		// 未登录状态
		LoginStatus_Logged,			// 登陆成功 ***
		LoginStatus_Logging,		// 登录中，等待login验证结果
		LoginStatus_LogErr,			// 账号认证AccountAuth->连接错误
		LoginStatus_LoginFailed,	// 登录验证失败
		LoginStatus_Logout,			// 登出成功
		LoginStatus_LogoutFailed,	// 登出失败
		LoginStatus_ForceLogout,	// 异地设备登录，强制登出
		LoginStatus_LoginLimit,		// 最大登录限制
		LoginStatus_AuthFailed		// Authorization失效
	}
	
	/**
	 * 退出关闭websocket连接和service的标识
	 * @author Chenhy
	 *
	 */
	public enum ExitFlag { 
		ExitFlag_None,		// 未退出
		ExitFlag_NeedLogin,	// 退出，需要重新登录
		ExitFlag_AutoLogin	// 退出，但无需重新登录，自动登录并连接
	}
	
	/**
	 * 需呀回到登录页重新登录的原因
	 * @author Chenhy
	 *
	 */
	public enum ReloginReason {
		ReloginReason_None,			// 无原因
		ReloginReason_AuthFailed,	// 应用初始状态
		ReloginReason_Code1000		// 同账号登录强制下线
	}
	
	/**
	 * 会话结束时间
	 * @author Chenhy
	 *
	 */
	public enum SessionEndReason {
		/**
		 * 0 - 原因不明
		 * 1 - 机器人指令
		 * 2 - 被转接
		 * 3 - 取消订阅
		 * 4 - 坐席主动结束
		 * 5 - 会话超时
		 * 6 - 微信多客服接管了
		 * 7 - 服务重启中断
		 */
	}
	
	/* 系统初始上线时间 */
	public static final int SYS_YEAR = 2015;
	public static final int SYS_MONTH = 9;
	public static final int SYS_DAY = 1;
	
	public static String WS_PROTOCOL = REQ_HTTPS ? "wss" : "ws";
	public static String WS_HOST = "chat.v5kf.com" + (DEBUG ? "/debug" : ""); // /debug
	public static final String AUTH_KEY = "1357924680";	// 统一密钥串
//	public static final String UPDATE_URL_XML = "http://chat.v5kf.com/app/download/version.xml"; // 检查更新配置文件地址
//	public static final String UPDATE_URL = "http://chat.v5kf.com/app/download/"; // 检查更新地址
	
	public static final String LOGIN_URL = "https://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/wkauth?"; // 登录账号验证url(新版)
	public static final String URL_FEED_BACK = (WEB_HTTPS ? "https" : "http") + "://www.v5kf.com/public/vdata/collect.html?sid=10000&fid=11742&ref=android"; // 用户反馈页面地址
	public static final String URL_ABOUT = (WEB_HTTPS ? "https" : "http") + "://www.v5kf.com/"; // 关于页面地址
	public static final String URL_SITEINFO_FMT = (REQ_HTTPS ? "https" : "http") + "://www.v5kf.com/public/api_dkf/get_chat_siteinfo?sid=";
	public static final String UPLOAD_TXT_URL = ""; // 崩溃日志文件上传url(已由友盟替代)
	
	/* 每次 media_id 延迟时间和重试最大次数 */
	public static final int GET_MEDIA_ID_DELAY = 1000; // 1s
	public static final int GET_MEDIA_ID_TIMES = 10; // 10次
	
	/* 万象优图认证和图片上传地址 */
	@Deprecated
	public static final String APP_PIC_AUTH_URL = (REQ_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/wxyt/worker?type=image&suffix=jpeg&auth=";
	@Deprecated
	public static final String APP_WECHAT_PIC_AUTH_URL_FMT = (REQ_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/wxyt/worker?exchange=wechat&type=image&suffix=jpeg&auth=%s&account=%s"; // 获取微信图片上传url
	@Deprecated
	public static final String APP_WXQY_PIC_AUTH_URL_FMT = (REQ_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/wxyt/worker?exchange=wxqy&type=image&suffix=jpeg&auth=%s&account=%s"; // 获取微信企业号图片上传url
	@Deprecated
	public static final String APP_GET_MEDIA_ID_URL_FMT = (REQ_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/mirror/%s/%s/media_id"; // 获取media_id(参数：/site_id/fileid)
	/* 图片缩略图：1.来自微信采用chat.v5kf.com的图片镜像；2.来自万象优图采用url + "/thumbnail" */
//	public static final String APP_PIC_V5_THUMBNAIL_FMT = (REQ_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/resource/%s/%s?w=350&h=350&q=50"; // 图片质量0-100
	public static final String APP_PIC_V5_THUMBNAIL_FMT = (REQ_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/resource/%s/%s/thumbnail"; // 图片质量0-100
	
	/* 腾讯对象服务-一般interface上传 */
	@Deprecated
	public static final String APP_MEDIA_AUTH_URL_FMT = (REQ_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/wxyt/worker?type=%s&auth=%s&suffix=%s";
	/* 腾讯对象服务-微信interface上传 */
	@Deprecated
	public static final String APP_WXQY_MEDIA_AUTH_URL_FMT = (REQ_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/wxyt/worker?exchange=wxqy&type=%s&auth=%s&suffix=%s&account=%s";
	@Deprecated
	public static final String APP_WECHAT_MEDIA_AUTH_URL_FMT = (REQ_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/wxyt/worker?exchange=wechat&type=%s&auth=%s&suffix=%s&account=%s";
	@Deprecated
	public static final String APP_MEDIA_UPLOAD_CB = (REQ_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/wxyt/object/uploadcb";
	
	/* 媒体文件上传统一接口 */
	public static final String APP_MEDIA_POST_FMT = (REQ_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/upload/%s/%s/%s/%s"; // /$account_id/$visitor_id/[web|app|wechat|wxqy|yixin]/$filename
	/* 媒体文件下载统一接口 */
	public static final String APP_RESOURCE_V5_FMT = (REQ_HTTPS ? "https" : "http") + "://chat.v5kf.com/" + (DEBUG ? "debug" : "public") + "/resource/%s/%s"; // site_id/message_id
	
	/* v5kf站点相关数据获取 */
	public static final String HOT_QUES_URL = (REQ_HTTPS ? "https" : "http") + "://www.v5kf.com/public/api_dkf/get_hot_ques?sid="; // 常见问答url
	/* 素材库资源 */
	public static final String GET_IMAGE_URL = (REQ_HTTPS ? "https" : "http") + "://www.v5kf.com/public/api_dkf/get_image_list?sid=%s&page=%d&list=%d";
	public static final String GET_NEWS_URL = (REQ_HTTPS ? "https" : "http") + "://www.v5kf.com/public/api_dkf/get_news_list?sid=%s&page=%d&list=%d";
	public static final String GET_MUSIC_URL = (REQ_HTTPS ? "https" : "http") + "://www.v5kf.com/public/api_dkf/get_music_list?sid=%s&page=%d&list=%d";
	
	/* 腾讯地图web service API */
	public static final String MAP_PIC_API_FORMAT = (REQ_HTTPS ? "https" : "http") + "://apis.map.qq.com/ws/staticmap/v2/?center=%f,%f&zoom=15&size=300*200&maptype=roadmap&markers=size:small|color:0x207CC4|label:V|%f,%f&key=4NABZ-63HAJ-GWQFM-FI2NH-3VDN2-NJFKG";
	public static final String MAP_WS_API_FORMAT = (REQ_HTTPS ? "https" : "http") + "://apis.map.qq.com/ws/geocoder/v1/?location=%f,%f&key=4NABZ-63HAJ-GWQFM-FI2NH-3VDN2-NJFKG&get_poi=1";
	
	public static String USER_ID = "10000_abc";
	public static String SITE_ID = "";
	public static String AUTH = "";
	
	
	/* 连接超时时间 */
	public static final int SOCKET_TIME_OUT = 20000; /* 20s */
	public static final int WS_TIME_OUT = 20000; /* 20s */
	public static final long SESSION_TIME_DETA = 300; // 显示日期的时间间隔(单位:秒)
	
	/* Websocket 连接参数 */
	public static final String WS_PATH = "/websocket";
	public static final String WS_DEV = "phone";
	
	/* Broadcast广播Action-接收广播 */
	public static final String ACTION_ON_MAINTAB = "com.v5kf.ws.maintabactivity";
	/* 发送广播 */
	public static final String ACTION_ON_WS_HEARTBEAT = "com.v5kf.mcss.service.CoreService.heartbeat";
	public static final String ACTION_ON_PUSH = "com.v5kf.push.service";		/* PUSH进程服务 */
	/* 更新广播 */
	public static final String ACTION_ON_UPDATE = "com.v5kf.manage.updateservice";	
	
	/* Intent ACTION_ON_UPDATE 的广播消息类别 */
	public static final int EXTRA_TYPE_UP_ENABLE = 1;			/* 有更新 */
	public static final int EXTRA_TYPE_UP_DOWNLOAD_FINISH = 2;	/* 下载完成 */
	public static final int EXTRA_TYPE_UP_DOWNLOAD = 3;			/* 允许下载 */
	public static final int EXTRA_TYPE_UP_INSTALL = 4; 			/* 允许安装 */
	public static final int EXTRA_TYPE_UP_NO_NEWVERSION = 5; 	/* 无更新 */
	public static final int EXTRA_TYPE_UP_FAILED = 6; 			/* 获取更新失败 */
	public static final int EXTRA_TYPE_UP_CANCEL = 7; 			/* 取消下载更新 */
	
	/* Notification通知ID,判断通知的类别 */
	public static final int NOTIFY_ID_NULL = 0;
	public static final int NOTIFY_ID_MESSAGE = 1;
	public static final int NOTIFY_ID_UPDATE = 2;
	public static final int NOTIFY_ID_WAIT_IN = 3;
	
	/* Action为DISCONNECT判断断开原因 */
	public static final String EXTRA_KEY_DISCONNECT_REASON = "disconnect_reason";
	public static final int DISCONNECT_REASON_NONE = 0;			/* 未退出 */
	public static final int DISCONNECT_REASON_FORCE_OUT = 1;	/* 异地登录强制退出 */
	public static final int DISCONNECT_REASON_NET_ERR = 2;		/* 网络异常 */
	public static final int DISCONNECT_REASON_WS_ERR = 3;		/* WS连接异常 */
	public static final int DISCONNECT_REASON_NO_INTERNET = 4;	/* WS连接异常 */
	
	/* Intent ACTION_ON_LOGIN传递的数据类别 */
	public static final int EXTRA_TYPE_LOGIN_SUCCESS = 1;		/* 登录成功 */
	public static final int EXTRA_TYPE_LOGIN_INVAL_CLIENT = 2;	/* 无效登录设备 */
	public static final int EXTRA_TYPE_LOGIN_LIMIT = 3;			/* 达到最大登录限制 */
	public static final int EXTRA_TYPE_LOGIN_INITING = 4;		/* 系统准备中 */
	public static final int EXTRA_TYPE_LOGIN_INVAL = 5;			/* 无效参数 */
	
	/* Intent ACTION_ON_PUSH消息类别 */
	public static final int EXTRA_TYPE_PUSH_STOP_WS = 1;		/* 停止pushservice的ws服务 */
	
	/* Intent ON_MAINTAB 传递的数据类别 */
	public static final int EXTRA_TYPE_NOTIFY = 2;			/* 接收通知栏响应 */	
	public static final int EXTRA_TYPE_ACTIVITY_START = 24;			// startActivityForResult相关
	public static final int EXTRA_TYPE_NULL = 0;
	public static final int EXTRA_TYPE_CLEAR_CLIENT_MSG = 37;	// 清空在线咨询消息 [保留]
	
	/* Intent 传递的Extra Key */
	public static final String EXTRA_KEY_INTENT_TYPE = "intent_type"; /* intent类别，每个携带Extra的intent必带此key */
	public static final String EXTRA_KEY_NOTIFY_TYPE = "notify_type";
	public static final int EXTRA_NOTIFY_TYPE_MESSAGE_SRV = 1;
	public static final int EXTRA_NOTIFY_TYPE_MESSAGE_WAIT = 2;
	public static final int EXTRA_NOTIFY_TYPE_JOIN_IN = 3;
	public static final int EXTRA_NOTIFY_TYPE_WAIT_IN = 4;
	public static final String EXTRA_KEY_NOTIFY_ID = "notify_id";
	public static final String EXTRA_KEY_SESSION_ID = "session_id";
	public static final String EXTRA_KEY_MSG_WRITE = "msg_write_string";
	public static final String EXTRA_KEY_W_ID = "w_id";
	public static final String EXTRA_KEY_C_ID = "c_id";
	public static final String EXTRA_KEY_V_ID = "v_id";
	public static final String EXTRA_KEY_S_ID = "s_id";
	public static final String EXTRA_KEY_STATUS = "status";
	public static final String EXTRA_KEY_SERVICE = "service";
	public static final String EXTRA_KEY_DOWN_ONLYWIFI = "only_wifi";
	
	
	/* 启动Activity和返回数据相关 */
	public static final int REQUEST_CODE_SWITCH_CSTM = 1;
	public static final int REQUEST_CODE_SERVING_SESSION_FRAGMENT = 1;
	public static final int REQUEST_CODE_WAITING_SESSION_FRAGMENT = 2;
	public static final int REQUEST_CODE_WORKER_CONTACT_FRAGMENT = 3;
	public static final int REQUEST_CODE_CUSTOMER_CONTACT_FRAGMENT = 4;
	public static final int REQUEST_CODE_FIND_FRAGMENT = 5;
	public static final int REQUEST_CODE_MINE_FRAGMENT = 6;
	public static final int REQUEST_CODE_CUSTOMER_INFO = 7;
	public static final int REQUEST_CODE_GET_LOCATION = 8;
	public static final int REQUEST_CODE_FORWARD_MSG = 9;
	public static final int REQUEST_CODE_ROBOT_CHAT = 10;
	public static final int REQUEST_CODE_MATERIAL = 11;
	public static final int REQUEST_CODE_HISTORY_VISITOR = 12;
	public static final int REQUEST_CODE_PHOTO = 13; // 图库选择图片
	public static final int REQUEST_CODE_PHOTO_KITKAT = 14; // 图库选择图片
	public static final int REQUEST_CODE_CAMERA = 15; // 拍照返回图片
	public static final int REQUEST_CODE_CROP = 16; // 裁剪返回图片
	public static final int REQUEST_CODE_PHOTO_PREV = 17; // 预览图片
	/* 权限申请返回码 */
	public static final int REQUEST_PERMISSION_CAMERA = 101; // 拍照权限
	public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 102; // 存储权限
	public static final int REQUEST_PERMISSION_RECORD_AUDIO = 103; // 录音权限
	public static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 104; // GPS位置权限
	public static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 105; // 网络位置权限
	public static final int REQUEST_PERMISSION_ALL = 100; // 全部权限

	public static final int RESULT_CODE_NULL = 0;
	public static final int RESULT_CODE_WORKER_INFO_CHANGE = 1;
	public static final int RESULT_CODE_CHAT_CONTENT_ADD = 2;
	public static final int RESULT_CODE_CHAT_CONTENT_NOCHANGE = 3;
	public static final int RESULT_CODE_CHAT_CHG_STICK = 4;
	public static final int RESULT_CODE_SWITCH_OK = 5;
	public static final int RESULT_CODE_SWITCH_FAIL = 6;
	public static final int RESULT_CODE_STATUS_SET = 7;
	public static final int RESULT_CODE_GET_LOCATION = 8;
	public static final int RESULT_CODE_END_SESSION = 9;
	public static final int RESULT_CODE_FORWARD_MSG = 10;
	public static final int RESULT_CODE_PICKUP_CSTM = 11;
	public static final int RESULT_CODE_ROBOT_CHAT = 12;
	public static final int RESULT_CODE_MATERIAL_SEND = 13;
	
	public static final String SERVICE_NAME_CS = "com.v5kf.mcss.service.CoreService";
	public static final String SERVICE_NAME_PUSH = "com.v5kf.mcss.service.PushService";
	public static final String ACTION_PUSH = "com.v5kf.mcss.service.push";
	public static final String ACTION_CORE = "com.v5kf.mcss.service.core";
	
	
	/**
	 * 备注：JSONObject optLong出现解析数值错误，改用Long.parseLong(JSON.optString(xxx))则正常
	 */
	
	public static final int LIST_MODE_NORMAL = 1;
	public static final int LIST_MODE_SWITCH = 2;
}
