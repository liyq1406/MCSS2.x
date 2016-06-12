package com.v5kf.mcss.ui.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.v5kf.client.lib.V5HttpUtil;
import com.v5kf.client.lib.callback.HttpResponseHandler;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.Config.ReloginReason;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.entity.SiteInfo;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.service.CoreService;
import com.v5kf.mcss.service.NetworkManager;
import com.v5kf.mcss.ui.activity.md2x.CustomLoginActivity;
import com.v5kf.mcss.ui.fragment.BaseFragment;
import com.v5kf.mcss.ui.fragment.ContactFragment;
import com.v5kf.mcss.ui.fragment.MineFragment;
import com.v5kf.mcss.ui.fragment.SessionFragment;
import com.v5kf.mcss.ui.view.ActionItem;
import com.v5kf.mcss.ui.view.HeaderLayout.onRightImageButtonClickListener;
import com.v5kf.mcss.ui.view.NonSlipViewPager;
import com.v5kf.mcss.ui.view.TabView;
import com.v5kf.mcss.ui.view.TitlePopup;
import com.v5kf.mcss.ui.view.TitlePopup.OnItemOnClickListener;
import com.v5kf.mcss.ui.widget.ModeSeekbarDialog;
import com.v5kf.mcss.utils.DevUtils;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.cache.URLCache;
//[取消]讯飞语音SDK

/**
 *
 * @author Chenhy	
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-1 下午7:03:45
 * @package com.v5kf.mcss.ui.activity of MCSS-Native
 * @file HomeActivity.java 
 *
 */
public class V51xTabActivity extends BaseActivity {
	private static final String TAG = "MainTabActivity";
	public static final int WHAT_START_CHAT_CONTENT_ACTIVITY = 1;
	public static final int WHAT_START_MORE_ACTIVITY = 2;
	public static final int WHAT_SEND_REQUEST = 3;
	public static final String MSG_KEY_REQUEST = "request";
	public static final String MSG_KEY_S = "request";

    private Map<Integer, BaseFragment> mFragmentMap;
    private int[] mTitles = {
    		R.string.tabhost_text_tag1,
    		R.string.tabhost_text_tag2,
//    		R.string.tabhost_text_tag3,
    		R.string.tabhost_text_tag4};
    private int[] mIconsSelect = {
    		R.drawable.v5_tab_session_highlight, 
    		R.drawable.v5_tab_contact_highlight, 
//    		R.drawable.ic_tab3_highlight, 
    		R.drawable.v5_tab_mine_highlight};
    private int[] mIconsNormal = {
    		R.drawable.v5_tab_session_normal, 
    		R.drawable.v5_tab_contact_normal, 
//    		R.drawable.ic_tab3_normal, 
    		R.drawable.v5_tab_mine_normal};
	public static int mCurrentPage; // 0~3表示Tab从左到右
	
	private View mHeaderTips;
    
    private TabView mTabView;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    NonSlipViewPager mViewPager;    
    // 定义标题栏弹窗按钮
  	private TitlePopup mTitlePopup;
  	// 定义SeekBar弹窗
  	private ModeSeekbarDialog mSeekBarDialog;
  	
  	// 服务绑定
  	private ServiceConnection mSconnection;
  	private CoreService mBindService;
  	
  	// 在线咨询消息广播
  	private ClientServiceReceiver mClientReceiver;
  	
    private void handleIntent() {
    	Logger.d(TAG, "handleIntent");    	
		Intent intentNotify = getIntent();
		Bundle extras = intentNotify.getExtras();
		int type = intentNotify.getIntExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_NULL);
		if (Config.EXTRA_TYPE_NOTIFY == type) {	
			Logger.d(TAG, "EXTRA_TYPE_NOTIFY");
			if (mViewPager != null) {
				mViewPager.setCurrentItem(0);
			}
			int notifyType = intentNotify.getIntExtra(Config.EXTRA_KEY_NOTIFY_TYPE, 0);
			Logger.w(TAG, "Notify -> Intent -> MainTabActivity:" + notifyType);
			switch (notifyType) {
			case Config.EXTRA_NOTIFY_TYPE_MESSAGE_SRV:
				startChatContentActivity(extras);
			case Config.EXTRA_NOTIFY_TYPE_JOIN_IN:
				if (mFragmentMap != null) {
					Message msg = new Message();
					msg.what = SessionFragment.WHAT_SWITCG_TO_SEGMENT_1;
					getFragment(0).sendHandlerMessage(msg);
				}
				break;

			case Config.EXTRA_NOTIFY_TYPE_MESSAGE_WAIT:
				startChatMessagesActivity(extras);
			case Config.EXTRA_NOTIFY_TYPE_WAIT_IN:
				if (mFragmentMap != null) {
					Message msg = new Message();
					msg.what = SessionFragment.WHAT_SWITCG_TO_SEGMENT_2;
					getFragment(0).sendHandlerMessage(msg);
				}
				break;
			}
		}
	}

    private void startChatContentActivity(Bundle extras) {
    	Intent intentChat = new Intent(this, ChattingListActivity.class);
    	intentChat.putExtras(extras);
		startActivityForResult(intentChat, Config.REQUEST_CODE_SERVING_SESSION_FRAGMENT);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

    private void startChatMessagesActivity(Bundle extras) {
    	Intent intentChat = new Intent(this, ChatMessagesActivity.class);
    	intentChat.putExtras(extras);
    	startActivityForResult(intentChat, Config.REQUEST_CODE_SERVING_SESSION_FRAGMENT);
    	overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	Logger.w(TAG, "onNewIntent");
    	super.onNewIntent(intent);
    	setIntent(intent);
    	handleIntent();
    }
    
    @Override
    public void onBackPressed() {
    	moveTaskToBack(true);
//    	mApplication.stopActivities();
//    	finish();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// super.onSaveInstanceState(outState);
    }

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);
        Logger.v(TAG, "[onCreate]");
        // 不可滑动返回
        setSwipeBackEnable(false);
        
        handleIntent();
        initData();
        findView();
        initView();
        loadData();
		
        //[取消]讯飞语音SDK
		/* 初始化讯飞语音sdk */
		SpeechUtility.createUtility(this, SpeechConstant.APPID + "=" + getString(R.string.xfyun_app_id));
		Setting.setShowLog(false);
		
//		/* 未启动则开启CoreService */
//		if (!IntentUtil.isServiceWork(this, Config.SERVICE_NAME_CS)) {
//			Intent localIntent = new Intent();
//			localIntent.setAction(Config.ACTION_CORE);
//			localIntent.setPackage("com.v5kf.mcss");
//			localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//	        startService(localIntent);
//		}
//        
//        /* 初始化WorkerSP */
//        mApplication.getWorkerSp().saveExitFlag(0);
//        
//        /* 初始化登录数据 */
//        Logger.d(TAG, "初始化条件：AppStatus:" + mApplication.getAppStatus() + 
//        		" CoreService:" + IntentUtil.isServiceWork(this, Config.SERVICE_NAME_CS)
//        		+ " LoginStatus:" + mApplication.getLoginStatus());
//        if (mApplication.getAppStatus() != Config.LOADED
//				&& mApplication.getLoginStatus() == Config.LOGGED) {
//			/* App在后台被kill调后重新打开App初始化数据 */
//			Logger.w(TAG, "DO onWebsocketLogin to restore data");
//			onWebsocketLogin();
//		}
        
        /* 绑定CoreService */
        mSconnection = new ServiceConnection() {			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				Logger.w(TAG, "MainTabActivity <-> CoreService 服务解绑");
				mBindService = null;
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Logger.w(TAG, "MainTabActivity >-< CoreService 服务绑定");
				mBindService = ((CoreService.MyBinder)service).getService();
				if (mBindService == null) {
					Logger.e(TAG, "Service bind failed.");
				}
			}
		};
        Intent bindIntent = new Intent(this, CoreService.class);
        bindService(bindIntent, mSconnection, Context.BIND_AUTO_CREATE);
        
        // 查询查询站点信息
        getSiteInfo();
    }
	
	
	private void getSiteInfo() {
		// 请求常见问答
		final String url= Config.URL_SITEINFO_FMT + Config.SITE_ID;
		// 1.先获取对应的url缓存
		final URLCache urlCache = new URLCache();
		String responseString = urlCache.get(url);
		if (null != responseString) {
			try {
				parseSiteInfo(responseString);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return;
		}
		
		// 2.无缓存则发起GET请求
		V5HttpUtil.get(url, new HttpResponseHandler(this) {
			@Override
			public void onSuccess(int statusCode, String responseString) {
				responseString = DevUtils.decodeUnicode(responseString);
				urlCache.put(url, responseString);
				try {
					parseSiteInfo(responseString);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "getSiteInfo.onFailure(" + statusCode + "):" + responseString);
			}
		});
	}

	private void parseSiteInfo(String responseString) throws JSONException {
		JSONObject json = new JSONObject(responseString);
		if (json.getString("state").equals("ok")) {
			if (mAppInfo.getSiteInfo() == null) {
				mAppInfo.setSiteInfo(new SiteInfo());
			}
			mAppInfo.getSiteInfo().updateFromJSON(json);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		Logger.d(TAG, "[onStart]");
		if (CoreService.isConnected() && mHeaderTips.getVisibility() == View.VISIBLE) {
			dismissViewWithAnimation(mHeaderTips, getAnimationY(false));
		}
//		IntentUtil.acquireWakeLock(this);
		
//		if (mApplication.getWorkerSp().readExitFlag() == 1) {
//			if (!isDialogShow()) {
//				showConfirmDialog(
//						R.string.on_other_dev_login_err, 
//						WarningDialog.MODE_ONE_BUTTON, 
//						new WarningDialogListener() {
//					@Override
//					public void onClick(View view) {
//						dismissWarningDialog();
//						mApplication.terminate();
//						gotoActivityAndFinishThis(UserLoginActivity.class);	
//					}
//				});
//			}
//		} else { // 恢复启动消息服务
//			mApplication.getWorkerSp().saveExitFlag(0);
//			if (!IntentUtil.isServiceWork(this, Config.SERVICE_NAME_CS)) {
//				Intent localIntent = new Intent();
//				localIntent.setAction(Config.ACTION_CORE);
//				localIntent.setPackage("com.v5kf.mcss");
//				localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		        startService(localIntent);
//			}
//		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Logger.d(TAG, "[onResume]");
//		acquireWakeLock();
//		Logger.w("NetworkManager", "MainTab onresume net:" + NetworkManager.isConnected(this));
//		mHeaderTips.setVisibility(View.GONE);
//		mHandler.sendEmptyMessageDelayed(WHAT_UP_NET, 1000);
//		// 清空等待客户的通知消息
//		mApplication.clearNotification(Config.NOTIFY_ID_WAIT_IN);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Logger.d(TAG, "[onPause]");
//		releaseWakeLock();
	}
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	Logger.w(TAG, "[onDestroy]");
//    	IntentUtil.releaseWakeLock(this);
    	unregisterReceiver(mClientReceiver);
    	
    	Logger.d(TAG, "MainTabActivity destroy " + mApplication.getAppStatus());
    	/* 断开绑定service */  
        unbindService(mSconnection);
        
        dismissWarningDialog();
    }
    

    private void initData() {
    	mCurrentPage = 0;
    	mFragmentMap = new HashMap<>();
    	mClientReceiver = new ClientServiceReceiver();
    	/* 注册广播接收 */
		IntentFilter filter=new IntentFilter();
		filter.addAction("com.v5kf.android.intent.action_message");
		registerReceiver(mClientReceiver, filter);
    }
        

    private void findView() {
		mViewPager = (NonSlipViewPager) findViewById(R.id.id_view_pager);
		mTabView = (TabView)findViewById(R.id.id_tab);
		mHeaderTips = findViewById(R.id.header_tips);
	}
    
    
    private void initView() {
    	/* 禁用手势滑动 */
    	mViewPager.setScrollable(false);
    	
    	/* 初始化标题栏 */
		initTitleBar();
		initPopupMenu();
		
		/* 添加监听器 */
		addListener();
		
		if (!NetworkManager.isConnected(this)) {
			mHeaderTips.setVisibility(View.VISIBLE);
		}
		mHeaderTips.findViewById(R.id.id_tips_btn_right).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				closeHeaderTips(v);
			}
		});
	}
    
    private void addListener() {
		mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				getFragment(arg0).onFragmentSelected();
				mCurrentPage = arg0;
				showHeader();
				switch (arg0) {
				case 0:
					initTopBarForSegment(
						R.string.seg_title_serving, 
						R.string.seg_title_waiting, 
						null);
					break;
					
				case 1:
					initTopBarForSegment(
							R.string.seg_title_worker, 
							R.string.seg_title_customer, 
							null);
					break;
					
				case 2:
//					initTopBarForTitle(R.string.app_name);
					hideHeader();
					break;
					
				case 3:
					initTopBarForTitle(R.string.app_name);
					hideHeader();
					break;
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}

    /**
     * 重试
     * @param closeHeaderTips MainTabActivity 
     * @return void
     * @param v
     */
	public void closeHeaderTips(View v) {
    	//dismissViewWithAnimation(mHeaderTips, getAnimationY(false));
		EventBus.getDefault().post(Boolean.valueOf(true), EventTag.ETAG_ON_LINE);
    }
    
    private void loadData() {
        // Set up the ViewPager with the sections adapter.
        mViewPager.setOffscreenPageLimit(mTitles.length);
        mViewPager.setAdapter(new PageAdapter(getSupportFragmentManager()));
        mTabView.setViewPager(mViewPager);
		
        /* Tab栏红点 */
		if (mAppInfo.getCustomerMap().size() > 0) {
			mTabView.setBadgeOnItem(0);
		}
	}
    
    
    private void initTitleBar() {
//    	initTopBarForLeftImageAndRightImage(
    	initTopBarForRightImage(
    			R.string.app_name, 
//    			R.drawable.aic_search,
    			R.drawable.v5_mode_set, 
//    			null,
    			new onRightImageButtonClickListener() {
					
					@Override
					public void onClick(View v) {
//						mTitlePopup.show(v);
						showSeekBar();
					}
				});
	}
    
    
    /**
     * 初始化弹出菜单栏
     * @param initPopupMenu MainTabActivity 
     * @return void
     */
    private void initPopupMenu(){
    	//实例化标题栏弹窗
    	mTitlePopup = new TitlePopup(this, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	
    	//给标题栏弹窗添加子类
    	mTitlePopup.addAction(new ActionItem(this, "转接", R.drawable.v5_popmenu_switch));
    	mTitlePopup.addAction(new ActionItem(this, "托管", R.drawable.v5_popmenu_off_trust));
    	mTitlePopup.addAction(new ActionItem(this, "退出", R.drawable.v5_popmenu_end_session));
    	
    	mTitlePopup.setItemOnClickListener(new OnItemOnClickListener() {
			
			@Override
			public void onItemClick(ActionItem item, int position) {
				switch (position) {
				case 0:
					
					break;
				case 1:
					// 打开接入设置窗口
					showSeekBar();
					break;
				case 2:
					
					break;
				case 3:
					break;
				}
			}
		});
	}
        
    
    /**
     * 显示滑动杆对话框
     * @param showSeekBar MainTabActivity 
     * @return void
     */
    protected void showSeekBar() {
    	if (null == mSeekBarDialog) {
    		mSeekBarDialog = new ModeSeekbarDialog(this, mAppInfo.getUser());
    	}
    	mSeekBarDialog.initDialog();
    	mSeekBarDialog.show();
	}

	private BaseFragment getFragment(int position){
        BaseFragment fragment = mFragmentMap.get(position) ;
        if(fragment == null){
        	Logger.d(TAG, "fragment new" + position);
            switch (position){
                case 0:
                    fragment = SessionFragment.newInstance(position);
                    break;
                case 1:
                    fragment = ContactFragment.newInstance(position);
                    break;
                case 2:
                    fragment = MineFragment.newInstance(position); /**/
                    break;
                case 3:
                    fragment = MineFragment.newInstance(position);
                    break;
            }
            mFragmentMap.put(position, fragment);
        }
        
        return fragment;
    }
    
    public void sendHandleMessage(Message msg) {
    	mHandler.sendMessage(msg);
    }
    
    @Override
    protected void handleMessage(Message msg) {
    	/* 处理消息更新界面 */
    	switch (msg.what) {
    	case WHAT_START_CHAT_CONTENT_ACTIVITY: {
    		Bundle extras = msg.getData();
    		startChatContentActivity(extras);
    		break;
    	}
    	case WHAT_SEND_REQUEST: { // 向ws发送请求
    		if (mBindService != null) {
    			Bundle extras = msg.getData();
    			mBindService.sendMessage(extras.getString(MSG_KEY_REQUEST));
    		}
    		break;
    	}
    	
    	default:
    		
    		break;
    	}
    }
    
    /**
     * ViewPager适配器
     * @author Chenhy	
     * @email chenhy@v5kf.com
     * @version v1.0 2015-8-4 上午10:49:22
     * @package com.v5kf.mcss.ui.activity of MCSS-Native
     * @file MainTabActivity.java 
     *
     */
    class PageAdapter extends FragmentPagerAdapter implements TabView.OnItemIconTextSelectListener{

        public PageAdapter(FragmentManager fm) {
            super(fm);
        }
        
        @Override
        public Fragment getItem(int position) {
            return getFragment(position);
        }
        
        @Override
        public int[] onIconSelect(int position) {
            int icon[] = new int[2] ;
            icon[0] = mIconsSelect[position] ;
            icon[1] = mIconsNormal[position] ;
            return icon;
        }
        
        @Override
        public String onTextSelect(int position) {
            return getString(mTitles[position]);
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }
    }

	@Override
	protected void onReceive(Context context, Intent intent) {
		if (null == intent) {
			return;
		}
		
		if (intent.getAction().equals(Config.ACTION_ON_MAINTAB)) {
			Bundle bundle = intent.getExtras();
			int intent_type = bundle.getInt(Config.EXTRA_KEY_INTENT_TYPE);
			switch (intent_type) {
			case Config.EXTRA_TYPE_CLEAR_CLIENT_MSG:
				mTabView.clearBadgeOnItem(2);
				break;
			}
		}
	}
	
	/**
	 * 更新红点显示
	 */
	private void updateBadge() {
		if (mCurrentPage == 0) {
			setSegmentBadge(mAppInfo.getServingCustomerCount(), 0);
			setSegmentBadge(mAppInfo.getWaitingCustomerCount(), 1);
		}
		
		// 更新红点
		if (mAppInfo.getCustomerMap().size() > 0) {
			mTabView.setBadgeOnItem(0);
		} else {
			mTabView.clearBadgeOnItem(0);
		}
	}
	
	private void newPickupEvent() {
		// TODO
		mTabView.setCurrentItem(0);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Logger.d(TAG, "--- onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode + " intent-data:" + data);
		switch (requestCode) {
		case Config.REQUEST_CODE_SERVING_SESSION_FRAGMENT:
		case Config.REQUEST_CODE_WAITING_SESSION_FRAGMENT:
			getFragment(0).onActivityResult(requestCode, resultCode, data);
			if (resultCode == Config.RESULT_CODE_PICKUP_CSTM) {
				// goto first fragment, and first fragment set to segment 0
				newPickupEvent();
			}
			break;
		
		case Config.REQUEST_CODE_MINE_FRAGMENT:
			getFragment(2).onActivityResult(requestCode, resultCode, data);
//			getFragment(3).onActivityResult(requestCode, resultCode, data);
			break;
			
		case Config.REQUEST_CODE_HISTORY_VISITOR:
			if (resultCode == Config.RESULT_CODE_PICKUP_CSTM) {
				// goto first fragment, and first fragment set to segment 0
				newPickupEvent();
			}
			break;
		}
	}
	
	class ClientServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (null == intent) {
				return;
			}
			if (intent.getAction().equals("com.v5kf.android.intent.action_message")) {
				mTabView.setBadgeOnItem(2);
			}
		}
	}
	
	
	/***** event *****/
	
	@Subscriber(tag = EventTag.ETAG_RELOGIN, mode = ThreadMode.MAIN)
	private void gotoLogin(ReloginReason reason) {
		Logger.e(TAG + "-eventbus", "eventbus -> ETAG_RELOGIN");
		Intent intent = new Intent(this, CustomLoginActivity.class);
		intent.putExtra(EventTag.ETAG_RELOGIN, reason.ordinal());
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		
		mApplication.terminate();
	}
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		Logger.d(TAG, "eventbus -> ETAG_CONNECTION_CHANGE: " + isConnect);
		if (isConnect) {
			dismissWarningDialog();
			updateBadge();
			mHeaderTips.setVisibility(View.GONE);
//			dismissViewWithAnimation(mHeaderTips, getAnimationY(false));
		} else {
			updateBadge();
			mHeaderTips.setVisibility(View.VISIBLE);
//			showViewWithAnimation(mHeaderTips, getAnimationY(true));
		}
	}
	
	@Subscriber(tag = EventTag.ETAG_SERVING_CSTM_CHANGE, mode = ThreadMode.MAIN)
	private void servingCustomerChange(AppInfoKeeper appinfo) {
		updateBadge();
	}
	
	@Subscriber(tag = EventTag.ETAG_WAITING_CSTM_CHANGE, mode = ThreadMode.MAIN)
	private void waitingCustomerChange(AppInfoKeeper appinfo) {
		updateBadge();
	}
}
