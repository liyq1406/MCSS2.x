package com.v5kf.mcss.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.v5kf.mcss.R;
import com.v5kf.mcss.ui.widget.BadgeView;
import com.v5kf.mcss.ui.widget.SegmentView;
import com.v5kf.mcss.ui.widget.SegmentView.onSegmentViewClickListener;

/** 自定义头部布局
  * @ClassName: HeaderLayout
  * @Description: TODO
  * @author chyrain
  * @date 2014-12-23 09:30
  */
public class HeaderLayout extends LinearLayout {
	private LayoutInflater mInflater;
	private View mHeader;
	private LinearLayout mLayoutLeftContainer;
	private LinearLayout mLayoutRightContainer;
	private TextView mHtvSubTitle;
	private SegmentView mHtSegment;
	private View mSegmentRl;
	private BadgeView mSegBadgeLeft, mSegBadgeRight;
	
	private LinearLayout mLayoutRightImageButtonLayout;
	private Button mRightButton;
	private ImageButton mRightImageButton;
	private onRightImageButtonClickListener mRightImageButtonClickListener;

	private LinearLayout mLayoutLeftImageButtonLayout;
	private ImageButton mLeftImageButton;
	private onLeftImageButtonClickListener mLeftImageButtonClickListener;

	public enum HeaderStyle {// 头部整体样式
		DEFAULT_TITLE, 
		TITLE_LIFT_IMAGEBUTTON, 
		TITLE_RIGHT_IMAGEBUTTON, 
		TITLE_RIGHT_BUTTON, 
		TITLE_DOUBLE_IMAGE, 
		TITLE_DOUBLE_IMAGEBUTTON
	}
	
	public enum TittleStyle {
		TEXT,
		SEGMENT
	}
	

	public HeaderLayout(Context context) {
		super(context);
		init(context);
	}

	public HeaderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void init(Context context) {
		mInflater = LayoutInflater.from(context);
		mHeader = mInflater.inflate(R.layout.common_header, null);
		addView(mHeader);
		initViews();
	}

	public void initViews() {
		mLayoutLeftContainer = (LinearLayout) findViewByHeaderId(R.id.header_layout_leftview_container);
		// mLayoutMiddleContainer = (LinearLayout)
		// findViewByHeaderId(R.id.header_layout_middleview_container);中间部分添加搜索或者其他按钮时可打开
		mLayoutRightContainer = (LinearLayout) findViewByHeaderId(R.id.header_layout_rightview_container);
		mHtvSubTitle = (TextView) findViewByHeaderId(R.id.header_htv_subtitle);
		mHtSegment = (SegmentView) findViewByHeaderId(R.id.header_segment);
		mSegmentRl = findViewByHeaderId(R.id.id_segment_layout);
	}
	
	public View getLeftView() {
		return mLayoutLeftContainer;
	}
	
	public View getRightView() {
		return mLayoutRightContainer;
	}

	public View findViewByHeaderId(int id) {
		return mHeader.findViewById(id);
	}

	public void init(HeaderStyle hStyle) {
		switch (hStyle) {
		case DEFAULT_TITLE:
			defaultTitle();
			break;

		case TITLE_LIFT_IMAGEBUTTON:
			defaultTitle();
			titleLeftImageButton();
			break;
			
		case TITLE_RIGHT_IMAGEBUTTON:
			defaultTitle();
			titleRightImageButton();
			break;

		case TITLE_RIGHT_BUTTON:
			defaultTitle();
			titleRightButton();
			break;

		case TITLE_DOUBLE_IMAGEBUTTON:
			defaultTitle();
			titleLeftImageButton();
			titleRightButton();
			break;
		case TITLE_DOUBLE_IMAGE:
			defaultTitle();
			titleLeftImageButton();
			titleRightImageButton();
			break;
		}
	}
	
	public void init(HeaderStyle hStyle, TittleStyle tStyle) {
		setTitleStyle(tStyle);
		init(hStyle);
	}
	
	public void setTitleStyle(TittleStyle tStyle) {
		switch (tStyle) {
		case TEXT:
			mHtSegment.setVisibility(View.GONE);
			mHtvSubTitle.setVisibility(View.VISIBLE);
			mSegmentRl.setVisibility(View.GONE);
			if (mSegBadgeLeft != null) {
				mSegBadgeLeft.setVisibility(View.GONE);
			}
			if (mSegBadgeRight != null) {
				mSegBadgeRight.setVisibility(View.GONE);
			}
			break;
		
		case SEGMENT:
			mHtSegment.setVisibility(View.VISIBLE);
			mHtvSubTitle.setVisibility(View.GONE);
			mSegmentRl.setVisibility(View.VISIBLE);
			break;
		}
	}

	// 默认文字标题
	private void defaultTitle() {		
		mLayoutLeftContainer.removeAllViews();
		mLayoutRightContainer.removeAllViews();
	}

	// 左侧自定义按钮（图）
	private void titleLeftImageButton() {
		View mleftImageButtonView = mInflater.inflate(
				R.layout.common_header_imagebutton, null);
		mLayoutLeftContainer.addView(mleftImageButtonView);
		mLayoutLeftImageButtonLayout = (LinearLayout) mleftImageButtonView
				.findViewById(R.id.header_layout_imagebuttonlayout);
		mLeftImageButton = (ImageButton) mleftImageButtonView
				.findViewById(R.id.header_ib_imagebutton);
		mLayoutLeftImageButtonLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mLeftImageButtonClickListener != null) {
					mLeftImageButtonClickListener.onClick(arg0);
				}
			}
		});
	}
	
	// 右侧自定义按钮（文）
	private void titleRightImageButton() {
		View mRightImageButtonView = mInflater.inflate(
				R.layout.common_header_imagebutton, null);
		mLayoutRightContainer.addView(mRightImageButtonView);
		mLayoutRightImageButtonLayout = (LinearLayout) mRightImageButtonView
				.findViewById(R.id.header_layout_imagebuttonlayout);
		mRightImageButton = (ImageButton) mRightImageButtonView
				.findViewById(R.id.header_ib_imagebutton);
		mLayoutRightImageButtonLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mRightImageButtonClickListener != null) {
					mRightImageButtonClickListener.onClick(arg0);
				}
			}
		});
	}

	// 右侧自定义按钮（文）
	private void titleRightButton() {
		View mRightImageButtonView = mInflater.inflate(
				R.layout.common_header_button, null);
		mLayoutRightContainer.addView(mRightImageButtonView);
		mLayoutRightImageButtonLayout = (LinearLayout) mRightImageButtonView
				.findViewById(R.id.header_layout_buttonlayout);
		mRightButton = (Button) mRightImageButtonView
				.findViewById(R.id.header_ib_button);
		mLayoutRightImageButtonLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mRightImageButtonClickListener != null) {
					mRightImageButtonClickListener.onClick(arg0);
				}
			}
		});
	}

	/** 获取右边按钮
	  * @Title: getRightImageButton
	  * @Description: TODO
	  * @param @return 
	  * @return Button
	  * @throws
	  */
	public Button getRightImageButton(){
		if(mRightButton!=null){
			return mRightButton;
		}
		return null;
	}
	
	public void setDefaultTitle(CharSequence title) {
		if (title != null) {
			mHtvSubTitle.setText(title);
		} else {
			mHtvSubTitle.setVisibility(View.GONE);
		}
	}
	
	public void setDefaultTitle(int resId) {
		if (resId > 0) {
			mHtvSubTitle.setText(resId);
		} else {
			mHtvSubTitle.setVisibility(View.GONE);
		}
	}

//	public void setTitleAndRightButton(CharSequence title, int backid,String text,
//			onRightImageButtonClickListener onRightImageButtonClickListener) {
//		setDefaultTitle(title);
//		mLayoutRightContainer.setVisibility(View.VISIBLE);
//		if (mRightImageButton != null && backid > 0) {
////			mRightImageButton.setWidth(PixelUtil.dp2px(45));
////			mRightImageButton.setHeight(PixelUtil.dp2px(40));
//			mRightImageButton.setBackgroundResource(backid);
//			mRightImageButton.setText(text);
//			setOnRightImageButtonClickListener(onRightImageButtonClickListener);
//		}
//	}
	
//	public void setTitleAndRightButton(CharSequence title, String rightText,
//			onRightImageButtonClickListener onRightImageButtonClickListener) {
//		setDefaultTitle(title);
//		mLayoutRightContainer.setVisibility(View.VISIBLE);
//		if (mRightImageButton != null) {
////			mRightImageButton.setWidth(PixelUtil.dp2px(45));
////			mRightImageButton.setHeight(PixelUtil.dp2px(40));
//			mRightImageButton.setText(rightText);
//			setOnRightImageButtonClickListener(onRightImageButtonClickListener);
//		}
//	}
	
//	public void setTitleAndRightImageButton(CharSequence title, int backid,
//			onRightImageButtonClickListener onRightImageButtonClickListener) {
//		setDefaultTitle(title);
//		mLayoutRightContainer.setVisibility(View.VISIBLE);
//		if (mRightImageButton != null && backid > 0) {
//			mRightImageButton.setWidth(PixelUtil.dp2px(30));
//			mRightImageButton.setHeight(PixelUtil.dp2px(30));
//			mRightImageButton.setLayoutParams(new LayoutParams(PixelUtil.dp2px(30), PixelUtil.dp2px(30)));
//			mRightImageButton.setTextColor(getResources().getColor(R.color.transparent));
//			mRightImageButton.setBackgroundResource(backid);
//			setOnRightImageButtonClickListener(onRightImageButtonClickListener);
//		}
//	}

//	public void setTitleAndLeftImageButton(CharSequence title, int id,
//			onLeftImageButtonClickListener listener) {
//		setDefaultTitle(title);
//		if (mLeftImageButton != null && id > 0) {
//			mLeftImageButton.setImageResource(id);
//			setOnLeftImageButtonClickListener(listener);
//		}
//		mLayoutRightContainer.setVisibility(View.INVISIBLE);
//	}
	
//	public void setTitleAndRightButton(int titleResId, int backid, String text,
//			onRightImageButtonClickListener onRightImageButtonClickListener) {
//		setDefaultTitle(titleResId);
//		mLayoutRightContainer.setVisibility(View.VISIBLE);
//		if (mRightImageButton != null && backid > 0) {
////			mRightImageButton.setWidth(PixelUtil.dp2px(45));
////			mRightImageButton.setHeight(PixelUtil.dp2px(40));
//			mRightImageButton.setBackgroundResource(backid);
//			mRightImageButton.setText(text);
//			setOnRightImageButtonClickListener(onRightImageButtonClickListener);
//		}
//	}

	public void setTitleAndRightText(int titleResId, int backid, int textId,
			onRightImageButtonClickListener onRightImageButtonClickListener) {
		setDefaultTitle(titleResId);
		mLayoutRightContainer.setVisibility(View.VISIBLE);
		if (mRightButton != null && backid > 0) {
//			mRightImageButton.setWidth(PixelUtil.dp2px(45));
//			mRightImageButton.setHeight(PixelUtil.dp2px(40));
			mRightButton.setBackgroundResource(backid);
			mRightButton.setText(textId);
			setOnRightImageButtonClickListener(onRightImageButtonClickListener);
		}
	}
	
//	public void setTitleAndRightButton(int titleResId, int rightTextId,
//			onRightImageButtonClickListener onRightImageButtonClickListener) {
//		setDefaultTitle(titleResId);
//		mLayoutRightContainer.setVisibility(View.VISIBLE);
//		if (mRightImageButton != null) {
////			mRightImageButton.setWidth(PixelUtil.dp2px(45));
////			mRightImageButton.setHeight(PixelUtil.dp2px(40));
//			mRightImageButton.setText(rightTextId);
//			setOnRightImageButtonClickListener(onRightImageButtonClickListener);
//		}
//	}
	
	public void setTitleAndRightImage(int titleResId, int rightImgId,
			onRightImageButtonClickListener onRightImageButtonClickListener) {
		setDefaultTitle(titleResId);
		mLayoutRightContainer.setVisibility(View.VISIBLE);
		if (mRightImageButton != null && rightImgId > 0) {
			mRightImageButton.setImageResource(rightImgId);
			setOnRightImageButtonClickListener(onRightImageButtonClickListener);
		}
	}
	
	public void setTitleAndRightImage(CharSequence title, int rightImgId,
			onRightImageButtonClickListener onRightImageButtonClickListener) {
		setDefaultTitle(title);
		mLayoutRightContainer.setVisibility(View.VISIBLE);
		if (mRightImageButton != null && rightImgId > 0) {
			mRightImageButton.setImageResource(rightImgId);
			setOnRightImageButtonClickListener(onRightImageButtonClickListener);
		}
	}
	
	public void setTitleAndLeftImage(int titleResId, int leftImagId,
			onLeftImageButtonClickListener listener) {
		setDefaultTitle(titleResId);
		if (mLeftImageButton != null && leftImagId > 0) {
			mLeftImageButton.setImageResource(leftImagId);
			setOnLeftImageButtonClickListener(listener);
		}
		mLayoutRightContainer.setVisibility(View.INVISIBLE);
	}
	
	public void setTitleAndLeftImage(CharSequence title, int leftImagId,
			onLeftImageButtonClickListener listener) {
		setDefaultTitle(title);
		if (mLeftImageButton != null && leftImagId > 0) {
			mLeftImageButton.setImageResource(leftImagId);
			setOnLeftImageButtonClickListener(listener);
		}
		mLayoutRightContainer.setVisibility(View.INVISIBLE);
	}

	public void setOnRightImageButtonClickListener(
			onRightImageButtonClickListener listener) {
		mRightImageButtonClickListener = listener;
	}

	public interface onRightImageButtonClickListener {
		void onClick(View arg0);
	}

	public void setOnLeftImageButtonClickListener(
			onLeftImageButtonClickListener listener) {
		mLeftImageButtonClickListener = listener;
	}

	public interface onLeftImageButtonClickListener {
		void onClick(View v);
	}

	public void setSegmentTitle(int leftImgId, int rightTitleId) {
		// TODO Auto-generated method stub
		mHtSegment.setSegmentText(leftImgId, 0);
		mHtSegment.setSegmentText(rightTitleId, 1);
	}
	
	public void setSegmentListener(onSegmentViewClickListener segmentListener) {
		if (null != segmentListener) {
			mHtSegment.setOnSegmentViewClickListener(segmentListener);
		}
	}

	public SegmentView getSegmentView() {
		// TODO Auto-generated method stub
		return mHtSegment;
	}

	public void setSegmentBadge(int count, int position) {
		if (position == 0) {
			if (null == mSegBadgeLeft) {
				mSegBadgeLeft = new BadgeView(getContext());
			}
			mSegBadgeLeft.setTargetView(mSegmentRl);
			mSegBadgeLeft.setBadgeGravity(Gravity.LEFT|Gravity.TOP);
			mSegBadgeLeft.setBadgeCount(count);
		} else if (position == 1) {
			if (null == mSegBadgeRight) {
				mSegBadgeRight = new BadgeView(getContext());
			}
			mSegBadgeRight.setTargetView(mSegmentRl);
			mSegBadgeRight.setBadgeGravity(Gravity.RIGHT|Gravity.TOP);
			mSegBadgeRight.setBadgeCount(count);
		}
	}
}
