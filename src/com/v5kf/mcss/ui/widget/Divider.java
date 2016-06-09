package com.v5kf.mcss.ui.widget;

import com.v5kf.mcss.utils.DevUtils;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by moon.zhong on 2015/2/5.
 */
public class Divider extends RecyclerView.ItemDecoration {

	private int mPaddingLeft = 68;
    private Paint mPaint;
    private int mColor = 0xFFE5E5E6;
    private int mHeight = 1; //mHeight = Utils.dp2px(0.5f);
    private final Rect mRect = new Rect();
    
    public Divider() {
        mPaint = new Paint();
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public Divider(int color, int paddingLeft) {
    	mPaint = new Paint();
    	mColor = color;
    	mPaddingLeft = paddingLeft;
    	mPaint.setColor(mColor);
    	mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft() + DevUtils.dp2px(mPaddingLeft);
        int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        int N = parent.getChildCount();
        for(int i = 0 ; i < N ; i ++){
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams)child .getLayoutParams();
            int top = child.getBottom() + layoutParams.bottomMargin + Math.round(ViewCompat.getTranslationX(child));
            int bottom = top + mHeight;
            mRect.set(left, top, right, bottom);
            c.drawRect(mRect, mPaint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0, 0, 0, mHeight);
    }
}
