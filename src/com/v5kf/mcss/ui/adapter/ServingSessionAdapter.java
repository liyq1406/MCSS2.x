package com.v5kf.mcss.ui.adapter;

import java.util.List;

import org.json.JSONException;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.v5kf.client.ui.emojicon.EmojiconTextView;
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
import com.v5kf.mcss.ui.activity.md2x.ChattingListActivity;
import com.v5kf.mcss.ui.activity.md2x.WorkerTreeActivity;
import com.v5kf.mcss.ui.widget.BadgeView;
import com.v5kf.mcss.ui.widget.CircleImageView;
import com.v5kf.mcss.ui.widget.CustomOptionDialog.OptionDialogListener;
import com.v5kf.mcss.utils.DateUtil;
import com.v5kf.mcss.utils.IntentUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.cache.ImageLoader;

/**
 * Created by moon.zhong on 2015/2/5.
 */
public class ServingSessionAdapter extends RecyclerView.Adapter<ServingSessionAdapter.ViewHolder> {

	private static final String TAG = "ServingSessionAdapter";
    private List<CustomerBean> mRecycleBeans ;
    private ActivityBase mActivity;
    private AppInfoKeeper mAppInfo;

    public ServingSessionAdapter(List<CustomerBean> mRecycleBeans, ActivityBase activity) {
    	super();
    	Logger.v(TAG, "ServingSessionAdapter construct");
        this.mRecycleBeans = mRecycleBeans;
        this.mActivity = activity;
        this.mAppInfo = CustomApplication.getAppInfoInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_md2x_serving_session, parent, false);
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
    	holder.setRecyclerBean(customer);
    	holder.mDate.setText(DateUtil.timeFormat(session.getDefaultTime(), true));
    	// [新增]离开状态提示
    	if (customer.getAccessable() != null && customer.getAccessable().equals(QAODefine.ACCESSABLE_AWAY)) {
    		holder.mTitle.setText("[离开]" + Html.fromHtml(customer.getDefaultName()));
    	} else {
    		holder.mTitle.setText(Html.fromHtml(customer.getDefaultName()));
    	}
    	Logger.w(TAG, "托管状态：" + session.isInTrust());
    	if (session.isInTrust()) {
    		holder.mTrustIv.setVisibility(View.VISIBLE);
//    		Drawable drawable = UITools.getDrawable(mActivity, R.drawable.v5_popmenu_in_trust);
//    		drawable.setBounds(0, 0, holder.mTitle.getHeight(), holder.mTitle.getHeight());
//    		holder.mTitle.setCompoundDrawables(			
//    				drawable, 
//    				null, 
//    				null, 
//    				null);
    	} else {
    		holder.mTrustIv.setVisibility(View.GONE);
//    		holder.mTitle.setCompoundDrawables(null, null, null, null);
    	}
    	
    	// 最新消息未获取到
    	if (mAppInfo.getLastestMessageOfCustomer(customer) != null) {
	    	String content = mAppInfo.getLastestMessageOfCustomer(customer).getDefaultContent(mActivity);
//	    	content = content.replaceAll("<", "&lt;");
//	    	content = content.replaceAll(">", "&gt;");
	    	Spanned spanText = Html.fromHtml(content.replace("\n", "<br>"));
	    	holder.mContent.setText(spanText);
			holder.mContent.setMovementMethod(LinkMovementMethod.getInstance());
    	} else {
    		holder.mContent.setText("");
    	}
    	
    	// 客户接口信息设置
    	UITools.setInterfaceInfo(customer.getIface(), holder.mIfaceTv, holder.mIfaceImg);
    	
    	Logger.i(TAG, "ServingSessionAdapter.onBindViewHolder -> ImageLoader.displayimage Accessable:" + customer.getAccessable());
    	ImageLoader imgLoader = new ImageLoader(mActivity, true, R.drawable.v5_photo_default_cstm, new ImageLoader.ImageLoaderListener() {
			
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
    	private CustomerBean mRecyclerBean;

		public CircleImageView mPhoto;
        public TextView mTitle;
        public EmojiconTextView mContent;
        public ImageView mIfaceImg;
        public TextView mIfaceTv;
        public TextView mDate;
        
        public View mImgLayout;
        public BadgeView mBadgeView;
        
        public ImageView mTrustIv;
        public View mBottomFuncLayout;
        public boolean isBottomShow;

        public ViewHolder(View itemView) {
            super(itemView);
            mPhoto = (CircleImageView) itemView.findViewById(R.id.id_item_photo);
            mTitle = (TextView) itemView.findViewById(R.id.id_item_title);
            mContent = (EmojiconTextView) itemView.findViewById(R.id.id_item_content);
            mIfaceImg = (ImageView) itemView.findViewById(R.id.id_item_iface_img);
            mIfaceTv = (TextView) itemView.findViewById(R.id.id_item_iface_tv);
            mDate = (TextView) itemView.findViewById(R.id.id_item_date);
            mImgLayout = itemView.findViewById(R.id.id_img_layout);
            mBottomFuncLayout = itemView.findViewById(R.id.layout_bottom_func);
            mTrustIv = (ImageView)itemView.findViewById(R.id.id_trust_robot);
            mBadgeView = new BadgeView(mActivity);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            
            itemView.findViewById(R.id.layout_more).setOnClickListener(this);
            itemView.findViewById(R.id.id_trust_btn).setOnClickListener(this);
            itemView.findViewById(R.id.id_switch_btn).setOnClickListener(this);
            itemView.findViewById(R.id.id_close_btn).setOnClickListener(this);
        }

		@Override
		public boolean onLongClick(View v) {
			Logger.d(TAG, "item onLongCLick View.id=" + v.getId());		
			if (null == mRecyclerBean || mRecyclerBean.getSession() == null) {
				Logger.e(TAG, "ViewHolder has null SessionRecyclerBean");
				return false;
			}
			
			itemView.setBackgroundResource(R.color.list_session_item_bg_pressed);
			mActivity.showOptionDialogInSession(mRecyclerBean.getSession().isInTrust(), new OptionDialogListener() {				
				@Override
				public void onClick(View view) {
					CustomerRequest creq = null;
					try {
						creq = (CustomerRequest) RequestManager.getRequest(QAODefine.O_TYPE_WCSTM, mActivity);
						switch (view.getId()) {
//						case R.id.btn_dialog_top_option: {		// 标为未读或已读
//							if (mRecyclerBean.getSession().getUnreadMessageNum() > 0) {
//								/* 标为已读 */
//								mRecyclerBean.getSession().clearUnreadMessageNum();
//							} else {
//								/* 标为未读 */
//								mRecyclerBean.getSession().setUnreadMessageNum(1);
//							}
//							ServingSessionAdapter.this.notifyItemChanged(getAdapterPosition());
//							break;
//						}
//						
//						case R.id.btn_dialog_middle_option1: {	// 置顶
//							mRecycleBeans.remove(getAdapterPosition());
//							mRecycleBeans.add(0, mRecyclerBean);
//							ServingSessionAdapter.this.notifyItemRangeChanged(0, getAdapterPosition() + 1);
//							break;
//						}
						
						case R.id.btn_dialog_top_option: {	// set_in_trust
							if (mRecyclerBean.getSession().isInTrust()) {
								/* 取消托管 */
								mRecyclerBean.getSession().setInTrust(false);
								creq.setInTrust(mRecyclerBean.getC_id(), 0);
//								if (null != cstm) {
//									mRecyclerBean.setTitle(cstm.getDefaultName());
//								}
								
							} else {
								/* 设置托管 */
								mRecyclerBean.getSession().setInTrust(true);
								creq.setInTrust(mRecyclerBean.getC_id(), 1);
//								if (null != cstm) {
//									mRecyclerBean.setTitle(mActivity.getString(R.string.is_in_trust) + cstm.getDefaultName());
//								}
							}
							creq.getInTrust(mRecyclerBean.getC_id());
							ServingSessionAdapter.this.notifyDataSetChanged(); //notifyItemChanged(getAdapterPosition());
							break;
						}
						
						case R.id.btn_dialog_middle_option1: { // 转接							
							Intent intent = IntentUtil.getStartActivityIntent(
									mActivity, 
									WorkerTreeActivity.class, 
									mRecyclerBean.getC_id(), 
									mRecyclerBean.getSession().getS_id(), 
									mRecyclerBean.getService());
							mActivity.startActivityForResult(intent, Config.REQUEST_CODE_SERVING_SESSION_FRAGMENT);
							mActivity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
							break;
						}
						
						case R.id.btn_dialog_bottom_option: {	// end_session
							/* 结束会话 */
							creq.endSession(mRecyclerBean.getC_id());
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
			if (null == mRecyclerBean || mRecyclerBean.getSession() == null) {
				Logger.e(TAG, "ViewHolder has null SessionBean");
				return;
			}
			switch (v.getId()) {
			case R.id.layout_more: // 展开
				if (mBottomFuncLayout.getVisibility() == View.GONE) {
					mBottomFuncLayout.setVisibility(View.VISIBLE);
				} else if (mBottomFuncLayout.getVisibility() == View.VISIBLE) {
					mBottomFuncLayout.setVisibility(View.GONE);
				}
//				itemView.invalidate();
				if (itemView.getParent() != null) {
					itemView.getParent().requestLayout();
				}
				break;
			case R.id.layout_chat_serving_session: // 点击整项
				mRecyclerBean.getSession().clearUnreadMessageNum();
				
				/* 进入对话界面 */
				Intent intent = IntentUtil.getStartActivityIntent(
						mActivity, 
						ChattingListActivity.class,
						mRecyclerBean.getC_id(),
						mRecyclerBean.getSession().getS_id());
				mActivity.startActivityForResult(intent, Config.REQUEST_CODE_SERVING_SESSION_FRAGMENT);
				mActivity.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				break;
			case R.id.id_trust_btn: // 托管
				
				break;
			case R.id.id_switch_btn: // 转接
				
				break;
			case R.id.id_close_btn: // 关闭
				
				break;
			default:
				break;
			}
		}

		public void setRecyclerBean(CustomerBean bean) {
			this.mRecyclerBean = bean;
		}
    }
}
