package com.v5kf.mcss.ui.activity;

import org.json.JSONException;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.chyrain.view.indicator.Indicator;
import com.chyrain.view.indicator.IndicatorViewPager;
import com.chyrain.view.indicator.IndicatorViewPager.IndicatorFragmentPagerAdapter;
import com.chyrain.view.viewpager.SViewPager;
import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.mikepenz.actionitembadge.library.utils.NumberUtils;
import com.mikepenz.actionitembadge.library.utils.UIUtil;
import com.umeng.analytics.MobclickAgent;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.Config.ReloginReason;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.entity.ArchWorkerBean;
import com.v5kf.mcss.entity.WorkerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.WorkerRequest;
import com.v5kf.mcss.ui.activity.md2x.BaseToolbarActivity;
import com.v5kf.mcss.ui.activity.md2x.CustomLoginActivity;
import com.v5kf.mcss.ui.activity.md2x.WaitingCustomerActivity;
import com.v5kf.mcss.ui.activity.md2x.WorkerInfoActivity;
import com.v5kf.mcss.ui.fragment.md2x.TabHistoryVisitorFragment;
import com.v5kf.mcss.ui.fragment.md2x.TabSecondFragment;
import com.v5kf.mcss.ui.fragment.md2x.TabServingSessionFragment;
import com.v5kf.mcss.ui.fragment.md2x.TabThirdFragment;
import com.v5kf.mcss.ui.fragment.md2x.TabWorkerListFragment;
import com.v5kf.mcss.ui.widget.CircleImageView;
import com.v5kf.mcss.ui.widget.ModeSeekbarDialog;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.cache.ImageLoader;

public class MainTabActivity extends BaseToolbarActivity {
	private static final String TAG = "TabMainActivity";
	
	private IndicatorViewPager indicatorViewPager;
	private DrawerLayout mDrawerLayout;
	private NavigationView mNavigationView;
	private FloatingActionButton mFab;
//	private View mContent;
	
	private int mCurrentPageIndex;
	
	private CircleImageView mAvatar;
	private TextView mName;
	private TextView mGroup;
	private Button mMode;
	private Spinner mStatusSpinner;
	private TextView mStatusText;
	private View mStatusLayout;
	// mode设置SeekBar弹窗
	private ModeSeekbarDialog mSeekBarDialog;
	
	private String[] tabNames;
	private int[] tabIcons;
	
	@Override
    public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			Logger.d(TAG, "mDrawerLayout.isDrawerOpen");
			mDrawerLayout.closeDrawers();
		} else if (mCurrentPageIndex == 0) {
			moveTaskToBack(true);
		} else {
			mNavigationView.getMenu().getItem(0).setChecked(true);
			indicatorViewPager.setCurrentItem(0, false);
		}
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_md2x_tabmain);
		// 不可滑动返回 
		setSwipeBackEnable(false);
		
		findView();
		initToolbar();
		initSlideMenu();
		initTabViewPager();
	}

	private void findView() {
//		mContent = findViewById(R.id.id_content);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.id_drawer_layout);
		mNavigationView = (NavigationView) findViewById(R.id.id_navigation_view);
		mFab = (FloatingActionButton) findViewById(R.id.id_fab);
		
	}
	
	private void initToolbar() {
		initTopBarForOnlyTitle(R.string.conversation);
		if (getToolbar() != null) {
			setSupportActionBar(getToolbar());
		} else {
			Logger.e(TAG, "[initToolbar] getToolbar() is null");
		}
		
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			// 启用home as up
		    actionBar.setHomeAsUpIndicator(R.drawable.md2x_ic_menu_blue);
		    actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}
	
	public void hideFab() {
		mFab.setVisibility(View.GONE);
	}
	
	public void showFab() {
		mFab.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 切换Fragment时更新Toolbar的标题和menu
	 * @deprecated 不需要，改在pagelistener执行
	 */
	public void updateToolbar(int index) {
//		if (index == 0) {
//			initTopBarForOnlyTitle(R.string.conversation);
//		} else {
//			initTopBarForOnlyTitle("");
//		}
//		getSupportActionBar().invalidateOptionsMenu();
	}

	private void initFromGroup() {
    	String from = null;
    	from = mAppInfo.getUser().getJob();
    	if (from == null || from.isEmpty()) {
    		from = getString(R.string.group);
    		ArchWorkerBean coWorker = mAppInfo.getCoWorker(mAppInfo.getUser().getW_id());
    		if (null != coWorker) {
    			from += coWorker.getGroup_name();
    		} else {
    			from += "暂无";
    		}
    	}
    	
    	mGroup.setText(from);
	}
	
	/**
	 * 初始化侧边栏菜单
	 */
	private void initSlideMenu() {
		View headerView = mNavigationView.getHeaderView(0);
		if (headerView != null) { // 初始化登录坐席与状态信息
			mAvatar = (CircleImageView) headerView.findViewById(R.id.drawer_avatar);
			mName = (TextView) headerView.findViewById(R.id.drawer_name);
			mGroup = (TextView) headerView.findViewById(R.id.drawer_group);
			mMode = (Button) headerView.findViewById(R.id.drawer_mode);
			mStatusSpinner = (Spinner) headerView.findViewById(R.id.drawer_status_spinner);
			mStatusText = (TextView) headerView.findViewById(R.id.drawer_status_text);
			mStatusLayout = headerView.findViewById(R.id.drawer_status_layout);
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.md2x_simple_spinner_item);
			String level[] = getResources().getStringArray(R.array.status_spinner_arr); //资源文件
			for (int i = 0; i < level.length; i++) {
				adapter.add(level[i]);
			}
			adapter.setDropDownViewResource(R.layout.md2x_simple_spinner_dropdown_item);
//			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mStatusSpinner.setAdapter(adapter);
			
			mAvatar.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					gotoWorkerInfoActivity();
				}
			});
			mMode.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					showSeekBar();
				}
			});
			mStatusSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int position, long id) {
					Logger.d(TAG, "Status item selected:" + position);
					short status = mAppInfo.getUser().getStatus();
					switch (position) {
					case 0:
						status = QAODefine.STATUS_ONLINE;
						break;
					case 1:
						status = QAODefine.STATUS_LEAVE;
						break;
					case 2:
						status = QAODefine.STATUS_HIDE;
						break;
					default:
						break;
					}
					
					if (status != mAppInfo.getUser().getStatus()) {		
						try {
							WorkerRequest workerRequest = (WorkerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WWRKR, MainTabActivity.this);
							workerRequest.setWorkerStatus(status);
							mAppInfo.getUser().setStatus(status);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					updateStatusSpinner(mAppInfo.getUser().getStatus());
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub
					
				}
	        });
		}
		updateSlideMenu();
		
		mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
		    @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
		    	Logger.i(TAG, "[onNavigationItemSelected] :" + menuItem.getTitle());
		        switch (menuItem.getItemId()) {
		        case R.id.drawer_session: // 对话
		        	menuItem.setChecked(true);
			        mDrawerLayout.closeDrawers();
			        indicatorViewPager.setCurrentItem(0, false);
		        	break;
		        case R.id.drawer_worker: // 客服
		        	menuItem.setChecked(true);
			        mDrawerLayout.closeDrawers();
			        indicatorViewPager.setCurrentItem(1, false);
		        	break;
		        case R.id.drawer_history: // 历史
		        	menuItem.setChecked(true);
			        mDrawerLayout.closeDrawers();
			        indicatorViewPager.setCurrentItem(2, false);
		        	break;
		        case R.id.drawer_explore: // 发现
		        	menuItem.setChecked(true);
			        mDrawerLayout.closeDrawers();
			        indicatorViewPager.setCurrentItem(3, false);
		        	break;
		        case R.id.drawer_more: // 更多
		        	menuItem.setChecked(true);
		        	mDrawerLayout.closeDrawers();
		        	indicatorViewPager.setCurrentItem(4, false);
		        	break;
		        case R.id.drawer_quit: // 退出登录
		        	menuItem.setChecked(false);
		        	showAlertDialog(R.string.confirm_logout, new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							mApplication.getWorkerSp().clearAutoLogin();
							dismissWarningDialog();
							mApplication.logout();
							gotoActivity(CustomLoginActivity.class);
							MobclickAgent.onEvent(MainTabActivity.this, "APP_LOGOUT");
						}
					}, null);
					break;
		        }
//		    	ShowToast(menuItem.getTitle() + " pressed");
//		    	showSnackbar(mContent, menuItem.getTitle() + " pressed");
//		        menuItem.setChecked(true);
//		        mDrawerLayout.closeDrawers();
		        return true;
		    }
		});
	}
	
	/**
	 * 进入坐席个人信息页
	 */
	protected void gotoWorkerInfoActivity() {
		Bundle bundle = new Bundle();
		bundle.putInt(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_ACTIVITY_START);
		bundle.putString(Config.EXTRA_KEY_W_ID, mAppInfo.getUser().getW_id());			
		Intent intent = new Intent(this, WorkerInfoActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}

	/**
	 * 刷新Toolbar红点
	 */
	private void updateBadge() {
		getSupportActionBar().invalidateOptionsMenu();
	}

	/**
	 * 更新整个侧边滑动栏
	 */
	private void updateSlideMenu() {
		if (mNavigationView.getHeaderView(0) == null) {
			return;
		}
		Logger.i(TAG, "[updateSlideMenu] photo:" + mAppInfo.getUser().getPhoto());
		ImageLoader imageLoader = new ImageLoader(this, true, R.drawable.v5_photo_default, new ImageLoader.ImageLoaderListener() {
			
			@Override
			public void onSuccess(String url, ImageView imageView) {
				Logger.i(TAG, "updateSlideMenu -> onSuccess");
				// [新增]离线状态头像变灰
				if (mAppInfo.getUser().getStatus() == QAODefine.STATUS_OFFLINE) {
		    		UITools.grayImageView(imageView);
		    	}
			}
			
			@Override
			public void onFailure(ImageLoader imageLoader, String url,
					ImageView imageView) {
				Logger.i(TAG, "updateSlideMenu -> onFailure");
				// [新增]离线状态头像变灰
		    	if (mAppInfo.getUser().getStatus() == QAODefine.STATUS_OFFLINE) {
		    		UITools.grayImageView(imageView);
		    	}
			}
		});
		imageLoader.DisplayImage(mAppInfo.getUser().getPhoto(), mAvatar);
		
		mName.setText(mAppInfo.getUser().getDefaultName());
		initFromGroup();
		
		updateModeButton(mMode, mAppInfo.getUser().getMode());
		updateStatusSpinner(mAppInfo.getUser().getStatus());
	}
	
	/**
	 * 更新状态
	 * @param status
	 */
	private void updateStatusSpinner(int status) {
		mStatusSpinner.setEnabled(true);
		//mNavigationView.getHeaderView(0).setBackgroundResource(R.color.main_color);
    	//setToolbarColor(UITools.getColor(R.color.main_color));
    	//setNavigationBarColor(colorBurn(UITools.getColor(R.color.main_color)));
    	//setWindowBackground(UITools.getColor(R.color.main_color));
		UITools.setDrawerStatusSpinnerBg(status, mStatusLayout);
		switch (status) {
    	case QAODefine.STATUS_OFFLINE:
    		mStatusText.setText(R.string.status_offline);
//    		mStatusSpinner.setSelection(4);
    		//mNavigationView.getHeaderView(0).setBackgroundResource(R.color.offline_color);
    		//setToolbarColor(UITools.getColor(R.color.offline_color));
    		//setNavigationBarColor(colorBurn(UITools.getColor(R.color.offline_color)));
    		//setWindowBackground(UITools.getColor(R.color.offline_color));
    		break;
    		
    	case QAODefine.STATUS_ONLINE:
    		mStatusText.setText(R.string.status_online);
    		mStatusSpinner.setSelection(0);
    		break;
    		
    	case QAODefine.STATUS_HIDE:
    		mStatusText.setText(R.string.status_hide);
    		mStatusSpinner.setSelection(2);
    		break;
    		
    	case QAODefine.STATUS_LEAVE:
    		mStatusText.setText(R.string.status_leave);
    		mStatusSpinner.setSelection(1);
    		break;
    		
    	case QAODefine.STATUS_BUSY:
    		mStatusText.setText(R.string.status_busy);
    		mStatusSpinner.setEnabled(false);
//    		mStatusSpinner.setSelection(3);
    		break;    		
    	}
    }
    
	/**
	 * 更新接入模式
	 * @param modeView
	 * @param mode
	 */
    private void updateModeButton(Button modeView, int mode) {
    	if (mode == QAODefine.MODE_AUTO) { // 自动接入
    		modeView.setBackgroundResource(R.drawable.v5_drawer_mode_auto_bg);
    		modeView.setText(getString(R.string.set_mode_auto)
    				+ "(" + mAppInfo.getUser().getAccepts() + ")");
    	} else if (mode == QAODefine.MODE_SWITCH_ONLY) { // 仅转接
    		modeView.setBackgroundResource(R.drawable.v5_drawer_mode_switchonly_bg);
    		modeView.setText(getString(R.string.set_mode_switchable)
    				+ "(" + mAppInfo.getUser().getConnects() + ")");
    	}
    }
    
    /**
     * 显示模式设置对话框
     * @param showSeekBar MainTabActivity 
     * @return void
     */
    private void showSeekBar() {
    	if (null == mSeekBarDialog) {
    		mSeekBarDialog = new ModeSeekbarDialog(this, mAppInfo.getUser());
    	}
    	mSeekBarDialog.initDialog();
    	mSeekBarDialog.show();
	}

	private void initTabViewPager() {
		SViewPager viewPager = (SViewPager) findViewById(R.id.tabmain_viewPager);
		Indicator indicator = (Indicator) findViewById(R.id.tabmain_indicator);
		indicatorViewPager = new IndicatorViewPager(indicator, viewPager);
		indicatorViewPager.setAdapter(new V5TabAdapter(getSupportFragmentManager()));
		viewPager.addOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				mCurrentPageIndex = position;
				Logger.d(TAG, "onPageSelected: " + position);
				
				// 更新ToolBar标题和导航
				getToolbar().setTitle(tabNames[position]);
				getSupportActionBar().invalidateOptionsMenu();
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
				
			}
		});
		// 禁止viewpager的滑动事件
		viewPager.setCanScroll(false);
		// 设置viewpager保留界面不重新加载的页面数量
		viewPager.setOffscreenPageLimit(3);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Logger.d(TAG, "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Logger.d(TAG, "onPrepareOptionsMenu");
		menu.clear();
		if (mCurrentPageIndex == 0) {
			getMenuInflater().inflate(R.menu.main, menu);
			int badgeCount = mAppInfo.getWaitingCustomerCount();
			// badge红点提示
			ActionItemBadge.update(this, 
        			menu.findItem(R.id.action_waiting), 
        			UIUtil.getCompatDrawable(this, R.drawable.v5_baritem_waiting), 
        			ActionItemBadge.BadgeStyles.RED.getStyle(), 
        			NumberUtils.formatNumber(badgeCount));
			if (badgeCount == 0) {
//	        	ActionItemBadge.hide(menu.findItem(R.id.action_waiting));
	        }
		} else if (mCurrentPageIndex == 1) {
			
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home: // Toolbar home点击
	            mDrawerLayout.openDrawer(GravityCompat.START);
	            return true;
//	        case R.id.action_search:
//	        	showSnackbar(mContent, "action_search");
//				return true;
			case R.id.action_waiting: // 等待列表点击
//				showSnackbar(mContent, "action_waiting");
				gotoActivity(WaitingCustomerActivity.class);
				return true;
			default:
				break;
	    }
	  
	    return super.onOptionsItemSelected(item);
	}

	/**
	 * Tab页面Adapter
	 * @author Chenhy
	 *
	 */
	private class V5TabAdapter extends IndicatorFragmentPagerAdapter {
		
		private LayoutInflater inflater;

		public V5TabAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
			tabNames = new String[]{
		    		getString(R.string.tabhost_text_session),
		    		getString(R.string.tabhost_text_worker),
		    		getString(R.string.tabhost_text_history),
		    		getString(R.string.tabhost_text_explore),
		    		getString(R.string.tabhost_text_more)};
			tabIcons = new int[]{
		    		R.drawable.md2x_ic_drawer_chat, 
		    		R.drawable.md2x_ic_drawer_worker, 
		    		R.drawable.md2x_ic_drawer_history, 
		    		R.drawable.md2x_ic_drawer_explore, 
		    		R.drawable.md2x_ic_drawer_settings};
			inflater = LayoutInflater.from(getApplicationContext());
		}

		@Override
		public int getCount() {
			return tabNames.length;
		}

		@Override
		public View getViewForTab(int position, View convertView, ViewGroup container) {
			if (convertView == null) {
				convertView = (TextView) inflater.inflate(R.layout.tab_main, container, false);
			}
			TextView textView = (TextView) convertView;
			textView.setText(tabNames[position]);
			textView.setCompoundDrawablesWithIntrinsicBounds(0, tabIcons[position], 0, 0);
			return textView;
		}

		@Override
		public Fragment getFragmentForPage(int position) {
			Fragment fragment = null;
			switch (position) {
			case 0:
				fragment = new TabServingSessionFragment(MainTabActivity.this, position);
				break;
			case 1:
				fragment = new TabWorkerListFragment(MainTabActivity.this, position);
				break;
			case 2:
				fragment = new TabHistoryVisitorFragment(MainTabActivity.this, position);
				break;
			case 3:
				fragment = new TabSecondFragment(MainTabActivity.this, position);
				break;
			case 4:
				fragment = new TabThirdFragment(MainTabActivity.this, position);
				break;
			default:
				fragment = new TabServingSessionFragment(MainTabActivity.this, position);
				break;
			}
			
			return fragment;
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
	}
	
	/***** event *****/
	
	@Subscriber(tag = EventTag.ETAG_RELOGIN, mode = ThreadMode.MAIN)
	private void gotoLogin(ReloginReason reason) {
		Logger.e(TAG + "-eventbus", "eventbus -> ETAG_RELOGIN");
		mApplication.terminate();
		
		Intent intent = new Intent(this, CustomLoginActivity.class);
		intent.putExtra(EventTag.ETAG_RELOGIN, reason.ordinal());
		startActivity(intent);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		Logger.d(TAG + "-eventbus", "connectionChange -> ETAG_CONNECTION_CHANGE");
		if (isConnect) {
			//dismissWarningDialog();
			updateSlideMenu();
			updateBadge();
		} else {
			updateSlideMenu();
			updateBadge();
		}
	}
	
	@Subscriber(tag = EventTag.ETAG_SERVING_CSTM_CHANGE, mode = ThreadMode.MAIN)
	private void servingCustomerChange(AppInfoKeeper appinfo) {
		Logger.d(TAG + "-eventbus", "servingCustomerChange -> ETAG_SERVING_CSTM_CHANGE");
		//updateBadge();
		//getSupportActionBar().invalidateOptionsMenu();
	}
	
	@Subscriber(tag = EventTag.ETAG_WAITING_CSTM_CHANGE, mode = ThreadMode.MAIN)
	private void waitingCustomerChange(AppInfoKeeper appinfo) {
		Logger.d(TAG + "-eventbus", "waitingCustomerChange -> ETAG_WAITING_CSTM_CHANGE");
		updateBadge();
	}

	@Subscriber(tag = EventTag.ETAG_UPDATE_USER_INFO, mode = ThreadMode.MAIN)
	private void updateUserInfo(WorkerBean user) {
		Logger.d(TAG + "-eventbus", "updateUserInfo -> ETAG_UPDATE_USER_INFO");
		updateSlideMenu();
	}

}
