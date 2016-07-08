package com.v5kf.mcss.ui.adapter;

import java.util.List;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.v5kf.client.lib.entity.V5ArticleBean;
import com.v5kf.client.lib.entity.V5ArticlesMessage;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MusicMessage;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.ui.activity.md2x.BaseChatActivity;
import com.v5kf.mcss.ui.fragment.md2x.MaterialBaseFragment;
import com.v5kf.mcss.ui.widget.ListLinearLayout;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.cache.ImageLoader;

/**
 * @author Chenhy	
 * @email chenhy@v5kf.com
 * @version v1.0 2015-8-4 下午3:05:16
 * @package com.v5kf.mcss.ui.adapter of MCSS-Native
 * @file OnChatRecyclerAdapter.java 
 *
 */
public class MaterialRecyclerAdapter extends RecyclerView.Adapter<MaterialRecyclerAdapter.RobotViewHolder> {
	private static final int TYPE_NULL = 0;
	private static final int TYPE_SINGLE_NEWS = 99;
	private static final int TYPE_NEWS = 199;
	private static final int TYPE_IMG = 299;
	private static final int TYPE_MUSIC = 399;
	protected static final String TAG = "MaterialRecyclerAdapter";
	private LayoutInflater mInflater;
	private List<V5Message> mRecyclerBeans;
	private MaterialBaseFragment mFragment;
	private ActivityBase mActivity;
	
	// 语音媒体
	private MediaPlayer mPlayer;
	
    public MaterialRecyclerAdapter(MaterialBaseFragment fragment, ActivityBase activity, List<V5Message> recyclerBeans) {
        super();
        this.mRecyclerBeans = recyclerBeans;
        this.mFragment = fragment;
        this.mActivity = activity;
        mInflater = LayoutInflater.from(mFragment.getActivity());
    }

    @Override
    public int getItemViewType(int position) {
    	V5Message msgContent = mRecyclerBeans.get(position);
    	switch (msgContent.getMessage_type()) {
    	case QAODefine.MSG_TYPE_IMAGE:    		
    		return TYPE_IMG;
    		
    	case QAODefine.MSG_TYPE_NEWS:
    		if (((V5ArticlesMessage)msgContent).getArticles() == null) {
    			return TYPE_NULL;
    		}
    		if (((V5ArticlesMessage)msgContent).getArticles().size() == 1) {
    			return TYPE_SINGLE_NEWS;
    		} else if (((V5ArticlesMessage)msgContent).getArticles().size() > 1) {
    			return TYPE_NEWS;
    		}
    		return TYPE_SINGLE_NEWS;
    	
    	case QAODefine.MSG_TYPE_MUSIC:
    		
    		return TYPE_MUSIC;
    	}
    	return TYPE_NULL;
    }
    
    @Override
    public long getItemId(int position) {
    	return position;
    }

    @Override
    public RobotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    	RobotViewHolder viewHolder = null;
    	View itemView = null;
        switch (viewType) {        	
        case TYPE_IMG:
        	itemView = mInflater.inflate(R.layout.item_material_img, parent, false);
        	viewHolder = new RobotViewHolder(viewType, itemView);
        	break;
        	        	
        case TYPE_SINGLE_NEWS:
        	itemView = mInflater.inflate(R.layout.item_material_single_news, parent, false);
        	viewHolder = new RobotViewHolder(viewType, itemView);
        	break;
        	
        case TYPE_NEWS:
        	itemView = mInflater.inflate(R.layout.item_material_multi_news, parent, false) ;
        	viewHolder = new RobotViewHolder(viewType, itemView);
        	break;
        	
        case TYPE_MUSIC:
        	itemView = mInflater.inflate(R.layout.item_material_music, parent, false) ;
        	viewHolder = new RobotViewHolder(viewType, itemView);
        	break;
        	
        default:
        	itemView = mInflater.inflate(R.layout.item_material_null, parent, false) ;
        	viewHolder = new RobotViewHolder(viewType, itemView);
        	break;
        }
        
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RobotViewHolder holder, int position) {
    	// 设置数据
    	V5Message msgContent = mRecyclerBeans.get(position);
        holder.setBean(msgContent);
				
		if (getItemViewType(position) == TYPE_SINGLE_NEWS) { // 单图文
			V5ArticleBean article = ((V5ArticlesMessage)msgContent).getArticles().get(0);
			holder.mNewsTitle.setText(article.getTitle());
			holder.mNewsContent.setText(article.getDescription());
			if (TextUtils.isEmpty(article.getPic_url())) {
				holder.mNewsImg.setVisibility(View.GONE);
			} else {
				holder.mNewsImg.setVisibility(View.VISIBLE);
				ImageLoader imgLoader = new ImageLoader(mActivity, true, R.drawable.v5_img_src_loading);
	        	imgLoader.DisplayImage(article.getPic_url(), holder.mNewsImg);
			}
		} else if (getItemViewType(position) == TYPE_NEWS) { // 多图文
			holder.mNewsAdapter = new NewsListAdapter(
					mActivity, 
					((V5ArticlesMessage)msgContent).getArticles(),
					false);
			holder.mNewsListLayout.bindLinearLayout(holder.mNewsAdapter);
		} else if (getItemViewType(position) == TYPE_IMG) { // 图片
			holder.mImgTitle.setText(((V5ImageMessage)msgContent).getTitle());
			ImageLoader imgLoader = new ImageLoader(mActivity, true, R.drawable.v5_img_src_loading);
        	imgLoader.DisplayImage(((V5ImageMessage)msgContent).getPic_url(), holder.mImg);			
		} else if (getItemViewType(position) == TYPE_MUSIC) { // 音乐
			holder.mMusicTitle.setText(((V5MusicMessage)msgContent).getTitle());
			holder.mMusicDesc.setText(((V5MusicMessage)msgContent).getDescription());
		}
    }

    @Override
    public int getItemCount() {
        return mRecyclerBeans.size();
    }

    
    class RobotViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    	private static final String TAG = "MaterialRecyclerAdapter.RobotViewHolder";
    	
    	private V5Message mBean;
        public Button mSendBtn;
//        private int mType;
        
        /* 单图文news */
        public ImageView mNewsImg;
        public TextView mNewsTitle;
        public TextView mNewsContent;
        
        /* 多图文 */
        public ListLinearLayout mNewsListLayout;
        public NewsListAdapter mNewsAdapter;
        
        /* 图片 */
        public ImageView mImg;
        public TextView mImgTitle;
        
        /* 音频 */
        public ImageView mMusicController;
        public TextView mMusicTitle;
        public TextView mMusicDesc;

        public RobotViewHolder(int viewType, View itemView) {
            super(itemView);
//            mType = viewType;
            switch (viewType) {
            case TYPE_IMG:
            	mImg = (ImageView) itemView.findViewById(R.id.id_material_img);
            	mImgTitle = (TextView) itemView.findViewById(R.id.id_img_title_tv);
            	mImg.setOnClickListener(this);
            	break;
            
            case TYPE_SINGLE_NEWS:
            	mNewsImg = (ImageView) itemView.findViewById(R.id.id_news_img);
            	mNewsTitle = (TextView) itemView.findViewById(R.id.id_news_title_inner_text);
            	mNewsContent = (TextView) itemView.findViewById(R.id.id_news_desc_text);
            	itemView.findViewById(R.id.id_news_layout).setOnClickListener(this);
            	break;
            	
            case TYPE_NEWS:
            	mNewsListLayout = (ListLinearLayout) itemView.findViewById(R.id.id_news_layout);
            	mNewsListLayout.setOnClickListener(this);
            	break;
            	
            case TYPE_MUSIC:
            	mMusicController = (ImageView) itemView.findViewById(R.id.id_music_control_img);
            	mMusicTitle = (TextView) itemView.findViewById(R.id.id_music_title);
            	mMusicDesc = (TextView) itemView.findViewById(R.id.id_music_desc);
            	mMusicController.setOnClickListener(this);
            	break;
            	
            default:            	
            	break;
            }
            mSendBtn = (Button) itemView.findViewById(R.id.id_send_btn);
            mSendBtn.setOnClickListener(this);
        }

		@Override
		public void onClick(View v) {
			Logger.d(TAG, "item onCLick View.id=" + v.getId());
			if (null == mBean) {
				Logger.e(TAG, "ViewHolder has null ChatRecycleBean");
				return;
			}
			
			switch (v.getId()) {
			case R.id.id_send_btn: // 发送
				onSendClick();
				break;
				
			case R.id.id_music_control_img: // 音乐播放/停止控制
				
				break;
				
			case R.id.id_news_layout:
				if (getItemViewType() == TYPE_SINGLE_NEWS) {
					onSingleNewsClick(((V5ArticlesMessage)mBean).getArticles().get(0).getUrl());
				}
				break;
				
			case R.id.id_material_img: // 查看图片
				mActivity.gotoImageActivity(((V5ImageMessage)mBean).getDefaultPicUrl());
				break;
			}
		}
		
		private void onSingleNewsClick(String url) {
			mActivity.gotoWebViewActivity(url);
		}

		private void onSendClick() {
			Message msg = new Message();
			msg.what = BaseChatActivity.HDL_WHAT_CANDIDATE_SEND;
			Bundle bundle = new Bundle();
			bundle.putInt(BaseChatActivity.MSG_KEY_POSITION, getAdapterPosition());
			msg.setData(bundle);
			mFragment.sendHandlerMessage(msg);
		}
		
		public V5Message getBean() {
			return mBean;
		}

		public void setBean(V5Message bean) {
			this.mBean = bean;
		}
		
		public void updateMusicStartPlayingState() {
			Logger.i(TAG, "UI - updateMusicStartPlayingState position:" + getAdapterPosition());
//			mChatBean.setPlaying(true);
			mMusicController.setImageResource(R.drawable.img_music_stop);
		}

		public void updateMusicStopPlayingState() {
			Logger.i(TAG, "UI - updateMusicStopPlayingState position:" + getAdapterPosition());
//			mChatBean.setPlaying(false);
			mMusicController.setImageResource(R.drawable.img_music_play);
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
//				resetOtherItemsExcept(mChatBean);
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

		private void stopPlayingMusic() {
	    	Logger.i(TAG, "MediaPlayer - stopPlayingMusic " + getAdapterPosition());
	    	if (mPlayer != null) {
	    		mPlayer.stop();
	    		mPlayer.release();
	    		mPlayer = null;
	    	}
	    	updateMusicStopPlayingState();
	    }
    }
    
//    private void resetOtherItemsExcept(ChatRecyclerBean chatBean) {
//    	Logger.d(TAG, "resetOtherItems");
//		for (ChatRecyclerBean bean : this.mRecycleBeans) {
//			bean.setPlaying(false);
//		}
//		chatBean.setPlaying(true);
//		notifyDataSetChanged();
//	}
    
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
