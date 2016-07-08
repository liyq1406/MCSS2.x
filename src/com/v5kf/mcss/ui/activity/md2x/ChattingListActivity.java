package com.v5kf.mcss.ui.activity.md2x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.v5kf.client.lib.V5HttpUtil;
import com.v5kf.client.lib.V5KFException.V5ExceptionStatus;
import com.v5kf.client.lib.V5MessageManager;
import com.v5kf.client.lib.callback.HttpResponseHandler;
import com.v5kf.client.lib.callback.MessageSendCallback;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5LocationMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5TextMessage;
import com.v5kf.client.lib.entity.V5VoiceMessage;
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
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.entity.LocationBean;
import com.v5kf.mcss.entity.SessionBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.service.CoreService;
import com.v5kf.mcss.service.MessageSendHelper;
import com.v5kf.mcss.ui.adapter.ChattingListAdapter;
import com.v5kf.mcss.ui.adapter.ChattingListAdapter.ChatMessagesListener;
import com.v5kf.mcss.ui.adapter.RobotRecyclerAdapter;
import com.v5kf.mcss.ui.entity.ChatRecyclerBean;
import com.v5kf.mcss.ui.view.ActionItem;
import com.v5kf.mcss.ui.view.TitlePopup;
import com.v5kf.mcss.ui.view.TitlePopup.OnItemOnClickListener;
import com.v5kf.mcss.ui.widget.CircleImageView;
import com.v5kf.mcss.utils.DateUtil;
import com.v5kf.mcss.utils.DevUtils;
import com.v5kf.mcss.utils.FileUtil;
import com.v5kf.mcss.utils.IntentUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.V5VoiceRecord;
import com.v5kf.mcss.utils.VoiceErrorCode;
import com.v5kf.mcss.utils.cache.ImageLoader;
import com.v5kf.mcss.utils.cache.URLCache;
//import com.chyrain.EmoticonsKeyBoardBar;
//import com.chyrain.bean.EmoticonBean;
//import com.chyrain.utils.AppBean;
//import com.chyrain.utils.AppsAdapter.FuncItemClickListener;
//import com.chyrain.utils.EmoticonsUtils;
//import com.chyrain.utils.Utils;
//import com.chyrain.view.AppFuncPageView;
//import com.chyrain.view.EmoticonsIndicatorView;
//import com.chyrain.view.I.IView;
//import com.chyrain.emojicon.EmojiconEditText;
//import com.chyrain.view.EmoticonsToolBarView;

/**
 * 
 * @author Chenhy
 *
 */
public class ChattingListActivity extends BaseChatActivity implements ChatMessagesListener, V5VoiceRecord.VoiceRecordListener {
	private static final String TAG = "ChattingListActivity";
	private static final int HDL_VOICE_DISMISS = 101; 
	private static final int HDL_APP_BACKGROUND = 102; 
	
	/* 消息发送工具类 */
	private MessageSendHelper mMessageHelper;

	EmoticonsKeyBoardBar mKeyBar;
    
    private int mQuestionPosition;
    
    private LayoutInflater mInflater;
    private boolean isInTrust;
//    private boolean hasSetIntrust = false;
    
    /* Chat Action Bar */
//	private LinearLayout mLeftLayout;
//	private TextView mTitleTv;
	private ImageView mMoreIv;
	private CircleImageView mCustomerPhotoIv;
	// 标题栏弹窗
	private TitlePopup mTitlePopup;
    
    /* 对话列表 */
//	private PullRefreshRecyclerView mRecyclerView;
//	private RecyclerView mRecyclerView;
//	private OnChatRecyclerAdapter mRecyclerAdapter;
	private ListView mChatListView;
	private ChattingListAdapter mChatListAdapter;
	
	/* 候选回复列表 */
	private RecyclerView mCandidateList;
	/* 推荐回复-瀑布流 */
	private RobotRecyclerAdapter mRobotAdapter;
	private List<ChatRecyclerBean> mRobotDatas;
	
	/* 消息列表 */
	private List<ChatRecyclerBean> mDatas;
//	private V5Message mMessage;
//	private EditText mChatEdit;
	
	private EmojiconEditText mChatEdit;
	
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
//	// 语音听写对象
//	private SpeechRecognizer mIat;
//	// 听写监听器
//	private RecognizerListener mRecoListener;
//	// 用HashMap存储听写结果
//	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	
	// 判断是否使用机器人答案回复
	private boolean isRobot = false;
	private long mPid;
	
	// 拍照、图片
	private String mImageFileName;
		
	@Override
	public void onBackPressed() {
		setFinishType(FIN_TYPE_NONE);
		super.onBackPressed();
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_md2x_chatting_list);

        mMessageHelper = new MessageSendHelper(mCustomer, this, mHandler);
        mDatas = new ArrayList<>();
        initData();
        findView();
        initView();
        /* 更新数据 */
        updateList(false);
		
//		/* 设置监听布局变化(虚拟键盘弹出) */
//		listenerLayoutChange();
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	// 刷新customer
    	mCustomer = mAppInfo.getAliveCustomer(c_id);
    }

    @Override
    protected void onStop() {
    	super.onStop();
    	mKeyBar.setEditableState(false);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	// 退出页面时关闭语音播放
    	if (mChatListAdapter != null) {
    		mChatListAdapter.stopVoicePlaying();
    	}
    }
    
    private void initView() {
    	initChatActionBar();
    	initAppfunc();	// 位置1
    	initCandidate(); // 位置2
    	initChatListView();
    	initKeyboardListener();
    	initBtnVoice();
	}

    /**
     * 初始化标题栏
     */
	private void initChatActionBar() {
		if (!checkCustomer()) {
			if (!Config.DEBUG) {
				return;
			}
			Logger.e(TAG, "checkCustomer failed! null!");
		}
		
		initPopupMenu();
		updateCustomerTitle();
		
		final SessionBean session = mAppInfo.getSessionBean(s_id);
		if (session != null && session.isInTrust()) {
			isInTrust = true;
		} else {
			isInTrust = false;
		}
//		mTrustIv.setImageResource(R.drawable.v5_action_bar_more);
//		mLeftLayout.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				setFinishType(FIN_TYPE_NONE);
//				finishActivity();
//			}
//		});
		mMoreIv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				try {
//					CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, ChattingListActivity.this);
//					if (isInTrust) {
//						mTrustIv.setImageResource(R.drawable.v5_off_trust);
//						cReq.setInTrust(c_id, 0);
//					} else {
//						mTrustIv.setImageResource(R.drawable.v5_in_trust);
//						cReq.setInTrust(c_id, 1);
//					}
//					hasSetIntrust = true;
//					cReq.getInTrust(c_id);
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
				if (isInTrust) {
					mTitlePopup.getAction(0).mTitle = getString(R.string.option_cancel_trust);
					mTitlePopup.getAction(0).mDrawableId = R.drawable.v5_popmenu_in_trust;
				} else {
					mTitlePopup.getAction(0).mTitle = getString(R.string.option_in_trust);
					mTitlePopup.getAction(0).mDrawableId = R.drawable.v5_popmenu_off_trust;
				}
				mTitlePopup.show(v); // 显示弹出框
			}
		});
		mCustomerPhotoIv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				gotoCustomerInfoActivity();
			}
		});
	}
	
	/**
	 * 更新客户信息
	 */
	private void updateCustomerTitle() {
		if (!checkCustomer()) {
			if (!Config.DEBUG) {
				return;
			}
			Logger.e(TAG, "checkCustomer failed! null!");
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
	
	/**
     * 初始化弹出菜单栏
     * @param initPopupMenu MainTabActivity 
     * @return void
     */
    private void initPopupMenu(){
    	// 实例化标题栏弹窗
    	mTitlePopup = new TitlePopup(this, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	
    	// 给标题栏弹窗添加子类
    	mTitlePopup.addAction(new ActionItem(this, 
    			isInTrust ? R.string.option_in_trust : R.string.option_cancel_trust, 
    					isInTrust ? R.drawable.v5_popmenu_off_trust : R.drawable.v5_popmenu_in_trust));
    	mTitlePopup.addAction(new ActionItem(this, R.string.option_switch, R.drawable.v5_popmenu_switch));
    	mTitlePopup.addAction(new ActionItem(this, R.string.option_end_session, R.drawable.v5_popmenu_end_session));
    	
    	mTitlePopup.setItemOnClickListener(new OnItemOnClickListener() {
			
			@Override
			public void onItemClick(ActionItem item, int position) {
				switch (position) {
				case 0:
					Logger.d(TAG, "点击托管");
					try {
						CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, ChattingListActivity.this);
						if (isInTrust) {
							mTitlePopup.getAction(0).mTitle = getString(R.string.option_in_trust);
							mTitlePopup.getAction(0).mDrawableId = R.drawable.v5_popmenu_off_trust;
							cReq.setInTrust(c_id, 0);
						} else {
							mTitlePopup.getAction(0).mTitle = getString(R.string.option_cancel_trust);
							mTitlePopup.getAction(0).mDrawableId = R.drawable.v5_popmenu_in_trust;
							cReq.setInTrust(c_id, 1);
						}
//						hasSetIntrust = true;
						cReq.getInTrust(c_id);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					break;
				case 1:
					Logger.d(TAG, "点击转接");
					Intent intent = IntentUtil.getStartActivityIntent(
							ChattingListActivity.this, 
							WorkerTreeActivity.class, 
							c_id, 
							s_id, 
							mCustomer.getService());
					startActivityForResult(intent, Config.REQUEST_CODE_SERVING_SESSION_FRAGMENT);
					overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
					break;
				case 2:
					Logger.d(TAG, "点击结束会话");
					showAlertDialog(R.string.confirm_end_session, new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							/* 结束会话 */
							try {
								CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, ChattingListActivity.this);
								cReq.endSession(c_id);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}, null);
					break;
				}
			}
		});
	}
    
	private boolean checkCustomer() {
		if (mCustomer == null) {
			mCustomer = mAppInfo.getAliveCustomer(c_id);
		}
		return mCustomer != null;
	}

	private void initData() {
		if (!checkCustomer() || mCustomer.getSession() == null) {
			Logger.e(TAG, "customer.getSession null!");
			if (!Config.DEBUG) {
				return;
			}
			Logger.e(TAG, "checkCustomer failed! null!");
		}
        List<V5Message> msg_list = mCustomer.getSession().getMessageArray();
        if (null == msg_list || msg_list.isEmpty()) {
        	Logger.e(TAG, "Got customer messages null!");
        	return;
        }
        
        for (V5Message msg : msg_list) {
        	addRecyclerBean(msg, true);
        }
        if (mDatas.size() > 0) {
        	mDatas.get(0).setSessionStart(true);
        }
	}
	
	public void findView() {
		/* Chat Title Action Bar */
//		mLeftLayout = (LinearLayout) findViewById(R.id.header_layout_leftview_container);
//		mTitleTv = (TextView) findViewById(R.id.header_htv_subtitle);
		mMoreIv = (ImageView) findViewById(R.id.more_iv);
		mCustomerPhotoIv = (CircleImageView) findViewById(R.id.cstm_photo_iv);
		
        mKeyBar = (EmoticonsKeyBoardBar) findViewById(R.id.ic_chat_activity_root);
        mChatEdit = mKeyBar.getEt_chat();
        
        /* 语音有关 */
        mBtnVoice = mKeyBar.getBtn_voice(); // 长按语音输入按钮
        layout_record = (RelativeLayout) findViewById(R.id.id_mask_view);
		tv_voice_tips = (TextView) findViewById(R.id.tv_voice_tips);
		tv_voice_title = (TextView) findViewById(R.id.tv_voice_title);
		tv_voice_second = (TextView) findViewById(R.id.tv_voice_second);
		iv_record = (ImageView) findViewById(R.id.iv_record);
        
        mKeyBar.setBuilder(EmoticonsUtils.getBuilder(this, false));
        
        /* 添加表情页底部选择栏按钮 */
//		kv_bar.addToolView(com.chyrain.view.R.drawable.icon_face_pop);

//		TextView tvLeft = new TextView(this);
//		tvLeft.setText("LEFT");
//		tvLeft.setGravity(Gravity.CENTER);
//		kv_bar.addFixedView(tvLeft, false);

        /* 表情删除钮 */
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View toolBtnView = mInflater.inflate(R.layout.view_toolbtn_right_simple, null);
        toolBtnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKeyBar.del();
            }
        });
        // [修改]取消emoji只显示qface
        mKeyBar.addFixedView(toolBtnView, true); // 底部栏固定位置的删除按钮

        /* 分组标签-表情 */
//		TextView view2 =  (TextView)inflater.inflate(R.layout.view_test, null);
//		view2.setText("PAGE 3");
//		kv_bar.add(view2);
    }

	/**
	 * 初始化输入库
	 */
	private void initKeyboardListener() {
//		/* [修改]取消emoji，仅一个表情组 表情组选择点击 */
//		mKeyBar.getEmoticonsToolBarView().addOnToolBarItemClickListener(new EmoticonsToolBarView.OnToolBarItemClickListener() {
//            @Override
//            public void onToolBarItemClick(int position) {
////				Toast.makeText(ChattingListActivity.this, "EmoticonsToolBarView Click : " + position, Toast.LENGTH_SHORT).show();
////                if (position == 3) {
////                    kv_bar.show(3);
////                } else if (position == 4) {
////                    kv_bar.show(4);
////                } else if (position == 5) {
////                    kv_bar.show(5);
////                }
//            }
//        });

		/* 表情点击 */
        mKeyBar.getEmoticonsPageView().addIViewListener(new IView() {
            @Override
            public void onItemClick(EmoticonBean bean) {
            	/* 点击某个自定义表情图标:直接显示到对话列表 */
            	if (bean.getEventType() == EmoticonBean.FACE_TYPE_NOMAL) { // 表情图标
            		mChatEdit.updateText();
//            		updateText(mChatEdit);
            	} else if (bean.getEventType() == EmoticonBean.FACE_TYPE_USERDEF) { // 自定义图片表情
            		// 
            	}
            }

            @Override
            public void onItemDisplay(EmoticonBean bean) {

            }

            @Override
            public void onPageChangeTo(int position) {

            }
        });

        /* 多媒体栏按钮点击 */
        mKeyBar.setOnKeyBoardBarViewListener(new EmoticonsKeyBoardBar.KeyBoardBarViewListener() {
            @Override
            public void OnKeyBoardStateChange(int state, final int height) {
            	Logger.i(TAG, "OnKeyBoardStateChange -- 键盘变化height:" + height);
            	mKeyBar.post(new Runnable() {
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
                    		return;
                    	}
                    	Logger.d(TAG, "542 OnKeyBoardStateChange scrollToBottom:" + (mDatas.size() - 1));
                		//listScrollToBottom(false);
                    	updateList(true);
                    }
                });
            }

            @Override
            public void OnSendBtnClick(String msg) { // 发送按钮
    			if (TextUtils.isEmpty(msg)) {
//    				Toast.makeText(ChattingListActivity.this, R.string.send_empty_tip, Toast.LENGTH_SHORT).show();
    				return;
    			}
    			if (mCustomer.getAccessable() != null && mCustomer.getAccessable().equals(QAODefine.ACCESSABLE_AWAY)) {
					ShowToast("客户暂时离开，无法接收消息");
					return;
				}
    			V5TextMessage textMessage = V5MessageManager.obtainTextMessage(msg);
//    			updateOutTextMessage(msg);
    			addMessage(textMessage);    			
    			
    			mKeyBar.clearEditText();
            }

            @Override
            public void OnVideoBtnClick() { // 录音说话按钮点击 
            	
            }

            @Override
            public void OnMultimediaBtnClick() { // "+"
            	
            }
        });
	}
	
	/**
	 * 多功能输入选项页初始化
	 */
	private void initAppfunc() {
		View viewApps =  mInflater.inflate(R.layout.v5_view_apps, null);
        mKeyBar.add(viewApps); // 位置1-添加“+”号打开的功能界面(多媒体输入)
        
        /* 图片、拍照、位置等功能界面 */
        AppFuncPageView pageApps = (AppFuncPageView)viewApps.findViewById(R.id.view_apv);
		EmoticonsIndicatorView indicatorView = (EmoticonsIndicatorView)viewApps.findViewById(R.id.view_eiv);
		pageApps.setIndicatorView(indicatorView);
        ArrayList<AppBean> mAppBeanList = new ArrayList<AppBean>();
        String[] funcArray = getResources().getStringArray(com.chyrain.iminputextension.R.array.v5_apps_func);
        String[] funcIconArray = getResources().getStringArray(com.chyrain.iminputextension.R.array.v5_apps_func_icon);
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
				if (!checkCustomer()) {
					if (!Config.DEBUG) {
						return;
					}
					Logger.e(TAG, "checkCustomer failed! null!");
				}
				if (mCustomer.getAccessable() != null && mCustomer.getAccessable().equals(QAODefine.ACCESSABLE_AWAY)) {
					ShowToast("客户暂时离开，无法接收消息");
					return;
				}
				switch (bean.getId()) {
				case 0: // 常见问答
					getHotQuesAndShow();
					break;
				case 1: // 提问机器人
					gotoRobotChatActivity();
					break;
				case 2: // 图片
					if (mCustomer.getIface() == QAODefine.CSTM_IF_ALIPAY || 
						mCustomer.getIface() == QAODefine.CSTM_IF_QQ) {
						ShowToast(R.string.send_image_unsupport);
					} else {
						if (DevUtils.hasPermission(ChattingListActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
							systemPhoto();
						} else {
							showAlertDialog(R.string.v5_permission_photo_deny, null);
						}
					}
					break;
				case 3: // 拍照
					if (mCustomer.getIface() == QAODefine.CSTM_IF_ALIPAY || 
						mCustomer.getIface() == QAODefine.CSTM_IF_QQ) {
						ShowToast(R.string.send_image_unsupport);
					} else {
						if (DevUtils.hasPermission(ChattingListActivity.this, "android.permission.CAMERA")) {
							cameraPhoto();
						} else {
							showAlertDialog(R.string.v5_permission_camera_deny, null);
						}
					}
					break;
				case 4: // 素材
					gotoImageMaterialActivity();
					break;
				case 5: // 位置
					if (DevUtils.checkAndRequestPermission(ChattingListActivity.this, 
							new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"})) {
						//showProgressDialog();
						gotoLocationMapActivity();
					} else {
						showAlertDialog(R.string.v5_permission_location_deny, null);
					}
					break;
				}
			}
		});
	}
	

	/**
	 * 初始化语音录入功能
	 */
	private void initBtnVoice() {
		mBtnVoice.setOnTouchListener(new VoiceTouchListen());
		mRecorder = new V5VoiceRecord(this, this);
	}
	
	/**
	 * 加载列表，滑动到底部
	 * @param smooth 是否动态滑动
	 */
	private void updateList(boolean smooth) {
		if (smooth) {
			mHandler.sendEmptyMessageDelayed(HDL_WHAT_UPDATE_UI_SMOOTH, 0);
		} else {
			mHandler.sendEmptyMessageDelayed(HDL_WHAT_UPDATE_UI, 0);
		}
	}
	
	private void listScrollToBottom(boolean smooth) {
		Logger.d(TAG, "699 listScrollToBottom smooth:" + smooth);
		if (mDatas.size() > 0) {
			Logger.d(TAG, "701 listScrollToBottom:" + (mDatas.size() - 1));
			if (smooth) {
				mChatListView.setSmoothScrollbarEnabled(true);
				mChatListView.smoothScrollToPosition(mDatas.size() - 1);
			} else {
				mChatListView.setSelection(mDatas.size() - 1);
			}
		}
	}
	
	/** 
	 * 进入提问机器人页面
	 */
	protected void gotoRobotChatActivity() {
		Intent i = new Intent(this, RobotChatActivity.class);
		i.putExtra(Config.EXTRA_KEY_S_ID, s_id);
		i.putExtra(Config.EXTRA_KEY_C_ID, c_id);
		startActivityForResult(i, Config.REQUEST_CODE_ROBOT_CHAT);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	/**
	 * 进入图片素材页面
	 */
	protected void gotoImageMaterialActivity() {
		Intent i = new Intent(this, MaterialResActivity.class);
		i.putExtra("type",  MaterialResActivity.TYPE_IMG);
		startActivityForResult(i, Config.REQUEST_CODE_MATERIAL);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	/** 
	 * 获取常见问答 
	 */
	protected void getHotQuesAndShow() {
		showProgressDialog();
		// 请求常见问答
		final String url= Config.HOT_QUES_URL + Config.SITE_ID;
		// 1.先获取对应的url缓存
		final URLCache urlCache = new URLCache();
		String responseString = urlCache.get(url);
		if (null != responseString) {
			mHandler.post(new UpdateHotQuesRunnable(responseString));
			return;
		}
		
		// 2.无缓存则发起GET请求
		V5HttpUtil.get(url, new HttpResponseHandler(this) {
			@Override
			public void onSuccess(int statusCode, String responseString) {
				urlCache.put(url, responseString);
				mHandler.post(new UpdateHotQuesRunnable(responseString));
			}

			@Override
			public void onFailure(int statusCode, String responseString) {
				mHandler.post(new UpdateHotQuesRunnable(null));
			}
		});
		
//		// 请求常见问答
//		HotReqsHttpClient.get(null, new TextHttpResponseHandler() {			
//			@Override
//			public void onSuccess(int statusCode, Header[] headers,
//					String responseString) {
//				dismissProgressDialog();
//				try {
//					JSONObject resp = new JSONObject(DevUtils.decodeUnicode(responseString));
//					if (resp.get("state").equals("ok") && resp.getInt("total") > 0) {
//						JSONArray arr = resp.getJSONArray("items");
//						mRobotDatas.clear(); // 刷新列表
//						mRobotAdapter.setMode(true);
//						for (int i = 0; i < arr.length(); i++) {
//							String str = arr.getString(i);
//							updateRobotTextMessage(str);
//						}
//						closeSoftKeyboardAndShowChildView(EmoticonsKeyBoardBar.FUNC_CHILLDVIEW_ROBOT);
//					} else {
//						ShowToast(R.string.toast_hot_reqs_empty);
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//			
//			@Override
//			public void onFailure(int statusCode, Header[] headers,
//					String responseString, Throwable throwable) {
//				dismissProgressDialog();
//			}
//		});
	}

	/**
	 * 关闭软键盘输入并显示指定功能页
	 * @param childViewPosition
	 */
	protected void closeSoftKeyboardAndShowChildView(int childViewPosition) {
		mKeyBar.setClicked(true);
		Utils.closeSoftKeyboard(ChattingListActivity.this);
		mKeyBar.showAutoView();
		mKeyBar.show(childViewPosition);
	}

	/** 
	 * 进入定位页面 
	 */
	protected void gotoLocationMapActivity() {
		Intent intent = new Intent(this, LocationMapActivity.class);
		startActivityForResult(intent, Config.REQUEST_CODE_GET_LOCATION);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	/**
	 * 机器人后续回复列表初始化
	 */
	private void initCandidate() {
		View viewRobot =  mInflater.inflate(R.layout.view_robot_candidate, null);
        mKeyBar.add(viewRobot); // 位置2-添加机器人候选答案显示界面
        
        /* 机器人候选答案列表初始化 */
        mCandidateList = (RecyclerView) viewRobot.findViewById(R.id.id_candidate_list);
        /* 适配器初始化 */
        mRobotDatas = new ArrayList<ChatRecyclerBean>();
        mRobotAdapter = new RobotRecyclerAdapter(this, mRobotDatas);
        mCandidateList.setItemAnimator(new DefaultItemAnimator());
        mCandidateList.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mCandidateList.setAdapter(mRobotAdapter);
	}
	
	/**
	 * 对话消息列表初始化
	 */
    private void initChatListView() {
    	 mChatListView = (ListView) findViewById(R.id.id_list_view_msgs);
         mChatListAdapter = new ChattingListAdapter(this, mDatas, this);
         mChatListView.setAdapter(mChatListAdapter);
//         LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//         mRecyclerView.setLayoutManager(layoutManager);
//         mRecycleView.setRefreshLoadMoreListener(this);
//         mRecycleView.setPullRefreshEnable(false);
//         mRecycleView.setPullLoadMoreEnable(false);
//         mRecycleView.setVertical();
         /* 列表空白点击事件 */
         mChatListView.setOnTouchListener(new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				//Logger.d(TAG, "【onTouch】");
 				if (mKeyBar.isKeyBoardFootShow()) { // 拦截触摸列表动作，关闭底部栏
		    		mKeyBar.hideAutoView();
		    		return true;
		    	}
 				switch (event.getAction()) {
 			    case MotionEvent.ACTION_DOWN:
 			    	
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
         
        mChatListView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
 			
 			@Override
 			public void onLayoutChange(View v, int left, int top, int right,
 					int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
 				// TODO Auto-generated method stub
 				Logger.d(TAG, "ChatListView onLayoutChange");
 				mChatListView.postInvalidateDelayed(10);
 			}
 		});
    }
    
    /**
     * 添加数据项到数据源
     * @param addRecycleBean ChattingListActivity 
     * @return void
     * @param msg
     * @param addAtTop
     */
    private void addRecyclerBean(V5Message msg, boolean addAtTop) {
    	if (!checkCustomer()) {
			if (!Config.DEBUG) {
				return;
			}
			Logger.e(TAG, "checkCustomer failed! null!");
		}
		if (msg.getDirection() == QAODefine.MSG_DIR_W2R || msg.getDirection() == QAODefine.MSG_DIR_R2W
				|| msg.getDirection() == QAODefine.MSG_DIR_R2CW
				|| msg.getMessage_type() == QAODefine.MSG_TYPE_CONTROL) { // 过滤非对话消息 -> [修改]过滤全部控制消息
    		return;
    	} else if (msg.getMessage_type() == QAODefine.MSG_TYPE_TEXT && (null == msg.getDefaultContent(this) 
				|| msg.getDefaultContent(this).isEmpty())) { // 过滤空消息
    		return;
    	}
		
		// 添加机器人答复显示在右侧
		if (addAtTop && msg.getCandidate() != null && msg.getCandidate().size() > 0) {
			V5Message robotMsg = msg.cloneDefaultRobotMessage();
			if (robotMsg != null && (robotMsg.getDirection() == QAODefine.MSG_DIR_FROM_ROBOT || 
					robotMsg.getDirection() == QAODefine.MSG_DIR_FROM_WAITING
					|| robotMsg.getDirection() == QAODefine.MSG_DIR_R2WM)) { // 机器人回复
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
    	
    	// 添加机器人答复显示在右侧
		if (!addAtTop && msg.getCandidate() != null && msg.getCandidate().size() > 0) {
			V5Message robotMsg = msg.cloneDefaultRobotMessage();
			if (robotMsg != null && (robotMsg.getDirection() == QAODefine.MSG_DIR_FROM_ROBOT || 
					robotMsg.getDirection() == QAODefine.MSG_DIR_FROM_WAITING
					|| robotMsg.getDirection() == QAODefine.MSG_DIR_R2WM)) { // 机器人回复
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
    	Logger.d(TAG, "[addRecycleBean] +1");
	}
	
	/**
	 * 添加到消息列表并发送消息
	 * @param msg
	 */
	protected void addMessage(V5Message msg) {
		if (!checkCustomer()) {
			if (!Config.DEBUG) {
				return;
			}
			Logger.e(TAG, "checkCustomer failed! null!");
		}
		isMessageAdded = true;
		// 设置消息已知属性
		msg.setDirection(QAODefine.MSG_DIR_TO_CUSTOMER);
		msg.setS_id(mCustomer.getSession().getS_id());
		msg.setC_id(mCustomer.getC_id());
		msg.setState(V5Message.STATE_SENDING);
		
		// 保存消息，加入对话列表
		ChatRecyclerBean toMessage = new ChatRecyclerBean(msg);
		toMessage.setName(mAppInfo.getUser().getDefaultName());
		toMessage.setTime(DateUtil.getCurrentTime());
		mDatas.add(toMessage);
		mCustomer.getSession().addMessage(msg, true);
		updateList(true);
		
		if (isRobot && mPid != 0) {
			msg.setP_id(mPid);
			isRobot = false;
			mPid = 0;
		}
		sendMessage(msg);
	}
	
	private void sendMessage(V5Message msg) {
		Logger.i(TAG, "sendMessage mPid:" + mPid + " isRobot:" + isRobot);
		mMessageHelper.sendMessage(msg, new MessageSendCallback() {
			
			@Override
			public void onSuccess(V5Message message) {
				Logger.i(TAG, "sendMessage -> onSuccess");
				updateList(false);
			}
			
			@Override
			public void onFailure(V5Message message, V5ExceptionStatus statusCode,
					String desc) {
				Logger.e(TAG, "sendMessage -> onFailure");
				updateList(false);
			}
		});
	}
	
	/**
	 * 添加到消息列表并发送消息
	 * @param imageMessage
	 */
	protected void addImageMessage(V5ImageMessage imageMessage) {
		if (!checkCustomer()) {
			if (!Config.DEBUG) {
				return;
			}
			Logger.e(TAG, "checkCustomer failed! null!");
		}
		isMessageAdded = true;
		// 设置消息已知属性
		imageMessage.setDirection(QAODefine.MSG_DIR_TO_CUSTOMER);
		imageMessage.setS_id(mCustomer.getSession().getS_id());
		imageMessage.setC_id(mCustomer.getC_id());
		imageMessage.setState(V5Message.STATE_SENDING);
		
		// 保存消息，加入对话列表
		ChatRecyclerBean toMessage = new ChatRecyclerBean(imageMessage);
		toMessage.setName(mAppInfo.getUser().getDefaultName());
		toMessage.setTime(DateUtil.getCurrentTime());
		
		mDatas.add(toMessage);
		mCustomer.getSession().addMessage(imageMessage, true);
		updateList(true);
		Logger.w(TAG, "sendImageMessage mDatas:" + mDatas.size());
		
		if (isRobot && mPid != 0) {
			imageMessage.setP_id(mPid);
			isRobot = false;
			mPid = 0;
		}
		sendImageMessage(imageMessage);
	}
	
	private void sendImageMessage(V5ImageMessage imageMessage) {
		mMessageHelper.sendImageMessage(imageMessage, new MessageSendCallback() {
			
			@Override
			public void onSuccess(V5Message message) {
				Logger.i(TAG, "sendImageMessage -> onSuccess");
				updateList(false);
			}
			
			@Override
			public void onFailure(V5Message message, V5ExceptionStatus statusCode,
					String desc) {
				Logger.e(TAG, "sendImageMessage -> onFailure");
				updateList(false);
			}
		});
	}
	
	/**
	 * 添加到消息列表并发送消息
	 * @param imageMessage
	 */
	protected void addVoiceMessage(V5VoiceMessage voiceMessage) {
		if (!checkCustomer()) {
			if (!Config.DEBUG) {
				return;
			}
			Logger.e(TAG, "checkCustomer failed! null!");
		}
		isMessageAdded = true;
		// 设置消息已知属性
		voiceMessage.setDirection(QAODefine.MSG_DIR_TO_CUSTOMER);
		voiceMessage.setS_id(mCustomer.getSession().getS_id());
		voiceMessage.setC_id(mCustomer.getC_id());
		voiceMessage.setState(V5Message.STATE_SENDING);
		Logger.i(TAG, "sendVoiceMessage -> start -> STATE_SENDING " + voiceMessage.getState());
		// 保存消息，加入对话列表
		ChatRecyclerBean toMessage = new ChatRecyclerBean(voiceMessage);
		toMessage.setName(mAppInfo.getUser().getDefaultName());
		toMessage.setTime(DateUtil.getCurrentTime());
		
		mDatas.add(toMessage);
		mCustomer.getSession().addMessage(voiceMessage, true);
		updateList(true);
		Logger.w(TAG, "sendVoiceMessage mDatas:" + mDatas.size());
		
		if (isRobot && mPid != 0) {
			voiceMessage.setP_id(mPid);
			isRobot = false;
			mPid = 0;
		}
		sendVoiceMessage(voiceMessage);
	}
	
	private void sendVoiceMessage(V5VoiceMessage voiceMessage) {
		mMessageHelper.sendVoiceMessage(voiceMessage, new MessageSendCallback() {
			
			@Override
			public void onSuccess(V5Message message) {
				Logger.i(TAG, "sendVoiceMessage -> onSuccess " + message.getState());
				updateList(false);
			}
			
			@Override
			public void onFailure(V5Message message, V5ExceptionStatus statusCode,
					String desc) {
				Logger.e(TAG, "sendVoiceMessage -> onFailure " + message.getState());
				updateList(false);
			}
		});
	}
	
	/**
	 * 将机器人答案添加到编辑框中
	 * @param showCandidate ChattingListActivity 
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
		if (text.isEmpty()) {
			isRobot = false;
		} else {
			isRobot = true;
		}
		mChatEdit.setText(text);
		mChatEdit.setSelection(text.length());
		mChatEdit.updateText();
	}
	
	/**
	 * 机器人推荐回复获取
	 * @param updateRobotTextMessage ChattingListActivity 
	 * @return void
	 * @param msgStr
	 */
	private void updateRobotTextMessage(String msgStr) {
		V5TextMessage new_msg = V5MessageManager.obtainTextMessage(msgStr);
		new_msg.setS_id(s_id);
		new_msg.setC_id(c_id);
		ChatRecyclerBean robotMessage = new ChatRecyclerBean(new_msg);
		robotMessage.setName(mAppInfo.getUser().getDefaultName());
		
		mRobotDatas.add(robotMessage);		
		notifyRobotDataSetChange();
	}
	
	/**
	 * 刷新机器人回复列表(指定位置的消息)
	 * @param updateRobotMessage ChattingListActivity 
	 * @return void
	 * @param position
	 */
	private void updateRobotMessage(int position) {
		if (position < 0 || position >= mDatas.size()) {
			Logger.w(TAG, "[updateRobotMessage] 来自" + position + "的消息不存在");
			return;
		}
		mRobotAdapter.setMode(false);
		mRobotDatas.clear(); // 刷新列表
		ChatRecyclerBean bean = mDatas.get(position);
		mPid = bean.getMessage().getP_id();
		if (bean != null && bean.getMessage() != null && bean.getMessage().getCandidate() != null) {
			for (V5Message content : bean.getMessage().getCandidate()) {
				Logger.i(TAG, "[消息类型]：" + content.getMessage_type());
				content.setS_id(bean.getMessage().getS_id());
				content.setC_id(bean.getMessage().getC_id());
				
				ChatRecyclerBean robotMessage = new ChatRecyclerBean(content);
				robotMessage.setName(mAppInfo.getUser().getDefaultName());				
				mRobotDatas.add(robotMessage);
				Logger.i(TAG, "[updateRobotMessage] Message_type:" + robotMessage.getMessage().getMessage_type());
			}
			
			// 显示机器人推荐
			closeSoftKeyboardAndShowChildView(EmoticonsKeyBoardBar.FUNC_CHILLDVIEW_ROBOT);
		}
		notifyRobotDataSetChange();
	}
	
	/**
	 * 刷新机器人推荐列表(指定消息对象)
	 * @param updateRobotMessage ChattingListActivity 
	 * @return void
	 * @param message
	 */
	private void updateRobotMessage(V5Message message) {
		if (message == null || message.getCandidate() == null) {
			Logger.w(TAG, "[updateRobotMessage] 来自robot的消息为null");
			return;
		}
		mRobotAdapter.setMode(false);
		mRobotDatas.clear(); // 刷新列表
		for (V5Message content : message.getCandidate()) {
			Logger.i(TAG, "[消息类型]：" + message.getMessage_type());
			content.setS_id(message.getS_id());
			content.setC_id(message.getC_id());
			
			ChatRecyclerBean robotMessage = new ChatRecyclerBean(content);
			robotMessage.setName(mAppInfo.getUser().getDefaultName());				
			mRobotDatas.add(robotMessage);
			Logger.i(TAG, "[updateRobotMessage] Message_type:" + robotMessage.getMessage().getMessage_type());
		}
		notifyRobotDataSetChange();
	}
	
	/**
	 * 处理Handler消息队列消息
	 */
	@Override
	protected void handleMessage(Message msg) {
		Log.d(TAG, "handleMessage:" + msg.what + " size:" + mDatas.size());		
		switch (msg.what) {
		case HDL_WHAT_UPDATE_UI_SMOOTH: // 滚动更新消息列表
			notifyChatDataSetChange();
			Logger.d(TAG, "1257 HDL_WHAT_UPDATE_UI scrollToBottom:" + (mDatas.size() - 1));
			listScrollToBottom(true);
			break;

		case HDL_WHAT_UPDATE_UI: // 更新消息列表
			notifyChatDataSetChange();
			Logger.d(TAG, "1257 HDL_WHAT_UPDATE_UI scrollToBottom:" + (mDatas.size() - 1));
			listScrollToBottom(false);
			break;
			
		case HDL_WHAT_REFRESH_HISTORY:
//			mRecycleView.stopRefresh();
			break;
			
		case HDL_WHAT_SHOW_ROBOT_MENU: // 显示底部机器人推荐栏
			int type = msg.getData().getInt(MSG_KEY_TYPE, 0);
			mQuestionPosition = msg.getData().getInt(MSG_KEY_POSITION, -1);
			if (type == MSG_KEY_TYPE_STR) {
				int num = msg.getData().getInt(MSG_KEY_RESPONSE_NUM, 1);
				if (num == 1) {
					String resp = msg.getData().getString(MSG_KEY_RESPONSE);
					Logger.d(TAG, "MSG_KEY_RESPONSE:" + resp);
					updateRobotTextMessage(resp);
				} else if (num > 1) {
					//String resp[] = msg.getData().getStringArray(MSG_KEY_RESPONSE);
				}
			} else if (type == MSG_KEY_TYPE_R) {
				updateRobotMessage(mQuestionPosition);
			}
			closeSoftKeyboardAndShowChildView(EmoticonsKeyBoardBar.FUNC_CHILLDVIEW_ROBOT);
			break;
		
		case HDL_WHAT_HIDE_BOTTOM: // 隐藏底部栏
			mKeyBar.hideAutoView();
			break;
			
		case HDL_WHAT_CANDIDATE_ADD: { 
			Bundle bundle = msg.getData();
			int pos = bundle.getInt(MSG_KEY_POSITION);
			mRobotDatas.get(pos).setSelected(true);
			showCandidate();
			break;
		}
		
		case HDL_WHAT_CANDIDATE_DEL: {
			Bundle bundle = msg.getData();
			int pos = bundle.getInt(MSG_KEY_POSITION);
			mRobotDatas.get(pos).setSelected(false);
			showCandidate();
			break;
		}
		
		case HDL_WHAT_CANDIDATE_SEND: {
			Bundle bundle = msg.getData();
			int pos = bundle.getInt(MSG_KEY_POSITION);
			V5Message robotMsg = mRobotDatas.get(pos).getMessage();
//			updateOutMessage(msgContent);
			isRobot = true;
			addMessage(robotMsg);
			break;
		}
		
		case HDL_VOICE_DISMISS:
			// 隐藏录音layout[延迟隐藏]
			layout_record.setVisibility(View.GONE);
			break;
			
		case HDL_APP_BACKGROUND:
			if (mApplication.getAppForeground() > 1) {
				mApplication.setAppBackground();
			}
			break;
		}
	}

	/**
	 * Activity结束回调
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Config.REQUEST_CODE_GET_LOCATION) {
	    	dismissProgressDialog();
	    	switch (resultCode) {
			case Config.RESULT_CODE_GET_LOCATION:
				if (data == null) {
					ShowToast(R.string.on_location_empty);
				} else {
					LocationBean lb = (LocationBean) data.getSerializableExtra("location");
					if (lb == null) {
						ShowToast(R.string.on_location_empty);
						return;
					}
					V5LocationMessage locationMsg = V5MessageManager.
							obtainLocationMessage(
									lb.getLatitude(), 
									lb.getLongitude(), 
									lb.getAccuracy(), 
									lb.getAddress());
					addMessage(locationMsg);
				}
				break;
			}
		} else if (requestCode == Config.REQUEST_CODE_CUSTOMER_INFO) {
			if (resultCode == Config.RESULT_CODE_END_SESSION) {
				setFinishType(FIN_TYPE_NONE);
				finish();
			}
		} else if (requestCode == Config.REQUEST_CODE_ROBOT_CHAT) {
			if (resultCode == Config.RESULT_CODE_FORWARD_MSG && data != null) {
				// 提问结果发送
				V5Message message = (V5Message) data.getSerializableExtra("message");
				addMessage(message);
			}
		} else if (requestCode == Config.REQUEST_CODE_MATERIAL) {
			if (resultCode == Config.RESULT_CODE_MATERIAL_SEND && data != null) {
				// 素材发送
				V5Message messageContent = (V5Message) data.getSerializableExtra("message_content");
				//updateOutMessage(messageContent);
				addMessage(messageContent);
			}
		} else if (requestCode == Config.REQUEST_CODE_CAMERA || 
				requestCode == Config.REQUEST_CODE_PHOTO_KITKAT ||
				requestCode == Config.REQUEST_CODE_PHOTO) {
			// 必须在this.onStart()之后调用，需延时。
			if (mApplication.getAppForeground() > 1) {
				mApplication.setAppBackground(); // 防止打开图库使得应用离线
			} else {
				mHandler.sendEmptyMessageDelayed(HDL_APP_BACKGROUND, 200);
			}
			
			if (data != null) {
				// 图库获取拍好的图片
				if (data.getData() != null) { //防止没有返回结果 
					Uri uri = data.getData();
					if (uri != null) {
						String filePath = FileUtil.getPath(getApplicationContext(), uri);
						Logger.i(TAG, "Photo:" + filePath);
						// 图片角度矫正
						UITools.correctBitmapAngle(filePath);
						V5ImageMessage imageMessage = V5MessageManager.obtainImageMessage(filePath);
						addImageMessage(imageMessage);
					}
				}
			} else if (resultCode == RESULT_OK) {
				// 拍照返回
				String filePath = FileUtil.getImageSavePath(this) + "/" + mImageFileName;
				Logger.i(TAG, "Camera:" + filePath);
				// 图片角度矫正
				UITools.correctBitmapAngle(filePath);
				V5ImageMessage imageMessage = V5MessageManager.obtainImageMessage(filePath);
				addImageMessage(imageMessage);
			}
		} else if (requestCode == Config.REQUEST_CODE_SWITCH_CSTM) { // 转接返回
			if (Config.RESULT_CODE_SWITCH_OK == resultCode) { // 转接成功
				//finishActivity(); // 转接成功会收到CSTM_JOIN_OUT
			}
		}
	}
	
//	/**
//	 * 打卡查看图片页
//	 * @param reqCode
//	 * @param imgPath
//	 */
//	private void gotoPhotoPreview(int reqCode, String imgPath) {
//		Logger.d(TAG, "[editBitmapAndSend]");
//		Intent intent = new Intent(this, PhotoPreviewActivity.class);
//		intent.putExtra("reqCode", reqCode);
//		intent.putExtra("imgPath", imgPath);
//		startActivityForResult(intent, Config.REQUEST_CODE_PHOTO_PREV);
//	}

	/**
     * 打开系统相册
     */
    private void systemPhoto() {
    	mApplication.setAppForeground(); // 防止打开图库使得应用离线
    	
//        Intent intent = new Intent();
////		intent.setType("image/*");
//        intent.setDataAndType(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                "image/*");
//        intent.setAction(Intent.ACTION_PICK);
//        startActivityForResult(intent, Config.REQUEST_CODE_PHOTO);
        
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);//ACTION_OPEN_DOCUMENT  
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {                  
        	startActivityForResult(intent, Config.REQUEST_CODE_PHOTO_KITKAT);    
        } else {
        	startActivityForResult(intent, Config.REQUEST_CODE_PHOTO);   
        }
    }
 
    /**
     * 调用相机拍照
     */
    private void cameraPhoto() {
    	mApplication.setAppForeground(); // 防止打开图库使得应用离线
    	
        String sdStatus = Environment.getExternalStorageState();
        /* 检测sdcard是否可用 */
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "SD卡不可用！", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mImageFileName = FileUtil.getImageName("capture");        
        // 必须确保文件夹路径存在，否则拍照后无法完成回调 
        File vFile = new File(FileUtil.getImageSavePath(this), mImageFileName);
        File vDirPath = vFile.getParentFile();
        if(!vDirPath.exists()) {
        	vDirPath.mkdirs();
        }
        Uri uri = Uri.fromFile(vFile);
        Logger.d(TAG, "保存 Uri：" + FileUtil.getPath(this, uri));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, Config.REQUEST_CODE_CAMERA);
	}
    
    class UpdateHotQuesRunnable implements Runnable {

		private String responseStr;
		public UpdateHotQuesRunnable(String rst) {
			responseStr = rst;
		}
		
		@Override
		public void run() {
			dismissProgressDialog();
			if (null == responseStr) {
				return;
			}
			try {
				JSONObject resp = new JSONObject(DevUtils.decodeUnicode(responseStr));
				if (resp.get("state").equals("ok") && resp.getInt("total") > 0) {
					JSONArray arr = resp.getJSONArray("items");
					mRobotDatas.clear(); // 刷新列表
					mRobotAdapter.setMode(true);
					for (int i = 0; i < arr.length(); i++) {
						String str = arr.getString(i);
						updateRobotTextMessage(str);
					}
					closeSoftKeyboardAndShowChildView(EmoticonsKeyBoardBar.FUNC_CHILLDVIEW_ROBOT);
				} else {
					ShowToast(R.string.toast_hot_reqs_empty);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
    
	/**
	 * 长按说话（讯飞语音输入控制）
	 * @ClassName: VoiceTouchListen
	 * @Description: null
	 * @author smile
	 * @date 2014-7-1 下午6:10:16
	 */
	class VoiceTouchListen implements View.OnTouchListener {
		
		private boolean isPressing;
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			v.performClick();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				Logger.e(TAG, "ACTION_DOWN");
				if (!DevUtils.hasPermission(ChattingListActivity.this, "android.permission.RECORD_AUDIO")) {
					showAlertDialog(R.string.v5_permission_record_deny, null);
					return false;
				}
				if (mCustomer.getAccessable() != null && mCustomer.getAccessable().equals(QAODefine.ACCESSABLE_AWAY)) {
					ShowToast("客户暂时离开，无法接收消息");
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
				Logger.e(TAG, "ACTION_UP");
				v.setPressed(false);
				try {
					if (isPressing) {
						isPressing = false;
						if (event.getY() < 0) { // 放弃录音
							mRecorder.cancel(-1);
							Logger.i(TAG, "放弃发送语音");
						} else { // 结束录音
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
		if (mCustomer.getAccessable() != null && mCustomer.getAccessable().equals(QAODefine.ACCESSABLE_AWAY)) {
			ShowToast("客户暂时离开，无法接收消息");
			return;
		}
		// 发送语音
		V5VoiceMessage voiceMessage = V5MessageManager.obtainVoiceMessage(path);
		addVoiceMessage(voiceMessage);
	}
	
	/** 
	 * 继承 CountDownTimer 
	 * 
	 * 重写 父类的方法 onTick() 、 onFinish() 
	 */ 
	class MyCountDownTimer extends CountDownTimer {
		
		protected long  millisInCurrent;
		protected long  millisTotal;
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
	    public void onTick(long millisUntilFinished) { 
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
	
	/* [修改]解决屏幕刷新黑块问题 */
	private void notifyRobotDataSetChange() {
		mRobotAdapter.notifyDataSetChanged();
		mCandidateList.postInvalidateDelayed(10);
	}

	private void notifyChatDataSetChange() {
		mChatListAdapter.notifyDataSetChanged();
		mChatListView.postInvalidateDelayed(10);
	}
	
	/***** event *****/

	@Subscriber(tag = EventTag.ETAG_CSTM_OUT, mode = ThreadMode.MAIN)
	private void updateCustomerOut(CustomerBean cstm) {
		Logger.d(TAG + "-eventbus", "updateCustomerOut -> ETAG_CSTM_OUT");
		if (cstm.getC_id().equals(mCustomer.getC_id())) {
			//UIUtil.noticeEndSession(this, cstm.getClosingReason());
			finishActivity();
		}
	}
	
	@Subscriber(tag = EventTag.ETAG_MESSAGE_ARRAY_CHANGE, mode = ThreadMode.MAIN)
	private void messageArrayChange(SessionBean session) {
		Logger.d(TAG + "-eventbus", "messageArrayChange -> ETAG_MESSAGE_ARRAY_CHANGE");
		if (session.getC_id().equals(c_id)) {
			if (!checkCustomer()) {
				Logger.e(TAG, "checkCustomer failed! null! 1609");
			}
			mDatas.clear();
			initData();
			/* 更新数据 */
			updateList(false);
		}
	}

	@Subscriber(tag = EventTag.ETAG_NEW_MESSAGE, mode = ThreadMode.MAIN)
	private void newMessage(V5Message message) {
		Logger.d(TAG + "-eventbus", "newMessage -> ETAG_NEW_MESSAGE");
		if (!this.s_id.equals(message.getS_id())) {
			return;
		}
		if (!checkCustomer()) {
			if (!Config.DEBUG) {
				return;
			}
			Logger.e(TAG, "checkCustomer failed! null!");
		}
		
		List<V5Message> msg_list = mCustomer.getSession().getMessageArray();
		if (msg_list == null) {
			Logger.e(TAG, "NEW_MESSAGE: null MessageBean list");
			return;
		}
		V5Message msg = msg_list.get(0);
		addRecyclerBean(msg, false);

		//mRecyclerAdapter.notifyItemInserted(mDatas.size() - 1);
		notifyChatDataSetChange();
		Logger.d(TAG, "1700 newMessage scrollToBottom:" + (mDatas.size() - 1));
		listScrollToBottom(true);
		isMessageAdded = true;
		
		// 推荐回复列表更新
		Logger.i(TAG, "NEW_MESSAGE: MessageBean:" + msg.getDefaultContent(this));
		updateRobotMessage(mDatas.size() - 1);
	}

	@Subscriber(tag = EventTag.ETAG_ROBOT_ANSWER, mode = ThreadMode.MAIN)
	private void robotAnswer(V5Message message) {
		if (!this.s_id.equals(message.getS_id())) {
			return;
		}
		closeSoftKeyboardAndShowChildView(EmoticonsKeyBoardBar.FUNC_CHILLDVIEW_ROBOT);
		if (message.getDirection() == QAODefine.MSG_DIR_R2W) {
			updateRobotMessage(message);
		} else if (message.getDirection() == QAODefine.MSG_DIR_W2R) {
			updateRobotMessage(message);
		}
	}
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		if (isConnect) {
			
		} else {
			finishActivity();
		}
	}

	@Subscriber(tag = EventTag.ETAG_IN_TRUST_CHANGE, mode = ThreadMode.MAIN)
	private void inTrustChange(SessionBean session) {
		if (session.getS_id().equals(s_id)) {
			isInTrust = session.isInTrust();
//			if (isInTrust) {
//				ShowToast(R.string.toast_in_trust);
//			} else {
//				ShowToast(R.string.toast_off_trust);
//			}
//			hasSetIntrust = false;
		}
	}

	@Override
	public void resendFailureMessage(V5Message message, int position) {
		CoreService.reConnect(this);
		message.setState(V5Message.STATE_SENDING);
		if (message.getMessage_type() == QAODefine.MSG_TYPE_IMAGE) {
			sendImageMessage((V5ImageMessage)message);
		} else {
			sendMessage(message);
		}
		notifyChatDataSetChange();
	}

	/***** event *****/

	@Subscriber(tag = EventTag.ETAG_UPDATE_CSTM_INFO, mode = ThreadMode.MAIN)
	private void updateCustomerInfo(CustomerBean cstm) {
		Logger.d(TAG + "-eventbus", "updateCustomerInfo -> ETAG_UPDATE_CSTM_INFO");
		if (cstm.getC_id().equals(c_id)) {
			if (cstm != mCustomer) {
				mCustomer = cstm;
			}
			updateCustomerTitle();
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
