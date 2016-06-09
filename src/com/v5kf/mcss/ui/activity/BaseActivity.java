package com.v5kf.mcss.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.v5kf.mcss.R;
import com.v5kf.mcss.ui.view.HeaderLayout;
import com.v5kf.mcss.ui.view.HeaderLayout.HeaderStyle;
import com.v5kf.mcss.ui.view.HeaderLayout.TittleStyle;
import com.v5kf.mcss.ui.view.HeaderLayout.onLeftImageButtonClickListener;
import com.v5kf.mcss.ui.view.HeaderLayout.onRightImageButtonClickListener;
import com.v5kf.mcss.ui.widget.SegmentView;
import com.v5kf.mcss.ui.widget.SegmentView.onSegmentViewClickListener;

/** 
  * 基类，提供标题的各种初始化的函数
  * @ClassName: BaseActivity
  * @Description: TODO
  * @author Chyrain
  * @date 2014-12-23 09:46
  */
public abstract class BaseActivity extends ActivityBase {

	protected HeaderLayout mHeaderLayout;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	public void hideHeader() {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.setVisibility(View.GONE);
	}
	
	public void showHeader() {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.setVisibility(View.VISIBLE);
	}
	
	public void initTopBarForOnlyTitle(String titleName) {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.init(HeaderStyle.DEFAULT_TITLE);
		mHeaderLayout.setDefaultTitle(titleName);
	}
	
	public void initTopBarForOnlyTitle(int titleTextId) {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.init(HeaderStyle.DEFAULT_TITLE);
		mHeaderLayout.setDefaultTitle(titleTextId);
	}

	public void initTopBarForLeftBackAndRightText(int titleName, int rightText,
			onRightImageButtonClickListener listener) {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGEBUTTON);
		mHeaderLayout.setTitleAndLeftImage(titleName,
				R.drawable.base_action_bar_back_bg_selector,
				new OnBackBtnClickListener());
		mHeaderLayout.setTitleAndRightText(titleName, R.color.transparent, rightText,
				listener);
	}
	
	public void initTopBarForLeftBackAndRightImage(int titleId, int rightImgId,
			onRightImageButtonClickListener listener) {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGE);
		mHeaderLayout.setTitleAndLeftImage(titleId,
				R.drawable.base_action_bar_back_bg_selector,
				new OnBackBtnClickListener());
		mHeaderLayout.setTitleAndRightImage(titleId, rightImgId, listener);
	}
	
	public void initTopBarForLeftBackAndRightImage(String titleName, int rightImgId,
			onRightImageButtonClickListener listener) {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGE);
		mHeaderLayout.setTitleAndLeftImage(titleName,
				R.drawable.base_action_bar_back_bg_selector,
				new OnBackBtnClickListener());
		mHeaderLayout.setTitleAndRightImage(titleName, rightImgId, listener);
	}
	
	public void initTopBarForLeftImageAndRightImage(int titleId, int leftImgId, int rightImgId, 
			onLeftImageButtonClickListener leftListener, onRightImageButtonClickListener rightListener) {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGE);
		mHeaderLayout.setTitleAndLeftImage(titleId,	leftImgId, leftListener);
		mHeaderLayout.setTitleAndRightImage(titleId, rightImgId, rightListener);
	}
	
	public void initTopBarForLeftImageAndRightImage(CharSequence title, int leftImgId, int rightImgId, 
			onLeftImageButtonClickListener leftListener, onRightImageButtonClickListener rightListener) {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGE);
		mHeaderLayout.setTitleAndLeftImage(title,	leftImgId, leftListener);
		mHeaderLayout.setTitleAndRightImage(title, rightImgId, rightListener);
	}
	
	public void initTopBarForLeftImageAndRightText(int titleId, int leftImgId, int rightTextId, 
			onLeftImageButtonClickListener leftListener, onRightImageButtonClickListener rightListener) {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGEBUTTON);
		mHeaderLayout.setTitleAndLeftImage(titleId,	leftImgId, leftListener);
		mHeaderLayout.setTitleAndRightText(titleId, R.color.transparent, rightTextId, rightListener);
	}

	public void initTopBarForRightImage(int titleId, int rightImgId, onRightImageButtonClickListener listener) {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.init(HeaderStyle.TITLE_RIGHT_IMAGEBUTTON);
		mHeaderLayout.setTitleAndRightImage(titleId, rightImgId, listener);
	}

	public void initTopBarForRightText(int titleId, int rightTextId,
			onRightImageButtonClickListener listener) {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.init(HeaderStyle.TITLE_RIGHT_BUTTON);
		mHeaderLayout.setTitleAndRightText(titleId, R.color.transparent, rightTextId,
				listener);
	}
	
	public void initTopBarForLeftImage(int titleId, int leftResId, onLeftImageButtonClickListener listener) {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.init(HeaderStyle.TITLE_LIFT_IMAGEBUTTON);
		mHeaderLayout.setTitleAndLeftImage(titleId,	leftResId, listener);
	}
	
	public void initTopBarForLeftBack(int titleId) {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.init(HeaderStyle.TITLE_LIFT_IMAGEBUTTON);
		mHeaderLayout.setTitleAndLeftImage(titleId,
				R.drawable.base_action_bar_back_bg_selector,
				new OnBackBtnClickListener());
	}
	
	
	public void initTopSegmentBarForLeftImageAndRightImage(int leftTitleId, int rightTitleId, int leftImgId, int rightImgId, 
			onSegmentViewClickListener segmentListener, onLeftImageButtonClickListener leftListener, onRightImageButtonClickListener rightListener) {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGE, TittleStyle.SEGMENT);
		mHeaderLayout.setSegmentTitle(leftTitleId,  rightTitleId);
		mHeaderLayout.setSegmentListener(segmentListener);
		mHeaderLayout.setTitleAndLeftImage("",	leftImgId, leftListener);
		mHeaderLayout.setTitleAndRightImage("", rightImgId, rightListener);
	}
	
	/**
	 * 仅初始化头部段选择控件，可与其他initTopBar***共用
	 * @param initTopBarForSegment BaseActivity 
	 * @return void
	 * @param leftTitleId
	 * @param rightTitleId
	 * @param segmentListener
	 */
	public void initTopBarForSegment(int leftTitleId, int rightTitleId, onSegmentViewClickListener segmentListener) {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.setTitleStyle(TittleStyle.SEGMENT);
		mHeaderLayout.setSegmentTitle(leftTitleId,  rightTitleId);
		mHeaderLayout.setSegmentListener(segmentListener);
	}
	
	public void initTopBarForTitle(int tId) {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		mHeaderLayout.setTitleStyle(TittleStyle.TEXT);
		mHeaderLayout.setDefaultTitle(tId);
	}
	
	public SegmentView getSegmentViewFromTitle() {
		if (null == mHeaderLayout) {
			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
		}
		return mHeaderLayout.getSegmentView();
	}

    public void setSegmentBadge(int count, int position) {
    	mHeaderLayout.setSegmentBadge(count, position);
    }
	
	public void setTitle(String title) {
		mHeaderLayout.setDefaultTitle(title);
	}
	
	// 左边按钮的点击事件
	public class OnBackBtnClickListener implements
			onLeftImageButtonClickListener {

		@Override
		public void onClick(View v) {
			finishActivity();
		}
	}
	
	/** 隐藏软键盘
	  * hideSoftInputView
	  * @Title: hideSoftInputView
	  * @Description: TODO
	  * @param  
	  * @return void
	  * @throws
	  */
	public boolean hideSoftInputView() {
		InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null)
				return manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
		return false;
	}
	
	/**
	 * 显示软键盘
	 * @param showSoftInputView BaseActivity 
	 * @return void
	 */
	public boolean showSoftInputView() {
		InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
		if (getCurrentFocus() != null)
			return manager.showSoftInput(getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
		return false;
	}
	
	/**
	 * 切换软键盘
	 */
	public void changeInput() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(arg0, arg1, arg2);
	}
}
