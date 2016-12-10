package com.v5kf.mcss.service;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.v5kf.client.lib.V5HttpUtil;
import com.v5kf.client.lib.V5KFException.V5ExceptionStatus;
import com.v5kf.client.lib.V5Util;
import com.v5kf.client.lib.callback.HttpResponseHandler;
import com.v5kf.client.lib.callback.MessageSendCallback;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5VoiceMessage;
import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.MessageRequest;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.cache.MediaLoader;

public class MessageSendHelperMd2x {
	private static final String TAG = "MessageSendHelperMd2x";
	private Context mContext;
	private Handler mHandler;
	private CustomerBean mCustomer;
	private CustomApplication mApplication;
	
	public MessageSendHelperMd2x(CustomerBean cstm, Context context, Handler handler) {
		this.mContext = context;
		this.mHandler = handler;
		this.mCustomer = cstm;
		this.mApplication = CustomApplication.getInstance();
	}
	
	public void sendMessage(V5Message message, MessageSendCallback callback) {
		switch (message.getMessage_type()) {
		case QAODefine.MSG_TYPE_IMAGE:
			sendImageMessage((V5ImageMessage)message, callback);
			break;
			
		case QAODefine.MSG_TYPE_VOICE:
			sendVoiceMessage((V5VoiceMessage)message, callback);
			break;
			
		case QAODefine.MSG_TYPE_TEXT:
		default:
			sendMessageRequest(message, callback);
			break;
		}
	}
	
	public void sendImageMessage(final V5ImageMessage imageMessage, final MessageSendCallback callback) {
		imageMessage.setState(V5Message.STATE_SENDING);
		
		// 判断是否本地图片
		if (imageMessage.getFilePath() != null) {
			if (imageMessage.getPic_url() == null) { // 上传图片前
				// 图片格式验证
				String type = V5Util.getImageMimeType(imageMessage.getFilePath());
				imageMessage.setFormat(type);
				if (!V5Util.isValidImageMimeType(type)) {
					sendFailedHandle(
							callback, 
							imageMessage, 
							V5ExceptionStatus.ExceptionMessageSendFailed, 
							"Unsupport image mimetype");
				} else {
					postMedia(imageMessage, mCustomer.getDefaultAccountId(), mCustomer.getVisitor_id(), mCustomer.getIfaceString(), imageMessage.getFilePath(), callback);
				}
			} else { // 上传图片后
				// 发送消息请求
				sendMessageRequest(imageMessage, callback);
			}
		} else if (imageMessage.getPic_url() != null) {
			// 发送消息请求
			sendMessageRequest(imageMessage, callback);
		} else {
			sendFailedHandle(
					callback, 
					imageMessage, 
					V5ExceptionStatus.ExceptionMessageSendFailed, 
					"Empty voice message");
		}
	}
	
	public void sendVoiceMessage(final V5VoiceMessage voiceMessage, final MessageSendCallback callback) {
		voiceMessage.setState(V5Message.STATE_SENDING);
		
		// 判断是否本地语音
		if (voiceMessage.getFilePath() != null) {
			if (!voiceMessage.isUpload() || voiceMessage.getUrl() == null) { // 上传语音前
				postMedia(voiceMessage, mCustomer.getDefaultAccountId(), mCustomer.getVisitor_id(), mCustomer.getIfaceString(), voiceMessage.getFilePath(), callback);
			} else { // 上传语音后
				// 发送消息请求
				Logger.i(TAG, "sendVoiceMessage -> sendMessage " + voiceMessage.getState());
				sendMessageRequest(voiceMessage, callback);
			}
		} else if (voiceMessage.getUrl() != null) {
			// 发送消息请求
			sendMessageRequest(voiceMessage, callback);
		} else {
			sendFailedHandle(
					callback, 
					voiceMessage, 
					V5ExceptionStatus.ExceptionMessageSendFailed, 
					"Empty voice message");
		}
	}

	/**
	 * Message消息请求发送
	 * @param msg
	 * 
	 */
	protected void sendMessageRequest(V5Message msg, MessageSendCallback callback) {
		msg.setState(V5Message.STATE_SENDING);
		try {
			/* 发送Message请求 */
			MessageRequest mReq = (MessageRequest) RequestManager.getRequest(QAODefine.O_TYPE_MESSAGE, mContext);
			mReq.sendMessage(msg);
			if (CoreService.isConnected()) {
				msg.setState(V5Message.STATE_ARRIVED);
				if (callback != null) {
					callback.onSuccess(msg);
				}
			} else {
				msg.setState(V5Message.STATE_FAILURE);
				sendFailedHandle(
						callback, 
						msg, 
						V5ExceptionStatus.ExceptionMessageSendFailed, 
						"Connection closed");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			msg.setState(V5Message.STATE_FAILURE);
			sendFailedHandle(
					callback, 
					msg, 
					V5ExceptionStatus.ExceptionMessageSendFailed, 
					"JSONException:" + e.getMessage());
		}
	}
	
	/**
	 * 上传媒体资源（一步完成）
	 * /$account_id/$visitor_id/[web|app|wechat|wxqy|yixin]/$filename
	 * @param message
	 * @param file
	 */
	private void postMedia(final V5Message message, String a_id, String v_id, String ifaceStr, String filePath, final MessageSendCallback handler) {
		final File file = new File(filePath);
		String url = String.format(Config.APP_MEDIA_POST_FMT, a_id, v_id, ifaceStr, file.getName());
		String auth = mApplication.getWorkerSp().readAuthorization();
		Logger.d(TAG, "[postMedia] url:" + url);
		V5HttpUtil.postFile(message, file, url, auth, new HttpResponseHandler(mApplication) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				// 解析地址
				Logger.i(TAG, "[postMedia] success responseString:" + responseString);
				if (statusCode == 200) {
					try {
						JSONObject data = new JSONObject(responseString);
						String url = data.optString("url");
						String media_id = data.optString("media_id");
						if (!TextUtils.isEmpty(url)) {
							if (message.getMessage_type() == QAODefine.MSG_TYPE_IMAGE) {
								V5ImageMessage imageMessage = (V5ImageMessage)message;
								imageMessage.setUpload(true);
								imageMessage.setPic_url(url); // 设置图片pic_url
								if (!TextUtils.isEmpty(media_id)) {
									imageMessage.setMedia_id(media_id);
								}
								sendImageMessage(imageMessage, handler);
							} else {
								if (message.getMessage_type() == QAODefine.MSG_TYPE_VOICE) {
									V5VoiceMessage voiceMessage = (V5VoiceMessage)message;
									if (!TextUtils.isEmpty(media_id)) {
										voiceMessage.setMedia_id(media_id);
									}
									voiceMessage.setUrl(url);
									voiceMessage.setUpload(true);
									// 删除临时语音文件，重命名
									MediaLoader.copyPathToFileCche(getContext(), file, url);
									sendVoiceMessage(voiceMessage, handler);
								} else {
									// 其他文件类型
									sendFailedHandle(
											handler, 
											message, 
											V5ExceptionStatus.ExceptionMessageSendFailed, 
											"Media upload error: unsupport type");
								}
							}
							return;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				sendFailedHandle(
						handler, 
						message, 
						V5ExceptionStatus.ExceptionMessageSendFailed, 
						"Media upload error: response error");
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "[postMedia] failure(" + statusCode + 
						") responseString:" + responseString);
				sendFailedHandle(
						handler, 
						message, 
						V5ExceptionStatus.ExceptionMessageSendFailed, 
						responseString);
			}
		});
	}
	
	/**
     * 获得图片上传地址和签名
     * @param url
     * @param auth
     * @param imageMessage
     * @param handler
     * @deprecated
     */
    private void getMediaUploadService(String url, String auth, final V5Message message, final MessageSendCallback handler) {
    	V5HttpUtil.getPicService(url, auth, new HttpResponseHandler(mApplication) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				Logger.i(TAG, "getPictureService responseString:" + responseString);
				if (statusCode == 200) {
					try {
						JSONObject js = new JSONObject(responseString);
						if (js.has("url")) { // 成功返回signature
							String authorization = js.getString("authorization");
							String url = js.getString("url");
							String magicContext = js.optString("magic_content");
							String fileid = js.optString("fileid");
							if (message.getMessage_type() == QAODefine.MSG_TYPE_IMAGE) {
								// post上传图片
								postImageAndSend((V5ImageMessage)message, url, authorization, magicContext, fileid, handler);
							} else if (message.getMessage_type() == QAODefine.MSG_TYPE_VOICE) {
								// post上传语音  message,auth,url,magic_context,fileid
								Logger.i(TAG, "sendVoiceMessage -> postMediaMessageAndSend " + message.getState());
								postMediaMessageAndSend(message, url, authorization, magicContext, fileid, handler);
							} else {
								sendFailedHandle(
										handler, 
										message, 
										V5ExceptionStatus.ExceptionMessageSendFailed, 
										"Media upload error: unsupport media message type");
							}
							return;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					sendFailedHandle(
							handler, 
							message, 
							V5ExceptionStatus.ExceptionImageUploadFailed, 
							"Image upload error: get upload url failed");
				}
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "getPictureService statusCode:" + statusCode + " responseString:" + responseString);
				sendFailedHandle(
						handler, 
						message, 
						V5ExceptionStatus.ExceptionImageUploadFailed, 
						"Image upload error: get upload url failed");
			}
		});
	}
    
    /**
     * post上传图片
     * @param imageMessage
     * @param url
     * @param authorization
     * @param handler
     * @deprecated
     */
    private void postImageAndSend(final V5ImageMessage imageMessage, String url, 
    		String authorization, String magicContext, String fileid, final MessageSendCallback handler) {
		String filePath = imageMessage.getFilePath();
		if (null == url || null == authorization) {
			sendFailedHandle(
					handler, 
					imageMessage, 
					V5ExceptionStatus.ExceptionImageUploadFailed, 
					"Image upload error: no url or authorization");
			return;
		}
		V5HttpUtil.postLocalImage(url, filePath, authorization, magicContext, new HttpResponseHandler(mApplication) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				// 解析图片地址
				Logger.i(TAG, "[postLocalImage] success responseString:" + responseString);
				try {
					JSONObject js = new JSONObject(responseString);
					int code = js.getInt("code");
//					String message = js.optString("message");
					JSONObject data = js.optJSONObject("data");
					if (null != data && 0 == code) {
//						String url = data.optString("url");
						String download_url = data.optString("download_url");
						if (null != download_url) {
							imageMessage.setPic_url(download_url); // 设置图片pic_url
							sendImageMessage(imageMessage, handler);
							return;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				sendFailedHandle(
						handler, 
						imageMessage, 
						V5ExceptionStatus.ExceptionImageUploadFailed, 
						"Image upload error: response error");
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "[postLocalImage] failure(" + statusCode + 
						") responseString:" + responseString);
				sendFailedHandle(
						handler, 
						imageMessage, 
						V5ExceptionStatus.ExceptionImageUploadFailed, 
						responseString);
			}
		});
	}
    
    /**
     * post上传图片
     * @param imageMessage
     * @param url
     * @param authorization
     * @param handler
     * @deprecated
     */
    private void postMediaMessageAndSend(final V5Message message, String url, 
    		String authorization, final String magicContext, final String fileid, final MessageSendCallback handler) {
		if (null == url || null == authorization) {
			sendFailedHandle(
					handler, 
					message, 
					V5ExceptionStatus.ExceptionMessageSendFailed, 
					"Media upload error: no url or authorization");
			return;
		}
		String filePath = null;
		if (message.getMessage_type() == QAODefine.MSG_TYPE_IMAGE) {
			filePath = ((V5ImageMessage)message).getFilePath();
		} else if (message.getMessage_type() == QAODefine.MSG_TYPE_VOICE) {
			filePath = ((V5VoiceMessage)message).getFilePath();
		}
		final File file = new File(filePath);
		V5HttpUtil.postLocalMedia(message, filePath, url, authorization, magicContext, new HttpResponseHandler(mApplication) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				// 解析地址
				Logger.i(TAG, "[postLocalMedia] success responseString:" + responseString);
				try {
					JSONObject js = new JSONObject(responseString);
					int code = js.getInt("code");
//					String message = js.optString("message");
					JSONObject data = js.optJSONObject("data");
					if (null != data && 0 == code) {
//						String url = data.optString("url");
						String access_url = data.optString("access_url");
						if (!TextUtils.isEmpty(access_url)) {
							if (message.getMessage_type() == QAODefine.MSG_TYPE_IMAGE) {
								V5ImageMessage imageMessage = (V5ImageMessage)message;
								imageMessage.setUpload(true);
								imageMessage.setPic_url(access_url); // 设置图片pic_url
								sendImageMessage(imageMessage, handler);
							} else {
								if (message.getMessage_type() == QAODefine.MSG_TYPE_VOICE) {
									V5VoiceMessage voiceMessage = (V5VoiceMessage)message;
									voiceMessage.setUrl(access_url);
									voiceMessage.setUpload(true);
									// 删除临时语音文件，重命名
									MediaLoader.copyPathToFileCche(getContext(), file, access_url);
									// post 上传完毕信息
									if (mCustomer.getIface() == QAODefine.CSTM_IF_WXQY ||
											mCustomer.getIface() == QAODefine.CSTM_IF_WEIXIN) {
										Logger.i(TAG, "sendVoiceMessage -> postMediaObjectUploadCb " + message.getState());
										postMediaObjectUploadCb(message, access_url, magicContext, fileid, file.length(), handler);
									} else {
										// 或者 非微信接口的直接发送
										Logger.i(TAG, "sendVoiceMessage -> sendVoiceMessage(voiceMessage, handler) " + message.getState());
										sendVoiceMessage(voiceMessage, handler);
									}
								} else {
									// 其他文件类型
									sendFailedHandle(
											handler, 
											message, 
											V5ExceptionStatus.ExceptionMessageSendFailed, 
											"Media upload error: unsupport type");
								}
							}
							return;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				sendFailedHandle(
						handler, 
						message, 
						V5ExceptionStatus.ExceptionMessageSendFailed, 
						"Media upload error: response error");
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "[postLocalImage] failure(" + statusCode + 
						") responseString:" + responseString);
				sendFailedHandle(
						handler, 
						message, 
						V5ExceptionStatus.ExceptionMessageSendFailed, 
						responseString);
			}
		});
	}
    
    /**
     * 
     * @param message
     * @param accessUrl
     * @param magicContext
     * @param fileId
     * @param size
     * @param handler
     * @deprecated
     */
    private void postMediaObjectUploadCb(final V5Message message, String accessUrl, String magicContext, String fileId, long size, final MessageSendCallback handler) {
    	JSONObject parametres = new JSONObject();
    	try {
    		parametres.put("url", accessUrl);
        	parametres.put("size", size);
			parametres.put("magic_context", magicContext);
			parametres.put("fileid", fileId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	Logger.i(TAG, "postMediaObjectUploadCb: " + parametres.toString());
    	V5HttpUtil.post(Config.APP_MEDIA_UPLOAD_CB, parametres.toString(), new HttpResponseHandler(mContext) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				if (message.getMessage_type() == QAODefine.MSG_TYPE_VOICE) {
					Logger.i(TAG, "sendVoiceMessage -> sendVoiceMessage((V5VoiceMessage)message, handler) " + message.getState());
					sendVoiceMessage((V5VoiceMessage)message, handler);
				} else if (message.getMessage_type() == QAODefine.MSG_TYPE_IMAGE) {
					sendImageMessage((V5ImageMessage)message, handler);
				} else {
					// 其他类型
				}
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "[postMediaObjectUploadCb] failure(" + statusCode + 
						") responseString:" + responseString);
				sendFailedHandle(
						handler, 
						message, 
						V5ExceptionStatus.ExceptionMessageSendFailed, 
						responseString);
			}
		});
    }
    
    /**
     * 获取图片的media_id
     * @param imageMessage
     * @deprecated
     */
    private void getMediaIdOfMessage(final V5Message message, final MessageSendCallback handler, final int MediaIdGetCounter) {
    	
    	// 限制重试次数
    	if (MediaIdGetCounter > Config.GET_MEDIA_ID_TIMES) {
    		sendFailedHandle(
					handler, 
					message, 
					V5ExceptionStatus.ExceptionMessageSendFailed, 
					"Media upload error: get media_id failed");
    		return;
    	}
    	String url = null;
    	if (message.getMessage_type() == QAODefine.MSG_TYPE_IMAGE) {
    		V5ImageMessage imageMessage = (V5ImageMessage)message;
			url = String.format(Config.APP_GET_MEDIA_ID_URL_FMT, 
					mApplication.getAppInfo().getUser().getE_id(), 
					imageMessage.getPic_url().substring(imageMessage.getPic_url().length() - 32));
    	} else if (message.getMessage_type() == QAODefine.MSG_TYPE_VOICE) {
    		V5VoiceMessage voiceMessage = (V5VoiceMessage)message;
    		url =  String.format(Config.APP_GET_MEDIA_ID_URL_FMT, 
    				mApplication.getAppInfo().getUser().getE_id(), 
					voiceMessage.getUrl().substring(voiceMessage.getUrl().length() - 32));
    	}
		Logger.i(TAG, "[getMediaIdOfImage] url(" + MediaIdGetCounter +"):" + url);
		V5HttpUtil.get(url, new HttpResponseHandler(mContext) {
			@Override
			public void onSuccess(int statusCode, String responseString) {
				Logger.i(TAG, "[getMediaIdOfImage] success responseString:" + responseString);
				try {
					JSONObject js = new JSONObject(responseString);
					int o_error = js.optInt("o_error");
					String media_id = js.optString("media_id");
					if (!TextUtils.isEmpty(media_id) && 0 == o_error) {
						if (message.getMessage_type() == QAODefine.MSG_TYPE_IMAGE) {
							((V5ImageMessage)message).setMedia_id(media_id);
							sendMessageRequest((V5ImageMessage)message, handler);
						} else if (message.getMessage_type() == QAODefine.MSG_TYPE_VOICE) {
							((V5VoiceMessage)message).setMedia_id(media_id);
							Logger.i(TAG, "sendVoiceMessage -> sendMessage " + message.getState());
							sendMessageRequest((V5VoiceMessage)message, handler);
						} else {
							// 其他类型
						}
						return;
					} else {
						// 未获取到，重试
						mHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								getMediaIdOfMessage(message, handler, MediaIdGetCounter + 1);
							}
						}, Config.GET_MEDIA_ID_DELAY);
						return;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				sendFailedHandle(
						handler, 
						message, 
						V5ExceptionStatus.ExceptionImageUploadFailed, 
						"Get media_id of image error: response error");
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "[getMediaIdOfImage] failure(" + statusCode + 
						") responseString:" + responseString);
				sendFailedHandle(
						handler, 
						message, 
						V5ExceptionStatus.ExceptionImageUploadFailed, 
						responseString);
			}
		});
	}
	
	// TODO end
    
    /**
	 * 消息发送失败处理
	 * @param handler
	 * @param code
	 * @param desc
	 */
	protected void sendFailedHandle(MessageSendCallback handler, V5Message message, V5ExceptionStatus code, String desc) {
		Logger.e(TAG, "sendFailedHandle:" + desc);
		if (message != null) {
			message.setState(V5Message.STATE_FAILURE);
		}
		if (null != handler) {
			handler.onFailure(message, code, desc);
		}
	}
}
