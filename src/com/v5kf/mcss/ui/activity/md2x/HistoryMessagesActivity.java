package com.v5kf.mcss.ui.activity.md2x;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.litepal.crud.DataSupport;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidFragment.OnDismissListener;
import com.roomorama.caldroid.CaldroidListener;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.ArchWorkerBean;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.entity.MessageBean;
import com.v5kf.mcss.entity.SessionBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.QAOManager;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.TicketRequest;
import com.v5kf.mcss.ui.adapter.HistoryMessagesAdapter;
import com.v5kf.mcss.ui.entity.ChatRecyclerBean;
import com.v5kf.mcss.ui.widget.Divider;
import com.v5kf.mcss.utils.DateUtil;
import com.v5kf.mcss.utils.Logger;

public class HistoryMessagesActivity extends BaseToolbarActivity implements OnClickListener {
	
	private static final String TAG = "HistoryMessagesActivity";
	private static final int HDL_INIT_DONE = 1;
	private static final int HDL_NONE_MESSAGE = 11;
	private static final int SEARCH_TYPE_RANGE_LAST = 1; // 前一天
	private static final int SEARCH_TYPE_RANGE_NEXT = 2; // 后一天
	private static final int SEARCH_TYPE_NO_RANGE = 3; // 仅查当天
	
	private static final int MON_VAL_INVAL = -1; // 非法月份
	private static final int MON_VAL_UNGET = 0;	// 未获取该月
	private static final int MON_VAL_GETTING = 1; // 正在获取
	private static final int MON_VAL_HAS = 2; // 存在该月消息
//	private static final int MON_VAL_NONE = 3; // 不存在
	private static final int MON_VAL_GETTED = 4; // 已获取
	
	protected String v_id;
	protected String c_id;
	protected CustomerBean mCustomer;
//	protected int mOldSessionShownNum; // 已显示的历史会话数量(当天)
	protected int mCountTemp;
	
	private long mCurrentSearchDay; // 毫秒级/年月日
	private long mCurrentSearchMonth; // 毫秒级/年月
	private long mCurrentShowDay; // 毫秒级/年月日
	
	/* 对话列表 */
	private RecyclerView mRecycleView;
	private HistoryMessagesAdapter mRecyclerAdapter;	
	private List<ChatRecyclerBean> mDatas;
	private boolean mIsRefreshing;
	
	/* 底部日期选择控件 */
	private LinearLayout mDayLastLL;
	private LinearLayout mDayNextLL;
	private LinearLayout mCurDayLL;
	private TextView mCurDayTv;
	
	private CaldroidFragment mDialogCaldroidFragment;
	private int mCalShowYear;
	private int mCalShowMonth;
	
	/* 会话是否存在的映射 */
	private Map<Long, Boolean> mDateHasSessionMap = new ConcurrentHashMap<Long, Boolean>(); // 当日0时时间作为key
	private Map<Long, Integer> mMonthHasSessionMap = new ConcurrentHashMap<Long, Integer>(); // 当月一号0时时间作为key -> value: 0-正在请求 1-存在 2-不存在
	private Map<String, Boolean> mSessionReqMap = new HashMap<String, Boolean>(); // 是否请求过该会话的消息
	
	protected void handleIntent() {
		Intent intent = getIntent();
		int type = intent.getIntExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_NULL);
		if (Config.EXTRA_TYPE_ACTIVITY_START == type) {
			v_id = intent.getStringExtra(Config.EXTRA_KEY_V_ID);
			c_id = intent.getStringExtra(Config.EXTRA_KEY_C_ID);
			Logger.d(TAG, "MainTabActivity -> Intent -> HistoryMessagesActivity\n v_id:" + v_id);
		}
		
		if (!TextUtils.isEmpty(v_id) || !TextUtils.isEmpty(c_id)) { // CSTM_VISITOR
			mCustomer = mAppInfo.getVisitor(v_id);
			if (null == mCustomer) {
				/* 查找对话中客户 */
				if (!TextUtils.isEmpty(c_id)) {
					mCustomer = mAppInfo.getCustomerBean(c_id);
					if (mCustomer == null) {
						mCustomer = mAppInfo.getMonitorCustomer(c_id);
					}
				}
	        }
		}
		if (mCustomer == null) {
			Logger.e(TAG, "Customer(null) not found");
			ShowToast(R.string.on_history_messages_empty);
        	finishActivity();
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_messages);
		
		int year = DateUtil.getYear();
		int month = DateUtil.getMonth();
		int day = DateUtil.getDay();
		mCurrentSearchDay = DateUtil.getDate(year, month, day);
		mCurrentSearchMonth = DateUtil.getYearAndMonth(DateUtil.getCurrentLongTime());
		mCurrentShowDay = mCurrentSearchDay;
		
		handleIntent();
		findView();
		initView();
		initData();
	}
	
	
	private void initData() {
		showProgressDialog();

		/* 从数据库初始化客户会话数据 */
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (mCustomer != null && v_id != null) {
					List<SessionBean> slist = mCustomer.getSessionArray();
					if (slist == null || slist.isEmpty()) {			
						List<SessionBean> sessions = DataSupport.where("visitor_id = ? and worker_id = ?", v_id, mAppInfo.getUser().getW_id()).order("first_time desc").find(SessionBean.class);
						if (sessions != null && !sessions.isEmpty()) {
							for (SessionBean session : sessions) {
								mCustomer.addSession(session);
								mAppInfo.addSession(session);
								long date = DateUtil.dateTrim(1000 * session.getFirst_time());
								if (date != 0) {
									mDateHasSessionMap.put(date, true);
								}
							}
						}
						initMonthHasSessionMap();
					} else {
						initDateHasSessionMap();
						initMonthHasSessionMap();
					}
				}
				mHandler.sendEmptyMessage(HDL_INIT_DONE);
			}
		}).start();
	}


	protected void initDateHasSessionMap() {
		List<SessionBean> list = mCustomer.getSessionArray();
		if (list == null || list.isEmpty()) {
			return;
		}
		ListIterator<SessionBean> values = list.listIterator();
		while (values.hasNext()) {
			SessionBean session = values.next();
			if (null != session) {
				long date = DateUtil.dateTrim(1000 * session.getFirst_time());
				if (date != 0 && !mDateHasSessionMap.containsKey(date)) {
					mDateHasSessionMap.put(date, true);
					Logger.d(TAG, "[mDateHasSessionMap] put Date:" + date);
				}
			}
		}
	}
	
	private void initMonthHasSessionMap() {
		for (long date : mDateHasSessionMap.keySet()) {
			long month = DateUtil.getYearAndMonth(date);
			if (!mMonthHasSessionMap.containsKey(month)) {
				mMonthHasSessionMap.put(month, MON_VAL_HAS);
				Logger.d(TAG, "[mMonthHasSessionMap] put Month:" + month);
			}
		}
	}

	private void loadData() {
		if (null == mCustomer) {
			return;
		}
		long today = DateUtil.getCurrentLongTime();
		mCalShowYear = DateUtil.getYear();
		mCalShowMonth = DateUtil.getMonth();
		requestMessagesOfDay(today, SEARCH_TYPE_RANGE_LAST);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (null == mCustomer) {
			ShowToast(R.string.on_history_messages_empty);
    		finishActivity();
		}
	}

	private void findView() {
		mRecycleView = (RecyclerView) findViewById(R.id.id_recycle_view_msgs);
		mDayLastLL = (LinearLayout) findViewById(R.id.search_last_day_ll);
		mDayNextLL = (LinearLayout) findViewById(R.id.search_next_day_ll);
		mCurDayLL = (LinearLayout) findViewById(R.id.cur_date_ll);
		mCurDayTv = (TextView) findViewById(R.id.id_cur_date);
	}


	private void initView() {
		initTitleBar();
		initRecyclerView();
		checkListEmpty();
		
		mCurDayTv.setText(DateUtil.getDayString(mCurrentSearchDay));
		mDayLastLL.setOnClickListener(this);
		mDayNextLL.setOnClickListener(this);
		mCurDayLL.setOnClickListener(this);
	}
	
	/**
	 * 更新数据（前一天还是后一天）
	 * @param refreshData HistoryMessagesActivity 
	 * @return void
	 * @param isNext
	 */
	private void refreshData(int searchType) {
		Logger.d(TAG, "[refreshData]->getMessagesOfDay searchType:" + searchType);
		getMessagesOfDay(mCurrentSearchDay, searchType);
		mIsRefreshing = false;
		//dismissProgressDialog();
	}

	/**
	 * 获得指定日期的消息(10位精确到秒)
	 * @param getMessagesOfDay HistoryMessagesActivity 
	 * @return void
	 * @param currentSearchDay
	 */
	private void getMessagesOfDay(long currentSearchDay, int searchType) {
		mCurrentSearchDay = currentSearchDay;
		List<SessionBean> s_list = mAppInfo.getSessionsOnDayOfVisitor(currentSearchDay, mCustomer);
		Logger.d(TAG, "getMessagesOfDay:" + currentSearchDay + " s_list:" + s_list + " " + DateUtil.getDayString(currentSearchDay));
		if (null == s_list || s_list.isEmpty()) { // 自动往前跳过没有消息的日期，跳过数量为本月份日期
			if (searchType == SEARCH_TYPE_RANGE_NEXT) { // 查后一天
				mCurrentSearchDay += 24 * 3600 * 1000;
			} else if (searchType == SEARCH_TYPE_RANGE_LAST) { // 查前一天
				mCurrentSearchDay -= 24 * 3600 * 1000;
			} else { // 仅查当天
				Logger.i(TAG, "getMessagesOfDay SEARCH_TYPE_NONE return");
				dismissProgressDialog();
				return;
			}
			// 系统有效时间内，且大于客服创建时间
			if (mAppInfo.getUser().getCreate_time() < mCurrentSearchDay/1000 &&
					DateUtil.isValidMonth(mCurrentSearchDay)) { // && DateUtil.isOnSameMonth(mCurrentSearchDay, currentSearchDay)
				Logger.v(TAG, "mCurrentSearchDay:" + mCurrentSearchDay + " mCurrentSearchMonth:" + mCurrentSearchMonth);
				if (mCurrentSearchDay < mCurrentSearchMonth) { // 前一个月
					mCurrentSearchMonth = DateUtil.getLastMonth(mCurrentSearchMonth);
					requestMessagesOfDay(mCurrentSearchDay, searchType);
				} else {
					getMessagesOfDay(mCurrentSearchDay, searchType);
				}
			} else { // 未找到客户消息
				Logger.w(TAG, "未找到客户消息");
				mHandler.obtainMessage(HDL_NONE_MESSAGE).sendToTarget();
				Logger.d(TAG, "客服创建日期：" + DateUtil.getDayString(mAppInfo.getUser().getCreate_time()) + " mCurrentSearchDay:" + DateUtil.getDayString(mCurrentSearchDay) + " valid:" + DateUtil.isValidMonth(mCurrentSearchDay));
//				if (mCurrentShowDay != 0) {
//					mCurrentSearchDay = mCurrentShowDay;
//				}
			}
			Logger.i(TAG, "getMessagesOfDay s_list is null return " + DateUtil.getDayString(currentSearchDay));
			dismissProgressDialog();
			return;
		}

		mDatas.clear();
		Logger.w(TAG, "历史会话：" + s_list.size());
		for (SessionBean session : s_list) {
			List<V5Message> msgs = session.getMessageArray();
			Logger.w(TAG, "历史消息：" + msgs);
			if (null == msgs || msgs.isEmpty()) {
				// 启用数据库
				List<MessageBean> msgList = DataSupport.where("session_id = ?", session.getS_id()).order("create_time asc").find(MessageBean.class);
				Logger.w(TAG, "历史消息：" + msgList.size());
				if (msgList != null && !msgList.isEmpty()) {
					for (MessageBean msgBean : msgList) {
						V5Message v5Message;
						try {
							v5Message = QAOManager.receiveMessage(msgBean);
							session.addMessage(v5Message);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					updateMessagesOfSession(session);
				} else {
					Logger.i(TAG, "getMessagesOfDay -> getMessagesOfSession sid:" + session.getS_id());
					getMessagesOfSession(session.getS_id());
				}
			} else {
				Logger.w(TAG, "不为空 -> 更新消息");
				updateMessagesOfSession(session);
			}
			
			// 不启用数据库
//			if (null == msgs || msgs.isEmpty()) {
//				Logger.i(TAG, "getMessagesOfDay -> getMessagesOfSession sid:" + session.getS_id());
//				getMessagesOfSession(session.getS_id());
//			} else {
//				updateMessagesOfSession(session);
//			}
		}
		mCurrentShowDay = currentSearchDay;
		updateUI();
		Logger.i(TAG, "getMessagesOfDay updateUI return");
	}

	private void updateUI() {
		// 排序按照最新活动时间排序
		MessageTimeCompartor mtc = new MessageTimeCompartor();
		Collections.sort(mDatas, mtc);
		mRecyclerAdapter.notifyDataSetChanged();
		checkListEmpty();
		mCurDayTv.setText(DateUtil.getDayString(mCurrentShowDay));
		dismissProgressDialog();
	}


	private void addRecyclerBean(V5Message msg, String workerName) {
		if (null == msg || null == msg.getDefaultContent(this) || 
				msg.getDefaultContent(this).isEmpty() ||
				msg.getMessage_type() == QAODefine.MSG_TYPE_CONTROL
				|| msg.getMessage_type() == QAODefine.MSG_TYPE_WXCS) { // [修改]过滤全部控制消息 
			// 内容为空不显示
    		return;
    	}
		
		/* [修改]取消查重 */
//		for (ChatRecyclerBean tmp : mDatas) {
//			if (msg.getMessage_id().equals(tmp.getMessage().getMessage_id())) {
//				Logger.d(TAG, "[addRecyclerBean] [查重：存在重复Message]");
//				return;
//			}
//		}
		
    	ChatRecyclerBean bean = new ChatRecyclerBean(msg);
    	if (msg.getDirection() == QAODefine.MSG_DIR_TO_WORKER) {
    		bean.setName(mCustomer.getDefaultName());
    	} else if (msg.getDirection() == QAODefine.MSG_DIR_FROM_ROBOT) {
    		bean.setName(getString(R.string.robot_name));
    	} else if (msg.getDirection() == QAODefine.MSG_DIR_TO_CUSTOMER) {
    		bean.setName(workerName);
    	} else {
    		bean.setName("未知方向:" + msg.getDirection());
    	}
    	mDatas.add(0, bean); // 先旧后新
    	Logger.d(TAG, "[addRecycleBean] +1");
	}


	private void initRecyclerView() {
		if (null == mDatas) {
			mDatas = new ArrayList<ChatRecyclerBean>();
		}
		mRecyclerAdapter = new HistoryMessagesAdapter(this, mDatas);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		mRecycleView.setLayoutManager(layoutManager);
		mRecycleView.addItemDecoration(new Divider(0xFFE2E3E4, 24));
        mRecycleView.setAdapter(mRecyclerAdapter);
        mRecycleView.setScrollbarFadingEnabled(true);
        mRecycleView.setScrollBarStyle(RecyclerView.SCROLLBAR_POSITION_RIGHT);
        
        /* 用于解决RecyclerView的BUG：滑动过程中更新数据造成的崩溃 */
        mRecycleView.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility") @Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mIsRefreshing) {
                    return true;
                } else {
                    return false;
                }
			}
        });
        mRecycleView.addOnScrollListener(new OnScrollListener() {
        	
        	@Override
        	public void onScrollStateChanged(RecyclerView recyclerView,
        			int newState) {
        		super.onScrollStateChanged(recyclerView, newState);
        		/* 用于解决RecyclerView的BUG：滑动过程中更新数据造成的崩溃 */
        		if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
        			mDayLastLL.setEnabled(false);
        			mDayNextLL.setEnabled(false);        			
        		} else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
        			mDayLastLL.setEnabled(true);
        			mDayNextLL.setEnabled(true);
        		}
        	}
        	
        	@Override
        	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        		super.onScrolled(recyclerView, dx, dy);
        	}        	
		});
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		/* 清空历史会话内存 */
//		mAppInfo.clearCustomerSession(mCustomer);
		
		// TODO 退出页面时关闭语音播放
    	mRecyclerAdapter.stopVoicePlaying();
	}
	
	protected void initTitleBar() {
		initTopBarForLeftBack(R.string.history_messages);
	}
	
	private void checkListEmpty() {
    	if (null == mRecycleView) {
    		mRecycleView = (RecyclerView) findViewById(R.id.id_recycle_view_msgs);
    	}
    	if (null == mDatas) {
			mDatas = new ArrayList<ChatRecyclerBean>();
		}
    	if (mDatas.size() == 0) {
			mRecycleView.setVisibility(View.INVISIBLE);
			findViewById(R.id.layout_container_empty).setVisibility(View.VISIBLE);
		} else {
			mRecycleView.setVisibility(View.VISIBLE);
			findViewById(R.id.layout_container_empty).setVisibility(View.GONE);
		}
    }
	
	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case HDL_INIT_DONE:
			loadData();
			break;
		case HDL_NONE_MESSAGE:
			if (mDatas.isEmpty()) {
				ShowToast(R.string.on_history_messages_empty);
			} else {
				ShowToast(R.string.no_more_msg);
			}
			break;
		}
	}

	private void updateMessagesOfSession(SessionBean session) {
		// 坐席name查找
		String w_id = session.getWorker_id();
		ArchWorkerBean coWorker = mAppInfo.getCoWorker(w_id);
		String workerName = getString(R.string.worker_name);
		if (null != coWorker) {
			workerName = coWorker.getDefaultName();
		}
//		for (MessageBean msg : msgs) { /* 出现ConcurrentModificationException，改为下方Iterator实现 */
//			addRecyclerBean(msg, workerName);
////			if (msg.getMsg_dir() < 3) { // 0-坐席发出 1-顾客发 2-机器人回复 
////							
////			}
//		}
		List<V5Message> msgs = session.getMessageArray();
		synchronized(msgs) { // 手动同步
			Iterator<V5Message> iterator = msgs.iterator();  
		    while (iterator.hasNext()) { // java.util.ConcurrentModificationException
		    	V5Message msg = iterator.next();
		    	addRecyclerBean(msg, workerName);
		    }
		}
	}


	/**
	 * 请求指定月份的消息(14位精确到毫秒)
	 * @param requestMessagesOfDay HistoryMessagesActivity 
	 * @return void
	 * @param currentLongTime
	 */
	private void requestMessagesOfDay(long currentLongTime, int searchType) {
		showProgressDialog();
		int year = DateUtil.getYear(currentLongTime);
		int month = DateUtil.getMonth(currentLongTime);
		int day = DateUtil.getDay(currentLongTime);
		mCurrentSearchDay = DateUtil.getDate(year, month, day); // 该日的0点时间
		
//		// 清空界面，等待新的数据加载
//		updateUI();
		
		// 查询是否存在当月历史记录，存在则不需请求，否则进行getWorkerSession请求		
		try {
			TicketRequest tReq = (TicketRequest) RequestManager.getRequest(QAODefine.O_TYPE_WTICKET, this);
			// 查找这个月的第一天到最后一天session（一次请求一个月数据）
			Logger.i(TAG, "[requestMessagesOfDay] mCurrentSearchDay=" + DateUtil.getDayString(mCurrentSearchDay) + " mCurrentSearchMonth=" + DateUtil.getDayString(mCurrentSearchMonth) + " isExistCustomerSessionOfMonth:" + month);
			if (isExistVisitorSessionOfMonth(year, month) == MON_VAL_UNGET) {
				tReq.getCustomerSession(year, month, 0, mCustomer.getVisitor_id(), mCustomer.getF_id());
				long monthKey = DateUtil.getDate(year, month, 1);
				mMonthHasSessionMap.put(monthKey, MON_VAL_GETTING);
				Logger.i(TAG, "[requestMessagesOfDay] -> getCustomerSession of month:" + month + " mCurrentSearchMonth:" + monthKey);
			} else { // 直接更新
				Logger.d(TAG, "[requestMessagesOfDay]->[refreshData]");
				refreshData(searchType);
			}
			
//			if (!mAppInfo.isExistCustomerSessionOfMonth(mCustomer, year, month, 1)) { // 不存在则先进行请求
////				tReq.getCustomerSession(year, month, 0, mCustomer.getVisitor_id(),
////						mCustomer.getIface(), mCustomer.getChannel(), mCustomer.getService());
//				tReq.getCustomerSession(year, month, 0, mCustomer.getVisitor_id(), mCustomer.getF_id());
//				long monthKey = DateUtil.getDate(year, month, 1);
//				mMonthHasSessionMap.put(monthKey, MON_VAL_GETTING);
//				Logger.i(TAG, "[requestMessagesOfDay] -> getCustomerSession of month:" + month + " mCurrentSearchMonth:" + monthKey);
//			} else { // 直接更新
//				refreshData(searchType);
//			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search_last_day_ll:
			showProgressDialog();
			mIsRefreshing = true;
			mCurrentSearchDay -= 24 * 3600 * 1000;
			Logger.d(TAG, "[click_last_day]->[refreshData]");
			requestMessagesOfDay(mCurrentSearchDay, SEARCH_TYPE_RANGE_LAST);
			//refreshData(SEARCH_TYPE_RANGE_LAST);
			break;
			
		case R.id.search_next_day_ll:
			showProgressDialog();
			mIsRefreshing = true;
			mCurrentSearchDay += 24 * 3600 * 1000;
			Logger.d(TAG, "[click_next_day]->[refreshData]");
			requestMessagesOfDay(mCurrentSearchDay, SEARCH_TYPE_RANGE_NEXT);
			//refreshData(SEARCH_TYPE_RANGE_NEXT);
			break;
					
		case R.id.cur_date_ll:
			// show date pick popup dialog
			showCalenderDialog();
			mCurDayLL.setEnabled(false);
			break;
		}
	}

	
	/**
	 * 初始化CaldroidFragment对象，设置监听器和基本属性
	 * @param initCalender HistoryMessagesActivity 
	 * @return void
	 */
	private void initCalender() {
		if (mCustomer == null) {
			ShowToast(R.string.on_history_messages_empty);
			finishActivity();
			return;
		}
		mDialogCaldroidFragment = new CaldroidFragment();        
        mDialogCaldroidFragment.setCaldroidListener(new CaldroidListener() {			
			@Override
			public void onSelectDate(Date date, View view) {
				mDatas.clear();
				requestMessagesOfDay(date.getTime(), SEARCH_TYPE_NO_RANGE);
				mDialogCaldroidFragment.dismiss();
			}
			
			@Override
			public void onChangeMonth(int month, int year) {
				super.onChangeMonth(month, year);
				mCalShowMonth = month;
				mCalShowYear = year;
				
				try {
					TicketRequest tReq = (TicketRequest) RequestManager.getRequest(QAODefine.O_TYPE_WTICKET, HistoryMessagesActivity.this);
					// 查找这个月的第一天到最后一天（一次请求一个月数据）
					Logger.i(TAG, "[onChangeMonth（1）] mCurrentSearchDay=" + DateUtil.getDayString(mCurrentSearchDay) + " isExistCustomerSessionOfMonth:" + month);
					if (isExistVisitorSessionOfMonth(year, month) == MON_VAL_UNGET) {
						tReq.getCustomerSession(year, month, 0, mCustomer.getVisitor_id(), mCustomer.getF_id());
						long monthKey = DateUtil.getDate(year, month, 1);
						mCurrentSearchMonth = monthKey;
						mMonthHasSessionMap.put(monthKey, MON_VAL_GETTING);
						Logger.i(TAG, "[onChangeMonth] -> getCustomerSession of month:" + month + " mCurrentSearchMonth:" + monthKey);
					}
//					if (!mAppInfo.isExistCustomerSessionOfMonth(mCustomer, year, month, 1)) { // 不存在则先进行请求
//						tReq.getCustomerSession(year, month, 0, mCustomer.getVisitor_id(), mCustomer.getF_id());
//						long monthKey = DateUtil.getDate(year, month, 1);
//						mMonthHasSessionMap.put(monthKey, MON_VAL_GETTING);
//						Logger.i(TAG, "[onChangeMonth] -> getCustomerSession of month:" + month + " mCurrentSearchMonth:" + monthKey);
//					}
					
					/* 改为收到当月的再请求前一月的 */
//					if (month == 1) {
//			        	year--;
//			        	month = 12;
//			        } else {
//			        	month--;
//			        }
//					Logger.i(TAG, "[onChangeMonth（2）] mCurrentSearchDay=" + mCurrentSearchDay + " isExistCustomerSessionOfMonth:" + month);
//					if (isExistVisitorSessionOfMonth(year, month) == MON_VAL_UNGET) {
//						tReq.getCustomerSession(year, month, 0, mCustomer.getVisitor_id(), mCustomer.getF_id());
//						long monthKey = DateUtil.getDate(year, month, 1);
//						mMonthHasSessionMap.put(monthKey, MON_VAL_GETTING);
//						Logger.i(TAG, "[onChangeMonth] -> getCustomerSession of month:" + month + " mCurrentSearchMonth:" + monthKey);
//					}
					
//					if (!mAppInfo.isExistCustomerSessionOfMonth(mCustomer, year, month, 1)) { // 不存在则先进行请求
//						tReq.getCustomerSession(year, month, 0, mCustomer.getVisitor_id(), mCustomer.getF_id());
//						long monthKey = DateUtil.getDate(year, month, 1);
//						mMonthHasSessionMap.put(monthKey, MON_VAL_GETTING);
//						Logger.i(TAG, "[onChangeMonth] -> getCustomerSession of month:" + month + " mCurrentSearchMonth:" + monthKey);
//					}
					
//					month = mCalShowMonth;
//					year = mCalShowYear;
//					if (month == 12) {
//			        	year++;
//			        	month = 1;
//			        } else {
//			        	month++;
//			        }
//					Logger.i(TAG, "[onChangeMonth（3）] mCurrentSearchDay=" + mCurrentSearchDay + " isExistCustomerSessionOfMonth:" + month);
//					if (!mAppInfo.isExistCustomerSessionOfMonth(mCustomer, year, month, 1)) { // 不存在则先进行请求
//						tReq.getCustomerSession(year, month, 0, mCustomer.getVisitor_id(), mCustomer.getF_id());
//						Logger.i(TAG, "[onChangeMonth] -> getCustomerSession of month:" + month);
//					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				setCalenderShowDates(year, month);
			}
		});
                
        mDialogCaldroidFragment.setOnDismissListener(new OnDismissListener() {			
			@Override
			public void onDismiss() {
				mCurDayLL.setEnabled(true);
			}
		});
        
        setCalenderShowDates(DateUtil.getYear(), DateUtil.getMonth());
        
        // Setup arguments
        Bundle bundle = new Bundle();
        bundle.putBoolean(CaldroidFragment.ENABLE_CLICK_ON_DISABLED_DATES, false);
        // Setup dialogTitle
        mDialogCaldroidFragment.setArguments(bundle);
	}
	

	/**
	 * 过滤日历显示中的无消息日期，包含当前月和前后月
	 * @param setCalenderShowDates HistoryMessagesActivity 
	 * @return void
	 * @param year
	 * @param month
	 */
	private void setCalenderShowDates(int year, int month) {
        mDialogCaldroidFragment.clearSelectedDates();
        ArrayList<DateTime> disableDates = new ArrayList<DateTime>();
        
        int days = DateUtil.getMonthDays(year, month);
        for (int day = 1; day <= days; day++) { // 本月
        	if (isExistVisitorSessionOfDate(year, month, day)) {
        		continue;
        	}
    		disableDates.add(new DateTime(year, month, day, 0, 0, 0, 0));
        }
        
        if (month == 1) {
        	year--;
        	month = 12;
        } else {
        	month--;
        }
        days = DateUtil.getMonthDays(year, month);
        for (int day = 1; day <= days; day++) { // 前一月
        	if (isExistVisitorSessionOfDate(year, month, day)) {
        		continue;
        	}
    		disableDates.add(new DateTime(year, month, day, 0, 0, 0, 0));
        }
        
        if (month == 12) {
        	month = 2;
        	year++;
        } else if (month == 11) {
        	month = 1;
        	year++;
        } else {
        	month += 2;
        }
        days = DateUtil.getMonthDays(year, month);
        for (int day = 1; day <= days; day++) { // 后一月
        	if (isExistVisitorSessionOfDate(year, month, day)) {
        		continue;
        	}
    		disableDates.add(new DateTime(year, month, day, 0, 0, 0, 0));
        }
        mDialogCaldroidFragment.setDisableDateTimes(disableDates);
        mDialogCaldroidFragment.refreshView();
	}


	private void showCalenderDialog() {
		// Setup caldroid to use as dialog
        if (null == mDialogCaldroidFragment) {
        	initCalender();
        }
        
        // If activity is recovered from rotation
        final String dialogTag = "CALDROID_DIALOG_FRAGMENT";
                
        mDialogCaldroidFragment.show(getSupportFragmentManager(),
                dialogTag);
	}
	
	private void getMessagesOfSession(String sid) {
		if (sid == null || mSessionReqMap.containsKey(sid)) {
			return;
		}
		mSessionReqMap.put(sid, true);
		TicketRequest tReq = null;
		try {
			tReq = (TicketRequest) RequestManager.getRequest(QAODefine.O_TYPE_WTICKET, this);
			tReq.getMessages(sid);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 历史对话消息时间比较器
	 * @author Chenhy	
	 * @email chenhy@v5kf.com
	 * @version v1.0 2015-10-21 下午3:18:36
	 * @package com.v5kf.mcss.ui.activity of MCSS-Native
	 * @file HistoryMessagesActivity.java 
	 *
	 */
	static class MessageTimeCompartor implements Comparator<ChatRecyclerBean> {

		@Override
		public int compare(ChatRecyclerBean lhs, ChatRecyclerBean rhs) {
			long l = lhs.getMessage().getCreate_time();
			long r = rhs.getMessage().getCreate_time();
			if (l > r) {
				return 1;
			} else if (l < r) {
				return -1;
			} else {
				if (lhs.getMessage().getDirection() == QAODefine.MSG_DIR_FROM_ROBOT) {
					return 1;
				} else if (rhs.getMessage().getDirection() == QAODefine.MSG_DIR_FROM_ROBOT) {
					return -1;
				}
				return 0;
			}
		}
	}
	
	private boolean isExistVisitorSessionOfDate(int year, int month, int day) {
		long date = DateUtil.getDate(year, month, day);
		if (date > DateUtil.getCurrentLongTime() || date < DateUtil.getSystemInitTime()) {
			return false;
		}
		if (mDateHasSessionMap.containsKey(date)) {
			Logger.i(TAG, "有会话日期date:" + DateUtil.getDayString(date/1000));
			return true;
		}
		Logger.w(TAG, "没有会话日期date:" + DateUtil.getDayString(date/1000));
		return false;
	}

	/**
	 * 是否存在该月会话：-1-超出范围 0-未获取 1-获取该月会话中 2-存在 3-不存在
	 * @param isExistVisitorSessionOfMonth HistoryMessagesActivity 
	 * @return int
	 * @param year
	 * @param month
	 * @return
	 */
	private int isExistVisitorSessionOfMonth(int year, int month) {
		long date = DateUtil.getDate(year, month, 1);
		Logger.d(TAG, "[mMonthHasSessionMap] search Month:" + date);
		if (date > DateUtil.getCurrentLongTime() || date < DateUtil.getSystemInitTime()) {
			return MON_VAL_INVAL;
		}
		Integer mon = mMonthHasSessionMap.get(date);
		if (null == mon) {
			return MON_VAL_UNGET;
		} else {
			return mon;
		}
	}
	
	/***** event *****/
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		if (isConnect) {
			
		} else {
			finishActivity();
		}
	}
	
	@Subscriber(tag = EventTag.ETAG_SESSION_ARRAY_CHANGE, mode = ThreadMode.MAIN)
	private void sessionArrayChange(CustomerBean customer) {
		Logger.d(TAG + "-eventbus", "eventbus -> ETAG_SESSION_ARRAY_CHANGE");
		int year = DateUtil.getYear(mCurrentSearchMonth);
		int month = DateUtil.getMonth(mCurrentSearchMonth);
		Logger.i(TAG, "[ETAG_SESSION_ARRAY_CHANGE] mCurrentSearchDay=" + mCurrentSearchDay + " isExistCustomerSessionOfMonth:" + month);
		if (isExistVisitorSessionOfMonth(year, month) == MON_VAL_GETTING) {
			mMonthHasSessionMap.put(mCurrentSearchMonth, MON_VAL_GETTED);
		}
		/* 更新两个映射Map */
		initDateHasSessionMap();
		initMonthHasSessionMap();
		
		/* 更新日历上会话显示 */
		if (null != mDialogCaldroidFragment) {
			setCalenderShowDates(mCalShowYear, mCalShowMonth);
		}
		Logger.d(TAG, "[ETAG_SESSION_ARRAY_CHANGE]->[refreshData]");
		refreshData(SEARCH_TYPE_RANGE_LAST);
	}
	
	@Subscriber(tag = EventTag.ETAG_MESSAGE_ARRAY_CHANGE, mode = ThreadMode.MAIN)
	private void messageArrayChange(SessionBean session) {
		Logger.d(TAG + "-eventbus", "eventbus -> ETAG_MESSAGE_ARRAY_CHANGE");
		if (session.isActive()) {
			return;
		}
		Logger.d(TAG, "[ETAG_MESSAGE_ARRAY_CHANGE]->[refreshData]");
		refreshData(SEARCH_TYPE_NO_RANGE);
	}

}
