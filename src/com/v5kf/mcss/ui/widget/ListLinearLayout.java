package com.v5kf.mcss.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.v5kf.mcss.ui.adapter.LayoutListAdapter;
import com.v5kf.mcss.utils.Logger;

public class ListLinearLayout extends LinearLayout {
	
	private OnListLayoutClickListener mListener;
	
	public interface OnListLayoutClickListener {
		public void onListLayoutClick(View v, int pos);
	}
	
	public void setOnListLayoutClickListener(OnListLayoutClickListener l) {
		this.mListener = l;
	}

	public ListLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
	}

	/**
     * 绑定布局
     */
    public void bindLinearLayout(LayoutListAdapter adapter) {
        int count = adapter.getCount();
        Logger.d("ListLinearLayout", "LayoutListAdapter:" + adapter.getCount());
        this.removeAllViews();
        for (int i = 0; i < count; i++) {
        	if (i > 0) {
        		addView(adapter.getDivider(this));
        	}
            View v = adapter.getView(i, null, this);
            v.setTag(i);
			v.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (mListener != null) {
						mListener.onListLayoutClick(v, (int)v.getTag());
					}
				}
			});
            addView(v);
        }
    }
}
