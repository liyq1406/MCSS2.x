package com.v5kf.mcss.ui.adapter;

import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
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
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.ui.activity.md2x.LocationMapActivity;
import com.v5kf.mcss.ui.activity.md2x.WebViewActivity;
import com.v5kf.mcss.ui.entity.ChatRecyclerBean;
import com.v5kf.mcss.ui.widget.ListLinearLayout;
import com.v5kf.mcss.ui.widget.ListLinearLayout.OnListLayoutClickListener;
import com.v5kf.mcss.utils.DateUtil;
import com.v5kf.mcss.utils.FileUtil;
import com.v5kf.mcss.utils.IntentUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.MapUtil;
import com.v5kf.mcss.utils.UITools;
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
public class HistoryMessagesAdapter extends RecyclerView.Adapter<HistoryMessagesAdapter.ViewHolder> {

	protected static final String TAG = "HistoryMessagesAdapter";
	private static final int TYPE_SINGLE_NEWS = 99;
	private static final int TYPE_NEWS = 199;
	private LayoutInflater mInflater;
	private List<ChatRecyclerBean> mRecycleBeans ;
	private ActivityBase mActivity;
	
	// 语音
	private MediaPlayer mPlayer;
	
    public HistoryMessagesAdapter(ActivityBase activity, List<ChatRecyclerBean> mRecycleBeans) {
        super();
        this.mRecycleBeans = mRecycleBeans;
        this.mActivity = activity;
        mInflater = LayoutInflater.from(activity);
    }
    
    @Override
    public long getItemId(int position) {
    	return position;
    }
    
    @Override
    public int getItemViewType(int position) {
        ChatRecyclerBean chatMessage = mRecycleBeans.get(position);
        if (chatMessage.getMessage() == null) {
        	return QAODefine.MSG_TYPE_NULL;
        }
        
        int msgType = chatMessage.getMessage().getMessage_type();
        if (msgType == QAODefine.MSG_TYPE_NEWS) {
        	V5Message msgContent = mRecycleBeans.get(position).getMessage();
    		if (msgContent != null && ((V5ArticlesMessage)msgContent).getArticles() != null) {
	    		if (((V5ArticlesMessage)msgContent).getArticles().size() == 1) { // 单图文
	    			return TYPE_SINGLE_NEWS;
	    		} else if (((V5ArticlesMessage)msgContent).getArticles().size() > 1) { // 多图文
	    			return TYPE_NEWS;
	    		}
    		}
        }
        
    	return msgType; //msgType
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    	ViewHolder viewHolder = null;
    	View itemView = null;
    	switch (viewType) {
    	case QAODefine.MSG_TYPE_VOICE:
    		itemView = mInflater.inflate(R.layout.item_history_msg_voice, parent, false) ;
    		break;
    	
        case QAODefine.MSG_TYPE_IMAGE:
        	itemView = mInflater.inflate(R.layout.item_history_msg_img, parent, false) ;
        	break;
        	
        case QAODefine.MSG_TYPE_LOCATION:
        	itemView = mInflater.inflate(R.layout.item_history_msg_location, parent, false);
        	break;
        	
        case TYPE_NEWS:
        	itemView = mInflater.inflate(R.layout.item_history_msg_multi_news, parent, false);
        	break;
        
        case TYPE_SINGLE_NEWS:
        	itemView = mInflater.inflate(R.layout.item_history_msg_single_news, parent, false);
        	break;
        	
        case QAODefine.MSG_TYPE_TEXT:
        case QAODefine.MSG_TYPE_NULL:
        default:
        	itemView = mInflater.inflate(R.layout.item_history_msg_text, parent, false) ;
        	break;
    	}
    	
    	viewHolder = new ViewHolder(itemView, viewType);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
    	// 设置数据
        ChatRecyclerBean chatBean = mRecycleBeans.get(position);
        holder.setChatBean(chatBean);
        V5Message msgContent = chatBean.getMessage();
        
        switch (getItemViewType(position)) {
        case QAODefine.MSG_TYPE_VOICE: {
        	V5VoiceMessage voiceMessage = (V5VoiceMessage)msgContent;
			Logger.d(TAG, "list load Voice ----- duration:" + voiceMessage.getDuration());
        	holder.mVoiceSecondTv.setText(String.format("%.1f″", voiceMessage.getDuration()/1000.0f));
        	// 语音状态
        	if (chatBean.isVoicePlaying()) {
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
        	
        	String url = voiceMessage.getUrl();
        	if (TextUtils.isEmpty(url)) {
        		if (TextUtils.isEmpty(voiceMessage.getMessage_id()) && voiceMessage.getFilePath() != null) {
        			url = voiceMessage.getFilePath();
        		} else {
        			url = String.format(Config.APP_RESOURCE_V5_FMT, Config.SITE_ID, msgContent.getMessage_id());
        			voiceMessage.setUrl(url);
        		}
        	}
        	
        	MediaLoader mediaLoader = new MediaLoader(mActivity, holder, new MediaLoaderListener() {
				
				@Override
				public void onSuccess(V5Message msg, Object obj, MediaCache media) {
					((V5VoiceMessage)msg).setFilePath(media.getLocalPath());
					((V5VoiceMessage)msg).setDuration(media.getDuration());
					msg.setState(V5Message.STATE_ARRIVED);
					((ViewHolder)obj).mVoiceSecondTv.setText(String.format("%.1f″", media.getDuration()/1000.0f));
				}
				
				@Override
				public void onFailure(MediaLoader mediaLoader, V5Message msg, Object obj) {
					msg.setState(V5Message.STATE_FAILURE);
					// [历史语音消息不需要重试，会话中语音需要重试获取media_id]
//					if (mediaLoader.getUrl().contains("chat.v5kf.com/") 
//							&& mediaLoader.getmTryTimes() < 5) { // 最多重试5次
//						mActivity.getHandler().postDelayed(new Runnable() {
//							public void run() {
//								mediaLoader.loadMedia(mediaLoader.getUrl(), msg, null);
//							}
//						}, 500); // 0.5s后重试
//					}
					notifyDataSetChanged();
				}
			});
        	mediaLoader.loadMedia(url, msgContent, null);
        }
        	break;
        
        case QAODefine.MSG_TYPE_IMAGE: {
        	ImageLoader mapImgLoader = new ImageLoader(mActivity, true, R.drawable.v5_img_src_loading);
        	String urlPath = UITools.getThumbnailUrlOfImage((V5ImageMessage)msgContent, Config.SITE_ID);
        	mapImgLoader.DisplayImage(urlPath, holder.mImgIv);
        }
        	break;
        	
        case QAODefine.MSG_TYPE_LOCATION: {
        	ImageLoader mapImgLoader = new ImageLoader(mActivity, true, R.drawable.v5_img_src_loading);
        	double lat = ((V5LocationMessage)msgContent).getX();
        	double lng = ((V5LocationMessage)msgContent).getY();
        	String url = String.format(Locale.getDefault(), Config.MAP_PIC_API_FORMAT, lat, lng, lat, lng);
        	// url = MD5.encode(chatMessage.getMessage().getMsg_content().getLabel());
        	mapImgLoader.DisplayImage(url, holder.mImgIv);
        	holder.mLocationTv.setText(mActivity.getString(R.string.loading));
        	MapUtil.getLocationTitle(lat, lng, holder.mLocationTv);
		}
        	break;
        	
        case TYPE_NEWS: // 多图文
        	holder.mNewsAdapter = new NewsListAdapter(
					mActivity, 
					((V5ArticlesMessage)msgContent).getArticles(),
					true);
			holder.mNewsListLayout.bindLinearLayout(holder.mNewsAdapter);
			break;
        	
        case TYPE_SINGLE_NEWS: // 单图文
        	V5ArticleBean article = ((V5ArticlesMessage)msgContent).getArticles().get(0);
			if (article == null) {
				Logger.e(TAG, "TYPE_SINGLE_NEWS got null article");
				break;
			}
			holder.mNewsTitle.setText(article.getTitle());
			holder.mNewsContent.setText(article.getDescription());
			
			ImageLoader imgLoader = new ImageLoader(mActivity, true, R.drawable.v5_img_src_loading);
        	imgLoader.DisplayImage(article.getPic_url(), holder.mNewsPic);
        	break;
        	
        case QAODefine.MSG_TYPE_TEXT:
        case QAODefine.MSG_TYPE_NULL:
        default: {
        	String str = chatBean.getDefaultContent(mActivity) == null ? "" : chatBean.getDefaultContent(mActivity);
//			str = str.replaceAll("&amp;", "&"); // 去除重复的HTML实体符号
        	
        	/* URLCLICKSPAN */
//        	Spannable textWithLinkText = (Spannable) Html.fromHtml(str.replace("\n", "<br>"));
//        	int end = textWithLinkText.length();
//        	URLSpan[] urls = textWithLinkText.getSpans(0, end, URLSpan.class);
//        	SpannableStringBuilder style = new SpannableStringBuilder(textWithLinkText);  
//            style.clearSpans();
//            for (URLSpan url : urls) {
//                UrlClickSpan myURLSpan = new UrlClickSpan(url.getURL());  
//                style.setSpan(myURLSpan, textWithLinkText.getSpanStart(url), textWithLinkText.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
//            }
//            holder.mMsg.setText(style);
//    		holder.mMsg.setMovementMethod(LinkMovementMethod.getInstance());
        	
    		Spanned text = Html.fromHtml(str.replace("\n", "<br>"));
    		holder.mMsg.setText(text);
    		holder.mMsg.setMovementMethod(LinkMovementMethod.getInstance());
        }
        	break;
        }
		
		switch (chatBean.getMessage().getDirection()) {
		case QAODefine.MSG_DIR_TO_CUSTOMER:
			holder.mName.setTextColor(Color.rgb(0xC8, 0x6F, 0x10));//c86f10
			break;
			
		case QAODefine.MSG_DIR_FROM_ROBOT:
			holder.mName.setTextColor(Color.rgb(0x20, 0x7C, 0xC4));//207cc4
			break;
			
		case QAODefine.MSG_DIR_TO_WORKER:
			holder.mName.setTextColor(Color.rgb(0x57, 0xAF, 0x13));//57af13
			break;
		}
		holder.mDate.setText(
				DateUtil.timeFormat(chatBean.getDefaultTime(), false));
        holder.mName.setText(chatBean.getName());
    }

    @Override
    public int getItemCount() {
        return mRecycleBeans.size();
    }

    
    class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener, OnLongClickListener{

		private static final String TAG = "HistoryMessagesAdapter.ViewHolder";
    	
    	private ChatRecyclerBean mChatBean;
//    	private int mType;
        public TextView mName;
        public TextView mDate;
        
        /* 文本 */
        public EmojiconTextView mMsg;
        
        /* 图片 */
        public ImageView mImgIv;
        
        /* 语音 */
        public TextView mVoiceSecondTv;
        public ImageView mVoiceIv;
        public AnimationDrawable mVoiceAnimDrawable;
        
        /* 位置 */
        // 复用mImgIv
        public TextView mLocationTv;
        
        /* 单图文news */
        public TextView mNewsTitle;
        public TextView mNewsContent;
		public ImageView mNewsPic;
        
        /* 多图文 */
		public ListLinearLayout mNewsListLayout;
        public NewsListAdapter mNewsAdapter;

    	public ViewHolder(View arg0, int viewType) {
			super(arg0);
//    		mType = viewType;
			mName = (TextView) itemView.findViewById(R.id.id_user_name);
        	mDate = (TextView) itemView.findViewById(R.id.id_msg_date);
    		switch (viewType) {
    		case QAODefine.MSG_TYPE_VOICE:
    			mVoiceIv = (ImageView) itemView.findViewById(R.id.id_type_voice_iv);
    			mVoiceSecondTv = (TextView) itemView.findViewById(R.id.id_type_voice_tv);
    			itemView.findViewById(R.id.id_type_voice_layout).setOnClickListener(this);
    			break;
    		
    		case QAODefine.MSG_TYPE_IMAGE:
    			mImgIv = (ImageView) itemView.findViewById(R.id.ic_type_img_iv);
    			mImgIv.setOnClickListener(this);
    			break;
    			
    		case QAODefine.MSG_TYPE_LOCATION:
    			mLocationTv = (TextView) itemView.findViewById(R.id.id_map_address_text);
    			mImgIv = (ImageView) itemView.findViewById(R.id.ic_map_img_iv);
    			mImgIv.setOnClickListener(this);
    			break;
    		
    		case TYPE_NEWS: // 多图文
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
    			
    		case TYPE_SINGLE_NEWS: // 单图文
            	mNewsPic = (ImageView) itemView.findViewById(R.id.chat_item_news_img);
            	mNewsTitle = (TextView) itemView.findViewById(R.id.id_news_title_inner_text);
            	mNewsContent = (TextView) itemView.findViewById(R.id.id_news_desc_text);
            	itemView.findViewById(R.id.id_news_layout).setOnClickListener(this);
    			break;
    			
    		case QAODefine.MSG_TYPE_TEXT:
    		default:
    			mMsg = (EmojiconTextView) itemView.findViewById(R.id.id_msg_text);
//    	        mMsg.setOnLongClickListener(this);
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
    			break;
    		}
//	        itemView.setOnClickListener(this);
		}
    	
		@Override
		public void onClick(View v) {
			Logger.d(TAG, "item onCLick View.id=" + v.getId());
			if (null == mChatBean) {
				Logger.e(TAG, "ViewHolder has null ChatRecycleBean");
				return;
			}
			
			switch (v.getId()) {
			case R.id.ic_type_img_iv: // 点击图片
				gotoShowImageActivity(((V5ImageMessage)mChatBean.getMessage()).getDefaultPicUrl());
				break;
				
			case R.id.ic_map_img_iv: // 点击地图
				gotoLocationMapActivity(((V5LocationMessage)mChatBean.getMessage()).getX(), ((V5LocationMessage)mChatBean.getMessage()).getY());
				break;
			
			case R.id.id_news_layout: // 点击单图文
				String url = ((V5ArticlesMessage)mChatBean.getMessage()).
						getArticles().get(0).getUrl();
				if (url != null) {
					onSingleNewsClick(url);
				}
				break;
			case R.id.id_type_voice_layout: // 点击语音
				if (mChatBean.getMessage().getMessage_type() == QAODefine.MSG_TYPE_VOICE) { // 点击语音
//					resetOtherItems();					
					if (mChatBean.isVoicePlaying()) { // 停止播放
						stopPlaying();
					} else { // 开始播放
						if (mVoiceAnimDrawable != null) {
							mVoiceAnimDrawable.stop();
						}
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
				
			default:
				break;
			}
		}
		
		public void updateVoiceStartPlayingState() {
			Logger.i(TAG, "UI - updateVoiceStartPlayingState");
			mChatBean.setVoicePlaying(true);
			mVoiceIv.setBackgroundResource(R.anim.anim_leftgray_voice);
			mVoiceAnimDrawable = (AnimationDrawable)mVoiceIv.getBackground();
			mVoiceAnimDrawable.start();						
		}
		
		public void updateVoiceStopPlayingState() {
			Logger.i(TAG, "UI - updateVoiceStopPlayingState");
			mChatBean.setVoicePlaying(false);
			if (mVoiceAnimDrawable != null) {
				mVoiceAnimDrawable.stop();
				mVoiceAnimDrawable = null;
			}
			mVoiceIv.setBackgroundResource(R.drawable.chat_animation_left_gray3);
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
			switch (v.getId()) {
			
			}
			return true;
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

		public ChatRecyclerBean getChatBean() {
			return mChatBean;
		}

		public void setChatBean(ChatRecyclerBean mChatBean) {
			this.mChatBean = mChatBean;
		}
		
		private void startPlaying(V5VoiceMessage voiceMessage, OnCompletionListener completionListener) {
	    	Logger.i(TAG, "MediaPlayer - startPlaying ");
	    	if (mPlayer != null) {
	    		if (mPlayer.isPlaying()) {
	    			mPlayer.stop();
	    		}
	    		mPlayer.release();
	    		mPlayer = null;
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
	        } catch (Exception e) {
	            Logger.e(TAG, "MediaPlayer prepare() failed");
	            mPlayer.release();
	    		mPlayer = null;
	    		updateVoiceStopPlayingState(); // UI
	        }
	    }
	    
	    private void stopPlaying() {
	    	Logger.i(TAG, "MediaPlayer - stopPlayer ");
	    	if (mPlayer != null) {
		    	mPlayer.stop();
		        mPlayer.release();
		        mPlayer = null;
	    	}
	        updateVoiceStopPlayingState();
	    }
    }
    
    private void resetOtherItemsExcept(ChatRecyclerBean chatBean) {
    	Logger.d(TAG, "resetOtherItems");
		for (ChatRecyclerBean bean : this.mRecycleBeans) {
			bean.setVoicePlaying(false);
			if (bean == chatBean) {
				bean.setVoicePlaying(true);
			}
		}
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
