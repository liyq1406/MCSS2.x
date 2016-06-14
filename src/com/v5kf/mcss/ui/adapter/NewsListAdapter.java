package com.v5kf.mcss.ui.adapter;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.v5kf.client.lib.entity.V5ArticleBean;
import com.v5kf.mcss.R;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.utils.cache.ImageLoader;

/**
 * @author Chenhy	
 * @email chenhy@v5kf.com
 * @version v1.0 2015-8-4 下午3:05:16
 * @package com.v5kf.mcss.ui.adapter of MCSS-Native
 * @file OnChatRecyclerAdapter.java 
 *
 */
public class NewsListAdapter extends LayoutListAdapter {

//	private static final String TAG = "NewsListAdapter";
	private static final int VIEW_TYPE_HEAD = 1;
	private static final int VIEW_TYPE_OTHER = 2;
	private LayoutInflater mInflater;
	private List<V5ArticleBean> mDatas;
	private ActivityBase mActivity;
	private boolean mFlagChat;
	
    public NewsListAdapter(ActivityBase activity, List<V5ArticleBean> articles, boolean flagChat) {
        super();
        this.mDatas = articles;
        this.mActivity = activity;
        this.mFlagChat = flagChat;
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
		return 2;
	}


    @Override
    public int getItemViewType(int position) {
    	if (0 == position) {
			return VIEW_TYPE_HEAD;
		} else {
			return VIEW_TYPE_OTHER;
		}
    }	
	
    
    @Override
    public long getItemId(int position) {
    	return position;
    }
    
    
    public View getDivider(ViewGroup parent) {
    	View div = mInflater.inflate(R.layout.item_divider, parent, false);
    	return div;
    }
    

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;					   
        if (convertView == null) {
        	if (getItemViewType(position) == VIEW_TYPE_HEAD) {
        		if (mFlagChat) {
	        		convertView = mInflater.inflate(R.layout.item_chat_news_head, parent, false);
	    			holder = new ViewHolder(convertView);
        		} else {
        			convertView = mInflater.inflate(R.layout.item_robot_news_head, parent, false);
	    			holder = new ViewHolder(convertView);
        		}
    		} else {
    			if (mFlagChat) {
    				convertView = mInflater.inflate(R.layout.item_chat_news_item, parent, false);
    				holder = new ViewHolder(convertView);
    			} else {
    				convertView = mInflater.inflate(R.layout.item_robot_news_item, parent, false);
    				holder = new ViewHolder(convertView);
    			}
    		}
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();    
        }
        
        V5ArticleBean article = mDatas.get(position);    
    	holder.mTitleTv.setText(article.getTitle());
    	ImageLoader imgLoader = new ImageLoader(mActivity, true, R.drawable.v5_empty_img);
    	imgLoader.DisplayImage(article.getPic_url(), holder.mPicIv);
        		
		return convertView;
	}
	
    
    class ViewHolder {
    	public TextView mTitleTv;
    	public ImageView mPicIv;
    	
    	public ViewHolder(View itemView) {
			mTitleTv = (TextView) itemView.findViewById(R.id.id_news_title_text);
			mPicIv = (ImageView) itemView.findViewById(R.id.ic_news_img_iv);
		}

    }

}
