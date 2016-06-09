package com.v5kf.client.ui;

import android.view.View;

public interface OnRecyclerClickListener {
	public void onMultiNewsClick(View v, int position, int viewType, int newsPos);
	public void onItemClick(View v, int position, int viewType);
	public void onItemLongClick(View v, int position, int viewType);
}