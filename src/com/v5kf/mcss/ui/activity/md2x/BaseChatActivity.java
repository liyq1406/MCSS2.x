package com.v5kf.mcss.ui.activity.md2x;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.entity.CustomerBean.CustomerType;
import com.v5kf.mcss.ui.activity.MainTabActivity;
import com.v5kf.mcss.ui.activity.md2x.CustomerInfoListActivity;
import com.v5kf.mcss.utils.DevUtils;
import com.v5kf.mcss.utils.IntentUtil;
import com.v5kf.mcss.utils.Logger;

/**
 * @author chenhy
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-18 上午12:37:02
 * @package com.v5kf.mcss.ui.activity.ChatActivity.java
 * @description
 *
 */
public abstract class BaseChatActivity extends BaseToolbarActivity {
		
	private static final String TAG = "BaseChatActivity";
	protected String c_id;
	protected String s_id;
	public CustomerBean mCustomer;
	protected boolean isMessageAdded; // 新消息接收或发送(是否需要更新会话列表)
	private int mLastLayoutHeight;
	
	/* Handler处理消息类型 */
	public static final int HDL_WHAT_UPDATE_UI = 1; // 更新列表，定位最下面内容
	public static final int HDL_WHAT_UPDATE_UI_SMOOTH = 8; // 滚动更新列表，定位最下面内容
	public static final int HDL_WHAT_REFRESH_HISTORY = 2;
	public static final int HDL_WHAT_SHOW_ROBOT_MENU = 3;
	public static final int HDL_WHAT_HIDE_BOTTOM = 4;
	public static final int HDL_WHAT_CANDIDATE_ADD = 5;
	public static final int HDL_WHAT_CANDIDATE_DEL = 6;
	public static final int HDL_WHAT_CANDIDATE_SEND = 7;
	
	public static final String MSG_KEY_RESPONSE_NUM = "num";
	public static final String MSG_KEY_RESPONSE = "response";
	public static final String MSG_KEY_CANDIDATE_INDEX = "index";
	public static final String MSG_KEY_POSITION = "position";
	public static final String MSG_KEY_TYPE = "type";
	public static final int MSG_KEY_TYPE_R = 1; // 机器人消息对象
	public static final int MSG_KEY_TYPE_STR = 2; // 字符串
	
	protected static final int FIN_TYPE_NONE = 0;
//	protected static final int FIN_TYPE_END_SESSION = 1;
//	protected static final int FIN_TYPE_CHG_TRUST = 2;
	protected static final int FIN_TYPE_CHG_STICK = 3; // 改变置顶
	
	protected void handleIntent() {
		Intent intentNotify = getIntent();
		int type = intentNotify.getIntExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_NULL);
		if (Config.EXTRA_TYPE_NOTIFY == type) {			
			s_id = intentNotify.getStringExtra(Config.EXTRA_KEY_S_ID);
			c_id = intentNotify.getStringExtra(Config.EXTRA_KEY_C_ID);
		} else if (Config.EXTRA_TYPE_ACTIVITY_START == type) {
			s_id = intentNotify.getStringExtra(Config.EXTRA_KEY_S_ID);
			c_id = intentNotify.getStringExtra(Config.EXTRA_KEY_C_ID);
			Logger.d(TAG, "MainTabActivity -> Intent -> BaseChatActivity\n s_id:" + s_id + " c_id:" + c_id);
		}
		
		if (null != c_id) { // CSTM_ALIVE_WAIT
			mCustomer = mAppInfo.getCustomerBean(c_id);
	        if (null == mCustomer) {
	        	Logger.e(TAG, "Customer(null) not found");
	        	finish();
	        	return;
	        }
		}  else {
			Logger.e(TAG, "Customer(null) c_id not found");
			finish();
			return;
		}
		if (mCustomer.getSession() == null) {
			Logger.e(TAG, "Customer session is null");
			finish();
			return;
		}
		if (s_id == null) {
			s_id = mCustomer.getSession().getS_id();
		}
	}
	
	/**
	 * 判断弹出键盘动作
	 */
	protected void listenerLayoutChange() {
		final View activityRootView = findViewById(R.id.id_center_frame);
		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				int heightDiff = DevUtils.px2dp(mLastLayoutHeight - activityRootView.getHeight());
				mLastLayoutHeight = activityRootView.getHeight();
				if (heightDiff > 80 && heightDiff < 150) { // 如果高度差超过100像素，就很有可能是有软键盘...
					mHandler.sendEmptyMessage(HDL_WHAT_UPDATE_UI);
				} else if (heightDiff > 200 && heightDiff < 350) {
					mHandler.sendEmptyMessage(HDL_WHAT_UPDATE_UI);
				} else if (heightDiff > 350) {
					mHandler.sendEmptyMessage(HDL_WHAT_UPDATE_UI);
				}
			}
		});
	}
	
	@Override
	protected void onSwipeBackDone() {
		super.onSwipeBackDone();
		setFinishType(FIN_TYPE_NONE);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handleIntent();
	}	
	

	protected void setActivityResult(int resultCode) {
		// TODO 改为md2x TabMainActivity
//		Intent resultIntent = new Intent(BaseChatActivity.this, MainTabActivity.class);
		Intent resultIntent = new Intent(BaseChatActivity.this, MainTabActivity.class);
		resultIntent.putExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_ACTIVITY_START);
		resultIntent.putExtra(Config.EXTRA_KEY_S_ID, s_id);
		resultIntent.putExtra(Config.EXTRA_KEY_C_ID, c_id);
		setResult(resultCode, resultIntent);
		Logger.w(TAG, "--- setActivityResult");
	}
	

	@Override
	protected void onStart() {
		super.onStart();
		if (null == mCustomer) {
			finishActivity();
			Logger.e(TAG, "Null customer so finish.");
			return;
		}
		
		// 清空此会话的未读消息数量和通知栏
		if (mCustomer.getCstmType() == CustomerType.CustomerType_ServingAlive) { // 服务中列表客户
			Logger.i(TAG, "[onStart] 进入会话 对话中。。。");
			mApplication.setOnChat(true);
			mApplication.setOnChatCustomer(c_id);
		}
		mAppInfo.clearUnreadMessageNumOfSession(mCustomer.getSession());
		mApplication.clearNotification(c_id);
	}
	
	
	@Override
	protected void onStop() {
		super.onStop();
		mApplication.setOnChat(false);
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	
	public String getCustomerPhoto() {
		return mCustomer.getDefaultPhoto();
	}
	
	public String getWorkerPhoto() {
		return mAppInfo.getUser().getPhoto();
	}
	
	protected void gotoCustomerInfoActivity() {
		Intent intent = IntentUtil.getStartActivityIntent(
				this, 
				CustomerInfoListActivity.class, 
				c_id, 
				s_id);
				
		startActivityForResult(intent, Config.REQUEST_CODE_CUSTOMER_INFO);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}
	
	protected void setFinishType(int type) {
		switch (type) {
		case FIN_TYPE_NONE:
			if (isMessageAdded) {
				setActivityResult(Config.RESULT_CODE_CHAT_CONTENT_ADD);
			} else {
				setActivityResult(Config.RESULT_CODE_CHAT_CONTENT_NOCHANGE);
			}
			break;
		case FIN_TYPE_CHG_STICK:
			setActivityResult(Config.RESULT_CODE_CHAT_CHG_STICK);
			break;
		}
	}

}
