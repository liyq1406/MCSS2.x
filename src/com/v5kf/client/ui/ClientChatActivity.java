package com.v5kf.client.ui;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.v5kf.client.lib.DBHelper;
import com.v5kf.client.lib.Logger;
import com.v5kf.client.lib.V5ClientAgent;
import com.v5kf.client.lib.V5ClientAgent.ClientOpenMode;
import com.v5kf.client.lib.V5ClientConfig;
import com.v5kf.client.lib.V5KFException;
import com.v5kf.client.lib.V5KFException.V5ExceptionStatus;
import com.v5kf.client.lib.V5MessageManager;
import com.v5kf.client.lib.V5Util;
import com.v5kf.client.lib.callback.MessageSendCallback;
import com.v5kf.client.lib.callback.OnGetMessagesCallback;
import com.v5kf.client.lib.callback.V5MessageListener;
import com.v5kf.client.lib.entity.V5ArticlesMessage;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5LocationMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MessageDefine;
import com.v5kf.client.lib.entity.V5TextMessage;
import com.v5kf.client.lib.entity.V5VoiceMessage;
import com.v5kf.client.ui.QuesListAdapter.OnQuesClickListener;
import com.v5kf.client.ui.emojicon.EmojiconEditText;
import com.v5kf.client.ui.keyboard.AppBean;
import com.v5kf.client.ui.keyboard.AppFuncPageView;
import com.v5kf.client.ui.keyboard.AppsAdapter.FuncItemClickListener;
import com.v5kf.client.ui.keyboard.EmoticonBean;
import com.v5kf.client.ui.keyboard.EmoticonsIndicatorView;
import com.v5kf.client.ui.keyboard.EmoticonsKeyBoardBar;
import com.v5kf.client.ui.keyboard.EmoticonsUtils;
import com.v5kf.client.ui.keyboard.IView;
import com.v5kf.client.ui.keyboard.Utils;
import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.entity.LocationBean;
import com.v5kf.mcss.ui.activity.md2x.BaseToolbarActivity;
import com.v5kf.mcss.ui.activity.md2x.LocationMapActivity;
import com.v5kf.mcss.utils.DevUtils;
import com.v5kf.mcss.utils.FileUtil;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.V5VoiceRecord;
import com.v5kf.mcss.utils.V5VoiceRecord.VoiceRecordListener;
import com.v5kf.mcss.utils.VoiceErrorCode;


public class ClientChatActivity extends BaseToolbarActivity implements V5MessageListener, 
		OnRecyclerClickListener, OnQuesClickListener, OnRefreshListener, VoiceRecordListener {

    private static final String TAG = "ClientChatActivity";
    
    private EmoticonsKeyBoardBar mKeyBar;
    private LayoutInflater mInflater;
    
    // List
    private RecyclerView mRecyclerView;
    private ClientChatRecyclerAdapter mRecyclerAdapter;
    private List<V5ChatBean> mDatas;
    private SwipeRefreshLayout mRefreshLayout;
    
    // 常见问答
    private ListView mQuesList;
    private QuesListAdapter mQuesAdapter;
    private List<V5TextMessage> mQuesContents;
    private TextView mQuestionDesc;
    
    // 语音有关view
 	private Button mBtnVoice;
 	RelativeLayout layout_record;
 	TextView tv_voice_second;
 	TextView tv_voice_tips;
 	TextView tv_voice_title;
 	ImageView iv_record;
 	
 	private MyCountDownTimer voice_timer;
 	private V5VoiceRecord mRecorder;
 	//[取消]讯飞语音SDK
// 	// 语音听写对象
// 	private SpeechRecognizer mIat;
// 	// 听写监听器
// 	private RecognizerListener mRecoListener;
// 	// 用HashMap存储听写结果
// 	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    
 	// 文本输入
    private EmojiconEditText mInputEt;
    
    private Handler mHandler;
//    private MessageSendCallback mSendHandler;
    
	// 历史会话序号
    private int mOffset = 0;
    private boolean isHistoricalFinish = false;
    // 连接标识
	private boolean isConnected = false;
	private int foregroundCount = 0;
	// 每页刷新数量
	private static final int NUM_PER_PAGE = 10;
    private String mImageFileName;
    
    private int numOfMessagesOnRefresh = NUM_PER_PAGE;	// 下拉刷新加载历史消息数量
	private int numOfMessagesOnOpen = 10;				// 打开页面时加载历史消息数量
	private V5Message mOpenAnswer;						// 开场问题的答案，缓存以做保存数据库判断
	public ClientOpenMode mOpenMode = ClientOpenMode.clientOpenModeDefault;
	public String mOpenQuestion;
    
    private static final int UI_LIST_ADD = 1;
    private static final int UI_LIST_UPDATE = 2;
    private static final int UI_LIST_UPDATE_HISTORICAL = 3;
    private static final int HDL_VOICE_DISMISS = 101; 
    private static final int HDL_APP_BACKGROUND = 102; 

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_md2x_v5client_chat);
        
        mHandler = new BaseHandler(this);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        findView();
        initView();
        initData();
        
        // 接口初始化及配置添加到onCreate最后
        V5ClientConfig config = V5ClientConfig.getInstance(this);
        config.setShowLog(true); // 默认为true
        config.setLogLevel(V5ClientConfig.LOG_LV_DEBUG); // 默认为全部显示
        config.setNickname(config.getSiteAccount() + "." + mAppInfo.getUser().getDefaultName()); // 设置昵称
        config.setUid(mAppInfo.getUser().getW_id());
        config.setAvatar(mAppInfo.getUser().getPhoto()); // 头像
        config.setGender(mAppInfo.getUser().getGender()); // 性别
        config.setDeviceToken(mApplication.getDeviceToken());
        
        V5ClientAgent.getInstance().start(this, this); // 开启消息服务
    }
	
	private void findView() {
		mRecyclerView = (RecyclerView) findViewById(R.id.id_recycler_msgs);
		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.id_swipe_refresh);
		mKeyBar = (EmoticonsKeyBoardBar) findViewById(R.id.chat_activity_keybar);
		
		mInputEt = mKeyBar.getEt_chat();
		
		/* 语音有关 */
		mBtnVoice = mKeyBar.getBtn_voice(); // 长按语音输入按钮
        layout_record = (RelativeLayout) findViewById(R.id.id_mask_view);
		tv_voice_tips = (TextView) findViewById(R.id.tv_voice_tips);
		tv_voice_title = (TextView) findViewById(R.id.tv_voice_title);
		tv_voice_second = (TextView) findViewById(R.id.tv_voice_second);
		iv_record = (ImageView) findViewById(R.id.iv_record);
		
		mKeyBar.setBuilder(EmoticonsUtils.getBuilder(this, false));
		
		/* 添加表情删除按钮 */
		View toolBtnView = mInflater.inflate(R.layout.view_toolbtn_right_simple, null);
        toolBtnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	mKeyBar.del();
            }
        });
        // [修改]取消emoji只显示qface
        mKeyBar.addFixedView(toolBtnView, true); // 底部栏固定位置的删除按钮
	}

	private void initView() {
		initTopBarForLeftBack(R.string.v5_on_connection);
		
		initRecyclerView();
		initAppfunc();	// 位置1
		initRobotList(); // 位置2
		initKeyboardListener();
		initBtnVoice();
//		mKeyBar.getS
	}

	private void initAppfunc() {
		View viewApps =  mInflater.inflate(R.layout.v5_view_apps, null);
		mKeyBar.add(viewApps); // 位置1-添加“+”号打开的功能界面(富媒体输入)
		
		/* 图片、拍照、位置等功能界面 */
		AppFuncPageView pageApps = (AppFuncPageView)viewApps.findViewById(R.id.view_apv);
		EmoticonsIndicatorView indicatorView = (EmoticonsIndicatorView)viewApps.findViewById(R.id.view_eiv);
		pageApps.setIndicatorView(indicatorView);
		ArrayList<AppBean> mAppBeanList = new ArrayList<AppBean>();
		String[] funcArray = getResources().getStringArray(R.array.chat_func);
		String[] funcIconArray = getResources().getStringArray(R.array.chat_func_icon);
		for (int i = 0; i < funcArray.length; i++) {
			AppBean bean = new AppBean();
			bean.setId(i);
			bean.setIcon(funcIconArray[i]);
			bean.setFuncName(funcArray[i]);
			mAppBeanList.add(bean);
		}
		pageApps.setAppBeanList(mAppBeanList);
		pageApps.setFuncItemClickListener(new FuncItemClickListener() {			
			@Override
			public void onFuncItemClick(View v, AppBean bean) {
				if (!isConnected) {
					showToast(R.string.v5_waiting_for_connection);
					return;
				}
				switch (bean.getId()) {
				case 0: // 常见问题
					getHotQuesAndShow();				
					break;
					
				case 1: // 相关问题
					mQuestionDesc.setText(R.string.v5_relative_question);
					showQuestionList();
					break;
					
				case 2: // 图片
					if (DevUtils.hasPermission(ClientChatActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
						openSystemPhoto();
					} else {
						showAlertDialog(R.string.v5_permission_photo_deny, null);
					}
					break;
					
				case 3: // 拍照
					if (DevUtils.hasPermission(ClientChatActivity.this, "android.permission.CAMERA")) {
						cameraPhoto();
					} else {
						showAlertDialog(R.string.v5_permission_camera_deny, null);
					}
					break;
				
				case 4: // 人工客服
					V5ClientAgent.getInstance().switchToArtificialService(new MessageSendCallback() {
							
						@Override
						public void onSuccess(V5Message message) {
							mDatas.add(new V5ChatBean(message));
							mHandler.sendEmptyMessage(UI_LIST_UPDATE);
						}
						
						@Override
						public void onFailure(V5Message message, V5ExceptionStatus status, String desc) {
							if (status != V5ExceptionStatus.ExceptionNoError) {
								showToast(R.string.artificial_service_failed);
							}
						}
					});
					break;
				
				case 5: // 位置
					if (DevUtils.checkAndRequestPermission(ClientChatActivity.this, 
							new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"})) {
						Intent intent = new Intent(ClientChatActivity.this, LocationMapActivity.class);
						startActivityForResult(intent, Config.REQUEST_CODE_GET_LOCATION);
						overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
					} else {
						showAlertDialog(R.string.v5_permission_location_deny, null);
					}
					break;
				}
			}
		});
	}
	
	private void initRobotList() {
		View viewQues =  mInflater.inflate(R.layout.v5_view_robot_candidate, null);
		mQuestionDesc = (TextView) viewQues.findViewById(R.id.id_candidate_desc);
		mQuestionDesc.setVisibility(View.VISIBLE);
		mQuestionDesc.setText("相关问题：");
        mKeyBar.add(viewQues); // 位置2-添加常见问答、相关问题显示界面
        
        /* 机器人候选答案列表初始化 */
        mQuesList = (ListView) viewQues.findViewById(R.id.id_candidate_list);
        /* 适配器初始化 */
        mQuesContents = new ArrayList<V5TextMessage>();
        mQuesAdapter = new QuesListAdapter(this, mQuesContents, this);
        mQuesList.setAdapter(mQuesAdapter);
	}

	private void initKeyboardListener() {
		/* 添加标签组 */
//		TextView view2 =  (TextView)inflater.inflate(R.layout.view_test, null);
//		view2.setText("PAGE 3");
//		mKeyBar.add(view2);
		/* 标签组点击事件 */
//		mKeyBar.getEmoticonsToolBarView().addOnToolBarItemClickListener(new EmoticonsToolBarView.OnToolBarItemClickListener() {
//            @Override
//            public void onToolBarItemClick(int position) {
//                if (position == 3) {
//                    mKeyBar.show(3);
//                } else if (position == 4) {
//                    mKeyBar.show(4);
//                } else if (position == 5) {
//                    mKeyBar.show(5);
//                }
//            }
//        });
		
		/* 表情点击 */
        mKeyBar.getEmoticonsPageView().addIViewListener(new IView() {
            @Override
            public void onItemClick(EmoticonBean bean) {
            	if (bean.getEventType() == EmoticonBean.FACE_TYPE_NOMAL) { // 表情图标
            		mInputEt.updateText();
            	} else if (bean.getEventType() == EmoticonBean.FACE_TYPE_USERDEF) { // 自定义图片表情
            		/* 
            		 * 自定义表情图片:发送图片
            		 * 图片发送功能完成后，后期可增加将图片添加为表情功能
            		 */
            	}
            }

            @Override
            public void onItemDisplay(EmoticonBean bean) {
            	
            }

            @Override
            public void onPageChangeTo(int position) {
            	
            }
        });

        /* KeyboardBar输入栏按钮监听器 */
        mKeyBar.setOnKeyBoardBarViewListener(new EmoticonsKeyBoardBar.KeyBoardBarViewListener() {
            @Override
            public void OnKeyBoardStateChange(int state, final int height) {
            	Logger.i(TAG, "OnKeyBoardStateChange -- 键盘变化height:" + height);
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                    	if (height > 0) { // 软键盘弹出
                    		mKeyBar.setClicked(false);
                    	} else if (height == 0) { // 软键盘收起
                    		if (mKeyBar.isKeyBoardFootShow() && !mKeyBar.isClicked()) {
                				mKeyBar.hideAutoView();
                			}
                    		mKeyBar.setClicked(false);
                    	} else if (height == -1) { // 输入扩展框大小变化
                    		
                    	}
//                    	mRecyclerView.scrollToPosition(mDatas.size() - 1);
                    	if (mDatas.size() > 1) {
                    		mRecyclerView.smoothScrollToPosition(mDatas.size() - 1);
                    	}
                    }
                });
            }

            @Override
            public void OnSendBtnClick(String msg) { // 发送按钮
            	if (!isConnected) {
					showToast(R.string.v5_waiting_for_connection);
					if (!Config.DEBUG) {
						return;
					}
				}
    			onSendClick(msg);    			
    			mKeyBar.clearEditText();
            }

			@Override
			public void OnVideoBtnClick() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void OnMultimediaBtnClick() {
				// TODO Auto-generated method stub
				
			}
        });
	}
	
	private void initRecyclerView() {
		if (null == mDatas) {
			mDatas = new ArrayList<V5ChatBean>();
		}
		if (null == mRecyclerAdapter) {
			mRecyclerAdapter = new ClientChatRecyclerAdapter(this, mDatas, this);
		}
		mRecyclerView.setAdapter(mRecyclerAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        
        // 列表空白点击
        mRecyclerView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
			    case MotionEvent.ACTION_DOWN:
			    	// 隐藏键盘
			    	mKeyBar.hideAutoView();
			    	DevUtils.hideSoftInputMethod(ClientChatActivity.this);
			        break;
			    case MotionEvent.ACTION_UP:
			        v.performClick();
			        break;
			    default:
			        break;
			    }
				return false;
			}
		});
        
        // 下拉刷新
	    mRefreshLayout.setOnRefreshListener(this);
	    mRefreshLayout.setColorSchemeColors(
	    		V5Util.getIdByName(this, "color", "green"), 
	    		V5Util.getIdByName(this, "color", "red"), 
	    		V5Util.getIdByName(this, "color", "blue"),
	    		V5Util.getIdByName(this, "color", "yellow"));
	}
	
	/* 初始化语音录入功能 */
	private void initBtnVoice() {
		mBtnVoice.setOnTouchListener(new VoiceTouchListen());
		mRecorder = new V5VoiceRecord(this, this);
	}
	
	private void initData() {
		if (null == mDatas) {
			mDatas = new ArrayList<V5ChatBean>();
		}
	}
	
	/**
	 * 发送按钮点击
	 * @param v
	 */
	protected void onSendClick(String msg) {
		if (TextUtils.isEmpty(mInputEt.getText())) {
			showToast("您尚未输入内容");
			return;
		}
		V5TextMessage message = V5MessageManager.obtainTextMessage(msg);
		sendV5Message(message);
	}
	
	/**
	 * 获取常见问答(额外接口)
	 */
	protected void getHotQuesAndShow() {
		// 请求常见问答
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				final String responseString = HttpUtil.getHttpResp(HttpUtil.getHotReqsHttpUrl(getApplicationContext()));
				if (responseString != null) {
					mHandler.post(new Runnable() { // 在UI线程执行
						
						@Override
						public void run() {
							try {
								JSONObject resp = new JSONObject(DevUtils.decodeUnicode(responseString));
								Logger.d(TAG, "[HotReqsHttpClient] " + DevUtils.decodeUnicode(responseString));
								if (resp.get("state").equals("ok") && resp.getInt("total") > 0) {
									JSONArray arr = resp.getJSONArray("items");
									mQuesContents.clear(); // 刷新列表
									for (int i = 0; i < arr.length(); i++) {
										String str = arr.getString(i);
										V5TextMessage msg = V5MessageManager.obtainTextMessage(str);
										mQuesContents.add(msg);
									}						
									mQuesAdapter.notifyDataSetChanged();
									mQuestionDesc.setText(R.string.v5_hot_question);
									showQuestionList();
								} else {
									showToast(R.string.toast_hot_reqs_empty);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});					
				}
			}
		}).start();
	}

	private void showToast(int resId) {
		Toast.makeText(getApplicationContext(), resId,
				Toast.LENGTH_SHORT).show();
	}

	private void showToast(String text) {
		Toast.makeText(getApplicationContext(), text,
				Toast.LENGTH_SHORT).show();
	}
	
	private void showQuestionList() {
		mKeyBar.setClicked(true);
		Utils.closeSoftKeyboard(ClientChatActivity.this);
		mKeyBar.showAutoView();
		mKeyBar.show(EmoticonsKeyBoardBar.FUNC_CHILLDVIEW_ROBOT);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (foregroundCount == 0) {
			V5ClientAgent.getInstance().onStart();
			
			Bundle bundle = new Bundle();
			bundle.putInt(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_CLEAR_CLIENT_MSG);
			Intent intent = new Intent(Config.ACTION_ON_MAINTAB);
			intent.putExtras(bundle);
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
			foregroundCount++;
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (foregroundCount == 1) {
			foregroundCount--;
			V5ClientAgent.getInstance().onStop();
			
			Bundle bundle = new Bundle();
			bundle.putInt(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_CLEAR_CLIENT_MSG);
			Intent intent = new Intent(Config.ACTION_ON_MAINTAB);
			intent.putExtras(bundle);
			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		V5ClientAgent.getInstance().onDestroy();
		
		// 退出页面时关闭语音播放
    	mRecyclerAdapter.stopVoicePlaying();		
	}
	
	/**
	 * 向会话消息列表尾添加消息
	 * @param message
	 */
	private void addMessage(V5Message message) {
		if (message.getMsg_id() > 0 && message.getMsg_id() < V5ClientAgent.OPEN_QUES_MAX_ID) {
			// 开场消息在保存数据库时++
		} else {
			mOffset++;
		}
		mDatas.add(new V5ChatBean(message));
		mHandler.obtainMessage(UI_LIST_ADD).sendToTarget();
	}
	
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case UI_LIST_ADD: {
			int position = mDatas.size() -  1;
			if (position >= 0) {
//				mRecyclerAdapter.notifyItemInserted(position);
				mRecyclerAdapter.notifyDataSetChanged();
//				mRecyclerView.scrollToPosition(position);
				mRecyclerView.smoothScrollToPosition(position);
			}
			break;
		}
		case UI_LIST_UPDATE: {
			mRecyclerAdapter.notifyDataSetChanged();
			int position = mDatas.size() -  1;
			if (position >= 0) {
//				mRecyclerView.scrollToPosition(position);
				mRecyclerView.smoothScrollToPosition(position);
			}
			if (mRefreshLayout.isRefreshing()) {
				mRefreshLayout.setRefreshing(false);
			}
			break;
		}
		
		case UI_LIST_UPDATE_HISTORICAL: {
			if (mRefreshLayout.isRefreshing()) {
				mRefreshLayout.setRefreshing(false);
			}
			mRecyclerAdapter.notifyDataSetChanged();
			break;
		}
		
		case HDL_VOICE_DISMISS: {
			// 隐藏录音layout[延迟隐藏]
			layout_record.setVisibility(View.GONE);
			break;
		}
		
		case HDL_APP_BACKGROUND:
			if (mApplication.getAppForeground() > 1) {
				foregroundCount--; // 防止触发V5ClientAgent.onStop()
				mApplication.setAppBackground();
			}
			break;
		}
	}

    public static class BaseHandler extends Handler {
		
		WeakReference<ClientChatActivity> mActivity;
		
		public BaseHandler(ClientChatActivity activity) {
			mActivity = new WeakReference<ClientChatActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			mActivity.get().handleMessage(msg);
		}
	}
    
    @Override
	public void onConnect() {
    	Logger.i(TAG, "[onConnect]");
    	getToolbar().setTitle(R.string.service_online);
    	if (!isConnected) { // 仅首次连接成功执行下列操作
        	mOffset = 0;
    		loadMessages(); // 获取会话消息
        	isConnected = true;
    	} else {
    		mDatas.clear();
    		int openNum = mOffset;
    		mOffset = 0;
    		getHistoricalMessages(openNum);
    	}
	}

	@Override
	public void onMessage(String message) {
		Logger.d(TAG, "onMessage:" + message);
	}

	@Override
	public void onMessage(V5Message message) {
		if (null == message) {
			Logger.e(TAG, "Null message object");
			return;
		}
		try {
			Logger.d(TAG, "onMessage<MessageBean>:" + message.toJson().toString());
		} catch (Exception e) { // JSONException
			e.printStackTrace();
		}
		if (message != null) {
			switch (message.getDirection()) {
			case 0: // 普通消息-来自人工
			case 2: // 普通消息-来自机器人
				if (!message.getDefaultContent(this).isEmpty()) {
					addMessage(message);
					if (foregroundCount > 0) {
						CustomApplication.getInstance().noticeMessage(message.getDefaultContent(mApplication));
					}
				}
			break;
			
			case 8: // 相关问题
				if (message.getCandidate() != null) {
					mQuesContents.clear(); // 刷新列表
					for (V5Message msgContent : (List<V5Message>)message.getCandidate()) {
						if (msgContent.getMessage_type() == V5MessageDefine.MSG_TYPE_TEXT) {
							msgContent.setDirection(V5MessageDefine.MSG_DIR_TO_WORKER);
							mQuesContents.add((V5TextMessage)msgContent);
						}
					}
					mQuesAdapter.notifyDataSetChanged();
					mQuestionDesc.setText(R.string.v5_relative_question);
					showQuestionList();
				}
				break;
				
			case 9: // 评价问卷
				
				break;
			}
		}
		if (message.getMsg_id() > 0 && message.getMsg_id() < V5ClientAgent.OPEN_QUES_MAX_ID) {
			this.mOpenAnswer = message;
		}
	}

	@Override
	public void onError(V5KFException error) {
		Logger.e(TAG, "onError " + error.toString());
		//this.isConnected = V5ClientAgent.isConnected();
		if (!V5ClientAgent.isConnected()) {
			switch (error.getStatus()) {
			case ExceptionNoNetwork: // 无网络连接，需要检查网络
				getToolbar().setTitle(R.string.v5_connect_closed);
				if (foregroundCount > 0) {
					showToast(R.string.v5_connect_no_network);
				}
				break;
			case ExceptionConnectionError: // 连接异常出错，会自动重连
				break;
			case ExceptionNotConnected: // 未连接（发送消息时的反馈）
				getToolbar().setTitle(R.string.v5_connect_closed);
				if (foregroundCount > 0) {
					showToast(R.string.v5_connect_timeout);
				}
				break;
			case ExceptionNotInitialized:
				if (V5ClientConfig.getInstance(getApplicationContext()).getShowLog()) {
					showToast("SDK init failed");
				}
				break;
			case ExceptionWSAuthFailed: {
				getToolbar().setTitle(R.string.v5_connect_closed);
				if (foregroundCount > 0) {
					showAlertDialog(R.string.tips, 
							R.string.v5_connect_retry, 
							R.string.retry, 
							R.string.cancel, 
							new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							V5ClientAgent.getInstance().reconnect();
						}
					}, null);
				}
				break;
			}
			default:
				break;
			}
		}
		mHandler.obtainMessage(UI_LIST_UPDATE_HISTORICAL).sendToTarget();
	}

	@Override
	public void onItemClick(View v, int position, int viewType) {
		V5Message message = mDatas.get(position).getMessage();
		if (null == message) {
			Logger.e(TAG, "ViewHolder position:" + position + " has null V5Message");
			return;
		}
		
		if (v.getId() == R.id.id_news_layout) {
			if (ClientChatRecyclerAdapter.TYPE_SINGLE_NEWS == viewType) {
				V5ArticlesMessage msg = (V5ArticlesMessage) message;
				if (msg.getArticles().get(0) != null) {
					onSingleNewsClick(msg.getArticles().get(0).getUrl());
				}
			}
		} else if (v.getId() == R.id.ic_type_img_iv) {
			if (ClientChatRecyclerAdapter.TYPE_IMG_L == viewType || 
					ClientChatRecyclerAdapter.TYPE_IMG_R == viewType) {
				V5ImageMessage imageMsg = (V5ImageMessage) message;
				gotoShowImageActivity(imageMsg.getDefaultPicUrl());
			}
		} else if (v.getId() == R.id.ic_map_img_iv) {
			if (ClientChatRecyclerAdapter.TYPE_LOCATION_L == viewType || 
					ClientChatRecyclerAdapter.TYPE_LOCATION_R == viewType) {
				V5LocationMessage locationMsg = (V5LocationMessage) message;
				gotoLocationMapActivity(locationMsg.getX(), locationMsg.getY());
			}
		} else {
			Logger.d(TAG, "Click to close soft keyboard.");
			DevUtils.hideSoftInputMethod(this);
			if (mKeyBar.isKeyBoardFootShow()) {
				mKeyBar.hideAutoView();
			}
		}
		
//		switch (v.getId()) {
//		case R.id.id_from_msg_text: {
//			break;
//		}
//		case R.id.id_to_msg_text: {				
//			break;
//		}
//		case R.id.id_news_layout: { // 点击单图文
//			if (ClientChatRecyclerAdapter.TYPE_SINGLE_NEWS == viewType) {
//				onSingleNewsClick();
//			}
//			break;
//		}
//		case R.id.ic_type_img_iv: { // 点击图片
//			if (ClientChatRecyclerAdapter.TYPE_IMG_L == viewType || 
//					ClientChatRecyclerAdapter.TYPE_IMG_R == viewType) {
//				if (message == null || message.getMsg_content() == null) {
//					return;
//				}
//				gotoShowImageActivity(message.getMsg_content().getPic_url());
//			}
//			break;
//		}
//		case R.id.ic_map_img_iv: { // 点击地图
//			if (ClientChatRecyclerAdapter.TYPE_LOCATION_L == viewType || 
//					ClientChatRecyclerAdapter.TYPE_LOCATION_R == viewType) {
//				if (message == null || message.getMsg_content() == null) {
//					return;
//				}
//				gotoLocationMapActivity(message.getMsg_content().getX(), message.getMsg_content().getY());
//			}
//			break;
//		}
//		
//		case R.id.id_right_msg_layout:
//		case R.id.id_left_msg_layout:
//		default:
//			Logger.d(TAG, "Click to close soft keyboard.");
//			UIUtil.closeSoftKeyboard(this);
//			if (mKeyBar.isKeyBoardFootShow()) {
//				mKeyBar.hideAutoView();
//			}
//			break;
//		}
	}

	@Override
	public void onItemLongClick(View v, int position, int viewType) {
		
	}
	
	private void gotoLocationMapActivity(double x, double y) {
		Intent intent = new Intent(this, LocationMapActivity.class);
		intent.putExtra("x", x);
		intent.putExtra("y", y);
		gotoActivity(intent);
	}

	private void gotoShowImageActivity(String pic_url) {
		this.gotoImageActivity(pic_url);
	}

	private void onSingleNewsClick(String url) {
		gotoWebViewActivity(url);
	}

//	@Override
//	public void onQuestionSend(int position) {
//		V5Message message = mRobotContents.get(position);
//		if (null == message) {
//			Logger.e(TAG, "onQuestionSend position:" + position + " has null V5Message");
//			return;
//		}
//		sendV5Message(message);
//	}
//
//	@Override
//	public void onQuestionAdd(int position) {
//		V5Message message = mRobotContents.get(position);
//		if (null == message) {
//			Logger.e(TAG, "onQuestionAdd position:" + position + " has null V5Message");
//			return;
//		}
//		mInputEt.setText(message.getDefaultContent(this));
//		mInputEt.setSelection(message.getDefaultContent(this).length());
//	}
//
//	@Override
//	public void onQuestionDel(int position) {
//		V5Message message = mRobotContents.get(position);
//		if (null == message) {
//			Logger.e(TAG, "onQuestionDel position:" + position + " has null V5Message");
//			return;
//		}
//		mInputEt.setText("");
//	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Config.REQUEST_CODE_GET_LOCATION) {
			switch (resultCode) {
			case Config.RESULT_CODE_GET_LOCATION:
				if (data == null) {
					showToast(R.string.on_location_empty);
				} else {
					LocationBean lb = (LocationBean) data.getSerializableExtra("location");
					if (lb == null) {
						showToast(R.string.on_location_empty);
						return;
					}
					V5LocationMessage locationMsg = V5MessageManager.
							obtainLocationMessage(
									lb.getLatitude(), 
									lb.getLongitude(), 
									lb.getAccuracy(), 
									lb.getAddress());
					sendV5Message(locationMsg);
				}
				break;
			}
		} else if (requestCode == Config.REQUEST_CODE_CAMERA || 
				requestCode == Config.REQUEST_CODE_PHOTO_KITKAT ||
				requestCode == Config.REQUEST_CODE_PHOTO) {
			// 必须在this.onStart()之后调用。
			if (mApplication.getAppForeground() > 1) {
				foregroundCount--; // 防止触发V5ClientAgent.onStop()
				mApplication.setAppBackground(); // 防止打开图库使得应用离线
			} else {
				mHandler.sendEmptyMessageDelayed(HDL_APP_BACKGROUND, 200);
			}
//			isForeground--; // 防止触发V5ClientAgent.onStop()
//			mApplication.setAppBackground(); // 防止打开图库使得应用离线
			
			
			if (data != null) {
				// 图库获取拍好的图片
				if (data.getData() != null) { //防止没有返回结果 
					Uri uri = data.getData(); 
					if (uri != null) {
						String filePath = FileUtil.getPath(getApplicationContext(), uri);
						Logger.i(TAG, "Photo:" + filePath);
						// 图片格式验证
						String type = UITools.getImageMimeType(filePath);
						if (!UITools.isValidImageMimeType(type)) {
							ShowToast(String.format(getString(R.string.unsupport_image_type_fmt), type) + getString(R.string.upload_image_tips));
							return;
						}
						// 图片角度矫正
						UITools.correctBitmapAngle(filePath);
						
						V5ImageMessage imageMessage = V5MessageManager.obtainImageMessage(filePath);
						sendV5Message(imageMessage);
					}
				}
			} else if (resultCode == RESULT_OK) {
				// 拍照返回
				String filePath = FileUtil.getImageSavePath(this) + "/" + mImageFileName;
				Logger.i(TAG, "Camera:" + filePath);
				// 图片格式验证
				String type = UITools.getImageMimeType(filePath);
				if (!UITools.isValidImageMimeType(type)) {
					ShowToast(String.format(getString(R.string.unsupport_image_type_fmt), type) + getString(R.string.upload_image_tips));
					return;
				}
				// 图片角度矫正
				UITools.correctBitmapAngle(filePath);
				
				V5ImageMessage imageMessage = V5MessageManager.obtainImageMessage(filePath);
				sendV5Message(imageMessage);
			}
		}
	}

	@Override
	protected void onReceive(Context context, Intent intent) {
		
	}
	
	
	@Override
	public void onMultiNewsClick(View v, int position, int viewType, int newsPos) {
		if (position < mDatas.size()) {
			V5Message message = mDatas.get(position).getMessage();
			if (message.getMessage_type() == V5MessageDefine.MSG_TYPE_ARTICLES) {
				V5ArticlesMessage articles = (V5ArticlesMessage) message;
				if (articles != null && articles.getArticles().size() > newsPos) {
					String url = articles.getArticles().get(newsPos).getUrl();
					gotoWebViewActivity(url);
				}
			}
		}		
	}

	@Override
	public void onRefresh() {
		if (!isConnected) {
			showToast(R.string.waiting_for_connection);
			if (mRefreshLayout.isRefreshing()) {
				mRefreshLayout.setRefreshing(false);
			}
			return;
		}
		if (isHistoricalFinish) {
			showToast(R.string.no_more_msg);
			if (mRefreshLayout.isRefreshing()) {
				mRefreshLayout.setRefreshing(false);
			}
			return;
		}
		getHistoricalMessages(this.numOfMessagesOnRefresh);
	}
	
	/**
	 * 发送消息
	 * @param message
	 */
	private void sendV5Message(V5Message message) {
//		message.setMsg_id(0);
		addMessage(message);
		V5ClientAgent.getInstance().sendMessage(message, new MessageSendCallback() {
			
			@Override
			public void onSuccess(V5Message message) {
				Logger.d(TAG, "V5Message.getState:" + message.getState());
				if (mOpenAnswer != null) {
					DBHelper dbh = new DBHelper(getApplicationContext());
					dbh.insert(mOpenAnswer, true);
					mOffset++;
					mOpenAnswer = null;
				}
				Logger.w(TAG, "sendMessage("+message.getDefaultContent(getApplicationContext())+") success mDatas:" + mDatas.size());
//				for (V5Message msg : mDatas) {
//					Logger.w(TAG, "msg:" + msg.getDefaultContent(getApplicationContext()));
//				}
				
//				mRecyclerAdapter.notifyDataSetChanged();
				mHandler.obtainMessage(UI_LIST_ADD).sendToTarget();
			}
			
			@Override
			public void onFailure(V5Message message, V5KFException.V5ExceptionStatus statusCode, String desc) {
				Logger.e(TAG, "V5Message.getState:" + message.getState() + " exception(" + statusCode + "):" + desc);
//				mRecyclerAdapter.notifyDataSetChanged();
				mHandler.obtainMessage(UI_LIST_ADD).sendToTarget();
			}
		});
	}
	
	/**
	 * 加载历史消息
	 */
	private void loadMessages() {
		Logger.d(TAG, "[loadMessages]");
		if (null == mDatas) {
			mDatas = new ArrayList<V5ChatBean>();
		}
		mDatas.clear();
		getHistoricalMessages(this.numOfMessagesOnOpen);
	}
	
	private void getHistoricalMessages(final int msgSize) {
		V5ClientAgent.getInstance().
			getMessages(
				this, 
				mOffset,
				msgSize,
				new OnGetMessagesCallback() {
		
			@Override
			public void complete(List<V5Message> msgs, int offset, int size, boolean finish) {
				isHistoricalFinish = finish;
				if (msgs != null) {
					for (V5Message msg : msgs) {
						mDatas.add(0, new V5ChatBean(msg));
					}
//					// [修改]排序
//					MessagesCompartor mc = new MessagesCompartor();
//					Collections.sort(mDatas, mc);
					mOffset += msgs.size();
				}
				
				if (mDatas.isEmpty()) {
					V5ClientAgent.getInstance().getOpeningMessage(mOpenMode, mOpenQuestion);
				}
				if (offset == 0 && msgSize > 0) {
					mHandler.obtainMessage(UI_LIST_UPDATE).sendToTarget();
				} else {
					mHandler.obtainMessage(UI_LIST_UPDATE_HISTORICAL).sendToTarget();
				}
			}
		});
	}
	
	/**
     * 打开系统相册
     */
    private void openSystemPhoto() {
    	foregroundCount++; // 防止触发V5ClientAgent.onStop()
    	mApplication.setAppForeground(); // 防止打开图库使得应用离线
    	    	
        Intent intent = new Intent();
//		intent.setType("image/*");
        intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, Config.REQUEST_CODE_PHOTO);
    	
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);//ACTION_OPEN_DOCUMENT  
//        intent.addCategory(Intent.CATEGORY_OPENABLE);  
//        intent.setType("image/*");
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {                  
//        	startActivityForResult(intent, Config.REQUEST_CODE_PHOTO_KITKAT);    
//        } else {                
//        	startActivityForResult(intent, Config.REQUEST_CODE_PHOTO);   
//        }
    }
    
    /**
     * 调用相机拍照
     */
    private void cameraPhoto() {
    	foregroundCount++; // 防止触发V5ClientAgent.onStop()
    	mApplication.setAppForeground(); // 防止打开图库使得应用离线
    	
        String sdStatus = Environment.getExternalStorageState();
        /* 检测sdcard是否可用 */
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            showToast(R.string.v5_no_sdcard);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mImageFileName = FileUtil.getImageName("capture");
        //必须确保文件夹路径存在，否则拍照后无法完成回调 
        File vFile = new File(FileUtil.getImageSavePath(this), mImageFileName);
        File vDirPath = vFile.getParentFile();
        if(!vDirPath.exists()) {
        	vDirPath.mkdirs();
        }
        Uri uri = Uri.fromFile(vFile);
        Logger.d(TAG, " Uri:" + FileUtil.getPath(this, uri));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, Config.REQUEST_CODE_CAMERA);
	}

	@Override
	public void onQuesItemClick(View v, int position, boolean isClicked) {
		V5Message msgContent = mQuesContents.get(position);
		if (null == msgContent) {
			Logger.e(TAG, "onQuesItemClick position:" + position + " has null V5Message");
			return;
		}
		if (isClicked) {
			mInputEt.setText(msgContent.getDefaultContent(this));
			mInputEt.setSelection(msgContent.getDefaultContent(this).length());
		} else {
			mInputEt.setText("");			
		}
	}
	
	// TODO 语音
	
	/**
	 * 长按说话
	 * @ClassName: VoiceTouchListen
	 * @author smile
	 * @date 2015-11-13 下午6:10:16
	 */
	class VoiceTouchListen implements View.OnTouchListener {
		
		private boolean isPressing;
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			v.performClick();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				Logger.i(TAG, "ACTION_DOWN");
				if (!DevUtils.hasPermission(ClientChatActivity.this, "android.permission.RECORD_AUDIO")) {
					showAlertDialog(R.string.v5_permission_record_deny, null);
					return false;
				}
				v.setPressed(true);
				try {
					// 开始录音
					if (!isPressing) {
						isPressing = true;
						mRecorder.startListening();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			case MotionEvent.ACTION_MOVE: {
				if (event.getY() < 0) {
					tv_voice_tips.setText(getString(R.string.chat_voice_cancel_tips));
					//tv_voice_tips.setTextColor(Color.RED);
				} else {
					tv_voice_tips.setText(getString(R.string.chat_voice_up_tips));
					//tv_voice_tips.setTextColor(Color.rgb(0x78, 0x78, 0x78));
				}
				return true;
			}
			case MotionEvent.ACTION_UP:
				Logger.i(TAG, "ACTION_UP");
				v.setPressed(false);
				try {
					if (isPressing) {
						isPressing = false;
						if (event.getY() < 0) { // 放弃录音
							mRecorder.cancel(-1);
							Logger.i(TAG, "放弃发送语音");
						} else { // 结束录音
							Logger.w(TAG, "voice_timer:" + voice_timer + "  voice_timer.millisInCurrent:" + voice_timer.millisInCurrent);
							if (voice_timer != null && voice_timer.millisInCurrent < 1000) {
								
								Logger.i(TAG, "录音时间太短");
								mRecorder.cancel(-2);
							} else {
								mRecorder.stopListening();
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			default:
				return false;
			}
		}
	}
	
	/** 
	 * 继承 CountDownTimer 
	 * 
	 * 重写 父类的方法 onTick() 、 onFinish() 
	 */ 
	class MyCountDownTimer extends CountDownTimer {
		
		protected long  millisInCurrent; // 语音用时
		protected long  millisTotal;	 // 最大总时长
	    /** 
	     * 
	     * @param millisInFuture 
	     *      表示以毫秒为单位 倒计时的总数 
	     * 
	     *      例如 millisInFuture=1000 表示1秒 
	     * 
	     * @param countDownInterval 
	     *      表示 间隔 多少微秒 调用一次 onTick 方法 
	     * 
	     *      例如: countDownInterval =1000 ; 表示每1000毫秒调用一次onTick() 
	     * 
	     */
		public MyCountDownTimer(long millisInFuture, long countDownInterval) { 
			super(millisInFuture, countDownInterval); 
			millisInCurrent = 0;
			millisTotal = millisInFuture;
		} 
	  
		@Override
		public void onFinish() { 
			//
			Logger.i(TAG, "[onFinish]");
			mRecorder.stopListening();
		} 
	  
	    @Override
	    public void onTick(long millisUntilFinished) { // millisUntilFinished 剩余时长
	    	Logger.i(TAG, "[onTick] - " + millisUntilFinished);
	    	millisInCurrent = millisTotal - millisUntilFinished;
	    	tv_voice_second.setText(String.format("%.1f", millisUntilFinished/1000.0f));
	    	if (millisUntilFinished < 10000) {
	    		tv_voice_second.setTextColor(Color.RED); // 剩余时间小于10s时提示
	    	} else {
	    		tv_voice_second.setTextColor(0xff1ec3ff);
	    	}
	    } 
	}

	

	/* 语音 */
	// 更新录音UI状态
	private void showVoiceRecordingProgress() {
		// 显示录音layout
		layout_record.setVisibility(View.VISIBLE);
		tv_voice_tips.setVisibility(View.VISIBLE);
		tv_voice_title.setVisibility(View.VISIBLE);
		tv_voice_tips.setText(getString(R.string.chat_voice_cancel_tips));
		// 录音动画
		Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.round_loading);
		if (operatingAnim != null) {  
			iv_record.startAnimation(operatingAnim);  
		}
		// 开始倒计时
		tv_voice_second.setText("60.0");
		if (voice_timer == null) {
			voice_timer = new MyCountDownTimer(60000, 100);
		}
		voice_timer.start();
		Logger.d(TAG, "[showVoiceRecordingProgress] done");
	}
	
	/**
	 * 隐藏录音进度框
	 * @param state 0:成功  -1:取消 -2:太短
	 */
	private void dismissVoiceRecordingProgress(int state) {
		tv_voice_tips.setVisibility(View.GONE);
		tv_voice_title.setVisibility(View.GONE);
		
		// 停止倒计时
		if (voice_timer != null) {
			voice_timer.cancel();
		}
		Logger.i(TAG, "voice_timer stop");
		// 清除动画
		iv_record.clearAnimation();
		switch (state) {
		case 1:
			tv_voice_second.setText("结束");
			mHandler.sendEmptyMessageDelayed(HDL_VOICE_DISMISS, 400);
			break;
		
		case 0:
			tv_voice_second.setText("成功");
			mHandler.sendEmptyMessageDelayed(HDL_VOICE_DISMISS, 400);
			break;
		case -1:
			tv_voice_second.setText("取消");
			mHandler.sendEmptyMessageDelayed(HDL_VOICE_DISMISS, 400);
			break;
		case -2:
			tv_voice_second.setText("太短");
			mHandler.sendEmptyMessageDelayed(HDL_VOICE_DISMISS, 600);
			break;
		case -3:
			tv_voice_second.setText("出错");
			mHandler.sendEmptyMessageDelayed(HDL_VOICE_DISMISS, 600);
			break;
		}
//		// 隐藏录音layout[延迟隐藏]
//		layout_record.setVisibility(View.GONE);
		Logger.d(TAG, "[dismissVoiceRecordingProgress] done");
	}
	
	@Override
	public void onBeginOfSpeech() {
		Logger.i(TAG, "[onBeginOfSpeech]");
		showVoiceRecordingProgress();
	}

	@Override
	public void onCancelOfSpeech(int state) {
		Logger.i(TAG, "[onCancelOfSpeech]");
		dismissVoiceRecordingProgress(state);
	}

	@Override
	public void onErrorOfSpeech(int errorCode, String desc) {
		Logger.e(TAG, "[onErrorOfSpeech] code("+errorCode+"):" + desc);
		if (layout_record.getVisibility() == View.VISIBLE) {
			dismissVoiceRecordingProgress(-3);
		}
		
		switch (errorCode) {
		case VoiceErrorCode.E_RECORD_NOT_PERMIT: // 可能未取得录音权限
//			showWarningDialog(R.string.error_record_not_permit, null);
			showAlertDialog(R.string.error_record_not_permit);
			break;
		case VoiceErrorCode.E_NOSDCARD: // 存储路径错误
//			showWarningDialog(R.string.error_no_sdcard, null);
			showAlertDialog(R.string.error_no_sdcard);
			break;
		}
	}

	@Override
	public void onResultOfSpeech(String path) {
		Logger.i(TAG, "[onResultOfSpeech] " + path);
		dismissVoiceRecordingProgress(0);
		// 发送语音
		V5VoiceMessage voiceMessage = V5MessageManager.obtainVoiceMessage(path);
		sendV5Message(voiceMessage);
	}
}
