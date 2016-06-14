package com.v5kf.mcss.ui.adapter;

import java.util.List;

import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rockerhieu.emojicon.EmojiconTextView;
import com.v5kf.client.lib.entity.V5ArticleBean;
import com.v5kf.client.lib.entity.V5ArticlesMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.MessageRequest;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.ui.activity.md2x.BaseChatActivity;
import com.v5kf.mcss.ui.activity.md2x.WebViewActivity;
import com.v5kf.mcss.ui.entity.ChatRecyclerBean;
import com.v5kf.mcss.ui.widget.ListLinearLayout;
import com.v5kf.mcss.utils.IntentUtil;
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
public class RobotRecyclerAdapter extends RecyclerView.Adapter<RobotRecyclerAdapter.RobotViewHolder> {

	private static final int TYPE_SINGLE_NEWS = 99;
	private static final int TYPE_NEWS = 199;
	private static final int TYPE_QUESTION = 299;
	protected static final String TAG = "RobotRecyclerAdapter";
	private LayoutInflater mInflater;
	private List<ChatRecyclerBean> mRecyclerBeans;
	private ActivityBase mActivity;
	private boolean isQuestion;
	
    public RobotRecyclerAdapter(ActivityBase activity, List<ChatRecyclerBean> recyclerBeans) {
        super();
        this.mRecyclerBeans = recyclerBeans;
        this.mActivity = activity;
        mInflater = LayoutInflater.from(activity);
        isQuestion = false;
    }
    
    public void setMode(boolean isQues) {
    	isQuestion = isQues;
    }

    @Override
    public int getItemViewType(int position) {
    	if (isQuestion) {
        	return TYPE_QUESTION;
        }
    	
    	ChatRecyclerBean chatMessage = mRecyclerBeans.get(position);
        if (chatMessage.getMessage() == null) {
        	return QAODefine.MSG_TYPE_NULL;
        }        
        int msgType = chatMessage.getMessage().getMessage_type();
    	
    	if (msgType == QAODefine.MSG_TYPE_NEWS) {
    		V5Message msgContent = mRecyclerBeans.get(position).getMessage();
    		if (msgContent != null && ((V5ArticlesMessage)msgContent).getArticles() != null) {
	    		if (((V5ArticlesMessage)msgContent).getArticles().size() == 1) {
	    			return TYPE_SINGLE_NEWS;
	    		} else if (((V5ArticlesMessage)msgContent).getArticles().size() > 1) {
	    			return TYPE_NEWS;
	    		}
    		}
    	}
		return msgType;
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
        case TYPE_QUESTION:
        	itemView = mInflater.inflate(R.layout.item_robot_question, parent, false);
        	viewHolder = new RobotViewHolder(viewType, itemView);
        	break;
        	
//        case QAODefine.MSG_TYPE_IMAGE:
//        	itemView = mInflater.inflate(R.layout.item_robot_img, parent, false);
//        	viewHolder = new RobotViewHolder(viewType, itemView);
//        	break;
//        	
//        case QAODefine.MSG_TYPE_LOCATION:
//        	itemView = mInflater.inflate(R.layout.item_robot_location, parent, false);
//        	viewHolder = new RobotViewHolder(viewType, itemView);
//        	break;
        	        	
        case TYPE_SINGLE_NEWS:
        	itemView = mInflater.inflate(R.layout.item_robot_single_news, parent, false);
        	viewHolder = new RobotViewHolder(viewType, itemView);
        	break;
        	
        case TYPE_NEWS:
        	itemView = mInflater.inflate(R.layout.item_robot_multi_news, parent, false) ;
        	viewHolder = new RobotViewHolder(viewType, itemView);
        	break;
        	
        case QAODefine.MSG_TYPE_TEXT:
        default:
        	itemView = mInflater.inflate(R.layout.item_robot_text, parent, false) ;
        	viewHolder = new RobotViewHolder(viewType, itemView);
        	break;
        }
        
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RobotViewHolder holder, int position) {
    	// 设置数据
        ChatRecyclerBean chatMessage = mRecyclerBeans.get(position);
        holder.setChatBean(chatMessage);
				
		if (getItemViewType(position) == TYPE_SINGLE_NEWS) { // 单图文
			V5ArticleBean article = ((V5ArticlesMessage)chatMessage.getMessage()).getArticles().get(0);
			holder.mNewsTitle.setText(article.getTitle());
			holder.mNewsContent.setText(article.getDescription());
			ImageLoader imgLoader = new ImageLoader(mActivity, true, R.drawable.v5_img_src_loading);
        	imgLoader.DisplayImage(article.getPic_url(), holder.mNewsImg);			
		} else if (getItemViewType(position) == TYPE_NEWS) { // 多图文
			holder.mNewsAdapter = new NewsListAdapter(
					mActivity, 
					((V5ArticlesMessage)chatMessage.getMessage()).getArticles(),
					false);
			holder.mNewsListLayout.bindLinearLayout(holder.mNewsAdapter);
//		} else if (getItemViewType(position) == QAODefine.MSG_TYPE_IMAGE) { // 图片
//			
//		} else if (getItemViewType(position) == QAODefine.MSG_TYPE_LOCATION) { // 位置
			
		} else {
			String str = chatMessage.getDefaultContent(mActivity) == null ? "" : chatMessage.getDefaultContent(mActivity);
			Spanned text = Html.fromHtml(str.replace("\n", "<br>"));
			holder.mMsg.setText(text);
			holder.mMsg.setMovementMethod(LinkMovementMethod.getInstance());
			Logger.w("RobotRecyclerAdapter", "[" + str + "] SendHtmLText:" + text);
		}
    }

    @Override
    public int getItemCount() {
        return mRecyclerBeans.size();
    }

    
    class RobotViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    	private static final String TAG = "RobotRecyclerAdapter.RobotViewHolder";
    	
    	private ChatRecyclerBean mChatBean;
        public TextView mSerialNum; /* No use */
        boolean isAdded;
        public Button mRobotAdd;
        public Button mRobotSend;
//        private int mType;
        
        /* 单图文news */
        public ImageView mNewsImg;
        public TextView mNewsTitle;
        public TextView mNewsContent;
        
        /* 多图文 */
        public ListLinearLayout mNewsListLayout;
        public NewsListAdapter mNewsAdapter;
        
        /* 文本 */
        public EmojiconTextView mMsg;

        public RobotViewHolder(int viewType, View itemView) {
            super(itemView);
//            mType = viewType;
            switch (viewType) {
            case TYPE_QUESTION:
            	mSerialNum = (TextView) itemView.findViewById(R.id.id_serial_num);
            	mMsg = (EmojiconTextView) itemView.findViewById(R.id.id_robot_text);
            	mRobotAdd = (Button) itemView.findViewById(R.id.id_robot_add_btn);
            	mRobotSend = (Button) itemView.findViewById(R.id.id_robot_request_btn);
            	break;
            
            case TYPE_SINGLE_NEWS:
            	mSerialNum = (TextView) itemView.findViewById(R.id.id_serial_num);
            	mNewsImg = (ImageView) itemView.findViewById(R.id.id_news_img);
            	mNewsTitle = (TextView) itemView.findViewById(R.id.id_news_title_inner_text);
            	mNewsContent = (TextView) itemView.findViewById(R.id.id_news_desc_text);
            	itemView.findViewById(R.id.id_news_layout).setOnClickListener(this);
            	mRobotAdd = (Button) itemView.findViewById(R.id.id_robot_add_btn);
            	mRobotSend = (Button) itemView.findViewById(R.id.id_robot_send_btn);
            	break;
            	
            case TYPE_NEWS:
            	mNewsListLayout = (ListLinearLayout) itemView.findViewById(R.id.id_news_layout);
            	mRobotAdd = (Button) itemView.findViewById(R.id.id_robot_add_btn);
            	mRobotSend = (Button) itemView.findViewById(R.id.id_robot_send_btn);
            	mNewsListLayout.setOnClickListener(this);
            	break;
            	
            case QAODefine.MSG_TYPE_TEXT:
            default:
            	mSerialNum = (TextView) itemView.findViewById(R.id.id_serial_num);
            	mMsg = (EmojiconTextView) itemView.findViewById(R.id.id_robot_text);
            	mRobotAdd = (Button) itemView.findViewById(R.id.id_robot_add_btn);
            	mRobotSend = (Button) itemView.findViewById(R.id.id_robot_send_btn);
            	break;
            }
            mRobotAdd.setOnClickListener(this);
            mRobotSend.setOnClickListener(this);
            
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

		@Override
		public void onClick(View v) {
			Logger.d(TAG, "item onCLick View.id=" + v.getId());
			if (null == mChatBean) {
				Logger.e(TAG, "ViewHolder has null ChatRecycleBean");
				return;
			}
			
			switch (v.getId()) {
			case R.id.id_robot_add_btn: // 添加
				onRobotAddClick();
				break;
				
			case R.id.id_robot_send_btn: // 发送
				onRobotSendClick();
				break;
				
			case R.id.id_robot_request_btn: // 提问机器人
				onRobotQuestion();
				break;
			}
		}

		/**
		 * 提问机器人
		 * @param onRobotQuestion RobotViewHolder 
		 * @return void
		 */
		private void onRobotQuestion() {
			V5Message msg = mChatBean.getMessage();
			msg.setDirection(QAODefine.MSG_DIR_W2R);
			
			try {
				/* 发送Message请求 */
				MessageRequest mReq = (MessageRequest) RequestManager.getRequest(QAODefine.O_TYPE_MESSAGE, mActivity);
				mReq.sendMessage(msg);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 发送机器人推荐消息
		 * @param onRobotSendClick RobotViewHolder 
		 * @return void
		 */
		private void onRobotSendClick() {
			Message msg = new Message();
			msg.what = BaseChatActivity.HDL_WHAT_CANDIDATE_SEND;
			Bundle bundle = new Bundle();
			bundle.putInt(BaseChatActivity.MSG_KEY_POSITION, getAdapterPosition());
			msg.setData(bundle);
			mActivity.sendHandlerMessage(msg);
		}

		/**
		 * 机器人推荐消息加入编辑
		 * @param onRobotAddClick RobotViewHolder 
		 * @return void
		 */
		private void onRobotAddClick() {
			Message msg = new Message();
			if (isAdded) {
				mRobotAdd.setText(R.string.robot_msg_add);
				msg.what = BaseChatActivity.HDL_WHAT_CANDIDATE_DEL;
			} else {
				mRobotAdd.setText(R.string.robot_msg_del);
				msg.what = BaseChatActivity.HDL_WHAT_CANDIDATE_ADD;
			}
			isAdded = !isAdded;
			Bundle bundle = new Bundle();
			bundle.putInt(BaseChatActivity.MSG_KEY_POSITION, getAdapterPosition());
			msg.setData(bundle);
			mActivity.sendHandlerMessage(msg);
		}

		public ChatRecyclerBean getChatBean() {
			return mChatBean;
		}

		public void setChatBean(ChatRecyclerBean mChatBean) {
			this.mChatBean = mChatBean;
		}

    }	
}
