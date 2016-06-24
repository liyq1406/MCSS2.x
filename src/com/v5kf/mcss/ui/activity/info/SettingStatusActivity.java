package com.v5kf.mcss.ui.activity.info;

import org.json.JSONException;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.WorkerRequest;
import com.v5kf.mcss.ui.view.HeaderLayout.onLeftImageButtonClickListener;
import com.v5kf.mcss.ui.view.HeaderLayout.onRightImageButtonClickListener;

public class SettingStatusActivity extends BaseActivity implements OnClickListener {
	
	private RelativeLayout layout_online, layout_leave, layout_hide;

	private ImageView iv_online, iv_leave, iv_hide;
	
	private short mStatus;

	@Override
	public void onBackPressed() {
		super.onBackPressed();
//		finishStatusSet();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_status);
		
		findView();
		initView();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void findView() {
		layout_online = (RelativeLayout) findViewById(R.id.layout_status_online);
		layout_leave = (RelativeLayout) findViewById(R.id.layout_status_leave);
		layout_hide = (RelativeLayout) findViewById(R.id.layout_status_hide);
		iv_online = (ImageView) findViewById(R.id.id_status_online_iv);
		iv_leave = (ImageView) findViewById(R.id.id_status_leave_iv);
		iv_hide = (ImageView) findViewById(R.id.id_status_hide_iv);
	}

	private void initView() {
		initTitleBar();
		
		layout_online.setOnClickListener(this);
		layout_leave.setOnClickListener(this);
		layout_hide.setOnClickListener(this);
		
		resetImgView();
		mStatus = mAppInfo.getUser().getStatus();
		switch (mStatus) {
		case QAODefine.STATUS_ONLINE:
			iv_online.setVisibility(View.VISIBLE);
			break;
			
		case QAODefine.STATUS_LEAVE:
			iv_leave.setVisibility(View.VISIBLE);
			break;

		case QAODefine.STATUS_BUSY:
			
			break;

		case QAODefine.STATUS_HIDE:
			iv_hide.setVisibility(View.VISIBLE);
			break;
		}
	}
	
	
	private void initTitleBar() {
		initTopBarForLeftImageAndRightText(
				R.string.set_status, 
				R.drawable.base_action_bar_back_bg_selector, 
				R.string.btn_confirm, 
				new onLeftImageButtonClickListener() {
					@Override
					public void onClick(View v) {
//						finishStatusSet();
						finishActivity();
					}
				},
				new onRightImageButtonClickListener() {					
					@Override
					public void onClick(View arg0) {
						finishStatusSet();
					}
				});
	}

	@Override
	public void onClick(View v) {
		resetImgView();
		switch (v.getId()) {
		case R.id.layout_status_online:
			iv_online.setVisibility(View.VISIBLE);
			mStatus = QAODefine.STATUS_ONLINE;
			break;
			
		case R.id.layout_status_leave:
			iv_leave.setVisibility(View.VISIBLE);
			mStatus = QAODefine.STATUS_LEAVE;
			break;
			
		case R.id.layout_status_hide:
			iv_hide.setVisibility(View.VISIBLE);
			mStatus = QAODefine.STATUS_HIDE;
			break;			
		}
	}
	
	private void resetImgView() {
		iv_online.setVisibility(View.INVISIBLE);
		iv_leave.setVisibility(View.INVISIBLE);
		iv_hide.setVisibility(View.INVISIBLE);
	}
	
	private void finishStatusSet() {
		if (mStatus != mAppInfo.getUser().getStatus()) {		
			try {
				WorkerRequest workerRequest = (WorkerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WWRKR, this);
				workerRequest.setWorkerStatus(mStatus);
				mAppInfo.getUser().setStatus(mStatus);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		finishActivity();
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
}
