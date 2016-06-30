package com.v5kf.client.ui;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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

import com.v5kf.client.lib.Logger;
import com.v5kf.client.lib.entity.V5ArticleBean;
import com.v5kf.client.lib.entity.V5ArticlesMessage;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5LocationMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MessageDefine;
import com.v5kf.client.lib.entity.V5VoiceMessage;
import com.v5kf.client.ui.ClientListLinearLayout.OnListLayoutClickListener;
import com.v5kf.client.ui.emojicon.EmojiconTextView;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.MessageRequest;
import com.v5kf.mcss.service.CoreService;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.utils.DateUtil;
import com.v5kf.mcss.utils.FileUtil;
import com.v5kf.mcss.utils.MapUtil;
import com.v5kf.mcss.utils.cache.ImageLoader;
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
public class ClientChatRecyclerAdapter extends RecyclerView.Adapter<ClientChatRecyclerAdapter.ChatItemViewHolder> {

	public static final int TYPE_LEFT_TEXT = 0;
	public static final int TYPE_RIGHT_TEXT = 1;
	public static final int TYPE_SINGLE_NEWS = 2;
	public static final int TYPE_NEWS = 3;
	public static final int TYPE_LOCATION_R = 4;
	public static final int TYPE_LOCATION_L = 5;
	public static final int TYPE_IMG_L = 6;
	public static final int TYPE_IMG_R = 7;
	public static final int TYPE_VOICE_L = 8;
	public static final int TYPE_VOICE_R = 9;
	public static final int TYPE_TIPS = 10;
	protected static final String TAG = "OnChatRecyclerAdapter";
	private LayoutInflater mInflater;
	private List<V5ChatBean> mRecycleBeans ;
	private ActivityBase mContext;
	private OnRecyclerClickListener mListener;
	// 语音
	private MediaPlayer mPlayer;
	
    public ClientChatRecyclerAdapter(ActivityBase context, List<V5ChatBean> mRecycleBeans, OnRecyclerClickListener listener) {
        super();
        this.mRecycleBeans = mRecycleBeans;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mListener = listener;
        
    }
    

    @Override
    public int getItemViewType(int position) {
    	V5Message msgContent = mRecycleBeans.get(position).getMessage();
    	if (msgContent == null) {
    		return 0;
    	}
    	int msgType = msgContent.getMessage_type();
    	int msgDir = msgContent.getDirection();
    	
    	if (msgType == V5MessageDefine.MSG_TYPE_ARTICLES) {
    		V5ArticlesMessage msg = (V5ArticlesMessage) msgContent;
    		if (msg != null && msg.getArticles() != null) {
	    		if (msg.getArticles().size() == 1) {
	    			return TYPE_SINGLE_NEWS;
	    		} else if (msg.getArticles().size() > 1) {
	    			return TYPE_NEWS;
	    		}
    		}
    	} else if (msgType == V5MessageDefine.MSG_TYPE_LOCATION) {
    		if (msgDir == V5MessageDefine.MSG_DIR_TO_CUSTOMER || 
    				msgDir == V5MessageDefine.MSG_DIR_FROM_ROBOT) {
    			return TYPE_LOCATION_L;
    		} else if (msgDir == V5MessageDefine.MSG_DIR_TO_WORKER) {
    			return TYPE_LOCATION_R;
    		}
    	} else if (msgType == V5MessageDefine.MSG_TYPE_IMAGE) {
    		if (msgDir == V5MessageDefine.MSG_DIR_TO_WORKER) {
    			return TYPE_IMG_R;
    		} else if (msgDir == V5MessageDefine.MSG_DIR_TO_CUSTOMER ||
    				msgDir == V5MessageDefine.MSG_DIR_FROM_ROBOT) {
    			return TYPE_IMG_L;
    		}  			
    	} else if (msgType == V5MessageDefine.MSG_TYPE_VOICE) {
    		if (msgDir == V5MessageDefine.MSG_DIR_TO_WORKER) {
    			return TYPE_VOICE_R;
    		} else if (msgDir == V5MessageDefine.MSG_DIR_TO_CUSTOMER ||
    				msgDir == V5MessageDefine.MSG_DIR_FROM_ROBOT) {
    			return TYPE_VOICE_L;
    		}  			
    	} else if (msgType > V5MessageDefine.MSG_TYPE_APP_URL) {
    		return TYPE_TIPS;
    	}
    	
		if (msgDir == V5MessageDefine.MSG_DIR_TO_WORKER) {
			return TYPE_RIGHT_TEXT;
		} else { // V5MessageDefine.MSG_DIR_TO_CUSTOMER 或 V5MessageDefine.MSG_DIR_FROM_ROBOT
			return TYPE_LEFT_TEXT;
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
        	itemView = mInflater.inflate(R.layout.v5client_item_chat_multi_news, parent, false);
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
    public void onBindViewHolder(ChatItemViewHolder holder, int position) {
    	// 设置数据
        final V5Message messageContent = mRecycleBeans.get(position).getMessage();
        if (null == messageContent) {
			return;
		}

		holder.setPosition(position);
		
        // 显示时间：第一条和时间差相差5min以上
		if (position == 0 || 
				(messageContent.getCreate_time() - mRecycleBeans.get(position - 1).getMessage().getCreate_time()) > 300) {
			holder.mDate.setVisibility(View.VISIBLE);
			holder.mDate.setText(
					DateUtil.timeFormat(messageContent.getCreate_time() * 1000, false));
		} else {
			holder.mDate.setVisibility(View.GONE);
		}
		
		switch (getItemViewType(position)) {
			case TYPE_SINGLE_NEWS: { // 单图文
				V5ArticlesMessage articleMsg = (V5ArticlesMessage) messageContent;
				V5ArticleBean article = (V5ArticleBean) articleMsg.getArticles().get(0);
				holder.mNewsTitle.setText(article.getTitle());
				holder.mNewsContent.setText(article.getDescription());
				ImageLoader imgLoader = new ImageLoader(mContext, true, R.drawable.v5_img_src_loading, null);
	        	imgLoader.DisplayImage(article.getPic_url(), holder.mNewsPic);
	        	Logger.d(TAG, "单图文消息 url：" + article.getPic_url());
			}
			break;
			
			case TYPE_NEWS: { // 多图文
				V5ArticlesMessage articleMsg = (V5ArticlesMessage) messageContent;
				holder.mNewsAdapter = new NewsListAdapter(
						mContext, 
						articleMsg.getArticles(),
						true);
				holder.mNewsListLayout.bindLinearLayout(holder.mNewsAdapter);
			}
			break;
			
			case TYPE_LOCATION_R: { // 位置-发出
				V5LocationMessage locationMsg = (V5LocationMessage) messageContent;
				ImageLoader mapImgLoader = new ImageLoader(mContext, true, R.drawable.v5_img_src_loading, null);
	        	double lat = locationMsg.getX();
	        	double lng = locationMsg.getY();
	        	String url = String.format(Locale.CHINA, Config.MAP_PIC_API_FORMAT, lat, lng, lat, lng);
	        	
	        	// 异步加载图片和位置描述信息
	        	mapImgLoader.DisplayImage(url, holder.mMapIv);
	        	holder.mLbsDescTv.setText(mContext.getString(R.string.loading));
	        	MapUtil.getLocationTitle(lat, lng, holder.mLbsDescTv, null);
			}
			break;
			
			case TYPE_LOCATION_L: { // 位置-接收
				V5LocationMessage locationMsg = (V5LocationMessage) messageContent;
				ImageLoader mapImgLoader = new ImageLoader(mContext, true, R.drawable.v5_img_src_loading, null);
	        	double lat = locationMsg.getX();
	        	double lng = locationMsg.getY();
	        	String url = String.format(Locale.CHINA, Config.MAP_PIC_API_FORMAT, lat, lng, lat, lng);
	        	Logger.i(TAG, "[地图] URL:" + url);
	        	
	        	// 异步加载图片和位置描述信息
	        	mapImgLoader.DisplayImage(url, holder.mMapIv);
	        	holder.mLbsDescTv.setText(mContext.getString(R.string.loading));
	        	MapUtil.getLocationTitle(lat, lng, holder.mLbsDescTv, null);
			}
			break;
			
			case TYPE_IMG_R:
			case TYPE_IMG_L: {
				V5ImageMessage imageMsg = (V5ImageMessage) messageContent;
				ImageLoader mapImgLoader = new ImageLoader(mContext, true, R.drawable.v5_img_src_loading, null);
	        	String urlPath = imageMsg.getFilePath();
				if (TextUtils.isEmpty(urlPath)) { // 优先判断本地图片，否则加载网络图片
					urlPath = imageMsg.getThumbnailPicUrl();
				}
	        	mapImgLoader.DisplayImage(urlPath, holder.mMapIv);
			}
			break;
			
			case TYPE_VOICE_L:
			case TYPE_VOICE_R: { // 语音
				final V5VoiceMessage voiceMessage = (V5VoiceMessage)messageContent;
				Logger.d(TAG, "list load Voice ----- duration:" + voiceMessage.getDuration());
	        	holder.mVoiceSecondTv.setText(String.format("%.1f″", voiceMessage.getDuration()/1000.0f));
	        	// 语音状态
	        	if (mRecycleBeans.get(position).isVoicePlaying()) {
	        		holder.updateVoiceStartPlayingState();
	        	} else {
	        		holder.updateVoiceStopPlayingState();
	        	}
	        	// 背景
	        	if (messageContent.getDirection() == V5MessageDefine.MSG_DIR_TO_WORKER) {
	        		holder.mVoiceSecondTv.setTextColor(mContext.getResources().getColor(R.color.black));
	        	} else {
	        		holder.mVoiceSecondTv.setTextColor(mContext.getResources().getColor(R.color.white));
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
	        		if (voiceMessage.getFilePath() != null) {
	        			url = voiceMessage.getFilePath();
	        		}
	        	}
	        	Logger.d(TAG, "Voice url:" + url + " sendState:" + voiceMessage.getState());
	        	MediaLoader mediaLoader = new MediaLoader(mContext, holder, new MediaLoaderListener() {
					
					@Override
					public void onSuccess(V5Message msg, Object obj, MediaCache media) {
						//((V5VoiceMessage)msg).setFilePath(media.getLocalPath());
						//((V5VoiceMessage)msg).setDuration(media.getDuration());
//						if (((V5VoiceMessage)msg).getFilePath() == null) { // 非本地文件
//							msg.setState(V5Message.STATE_ARRIVED);
//							//sendStateChange(holder, voiceMessage);
//							notifyDataSetChanged();
//						}
						((ChatItemViewHolder)obj).mVoiceSecondTv.setText(String.format("%.1f″", media.getDuration()/1000.0f));
					}
					
					@Override
					public void onFailure(MediaLoader mediaLoader, V5Message msg, Object obj) {
//						if (((V5VoiceMessage)msg).getFilePath() == null) { // 非本地文件
//							msg.setState(V5Message.STATE_FAILURE);
//							//sendStateChange(holder, voiceMessage);
//						}
						notifyDataSetChanged();
					}
				});
	        	mediaLoader.loadMedia(url, voiceMessage, null);
			}
				break;
			
			case TYPE_TIPS:
				holder.mMsg.setText(messageContent.getDefaultContent(mContext));
				break;
			
			default: {
				String str = messageContent.getDefaultContent(mContext) == null ? "" : messageContent.getDefaultContent(mContext);
//				Logger.d(TAG, "对话：" + str + " 方向:" + message.getMsg_content().getDirection());
		    	CharSequence text = Html.fromHtml(str.replace("\n", "<br>"));
				Logger.d(TAG, "<消息>：" + text);
				holder.mMsg.setText(text);
				holder.mMsg.setMovementMethod(LinkMovementMethod.getInstance());
				
				if (messageContent.getDirection() == V5MessageDefine.MSG_DIR_TO_WORKER) {
					holder.mMsg.setBackgroundResource(R.drawable.v5_list_to_customer_bg);
					holder.mMsg.setTextColor(mContext.getResources().getColor(R.color.black));
				} else if (messageContent.getDirection() == V5MessageDefine.MSG_DIR_FROM_ROBOT
						|| messageContent.getDirection() == V5MessageDefine.MSG_DIR_COMMENT) {
					holder.mMsg.setBackgroundResource(R.drawable.v5_list_from_robot_bg);
					holder.mMsg.setTextColor(mContext.getResources().getColor(R.color.white));
				} else if (messageContent.getDirection() == V5MessageDefine.MSG_DIR_TO_CUSTOMER) {
					holder.mMsg.setBackgroundResource(R.drawable.v5_list_from_worker_bg);
					holder.mMsg.setTextColor(mContext.getResources().getColor(R.color.white));
				}
			}
			break;
		}
		
		if (holder.mBubbleLayout != null) {
			// 背景
			if (messageContent.getDirection() == V5MessageDefine.MSG_DIR_FROM_ROBOT ||
					messageContent.getDirection() == V5MessageDefine.MSG_DIR_COMMENT) {
				holder.mBubbleLayout.setBackgroundResource(R.drawable.v5_list_from_robot_bg);
			} else if (messageContent.getDirection() == V5MessageDefine.MSG_DIR_TO_WORKER) {
				holder.mBubbleLayout.setBackgroundResource(R.drawable.v5_list_to_customer_bg);
			} else if (messageContent.getDirection() == V5MessageDefine.MSG_DIR_TO_CUSTOMER) {
				holder.mBubbleLayout.setBackgroundResource(R.drawable.v5_list_from_worker_bg);
			}
		}
		
		if (holder.mSendFailedIv != null && holder.mSendingPb != null) {
        	if (messageContent.getDirection() == V5MessageDefine.MSG_DIR_TO_WORKER) {
        		if (messageContent.getState() == V5Message.STATE_FAILURE) {
        			holder.mSendingPb.setVisibility(View.GONE);
        			holder.mSendFailedIv.setVisibility(View.VISIBLE);
        		} else if (messageContent.getState() == V5Message.STATE_SENDING) {
        			holder.mSendingPb.setVisibility(View.VISIBLE);
        			holder.mSendFailedIv.setVisibility(View.GONE);
        		} else {
        			holder.mSendFailedIv.setVisibility(View.GONE);
            		holder.mSendingPb.setVisibility(View.GONE);
        		}
        	} else {
        		holder.mSendFailedIv.setVisibility(View.GONE);
        		holder.mSendingPb.setVisibility(View.GONE);
        	}
        	holder.mSendFailedIv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// 隐藏重发IV，显示ProgressBar
					CoreService.reConnect(mContext);
					
					try {
						/* 发送Message请求 */
						MessageRequest mReq = (MessageRequest) RequestManager.getRequest(QAODefine.O_TYPE_MESSAGE, mContext);
						mReq.sendMessage(messageContent);
						
						if (CoreService.isConnected()) {
							messageContent.setState(V5Message.STATE_ARRIVED);
						} else {
							messageContent.setState(V5Message.STATE_FAILURE);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					notifyDataSetChanged();
				}
			});
        }
    }

    @Override
    public int getItemCount() {
        return mRecycleBeans.size();
    }
    
    private void sendStateChange(ChatItemViewHolder holder, V5Message chatMessage) {
    	if (holder.mSendFailedIv != null && holder.mSendingPb != null) {
    		if (chatMessage.getState() == V5Message.STATE_FAILURE) {
    			holder.mSendingPb.setVisibility(View.GONE);
    			holder.mSendFailedIv.setVisibility(View.VISIBLE);
    		} else if (chatMessage.getState() == V5Message.STATE_SENDING) {
    			holder.mSendingPb.setVisibility(View.VISIBLE);
    			holder.mSendFailedIv.setVisibility(View.GONE);
    		} else {
    			holder.mSendFailedIv.setVisibility(View.GONE);
        		holder.mSendingPb.setVisibility(View.GONE);
    		}
        }
    }

    
    class ChatItemViewHolder extends RecyclerView.ViewHolder implements OnClickListener, OnLongClickListener{

    	private static final String TAG = "OnChatRecyclerAdapter.ChatItemViewHolder";
    	private int mPosition;
    	
        public TextView mDate;
        public TextView mMsg;
        public ImageView mSendFailedIv;
		public ProgressBar mSendingPb;
        
        /* 单图文news */
        public TextView mNewsTitle;
        public TextView mNewsContent;
		public ImageView mNewsPic;
        
        /* 多图文 */
		public ClientListLinearLayout mNewsListLayout;
        public NewsListAdapter mNewsAdapter;
        
        /* 位置 */
        public ImageView mMapIv;
        public TextView mLbsDescTv;
        
        /* 图片 */
        // mMapIv
        
        /* 语音 */
        public ImageView mVoiceIv;
        public TextView mVoiceSecondTv;
        public AnimationDrawable mVoiceAnimDrawable;
        
        public View mBubbleLayout;

        public ChatItemViewHolder(int viewType, View itemView) {
            super(itemView);
            
            mDate = (TextView) itemView.findViewById(R.id.id_chat_msg_date);
            switch (viewType) {
            case TYPE_LEFT_TEXT:
            	mMsg = (EmojiconTextView) itemView.findViewById(R.id.id_from_msg_text);
            	mMsg.setOnClickListener(this);
            	break;
            	
            case TYPE_RIGHT_TEXT:
            	mMsg = (EmojiconTextView) itemView.findViewById(R.id.id_to_msg_text);
            	mSendFailedIv = (ImageView) itemView.findViewById(R.id.id_msg_fail_iv);
            	mSendingPb = (ProgressBar) itemView.findViewById(R.id.id_msg_out_pb);
            	mMsg.setOnClickListener(this);
            	break;
            	
            case TYPE_SINGLE_NEWS:
            	mNewsPic = (ImageView) itemView.findViewById(R.id.chat_item_news_img);
            	mNewsTitle = (TextView) itemView.findViewById(R.id.id_news_title_inner_text);
            	mNewsContent = (TextView) itemView.findViewById(R.id.id_news_desc_text);
            	itemView.findViewById(R.id.id_news_layout).setOnClickListener(this);
            	break;
            	
            case TYPE_NEWS:
            	mNewsListLayout = (ClientListLinearLayout) itemView.findViewById(R.id.id_news_layout);
            	mNewsListLayout.setOnListLayoutClickListener(new OnListLayoutClickListener() {
					
					@Override
					public void onListLayoutClick(View v, int pos) {
						if (mListener != null) {
							mListener.onMultiNewsClick(v, getAdapterPosition(), getItemViewType(), pos);
						}
					}
				});
            	break;
            	
            case TYPE_LOCATION_R:
            	mMapIv = (ImageView) itemView.findViewById(R.id.ic_map_img_iv);
            	mLbsDescTv = (TextView) itemView.findViewById(R.id.id_map_address_text);
            	mSendFailedIv = (ImageView) itemView.findViewById(R.id.id_msg_fail_iv);
            	mSendingPb = (ProgressBar) itemView.findViewById(R.id.id_msg_out_pb);
            	//mBubbleLayout = itemView.findViewById(R.id.id_right_location_layout);
            	mMapIv.setOnClickListener(this);
            	break;
            	
            case TYPE_LOCATION_L:
            	mMapIv = (ImageView) itemView.findViewById(R.id.ic_map_img_iv);
            	mLbsDescTv = (TextView) itemView.findViewById(R.id.id_map_address_text);
            	//mBubbleLayout = itemView.findViewById(R.id.id_left_location_layout);
            	mMapIv.setOnClickListener(this);
            	break;
            	
            case TYPE_IMG_L:
            	mMapIv = (ImageView) itemView.findViewById(R.id.ic_type_img_iv);
            	//mBubbleLayout = itemView.findViewById(R.id.id_left_image_layout);
            	mMapIv.setOnClickListener(this);
            	break;

            case TYPE_IMG_R:
            	mMapIv = (ImageView) itemView.findViewById(R.id.ic_type_img_iv);
            	mSendFailedIv = (ImageView) itemView.findViewById(R.id.id_msg_fail_iv);
            	mSendingPb = (ProgressBar) itemView.findViewById(R.id.id_msg_out_pb);
            	//mBubbleLayout = itemView.findViewById(R.id.id_right_image_layout);
            	mMapIv.setOnClickListener(this);
            	//mBubbleLayout.setOnClickListener(this);
            	break;
            
            case TYPE_VOICE_L:
            	mBubbleLayout = (View) itemView.findViewById(R.id.id_left_voice_layout);
            	mVoiceIv = (ImageView) itemView.findViewById(R.id.id_from_voice_iv);
            	mVoiceSecondTv = (TextView) itemView.findViewById(R.id.id_from_voice_tv);
            	mBubbleLayout.setOnClickListener(this);
            	break;
            	
            case TYPE_VOICE_R:
            	mBubbleLayout = (View) itemView.findViewById(R.id.id_right_voice_layout);
            	mVoiceIv = (ImageView) itemView.findViewById(R.id.id_to_voice_iv);
            	mVoiceSecondTv = (TextView) itemView.findViewById(R.id.id_to_voice_tv);
            	
            	mSendFailedIv = (ImageView) itemView.findViewById(R.id.id_msg_fail_iv);
            	mSendingPb = (ProgressBar) itemView.findViewById(R.id.id_msg_out_pb);
            	mBubbleLayout.setOnClickListener(this);
            	break;
            	
            case TYPE_TIPS:
            	mMsg = (TextView) itemView.findViewById(R.id.id_msg_tips);
            	break;
            	
            default:
            	mMsg = (EmojiconTextView) itemView.findViewById(R.id.id_to_msg_text);
                mMsg.setOnClickListener(this);
            	break;
            }
            
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            
            if (mMsg instanceof EmojiconTextView) { // URL点击事件
            	((EmojiconTextView)mMsg).setURLClickListener(new EmojiconTextView.OnURLClickListener() {
					
					@Override
					public void onClick(View v, String url) {
						mContext.gotoWebViewActivity(url);
					}
				});
            }
        }

		@Override
		public void onClick(View v) {
			Logger.d(TAG, "item onCLick View.id=" + v.getId());
			V5Message msg = mRecycleBeans.get(mPosition).getMessage();
			switch (v.getId()) {
			case R.id.id_left_voice_layout:
			case R.id.id_right_voice_layout:
				if (msg.getMessage_type() == V5MessageDefine.MSG_TYPE_VOICE) {
					if (mRecycleBeans.get(mPosition).isVoicePlaying()) { // 停止播放
						stopPlaying();
					} else { // 开始播放
						if (mVoiceAnimDrawable != null) {
							mVoiceAnimDrawable.stop();
						}
						startPlaying((V5VoiceMessage)msg, new OnCompletionListener() {
							
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
				break;
			}
			
			if (mListener != null) {
				mListener.onItemClick(v, mPosition, getItemViewType());
				return;
			}
		}

		@Override
		public boolean onLongClick(View v) {
			Logger.d(TAG, "item onLongCLick View.id=" + v.getId());
			if (mListener != null) {
				mListener.onItemLongClick(v, mPosition, getItemViewType());
				return true;
			}
			return false;
		}
		
		public void updateVoiceStartPlayingState() {
			Logger.i(TAG, "UI - updateVoiceStartPlayingState " + mPosition);
			V5Message msg = mRecycleBeans.get(mPosition).getMessage();
			mRecycleBeans.get(mPosition).setVoicePlaying(true);
			mVoiceIv.setBackgroundResource(R.anim.anim_leftwhite_voice);
			if (msg.getDirection() == V5MessageDefine.MSG_DIR_TO_WORKER) {
				mVoiceIv.setBackgroundResource(R.anim.anim_rightgray_voice);
			} else if (msg.getDirection() == V5MessageDefine.MSG_DIR_FROM_ROBOT ||
					msg.getDirection() == V5MessageDefine.MSG_DIR_TO_CUSTOMER) {
				mVoiceIv.setBackgroundResource(R.anim.anim_leftwhite_voice);
			}
			mVoiceAnimDrawable = (AnimationDrawable)mVoiceIv.getBackground();
			mVoiceAnimDrawable.start();
		}
		
		public void updateVoiceStopPlayingState() {
			Logger.i(TAG, "UI - updateVoiceStopPlayingState " + mPosition);
			V5Message msg = mRecycleBeans.get(mPosition).getMessage();
			mRecycleBeans.get(mPosition).setVoicePlaying(false);
			if (mVoiceAnimDrawable != null) {
				mVoiceAnimDrawable.stop();
				mVoiceAnimDrawable = null;
			}
			if (msg.getDirection() == V5MessageDefine.MSG_DIR_TO_WORKER) {
				mVoiceIv.setBackgroundResource(R.drawable.chat_animation_right_gray3);
			} else if (msg.getDirection() == V5MessageDefine.MSG_DIR_FROM_ROBOT ||
					msg.getDirection() == V5MessageDefine.MSG_DIR_TO_CUSTOMER) {
				mVoiceIv.setBackgroundResource(R.drawable.chat_animation_left_white3);
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
	    		resetOtherItemsExcept(mRecycleBeans.get(mPosition));
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
				updateVoiceStartPlayingState(); // UI
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
    
    private void resetOtherItemsExcept(V5ChatBean bean) {
    	Logger.d(TAG, "resetOtherItems");
		for (V5ChatBean chatBean : mRecycleBeans) {
			chatBean.setVoicePlaying(false);
		}
		bean.setVoicePlaying(true);
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
