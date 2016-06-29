package com.v5kf.mcss.ui.view;

import pl.tajchert.sample.DotsTextView;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.LoadingLayoutBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.nineoldandroids.view.ViewHelper;
import com.v5kf.mcss.R;

/**
 * Created by zwenkai on 2015/12/19.
 */
public class V5RefreshLayout extends LoadingLayoutBase {

    static final String LOG_TAG = "PullToRefresh-JingDongHeaderLayout";

    private ViewGroup mInnerLayout;

    private final TextView mSubHeaderText;
    private final ProgressBar mRefreshPb;
    private final ImageView mRefreshImg;
    private final DotsTextView mRefreshDots;

    private CharSequence mPullLabel;
    private CharSequence mRefreshingLabel;
    private CharSequence mReleaseLabel;
    private CharSequence mFinishLabel;

    private AnimationDrawable animP;

    public V5RefreshLayout(Context context) {
        this(context, PullToRefreshBase.Mode.PULL_FROM_START);
    }

    public V5RefreshLayout(Context context, PullToRefreshBase.Mode mode) {
        super(context);

        LayoutInflater.from(context).inflate(R.layout.v5_header_loadinglayout, this);

        mInnerLayout = (ViewGroup) findViewById(R.id.fl_inner);
        mSubHeaderText = (TextView) mInnerLayout.findViewById(R.id.pull_to_refresh_sub_text);
        mRefreshImg = (ImageView) mInnerLayout.findViewById(R.id.pull_to_refresh_img);
        mRefreshPb = (ProgressBar) mInnerLayout.findViewById(R.id.pull_to_refresh_pb);
        mRefreshDots = (DotsTextView) mInnerLayout.findViewById(R.id.pull_to_refresh_dots);

        LayoutParams lp = (LayoutParams) mInnerLayout.getLayoutParams();
        lp.gravity = mode == PullToRefreshBase.Mode.PULL_FROM_END ? Gravity.TOP : Gravity.BOTTOM;

        // Load in labels
        if (mode == PullToRefreshBase.Mode.PULL_FROM_START) {
        	mPullLabel = context.getString(R.string.pull_to_refresh_pulldown);
        	mRefreshingLabel = context.getString(R.string.pull_to_refresh_refreshing);
        	mReleaseLabel = context.getString(R.string.pull_to_refresh_release);
        	mFinishLabel = context.getString(R.string.pull_to_refresh_finish);
        } else {
        	mPullLabel = context.getString(R.string.pull_to_refresh_pullup);
        	mRefreshingLabel = context.getString(R.string.pull_up_refresh_refreshing);
        	mReleaseLabel = context.getString(R.string.pull_up_refresh_release);
        	mFinishLabel = context.getString(R.string.pull_up_refresh_finish);
        }
        reset();
    }

    @Override
    public final int getContentSize() {
        return mInnerLayout.getHeight();
    }

    @Override
    public final void pullToRefresh() {
        mSubHeaderText.setText(mPullLabel);
    }

    @Override
    public final void onPull(float scaleOfLayout) {
    	//Logger.d("V5RefreshLayout", "onPull:" + scaleOfLayout);
        scaleOfLayout = scaleOfLayout > 1.0f ? 1.0f : scaleOfLayout;

        if (mRefreshImg.getVisibility() != View.VISIBLE) {
        	mRefreshImg.setVisibility(View.VISIBLE);
        }

        //透明度动画
        ObjectAnimator animAlphaP = ObjectAnimator.ofFloat(mRefreshImg, "alpha", -1, 1).setDuration(300);
        animAlphaP.setCurrentPlayTime((long) (scaleOfLayout * 300));

        //缩放动画
        ViewHelper.setPivotX(mRefreshImg, 0);  // 设置中心点
        ViewHelper.setPivotY(mRefreshImg, 0);
        ObjectAnimator animPX = ObjectAnimator.ofFloat(mRefreshImg, "scaleX", 0, 1).setDuration(300);
        animPX.setCurrentPlayTime((long) (scaleOfLayout * 300));
        ObjectAnimator animPY = ObjectAnimator.ofFloat(mRefreshImg, "scaleY", 0, 1).setDuration(300);
        animPY.setCurrentPlayTime((long) (scaleOfLayout * 300));

//        ViewHelper.setPivotX(mRefreshPb, mRefreshPb.getMeasuredWidth());
//        ObjectAnimator animGX = ObjectAnimator.ofFloat(mRefreshPb, "scaleX", 0, 1).setDuration(300);
//        animGX.setCurrentPlayTime((long) (scaleOfLayout * 300));
//        ObjectAnimator animGY = ObjectAnimator.ofFloat(mRefreshPb, "scaleY", 0, 1).setDuration(300);
//        animGY.setCurrentPlayTime((long) (scaleOfLayout * 300));
    }

    @Override
    public final void refreshing() {
        mSubHeaderText.setText(mRefreshingLabel);

//        if (animP == null) {
//        	mRefreshPb.setImageResource(R.drawable.refreshing_anim);
//            animP = (AnimationDrawable) mPersonImage.getDrawable();
//        }
//        animP.start();
//        mRefreshPb.
        if (mRefreshImg.getVisibility() == View.VISIBLE) {
        	mRefreshImg.setVisibility(View.GONE);
        }
        if (mRefreshPb.getVisibility() != View.VISIBLE) {
        	mRefreshPb.setVisibility(View.VISIBLE);
        }
        if (mRefreshDots.getVisibility() != View.VISIBLE) {
        	mRefreshDots.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public final void releaseToRefresh() {
        mSubHeaderText.setText(mReleaseLabel);
    }

    @Override
    public final void reset() {
        if (animP != null) {
            animP.stop();
            animP = null;
        }
        mSubHeaderText.setText(mFinishLabel);
        if (mRefreshPb.getVisibility() == View.VISIBLE) {
        	mRefreshPb.setVisibility(View.GONE);
        }
        if (mRefreshDots.getVisibility() == View.VISIBLE) {
        	mRefreshDots.setVisibility(View.INVISIBLE);
        }
        if (mRefreshImg.getVisibility() != View.VISIBLE) {
        	mRefreshImg.setVisibility(View.VISIBLE);
        }

        //透明度动画
        ObjectAnimator animAlphaP = ObjectAnimator.ofFloat(mRefreshImg, "alpha", 1, -1).setDuration(300);
        //animAlphaP.setCurrentPlayTime((long) (1.0 * 300));

        //缩放动画
        ViewHelper.setPivotX(mRefreshImg, 0);  // 设置中心点
        ViewHelper.setPivotY(mRefreshImg, 0);
        ObjectAnimator animPX = ObjectAnimator.ofFloat(mRefreshImg, "scaleX", 1, 0).setDuration(300);
        //animPX.setCurrentPlayTime((long) (1.0 * 300));
        ObjectAnimator animPY = ObjectAnimator.ofFloat(mRefreshImg, "scaleY", 1, 0).setDuration(300);
        //animPY.setCurrentPlayTime((long) (1.0 * 300));
        
        animAlphaP.start();
        animPX.start();
        animPY.start();
    }

    @Override
    public void setLastUpdatedLabel(CharSequence label) {
        mSubHeaderText.setText(label);
    }

    @Override
    public void setPullLabel(CharSequence pullLabel) {
        mPullLabel = pullLabel;
    }

    @Override
    public void setRefreshingLabel(CharSequence refreshingLabel) {
        mRefreshingLabel = refreshingLabel;
    }

    @Override
    public void setReleaseLabel(CharSequence releaseLabel) {
        mReleaseLabel = releaseLabel;
    }

    @Override
    public void setTextTypeface(Typeface tf) {
        mSubHeaderText.setTypeface(tf);
    }
}