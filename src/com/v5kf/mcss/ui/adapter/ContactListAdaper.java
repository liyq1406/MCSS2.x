package com.v5kf.mcss.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.ui.entity.ContactRecyclerBean;
import com.v5kf.mcss.ui.widget.PinnedSectionListView.PinnedSectionListAdapter;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.cache.ImageLoader;

public class ContactListAdaper extends BaseAdapter implements PinnedSectionListAdapter {
	
	private Context mContext;
	private LayoutInflater mInflater;
	
	private List<ContactRecyclerBean> mDatas;
	
	public ContactListAdaper(Context context, List<ContactRecyclerBean> datas) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.mDatas = datas;
		this.mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDatas.size();
	}
	
	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}
	
	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return mDatas.get(position).getType();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public boolean isItemViewTypePinned(int viewType) {
		// TODO Auto-generated method stub
		return viewType == ContactRecyclerBean.SECTION;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;    
        if (convertView == null) {    
            holder = new ViewHolder();
            if (getItemViewType(position) == ContactRecyclerBean.ITEM) {
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
        
        ContactRecyclerBean recyclerBean = mDatas.get(position);
        if (recyclerBean.getType() == ContactRecyclerBean.ITEM) {
        	holder.mNameTv.setText(recyclerBean.getWorker().getName());
//        	holder.mPhotoIv.setBackground(null);
//        	holder.mDescIv.setBackground(null);
        	ImageLoader imgLoader = new ImageLoader(mContext, true, R.drawable.v5_photo_default, null);
        	imgLoader.DisplayImage(recyclerBean.getWorker().getPhoto(), holder.mPhotoIv);
        	switch (recyclerBean.getWorker().getStatus()) {
        	case QAODefine.STATUS_ONLINE:
        		holder.mRightTv.setText(R.string.status_online);
        		holder.mRightTv.setTextColor(UITools.getColor(mContext, R.color.status_online_color));
        		break;
        	case QAODefine.STATUS_BUSY:
        		holder.mRightTv.setText(R.string.status_busy);
        		holder.mRightTv.setTextColor(UITools.getColor(mContext, R.color.status_busy_color));
        		break;
        	case QAODefine.STATUS_LEAVE:
        		holder.mRightTv.setText(R.string.status_leave);
        		holder.mRightTv.setTextColor(UITools.getColor(mContext, R.color.status_leave_color));
        		break;
        	case QAODefine.STATUS_OFFLINE:
        		holder.mRightTv.setText(R.string.status_offline);
        		holder.mRightTv.setTextColor(UITools.getColor(mContext, R.color.status_offline_color));
        		break;
        	}
        	
        } else {
        	holder.mNameTv.setText(recyclerBean.getPath());
//        	holder.mDescIv.setBackground(null);
        }
		
		return convertView;
	}
	
	
	class ViewHolder {
	    public TextView mNameTv;
	    public ImageView mPhotoIv;
	    public ImageView mDescIv;
	    public TextView mRightTv;
	}
	
}
