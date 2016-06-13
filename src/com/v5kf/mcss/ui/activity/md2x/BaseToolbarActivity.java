package com.v5kf.mcss.ui.activity.md2x;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.ActionBar;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.v5kf.mcss.R;
import com.v5kf.mcss.ui.activity.ActivityBase;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;

/** 
  * 基类，提供标题Toolbar的各种初始化的函数
  * @ClassName: BaseActivity
  * @author Chyrain
  * @date 2014-12-23 09:46
  */
public abstract class BaseToolbarActivity extends ActivityBase {

	private static final String TAG = "BaseToolbarActivity";
	private Toolbar mToolbar;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setStatusbarColor(UITools.getColor(R.color.main_color_dark));
		setNavigationBarColor(UITools.getColor(R.color.main_color_accent));
	}
	
	@Override
	public void setContentView(@LayoutRes int layoutResID) {
		super.setContentView(layoutResID);
		initToolbar();
	}
	
	@Override
	public void setContentView(View view) {
		super.setContentView(view);
		initToolbar();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home: // Toolbar home点击
	            Logger.d(TAG, "返回按钮点击");
	            onBackPressed();
	            return true;
			default:
				break;
	    }
	  
	    return super.onOptionsItemSelected(item);
	}
	
	private void initToolbar() {
		if (mToolbar == null) {
			mToolbar = (Toolbar) findViewById(R.id.id_common_toolbar);
			
			if (mToolbar != null) {
				mToolbar.setBackgroundColor(UITools.getColor(R.color.base_action_bar_bg));
				setSupportActionBar(mToolbar);
			} else {
				Logger.e(TAG, "[initToolbar] mToolbar is null");
			}
			
			final ActionBar actionBar = getSupportActionBar();
			if (actionBar != null) {
				// 启用home as up, 改变返回按钮颜色为main_color_accent
			    final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
			    upArrow.setColorFilter(getResources().getColor(R.color.main_color_accent), android.graphics.PorterDuff.Mode.SRC_ATOP);
			    actionBar.setHomeAsUpIndicator(upArrow);
			    
			    actionBar.setDisplayHomeAsUpEnabled(true);
			    actionBar.setHomeButtonEnabled(true);
			    actionBar.setDisplayShowHomeEnabled(true);
			}
		}
	}

	/**
	 * 初始化toolbar并设置DisplayHomeAsUpEnabled
	 * @return
	 */
	protected Toolbar getToolbar() {
		if (mToolbar == null) {
			initToolbar();
		}
		
		return mToolbar;
	}
	
	/**
	 * 设置顶部状态栏和toolbar颜色
	 * @param color
	 */
	public void setBarColor(int color) {
		setToolbarColor(color);
		setNavigationBarColor(color);
		setWindowBackground(color);
//		setStatusbarColor(color);
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP) 
	public void setToolbarColor(int color) {
		mToolbar.setBackgroundColor(color);
//		if (android.os.Build.VERSION.SDK_INT >= 21) {
//			Window window = getWindow();
			// 很明显，这两货是新API才有的。
//			window.setStatusBarColor(colorBurn(color));
//			window.setNavigationBarColor(colorBurn(color));
//			window.setStatusBarColor(UITools.getColor(R.color.transparent, this));
//			getWindow()
//		    .getDecorView()
//		    .setSystemUiVisibility(
//		                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//		}
	}
	
	/**
	 * 用bitmap图片主题色设置toolbar
	 * @param bitmap
	 */
	public void setToolbarColor(Bitmap bitmap) {
		Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch swatch=palette.getVibrantSwatch();
                setToolbarColor(swatch.getRgb());
            }
        });
	}
	
	public void hideToolbar() {
		if (getToolbar() != null) {
			getToolbar().setVisibility(View.GONE);
		}
	}
	
	public void showToolbar() {
		if (getToolbar() != null) {
			getToolbar().setVisibility(View.VISIBLE);
		}
	}
	
	protected void initTopBarForOnlyTitle(int titleTextId) {
		if (getToolbar() != null) {
			getToolbar().setTitle(titleTextId);
		}
	}

	protected void initTopBarForOnlyTitle(String title) {
		if (getToolbar() != null) {
			getToolbar().setTitle(title);
		}
	}
	
	protected void initTopBarForLeftBack(int titleId) {
		if (getToolbar() != null) {
			getToolbar().setTitle(titleId);
		}
	}

	protected void initTopBarForLeftBack(String title) {
		if (getToolbar() != null) {
			getToolbar().setTitle(title);
		}
	}
	
//	public void initTopBarForOnlyTitle(String titleName) {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		mHeaderLayout.init(HeaderStyle.DEFAULT_TITLE);
//		mHeaderLayout.setDefaultTitle(titleName);
//	}
//	
//	public void initTopBarForOnlyTitle(int titleTextId) {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		mHeaderLayout.init(HeaderStyle.DEFAULT_TITLE);
//		mHeaderLayout.setDefaultTitle(titleTextId);
//	}
//
//	public void initTopBarForLeftBackAndRightText(int titleName, int rightText,
//			onRightImageButtonClickListener listener) {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGEBUTTON);
//		mHeaderLayout.setTitleAndLeftImage(titleName,
//				R.drawable.base_action_bar_back_bg_selector,
//				new OnBackBtnClickListener());
//		mHeaderLayout.setTitleAndRightText(titleName, R.color.transparent, rightText,
//				listener);
//	}
//	
//	public void initTopBarForLeftBackAndRightImage(int titleId, int rightImgId,
//			onRightImageButtonClickListener listener) {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGE);
//		mHeaderLayout.setTitleAndLeftImage(titleId,
//				R.drawable.base_action_bar_back_bg_selector,
//				new OnBackBtnClickListener());
//		mHeaderLayout.setTitleAndRightImage(titleId, rightImgId, listener);
//	}
//	
//	public void initTopBarForLeftBackAndRightImage(String titleName, int rightImgId,
//			onRightImageButtonClickListener listener) {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGE);
//		mHeaderLayout.setTitleAndLeftImage(titleName,
//				R.drawable.base_action_bar_back_bg_selector,
//				new OnBackBtnClickListener());
//		mHeaderLayout.setTitleAndRightImage(titleName, rightImgId, listener);
//	}
//	
//	public void initTopBarForLeftImageAndRightImage(int titleId, int leftImgId, int rightImgId, 
//			onLeftImageButtonClickListener leftListener, onRightImageButtonClickListener rightListener) {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGE);
//		mHeaderLayout.setTitleAndLeftImage(titleId,	leftImgId, leftListener);
//		mHeaderLayout.setTitleAndRightImage(titleId, rightImgId, rightListener);
//	}
//	
//	public void initTopBarForLeftImageAndRightImage(CharSequence title, int leftImgId, int rightImgId, 
//			onLeftImageButtonClickListener leftListener, onRightImageButtonClickListener rightListener) {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGE);
//		mHeaderLayout.setTitleAndLeftImage(title,	leftImgId, leftListener);
//		mHeaderLayout.setTitleAndRightImage(title, rightImgId, rightListener);
//	}
//	
//	public void initTopBarForLeftImageAndRightText(int titleId, int leftImgId, int rightTextId, 
//			onLeftImageButtonClickListener leftListener, onRightImageButtonClickListener rightListener) {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGEBUTTON);
//		mHeaderLayout.setTitleAndLeftImage(titleId,	leftImgId, leftListener);
//		mHeaderLayout.setTitleAndRightText(titleId, R.color.transparent, rightTextId, rightListener);
//	}
//
//	public void initTopBarForRightImage(int titleId, int rightImgId, onRightImageButtonClickListener listener) {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		mHeaderLayout.init(HeaderStyle.TITLE_RIGHT_IMAGEBUTTON);
//		mHeaderLayout.setTitleAndRightImage(titleId, rightImgId, listener);
//	}
//
//	public void initTopBarForRightText(int titleId, int rightTextId,
//			onRightImageButtonClickListener listener) {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		mHeaderLayout.init(HeaderStyle.TITLE_RIGHT_BUTTON);
//		mHeaderLayout.setTitleAndRightText(titleId, R.color.transparent, rightTextId,
//				listener);
//	}
//	
//	public void initTopBarForLeftImage(int titleId, int leftResId, onLeftImageButtonClickListener listener) {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		mHeaderLayout.init(HeaderStyle.TITLE_LIFT_IMAGEBUTTON);
//		mHeaderLayout.setTitleAndLeftImage(titleId,	leftResId, listener);
//	}
//	
//	public void initTopBarForLeftBack(int titleId) {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		mHeaderLayout.init(HeaderStyle.TITLE_LIFT_IMAGEBUTTON);
//		mHeaderLayout.setTitleAndLeftImage(titleId,
//				R.drawable.base_action_bar_back_bg_selector,
//				new OnBackBtnClickListener());
//	}
//	
//	
//	public void initTopSegmentBarForLeftImageAndRightImage(int leftTitleId, int rightTitleId, int leftImgId, int rightImgId, 
//			onSegmentViewClickListener segmentListener, onLeftImageButtonClickListener leftListener, onRightImageButtonClickListener rightListener) {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		mHeaderLayout.init(HeaderStyle.TITLE_DOUBLE_IMAGE, TittleStyle.SEGMENT);
//		mHeaderLayout.setSegmentTitle(leftTitleId,  rightTitleId);
//		mHeaderLayout.setSegmentListener(segmentListener);
//		mHeaderLayout.setTitleAndLeftImage("",	leftImgId, leftListener);
//		mHeaderLayout.setTitleAndRightImage("", rightImgId, rightListener);
//	}
//	
//	/**
//	 * 仅初始化头部段选择控件，可与其他initTopBar***共用
//	 * @param initTopBarForSegment BaseActivity 
//	 * @return void
//	 * @param leftTitleId
//	 * @param rightTitleId
//	 * @param segmentListener
//	 */
//	public void initTopBarForSegment(int leftTitleId, int rightTitleId, onSegmentViewClickListener segmentListener) {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		mHeaderLayout.setTitleStyle(TittleStyle.SEGMENT);
//		mHeaderLayout.setSegmentTitle(leftTitleId,  rightTitleId);
//		mHeaderLayout.setSegmentListener(segmentListener);
//	}
//	
//	public void initTopBarForTitle(int tId) {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		mHeaderLayout.setTitleStyle(TittleStyle.TEXT);
//		mHeaderLayout.setDefaultTitle(tId);
//	}
//	
//	public SegmentView getSegmentViewFromTitle() {
//		if (null == mHeaderLayout) {
//			mHeaderLayout = (HeaderLayout) findViewById(R.id.common_actionbar);
//		}
//		return mHeaderLayout.getSegmentView();
//	}
//
//    public void setSegmentBadge(int count, int position) {
//    	mHeaderLayout.setSegmentBadge(count, position);
//    }
//	
//	public void setTitle(String title) {
//		mHeaderLayout.setDefaultTitle(title);
//	}
//	
//	// 左边按钮的点击事件
//	public class OnBackBtnClickListener implements
//			onLeftImageButtonClickListener {
//
//		@Override
//		public void onClick(View v) {
//			finishActivity();
//		}
//	}
	
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
