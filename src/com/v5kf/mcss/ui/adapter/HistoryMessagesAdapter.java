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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.v5kf.client.lib.entity.V5ArticleBean;
import com.v5kf.client.lib.entity.V5ArticlesMessage;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5LocationMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MusicMessage;
import com.v5kf.client.lib.entity.V5VideoMessage;
import com.v5kf.client.lib.entity.V5VoiceMessage;
import com.v5kf.client.ui.emojicon.EmojiconTextView;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.ui.activity.md2x.LocationMapActivity;
import com.v5kf.mcss.ui.entity.ChatRecyclerBean;
import com.v5kf.mcss.ui.widget.ListLinearLayout;
import com.v5kf.mcss.ui.widget.ListLinearLayout.OnListLayoutClickListener;
import com.v5kf.mcss.utils.DateUtil;
import com.v5kf.mcss.utils.FileUtil;
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
        	
        case QAODefine.MSG_TYPE_MUSIC:
        	itemView = mInflater.inflate(R.layout.item_history_msg_music, parent, false);
        	break;

        case QAODefine.MSG_TYPE_SHORT_VIDEO:
        case QAODefine.MSG_TYPE_VIDEO:
        	itemView = mInflater.inflate(R.layout.item_history_msg_video, parent, false);
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
        final ChatRecyclerBean chatBean = mRecycleBeans.get(position);
        holder.setChatBean(chatBean);
        V5Message msgContent = chatBean.getMessage();
        
        switch (getItemViewType(position)) {
        case QAODefine.MSG_TYPE_VOICE: {
        	V5VoiceMessage voiceMessage = (V5VoiceMessage)msgContent;
			Logger.d(TAG, "list load Voice ----- duration:" + voiceMessage.getDuration());
        	holder.mVoiceSecondTv.setText(String.format("%.1f″", voiceMessage.getDuration()/1000.0f));
        	// 语音状态
        	if (chatBean.isPlaying()) {
        		holder.updateVoiceStartPlayingState();
        	} else {
        		holder.updateVoiceStopPlayingState();
        	}
        	if (voiceMessage.getFilePath() != null && voiceMessage.getDuration() > 0
        			&& FileUtil.isFileExists(voiceMessage.getFilePath())) { // 已加载则不需要loadMedia
				Logger.d(TAG, "---已加载---");
				break;
			} else if (chatBean.isBadUrl()) {
				Logger.d(TAG, "---URL异常---");
				break;
			} else {
				Logger.d(TAG, "---首次加载---");
			}
        	
        	String url = voiceMessage.getDefaultMediaUrl();
        	
        	MediaLoader mediaLoader = new MediaLoader(mActivity, holder, new MediaLoaderListener() {
				
				@Override
				public void onSuccess(V5Message msg, Object obj, MediaCache media) {
					//((V5VoiceMessage)msg).setFilePath(media.getLocalPath());
					//((V5VoiceMessage)msg).setDuration(media.getDuration());
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
					chatBean.setBadUrl(true);
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
        	Logger.d(TAG, "list load Image ----- " + urlPath);
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
			if (TextUtils.isEmpty(article.getPic_url())) {
				holder.mNewsPic.setVisibility(View.GONE);
			} else {
				holder.mNewsPic.setVisibility(View.VISIBLE);
				ImageLoader imgLoader = new ImageLoader(mActivity, true, R.drawable.v5_img_src_loading);
	        	imgLoader.DisplayImage(article.getPic_url(), holder.mNewsPic);
			}
        	break;
        	
        case QAODefine.MSG_TYPE_SHORT_VIDEO:
        case QAODefine.MSG_TYPE_VIDEO: {
			V5VideoMessage videoMessage = (V5VideoMessage)msgContent;
			if (videoMessage.getCoverFrame() != null) {
				loadVideo(holder, videoMessage);
			} else {
				holder.mVideoBgIv.setImageResource(R.drawable.img_default_video);
			}
			
			// 播放状态
        	if (chatBean.isPlaying()) {
        		holder.updateVideoStartPlayingState();
        	} else {
        		holder.updateVideoStopPlayingState();
        	}
			if (videoMessage.getFilePath() != null && FileUtil.isFileExists(videoMessage.getFilePath())) { // 已加载则不需要loadMedia
				Logger.d(TAG, "---Video 已加载---");
				break;
			} else if (chatBean.isBadUrl()) {
				Logger.d(TAG, "---Video URL异常---");
				break;
			} else {
				Logger.d(TAG, "---Video 首次加载---");
			}
			
			// 加载语音
        	String url = videoMessage.getDefaultMediaUrl();
        	Logger.d(TAG, "Video url:" + url + " sendState:" + videoMessage.getState());
        	MediaLoader mediaLoader = new MediaLoader(mActivity, chatBean, new MediaLoaderListener() {
				
				@Override
				public void onSuccess(V5Message msg, Object obj, MediaCache media) {
					//((V5VideoMessage)msg).setFilePath(media.getLocalPath());
					Logger.d(TAG, "list load Video onSuccess ----- ");
					if (media.getCoverFrame() != null) {
						int index = mRecycleBeans.indexOf(obj);
						if (index >= 0 && index < mRecycleBeans.size()) {
							notifyItemChanged(index);
						}
						//loadVideo((ChatItemViewHolder)obj, (V5VideoMessage)msg);
					}
				}
				
				@Override
				public void onFailure(final MediaLoader mediaLoader, final V5Message msg, Object obj) {
					if (mediaLoader.getUrl().contains("chat.v5kf.com/") 
							&& mediaLoader.getmTryTimes() < 3) { // 最多重试3次
						mActivity.getHandler().postDelayed(new Runnable() {
							public void run() {
								mediaLoader.loadMedia(mediaLoader.getUrl(), msg, null);
							}
						}, 1000); // 1s后重试
					} else {
						chatBean.setBadUrl(true);
					}
				}
			});
        	mediaLoader.loadMedia(url, videoMessage, null);
		}
			break;
			
		case QAODefine.MSG_TYPE_MUSIC: {
			V5MusicMessage musicMessage = (V5MusicMessage)msgContent;
			if (!TextUtils.isEmpty(musicMessage.getTitle()) || !TextUtils.isEmpty(musicMessage.getTitle())) {
				holder.mMusicContentLayout.setVisibility(View.VISIBLE);
				holder.mMusicTitle.setText(musicMessage.getTitle());
				holder.mMusicDescription.setText(musicMessage.getDescription());
			} else {
				holder.mMusicContentLayout.setVisibility(View.GONE);
			}
			
			// 语音状态
        	if (chatBean.isPlaying()) {
        		holder.updateMusicStartPlayingState();
        	} else {
        		holder.updateMusicStopPlayingState();
        	}
			if (musicMessage.getFilePath() != null && FileUtil.isFileExists(musicMessage.getFilePath())) { // 已加载则不需要loadMedia
				Logger.d(TAG, "---Music 已加载---");
				break;
			} else if (chatBean.isBadUrl()) {
				Logger.d(TAG, "---Music URL异常---");
				break;
			} else {
				Logger.d(TAG, "---Music 首次加载---");
			}
			
			// 加载语音
        	String url = musicMessage.getDefaultMediaUrl();
        	Logger.d(TAG, "Music url:" + url + " sendState:" + musicMessage.getState());
        	MediaLoader mediaLoader = new MediaLoader(mActivity, holder, new MediaLoaderListener() {
				
				@Override
				public void onSuccess(V5Message msg, Object obj, MediaCache media) {
					//((V5MusicMessage)msg).setFilePath(media.getLocalPath());
					Logger.d(TAG, "list load Music onSuccess ----- ");
				}
				
				@Override
				public void onFailure(final MediaLoader mediaLoader, final V5Message msg, Object obj) {
					if (mediaLoader.getUrl().contains("chat.v5kf.com/") 
							&& mediaLoader.getmTryTimes() < 3) { // 最多重试3次
						mActivity.getHandler().postDelayed(new Runnable() {
							public void run() {
								mediaLoader.loadMedia(mediaLoader.getUrl(), msg, null);
							}
						}, 1000); // 1s后重试
					} else {
						chatBean.setBadUrl(true);
					}
				}
			});
        	mediaLoader.loadMedia(url, musicMessage, null);
		}
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

    private void loadVideo(ViewHolder holder, V5VideoMessage videoMessage) {
		holder.mVideoBgIv.setImageBitmap(videoMessage.getCoverFrame());
		
		// 设置宽高
		int width = videoMessage.getCoverFrame().getWidth();
		int height = videoMessage.getCoverFrame().getHeight();
		float density = mActivity.getResources().getDisplayMetrics().density;
		float maxWH = ImageLoader.IMAGE_MAX_WH * density + 0.5f;
		float minWH = ImageLoader.IMAGE_MIN_WH * density + 0.5f;
		if (width > maxWH && height > maxWH) {
			float scale = Math.max(maxWH / width, maxWH / height);
			width = (int)scale * width;
			height = (int)scale * height;
		} else if (width < minWH && height < minWH) {
			float scale = Math.max(minWH / width, minWH / height);
			width = (int)scale * width;
			height = (int)scale * height;
		}
		holder.mVideoBgIv.setLayoutParams(new FrameLayout.LayoutParams(width, height));
		holder.mVideoSurface.setLayoutParams(new FrameLayout.LayoutParams(width, height));
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
        
        /* 视频 */
        public ImageView mVideoControlIv; // 播放按钮
        public ImageView mVideoBgIv; // 视频背景
        public SurfaceView mVideoSurface;
        public SurfaceHolder mVideoSurfaceHolder;
        
        /* 音乐 */
        public ImageView mMusicControlIv;
        public TextView mMusicTitle;
        public TextView mMusicDescription;
        public ViewGroup mMusicContentLayout;

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
    			
    		case QAODefine.MSG_TYPE_SHORT_VIDEO:
    		case QAODefine.MSG_TYPE_VIDEO:
            	mVideoBgIv = (ImageView) itemView.findViewById(R.id.id_video_bg);
            	mVideoControlIv = (ImageView) itemView.findViewById(R.id.id_video_control_img);
            	mVideoSurface = (SurfaceView) itemView.findViewById(R.id.id_video_surface);
            	mVideoSurfaceHolder = mVideoSurface.getHolder();
            	mVideoControlIv.setOnClickListener(this);
            	mVideoBgIv.setOnClickListener(this);
            	mVideoSurface.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if (mVideoControlIv.getVisibility() == View.GONE) {
							mVideoControlIv.setVisibility(View.VISIBLE);
							mVideoControlIv.postDelayed(new Runnable() {
								
								@Override
								public void run() {
									if (mChatBean.isPlaying()) {
										mVideoControlIv.setVisibility(View.GONE);
									}
								}
							}, 1000);
						}
					}
				});
            	mVideoSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
					
					@Override
					public void surfaceDestroyed(SurfaceHolder holder) {
						Logger.d(TAG, "[SurfaceHolder.surfaceDestroyed]");
					}
					
					@Override
					public void surfaceCreated(SurfaceHolder holder) {
						Logger.d(TAG, "[SurfaceHolder.surfaceCreated]");
						// 必须在surface创建后才能初始化MediaPlayer,否则不会显示图像
						mPlayer = new MediaPlayer();
						mPlayer.setOnErrorListener(new OnErrorListener() {
							
							@Override
							public boolean onError(MediaPlayer mp, int what, int extra) {
								Logger.e(TAG, "MediaPlayer - onError");
								return false;
							}
						});
						mPlayer.setOnCompletionListener(new OnCompletionListener() {
							
							@Override
							public void onCompletion(MediaPlayer mp) {
								Logger.i(TAG, "MediaPlayer - completePlaying");
								updateVideoStopPlayingState();
								mp.release();
								mp = null;
								mPlayer = null;
							}
						});
						mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
							
							@Override
							public void onPrepared(MediaPlayer mp) {
								Logger.d(TAG, "[MediaPlayer.onPrepared]");
								mPlayer.start();
								updateVideoStartPlayingState();
							}
						});
						try {
							mPlayer.setDataSource(((V5VideoMessage)mChatBean.getMessage()).getFilePath());
							mPlayer.setDisplay(mVideoSurfaceHolder);
							mPlayer.prepare();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					@Override
					public void surfaceChanged(SurfaceHolder holder, int format, int width,
							int height) {
						Logger.d(TAG, "[SurfaceHolder.surfaceChanged]");
					}
				});
            	break;
            	
            case QAODefine.MSG_TYPE_MUSIC:
            	mMusicContentLayout = (ViewGroup) itemView.findViewById(R.id.id_music_content_layout);
            	mMusicControlIv = (ImageView) itemView.findViewById(R.id.id_music_control_img);
            	mMusicTitle = (TextView) itemView.findViewById(R.id.id_music_title);
            	mMusicDescription = (TextView) itemView.findViewById(R.id.id_music_desc);
            	mMusicControlIv.setOnClickListener(this);
            	break;
    			
    		case QAODefine.MSG_TYPE_TEXT:
    		default:
    			mMsg = (EmojiconTextView) itemView.findViewById(R.id.id_msg_text);
//    	        mMsg.setOnLongClickListener(this);
    			if (mMsg instanceof EmojiconTextView) { // URL点击事件
                	((EmojiconTextView)mMsg).setURLClickListener(new EmojiconTextView.OnURLClickListener() {
    					
    					@Override
    					public void onClick(View v, String url) {
    						mActivity.gotoWebViewActivity(url);
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
					if (mChatBean.isPlaying()) { // 停止播放
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
				
			case R.id.id_music_control_img: // 点击音乐播放
				if (mChatBean.getMessage().getMessage_type() == QAODefine.MSG_TYPE_MUSIC) {
					if (mChatBean.isPlaying()) { // 停止播放
						stopPlayingMusic();
					} else { // 开始播放
//						if (mVoiceAnimDrawable != null) {
//							mVoiceAnimDrawable.stop();
//						}
						startPlaying((V5MusicMessage)mChatBean.getMessage(), new OnCompletionListener() {
							
							@Override
							public void onCompletion(MediaPlayer mp) {
								Logger.i(TAG, "MediaPlayer - completePlaying");
								updateMusicStopPlayingState();
								mp.release();
								mp = null;
								mPlayer = null;
							}
						});
					}
				}
				break;
			
			case R.id.id_video_control_img: // 点击视频播放
				if (Config.ENABLE_PLAY_VEDIO_IN_LIST) {
					if (mChatBean.getMessage().getMessage_type() == QAODefine.MSG_TYPE_VIDEO || 
							mChatBean.getMessage().getMessage_type() == QAODefine.MSG_TYPE_SHORT_VIDEO) {
						if (mChatBean.isPlaying()) { // 停止播放
							stopPlayingVideo();
						} else { // 开始播放
//							if (mVoiceAnimDrawable != null) {
//								mVoiceAnimDrawable.stop();
//							}
							startPlaying((V5VideoMessage)mChatBean.getMessage(), new OnCompletionListener() {
								
								@Override
								public void onCompletion(MediaPlayer mp) {
									Logger.i(TAG, "MediaPlayer - completePlaying");
									updateVideoStopPlayingState();
									mp.release();
									mp = null;
									mPlayer = null;
								}
							});
						}
					}
					break;
				}				
			case R.id.id_video_bg: // 点击视频背景
				mActivity.gotoVedioPlayActivity(((V5VideoMessage)mChatBean.getMessage()).getFilePath());
				break;
				
			default:
				break;
			}
		}
		
		public void updateVoiceStartPlayingState() {
			Logger.i(TAG, "UI - updateVoiceStartPlayingState");
			mChatBean.setPlaying(true);
			mVoiceIv.setBackgroundResource(R.anim.anim_leftgray_voice);
			mVoiceAnimDrawable = (AnimationDrawable)mVoiceIv.getBackground();
			mVoiceAnimDrawable.start();						
		}
		
		public void updateVoiceStopPlayingState() {
			Logger.i(TAG, "UI - updateVoiceStopPlayingState");
			mChatBean.setPlaying(false);
			if (mVoiceAnimDrawable != null) {
				mVoiceAnimDrawable.stop();
				mVoiceAnimDrawable = null;
			}
			mVoiceIv.setBackgroundResource(R.drawable.chat_animation_left_gray3);
		}
		
		public void updateVideoStartPlayingState() {
			Logger.i(TAG, "UI - updateVideoStartPlayingState position:" + getAdapterPosition());
			mChatBean.setPlaying(true);
			mVideoControlIv.setImageResource(R.drawable.img_music_stop);
			mVideoControlIv.setVisibility(View.GONE);
			mVideoSurface.setVisibility(View.VISIBLE);
		}
		
		public void updateVideoStopPlayingState() {
			Logger.i(TAG, "UI - updateVideoStopPlayingState position:" + getAdapterPosition());
			mChatBean.setPlaying(false);
			mVideoControlIv.setImageResource(R.drawable.img_music_play);
			mVideoControlIv.setVisibility(View.VISIBLE);
			mVideoSurface.setVisibility(View.GONE);
		}
		
		public void updateMusicStartPlayingState() {
			Logger.i(TAG, "UI - updateMusicStartPlayingState position:" + getAdapterPosition());
			mChatBean.setPlaying(true);
			mMusicControlIv.setImageResource(R.drawable.img_music_stop);
		}

		public void updateMusicStopPlayingState() {
			Logger.i(TAG, "UI - updateMusicStopPlayingState position:" + getAdapterPosition());
			mChatBean.setPlaying(false);
			mMusicControlIv.setImageResource(R.drawable.img_music_play);
		}
		
		private void onSingleNewsClick(String url) {
			mActivity.gotoWebViewActivity(url);
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
	            mActivity.ShowToast(R.string.media_play_failed);
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
	    
	    private void startPlaying(V5MusicMessage musicMessage, OnCompletionListener completionListener) {
			Logger.i(TAG, "MediaPlayer - startPlaying " + getAdapterPosition());
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
				mPlayer.setDataSource(musicMessage.getFilePath());
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
				updateMusicStartPlayingState();
			} catch (Exception e) {
				e.printStackTrace();
				Logger.e(TAG, "MediaPlayer prepare() failed");
				mActivity.ShowToast(R.string.media_play_failed);
				mPlayer.release();
				mPlayer = null;
				updateMusicStopPlayingState(); // UI
			}
		}

		private void startPlaying(final V5VideoMessage videoMessage, final OnCompletionListener completionListener) {
			Logger.i(TAG, "MediaPlayer - startPlaying " + getAdapterPosition());
			if (mPlayer != null) {
				if (mPlayer.isPlaying()) {
					mPlayer.stop();
				}
				mPlayer.release();
				mPlayer = null;
				Logger.i(TAG, "MediaPlayer - stopPlaying all others");
				resetOtherItemsExcept(mChatBean);
			}
			try {
				//surfaceHolder.setKeepScreenOn(true);
//				mVideoSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
//					
//					@Override
//					public void surfaceDestroyed(SurfaceHolder holder) {
//						Logger.d(TAG, "[SurfaceHolder.surfaceDestroyed]");
//					}
//					
//					@Override
//					public void surfaceCreated(SurfaceHolder holder) {
//						Logger.d(TAG, "[SurfaceHolder.surfaceCreated]");
//						// 必须在surface创建后才能初始化MediaPlayer,否则不会显示图像
//						mPlayer = new MediaPlayer();
//						mPlayer.setOnErrorListener(new OnErrorListener() {
//							
//							@Override
//							public boolean onError(MediaPlayer mp, int what, int extra) {
//								Logger.e(TAG, "MediaPlayer - onError");
//								return false;
//							}
//						});
//						mPlayer.setOnCompletionListener(completionListener);
//						mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//							
//							@Override
//							public void onPrepared(MediaPlayer mp) {
//								Logger.d(TAG, "[MediaPlayer.onPrepared]");
//								mPlayer.start();
//								updateVideoStartPlayingState();
//							}
//						});
//						try {
//							mPlayer.setDataSource(videoMessage.getFilePath());
//							mPlayer.setDisplay(mVideoSurfaceHolder);
//							mPlayer.prepare();
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//					
//					@Override
//					public void surfaceChanged(SurfaceHolder holder, int format, int width,
//							int height) {
//						Logger.d(TAG, "[SurfaceHolder.surfaceChanged]");
//					}
//				});
				mVideoSurface.setVisibility(View.VISIBLE);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.e(TAG, "MediaPlayer prepare() failed");
				mActivity.ShowToast(R.string.media_play_failed);
				mPlayer.release();
				mPlayer = null;
				updateVideoStopPlayingState(); // UI
			}
		}

	    private void stopPlayingMusic() {
	    	Logger.i(TAG, "MediaPlayer - stopPlayingMusic " + getAdapterPosition());
	    	if (mPlayer != null) {
	    		mPlayer.stop();
	    		mPlayer.release();
	    		mPlayer = null;
	    	}
	    	updateMusicStopPlayingState();
	    }

	    private void stopPlayingVideo() {
	    	Logger.i(TAG, "MediaPlayer - stopPlayingVideo " + getAdapterPosition());
	    	if (mPlayer != null) {
	    		mPlayer.stop();
	    		mPlayer.release();
	    		mPlayer = null;
	    	}
	    	updateVideoStopPlayingState();
	    }
    }
    
    private void resetOtherItemsExcept(ChatRecyclerBean chatBean) {
    	Logger.d(TAG, "resetOtherItems");
		for (ChatRecyclerBean bean : this.mRecycleBeans) {
			bean.setPlaying(false);
			if (bean == chatBean) {
				bean.setPlaying(true);
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
