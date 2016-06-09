package com.v5kf.mcss.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * @author Chenhy	
 * @email chenhy@v5kf.com
 * @version v1.0 2015-8-4 下午3:05:16
 * @package com.v5kf.mcss.ui.adapter of MCSS-Native
 * @file OnChatRecyclerAdapter.java 
 *
 */
public abstract class LayoutListAdapter extends BaseAdapter {

//	private static final String TAG = "LayoutListAdapter";
	
    public abstract View getDivider(ViewGroup parent);
}
