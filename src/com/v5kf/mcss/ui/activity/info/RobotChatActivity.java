package com.v5kf.mcss.ui.activity.info;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.rockerhieu.emojicon.EmojiconEditText;
import com.v5kf.client.lib.V5MessageManager;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5TextMessage;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.MessageRequest;
import com.v5kf.mcss.ui.activity.BaseActivity;
import com.v5kf.mcss.ui.activity.BaseChatActivity;
import com.v5kf.mcss.ui.adapter.RobotRecyclerAdapter;
import com.v5kf.mcss.ui.entity.ChatRecyclerBean;
import com.v5kf.mcss.utils.Logger;

public class RobotChatActivity extends BaseActivity {
	private static final String TAG = "RobotChatActivity";
	private static final int HDL_WHAT_FINISH = 98;
	
	private String s_id;
	private String c_id;
	private V5Message mMessage;
	
	private EmojiconEditText mChatEdit;
	private Button mSubmitBtn;
	
	/* 机器人回复列表 */
	private RecyclerView mCandidateList;
	/* 机器人回复-瀑布流 */
	private RobotRecyclerAdapter mRobotAdapter;
	private List<ChatRecyclerBean> mRobotDatas;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_robot_chat);
		handleIntent();
//		initOutMessage();
		findView();
		initView();
	}

//	private void initOutMessage() {
//		// 发送时需设置m_id,content
//		MessageContentBean msgContent = new MessageContentBean();
//		msgContent.setMsg_type(QAODefine.MSG_TYPE_TEXT);
//		mMessage = new MessageBean(msgContent);
//		mMessage.setActive(true);
//		mMessage.setC_id(c_id);
//		mMessage.setS_id(s_id);
//		mMessage.setMessage_type(QAODefine.MSG_TYPE_TEXT);
//		mMessage.setMsg_dir(QAODefine.MSG_DIR_W2R);
//		
////		sendMessage(mMessage);
//	}

	private void handleIntent() {
		Intent i = getIntent();
		c_id = i.getStringExtra(Config.EXTRA_KEY_C_ID);
		s_id = i.getStringExtra(Config.EXTRA_KEY_S_ID);
	}

	private void findView() {
        mCandidateList = (RecyclerView) findViewById(R.id.id_candidate_list);
        mChatEdit = (EmojiconEditText) findViewById(R.id.id_emoji_et);
        mSubmitBtn = (Button) findViewById(R.id.id_submit);
	}

	private void initView() {
		initTopBarForLeftBack(R.string.title_robot_chat);
		initCandidate();
		
		mChatEdit.setFocusable(true);
		mChatEdit.requestFocus();
		showSoftInputView();
		
		 
		mSubmitBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(mChatEdit.getText())) {
					ShowToast(R.string.username_or_pwd_empty_tip);
					return;
				}
				
				String text = mChatEdit.getText().toString();
				V5TextMessage mesage = V5MessageManager.obtainTextMessage(text);
				mesage.setDirection(QAODefine.MSG_DIR_W2R);
				sendMessage(mesage);
				
				hideSoftInputView();
				mHandler.sendEmptyMessageDelayed(HDL_WHAT_FINISH, 180);
			}
		});
	}
	
	private void initCandidate() {
        /* 适配器初始化 */
        mRobotDatas = new ArrayList<ChatRecyclerBean>();
        mRobotAdapter = new RobotRecyclerAdapter(this, mRobotDatas);
        mCandidateList.setItemAnimator(new DefaultItemAnimator());
        mCandidateList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mCandidateList.setAdapter(mRobotAdapter);
	}
	
	private void finishThis() {
		Intent data = new Intent();
		Bundle bundle = new Bundle();
		bundle.putSerializable("message", mMessage);
		setResult(Config.RESULT_CODE_ROBOT_CHAT, data);
	}
	
	/**
	 * 将机器人答案添加到编辑框中
	 * @param showCandidate RobotChatActivity 
	 * @return void
	 */
	private void showCandidate() {
		if (null == mRobotDatas) {
			return;
		}
		String text = "";
		for (ChatRecyclerBean bean : mRobotDatas) {
			if (bean.isSelected() && bean.getMessage() != null) {
				if (!text.isEmpty()) {
					text += "\n";
				}
				text += bean.getMessage().getDefaultContent(this);
			}
		}
		mChatEdit.setText(text);
		mChatEdit.setSelection(text.length());
	}
	
	protected void sendMessage(V5Message msg) {
		msg.setDirection(QAODefine.MSG_DIR_W2R);
		msg.setS_id(s_id);
		msg.setC_id(c_id);
		try {
			/* 发送Message请求 */
			MessageRequest mReq = (MessageRequest) RequestManager.getRequest(QAODefine.O_TYPE_MESSAGE, this);
			mReq.sendMessage(msg);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case HDL_WHAT_FINISH:
			finishActivity();
			break;
		
		case BaseChatActivity.HDL_WHAT_CANDIDATE_ADD: {
			Bundle bundle = msg.getData();
			int pos = bundle.getInt(BaseChatActivity.MSG_KEY_POSITION);
			mRobotDatas.get(pos).setSelected(true);
			showCandidate();
			break;
		}
		
		case BaseChatActivity.HDL_WHAT_CANDIDATE_DEL: {
			Bundle bundle = msg.getData();
			int pos = bundle.getInt(BaseChatActivity.MSG_KEY_POSITION);
			mRobotDatas.get(pos).setSelected(false);
			showCandidate();
			break;
		}
		
		case BaseChatActivity.HDL_WHAT_CANDIDATE_SEND: {
			Bundle bundle = msg.getData();
			int pos = bundle.getInt(BaseChatActivity.MSG_KEY_POSITION);
			mMessage = mRobotDatas.get(pos).getMessage();
			finishThis();
			break;
		}
		}
	}
	
	private void updateRobotMessage(V5Message message) {
		if (message == null || message.getCandidate() == null) {
			Logger.w(TAG, "[updateRobotMessage] 来自robot的消息为null");
			return;
		}
		mRobotAdapter.setMode(false);
		mRobotDatas.clear(); // 刷新列表
		for (V5Message content : message.getCandidate()) {
			Logger.i(TAG, "[消息类型]：" + content.getMessage_type());
			ChatRecyclerBean robotMessage = new ChatRecyclerBean(content);
			robotMessage.setName(mAppInfo.getUser().getDefaultName());				
			mRobotDatas.add(robotMessage);
			Logger.i(TAG, "[updateRobotMessage] Message_type:" + robotMessage.getMessage().getMessage_type());
		}
		mRobotAdapter.notifyDataSetChanged();	
	}
	
	/***** event *****/
	
	@Subscriber(tag = EventTag.ETAG_ROBOT_ANSWER, mode = ThreadMode.MAIN)
	private void robotAnswer(V5Message message) {
		if (!this.s_id.equals(message.getS_id())) {
			return;
		}
		updateRobotMessage(message);
	}
}
