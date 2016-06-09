package com.v5kf.mcss.ui.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.nineoldandroids.view.ViewHelper;
import com.v5kf.mcss.R;
import com.v5kf.mcss.utils.Logger;

/**
 * Created by moon.zhong on 2015/2/4.
 */
public class TabView extends LinearLayout implements View.OnClickListener {

	private static final String TAG = "TabView";
    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private PagerAdapter mPagerAdapter;
    private int mChildSize;
    private List<TabItem> mTabItems;
    private OnItemIconTextSelectListener mListener;

    private boolean mClickFlag = false;
    private int mTextSize = 12;
    private int mTextColorSelect = 0xff1881d3;
    private int mTextColorNormal = 0xff7c7c7e;
    private int mPadding = 10;

    public TabView(Context context) {
        this(context, null);
    }

    public TabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabView(Context context, AttributeSet attrs, int defStyleAttr) {
//		super(context, attrs, defStyleAttr);
        super(context, attrs);
        TypedArray typedArray = getResources().obtainAttributes(attrs, R.styleable.TabView);
        int N = typedArray.getIndexCount();
        for (int i = 0; i < N; i++) {
            switch (typedArray.getIndex(i)) {
                case R.styleable.TabView_text_size:
                    mTextSize = (int) typedArray.getDimension(i, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                            mTextSize, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.TabView_text_normal_color:
                    mTextColorNormal = typedArray.getColor(i, mTextColorNormal);
                    break;
                case R.styleable.TabView_text_select_color:
                    mTextColorSelect = typedArray.getColor(i, mTextColorSelect);
                    break;
                case R.styleable.TabView_item_padding:
                    mPadding = (int) typedArray.getDimension(i, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                            mPadding, getResources().getDisplayMetrics()));
                    break;
            }
        }
        typedArray.recycle();
        mTabItems = new ArrayList<>();
    }

    
	public void setViewPager(final ViewPager mViewPager) {
        if (mViewPager == null) {
            return;
        }
        this.mViewPager = mViewPager;
        this.mPagerAdapter = mViewPager.getAdapter();
        if (this.mPagerAdapter == null) {
            throw new RuntimeException("PagerAdapter is mull");
        }
        this.mChildSize = this.mPagerAdapter.getCount();
        this.mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB) @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                View leftView;
                View rightView;                
                if (positionOffset > 0) {
                	if (!mClickFlag) {
	                    leftView = mViewPager.getChildAt(position);
	                    rightView = mViewPager.getChildAt(position + 1);
	                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
	                    	ViewHelper.setAlpha(leftView, 1 - positionOffset);
	                    	ViewHelper.setAlpha(rightView, positionOffset);
	                	} else {
	                		leftView.setAlpha(1 - positionOffset);
	                		rightView.setAlpha(positionOffset);
	                	}
	                    mTabItems.get(position).setTabAlpha(1 - positionOffset);
	                    mTabItems.get(position + 1).setTabAlpha(positionOffset);
                	}
                } else {
                	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                		ViewHelper.setAlpha(mViewPager.getChildAt(position), 1);
                	} else {
                		mViewPager.getChildAt(position).setAlpha(1);
                	}
                    mTabItems.get(position).setTabAlpha(1 - positionOffset);
                }
                
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageSelected(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageScrollStateChanged(state);
                }
                if (0 == state) { 
                	/* 滑动完成，不在滑动的状态下恢复flag */
                	mClickFlag = false;
                }
                for (TabItem tabItem : mTabItems) {
                    tabItem.setTabAlpha(0);
                }
                mTabItems.get(mViewPager.getCurrentItem()).setTabAlpha(1);
            }
        });
        
        if (mPagerAdapter instanceof OnItemIconTextSelectListener) {
            mListener = (OnItemIconTextSelectListener) mPagerAdapter;
        }else {
            throw new RuntimeException("pageAdapter should implements OnItemIconTextSelectListener");
        }
        initItem();
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener mOnPageChangeListener) {
        this.mOnPageChangeListener = mOnPageChangeListener;
    }

    private void initItem() {
        for (int i = 0; i < mChildSize; i++) {
            TabItem tabItem = new TabItem(getContext());
            LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            tabItem.setPadding(mPadding, mPadding, mPadding, mPadding);
            tabItem.setIconText(mListener.onIconSelect(i), mListener.onTextSelect(i));
            tabItem.setTextSize(mTextSize);
            tabItem.setTextColorNormal(mTextColorNormal);
            tabItem.setTextColorSelect(mTextColorSelect);
            tabItem.setLayoutParams(params);
            tabItem.setTag(i);
            tabItem.setOnClickListener(this);
            mTabItems.add(tabItem);
            addView(tabItem);
        }
    }
    
    public TabItem getTabItem(int location) {
    	return mTabItems.get(location);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    public void setCurrentItem(int position) {
    	if (mViewPager.getCurrentItem() == position) {
            return;
        }
    	for (TabItem tabItem : mTabItems) {
            tabItem.setTabAlpha(0);
        }
        mTabItems.get(position).setTabAlpha(1);
        
        /* 设置flag表示当前为点击切换模式，以取消渐变 */
        mClickFlag = true;
        mViewPager.setCurrentItem(position, false);
    }

    @Override
    public void onClick(View v) {
    	Logger.v(TAG, "TabView onClick:" + v.getId());
        int position = (Integer) v.getTag();
        if (mViewPager.getCurrentItem() == position) {
            return;
        }
        for (TabItem tabItem : mTabItems) {
            tabItem.setTabAlpha(0);
        }
        mTabItems.get(position).setTabAlpha(1);
        
        /* 设置flag表示当前为点击切换模式，以取消渐变 */
        mClickFlag = true;
        mViewPager.setCurrentItem(position, false);
    }

    public interface OnItemIconTextSelectListener {

        int[] onIconSelect(int position);

        String onTextSelect(int position);
    }

	public void setBadgeOnItem(int location) {
		mTabItems.get(location).setBadge(true);
	}
	
	public void clearBadgeOnItem(int location) {
		mTabItems.get(location).setBadge(false);
	}
}
