package com.v5kf.mcss.ui.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.v5kf.mcss.R;
import com.v5kf.mcss.ui.activity.ActivityBase;

/**
 * @author Chenhy	
 * @email chenhy@v5kf.com
 * @version v1.0 2015-8-4 下午3:05:16
 * @package com.v5kf.mcss.ui.adapter of MCSS-Native
 * @file OnChatRecyclerAdapter.java 
 *
 */
public class CustomerInfoListAdapter extends LayoutListAdapter {

//	private static final String TAG = "NewsListAdapter";
	private static final int VIEW_TYPE_CELL = 1;
	private LayoutInflater mInflater;
	private List<HashMap<String, String>> mDatas;
	@SuppressWarnings("unused")
	private ActivityBase mActivity;
	
    public CustomerInfoListAdapter(ActivityBase activity, List<HashMap<String, String>> datas) {
        super();
        this.mDatas = datas;
        this.mActivity = activity;
        mInflater = LayoutInflater.from(activity);
    }
    
	@Override
	public int getCount() {
		return mDatas.size();
	}


	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}
	
	
	@Override
	public int getViewTypeCount() {
		return 1;
	}


    @Override
    public int getItemViewType(int position) {
    	return VIEW_TYPE_CELL;
    }
	
    
    @Override
    public long getItemId(int position) {
    	return position;
    }
    
    
    public View getDivider(ViewGroup parent) {
    	View div = mInflater.inflate(R.layout.item_cstm_info_divider, parent, false);
    	return div;
    }
    

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;					   
        if (convertView == null) {
        	convertView = mInflater.inflate(R.layout.item_cstm_info_cell, parent, false);
			holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();    
        }
        
        Map<String, String> item = mDatas.get(position);
        String name = item.keySet().iterator().next();
        String value = item.get(name);
    	holder.mNameTv.setText(name);
    	holder.mValueTv.setText(value);
        		
		return convertView;
	}
	
    
    class ViewHolder {
    	public TextView mNameTv;
    	public TextView mValueTv;
    	
    	public ViewHolder(View itemView) {
    		mNameTv = (TextView) itemView.findViewById(R.id.id_info_name);
    		mValueTv = (TextView) itemView.findViewById(R.id.id_info_value);
		}

    }

}
