package com.v5kf.mcss.ui.activity.md2x;

import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.entity.CustomerRealBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.ui.widget.ClearEditText;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.cache.ImageLoader;

public class CustomerEditActivity extends BaseToolbarActivity implements OnClickListener {
	private static final String TAG = "CustomerInfoActivity";
	protected String c_id;
	public CustomerBean mCustomer;
	
	private View mHeadView;
	
	private TextView mNickNameTv, mIfaceTv;
	private ImageView mHeadIv, mIfaceIv, mSexIv;
	
	private ClearEditText mRealnameEt, mPhoneEt, mEmailEt, mWeixinEt, mQQEt, mCountryEt, mProvinceEt, 
		mCityEt, mAddressEt, mCompanyEt;
	
	protected void handleIntent() {
		Intent intent = getIntent();
		int type = intent.getIntExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_NULL);
		if (Config.EXTRA_TYPE_ACTIVITY_START == type) {
			c_id = intent.getStringExtra(Config.EXTRA_KEY_C_ID);
			Logger.d(TAG, "BaseChatActivity -> Intent -> CustomerInfoActivity\n c_id:" + c_id);
		}
		
		if (null != c_id) { // CSTM_ACTIVE
			mCustomer = mAppInfo.getAliveCustomer(c_id);
			if (null == mCustomer) {
	        	Logger.e(TAG, "Customer(null) not found");
	        	finishActivity();
	        }
		} else {
			Logger.e(TAG, "Customer(null) c_id/v_id not found");
        	finishActivity();
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_md2x_customer_edit);
		
		handleIntent();
		findView();
		initView();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (null == mCustomer) {
        	Logger.e(TAG, "[onStart] Customer(null) not found");
        	finishActivity();
        }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem itemAdd=menu.add(0, Menu.FIRST, Menu.NONE, R.string.submit);
		itemAdd.setIcon(R.drawable.v5_action_bar_ok);
		itemAdd.setShortcut('0', 's');
		itemAdd.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST:
			try {
				doSetCustomerInfo();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			finishActivity();
			break;
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void findView() {
		mHeadView = findViewById(R.id.layout_head);
		mNickNameTv = (TextView) findViewById(R.id.id_nickname_tv);
		mIfaceTv = (TextView) findViewById(R.id.id_iface_tv);
		mHeadIv = (ImageView) findViewById(R.id.id_head_iv);
		mIfaceIv = (ImageView) findViewById(R.id.id_iface_iv);
		mSexIv = (ImageView) findViewById(R.id.id_sex_iv);
				
		mRealnameEt = (ClearEditText) findViewById(R.id.id_realname_et);
		mPhoneEt = (ClearEditText) findViewById(R.id.id_phone_et);
		mEmailEt = (ClearEditText) findViewById(R.id.id_email_et);
		mWeixinEt = (ClearEditText) findViewById(R.id.id_weixin_et);
		mQQEt = (ClearEditText) findViewById(R.id.id_qq_et);
		mCountryEt = (ClearEditText) findViewById(R.id.id_country_et);
		mProvinceEt = (ClearEditText) findViewById(R.id.id_province_et);
		mCityEt = (ClearEditText) findViewById(R.id.id_city_et);
		mAddressEt = (ClearEditText) findViewById(R.id.id_address_et);
		mCompanyEt = (ClearEditText) findViewById(R.id.id_company_et);
	}

	private void initView() {
		initTopBarForLeftBack(R.string.customer_edit); // 标题栏
//		initTopBarForLeftImageAndRightImage(
//				R.string.customer_edit, 
//				R.drawable.v5_action_bar_cancel, 
//				R.drawable.v5_action_bar_ok, 
//				new onLeftImageButtonClickListener() {			
//					@Override
//					public void onClick(View v) {
//						finishActivity();
//					}
//				}, 
//				new onRightImageButtonClickListener() {
//				
//					@Override
//					public void onClick(View arg0) {
//						try {
//							doSetCustomerInfo();
//						} catch (JSONException e) {
//							e.printStackTrace();
//						}
//						finishActivity();
//					}
//				});
    	
    	initFirstLayout();
		initLayoutWithTextview();
		addListener();
	}

	private void initFirstLayout() {
		if (mCustomer == null) {
			return;
		}
		mNickNameTv.setText(mCustomer.getDefaultName());
		ImageLoader imgLoader = new ImageLoader(this, true, R.drawable.v5_photo_default_cstm);
    	imgLoader.DisplayImage(mCustomer.getDefaultPhoto(), mHeadIv);
    	
    	// 设置interface信息
    	UITools.setInterfaceInfo(mCustomer.getIface(), mIfaceTv, mIfaceIv);
    	
		// 性别
		if (mCustomer.getSex() != 0) {
			if (mCustomer.getSex() == 1) {
				mSexIv.setImageResource(R.drawable.ic_user_male);
			} else if (mCustomer.getSex() == 2) {
				mSexIv.setImageResource(R.drawable.ic_user_female);
			} else {
				mSexIv.setVisibility(View.GONE);
			}
		} else {
			mSexIv.setVisibility(View.GONE);
		}
	}

	private void initLayoutWithTextview() {
		if (mCustomer == null) {
			return;
		}
		
		// 真实姓名
		if (mCustomer.getDefaultRealname() != null && !mCustomer.getDefaultRealname().isEmpty()) {
			mRealnameEt.setText(mCustomer.getDefaultRealname());
		}
		// 电话
		if (mCustomer.getDefaultPhone() != null && !mCustomer.getDefaultPhone().isEmpty()) {
			mPhoneEt.setText(mCustomer.getDefaultPhone());
		}
		// 邮箱
		if (mCustomer.getDefaultEmail() != null && !mCustomer.getDefaultEmail().isEmpty()) {
			mEmailEt.setText(mCustomer.getDefaultEmail());
		}
		// 微信
		if (mCustomer.getDefaultWeixin() != null && !mCustomer.getDefaultWeixin().isEmpty()) {
			mWeixinEt.setText(mCustomer.getDefaultWeixin());
		}
		// QQ
		if (mCustomer.getDefaultQQ() != null && !mCustomer.getDefaultQQ().isEmpty()) {
			mQQEt.setText(mCustomer.getDefaultQQ());
		}
		// 国家
		if (mCustomer.getDefaultCountry() != null && !mCustomer.getDefaultCountry().isEmpty()) {
			mCountryEt.setText(mCustomer.getDefaultCountry());
		}
		// 省份
		if (mCustomer.getDefaultProvince() != null && !mCustomer.getDefaultProvince().isEmpty()) {
			mProvinceEt.setText(mCustomer.getDefaultProvince());
		}
		// 城市
		if (mCustomer.getDefaultCity() != null && !mCustomer.getDefaultCity().isEmpty()) {
			mCityEt.setText(mCustomer.getDefaultCity());
		}
		// 地址
		if (mCustomer.getDefaultAddress() != null && !mCustomer.getDefaultAddress().isEmpty()) {
			mAddressEt.setText(mCustomer.getDefaultAddress());
		}
		// 公司
		if (mCustomer.getDefaultCompany() != null && !mCustomer.getDefaultCompany().isEmpty()) {
			mCompanyEt.setText(mCustomer.getDefaultCompany());
		}
	}

	private void addListener() {
		mHeadView.setOnClickListener(this);

	}

	protected void doSetCustomerInfo() throws JSONException {
		JSONObject realJson = new JSONObject();
		CustomerRealBean cstmReal = mCustomer.getReal();
		if (!mRealnameEt.getText().toString().isEmpty() || 
				(cstmReal != null && cstmReal.getRealname() != null)) {
			realJson.putOpt("realname", mRealnameEt.getText().toString());
		}
		if (!mPhoneEt.getText().toString().isEmpty() || 
				(cstmReal != null && cstmReal.getPhone() != null)) {
			realJson.putOpt("phone", mPhoneEt.getText().toString());
		}
		if (!mEmailEt.getText().toString().isEmpty() || 
				(cstmReal != null && cstmReal.getEmail() != null)) {
			realJson.putOpt("email", mEmailEt.getText().toString());
		}
		if (!mWeixinEt.getText().toString().isEmpty() || 
				(cstmReal != null && cstmReal.getWeixin() != null)) {
			realJson.putOpt("weixin", mWeixinEt.getText().toString());
		}
		if (!mQQEt.getText().toString().isEmpty() || 
				(cstmReal != null && cstmReal.getQq() != null)) {
			realJson.putOpt("qq", mQQEt.getText().toString());
		}
		if (!mCountryEt.getText().toString().isEmpty() || 
				(cstmReal != null && cstmReal.getCountry() != null)) {
			realJson.putOpt("country", mCountryEt.getText().toString());
		}
		if (!mCompanyEt.getText().toString().isEmpty() || 
				(cstmReal != null && cstmReal.getCompany() != null)) {
			realJson.putOpt("company", mCompanyEt.getText().toString());
		}
		if (!mProvinceEt.getText().toString().isEmpty() || 
				(cstmReal != null && cstmReal.getProvince() != null)) {
			realJson.putOpt("province", mProvinceEt.getText().toString());
		}
		if (!mCityEt.getText().toString().isEmpty() || 
				(cstmReal != null && cstmReal.getCity() != null)) {
			realJson.putOpt("city", mCityEt.getText().toString());
		}
		if (!mAddressEt.getText().toString().isEmpty() || 
				(cstmReal != null && cstmReal.getAddress() != null)) {
			realJson.putOpt("address", mAddressEt.getText().toString());
		}
		
		CustomerRequest cReq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, this);
		cReq.setCustomerInfo(realJson, c_id);
		cReq.getCustomerInfo(c_id);
		// 获得客户消息[通知界面]更新逻辑[eventbus]
		postEvent(mCustomer, EventTag.ETAG_UPDATE_CSTM_INFO);
	}
	
	@Override
	public boolean onTouchEvent(android.view.MotionEvent event) {
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		return imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.layout_history_msg:
			break;
		
		case R.id.layout_head:
			break;
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


	@Override
	protected void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
	}
}
