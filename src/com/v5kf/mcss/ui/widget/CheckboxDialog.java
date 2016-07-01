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

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.v5kf.mcss.R;
import com.v5kf.mcss.utils.Logger;

public class CheckboxDialog extends Dialog implements android.view.View.OnClickListener {

    public final static int MODE_ONE_BUTTON = 1;
    public final static int MODE_TWO_BUTTON = 2;
    public final static int MODE_THREE_BUTTON = 3;
    public final static int MODE_FOUR_BUTTON = 4;

    private RelativeLayout layout_check1, layout_check2, layout_check3, layout_check4;
	private ImageView iv_check1, iv_check2, iv_check3, iv_check4;
	private TextView tv_check1, tv_check2, tv_check3, tv_check4;
	private View view1, view2, view3;
	protected Button mRightButton;
    protected Button mLeftButton;
    
    private CheckboxDialogListener mListener;
    private boolean isChecked1, isChecked2, isChecked3, isChecked4;
    
    public interface CheckboxDialogListener {
        public void onClick(View view, boolean[] checks);
    }

    public CheckboxDialog(Context context) {
        super(context, R.style.custom_dialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_checkbox, null);
        setContentView(contentView);
        
        // 调整dialog背景大小
        int dialogWidth = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.80);
        int maxWidth = (int) context.getResources().getDimension(R.dimen.dialog_width);
		Logger.w("CustomProgressDialog", "width dimen:" + dialogWidth + context.getResources().getDisplayMetrics().density);
		if (dialogWidth > maxWidth) {
			dialogWidth = maxWidth;
		}
		contentView.setLayoutParams(new FrameLayout.LayoutParams(dialogWidth, LayoutParams.WRAP_CONTENT));
		
        findView();
        initDialog();
        
//		setCanceledOnTouchOutside(true);
        setCancelable(false);
    }

	private void findView() {
		layout_check1 = (RelativeLayout) findViewById(R.id.layout_check1);
		layout_check2 = (RelativeLayout) findViewById(R.id.layout_check2);
		layout_check3 = (RelativeLayout) findViewById(R.id.layout_check3);
		layout_check4 = (RelativeLayout) findViewById(R.id.layout_check4);
		iv_check1 = (ImageView) findViewById(R.id.iv_check1);
		iv_check2 = (ImageView) findViewById(R.id.iv_check2);
		iv_check3 = (ImageView) findViewById(R.id.iv_check3);
		iv_check4 = (ImageView) findViewById(R.id.iv_check4);
		tv_check1 = (TextView) findViewById(R.id.tv_check1);
		tv_check2 = (TextView) findViewById(R.id.tv_check2);
		tv_check3 = (TextView) findViewById(R.id.tv_check3);
		tv_check4 = (TextView) findViewById(R.id.tv_check4);
		
        view1 = (View) findViewById(R.id.view1);
        view2 = (View) findViewById(R.id.view2);
        view3 = (View) findViewById(R.id.view3);
        
        mRightButton = (Button) findViewById(R.id.btn_dialog_warning_right);
        mLeftButton = (Button) findViewById(R.id.btn_dialog_warning_left);
        
        layout_check1.setOnClickListener(this);
        layout_check2.setOnClickListener(this);
        layout_check3.setOnClickListener(this);
        layout_check4.setOnClickListener(this);
        mLeftButton.setOnClickListener(this);
        mRightButton.setOnClickListener(this);
    }
    
    public void initDialog() {
        initData();
        initView();
    }

    private void initView() {
		// TODO Auto-generated method stub
		iv_check1.setVisibility(View.VISIBLE);
		iv_check2.setVisibility(View.VISIBLE);
		iv_check3.setVisibility(View.VISIBLE);
		iv_check4.setVisibility(View.VISIBLE);
	}

	private void initData() {
		// TODO Auto-generated method stub
    	isChecked1 = true;
    	isChecked2 = true;
    	isChecked3 = true;
    	isChecked4 = true;
	}
	
	/* 设置默认勾选项，必须 */
	public void setDefaultChecked(boolean[] checks) {
		switch (checks.length) {
		case 1:
			isChecked1 = checks[0];
	    	if (isChecked1) {
	    		iv_check1.setVisibility(View.VISIBLE);
	    	} else {
	    		iv_check1.setVisibility(View.INVISIBLE);
	    	}
			break;
		case 2:
			isChecked1 = checks[0];
			isChecked2 = checks[1];
			if (isChecked1) {
	    		iv_check1.setVisibility(View.VISIBLE);
	    	} else {
	    		iv_check1.setVisibility(View.INVISIBLE);
	    	}
			if (isChecked2) {
				iv_check2.setVisibility(View.VISIBLE);
			} else {
				iv_check2.setVisibility(View.INVISIBLE);
			}
			break;
		case 3:
			isChecked1 = checks[0];
			isChecked2 = checks[1];
			isChecked3 = checks[2];
			if (isChecked1) {
	    		iv_check1.setVisibility(View.VISIBLE);
	    	} else {
	    		iv_check1.setVisibility(View.INVISIBLE);
	    	}
			if (isChecked2) {
				iv_check2.setVisibility(View.VISIBLE);
			} else {
				iv_check2.setVisibility(View.INVISIBLE);
			}
			if (isChecked3) {
				iv_check3.setVisibility(View.VISIBLE);
			} else {
				iv_check3.setVisibility(View.INVISIBLE);
			}
			break;
		case 4:
			isChecked1 = checks[0];
			isChecked2 = checks[1];
			isChecked3 = checks[2];
			isChecked4 = checks[3];
			if (isChecked1) {
	    		iv_check1.setVisibility(View.VISIBLE);
	    	} else {
	    		iv_check1.setVisibility(View.INVISIBLE);
	    	}
			if (isChecked2) {
				iv_check2.setVisibility(View.VISIBLE);
			} else {
				iv_check2.setVisibility(View.INVISIBLE);
			}
			if (isChecked3) {
				iv_check3.setVisibility(View.VISIBLE);
			} else {
				iv_check3.setVisibility(View.INVISIBLE);
			}
			if (isChecked4) {
				iv_check4.setVisibility(View.VISIBLE);
			} else {
				iv_check4.setVisibility(View.INVISIBLE);
			}
			break;
		}
	}
	
	/* 必须 */
	public void setOnClickListener(CheckboxDialogListener listener) {
        this.mListener = listener;
    }
    
	/* 默认为MODE_FOUR_BUTTON */
	public void setCheckMode(int mode) {
		switch (mode) {
		case MODE_ONE_BUTTON:
			layout_check1.setVisibility(View.VISIBLE);
			layout_check2.setVisibility(View.GONE);
			layout_check3.setVisibility(View.GONE);
			layout_check4.setVisibility(View.GONE);
			view1.setVisibility(View.GONE);
			view2.setVisibility(View.GONE);
			view3.setVisibility(View.GONE);
			break;
		case MODE_TWO_BUTTON:
			layout_check1.setVisibility(View.VISIBLE);
			layout_check2.setVisibility(View.VISIBLE);
			layout_check3.setVisibility(View.GONE);
			layout_check4.setVisibility(View.GONE);
			view1.setVisibility(View.VISIBLE);
			view2.setVisibility(View.GONE);
			view3.setVisibility(View.GONE);
			break;
		case MODE_THREE_BUTTON:
			layout_check1.setVisibility(View.VISIBLE);
			layout_check2.setVisibility(View.VISIBLE);
			layout_check3.setVisibility(View.VISIBLE);
			layout_check4.setVisibility(View.GONE);
			view1.setVisibility(View.VISIBLE);
			view2.setVisibility(View.VISIBLE);
			view3.setVisibility(View.GONE);
			break;
		case MODE_FOUR_BUTTON:
			layout_check1.setVisibility(View.VISIBLE);
			layout_check2.setVisibility(View.VISIBLE);
			layout_check3.setVisibility(View.VISIBLE);
			layout_check4.setVisibility(View.VISIBLE);
			view1.setVisibility(View.VISIBLE);
			view2.setVisibility(View.VISIBLE);
			view3.setVisibility(View.VISIBLE);
			break;
		}
	}
	
	/* 必须(默认为清空缓存标题) */
	public void setCheckTitle(String[] titles) {
		switch (titles.length) {
		case 1:
			tv_check1.setText(titles[0]);
			break;
		case 2:
			tv_check1.setText(titles[0]);
			tv_check2.setText(titles[1]);
			break;
		case 3:
			tv_check1.setText(titles[0]);
			tv_check2.setText(titles[1]);
			tv_check3.setText(titles[2]);
			break;
		case 4:
			tv_check1.setText(titles[0]);
			tv_check2.setText(titles[1]);
			tv_check3.setText(titles[2]);
			tv_check4.setText(titles[3]);
			break;
		}
	}

	/* 必须(默认为清空缓存标题) */
	public void setCheckTitle(int[] titles) {
		switch (titles.length) {
		case 1:
			tv_check1.setText(titles[0]);
			break;
		case 2:
			tv_check1.setText(titles[0]);
			tv_check2.setText(titles[1]);
			break;
		case 3:
			tv_check1.setText(titles[0]);
			tv_check2.setText(titles[1]);
			tv_check3.setText(titles[2]);
			break;
		case 4:
			tv_check1.setText(titles[0]);
			tv_check2.setText(titles[1]);
			tv_check3.setText(titles[2]);
			tv_check4.setText(titles[3]);
			break;
		}
	}
    
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.layout_check1:
			if (isChecked1) {
				iv_check1.setVisibility(View.INVISIBLE);
			} else {
				iv_check1.setVisibility(View.VISIBLE);
			}
			isChecked1 = !isChecked1;
			break;
		case R.id.layout_check2:
			if (isChecked2) {
				iv_check2.setVisibility(View.INVISIBLE);
			} else {
				iv_check2.setVisibility(View.VISIBLE);
			}
			isChecked2 = !isChecked2;
			break;
		case R.id.layout_check3:
			if (isChecked3) {
				iv_check3.setVisibility(View.INVISIBLE);
			} else {
				iv_check3.setVisibility(View.VISIBLE);
			}
			isChecked3 = !isChecked3;
			break;
		case R.id.layout_check4:
			if (isChecked4) {
				iv_check4.setVisibility(View.INVISIBLE);
			} else {
				iv_check4.setVisibility(View.VISIBLE);
			}
			isChecked4 = !isChecked4;
			break;			
		case R.id.btn_dialog_warning_left:
			this.dismiss();
			break;
		case R.id.btn_dialog_warning_right:
			this.dismiss();
			if (mListener != null) {
				boolean[] checks = new boolean[4];
				checks[0] = isChecked1;
				checks[1] = isChecked2;
				checks[2] = isChecked3;
				checks[3] = isChecked4;
	    		mListener.onClick(v, checks); // Modify
	    	}
			break;
		}
	}
}
