package com.v5kf.mcss.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceView;

import com.v5kf.mcss.R;

public class BubbleSurfaceView extends SurfaceView {

	private static final int LOCATION_LEFT = 0;
	private ColorFilter cf;
	private int mAngle = dp2px(10);
	private int mArrowTop = dp2px(40);
	private int mArrowWidth = dp2px(20);
	private int mArrowHeight = dp2px(20);
	private int mArrowOffset = 0;
	private int mArrowLocation = LOCATION_LEFT;

	private Rect mDrawableRect;
	private Paint mPaint;
	
	private RectF mRect;
	private Path mPath;

	public BubbleSurfaceView(Context context) {
		super(context);
		initView(null);
	}

	public BubbleSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(attrs);
	}

	public BubbleSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(attrs);
	}

	private void initView(AttributeSet attrs) {
		if (attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BubbleImageView);
			mAngle = (int) a.getDimension(R.styleable.BubbleImageView_bubble_angle, mAngle);
			mArrowHeight = (int) a.getDimension(R.styleable.BubbleImageView_bubble_arrowHeight, mArrowHeight);
			mArrowOffset = (int) a.getDimension(R.styleable.BubbleImageView_bubble_arrowOffset, mArrowOffset);
			mArrowTop = (int) a.getDimension(R.styleable.BubbleImageView_bubble_arrowTop, mArrowTop);
			mArrowWidth = (int) a.getDimension(R.styleable.BubbleImageView_bubble_arrowWidth, mAngle);
			mArrowLocation = a.getInt(R.styleable.BubbleImageView_bubble_arrowLocation, mArrowLocation);
			a.recycle();
		}
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
//		if (getDrawable() == null) {
//			return;
//		}
//		if (mRect == null) {
			mRect = new RectF(getPaddingLeft(), getPaddingTop(), getRight() - getLeft() - getPaddingRight(), getBottom() - getTop() - getPaddingBottom());
//		}
//		if (mPath == null) {
			mPath = new Path();
//		}

		if (mArrowLocation == LOCATION_LEFT) {
			leftPath(mRect, mPath);
		} else {
			rightPath(mRect, mPath);
		}

		canvas.drawPath(mPath, mPaint);
	}

	public void rightPath(RectF rect, Path path) {
		path.moveTo(mAngle, rect.top);
		path.lineTo(rect.width(), rect.top);
		path.arcTo(new RectF(rect.right - mAngle * 2 - mArrowWidth, rect.top, rect.right - mArrowWidth, mAngle * 2 + rect.top), 270, 90);
		path.lineTo(rect.right - mArrowWidth, mArrowTop);
		path.lineTo(rect.right, mArrowTop - mArrowOffset);
		path.lineTo(rect.right - mArrowWidth, mArrowTop + mArrowHeight);
		path.lineTo(rect.right - mArrowWidth, rect.height() - mAngle);
		path.arcTo(new RectF(rect.right - mAngle * 2 - mArrowWidth, rect.bottom - mAngle * 2, rect.right - mArrowWidth, rect.bottom), 0, 90);
		path.lineTo(rect.left, rect.bottom);
		path.arcTo(new RectF(rect.left, rect.bottom - mAngle * 2, mAngle * 2 + rect.left, rect.bottom), 90, 90);
		path.lineTo(rect.left, rect.top);
		path.arcTo(new RectF(rect.left, rect.top, mAngle * 2 + rect.left, mAngle * 2 + rect.top), 180, 90);
		path.close();
	}

	public void leftPath(RectF rect, Path path) {
		path.moveTo(mAngle + mArrowWidth, rect.top);
		path.lineTo(rect.width(), rect.top);
		path.arcTo(new RectF(rect.right - mAngle * 2, rect.top, rect.right, mAngle * 2 + rect.top), 270, 90);
		path.lineTo(rect.right, rect.top);
		path.arcTo(new RectF(rect.right - mAngle * 2, rect.bottom - mAngle * 2, rect.right, rect.bottom), 0, 90);
		path.lineTo(rect.left + mArrowWidth, rect.bottom);
		path.arcTo(new RectF(rect.left + mArrowWidth, rect.bottom - mAngle * 2, mAngle * 2 + rect.left + mArrowWidth, rect.bottom), 90, 90);
		path.lineTo(rect.left + mArrowWidth, mArrowTop + mArrowHeight);
		path.lineTo(rect.left, mArrowTop - mArrowOffset);
		path.lineTo(rect.left + mArrowWidth, mArrowTop);
		path.lineTo(rect.left + mArrowWidth, rect.top);
		path.arcTo(new RectF(rect.left + mArrowWidth, rect.top, mAngle * 2 + rect.left + mArrowWidth, mAngle * 2 + rect.top), 180, 90);
		path.close();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		setup();
	}

//	@Override
//	public void setImageBitmap(Bitmap bm) {
//		super.setImageBitmap(bm);
//		mBitmap = bm;
//		setup();
//	}
//
//	@Override
//	public void setImageDrawable(Drawable drawable) {
//		super.setImageDrawable(drawable);
//		mBitmap = getBitmapFromDrawable(drawable);
//		setup();
//	}

//	@Override
//	public void setImageResource(int resId) {
//		super.setImageResource(resId);
//		mBitmap = getBitmapFromDrawable(getDrawable());
//		setup();
//	}
//
//	private Bitmap getBitmapFromDrawable(Drawable drawable) {
//		if (drawable == null) {
//			return null;
//		}
//
//		if (drawable instanceof BitmapDrawable) {
//			return ((BitmapDrawable) drawable).getBitmap();
//		}
//
//		try {
//			Bitmap bitmap;
//
//			if (drawable instanceof ColorDrawable) {
//				bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
//			} else {
//				bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
//			}
//
//			Canvas canvas = new Canvas(bitmap);
//			drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
//			drawable.draw(canvas);
//			return bitmap;
//		} catch (OutOfMemoryError e) {
//			return null;
//		}
//	}

	private void setup() {
		mPaint = new Paint();
		if (cf != null) {
			mPaint.setColorFilter(cf);
		}
		mPaint.setAntiAlias(true);
//		mPaint.setShader(mBitmapShader);
		
//		updateShaderMatrix();
		invalidate();
	}

	public void setColorFilter(ColorFilter cf) {
		this.cf = cf;
	}


	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
	}

}
