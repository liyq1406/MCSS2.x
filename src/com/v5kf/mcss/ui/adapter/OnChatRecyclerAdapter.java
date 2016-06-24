package com.v5kf.mcss.ui.adapter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.v5kf.client.lib.entity.V5ArticleBean;
import com.v5kf.client.lib.entity.V5ArticlesMessage;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5LocationMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5VoiceMessage;
import com.v5kf.client.ui.emojicon.EmojiconTextView;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.MessageRequest;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.ui.activity.md2x.ChatMessagesActivity;
import com.v5kf.mcss.ui.activity.md2x.LocationMapActivity;
import com.v5kf.mcss.ui.activity.md2x.WebViewActivity;
import com.v5kf.mcss.ui.entity.ChatRecyclerBean;
import com.v5kf.mcss.ui.widget.CustomOptionDialog;
import com.v5kf.mcss.ui.widget.CustomOptionDialog.OptionDialogListener;
import com.v5kf.mcss.ui.widget.ListLinearLayout;
import com.v5kf.mcss.ui.widget.ListLinearLayout.OnListLayoutClickListener;
import com.v5kf.mcss.utils.DateUtil;
import com.v5kf.mcss.utils.DevUtils;
import com.v5kf.mcss.utils.FileUtil;
import com.v5kf.mcss.utils.IntentUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.MapUtil;
import com.v5kf.mcss.utils.cache.ImageLoader;
import com.v5kf.mcss.utils.cache.ImageLoader.ImageLoaderListener;
import com.v5kf.mcss.utils.cache.MediaCache;
import com.v5kf.mcss.utils.cache.MediaLoader;
import com.v5kf.mcss.utils.cache.MediaLoader.MediaLoaderListener;

/**
 * @author Chenhy	
 * @email chenhy@v5kf.com
 * @version v1.0 2015-8-4 下午3:05:16
 * @package com.v5kf.mcss.ui.adapter of MCSS-Native
 * @file OnChatRecyclerAdapter.java 
 *
 */
public class OnChatRecyclerAdapter extends RecyclerView.Adapter<OnChatRecyclerAdapter.ChatItemViewHolder> {

	private static final int TYPE_LEFT_TEXT = 0;
	private static final int TYPE_RIGHT_TEXT = 1;
	private static final int TYPE_SINGLE_NEWS = 2;
	private static final int TYPE_NEWS = 3;
	private static final int TYPE_LOCATION_R = 4;
	private static final int TYPE_LOCATION_L = 5;
	private static final int TYPE_IMG_L = 6;
	private static final int TYPE_IMG_R = 7;
	private static final int TYPE_VOICE_L = 8;
	private static final int TYPE_VOICE_R = 9;	
	public static final int TYPE_TIPS = 10;
	protected static final String TAG = "OnChatRecyclerAdapter";
	private LayoutInflater mInflater;
	private List<ChatRecyclerBean> mRecycleBeans;
	private ActivityBase mActivity;
	private ChatMessagesListener mChatMessageListener;
	
	// 语音
	private MediaPlayer mPlayer;
		
	public interface ChatMessagesListener {
		public void resendFailureMessage(V5Message message, int position);
	}
	
    public OnChatRecyclerAdapter(ActivityBase activity, List<ChatRecyclerBean> mRecycleBeans, ChatMessagesListener listener) {
        super();
        this.mRecycleBeans = mRecycleBeans;
        this.mActivity = activity;
        this.mInflater = LayoutInflater.from(activity);
        this.mChatMessageListener = listener;
    }
    

    @Override
    public int getItemViewType(int position) {
    	int msgType = mRecycleBeans.get(position).getMessage().getMessage_type();
    	int msgDir = mRecycleBeans.get(position).getDir();
    	
    	if (msgType == QAODefine.MSG_TYPE_NEWS) {
    		V5Message msgContent = mRecycleBeans.get(position).getMessage();
    		if (msgContent != null && ((V5ArticlesMessage)msgContent).getArticles() != null) {
	    		if (((V5ArticlesMessage)msgContent).getArticles().size() == 1) {
	    			return TYPE_SINGLE_NEWS;
	    		} else if (((V5ArticlesMessage)msgContent).getArticles().size() > 1) {
	    			return TYPE_NEWS;
	    		}
    		}
    	} else if (msgType == QAODefine.MSG_TYPE_LOCATION) {
    		if (msgDir == QAODefine.MSG_DIR_TO_CUSTOMER || 
    				msgDir == QAODefine.MSG_DIR_FROM_ROBOT) {
    			return TYPE_LOCATION_R;
    		} else if (msgDir == QAODefine.MSG_DIR_TO_WORKER || 
    				msgDir == QAODefine.MSG_DIR_FROM_WAITING ||
    				msgDir == QAODefine.MSG_DIR_R2WM) {
    			return TYPE_LOCATION_L;
    		}
    	} else if (msgType == QAODefine.MSG_TYPE_IMAGE) {
    		if (msgDir == QAODefine.MSG_DIR_TO_WORKER || 
    				msgDir == QAODefine.MSG_DIR_FROM_WAITING ||
    				msgDir == QAODefine.MSG_DIR_R2WM) {
    			return TYPE_IMG_L;
    		} else if (msgDir == QAODefine.MSG_DIR_TO_CUSTOMER ||
    				msgDir == QAODefine.MSG_DIR_FROM_ROBOT) {
    			return TYPE_IMG_R;
    		}
    	} else if (msgType == QAODefine.MSG_TYPE_VOICE) {
    		if (msgDir == QAODefine.MSG_DIR_TO_WORKER || 
    				msgDir == QAODefine.MSG_DIR_FROM_WAITING ||
    				msgDir == QAODefine.MSG_DIR_R2WM) {
    			return TYPE_VOICE_L;
    		} else if (msgDir == QAODefine.MSG_DIR_TO_CUSTOMER ||
    				msgDir == QAODefine.MSG_DIR_FROM_ROBOT) {
    			return TYPE_VOICE_R;
    		}
    	} else if (msgType == QAODefine.MSG_TYPE_WXCS ||
    			msgType == QAODefine.MSG_TYPE_CONTROL) {
    		return TYPE_TIPS;
    	}
    	
		if (msgDir == QAODefine.MSG_DIR_TO_WORKER || 
				msgDir == QAODefine.MSG_DIR_FROM_WAITING ||
				msgDir == QAODefine.MSG_DIR_R2WM) {
			return TYPE_LEFT_TEXT;
		} else { // QAODefine.MSG_DIR_TO_CUSTOMER 或 QAODefine.MSG_DIR_FROM_ROBOT
			return TYPE_RIGHT_TEXT;
		}
    }
    
    @Override
    public long getItemId(int position) {
    	return position;
    }

    @Override
    public ChatItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    	ChatItemViewHolder viewHolder = null;
    	View itemView = null;
        switch (viewType) {
        case TYPE_LEFT_TEXT:
        	itemView = mInflater.inflate(R.layout.item_chat_from_msg, parent, false) ;
        	viewHolder = new ChatItemViewHolder(viewType, itemView);
        	break;
        	
        case TYPE_RIGHT_TEXT:
        	itemView = mInflater.inflate(R.layout.item_chat_to_msg, parent, false);
        	viewHolder = new ChatItemViewHolder(viewType, itemView);
        	break;
        	
        case TYPE_SINGLE_NEWS:
        	itemView = mInflater.inflate(R.layout.item_chat_single_news, parent, false);
        	viewHolder = new ChatItemViewHolder(viewType, itemView);
        	break;
        	
        case TYPE_NEWS:
        	itemView = mInflater.inflate(R.layout.item_chat_multi_news, parent, false);
        	viewHolder = new ChatItemViewHolder(viewType, itemView);
        	break;
        	
        case TYPE_LOCATION_R:
        	itemView = mInflater.inflate(R.layout.item_chat_to_location, parent, false);
        	viewHolder = new ChatItemViewHolder(viewType, itemView);
        	break;

        case TYPE_LOCATION_L:
        	itemView = mInflater.inflate(R.layout.item_chat_from_location, parent, false);
        	viewHolder = new ChatItemViewHolder(viewType, itemView);
        	break;
        	
        case TYPE_IMG_L:
        	itemView = mInflater.inflate(R.layout.item_chat_from_img, parent, false);
        	viewHolder = new ChatItemViewHolder(viewType, itemView);
        	break;

        case TYPE_IMG_R:
        	itemView = mInflater.inflate(R.layout.item_chat_to_img, parent, false);
        	viewHolder = new ChatItemViewHolder(viewType, itemView);
        	break;
        	
        case TYPE_VOICE_L:
        	itemView = mInflater.inflate(R.layout.item_chat_from_voice, parent, false);
        	viewHolder = new ChatItemViewHolder(viewType, itemView);
        	break;

        case TYPE_VOICE_R:
        	itemView = mInflater.inflate(R.layout.item_chat_to_voice, parent, false);
        	viewHolder = new ChatItemViewHolder(viewType, itemView);
        	break;
        	
        case TYPE_TIPS:
        	itemView = mInflater.inflate(R.layout.item_chat_tips, parent, false);
        	viewHolder = new ChatItemViewHolder(viewType, itemView);
        	break;
        	
        default:
        	itemView = mInflater.inflate(R.layout.item_chat_to_msg, parent, false);
        	viewHolder = new ChatItemViewHolder(viewType, itemView);
        	break;
        }
        
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ChatItemViewHolder holder, final int position) {
    	// 设置数据
        final ChatRecyclerBean chatMessage = mRecycleBeans.get(position);
		holder.setChatBean(chatMessage);
		holder.setPosition(position);
        if (position == 0 || 
//        	getItemViewType(position) > TYPE_RIGHT_TEXT || 
        	(chatMessage.getMessage().getCreate_time() - 
        		mRecycleBeans.get(position - 1).getMessage().getCreate_time()) 
        		> Config.SESSION_TIME_DETA) { // 相隔5min的消息显示时间戳
        	holder.mDate.setVisibility(View.VISIBLE);
			holder.mDate.setText(
					DateUtil.timeFormat(chatMessage.getDefaultTime(), false));
		} else {
			holder.mDate.setVisibility(View.GONE);
		}
		
		switch (getItemViewType(position)) {
			case TYPE_SINGLE_NEWS: { // 单图文
				V5ArticleBean article = ((V5ArticlesMessage)chatMessage.getMessage()).getArticles().get(0);
				holder.mNewsTitle.setText(article.getTitle());
				holder.mNewsContent.setText(article.getDescription());
				ImageLoader imgLoader = new ImageLoader(mActivity, true, R.drawable.v5_img_src_loading);
	        	imgLoader.DisplayImage(article.getPic_url(), holder.mNewsPic);
			}
			break;
			
			case TYPE_NEWS: { // 多图文
				holder.mNewsAdapter = new NewsListAdapter(
						mActivity, 
						((V5ArticlesMessage)chatMessage.getMessage()).getArticles(),
						true);
				holder.mNewsListLayout.bindLinearLayout(holder.mNewsAdapter);
			}
			break;
			
			case TYPE_LOCATION_R: { // 位置-发出
				ImageLoader mapImgLoader = new ImageLoader(mActivity, true, R.drawable.v5_img_src_loading);
	        	double lat = ((V5LocationMessage)chatMessage.getMessage()).getX();
	        	double lng = ((V5LocationMessage)chatMessage.getMessage()).getY();
	        	String url = String.format(Locale.CHINA, Config.MAP_PIC_API_FORMAT, lat, lng, lat, lng);
	        	// url = MD5.encode(chatMessage.getMessage().getMsg_content().getLabel());
	        	mapImgLoader.DisplayImage(url, holder.mMapIv);
	        	holder.mLbsDescTv.setText(mActivity.getString(R.string.loading));
	        	MapUtil.getLocationTitle(lat, lng, holder.mLbsDescTv);
			}
			break;
			
			case TYPE_LOCATION_L: { // 位置-接收
				if (null == chatMessage.getMessage()) {
					return;
				}
				ImageLoader mapImgLoader = new ImageLoader(mActivity, true, R.drawable.v5_img_src_loading);
	        	double lat = ((V5LocationMessage)chatMessage.getMessage()).getX();
	        	double lng = ((V5LocationMessage)chatMessage.getMessage()).getY();
	        	String url = String.format(Locale.CHINA, Config.MAP_PIC_API_FORMAT, lat, lng, lat, lng);
	        	// url = MD5.encode(chatMessage.getMessage().getMsg_content().getLabel());
	        	mapImgLoader.DisplayImage(url, holder.mMapIv);
	        	holder.mLbsDescTv.setText(mActivity.getString(R.string.loading));
	        	MapUtil.getLocationTitle(lat, lng, holder.mLbsDescTv);
			}
			break;
			
			case TYPE_IMG_R:
			case TYPE_IMG_L: { // 图片
				if (chatMessage.getMessage() == null) {
					Logger.w(TAG, "[onBindViewHolder] TYPE_IMG_L/R: null message content");
					return;
				}
				ImageLoader mapImgLoader = new ImageLoader(mActivity, true, R.drawable.v5_img_src_loading, new ImageLoaderListener() {
					@Override
					public void onSuccess(String url, ImageView imageView) {
					}
					
					@Override
					public void onFailure(final ImageLoader imageLoader, final String url, final ImageView imageView) {
						if (url.contains("chat.v5kf.com/") && imageLoader.getmTryTimes() < 5) { // 最多重试5次
							mActivity.getHandler().postDelayed(new Runnable() {
								public void run() {
									imageLoader.DisplayImage(url, imageView);
								}
							}, 500); // 0.5s后重试
						}
					}
				});
				String urlPath = ((V5ImageMessage)chatMessage.getMessage()).getThumbnailPicUrl();
	        	mapImgLoader.DisplayImage(urlPath, holder.mMapIv);
			}
			break;
			
			case TYPE_VOICE_L:
			case TYPE_VOICE_R: { // 语音
				if (chatMessage.getMessage() == null) {
					Logger.w(TAG, "[onBindViewHolder] TYPE_VOICE_L/R: null message content");
					return;
				}
				
				final V5VoiceMessage voiceMessage = (V5VoiceMessage)chatMessage.getMessage();
				Logger.d(TAG, "list load Voice ----- duration:" + voiceMessage.getDuration());
	        	holder.mVoiceSecondTv.setText(String.format("%.1f″", voiceMessage.getDuration()/1000.0f));
	        	
	        	// 背景
				if (chatMessage.getDir() == QAODefine.MSG_DIR_TO_WORKER || 
						chatMessage.getDir() == QAODefine.MSG_DIR_FROM_WAITING ||
						chatMessage.getDir() == QAODefine.MSG_DIR_R2WM) {
					holder.mVoiceLayout.setBackgroundResource(R.drawable.list_from_customer_bg);
				} else if (chatMessage.getDir() == QAODefine.MSG_DIR_FROM_ROBOT) {
					holder.mVoiceLayout.setBackgroundResource(R.drawable.list_to_robot_bg);
				} else if (chatMessage.getDir() == QAODefine.MSG_DIR_TO_CUSTOMER) {
					holder.mVoiceLayout.setBackgroundResource(R.drawable.list_to_worker_bg);
				}
				// 语音状态
	        	if (chatMessage.isVoicePlaying()) {
	        		holder.updateVoiceStartPlayingState();
	        	} else {
	        		holder.updateVoiceStopPlayingState();
	        	}
				if (voiceMessage.getFilePath() != null && voiceMessage.getDuration() > 0
						&& FileUtil.isFileExists(voiceMessage.getFilePath())) { // 已加载则不需要loadMedia
					Logger.d(TAG, "---已加载---");
					break;
				} else {
					Logger.d(TAG, "---首次加载---");
				}
				
				// 加载语音
	        	String url = voiceMessage.getUrl();
	        	if (TextUtils.isEmpty(url)) {
	        		if (TextUtils.isEmpty(voiceMessage.getMessage_id()) && voiceMessage.getFilePath() != null) {
	        			url = voiceMessage.getFilePath();
	        		} else {
	        			url = String.format(Config.APP_RESOURCE_V5_FMT, Config.SITE_ID, voiceMessage.getMessage_id());
	        			voiceMessage.setUrl(url);
	        		}
	        	}
	        	if (voiceMessage.getFilePath() != null) {
	        		Logger.w(TAG, "sendVoiceMessage -> message=" + voiceMessage);
	        		Logger.w(TAG, "sendVoiceMessage -> result -> Voice url:" + voiceMessage.getFilePath() + " sendState:" + voiceMessage.getState());
	        	}
	        	Logger.d(TAG, "Voice url:" + url + " sendState:" + voiceMessage.getState());
	        	MediaLoader mediaLoader = new MediaLoader(mActivity, holder, new MediaLoaderListener() {
					
					@Override
					public void onSuccess(V5Message msg, Object obj, MediaCache media) {
//						if (((V5VoiceMessage)msg).getFilePath() == null) { // 非本地文件
//							msg.setState(V5Message.STATE_ARRIVED);
//							sendStateChange(holder, chatMessage);
//						}
						((V5VoiceMessage)msg).setFilePath(media.getLocalPath());
						((V5VoiceMessage)msg).setDuration(media.getDuration());
						Logger.d(TAG, "list load Voice onSuccess ----- duration:" + voiceMessage.getDuration());
						((ChatItemViewHolder)obj).mVoiceSecondTv.setText(String.format("%.1f″", voiceMessage.getDuration()/1000.0f));
					}
					
					@Override
					public void onFailure(final MediaLoader mediaLoader, final V5Message msg, Object obj) {
//						if (((V5VoiceMessage)msg).getFilePath() == null) { // 非本地文件
//							msg.setState(V5Message.STATE_FAILURE);
//							sendStateChange(holder, chatMessage);
//						}
						if (mediaLoader.getUrl().contains("chat.v5kf.com/") 
								&& mediaLoader.getmTryTimes() < 5) { // 最多重试5次
							mActivity.getHandler().postDelayed(new Runnable() {
								public void run() {
									mediaLoader.loadMedia(mediaLoader.getUrl(), msg, null);
								}
							}, 500); // 0.5s后重试
						}
					}
				});
	        	mediaLoader.loadMedia(url, voiceMessage, null);
			}
				break;
			
			case TYPE_TIPS: {
				holder.mTips.setText(chatMessage.getDefaultContent(mActivity));
				break;
			}
			
			default: {
				String str = chatMessage.getDefaultContent(mActivity) == null ? "" : chatMessage.getDefaultContent(mActivity);
//				Logger.d(TAG, "对话：" + str);
				Spanned text = Html.fromHtml(str.replace("\n", "<br>"));
				holder.mMsg.setText(text);
				holder.mMsg.setMovementMethod(LinkMovementMethod.getInstance());
				
				if (chatMessage.getDir() == QAODefine.MSG_DIR_TO_WORKER ||
						chatMessage.getDir() == QAODefine.MSG_DIR_FROM_WAITING ||
						chatMessage.getDir() == QAODefine.MSG_DIR_R2WM) {
					holder.mMsg.setBackgroundResource(R.drawable.list_from_customer_bg);
//					ImageLoader imgLoader = new ImageLoader(mActivity, true, R.drawable.v5_photo_default_cstm);
//		        	imgLoader.DisplayImage(mActivity.getCustomerPhoto(), holder.mPic);
				} else if (chatMessage.getDir() == QAODefine.MSG_DIR_FROM_ROBOT) {
//					holder.mPic.setImageResource(R.drawable.ic_launcher);
					holder.mMsg.setBackgroundResource(R.drawable.list_to_robot_bg);
				} else if (chatMessage.getDir() == QAODefine.MSG_DIR_TO_CUSTOMER) {
					holder.mMsg.setBackgroundResource(R.drawable.list_to_worker_bg);
//					ImageLoader imgLoader = new ImageLoader(mActivity, true, R.drawable.v5kf);
//		        	imgLoader.DisplayImage(mActivity.getWorkerPhoto(), holder.mPic);
				}
			}
			break;
		}
		
		sendStateChange(holder, chatMessage);
		if (holder.mSendFailedIv != null && holder.mSendingPb != null && 
				chatMessage.getMessage().getDirection() == QAODefine.MSG_DIR_TO_CUSTOMER) {
        	holder.mSendFailedIv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (mChatMessageListener != null) {
						mChatMessageListener.resendFailureMessage(chatMessage.getMessage(), position);
					}
				}
			});
        }
    }
    
    private void sendStateChange(ChatItemViewHolder holder, final ChatRecyclerBean chatMessage) {
    	if (holder.mSendFailedIv != null && holder.mSendingPb != null) {
//        	if (chatMessage.getMessage().getDirection() == QAODefine.MSG_DIR_TO_CUSTOMER) {
//        		if (chatMessage.getMessage().getState() == V5Message.STATE_FAILURE) {
//        			holder.mSendingPb.setVisibility(View.GONE);
//        			holder.mSendFailedIv.setVisibility(View.VISIBLE);
//        		} else if (chatMessage.getMessage().getState() == V5Message.STATE_SENDING) {
//        			holder.mSendingPb.setVisibility(View.VISIBLE);
//        			holder.mSendFailedIv.setVisibility(View.GONE);
//        		} else {
//        			holder.mSendFailedIv.setVisibility(View.GONE);
//            		holder.mSendingPb.setVisibility(View.GONE);
//        		}
//        	} else {
//        		holder.mSendFailedIv.setVisibility(View.GONE);
//        		holder.mSendingPb.setVisibility(View.GONE);
//        	}
    		if (chatMessage.getMessage().getState() == V5Message.STATE_FAILURE) {
    			holder.mSendingPb.setVisibility(View.GONE);
    			holder.mSendFailedIv.setVisibility(View.VISIBLE);
    		} else if (chatMessage.getMessage().getState() == V5Message.STATE_SENDING) {
    			holder.mSendingPb.setVisibility(View.VISIBLE);
    			holder.mSendFailedIv.setVisibility(View.GONE);
    		} else {
    			holder.mSendFailedIv.setVisibility(View.GONE);
        		holder.mSendingPb.setVisibility(View.GONE);
    		}
        }
    }

    @Override
    public int getItemCount() {
        return mRecycleBeans.size();
    }

    
    class ChatItemViewHolder extends RecyclerView.ViewHolder implements OnClickListener, OnLongClickListener{

    	private static final String TAG = "OnChatRecyclerAdapter.ChatItemViewHolder";
    	private ChatRecyclerBean mChatBean;
    	private int mPosition;
    	
    	public ImageView mSendFailedIv;
		public ProgressBar mSendingPb;
		
//		public CircleImageView mPic;
        public TextView mDate;
        public EmojiconTextView mMsg;
        
        /* 单图文news */
        public TextView mNewsTitle;
        public TextView mNewsContent;
		public ImageView mNewsPic;
        
        /* 多图文 */
		public ListLinearLayout mNewsListLayout;
        public NewsListAdapter mNewsAdapter;
        
        /* 位置 */
        public ImageView mMapIv;
        public TextView mLbsDescTv;
        
        /* 图片 */
        // 共用mMapIv
        
        /* 语音 */
        public View mVoiceLayout;
        public ImageView mVoiceIv;
        public TextView mVoiceSecondTv;
        public AnimationDrawable mVoiceAnimDrawable;
        
        /* Tips */
        public TextView mTips;

        public ChatItemViewHolder(int viewType, View itemView) {
            super(itemView);
            switch (viewType) {
            case TYPE_LEFT_TEXT:
//            	mPic = (CircleImageView) itemView.findViewById(R.id.chat_item_left_img);
            	mDate = (TextView) itemView.findViewById(R.id.id_from_msg_date);
            	mMsg = (EmojiconTextView) itemView.findViewById(R.id.id_from_msg_text);
                mMsg.setOnClickListener(this);
                mMsg.setOnLongClickListener(this);
            	break;
            	
            case TYPE_RIGHT_TEXT:
//            	mPic = (CircleImageView) itemView.findViewById(R.id.chat_item_right_img);
            	mDate = (TextView) itemView.findViewById(R.id.id_to_msg_date);
            	mMsg = (EmojiconTextView) itemView.findViewById(R.id.id_to_msg_text);
            	mSendFailedIv = (ImageView) itemView.findViewById(R.id.id_msg_fail_iv);
            	mSendingPb = (ProgressBar) itemView.findViewById(R.id.id_msg_out_pb);
                mMsg.setOnClickListener(this);
                mMsg.setOnLongClickListener(this);
            	break;
            	
            case TYPE_SINGLE_NEWS:
            	mDate = (TextView) itemView.findViewById(R.id.id_news_msg_date);
            	mNewsPic = (ImageView) itemView.findViewById(R.id.chat_item_news_img);
            	mNewsTitle = (TextView) itemView.findViewById(R.id.id_news_title_inner_text);
            	mNewsContent = (TextView) itemView.findViewById(R.id.id_news_desc_text);
            	itemView.findViewById(R.id.id_news_layout).setOnClickListener(this);
            	break;
            	
            case TYPE_NEWS:
            	mDate = (TextView) itemView.findViewById(R.id.id_news_msg_date);
            	mNewsListLayout = (ListLinearLayout) itemView.findViewById(R.id.id_news_layout);
            	mNewsListLayout.setOnListLayoutClickListener(new OnListLayoutClickListener() {
					
					@Override
					public void onListLayoutClick(View v, int pos) {
						ChatRecyclerBean bean = mRecycleBeans.get(getAdapterPosition());
						if (((V5ArticlesMessage)bean.getMessage()).getArticles().size() > pos) {
							String url = ((V5ArticlesMessage)bean.getMessage()).getArticles().get(pos).getUrl();
							onSingleNewsClick(url);
						}
					}
				});
            	break;
            	
            case TYPE_LOCATION_R:
//            	mPic = (CircleImageView) itemView.findViewById(R.id.chat_item_right_img);
            	mDate = (TextView) itemView.findViewById(R.id.id_to_msg_date);
            	mMapIv = (ImageView) itemView.findViewById(R.id.ic_map_img_iv);
            	mLbsDescTv = (TextView) itemView.findViewById(R.id.id_map_address_text);
            	mSendFailedIv = (ImageView) itemView.findViewById(R.id.id_msg_fail_iv);
            	mSendingPb = (ProgressBar) itemView.findViewById(R.id.id_msg_out_pb);
            	mMapIv.setOnClickListener(this);
            	mMapIv.setOnLongClickListener(this);
            	break;
            	
            case TYPE_LOCATION_L:
//            	mPic = (CircleImageView) itemView.findViewById(R.id.chat_item_left_img);
            	mDate = (TextView) itemView.findViewById(R.id.id_from_msg_date);
            	mMapIv = (ImageView) itemView.findViewById(R.id.ic_map_img_iv);
            	mLbsDescTv = (TextView) itemView.findViewById(R.id.id_map_address_text);
            	mMapIv.setOnClickListener(this);
            	mMapIv.setOnLongClickListener(this);
            	break;
            	
            case TYPE_IMG_L:
            	mDate = (TextView) itemView.findViewById(R.id.id_from_msg_date);
            	mMapIv = (ImageView) itemView.findViewById(R.id.ic_type_img_iv);
            	mMapIv.setOnClickListener(this);
            	mMapIv.setOnLongClickListener(this);
            	break;

            case TYPE_IMG_R:
            	mDate = (TextView) itemView.findViewById(R.id.id_to_msg_date);
            	mMapIv = (ImageView) itemView.findViewById(R.id.ic_type_img_iv);
            	mSendFailedIv = (ImageView) itemView.findViewById(R.id.id_msg_fail_iv);
            	mSendingPb = (ProgressBar) itemView.findViewById(R.id.id_msg_out_pb);
            	mMapIv.setOnClickListener(this);
            	mMapIv.setOnLongClickListener(this);
            	break;
            	
            case TYPE_VOICE_L:
            	mDate = (TextView) itemView.findViewById(R.id.id_from_msg_date);
            	mVoiceLayout = (View) itemView.findViewById(R.id.id_left_voice_layout);
            	mVoiceIv = (ImageView) itemView.findViewById(R.id.id_from_voice_iv);
            	mVoiceSecondTv = (TextView) itemView.findViewById(R.id.id_from_voice_tv);
            	mVoiceLayout.setOnClickListener(this);
            	break;
            	
            case TYPE_VOICE_R:
            	mDate = (TextView) itemView.findViewById(R.id.id_to_msg_date);
            	mVoiceLayout = (View) itemView.findViewById(R.id.id_right_voice_layout);
            	mVoiceIv = (ImageView) itemView.findViewById(R.id.id_to_voice_iv);
            	mVoiceSecondTv = (TextView) itemView.findViewById(R.id.id_to_voice_tv);
            	
            	mSendFailedIv = (ImageView) itemView.findViewById(R.id.id_msg_fail_iv);
            	mSendingPb = (ProgressBar) itemView.findViewById(R.id.id_msg_out_pb);
            	mVoiceLayout.setOnClickListener(this);
            	break;
            	
            case TYPE_TIPS:
            	mDate = (TextView) itemView.findViewById(R.id.id_from_msg_date);
            	mTips = (TextView) itemView.findViewById(R.id.id_msg_tips);
            	break;
            	
            default:
            	mDate = (TextView) itemView.findViewById(R.id.id_to_msg_date);
            	mMsg = (EmojiconTextView) itemView.findViewById(R.id.id_to_msg_text);
            	mSendFailedIv = (ImageView) itemView.findViewById(R.id.id_msg_fail_iv);
            	mSendingPb = (ProgressBar) itemView.findViewById(R.id.id_msg_out_pb);
                mMsg.setOnClickListener(this);
                mMsg.setOnLongClickListener(this);
            	break;
            }
            
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            
            if (mMsg instanceof EmojiconTextView) { // URL点击事件
            	((EmojiconTextView)mMsg).setURLClickListener(new EmojiconTextView.OnURLClickListener() {
					
					@Override
					public void onClick(View v, String url) {
						Intent intent = IntentUtil.getStartWebViewIntent(
								mActivity, 
								WebViewActivity.class, 
								url, 
								0);
						mActivity.startActivity(intent);
						mActivity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
					}
				});
            }
        }
        
        public void setChatBean(ChatRecyclerBean chatBean) {
        	this.mChatBean = chatBean;
        }

		@Override
		public void onClick(View v) {
			Logger.d(TAG, "item onCLick View.id=" + v.getId());
			if (null == mChatBean) {
				Logger.e(TAG, "ViewHolder has null ChatRecycleBean");
				return;
			}
			
			int viewType = getItemViewType();
			switch (v.getId()) {
			case R.id.id_from_msg_text: {
				onFromMsgClick();
				break;
			}
			case R.id.id_to_msg_text: {
				
				break;
			}
			case R.id.id_news_layout: { // 点击单图文
				String url = ((V5ArticlesMessage)mChatBean.getMessage()).
						getArticles().get(0).getUrl();
				if (url != null) {
					onSingleNewsClick(url);
				}
				break;
			}
			case R.id.ic_type_img_iv: { // 点击图片
				if (TYPE_IMG_L == viewType || TYPE_IMG_R == viewType) {
					if (mChatBean.getMessage() == null) {
						return;
					}
					gotoShowImageActivity(((V5ImageMessage)mChatBean.getMessage()).getDefaultPicUrl());
				}
				break;
			}
			case R.id.ic_map_img_iv: { // 点击地图
				if (TYPE_LOCATION_L == viewType || TYPE_LOCATION_R == viewType) {
					if (mChatBean.getMessage() == null) {
						return;
					}
					gotoLocationMapActivity(((V5LocationMessage)mChatBean.getMessage()).getX(), ((V5LocationMessage)mChatBean.getMessage()).getY());
				}
				break;
			}
			case R.id.id_left_voice_layout:
			case R.id.id_right_voice_layout: // 点击语音
				if (mChatBean.getMessage().getMessage_type() == QAODefine.MSG_TYPE_VOICE) {
					if (mChatBean.isVoicePlaying()) { // 停止播放
						stopPlaying();
					} else { // 开始播放
//						if (mVoiceAnimDrawable != null) {
//							mVoiceAnimDrawable.stop();
//						}
						startPlaying((V5VoiceMessage)mChatBean.getMessage(), new OnCompletionListener() {
							
							@Override
							public void onCompletion(MediaPlayer mp) {
								Logger.i(TAG, "MediaPlayer - completePlaying");
								updateVoiceStopPlayingState();
								mp.release();
								mp = null;
								mPlayer = null;
							}
						});
					}
				}
				break;
			
			default:
				/* 点击其他地方隐藏bottom bar */
				Message msg = new Message();
				msg.what = ChatMessagesActivity.HDL_WHAT_HIDE_BOTTOM;
				mActivity.sendHandlerMessage(msg);
				break;
			}
			
		}

		private void gotoLocationMapActivity(double x, double y) {
			Intent intent = new Intent(mActivity, LocationMapActivity.class);
			intent.putExtra("x", x);
			intent.putExtra("y", y);
			mActivity.gotoActivity(intent);
		}

		private void gotoShowImageActivity(String pic_url) {
			mActivity.gotoImageActivity(pic_url);
		}

		private void onSingleNewsClick(String url) {
			Intent i = IntentUtil.getStartWebViewIntent(mActivity, WebViewActivity.class, url, 0);
			mActivity.gotoActivity(i);
		}

		@Override
		public boolean onLongClick(View v) {
			Logger.d(TAG, "item onLongCLick View.id=" + v.getId());
			if (null == mChatBean) {
				Logger.e(TAG, "ViewHolder has null ChatRecycleBean");
				return false;
			}
			
			onMsgLongClick();
/*
			int viewType = getItemViewType();
			switch (viewType) {
			case TYPE_LEFT_TEXT:
			case TYPE_RIGHT_TEXT:
				switch (v.getId()) {
				case R.id.id_from_msg_text:
					onFromMsgLongClick();
					break;
					
				case R.id.id_to_msg_text:
					onToMsgLongClick();
					break;
				}
				break;
				
			case TYPE_LOCATION_L:
			case TYPE_LOCATION_R:
			case TYPE_IMG_L:
			case TYPE_IMG_R:
			case TYPE_SINGLE_NEWS:
			case TYPE_NEWS:
			default:
				onToMsgLongClick();
				break;
			}
*/
			return true;
		}

		public void onFromMsgClick() {
			if (mChatBean.getMessage().getCandidate() != null && mChatBean.getMessage().getCandidate().size() > 0) {
				if (mChatBean.getMessage().getCandidate().get(0).getMessage_type() == QAODefine.MSG_TYPE_CONTROL) {
					return;
				}
				Message msg = new Message();
				msg.what = ChatMessagesActivity.HDL_WHAT_SHOW_ROBOT_MENU;
				Bundle bundle = new Bundle();
				bundle.putInt(ChatMessagesActivity.MSG_KEY_TYPE, ChatMessagesActivity.MSG_KEY_TYPE_R);
				bundle.putInt(ChatMessagesActivity.MSG_KEY_POSITION, getAdapterPosition());
				msg.setData(bundle);
				Logger.d(TAG, "[onFromMsgClick] Message：" + mChatBean.getMessage().getDefaultContent(mActivity));
				Logger.d(TAG, "[onFromMsgClick] 机器人：" + mChatBean.getDefaultCandidate(mActivity));
				Logger.d(TAG, "[onFromMsgClick] type:" + mChatBean.getMessage().getCandidate().get(0).getMessage_type());
				mActivity.sendHandlerMessage(msg);
			}
		}
		
		/**
		 * 长按弹出选项窗口，不同类型消息选项有区别
		 * @param onToMsgLongClick ChatItemViewHolder 
		 * @return void
		 */
		private void onMsgLongClick() {
//			mMsg.setBackgroundResource(R.drawable.chatto_bg_pressed);
			mActivity.showOptionDialogInChat(
					CustomOptionDialog.DISPLAY_TYPE_CHAT_TWO_BTN, 
					new OptionDialogListener() {
				@Override
				public void onClick(View view) {
					switch (view.getId()) {
					case R.id.btn_dialog_top_option:	// 复制
						DevUtils.copy(mChatBean.getDefaultContent(mActivity), mActivity);
						break;
						
					case R.id.btn_dialog_bottom_option: // 提问机器人
						sendMessageToRobot(mChatBean);
						break;
					}
				}

				@Override
				public void onDismiss(DialogInterface dialog) {
//					mMsg.setBackgroundResource(R.drawable.list_to_textview_bg);
				}
			});
		}
		
/*
		private void onFromMsgLongClick() {
			if (mChatBean.getMessage().getMessage_type() == QAODefine.MSG_TYPE_TEXT) {
//				mMsg.setBackgroundResource(R.drawable.chatfrom_bg_pressed);
				mActivity.showOptionDialogInChat(
						CustomOptionDialog.DISPLAY_TYPE_CHAT_TWO_BTN, 
						new OptionDialogListener() {					
					@Override
					public void onClick(View view) {
						switch (view.getId()) {
						case R.id.btn_dialog_top_option:	// 复制
							DevUtils.copy(mChatBean.getDefaultContent(mActivity), mActivity);
							break;
							
						case R.id.btn_dialog_bottom_option: // 机器人答案
							sendMessageToRobot(mChatBean);
							break;
						}
					}
	
					@Override
					public void onDismiss(DialogInterface dialog) {
//						mMsg.setBackgroundResource(R.drawable.list_from_textview_bg);
					}
				});
			}
		}
		
		private void onToMsgLongClick() {
//			mMsg.setBackgroundResource(R.drawable.chatto_bg_pressed);
			switch (mChatBean.getMessage().getMessage_type()) {
			case QAODefine.MSG_TYPE_TEXT: 
				mActivity.showOptionDialogInChat(
						CustomOptionDialog.DISPLAY_TYPE_CHAT_TWO_BTN, 
						new OptionDialogListener() {
					@Override
					public void onClick(View view) {
						switch (view.getId()) {
						case R.id.btn_dialog_top_option:	// 复制
							DevUtils.copy(mChatBean.getDefaultContent(mActivity), mActivity);
							break;
							
						case R.id.btn_dialog_bottom_option: // 提问机器人
							sendMessageToRobot(mChatBean);
							break;
						}
					}

					@Override
					public void onDismiss(DialogInterface dialog) {
//						mMsg.setBackgroundResource(R.drawable.list_to_textview_bg);
					}
				});
				break;
			
//			case QAODefine.MSG_TYPE_IMAGE:
//			case QAODefine.MSG_TYPE_NEWS:
//			case QAODefine.MSG_TYPE_LOCATION:
//				mActivity.showOptionDialogInChat(
//						CustomOptionDialog.DISPLAY_TYPE_CHAT_ONE_BTN, 
//						new OptionDialogListener() {
//					@Override
//					public void onClick(View view) {
//						switch (view.getId()) {
//						case R.id.btn_dialog_only_option:	// 转发
//							forwardMessage();
//							break;
//						}
//					}
//
//					@Override
//					public void onDismiss(DialogInterface dialog) {
////						mMsg.setBackgroundResource(R.drawable.list_to_textview_bg);
//					}
//				});
//				break;
			}
		}
*/
		
		/**
		 * 发送给机器人
		 * @param sendMessageToRobot ChatItemViewHolder 
		 * @return void
		 */
		protected void sendMessageToRobot(ChatRecyclerBean chatBean) {
			Logger.i(TAG, "sendMessageToRobot:" + getAdapterPosition());
			//ChatRecyclerBean mChatBean = mRecycleBeans.get(getAdapterPosition());
			V5Message msg = chatBean.getMessage().cloneMessage();
			msg.setDirection(QAODefine.MSG_DIR_W2R);
			
			sendMessage(msg);
		}
		
		protected void sendMessage(V5Message msg) {
			try {
				/* 发送Message请求 */
				MessageRequest mReq = (MessageRequest) RequestManager.getRequest(QAODefine.O_TYPE_MESSAGE, mActivity);
				mReq.sendMessage(msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		public void updateVoiceStartPlayingState() {
			Logger.i(TAG, "UI - updateVoiceStartPlayingState position:" + mPosition);
			mChatBean.setVoicePlaying(true);
			mVoiceIv.setBackgroundResource(R.anim.anim_rightwhite_voice);
			if (mChatBean.getDir() == QAODefine.MSG_DIR_TO_WORKER || 
					mChatBean.getDir() == QAODefine.MSG_DIR_FROM_WAITING) {
				mVoiceIv.setBackgroundResource(R.anim.anim_leftgray_voice);
			} else if (mChatBean.getDir() == QAODefine.MSG_DIR_FROM_ROBOT ||
					mChatBean.getDir() == QAODefine.MSG_DIR_TO_CUSTOMER) {
				mVoiceIv.setBackgroundResource(R.anim.anim_rightwhite_voice);
			}
			mVoiceAnimDrawable = (AnimationDrawable)mVoiceIv.getBackground();
			mVoiceAnimDrawable.start();
		}
		
		public void updateVoiceStopPlayingState() {
			Logger.i(TAG, "UI - updateVoiceStopPlayingState position:" + mPosition);
			mChatBean.setVoicePlaying(false);
			if (mVoiceAnimDrawable != null) {
				mVoiceAnimDrawable.stop();
				mVoiceAnimDrawable = null;
			}
			if (mChatBean.getDir() == QAODefine.MSG_DIR_TO_WORKER || 
					mChatBean.getDir() == QAODefine.MSG_DIR_FROM_WAITING) {
				mVoiceIv.setBackgroundResource(R.drawable.chat_animation_left_gray3);
			} else if (mChatBean.getDir() == QAODefine.MSG_DIR_FROM_ROBOT ||
					mChatBean.getDir() == QAODefine.MSG_DIR_TO_CUSTOMER) {
				mVoiceIv.setBackgroundResource(R.drawable.chat_animation_right_white3);
			}
		}
		
		private void startPlaying(V5VoiceMessage voiceMessage, OnCompletionListener completionListener) {
	    	Logger.i(TAG, "MediaPlayer - startPlaying " + mPosition);
	    	if (mPlayer != null) {
	    		if (mPlayer.isPlaying()) {
	    			mPlayer.stop();
	    		}
	    		mPlayer.release();
	    		mPlayer = null;
	    		Logger.i(TAG, "MediaPlayer - stopPlaying all others");
	    		resetOtherItemsExcept(mChatBean);
	    	}
	    	mPlayer = new MediaPlayer();
	        try {
	            mPlayer.setDataSource(voiceMessage.getFilePath());
	            mPlayer.prepare();
	            mPlayer.start();
	            mPlayer.setOnErrorListener(new OnErrorListener() {
					
					@Override
					public boolean onError(MediaPlayer mp, int what, int extra) {
						Logger.e(TAG, "MediaPlayer - onError");
						return false;
					}
				});
	            mPlayer.setOnCompletionListener(completionListener);
				updateVoiceStartPlayingState();
	        } catch (IOException e) {
	            Logger.e(TAG, "MediaPlayer prepare() failed");
	            mPlayer.release();
	    		mPlayer = null;
	    		updateVoiceStopPlayingState(); // UI
	        }
	    }
	    
	    private void stopPlaying() {
	    	Logger.i(TAG, "MediaPlayer - stopPlayer " + mPosition);
	    	if (mPlayer != null) {
	    		mPlayer.stop();
	        	mPlayer.release();
	        	mPlayer = null;
	    	}
	        updateVoiceStopPlayingState();
	    }

		public void setPosition(int mPosition) {
			this.mPosition = mPosition;
		}
    }
    
    private void resetOtherItemsExcept(ChatRecyclerBean chatBean) {
    	Logger.d(TAG, "resetOtherItems");
		for (ChatRecyclerBean bean : this.mRecycleBeans) {
			bean.setVoicePlaying(false);
		}
		chatBean.setVoicePlaying(true);
		notifyDataSetChanged();
	}
    
    public void stopVoicePlaying() {
    	if (mPlayer != null) {
    		if (mPlayer.isPlaying()) {
    			mPlayer.stop();
    		}
    		mPlayer.release();
    		mPlayer = null;
    	}
    }
    
}
