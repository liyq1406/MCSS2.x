package com.v5kf.mcss.ui.activity.info;

import org.json.JSONException;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.ui.activity.md2x.CustomerEditActivity;
import com.v5kf.mcss.ui.activity.md2x.HistoryMessagesActivity;
import com.v5kf.mcss.ui.widget.CircleImageView;
import com.v5kf.mcss.ui.widget.WarningDialog;
import com.v5kf.mcss.ui.widget.WarningDialog.WarningDialogListener;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.cache.ImageLoader;

public class CustomerInfoActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "CustomerInfoActivity";
	protected String c_id;
	protected String v_id;
	public CustomerBean mCustomer;
	
	private View mHeadView;
	
	private TextView mNickNameTv, mIfaceTv;
	private CircleImageView mHeadCiv;
	private ImageView mIfaceIv, mSexIv;	
	private RelativeLayout mHistoryMsgRl, mRealnameRl, mPhoneRl, mEmailRl, mWeixinRl, mQQRl, mCountryRl, 
		mProvinceRl, mCityRl, mAddressRl, mCompanyRl, mChatsRl, mVisitsRl;
	private TextView mRealnameTv, mPhoneTv, mEmailTv, mWeixinTv, mQQTv, mCountryTv, mProvinceTv, 
		mCityTv, mAddressTv, mCompanyTv, mChatsTv, mVisitsTv;
	private View mPhoneDiv, mEmailDiv, mWeixinDiv, mQQDiv, mCountryDiv, mProvinceDiv, 
		mCityDiv, mAddressDiv, mCompanyDiv, mChatsDiv, mVisitsDiv;
	
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
			mCustomer = mAppInfo.getCustomerBean(c_id);
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
		setContentView(R.layout.activity_customer_info);
		
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
		mNickNameTv = (TextView) findViewById(R.id.id_nickname_tv);
		mIfaceTv = (TextView) findViewById(R.id.id_iface_tv);
		mHeadCiv = (CircleImageView) findViewById(R.id.id_head_iv);
		mIfaceIv = (ImageView) findViewById(R.id.id_iface_iv);
		mSexIv = (ImageView) findViewById(R.id.id_sex_iv);
		
		mHistoryMsgRl = (RelativeLayout) findViewById(R.id.layout_history_msg);	
		mRealnameRl = (RelativeLayout) findViewById(R.id.layout_realname);	
		mPhoneRl = (RelativeLayout) findViewById(R.id.layout_phone);		
		mEmailRl = (RelativeLayout) findViewById(R.id.layout_email);		
		mWeixinRl = (RelativeLayout) findViewById(R.id.layout_weixin);		
		mQQRl = (RelativeLayout) findViewById(R.id.layout_qq);		
		mCountryRl = (RelativeLayout) findViewById(R.id.layout_country);		
		mProvinceRl = (RelativeLayout) findViewById(R.id.layout_province);		
		mCityRl = (RelativeLayout) findViewById(R.id.layout_city);		
		mAddressRl = (RelativeLayout) findViewById(R.id.layout_address);		
		mCompanyRl = (RelativeLayout) findViewById(R.id.layout_company);		
		mChatsRl = (RelativeLayout) findViewById(R.id.layout_chats);		
		mVisitsRl = (RelativeLayout) findViewById(R.id.layout_visits);		
		
		mRealnameTv = (TextView) findViewById(R.id.id_realname_tv);
		mPhoneTv = (TextView) findViewById(R.id.id_phone_tv);
		mEmailTv = (TextView) findViewById(R.id.id_email_tv);
		mWeixinTv = (TextView) findViewById(R.id.id_weixin_tv);
		mQQTv = (TextView) findViewById(R.id.id_qq_tv);
		mCountryTv = (TextView) findViewById(R.id.id_country_tv);
		mProvinceTv = (TextView) findViewById(R.id.id_province_tv);
		mCityTv = (TextView) findViewById(R.id.id_city_tv);
		mAddressTv = (TextView) findViewById(R.id.id_address_tv);
		mCompanyTv = (TextView) findViewById(R.id.id_company_tv);
		mChatsTv = (TextView) findViewById(R.id.id_chats_tv);
		mVisitsTv = (TextView) findViewById(R.id.id_visits_tv);

		mPhoneDiv = findViewById(R.id.divider_phone);
		mEmailDiv = findViewById(R.id.divider_email);
		mWeixinDiv = findViewById(R.id.divider_weixin);
		mQQDiv = findViewById(R.id.divider_qq);
		mCountryDiv = findViewById(R.id.divider_country);
		mProvinceDiv = findViewById(R.id.divider_province);
		mCityDiv = findViewById(R.id.divider_city);
		mAddressDiv = findViewById(R.id.divider_address);
		mCompanyDiv = findViewById(R.id.divider_company);	
		mChatsDiv = findViewById(R.id.divider_chats);	
		mVisitsDiv = findViewById(R.id.divider_visits);	
		
		mStartChatBtn = (Button) findViewById(R.id.btn_start_chat);
		mEndChatBtn = (Button) findViewById(R.id.btn_end_chat);
	}

	private void initView() {
		initTopBarForLeftBack(R.string.customer_info);
    	
    	initFirstLayout();    	
		initLayoutWithTextview();
		addListener();		
		initBottomBtn();
	}

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


	private void initFirstLayout() {
		mNickNameTv.setText(mCustomer.getDefaultName());
		ImageLoader imgLoader = new ImageLoader(this, true, R.drawable.v5_photo_default_cstm, null);
    	imgLoader.DisplayImage(mCustomer.getDefaultPhoto(), mHeadCiv);
    	
    	// 设置interface信息
    	UITools.setInterfaceInfo(mCustomer.getIface(), mIfaceTv, mIfaceIv);
		
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

	private void initLayoutWithTextview() {
		boolean isFirst = true;
		// 真实姓名
		if (mCustomer.getDefaultRealname() != null && !mCustomer.getDefaultRealname().isEmpty()) {
			mRealnameRl.setVisibility(View.VISIBLE);
			mRealnameTv.setText(mCustomer.getDefaultRealname());
			isFirst = false;
		} else {
			mRealnameRl.setVisibility(View.GONE);
			if (isFirst) {
				mPhoneDiv.setVisibility(View.GONE);
			}
		}
		// 电话
		if (mCustomer.getDefaultPhone() != null && !mCustomer.getDefaultPhone().isEmpty()) {
			mPhoneRl.setVisibility(View.VISIBLE);
			mPhoneTv.setText(mCustomer.getDefaultPhone());
			if (!isFirst) {
				mPhoneDiv.setVisibility(View.VISIBLE);
			}
			isFirst = false;
		} else {
			mPhoneRl.setVisibility(View.GONE);
			if (isFirst) {
				mEmailDiv.setVisibility(View.GONE);
			}
		}
		// 邮箱
		if (mCustomer.getDefaultEmail() != null && !mCustomer.getDefaultEmail().isEmpty()) {
			mEmailRl.setVisibility(View.VISIBLE);
			mEmailTv.setText(mCustomer.getDefaultEmail());
			if (!isFirst) {
				mEmailDiv.setVisibility(View.VISIBLE);
			}
			isFirst = false;
		} else {
			mEmailRl.setVisibility(View.GONE);
			if (isFirst) {
				mWeixinDiv.setVisibility(View.GONE);
			}
		}
		// 微信
		if (mCustomer.getDefaultWeixin() != null && !mCustomer.getDefaultWeixin().isEmpty()) {
			mWeixinRl.setVisibility(View.VISIBLE);
			mWeixinTv.setText(mCustomer.getDefaultWeixin());
			if (!isFirst) {
				mWeixinDiv.setVisibility(View.VISIBLE);
			}
			isFirst = false;
		} else {
			mWeixinRl.setVisibility(View.GONE);
			if (isFirst) {
				mQQDiv.setVisibility(View.GONE);
			}
		}
		// QQ
		if (mCustomer.getDefaultQQ() != null && !mCustomer.getDefaultQQ().isEmpty()) {
			mQQRl.setVisibility(View.VISIBLE);
			mQQTv.setText(mCustomer.getDefaultQQ());
			if (!isFirst) {
				mQQDiv.setVisibility(View.VISIBLE);
			}
			isFirst = false;
		} else {
			mQQRl.setVisibility(View.GONE);
			if (isFirst) {
				mCountryDiv.setVisibility(View.GONE);
			}
		}
		// 国家
		if (mCustomer.getDefaultCountry() != null && !mCustomer.getDefaultCountry().isEmpty()) {
			mCountryRl.setVisibility(View.VISIBLE);
			mCountryTv.setText(mCustomer.getDefaultCountry());
			if (!isFirst) {
				mCountryDiv.setVisibility(View.VISIBLE);
			}
			isFirst = false;
		} else {
			mCountryRl.setVisibility(View.GONE);
			if (isFirst) {
				mProvinceDiv.setVisibility(View.GONE);
			}
		}
		// 省份
		if (mCustomer.getDefaultProvince() != null && !mCustomer.getDefaultProvince().isEmpty()) {
			mProvinceRl.setVisibility(View.VISIBLE);
			mProvinceTv.setText(mCustomer.getDefaultProvince());
			if (!isFirst) {
				mProvinceDiv.setVisibility(View.VISIBLE);
			}
			isFirst = false;
		} else {
			mProvinceRl.setVisibility(View.GONE);
			if (isFirst) {
				mCityDiv.setVisibility(View.GONE);
			}
		}
		// 城市
		if (mCustomer.getDefaultCity() != null && !mCustomer.getDefaultCity().isEmpty()) {
			mCityRl.setVisibility(View.VISIBLE);
			mCityTv.setText(mCustomer.getDefaultCity());
			if (!isFirst) {
				mCityDiv.setVisibility(View.VISIBLE);
			}
			isFirst = false;
		} else {
			mCityRl.setVisibility(View.GONE);
			if (isFirst) {
				mAddressDiv.setVisibility(View.GONE);
			}
		}
		// 地址
		if (mCustomer.getDefaultAddress() != null && !mCustomer.getDefaultAddress().isEmpty()) {
			mAddressRl.setVisibility(View.VISIBLE);
			mAddressTv.setText(mCustomer.getDefaultAddress());
			if (!isFirst) {
				mAddressDiv.setVisibility(View.VISIBLE);
			}
			isFirst = false;
		} else {
			mAddressRl.setVisibility(View.GONE);
			if (isFirst) {
				mCompanyDiv.setVisibility(View.GONE);
			}
		}
		// 公司
		if (mCustomer.getDefaultCompany() != null && !mCustomer.getDefaultCompany().isEmpty()) {
			mCompanyRl.setVisibility(View.VISIBLE);
			mCompanyTv.setText(mCustomer.getDefaultCompany());
			if (!isFirst) {
				mCompanyDiv.setVisibility(View.VISIBLE);
			}
			isFirst = false;
		} else {
			mCompanyRl.setVisibility(View.GONE);
		}
		
		// 对话次数
		if (mCustomer.getVirtual() != null && mCustomer.getVirtual().getChats() > 0) {
			mChatsRl.setVisibility(View.VISIBLE);
			mChatsTv.setText(String.valueOf(mCustomer.getVirtual().getChats()));
			if (!isFirst) {
				mChatsDiv.setVisibility(View.VISIBLE);
			}
			isFirst = false;
		} else {
			mChatsRl.setVisibility(View.GONE);
		}
		
		// 访问次数
		if (mCustomer.getVirtual() != null && mCustomer.getVirtual().getVisits() > 0) {
			mVisitsRl.setVisibility(View.VISIBLE);
			mVisitsTv.setText(String.valueOf(mCustomer.getVirtual().getVisits()));
			if (!isFirst) {
				mVisitsDiv.setVisibility(View.VISIBLE);
			}
			isFirst = false;
		} else {
			mVisitsRl.setVisibility(View.GONE);
		}
		
		if (isFirst) {
			findViewById(R.id.group_top_div).setVisibility(View.GONE);
			findViewById(R.id.group_bottom_div).setVisibility(View.GONE);
		}
	}

	private void addListener() {
		mHeadView.setOnClickListener(this);
		mHistoryMsgRl.setOnClickListener(this);
		mRealnameRl.setOnClickListener(this);
		mPhoneRl.setOnClickListener(this);
		mEmailRl.setOnClickListener(this);
		mWeixinRl.setOnClickListener(this);
		mQQRl.setOnClickListener(this);
		mCountryRl.setOnClickListener(this);
		mProvinceRl.setOnClickListener(this);
		mCityRl.setOnClickListener(this);
		mAddressRl.setOnClickListener(this);
		mCompanyRl.setOnClickListener(this);

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
		
		case R.id.layout_head:
			if (c_id != null && v_id == null) {
				gotoCustomrtEditActivity();
			}
			break;
		
		case R.id.btn_end_chat:
			showConfirmDialog(
					R.string.confirm_end_session,
					WarningDialog.MODE_TWO_BUTTON, 
					new WarningDialogListener() {						
				@Override
				public void onClick(View view) {
					switch (view.getId()) {
					case R.id.btn_dialog_warning_left:								
						break;
					case R.id.btn_dialog_warning_right:
						try {
							CustomerRequest creq = (CustomerRequest) RequestManager.getRequest(
									QAODefine.O_TYPE_WCSTM, 
									CustomerInfoActivity.this);
							creq.endSession(c_id);
							setResult(Config.RESULT_CODE_END_SESSION);
							dismissWarningDialog();
							finishActivity();
						} catch (JSONException e) {
							e.printStackTrace();
						}
						break;
					}
				}
			});
			break;
		
		case R.id.btn_start_chat:
			showConfirmDialog(
					R.string.confirm_start_session,
					WarningDialog.MODE_TWO_BUTTON, 
					new WarningDialogListener() {						
				@Override
				public void onClick(View view) {
					switch (view.getId()) {
					case R.id.btn_dialog_warning_left:								
						break;
					case R.id.btn_dialog_warning_right:
						try {
							CustomerRequest creq = (CustomerRequest) RequestManager.getRequest(
									QAODefine.O_TYPE_WCSTM, 
									CustomerInfoActivity.this);
							if (c_id != null) {
								creq.pickCustomer(c_id);
							} else if (v_id != null) {
								creq.pickCustomer(v_id, mCustomer.getF_id());
//								creq.pickCustomer(v_id, mCustomer.getIface(), mCustomer.getChannel(), mCustomer.getService());
							}
							setResult(Config.RESULT_CODE_PICKUP_CSTM);
							dismissWarningDialog();
							finishActivity();
						} catch (JSONException e) {
							e.printStackTrace();
						}
						break;
					}
				}
			});
			break;
			
		case R.id.id_head_iv:
			if (!TextUtils.isEmpty(mCustomer.getDefaultPhoto())) {
				gotoShowImageActivity(mCustomer.getDefaultPhoto());
			}
			break;
		}
	}

	private void gotoShowImageActivity(String pic_url) {
		this.gotoImageActivity(pic_url);
	}

	private void gotoHistoryMessagesActivity() {
		Bundle bundle = new Bundle();
		bundle.putInt(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_ACTIVITY_START);
		bundle.putString(Config.EXTRA_KEY_V_ID, mCustomer.getVisitor_id());
		
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
		if (cstm.getC_id().equals(c_id)) {
			initFirstLayout();
			initLayoutWithTextview();
		} else if (v_id != null && v_id.equals(cstm.getVisitor_id())) {
			initFirstLayout();
			initLayoutWithTextview();
		}
    }
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		if (isConnect) {
			
		} else {
			finishActivity();
		}
	}
}
