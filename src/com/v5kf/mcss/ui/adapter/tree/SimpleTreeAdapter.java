package com.v5kf.mcss.ui.adapter.tree;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.v5kf.mcss.R;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.cache.ImageLoader;

public class SimpleTreeAdapter<T> extends TreeListViewAdapter<T> {

	public SimpleTreeAdapter(ListView mTree, Context context, List<T> datas,
			int defaultExpandLevel, int listMode) throws IllegalArgumentException,
			IllegalAccessException {
		super(mTree, context, datas, defaultExpandLevel, listMode);
	}

	@Override
	public View getConvertView(Node node , int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;    
        if (true) { // 必须全部更新，复用View会导致分组切换显示错乱
//        	if (convertView == null) {
            holder = new ViewHolder();
            if (node.getType().equals("worker")) {
	            convertView = mInflater.inflate(R.layout.item_contact, parent, false);  
	            holder.mPhotoIv = (ImageView) convertView.findViewById(R.id.id_item_photo);    
	            holder.mNameTv = (TextView) convertView.findViewById(R.id.id_item_name);  
	            holder.mRightTv = (TextView) convertView.findViewById(R.id.id_item_status_tv);  
	            holder.mDescIv = (ImageView) convertView.findViewById(R.id.id_item_status_img);
	            	            
            } else {
            	convertView = mInflater.inflate(R.layout.item_contact_section, parent, false);    
	            holder.mNameTv = (TextView) convertView.findViewById(R.id.id_section_name);    
	            holder.mRightTv = (TextView) convertView.findViewById(R.id.id_section_desc_tv);    
	            holder.mDescIv = (ImageView) convertView.findViewById(R.id.id_section_desc_img);
	                        
            }  
            convertView.setTag(holder);    
        } else {    
            holder = (ViewHolder) convertView.getTag();
        }
        
        if (node.isSelect()) {
        	convertView.setBackgroundResource(R.color.header_tips_bg_color_pressed);
        } else {
        	if (node.getType().equals("worker")) {
        		convertView.setBackgroundResource(R.drawable.session_item_bg_selector);
        	} else {
        		convertView.setBackgroundResource(R.color.arch_workers_section_bg);
        	}
        }
		
		if (node.getIcon() == -1) {
			holder.mDescIv.setVisibility(View.INVISIBLE);
		} else {
			holder.mDescIv.setVisibility(View.VISIBLE);
			holder.mDescIv.setImageResource(node.getIcon());
		}
		holder.mNameTv.setText(node.getName());
		
        if (node.getType().equals("worker") && node.getWorker() != null) {
//        	holder.mDescIv.setBackground(null);
        	ImageLoader imgLoader = new ImageLoader(mContext, true, R.drawable.v5_photo_default);
        	imgLoader.DisplayImage(node.getWorker().getPhoto(), holder.mPhotoIv);
        	UITools.setStatusTextInfo(node.getWorker().getStatus(), holder.mRightTv);
        }		
		
		return convertView;
	}

	class ViewHolder {
	    public TextView mNameTv;
	    public ImageView mPhotoIv;
	    public ImageView mDescIv;
	    public TextView mRightTv;
	}


	@Override
	public boolean isItemViewTypePinned(int viewType) {
		// TODO Auto-generated method stub
//		Logger.i("SimpleTreeAdapter", "[isItemViewTypePinned] ViewType:" + viewType);
		
		return viewType == Node.SECTION;
	}

}
