/*
 * Copyright (C), 2014, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * WarningDialog.java
 *
 * Author YuLibo
 *
 * Ver 1.0, 2014-10-27, YuLibo, Create file
 */

package com.v5kf.mcss.ui.widget;

import org.json.JSONException;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.WorkerBean;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.WorkerRequest;
import com.v5kf.mcss.ui.widget.ModeSeekBar.OnProgressChangedListener;
import com.v5kf.mcss.utils.Logger;

public class ModeSeekbarDialog extends Dialog implements android.view.View.OnClickListener {

	public final static int DISPLAY_TYPE_SESSION = 1;
	public final static int DISPLAY_TYPE_CHAT = 2;
	
    public final static int MODE_TWO_BUTTON = 2;
    public final static int MODE_THREE_BUTTON = 3;
    public final static int MODE_FOUR_BUTTON = 4;

    private RelativeLayout layout_switch_only, layout_auto_mode;

	private ImageView iv_switch_only, iv_auto_mode;

    protected ModeSeekBar mSeekBar;
    protected TextView mSeekTv;
    
    private WorkerBean mUser;
	private int mMode;
	private int mAutoModeNum;
	private int mSwitchModeNum;
    

    public ModeSeekbarDialog(Context context, WorkerBean user) {
        super(context, R.style.custom_dialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_seekbar, null);
        setContentView(contentView);
        
        this.mUser = user;

        findView();
        initDialog();
        
        // 调整dialog背景大小
        int dialogWidth = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.80);
        int maxWidth = (int) context.getResources().getDimension(R.dimen.dialog_width);
		Logger.w("CustomProgressDialog", "width dimen:" + dialogWidth + context.getResources().getDisplayMetrics().density);
		if (dialogWidth > maxWidth) {
			dialogWidth = maxWidth;
		}
		contentView.setLayoutParams(new FrameLayout.LayoutParams(dialogWidth, LayoutParams.WRAP_CONTENT));
        
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }
    
    public void initDialog() {
        initData();
        initSeekBar();
    }

    private void initData() {
		// TODO Auto-generated method stub
    	mMode = mUser.getMode();
		mAutoModeNum = mUser.getAccepts();
		mSwitchModeNum = mUser.getConnects();
	}

	private void findView() {
		layout_switch_only = (RelativeLayout) findViewById(R.id.layout_switch_only);
		layout_auto_mode = (RelativeLayout) findViewById(R.id.layout_auto_mode);
		iv_switch_only = (ImageView) findViewById(R.id.id_switch_only_iv);
		iv_auto_mode = (ImageView) findViewById(R.id.id_auto_mode_iv);
		
        mSeekBar = (ModeSeekBar) findViewById(R.id.id_seek_bar);
        mSeekTv = (TextView) findViewById(R.id.id_seek_tv);
        
        layout_switch_only.setOnClickListener(this);
		layout_auto_mode.setOnClickListener(this);
    }
    
    private void initSeekBar() {
		mSeekBar.setOnProgressChangedListener(new OnProgressChangedListener() {			
			@Override
			public void onProgressChanged(int mode, int progress) {
				// TODO Auto-generated method stub				
				setSeekTvText(mMode, mode);
				if (mMode == QAODefine.MODE_AUTO) {
					mAutoModeNum = mode;
				} else if (mMode == QAODefine.MODE_SWITCH_ONLY) {
					mSwitchModeNum = mode;
				}
			}
		});
		
		this.setOnDismissListener(new OnDismissListener() {			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				if (mMode == mUser.getMode() && 
						mAutoModeNum == mUser.getAccepts() &&
						mSwitchModeNum == mUser.getConnects()) {
					// 未发生改变
					return;
				}
				
				/* 发送模式设置请求 */
				try {
					WorkerRequest wReq = (WorkerRequest) RequestManager.getRequest(
							QAODefine.O_TYPE_WWRKR, 
							getContext());
					wReq.setWorkerMode(mMode, getSeekBarMode());
					wReq.getWorkerMode();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		resetImgView();
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
		
    }

	/**
     * @param getSeekBar SeekbarDialog 
     * @return ModeSeekBar extends SeekBar
     * @return
     */
    public ModeSeekBar getSeekBar() {
    	return mSeekBar;
    }
    
    /**
     * @param getSeekTextView SeekbarDialog 
     * @return TextView
     * @return
     */
    public TextView getSeekTextView() {
    	return mSeekTv;
    }
    
    public int getSeekBarMode() {
    	return mSeekBar.getSeekBarMode();
    }
       
    private void setSeekTvText(int type, int mode) {
		if (type == 0) {
			mSeekTv.setText(getContext().getString(R.string.seek_bar_mode_switch_tip) + Math.abs(mode));
		} else if (type == 1) {
			mSeekTv.setText(getContext().getString(R.string.seek_bar_mode_tip) + Math.abs(mode));
		}
	}
	
	private void resetImgView() {
		iv_switch_only.setVisibility(View.INVISIBLE);
		iv_auto_mode.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
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
}
