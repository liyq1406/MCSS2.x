package com.v5kf.mcss.ui.adapter;

import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rockerhieu.emojicon.EmojiconTextView;
import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.entity.SessionBean;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.CustomerRequest;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.ui.activity.md2x.ChatMessagesActivity;
import com.v5kf.mcss.ui.widget.BadgeView;
import com.v5kf.mcss.ui.widget.CircleImageView;
import com.v5kf.mcss.ui.widget.CustomOptionDialog.OptionDialogListener;
import com.v5kf.mcss.utils.DateUtil;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.IntentUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.cache.ImageLoader;

/**
 * Created by moon.zhong on 2015/2/5.
 */
public class WaitingSessionAdapter extends RecyclerView.Adapter<WaitingSessionAdapter.ViewHolder> {

	private static final String TAG = "SessionRecyclerAdapter";
    private List<CustomerBean> mRecycleBeans ;
    private Activity mContext;
    private AppInfoKeeper mAppInfo;

    public WaitingSessionAdapter(List<CustomerBean> mRecycleBeans, Activity f) {
    	super();
    	Logger.v(TAG, "WaitingSessionAdapter construct");
        this.mRecycleBeans = mRecycleBeans;
        this.mContext = f;
        this.mAppInfo = CustomApplication.getAppInfoInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_md2x_waiting_session, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
    	final CustomerBean customer = mRecycleBeans.get(position);
    	SessionBean session = customer.getSession();
    	if (session == null) {
    		session = new SessionBean(null, customer.getC_id());
    	}
    	holder.setPosition(position);
    	holder.mDate.setText(DateUtil.timeFormat(session.getDefaultTime(), true));
    	// [新增]离开状态提示
    	if (customer.getAccessable() != null && customer.getAccessable().equals(QAODefine.ACCESSABLE_AWAY)) {
    		holder.mTitle.setText("[离开]" + Html.fromHtml(customer.getDefaultName()));
    	} else {
    		holder.mTitle.setText(Html.fromHtml(customer.getDefaultName()));
    	}
    	// 最新消息未获取到
    	if (mAppInfo.getLastestMessageOfCustomer(customer) != null) {
	    	String content = mAppInfo.getLastestMessageOfCustomer(customer).getDefaultContent(mContext).replaceAll("<", "&lt;");
	    	content = content.replaceAll(">", "&gt;");
	    	holder.mContent.setText(Html.fromHtml(content));
    	} else {
    		holder.mContent.setText("");
    	}
    	ImageLoader imgLoader = new ImageLoader(mContext, true, R.drawable.v5_photo_default_cstm, new ImageLoader.ImageLoaderListener() {
			
			@Override
			public void onSuccess(String url, ImageView imageView) {
				Logger.d(TAG, "ImageLoaderListener.onSuccess");
				// [新增]离开状态提示
		    	if (customer.getAccessable() != null && customer.getAccessable().equals(QAODefine.ACCESSABLE_AWAY)) {
		    		Logger.d(TAG, "DisplayUtil.grayImageView Accessable:" + customer.getAccessable());
		    		UITools.grayImageView(imageView);
		    	}
			}
			
			@Override
			public void onFailure(ImageLoader imageLoader, String url,
					ImageView imageView) {
				Logger.d(TAG, "ImageLoaderListener.onFailure Accessable:" + customer.getAccessable());
				// [新增]离开状态提示
		    	if (customer.getAccessable() != null && customer.getAccessable().equals(QAODefine.ACCESSABLE_AWAY)) {
		    		Logger.d(TAG, "DisplayUtil.grayImageView begin");
		    		UITools.grayImageView(imageView);
		    		Logger.d(TAG, "DisplayUtil.grayImageView done");
		    	}
			}
		});
    	imgLoader.DisplayImage(customer.getDefaultPhoto(), holder.mPhoto);
    	
    	// 设置interface信息
    	UITools.setInterfaceInfo(customer.getIface(), holder.mIfaceTv, holder.mIfaceImg);
    	    	
//    	// 接入可否
//    	if (customer.getAccessable().equals(QAODefine.ACCESSABLE_IDLE)) {
//    		holder.mPickupLayout.setVisibility(View.VISIBLE);
//    	} else {
//    		holder.mPickupLayout.setVisibility(View.GONE);
//    	}
    	
    	if (session.getUnreadMessageNum() > 0) {
    		if (session.getUnreadMessageNum() > 99) {
    			session.setUnreadMessageNum(99);
    		}
    		holder.mBadgeView.setTargetView(holder.mImgLayout);
    		holder.mBadgeView.setBadgeGravity(Gravity.END | Gravity.TOP);  
    		holder.mBadgeView.setBadgeCount(session.getUnreadMessageNum());
    	} else if (session.getUnreadMessageNum() == -1) {
    		holder.mBadgeView.setTargetView(holder.mImgLayout);
    		holder.mBadgeView.setBadgeGravity(Gravity.END | Gravity.TOP);
    		holder.mBadgeView.setBadgeCount(0);
    	} else {
    		holder.mBadgeView.setVisibility(View.INVISIBLE);
    	}
    }

    @Override
    public int getItemCount() {
        return mRecycleBeans.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener, OnLongClickListener {
    	private static final String TAG = "SessionRecyclerAdapter.ViewHolder";
    	private int mPosition;

		public CircleImageView mPhoto;
        public TextView mTitle;
        public EmojiconTextView mContent;
        public ImageView mIfaceImg;
        public TextView mIfaceTv;
        public TextView mDate;        
        public BadgeView mBadgeView;
        public View mImgLayout;
        public View mPickupLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            mPhoto = (CircleImageView) itemView.findViewById(R.id.id_item_photo);
            mTitle = (TextView) itemView.findViewById(R.id.id_item_title);
            mContent = (EmojiconTextView) itemView.findViewById(R.id.id_item_content);
            mIfaceImg = (ImageView) itemView.findViewById(R.id.id_item_iface_img);
            mIfaceTv = (TextView) itemView.findViewById(R.id.id_item_iface_tv);
            mDate = (TextView) itemView.findViewById(R.id.id_item_date);
            mImgLayout = itemView.findViewById(R.id.id_img_layout);
            mPickupLayout = itemView.findViewById(R.id.layout_pickup);
            mBadgeView = new BadgeView(mContext);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.findViewById(R.id.layout_pickup).setOnClickListener(this);
        }

		@Override
		public boolean onLongClick(View v) {
			Logger.d(TAG, "item onLongCLick View.id=" + v.getId());
			final CustomerBean recyclerBean = mRecycleBeans.get(mPosition);			
			if (null == recyclerBean) {
				Logger.e(TAG, "ViewHolder has null SessionRecyclerBean");
				return false;
			}
			
			itemView.setBackgroundResource(R.color.list_session_item_bg_pressed);
			((ActivityBase)mContext).showOptionDialogInWaiting(new OptionDialogListener() {				
				@Override
				public void onClick(View view) {
					CustomerRequest creq = null;
					try {
						creq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, mContext);
						switch (view.getId()) {
//						case R.id.btn_dialog_top_option: {		// 标为未读或已读
//							if (recyclerBean.getSession().getUnreadMessageNum() > 0) {
//								/* 标为已读 */
//								recyclerBean.getSession().clearUnreadMessageNum();
//							} else {
//								/* 标为未读 */
//								recyclerBean.getSession().setUnreadMessageNum(1);
//							}
//							WaitingSessionAdapter.this.notifyItemChanged(mPosition);
//							break;
//						}
//						
//						case R.id.btn_dialog_middle_option1: {	// 置顶
//							mRecycleBeans.remove(mPosition);
//							mRecycleBeans.add(0, recyclerBean);
//							WaitingSessionAdapter.this.notifyItemRangeChanged(0, mPosition + 1);
//							break;
//						}
//										
//						case R.id.btn_dialog_bottom_option: { // 手动接入客户
//							creq.pickCustomer(recyclerBean.getC_id());
//							break;
//						}
						case R.id.btn_dialog_only_option: { // 手动接入客户
							creq.pickCustomer(recyclerBean.getC_id());
							break;
						}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onDismiss(DialogInterface dialog) {
					itemView.setBackgroundResource(R.drawable.session_item_bg_selector);
				}
			});
			
			return true;
		}

		@Override
		public void onClick(View v) {
			Logger.d(TAG, "item onCLick View.id=" + v.getId());
			CustomerBean recyclerBean = mRecycleBeans.get(mPosition);			
			if (null == recyclerBean) {
				Logger.e(TAG, "ViewHolder has null SessionRecyclerBean");
				return;
			}
			switch (v.getId()) {
			case R.id.layout_pickup:
				CustomerRequest creq = null;
				try {
					creq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, mContext);
					creq.pickCustomer(recyclerBean.getC_id());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case R.id.layout_chat_waiting_session:
				recyclerBean.getSession().clearUnreadMessageNum();
				
				((ActivityBase)mContext).startActivityForResult(
						IntentUtil.getStartActivityIntent(
								mContext, 
								ChatMessagesActivity.class,
								recyclerBean.getC_id(), 
								recyclerBean.getSession().getS_id()), 
						Config.REQUEST_CODE_WAITING_SESSION_FRAGMENT);
				((ActivityBase)mContext).overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				break;
			default:
				
				break;
			}
		}

		public void setPosition(int mPosition) {
			this.mPosition = mPosition;
		}
    }
}
