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
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;

import com.v5kf.mcss.R;
import com.v5kf.mcss.utils.Logger;

public class CustomOptionDialog extends Dialog implements OnClickListener {

	public final static int DISPLAY_TYPE_SERVING = 1; // 进行中的会话列表
	public final static int DISPLAY_TYPE_WAITING = 2; // 等待中客户列表
	public final static int DISPLAY_TYPE_CHAT_TWO_BTN = 3;	// 对话内容列表
	public final static int DISPLAY_TYPE_CHAT_ONE_BTN = 4;	// 对话内容列表
	public final static int DISPLAY_TYPE_SHOW_IMAGE_ONE_BTN = 5;	// 图片查看
	public final static int DISPLAY_TYPE_SELECT_IMAGE_TWO_BTN = 6;	// 图片or拍照选项
	
    public final static int MODE_ONE_BUTTON = 1;
    public final static int MODE_TWO_BUTTON = 2;
    public final static int MODE_THREE_BUTTON = 3;
    public final static int MODE_FOUR_BUTTON = 4;
    public final static int MODE_FIVE_BUTTON = 5;

    public interface OptionDialogListener {
        public void onClick(View view);
        public void onDismiss(DialogInterface dialog);
    }

    private OptionDialogListener mListener;

    protected Button mOnlyButton;
    protected Button mTopButton;
    protected Button mBottomButton;
    protected Button mMiddleButton1;
    protected Button mMiddleButton2;
    protected Button mMiddleButton3;

    public CustomOptionDialog(Context context) {
        super(context, R.style.custom_dialog);
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_option, null);
        setContentView(contentView);
        findView();
        
        // 调整dialog背景大小
        int dialogWidth = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.80);
        int maxWidth = (int) context.getResources().getDimension(R.dimen.dialog_width);
		Logger.w("CustomProgressDialog", "width dimen:" + dialogWidth + context.getResources().getDisplayMetrics().density);
		if (dialogWidth > maxWidth) {
			dialogWidth = maxWidth;
		}
		contentView.setLayoutParams(new FrameLayout.LayoutParams(dialogWidth, LayoutParams.WRAP_CONTENT));
		
        setOnClickListener();
        setCanceledOnTouchOutside(true);
        setCancelable(true);
        
        setOnDismissListener(new OnDismissListener() {			
			@Override
			public void onDismiss(DialogInterface dialog) {
				mListener.onDismiss(dialog);
			}
		});
    }

    private void findView() {
    	mOnlyButton = (Button) findViewById(R.id.btn_dialog_only_option);
        mTopButton = (Button) findViewById(R.id.btn_dialog_top_option);
        mBottomButton = (Button) findViewById(R.id.btn_dialog_bottom_option);
        mMiddleButton1 = (Button) findViewById(R.id.btn_dialog_middle_option1);
        mMiddleButton2 = (Button) findViewById(R.id.btn_dialog_middle_option2);
        mMiddleButton3 = (Button) findViewById(R.id.btn_dialog_middle_option3);
        mTopButton.setTag("top");
        mBottomButton.setTag("bottom");
        mMiddleButton1.setTag("middle1");
        mMiddleButton2.setTag("middle2");
        mMiddleButton3.setTag("middle3");
    }

    /**
     * @param getMidButtonFirst CustomOptionDialog 
     * @return Button
     * @return
     */
    public Button getMidButtonFirst() {
    	return mMiddleButton1;
    }
    
    /**
     * @param getMidButtonSecond CustomOptionDialog 
     * @return Button
     * @return
     */
    public Button getMidButtonSecond() {
    	return mMiddleButton2;
    }
    /**
     * @param getMidButtonThird CustomOptionDialog 
     * @return Button
     * @return
     */
    public Button getMidButtonThird() {
    	return mMiddleButton3;
    }

    public Button getTopButton() {
        return mTopButton;
    }

    public Button getBottomButton() {
        return mBottomButton;
    }

    public Button getOnlyButton() {
    	return mOnlyButton;
    }

    public void setOnClickListener(OptionDialogListener listener) {
        this.mListener = listener;
    }

    private void setOnClickListener() {
    	mOnlyButton.setOnClickListener(this);
    	mTopButton.setOnClickListener(this);
    	mBottomButton.setOnClickListener(this);
        mMiddleButton1.setOnClickListener(this);
        mMiddleButton2.setOnClickListener(this);
        mMiddleButton3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
    	this.dismiss();
    	
    	if (mListener != null) {
    		mListener.onClick(v); // Modify
    	}
    }

    private void setDialogMode(int mode) {
    	findViewById(R.id.layout_dialog_top_button).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_dialog_bottom_button).setVisibility(View.VISIBLE);
        if (mode == MODE_ONE_BUTTON) { // 1选项
        	findViewById(R.id.layout_dialog_only_button).setVisibility(View.VISIBLE);
        	findViewById(R.id.layout_dialog_top_button).setVisibility(View.GONE);
        	findViewById(R.id.layout_dialog_middle_button1).setVisibility(View.GONE);
        	findViewById(R.id.layout_dialog_middle_button2).setVisibility(View.GONE);
        	findViewById(R.id.layout_dialog_middle_button3).setVisibility(View.GONE);
        	findViewById(R.id.layout_dialog_bottom_button).setVisibility(View.GONE);
        	findViewById(R.id.v_middle_divider0).setVisibility(View.GONE);
        	findViewById(R.id.v_middle_divider1).setVisibility(View.GONE);
        	findViewById(R.id.v_middle_divider2).setVisibility(View.GONE);
        	findViewById(R.id.v_middle_divider3).setVisibility(View.GONE);
        } else if (mode == MODE_TWO_BUTTON) { // 2选项
            findViewById(R.id.layout_dialog_middle_button1).setVisibility(View.GONE);
            findViewById(R.id.layout_dialog_middle_button2).setVisibility(View.GONE);
            findViewById(R.id.layout_dialog_middle_button3).setVisibility(View.GONE);
            findViewById(R.id.v_middle_divider1).setVisibility(View.GONE);
            findViewById(R.id.v_middle_divider2).setVisibility(View.GONE);
        } else if (mode == MODE_THREE_BUTTON) {	// 3选项
            findViewById(R.id.layout_dialog_middle_button1).setVisibility(View.VISIBLE);
            findViewById(R.id.layout_dialog_middle_button2).setVisibility(View.GONE);
            findViewById(R.id.layout_dialog_middle_button3).setVisibility(View.GONE);
            findViewById(R.id.v_middle_divider1).setVisibility(View.GONE);
            findViewById(R.id.v_middle_divider2).setVisibility(View.GONE);
        } else if (mode == MODE_FOUR_BUTTON) { // 4选项
        	findViewById(R.id.layout_dialog_middle_button1).setVisibility(View.VISIBLE);
        	findViewById(R.id.layout_dialog_middle_button2).setVisibility(View.VISIBLE);
        	findViewById(R.id.layout_dialog_middle_button3).setVisibility(View.GONE);
        	findViewById(R.id.v_middle_divider1).setVisibility(View.VISIBLE);
        } else { // 5选项
        	findViewById(R.id.layout_dialog_middle_button1).setVisibility(View.VISIBLE);
        	findViewById(R.id.layout_dialog_middle_button2).setVisibility(View.VISIBLE);
        	findViewById(R.id.layout_dialog_middle_button3).setVisibility(View.VISIBLE);
        	findViewById(R.id.v_middle_divider1).setVisibility(View.VISIBLE);
        	findViewById(R.id.v_middle_divider2).setVisibility(View.VISIBLE);
        }
    }
    
    public void setDisplayType(int type) {
    	findViewById(R.id.layout_dialog_top_button).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_dialog_bottom_button).setVisibility(View.VISIBLE);
    	if (type == DISPLAY_TYPE_SERVING) { // 服务中客户5个选项 -> [修改]三个选项
//    		setDialogMode(CustomOptionDialog.MODE_FIVE_BUTTON);
//			  mTopButton.setText(R.string.option_mark_as_unread);
//            mMiddleButton1.setText(R.string.option_stick_session);
//            mMiddleButton2.setText(R.string.option_set_trust);
//            mMiddleButton3.setText(R.string.option_switch);
//            mBottomButton.setText(R.string.option_end_session);
    		setDialogMode(CustomOptionDialog.MODE_THREE_BUTTON);
            mTopButton.setText(R.string.option_set_trust);
            mMiddleButton1.setText(R.string.option_switch);
            mBottomButton.setText(R.string.option_end_session);
    	} else if (type == DISPLAY_TYPE_CHAT_TWO_BTN) { // 对话中 三个选项
    		setDialogMode(CustomOptionDialog.MODE_TWO_BUTTON);
    		mTopButton.setText(R.string.option_copy_content);
    		mBottomButton.setText(R.string.option_robot_answer);
    	} else if (type == DISPLAY_TYPE_CHAT_ONE_BTN) { // 对话中 一个选项
    		setDialogMode(CustomOptionDialog.MODE_ONE_BUTTON);
    		mOnlyButton.setText(R.string.option_forward_msg);
    	} else if (type == DISPLAY_TYPE_WAITING) { // 等待客户 三个选项 -> [修改]一个选项
//    		setDialogMode(CustomOptionDialog.MODE_THREE_BUTTON);
//    		mTopButton.setText(R.string.option_mark_as_read);
//            mMiddleButton1.setText(R.string.option_stick_session);
//            mBottomButton.setText(R.string.option_pickup);
            setDialogMode(CustomOptionDialog.MODE_ONE_BUTTON);
    		mOnlyButton.setText(R.string.option_pickup);
    	} else if (type == DISPLAY_TYPE_SHOW_IMAGE_ONE_BTN) { // 查看大图
    		setDialogMode(CustomOptionDialog.MODE_ONE_BUTTON);
    		mOnlyButton.setText(R.string.option_save_image);
    	} else if (type == DISPLAY_TYPE_SELECT_IMAGE_TWO_BTN) {
    		setDialogMode(CustomOptionDialog.MODE_TWO_BUTTON);
    		mTopButton.setText(R.string.picture);
    		mBottomButton.setText(R.string.camera);
    	}
    }

    /**
     * ServingSession专用
     * @param setDisplayType CustomOptionDialog 
     * @return void
     * @param unreadNum
     * @param inTrust
     */
	public void setDisplayTypeServing(boolean inTrust) {
		setDisplayType(DISPLAY_TYPE_SERVING);
		
		if (inTrust) {
			mTopButton.setText(R.string.option_cancel_trust);
		} else {
			mTopButton.setText(R.string.option_set_trust);
		}
	}
	
	public void setDisplayTypeWaiting() {
		setDisplayType(DISPLAY_TYPE_WAITING);
//		if (unreadNum > 0) {
//			mTopButton.setText(R.string.option_mark_as_read);
//		} else {
//			mTopButton.setText(R.string.option_mark_as_unread);
//		}
	}
}
