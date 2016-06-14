package com.v5kf.mcss.ui.activity.md2x;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.ui.adapter.OnChatRecyclerAdapter;
import com.v5kf.mcss.ui.entity.ChatRecyclerBean;
import com.v5kf.mcss.ui.widget.CircleImageView;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.cache.ImageLoader;

/**
 * @author chenhy
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-18 上午12:37:02
 * @package com.v5kf.mcss.ui.activity.ChatContentActivity.java
 * @description
 *
 */
public class ChatMessagesActivity extends BaseChatActivity {
	
	private static final String TAG = "ChatMessagesActivity";
	
	/* Chat Action Bar */
//	private LinearLayout mLeftLayout;
//	private TextView mTitleTv;
	private ImageView mTrustIv;
	private CircleImageView mCustomerPhotoIv;
	
	/* 对话列表 */
	private RecyclerView mRecyclerView;
	private OnChatRecyclerAdapter mRecycleAdapter;
	
	private List<ChatRecyclerBean> mDatas;
	
	/* 接入按钮 */
	private Button mPickupBtn;
		
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			setFinishType(FIN_TYPE_NONE);
			finishActivity();
		}
		return super.dispatchKeyEvent(event);
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_md2x_chat_messages);
		Logger.v(TAG, "onCreate");
		
		initData();
		findView();
		initView();
		
		/* 更新数据 */
		mHandler.sendEmptyMessage(HDL_WHAT_UPDATE_UI);
//		/* 设置监听布局变化(虚拟键盘弹出) */
//		listenerLayoutChange();
	}	
	
	private void initData() {
		mDatas = new ArrayList<>();
        
		if (mCustomer == null) {
			Logger.e(TAG, "customer null!");
			if (!Config.DEBUG) {
				return;
			}
		}
		
        List<V5Message> msg_list = mCustomer.getSession() != null ? mCustomer.getSession().getMessageArray() : null;
        if (null == msg_list || msg_list.isEmpty()) {
        	Logger.e(TAG, "Got customer messages null!");
        	return;
        }
        
        for (V5Message msg : msg_list) {
        	addRecycleBean(msg, true);
        }
        if (mDatas.size() > 0) {
        	mDatas.get(0).setSessionStart(true);
        }
	}
	
	
	private void findView() {
		/* Chat Title Action Bar */
//		mLeftLayout = (LinearLayout) findViewById(R.id.header_layout_leftview_container);
//		mTitleTv = (TextView) findViewById(R.id.header_htv_subtitle);
		mTrustIv = (ImageView) findViewById(R.id.more_iv);
		mCustomerPhotoIv = (CircleImageView) findViewById(R.id.cstm_photo_iv);
		
		mRecycleAdapter = new OnChatRecyclerAdapter(this, mDatas, null);
		mRecyclerView = (RecyclerView)findViewById(R.id.id_recycle_view_msgs);
    	mPickupBtn = (Button) findViewById(R.id.btn_waiting_pick_up);
	}

	private void initView() {
		/* 初始化标题栏 */
		initChatActionBar();
        
        /* 初始化对话列表 */
        initRecyclerView();
        
        mPickupBtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				try {
					CustomerRequest creq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, ChatMessagesActivity.this);
					creq.pickCustomer(c_id);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	

	private void initChatActionBar() {
		if (!checkCustomer()) {
			if (!Config.DEBUG) {
				return;
			}
			Logger.e(TAG, "[initChatActionBar]checkCustomer failed! null!");
		}
		updateCustomerTitle();
		mTrustIv.setVisibility(View.GONE);
//		mLeftLayout.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				setFinishType(FIN_TYPE_NONE);
//				finishActivity();
//			}
//		});
		mCustomerPhotoIv.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				gotoCustomerInfoActivity();
			}
		});
	}

	private boolean checkCustomer() {
		if (mCustomer == null) {
			mCustomer = mAppInfo.getCustomerBean(c_id);
		}
		return mCustomer != null;
	}
	
	private void listScrollToBottom(boolean smooth) {
		Logger.d(TAG, "756 listScrollToBottom smooth:" + smooth);
		if (mDatas.size() > 0) {
			Logger.d(TAG, "758 listScrollToBottom:" + (mDatas.size() - 1));
			if (smooth) {
				mRecyclerView.smoothScrollToPosition(mDatas.size() - 1);
			} else {
				mRecyclerView.scrollToPosition(mDatas.size() - 1);
			}
		}
	}
	
	/**
	 * 更新客户信息
	 */
	private void updateCustomerTitle() {
		if (!checkCustomer()) {
			if (!Config.DEBUG) {
				return;
			}
			Logger.e(TAG, "[updateCustomerTitle]checkCustomer failed! null!");
		}
		// [新增]离开状态
		if (mCustomer.getAccessable() != null && mCustomer.getAccessable().equals(QAODefine.ACCESSABLE_AWAY)) {
			getToolbar().setTitle("[离开]" + mCustomer.getDefaultName());
		} else {
			getToolbar().setTitle(mCustomer.getDefaultName());
		}
		ImageLoader imageLoader = new ImageLoader(this, true, R.drawable.v5_photo_default_cstm, new ImageLoader.ImageLoaderListener() {
			
			@Override
			public void onSuccess(String url, ImageView imageView) {
				Logger.d(TAG, "ImageLoaderListener.onSuccess");
				// [新增]离开状态提示
		    	if (mCustomer.getAccessable() != null && mCustomer.getAccessable().equals(QAODefine.ACCESSABLE_AWAY)) {
		    		Logger.d(TAG, "DisplayUtil.grayImageView Accessable:" + mCustomer.getAccessable());
		    		UITools.grayImageView(imageView);
		    	}
			}
			
			@Override
			public void onFailure(ImageLoader imageLoader, String url,
					ImageView imageView) {
				Logger.d(TAG, "ImageLoaderListener.onFailure Accessable:" + mCustomer.getAccessable());
				// [新增]离开状态提示
		    	if (mCustomer.getAccessable() != null && mCustomer.getAccessable().equals(QAODefine.ACCESSABLE_AWAY)) {
		    		Logger.d(TAG, "DisplayUtil.grayImageView begin");
		    		UITools.grayImageView(imageView);
		    		Logger.d(TAG, "DisplayUtil.grayImageView done");
		    	}
			}
		});
		imageLoader.DisplayImage(mCustomer.getDefaultPhoto(), mCustomerPhotoIv);
	}

	private void initRecyclerView() {
		mRecyclerView.setAdapter(mRecycleAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
//        mRecycleView.setRefreshLoadMoreListener(this);
//        mRecycleView.setPullRefreshEnable(false);
//        mRecycleView.setPullLoadMoreEnable(false);
//        mRecycleView.setVertical();
	}
	
	
	private void addRecycleBean(V5Message msg, boolean addAtTop) {
		if (msg.getDirection() == QAODefine.MSG_DIR_W2R || msg.getDirection() == QAODefine.MSG_DIR_R2W
				|| msg.getDirection() == QAODefine.MSG_DIR_R2CW
				|| msg.getMessage_type() == QAODefine.MSG_TYPE_CONTROL) { // 过滤非对话消息  -> [修改]过滤器全部控制消息
    		return;
    	} else if (msg.getMessage_type() == QAODefine.MSG_TYPE_TEXT && (null == msg.getDefaultContent(this) 
				|| msg.getDefaultContent(this).isEmpty())) { // 过滤空消息
    		return;
    	}
		
		// 机器人候选作应答消息需保存问题和机器人答复, 添加机器人答复显示在右侧
		if (addAtTop && msg.getCandidate() != null && msg.getCandidate().size() > 0) {
			V5Message robotMsg = msg.cloneDefaultRobotMessage();			
			if (robotMsg != null && (robotMsg.getDirection() == QAODefine.MSG_DIR_FROM_ROBOT || 
					robotMsg.getDirection() == QAODefine.MSG_DIR_FROM_WAITING)) { // 机器人回复
				if (robotMsg.getDefaultContent(this) == null || robotMsg.getDefaultContent(this).isEmpty()
						|| robotMsg.getMessage_type() == QAODefine.MSG_TYPE_WXCS) {
					// 排除空白消息内容和转客服消息
				} else {
					robotMsg.setDirection(QAODefine.MSG_DIR_FROM_ROBOT);
					ChatRecyclerBean robotBean = new ChatRecyclerBean(robotMsg);
	    			robotBean.setName("Robot");
	    			mDatas.add(0, robotBean);
				}
			}
		}
				
    	ChatRecyclerBean bean = new ChatRecyclerBean(msg);
    	bean.setName(mCustomer.getDefaultName());
    	if (addAtTop) {
    		mDatas.add(0, bean);
    	} else {
    		mDatas.add(bean);
    	}
    	Logger.d(TAG, "[addRecycleBean] +1");
    	
    	// 机器人候选作应答消息需保存问题和机器人答复, 添加机器人答复显示在右侧
		if (!addAtTop && msg.getCandidate() != null && msg.getCandidate().size() > 0) {
			V5Message robotMsg = msg.cloneDefaultRobotMessage(); 
			if (robotMsg != null && (robotMsg.getDirection() == QAODefine.MSG_DIR_FROM_ROBOT || 
					robotMsg.getDirection() == QAODefine.MSG_DIR_FROM_WAITING)) { // 机器人回复
				if (robotMsg.getDefaultContent(this) == null || robotMsg.getDefaultContent(this).isEmpty()
						|| robotMsg.getMessage_type() == QAODefine.MSG_TYPE_WXCS) {
					// 排除空白消息内容和转客服消息
				} else {
					robotMsg.setDirection(QAODefine.MSG_DIR_FROM_ROBOT);
					ChatRecyclerBean robotBean = new ChatRecyclerBean(robotMsg);
	    			robotBean.setName("Robot");
	    			mDatas.add(robotBean);
				}
			}
		}
	}
	
		
	@Override
	protected void handleMessage(Message msg) {
		Log.d(TAG, "handleMessage:" + msg.what + " size:" + mDatas.size());		
		switch (msg.what) {
		case HDL_WHAT_UPDATE_UI:
			mRecycleAdapter.notifyDataSetChanged();
			listScrollToBottom(false);
			break;
			
		case HDL_WHAT_REFRESH_HISTORY:
			
//			mRecycleView.stopRefresh();
			break;
		}
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Config.REQUEST_CODE_SERVING_SESSION_FRAGMENT) {
			setActivityResult(resultCode);
			finishActivity();
		}
	}

	/***** event *****/

	@Subscriber(tag = EventTag.ETAG_CSTM_WAIT_OUT, mode = ThreadMode.MAIN)
	private void updateCustomerOut(CustomerBean cstm) {
		Logger.d(TAG + "-eventbus", "updateCustomerOut -> ETAG_CSTM_WAIT_OUT");
		if (cstm.getC_id().equals(mCustomer.getC_id())) {
			//UIUtil.noticeEndSession(this, cstm.getClosingReason());
			setResult(Config.RESULT_CODE_PICKUP_CSTM);
			finishActivity();
		}
    }
	
	@Subscriber(tag = EventTag.ETAG_NEW_MESSAGE, mode = ThreadMode.MAIN)
	private void newMessage(V5Message message) {
		Logger.d(TAG + "-eventbus", "newMessage -> ETAG_NEW_MESSAGE");
		if (!this.s_id.equals(message.getS_id())) {
			return;
		}
		List<V5Message> msg_list = mCustomer.getSession().getMessageArray();
		if (msg_list == null) {
			Logger.e(TAG, "NEW_MESSAGE: null MessageBean list");
			return;
		}
		addRecycleBean(msg_list.get(0), false);
		mRecycleAdapter.notifyItemInserted(mDatas.size() - 1);
		listScrollToBottom(false);
		isMessageAdded = true;
	}
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		if (isConnect) {
			
		} else {
			finishActivity();
		}
	}
	
	@Subscriber(tag = EventTag.ETAG_ACCESSABLE_CHANGE, mode = ThreadMode.MAIN)
	private void onAccessableChange(CustomerBean cstm) {
		Logger.d(TAG + "-eventbus", "updateCustomerInfo -> ETAG_ACCESSABLE_CHANGE");
		if (cstm.getC_id().equals(c_id)) {
			if (cstm != mCustomer) {
				mCustomer = cstm;
			}
			updateCustomerTitle();
		}
    }
}
