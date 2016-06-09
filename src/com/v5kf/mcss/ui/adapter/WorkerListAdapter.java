package com.v5kf.mcss.ui.adapter;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.entity.ArchWorkerBean;
import com.v5kf.mcss.ui.activity.ActivityBase;
import com.v5kf.mcss.ui.activity.md2x.WorkerInfoActivity;
import com.v5kf.mcss.ui.widget.CircleImageView;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.cache.ImageLoader;

/**
 * Created by moon.zhong on 2015/2/5.
 */
public class WorkerListAdapter extends RecyclerView.Adapter<WorkerListAdapter.ViewHolder> {

	private static final String TAG = "WorkerListAdapter";
    private List<ArchWorkerBean> mRecycleBeans ;
    private ActivityBase mActivity;

    public WorkerListAdapter(ActivityBase activity, List<ArchWorkerBean> mRecycleBeans) {
    	super();
    	Logger.v(TAG, "WorkerListAdapter construct");
        this.mRecycleBeans = mRecycleBeans;
        this.mActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
    	ArchWorkerBean coWorker = mRecycleBeans.get(position);
    	holder.setPosition(position);    	
    	holder.mDescIv.setVisibility(View.GONE);
    	holder.mNameTv.setText(coWorker.getName());
    	ImageLoader imgLoader = new ImageLoader(mActivity, true, R.drawable.v5_photo_default);
    	imgLoader.DisplayImage(coWorker.getPhoto(), holder.mPhotoIv);
    	UITools.setStatusTextInfo(coWorker.getStatus(), holder.mRightTv);
    }

    @Override
    public int getItemCount() {
        return mRecycleBeans.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener, OnLongClickListener{
    	private static final String TAG = "HistoryVisitorAdapter.ViewHolder";
    	private int mPosition;

    	public TextView mNameTv;
	    public CircleImageView mPhotoIv;
	    public ImageView mDescIv;
	    public TextView mRightTv;

        public ViewHolder(View itemView) {
            super(itemView);
            mPhotoIv = (CircleImageView) itemView.findViewById(R.id.id_item_photo);
            mNameTv = (TextView) itemView.findViewById(R.id.id_item_name);
            mRightTv = (TextView) itemView.findViewById(R.id.id_item_status_tv);
            mDescIv = (ImageView) itemView.findViewById(R.id.id_item_status_img);
            itemView.setOnClickListener(this);
//			itemView.setOnLongClickListener(this);
        }

		@Override
		public boolean onLongClick(View v) {
			Logger.d(TAG, "item onLongCLick View.id=" + v.getId());
			final ArchWorkerBean bean = mRecycleBeans.get(mPosition);			
			if (null == bean) {
				Logger.e(TAG, "ViewHolder has null ArchWorkerBean");
				return false;
			}
						
			return false;
		}

		@Override
		public void onClick(View v) {
			Logger.d(TAG, "item onCLick View.id=" + v.getId());
			ArchWorkerBean recyclerBean = mRecycleBeans.get(mPosition);
			if (null == recyclerBean) {
				Logger.e(TAG, "ViewHolder has null SessionBean");
				return;
			}
			
			Bundle bundle = new Bundle();
			bundle.putInt(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_ACTIVITY_START);
			bundle.putString(Config.EXTRA_KEY_W_ID, recyclerBean.getW_id());			
			Intent intent = new Intent(mActivity, WorkerInfoActivity.class);
			intent.putExtras(bundle);
			mActivity.startActivity(intent);
			mActivity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		}

		public void setPosition(int mPosition) {
			this.mPosition = mPosition;
		}
    }
}
