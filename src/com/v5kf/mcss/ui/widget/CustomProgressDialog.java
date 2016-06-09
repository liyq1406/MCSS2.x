/*
 * Copyright (C), 2013, TP-LINK TECHNOLOGIES CO., LTD.
 *
 * CustomProgressDialog.java
 *
 * Description
 *
 * Author YuLibo
 *
 * Ver 1.0, 2013-12-16, YuLibo, Create file
 */

package com.v5kf.mcss.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.v5kf.mcss.R;


public class CustomProgressDialog extends Dialog {
	
	private Context mContext;
    private static CustomProgressDialog customProgressDialog = null;

    private CustomProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public static CustomProgressDialog createDialog(Context context) {
        return createDialog(context, false);
    }

    public static CustomProgressDialog createDialog(Context context, boolean hasTextView) {
    	customProgressDialog = new CustomProgressDialog(context, R.style.custom_progress_dialog);
    	customProgressDialog.mContext = context;
    	
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_process, null);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        if (hasTextView) {
            params.width = (int)context.getResources().getDimension(R.dimen.dialog_width);
        } else {
            contentView = LayoutInflater.from(context).inflate(R.layout.dialog_process_without_text, null);
        }
        customProgressDialog.setContentView(contentView, params);
        customProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        return customProgressDialog;
    }

    @Override
    public void show() {
//        ImageView imageView = (ImageView)customProgressDialog.findViewById(R.id.iv_process_dialog_img);
//        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.round_loading);
//        imageView.startAnimation(animation);
        super.show();
    }

    public CustomProgressDialog setTitile(String strTitle) {
        return customProgressDialog;
    }

    public CustomProgressDialog setMessage(String strMessage) {
        TextView tvMsg = (TextView)customProgressDialog.findViewById(R.id.tv_process_dialog_msg);

        if (tvMsg != null) {
            tvMsg.setText(strMessage);
        }

        return customProgressDialog;
    }

    public CustomProgressDialog setMessage(int resid) {
        TextView tvMsg = (TextView)customProgressDialog.findViewById(R.id.tv_process_dialog_msg);

        if (tvMsg != null) {
            tvMsg.setText(resid);
        }

        return customProgressDialog;
    }
}
