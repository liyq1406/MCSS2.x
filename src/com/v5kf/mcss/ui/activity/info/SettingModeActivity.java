package com.v5kf.mcss.ui.activity.info;

import org.json.JSONException;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.WorkerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.WorkerRequest;
import com.v5kf.mcss.ui.activity.BaseActivity;
import com.v5kf.mcss.ui.view.HeaderLayout.onLeftImageButtonClickListener;
import com.v5kf.mcss.ui.view.HeaderLayout.onRightImageButtonClickListener;
import com.v5kf.mcss.ui.widget.ModeSeekBar;
import com.v5kf.mcss.ui.widget.ModeSeekBar.OnProgressChangedListener;
import com.v5kf.mcss.utils.Logger;

public class SettingModeActivity extends BaseActivity implements OnClickListener {
	
	private RelativeLayout layout_switch_only, layout_auto_mode;

	private ImageView iv_switch_only, iv_auto_mode;
	
	private ModeSeekBar mSeekBar;
	private TextView mSeekTv;
	private EditText mSeekNumEt;
	
	private int mMode;
	private int mAutoModeNum;
	private int mSwitchModeNum;
	

	@Override
	public void onBackPressed() {
		super.onBackPressed();
//		finishModeSet();
	}
	
	/**
	 * 是否隐藏软键盘
	 * @param isShouldHideInput SettingModeActivity 
	 * @return boolean
	 * @param v
	 * @param event
	 * @return
	 */
	public  boolean isShouldHideInput(View v, MotionEvent event) {
		if (v != null && (v instanceof EditText)) {
			int[] leftTop = { 0, 0 };
			//获取输入框当前的location位置
			v.getLocationInWindow(leftTop);
			int left = leftTop[0];
			int top = leftTop[1];
			int bottom = top + v.getHeight();
			int right = left + v.getWidth();
			if (event.getX() > left && event.getX() < right
					&& event.getY() > top && event.getY() < bottom) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * 触摸操作判断是否点击输入框
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			Logger.i("SettingMode", "ACTION_DOWN");
			View v = getCurrentFocus();
			if (isShouldHideInput(v, ev)) {
				Logger.i("SettingMode", "isShouldHideInput");
				hideSoftInputView();
				mSeekNumEt.clearFocus();
			} else {
				Editable etext = mSeekNumEt.getText();
				mSeekNumEt.setSelection(etext.length());
			}
		}		
		return super.dispatchTouchEvent(ev);
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_mode);
		
		initData();
		findView();
		initView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	private void initData() {
		mMode = mAppInfo.getUser().getMode();
		mAutoModeNum = mAppInfo.getUser().getAccepts();
		mSwitchModeNum = mAppInfo.getUser().getConnects();
	}

	private void findView() {
		layout_switch_only = (RelativeLayout) findViewById(R.id.layout_switch_only);
		layout_auto_mode = (RelativeLayout) findViewById(R.id.layout_auto_mode);
		iv_switch_only = (ImageView) findViewById(R.id.id_switch_only_iv);
		iv_auto_mode = (ImageView) findViewById(R.id.id_auto_mode_iv);
		
		mSeekBar = (ModeSeekBar) findViewById(R.id.id_seek_bar);
		mSeekTv = (TextView) findViewById(R.id.id_seek_tv);
		mSeekNumEt = (EditText) findViewById(R.id.id_seek_num_et);
	}

	private void initView() {
		initTitleBar();		

		mSeekBar.setFocusable(true);
		mSeekBar.setFocusableInTouchMode(true);
		mSeekNumEt.clearFocus();
		mSeekNumEt.setOnTouchListener(new OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// 强制显示软键盘  
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                boolean bool = imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);  
                if(bool){
                	Logger.i("TestSettingMode", "弹出键盘s");
                	Editable etext = mSeekNumEt.getText();
                	mSeekNumEt.setSelection(etext.length());
                }
                v.performClick();
				return false;
			}
		});
		mSeekNumEt.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().isEmpty()) {
					mSeekBar.setSeekBarMode(0);
					return;
				}
				mSeekNumEt.setSelection(s.length());
				
				switch (mMode) {
				case QAODefine.MODE_AUTO:
					mAutoModeNum = Integer.parseInt(s.toString());
					mSeekBar.setSeekBarMode(mAutoModeNum);
					break;
					
				case QAODefine.MODE_SWITCH_ONLY:
					mSwitchModeNum = Integer.parseInt(s.toString());
					mSeekBar.setSeekBarMode(mSwitchModeNum);
					break;
				}
			}
		});
		
		layout_switch_only.setOnClickListener(this);
		layout_auto_mode.setOnClickListener(this);
		
		resetImgView();
		Logger.w("Test-set mode activity", "[init] mode:" + mMode + " connects:" + mSwitchModeNum + " accepts:" + mAutoModeNum);
		switch (mMode) {
		case QAODefine.MODE_AUTO:
			iv_auto_mode.setVisibility(View.VISIBLE);
			mSeekBar.setSeekBarMode(mAutoModeNum);
			setSeekTvText(mMode, mAutoModeNum);
			break;
			
		case QAODefine.MODE_SWITCH_ONLY:
			iv_switch_only.setVisibility(View.VISIBLE);
			mSeekBar.setSeekBarMode(mSwitchModeNum);
			setSeekTvText(mMode, mSwitchModeNum);
			break;

		}
		
		mSeekBar.setOnProgressChangedListener(new OnProgressChangedListener() {			
			@Override
			public void onProgressChanged(int mode, int progress) {
				setSeekTvText(mMode, mode);
				if (mMode == QAODefine.MODE_AUTO) {
					mAutoModeNum = mode;
				} else if (mMode == QAODefine.MODE_SWITCH_ONLY) {
					mSwitchModeNum = mode;
				}
			}
		});
	}
	
	
	private void initTitleBar() {
		initTopBarForLeftImageAndRightText(
				R.string.set_mode, 
				R.drawable.base_action_bar_back_bg_selector, 
				R.string.btn_confirm, 
				new onLeftImageButtonClickListener() {
					@Override
					public void onClick(View v) {
//						finishModeSet();
						finishActivity();
					}
				},
				new onRightImageButtonClickListener() {					
					@Override
					public void onClick(View arg0) {
						finishModeSet();
					}
				});
	}


	@Override
	public void onClick(View v) {
		resetImgView();
		switch (v.getId()) {
		case R.id.layout_switch_only:
			iv_switch_only.setVisibility(View.VISIBLE);
			mMode = QAODefine.MODE_SWITCH_ONLY;
			mSeekBar.setSeekBarMode(mSwitchModeNum);
			break;
			
		case R.id.layout_auto_mode:
			iv_auto_mode.setVisibility(View.VISIBLE);
			mMode = QAODefine.MODE_AUTO;
			mSeekBar.setSeekBarMode(mAutoModeNum);
			break;
		}
	}
	
	private void setSeekTvText(int type, int mode) {
		mSeekNumEt.setText(Math.abs(mode) + "");
		if (type == 0) {
			mSeekTv.setText(getString(R.string.seek_bar_mode_switch_tip));
		} else if (type == 1) {
			mSeekTv.setText(getString(R.string.seek_bar_mode_tip));
		}
	}
	
	private void resetImgView() {
		iv_switch_only.setVisibility(View.INVISIBLE);
		iv_auto_mode.setVisibility(View.INVISIBLE);
	}
	
	private void finishModeSet() {
		WorkerBean mUser = mAppInfo.getUser();
		if (mMode == mUser.getMode() && 
				mAutoModeNum == mUser.getAccepts() &&
				mSwitchModeNum == mUser.getConnects()) {
			// 未发生改变
		} else {		
			/* 发送模式设置请求 */
			try {
				WorkerRequest wReq = (WorkerRequest) RequestManager.getRequest(
						QAODefine.O_TYPE_WWRKR, 
						this);
				wReq.setWorkerMode(mMode, mSeekBar.getSeekBarMode());
				wReq.getWorkerMode();
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
