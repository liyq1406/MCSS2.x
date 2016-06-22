package com.v5kf.mcss.ui.adapter;

import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.R;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.entity.WorkerLogBean;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.utils.DateUtil;
import com.v5kf.mcss.utils.Logger;

/**
 * Created by moon.zhong on 2015/2/5.
 */
public class WorkerLogAdapter extends RecyclerView.Adapter<WorkerLogAdapter.ViewHolder> {

	private static final String TAG = "WorkerLogAdapter";
    private List<WorkerLogBean> mDatas ;
    private ActivityBase mActivity;
    private AppInfoKeeper mAppInfo;
    private long mTmpDate;

    public WorkerLogAdapter(List<WorkerLogBean> datas, ActivityBase activity) {
    	super();
    	Logger.v(TAG, "ServingSessionAdapter construct");
        this.mDatas = datas;
        this.mActivity = activity;
        this.mAppInfo = CustomApplication.getAppInfoInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_worker_log_text, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
    	WorkerLogBean workerLog = mDatas.get(position);
    	holder.setRecyclerBean(workerLog);
    	
    	holder.mLogTv.setText(workerLog.getDescription());
//		if (!DateUtil.isSameDate(mTmpDate, workerLog.getTime())) {
//			holder.mDateTv.setVisibility(View.VISIBLE);
//			holder.mDateTv.setText(DateUtil.timeFormat(workerLog.getTime(), false));
//		} else {
//			holder.mDateTv.setVisibility(View.GONE);
//		}
//		mTmpDate = workerLog.getTime();
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener, OnLongClickListener {
    	private static final String TAG = "SessionRecyclerAdapter.ViewHolder";
    	private WorkerLogBean mRecyclerBean;

    	public TextView mDateTv;
        public TextView mLogTv;

        public ViewHolder(View itemView) {
            super(itemView);
            mDateTv = (TextView) itemView.findViewById(R.id.id_log_date);
            mLogTv = (TextView) itemView.findViewById(R.id.id_worker_log_tv);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

		@Override
		public boolean onLongClick(View v) {
			Logger.d(TAG, "item onLongCLick View.id=" + v.getId());		
			if (null == mRecyclerBean) {
				Logger.e(TAG, "ViewHolder has null WorkerLogBean");
				return false;
			}
			
			return false;
		}

		@Override
		public void onClick(View v) {
			Logger.d(TAG, "item onCLick View.id=" + v.getId());
			if (null == mRecyclerBean) {
				Logger.e(TAG, "ViewHolder has null WorkerLogBean");
				return;
			}
		}

		public void setRecyclerBean(WorkerLogBean bean) {
			this.mRecyclerBean = bean;
		}
    }
    
}
