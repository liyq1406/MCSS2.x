package com.v5kf.mcss.ui.activity.md2x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.ArchWorkerBean;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.ui.adapter.CustomerInfoListAdapter;
import com.v5kf.mcss.ui.widget.CircleImageView;
import com.v5kf.mcss.ui.widget.ListLinearLayout;
import com.v5kf.mcss.utils.DateUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.cache.ImageLoader;

public class CustomerInfoListActivity extends BaseToolbarActivity implements OnClickListener {
	private static final String TAG = "CustomerInfoListActivity";
	protected String c_id;
	protected String v_id;
	public CustomerBean mCustomer;
	
	private View mHeadView;
	
	private TextView mNickNameTv, mIfaceTv, mVidTv, mOidTv, mVipTv;
	private CircleImageView mHeadCiv;
	private ImageView mIfaceIv, mSexIv;	
	private ViewGroup mHistoryMsgRl;
	private ViewGroup mKexiPlusLayout;
	private ViewGroup mKexiPlusRl;
	private ViewGroup mMagicLayout;
	
	// magic_info
	private ListLinearLayout mMagicListLayout;
    private CustomerInfoListAdapter mMagicAdapter;
    private List<HashMap<String, String>> mMagicList;
    // cstm_info
    private ListLinearLayout mCstmInfoListLayout;
    private CustomerInfoListAdapter mCstmInfoAdapter;
    private List<HashMap<String, String>> mCstmInfoList;
	
	private Button mStartChatBtn, mEndChatBtn;
	
	protected void handleIntent() {
		Intent intent = getIntent();
		int type = intent.getIntExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_NULL);
		if (Config.EXTRA_TYPE_ACTIVITY_START == type) {
			c_id = intent.getStringExtra(Config.EXTRA_KEY_C_ID);
			v_id = intent.getStringExtra(Config.EXTRA_KEY_V_ID);
			Logger.d(TAG, "BaseChatActivity -> Intent -> CustomerInfoActivity\n v_id:" + v_id + " c_id:" + c_id);
		}
		
		if (null != c_id) { // CSTM_ACTIVE
			mCustomer = mAppInfo.getAliveCustomer(c_id);
			if (null == mCustomer) {
	        	Logger.e(TAG, "Customer(null) not found c_id=" + c_id);
	        	finishActivity();
	        }
		} else if (null != v_id) {
			mCustomer = mAppInfo.getVisitor(v_id);
			if (null == mCustomer) {
	        	Logger.e(TAG, "Customer(null) not found v_id=" + v_id + 
	        			" count:" + mAppInfo.getVisitorMap().size());
	        	finishActivity();
	        } else {
	        	// 刷新real信息
	        	if (mCustomer.getVirtual() != null) {
	    			String real_id = mCustomer.getVirtual().getReal_id();
	    			if (null != real_id && !real_id.isEmpty()) {
						try {
							CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, this);
							cReq.getHistoryVisitorInfo(mCustomer.getVisitor_id(), mCustomer.getIface());
//							cReq.getCustomerInfo(v_id, mCustomer.getIface(), mCustomer.getChannel(), mCustomer.getService());
						} catch (JSONException e) {
							e.printStackTrace();
						}	    				
	    			}
	    		}
	        }
		} else {
			Logger.e(TAG, "Customer(null) c_id not found");
        	finishActivity();
		}
	}
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_md2x_customer_info_list);
		
		handleIntent();
		findView();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (null == mCustomer) {
			Logger.e(TAG, "[onStart] Customer(null) not found");
        	finishActivity();
		}
		initView();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}


	private void findView() {
		mHeadView = findViewById(R.id.layout_head);
		mVidTv = (TextView) findViewById(R.id.visitor_id_tv);
		mOidTv = (TextView) findViewById(R.id.open_id_tv);
		mNickNameTv = (TextView) findViewById(R.id.id_nickname_tv);
		mIfaceTv = (TextView) findViewById(R.id.id_iface_tv);
		mHeadCiv = (CircleImageView) findViewById(R.id.id_head_iv);
		mIfaceIv = (ImageView) findViewById(R.id.id_iface_iv);
		mSexIv = (ImageView) findViewById(R.id.id_sex_iv);
		mVipTv = (TextView) findViewById(R.id.id_item_vip);
		
		mHistoryMsgRl = (ViewGroup) findViewById(R.id.layout_history_msg);
		mKexiPlusLayout = (ViewGroup) findViewById(R.id.id_kexi_plus_layout);
		mKexiPlusRl = (ViewGroup) findViewById(R.id.layout_kexi_plus_url);
		mMagicLayout = (ViewGroup) findViewById(R.id.id_magic_info_layout);
		
		mStartChatBtn = (Button) findViewById(R.id.btn_start_chat);
		mEndChatBtn = (Button) findViewById(R.id.btn_end_chat);
		
		mMagicListLayout = (ListLinearLayout) findViewById(R.id.id_magic_list);
		mCstmInfoListLayout = (ListLinearLayout) findViewById(R.id.id_cstm_info_list);
	}

	private void initView() {
		if (mCustomer != null) {
			initTopBarForLeftBack(mCustomer.getDefaultName()); // 标题栏
		} else {
			initTopBarForLeftBack(R.string.customer_info); // 标题栏
		}
    	initFirstLayout(); // header信息
    	// 是否显示客服助手
    	if (mAppInfo.getSiteInfo() != null && mAppInfo.getSiteInfo().getKexi_plus_url() != null) {
    		mKexiPlusLayout.setVisibility(View.VISIBLE);
    	} else {
    		mKexiPlusLayout.setVisibility(View.GONE);
    	}
		initListLayout(); // info列表
		addListener();
		initBottomBtn(); // 底部按钮
	}

	/**
	 * 初始化底部按钮
	 */
	private void initBottomBtn() {
		switch (mCustomer.getCstmType()) {
		case CustomerType_ServingAlive:
			mEndChatBtn.setVisibility(View.VISIBLE);
			mStartChatBtn.setVisibility(View.GONE);
			break;
		case CustomerType_WaitingAlive:
			mEndChatBtn.setVisibility(View.GONE);
			mStartChatBtn.setVisibility(View.GONE);
			break;
		case CustomerType_Visitor:
			if (mCustomer.getAccessable() != null && 
				mCustomer.getAccessable().equals(QAODefine.ACCESSABLE_IDLE)) {
				mStartChatBtn.setVisibility(View.VISIBLE);
			} else {
				mStartChatBtn.setVisibility(View.GONE);
			}
			mEndChatBtn.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	/**
	 * 初始化头部布局：头像、昵称
	 */
	private void initFirstLayout() {
		Logger.d(TAG, "Name:" + mCustomer.getDefaultName());
		mNickNameTv.setText(mCustomer.getDefaultName());
		if (Config.SHOW_VID_OID) {
			String v_id = mCustomer.getVisitor_id();
			String o_id = mCustomer.getVirtual() != null ? mCustomer.getVirtual().getOpen_id() : null;
			if (!TextUtils.isEmpty(v_id)) {
				mVidTv.setText(getString(R.string.v_id_pre) + v_id);
				mVidTv.setVisibility(View.VISIBLE);
			} else {
				mVidTv.setVisibility(View.GONE);
			}
			if (!TextUtils.isEmpty(o_id)) {
				mOidTv.setText(getString(R.string.o_id_pre) + o_id);
				mOidTv.setVisibility(View.VISIBLE);
			} else {
				mOidTv.setVisibility(View.GONE);
			}
		}
		ImageLoader imgLoader = new ImageLoader(this, true, R.drawable.v5_photo_default_cstm);
    	imgLoader.DisplayImage(mCustomer.getDefaultPhoto(), mHeadCiv);
    	
    	// 设置interface信息
    	UITools.setInterfaceInfo(mCustomer.getIface(), mIfaceTv, mIfaceIv);
		// 设置VIP信息
    	UITools.setVipInfo(mCustomer.getVip(), mVipTv);
		// 性别
		if (mCustomer.getDefaultSex() == 1) {
			mSexIv.setImageResource(R.drawable.ic_user_male);
		} else if (mCustomer.getDefaultSex() == 2) {
			mSexIv.setImageResource(R.drawable.ic_user_female);
		} else {
			mSexIv.setVisibility(View.GONE);
		}
		
		if (c_id == null && v_id != null) {
			findViewById(R.id.cstm_edit_iv).setVisibility(View.GONE);
		}
	}

	private void initListLayout() {
		initMagicList();
		initCstmInfoList();
	}

	private void initCstmInfoList() {
		if (null == mCstmInfoList) {
			mCstmInfoList = new ArrayList<HashMap<String, String>>();
		} else {
			mCstmInfoList.clear();
		}
		
		// 真实姓名
		if (mCustomer.getDefaultRealname() != null && !mCustomer.getDefaultRealname().isEmpty()) {
			HashMap<String, String> item = new HashMap<String, String>(1);
		    item.put("真实姓名", mCustomer.getDefaultRealname());
		    mCstmInfoList.add(item);
		}
		// 电话
		if (mCustomer.getDefaultPhone() != null && !mCustomer.getDefaultPhone().isEmpty()) {
			HashMap<String, String> item = new HashMap<String, String>(1);
		    item.put("电话", mCustomer.getDefaultPhone());
		    mCstmInfoList.add(item);
		}
		// 邮箱
		if (mCustomer.getDefaultEmail() != null && !mCustomer.getDefaultEmail().isEmpty()) {
			HashMap<String, String> item = new HashMap<String, String>(1);
		    item.put("邮箱", mCustomer.getDefaultEmail());
		    mCstmInfoList.add(item);
		}
		// 微信
		if (mCustomer.getDefaultWeixin() != null && !mCustomer.getDefaultWeixin().isEmpty()) {
			HashMap<String, String> item = new HashMap<String, String>(1);
		    item.put("微信", mCustomer.getDefaultWeixin());
		    mCstmInfoList.add(item);
		}
		// QQ
		if (mCustomer.getDefaultQQ() != null && !mCustomer.getDefaultQQ().isEmpty()) {
			HashMap<String, String> item = new HashMap<String, String>(1);
		    item.put("QQ", mCustomer.getDefaultQQ());
		    mCstmInfoList.add(item);
		}
		// 国家
		if (mCustomer.getDefaultCountry() != null && !mCustomer.getDefaultCountry().isEmpty()) {
			HashMap<String, String> item = new HashMap<String, String>(1);
		    item.put("国家", mCustomer.getDefaultCountry());
		    mCstmInfoList.add(item);
		}
		// 省份
		if (mCustomer.getDefaultProvince() != null && !mCustomer.getDefaultProvince().isEmpty()) {
			HashMap<String, String> item = new HashMap<String, String>(1);
		    item.put("省份", mCustomer.getDefaultProvince());
		    mCstmInfoList.add(item);
		}
		// 城市
		if (mCustomer.getDefaultCity() != null && !mCustomer.getDefaultCity().isEmpty()) {
			HashMap<String, String> item = new HashMap<String, String>(1);
		    item.put("城市", mCustomer.getDefaultCity());
		    mCstmInfoList.add(item);
		}
		// 地址
		if (mCustomer.getDefaultAddress() != null && !mCustomer.getDefaultAddress().isEmpty()) {
			HashMap<String, String> item = new HashMap<String, String>(1);
		    item.put("地址", mCustomer.getDefaultAddress());
		    mCstmInfoList.add(item);
		}
		// 公司
		if (mCustomer.getDefaultCompany() != null && !mCustomer.getDefaultCompany().isEmpty()) {
			HashMap<String, String> item = new HashMap<String, String>(1);
		    item.put("公司", mCustomer.getDefaultCompany());
		    mCstmInfoList.add(item);
		}
		// 系统
		if (mCustomer.getVirtual() != null && mCustomer.getVirtual().getOs() > 0) {
			HashMap<String, String> item = new HashMap<String, String>(1);
			item.put("系统", UITools.stringOfCstmOs(mCustomer.getVirtual().getOs()));
			mCstmInfoList.add(item);
		}
		// 最近服务坐席
		if (mCustomer.getVirtual() != null && mCustomer.getVirtual().getLast_worker() != null
				&& !mCustomer.getVirtual().getLast_worker().isEmpty()) {
			HashMap<String, String> item = new HashMap<String, String>(1);
			ArchWorkerBean worker = mAppInfo.getCoWorker(mCustomer.getVirtual().getLast_worker());
			if (worker != null) {
				item.put("最近服务", worker.getDefaultName());
				mCstmInfoList.add(item);
			}
		}
		// 最近访问
		if (mCustomer.getVirtual() != null && mCustomer.getVirtual().getLast_service() > 0) {
			HashMap<String, String> item = new HashMap<String, String>(1);
			item.put("最近访问", DateUtil.timeFormat(mCustomer.getVirtual().getLast_service(), false));
			mCstmInfoList.add(item);
		}
		// 对话次数
		if (mCustomer.getVirtual() != null) {
			HashMap<String, String> item = new HashMap<String, String>(1);
		    item.put("对话次数", String.valueOf(mCustomer.getVirtual().getChats()));
		    mCstmInfoList.add(item);
		}
		// 访问次数
		if (mCustomer.getVirtual() != null) {
			HashMap<String, String> item = new HashMap<String, String>(1);
		    item.put("访问次数", String.valueOf(mCustomer.getVirtual().getVisits()));
		    mCstmInfoList.add(item);
		}
		
		if (null == mCstmInfoAdapter) {
			mCstmInfoAdapter = new CustomerInfoListAdapter(this, mCstmInfoList);
		}
		mCstmInfoListLayout.bindLinearLayout(mCstmInfoAdapter);
	}

	private void initMagicList() {
		if (mCustomer.getCustom_content() != null && mCustomer.getCustom_content().size() > 0) {
			// 有magic信息，加载magic
			mMagicLayout.setVisibility(View.VISIBLE);
			if (null == mMagicList) {
				mMagicList = new ArrayList<HashMap<String, String>>();
			} else {
				mMagicList.clear();
			}
			
			Iterator<Map.Entry<String, String>> entries = mCustomer.getCustom_content().entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry<String, String> entry = (Map.Entry<String, String>)entries.next();
			    String key = entry.getKey();
			    String value = entry.getValue();
			    HashMap<String, String> item = new HashMap<String, String>(1);
			    item.put(key, value);
			    mMagicList.add(item);
			}
			
			if (null == mMagicAdapter) {
				mMagicAdapter = new CustomerInfoListAdapter(this, mMagicList);
			}
			mMagicListLayout.bindLinearLayout(mMagicAdapter);
		} else {
			// 无magic信息
			mMagicLayout.setVisibility(View.GONE);
		}
	}

	private void addListener() {
		mHeadView.setOnClickListener(this);
		findViewById(R.id.cstm_edit_iv).setOnClickListener(this);
		mKexiPlusRl.setOnClickListener(this);
		mHistoryMsgRl.setOnClickListener(this);
		
		mEndChatBtn.setOnClickListener(this);
		mStartChatBtn.setOnClickListener(this);
		
		mHeadCiv.setOnClickListener(this);
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		default:
			break;
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.layout_history_msg:
			gotoHistoryMessagesActivity();
			break;
		
		case R.id.layout_kexi_plus_url:
			gotoKexiPlusWebActivity();
			break;
			
		case R.id.cstm_edit_iv:
			if (c_id != null && v_id == null) {
				gotoCustomrtEditActivity();
			}
			break;
		
		case R.id.btn_end_chat:
			showAlertDialog(R.string.confirm_end_session, new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					try {
						CustomerRequest creq = (CustomerRequest) RequestManager.getRequest(
								QAODefine.O_TYPE_WCSTM, 
								CustomerInfoListActivity.this);
						creq.endSession(c_id);
						setResult(Config.RESULT_CODE_END_SESSION);
						finish();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}, null);
			break;
		
		case R.id.btn_start_chat:
			showAlertDialog(R.string.confirm_start_session, new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					try {
						CustomerRequest creq = (CustomerRequest) RequestManager.getRequest(
								QAODefine.O_TYPE_WCSTM, 
								CustomerInfoListActivity.this);
						if (c_id != null) {
							creq.pickCustomer(c_id);
						} else if (v_id != null) {
							creq.pickCustomer(v_id, mCustomer.getF_id());
//							creq.pickCustomer(v_id, mCustomer.getIface(), mCustomer.getChannel(), mCustomer.getService());
						}
						setResult(Config.RESULT_CODE_PICKUP_CSTM);
						finish();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}, null);
			break;
			
		case R.id.id_head_iv:
			if (!TextUtils.isEmpty(mCustomer.getDefaultPhoto())) {
				gotoShowImageActivity(mCustomer.getDefaultPhoto());
			}
			break;
		}
	}

	private void gotoKexiPlusWebActivity() {
		String type = mCustomer.getIfaceString();
		String open_id = mCustomer.getVirtual().getOpen_id();
		String url = mAppInfo.getSiteInfo().getKexi_plus_url() + "&type=" + type + "&open_id=" + open_id;
		gotoWebViewActivity(url, R.string.kexi_plus);
	}

	private void gotoShowImageActivity(String pic_url) {
		this.gotoImageActivity(pic_url);
	}

	private void gotoHistoryMessagesActivity() {
		Bundle bundle = new Bundle();
		bundle.putInt(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_ACTIVITY_START);
		bundle.putString(Config.EXTRA_KEY_V_ID, mCustomer.getVisitor_id());
		bundle.putString(Config.EXTRA_KEY_C_ID, mCustomer.getC_id());
		
		Intent intent = new Intent(this, HistoryMessagesActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}
	
	private void gotoCustomrtEditActivity() {
		Bundle bundle = new Bundle();
		bundle.putInt(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_ACTIVITY_START);
		bundle.putString(Config.EXTRA_KEY_C_ID, c_id);
		bundle.putString(Config.EXTRA_KEY_V_ID, v_id);
		
		Intent intent = new Intent(this, CustomerEditActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
	}
	
	/***** event *****/

	@Subscriber(tag = EventTag.ETAG_UPDATE_CSTM_INFO, mode = ThreadMode.MAIN)
	private void updateCustomerInfo(CustomerBean cstm) {
		Logger.d(TAG + "-eventbus", "updateCustomerInfo -> ETAG_UPDATE_CSTM_INFO");
		if (c_id != null && cstm.getC_id() != null && cstm.getC_id().equals(c_id)) {
			initFirstLayout();
			initListLayout();
		} else if (v_id != null && v_id.equals(cstm.getVisitor_id())) {
			initFirstLayout();
			initListLayout();
		}
    }
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		if (isConnect) {
			
		} else {
			finishActivity();
		}
	}

	@Override
	protected void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
	}
	
}
