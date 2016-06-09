package com.v5kf.mcss.ui.adapter;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.ui.activity.ActivityBase;
import com.v5kf.mcss.ui.activity.md2x.CustomerInfoListActivity;
import com.v5kf.mcss.ui.widget.BadgeView;
import com.v5kf.mcss.ui.widget.CircleImageView;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.cache.ImageLoader;

/**
 * Created by moon.zhong on 2015/2/5.
 */
public class HistoryVisitorAdapter extends RecyclerView.Adapter<HistoryVisitorAdapter.ViewHolder> {

	private static final String TAG = "HistoryVisitorAdapter";
    private List<CustomerBean> mRecycleBeans ;
    private ActivityBase mActivity;
//    private AppInfoKeeper mAppInfo;

    public HistoryVisitorAdapter(List<CustomerBean> mRecycleBeans, ActivityBase activity) {
    	super();
    	Logger.v(TAG, "HistoryVisitorAdapter construct");
        this.mRecycleBeans = mRecycleBeans;
        this.mActivity = activity;
//        this.mAppInfo = CustomApplication.getAppInfoInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_customer, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
    	CustomerBean customer = mRecycleBeans.get(position);
    	holder.setPosition(position);
    	holder.mTitle.setText(customer.getDefaultName());
    	
    	// 设置interface信息
    	UITools.setInterfaceInfo(customer.getIface(), holder.mIfaceTv, holder.mIfaceImg);
    	
    	ImageLoader imgLoader = new ImageLoader(mActivity, true, R.drawable.v5_photo_default_cstm);
    	imgLoader.DisplayImage(customer.getDefaultPhoto(), holder.mPhoto);

    	if (customer.getAccessable() != null && 
    			customer.getAccessable().equals(QAODefine.ACCESSABLE_IDLE)) {
    		holder.mTitle.setTextColor(0xFF01BA1B);
    	} else {
    		holder.mTitle.setTextColor(0xFF000000);
    	}
    }

    @Override
    public int getItemCount() {
        return mRecycleBeans.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
    	private static final String TAG = "HistoryVisitorAdapter.ViewHolder";
    	private int mPosition;

		public CircleImageView mPhoto;
        public TextView mTitle;
        public ImageView mIfaceImg;
        public TextView mIfaceTv;
        public BadgeView mBadgeView;
        public TextView mDate;

        public ViewHolder(View itemView) {
            super(itemView);
            mPhoto = (CircleImageView) itemView.findViewById(R.id.id_item_photo);
            mTitle = (TextView) itemView.findViewById(R.id.id_item_title);
            mIfaceImg = (ImageView) itemView.findViewById(R.id.id_item_iface_img);
            mIfaceTv = (TextView) itemView.findViewById(R.id.id_item_iface_tv);
            mDate = (TextView) itemView.findViewById(R.id.id_item_date);
            mBadgeView = new BadgeView(mActivity);
            itemView.setOnClickListener(this);
//			itemView.setOnLongClickListener(this);
        }

//		@Override
//		public boolean onLongClick(View v) {
//			Logger.d(TAG, "item onLongCLick View.id=" + v.getId());
//			final CustomerBean cstm = mRecycleBeans.get(mPosition);			
//			if (null == cstm) {
//				Logger.e(TAG, "ViewHolder has null SessionRecyclerBean");
//				return false;
//			}
//			
//			itemView.setBackgroundResource(R.color.list_session_item_bg_pressed);
//			mActivity.showOptionDialogInSession(cstm.getSession().getUnreadMessageNum(), cstm.getSession().isInTrust(), new OptionDialogListener() {				
//				@Override
//				public void onClick(View view) {
//					CustomerRequest creq = null;
//					try {
//						creq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, mActivity);
//						switch (view.getId()) {
//						case R.id.btn_dialog_top_option: {		// 标为未读或已读
//							if (cstm.getSession().getUnreadMessageNum() > 0) {
//								/* 标为已读 */
//								cstm.getSession().clearUnreadMessageNum();
//							} else {
//								/* 标为未读 */
//								cstm.getSession().setUnreadMessageNum(1);
//							}
//							HistoryVisitorAdapter.this.notifyItemChanged(mPosition);
//							break;
//						}
//						case R.id.btn_dialog_middle_option1: {	// 置顶
//							mRecycleBeans.remove(mPosition);
//							mRecycleBeans.add(0, cstm);
//							HistoryVisitorAdapter.this.notifyItemRangeChanged(0, mPosition + 1);
//							break;
//						}
//						case R.id.btn_dialog_middle_option2: {	// set_in_trust
//							if (cstm.getSession().isInTrust()) {
//								/* 取消托管 */
//								cstm.getSession().setInTrust(false);
//								creq.setInTrust(cstm.getC_id(), 0);
//							} else {
//								/* 设置托管 */
//								cstm.getSession().setInTrust(true);
//								creq.setInTrust(cstm.getC_id(), 1);
//							}
//							creq.getInTrust(cstm.getC_id());
//							HistoryVisitorAdapter.this.notifyItemChanged(mPosition);
//							break;
//						}
//						
//						case R.id.btn_dialog_bottom_option: {	// end_session
//							/* 结束会话 */
//							creq.endSession(cstm.getC_id());
//							break;
//						}
//						}
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//				}
//
//				@Override
//				public void onDismiss(DialogInterface dialog) {
//					itemView.setBackgroundResource(R.drawable.session_item_bg_selector);
//				}
//			});
//			
//			return true;
//		}

		@Override
		public void onClick(View v) {
			Logger.d(TAG, "item onCLick View.id=" + v.getId());
			CustomerBean cstm = mRecycleBeans.get(mPosition);
			if (null == cstm) {
				Logger.e(TAG, "ViewHolder has null SessionBean");
				return;
			}
			Bundle bundle = new Bundle();
			bundle.putInt(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_ACTIVITY_START);
			bundle.putString(Config.EXTRA_KEY_V_ID, cstm.getVisitor_id());
			
			Intent intent = new Intent(mActivity, CustomerInfoListActivity.class);
			intent.putExtras(bundle);
			mActivity.startActivityForResult(intent, Config.REQUEST_CODE_HISTORY_VISITOR);
			mActivity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		}

		public void setPosition(int mPosition) {
			this.mPosition = mPosition;
		}
    }
}
