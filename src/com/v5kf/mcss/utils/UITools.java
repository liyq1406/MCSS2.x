package com.v5kf.mcss.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.CustomerBean;
import com.v5kf.mcss.ui.widget.CircleImageView;

/**
 * dp、sp 转换为 px 的工具类
 * 
 * @author fxsky 2012.11.12
 *
 */
public class UITools {
	
	public static void noticeEndSession(Context context, CustomerBean cstm) {
		String reason = stringOfEndReason(cstm.getClosingReason());
		Toast.makeText(context, "\"" + cstm.getDefaultName() + "\"的会话结束：" + reason,
				Toast.LENGTH_LONG).show();
	}
	
	public static void noticeEndSession(View v,
			CustomerBean cstm) {
		String reason = "\"" + cstm.getDefaultName() + "\"的会话结束：" + stringOfEndReason(cstm.getClosingReason());
		Snackbar.make(v, reason, Snackbar.LENGTH_SHORT).show();
	}
	
	public static int intOfInterfaceType(String type) {
		int iface = 0;
		switch (type) {
		case "web":
			iface = QAODefine.CSTM_IF_WEB;
			break;
		case "weixin":
		case "wechat":
			iface = QAODefine.CSTM_IF_WEIXIN;
			break;
		case "app":
			iface = QAODefine.CSTM_IF_APP;
			break;
		case "wxqy":
			iface = QAODefine.CSTM_IF_WXQY;
			break;
		case "yixin":
			iface = QAODefine.CSTM_IF_YIXIN;
			break;
		case "qq":
			iface = QAODefine.CSTM_IF_QQ;
			break;
		case "sina":
			iface = QAODefine.CSTM_IF_SINA;
			break;
		case "alipay":
			iface = QAODefine.CSTM_IF_ALIPAY;
			break;
		}
		return iface;
	}
	
	public static String stringOfInterface(int iface) {
		String type = "wechat";
		if ((iface & 0x00ff) == 0) {
			iface = iface >> 8;
		}
		switch (iface) {
		case QAODefine.CSTM_IF_APP:
			type = "app"; 
			break;
		case QAODefine.CSTM_IF_OPEN:
			type = "open";
			break;
		case QAODefine.CSTM_IF_WEB:
			type = "web";
			break;
		case QAODefine.CSTM_IF_WEIXIN:
			type = "wechat";
			break;
		case QAODefine.CSTM_IF_QQ:
			type = "qq";
			break;
		case QAODefine.CSTM_IF_ALIPAY:
			type = "alipay";
			break;
		case QAODefine.CSTM_IF_SINA:
			type = "sina";
			break;
		case QAODefine.CSTM_IF_WXQY:
			type = "wxqy";
		case QAODefine.CSTM_IF_YIXIN:
			type = "yixin";
			break;
		default:
			break;
		}
		return type;
	}
	
	public static String stringOfEndReason(int reasonCode) {
		/**
		 * 0 - 原因不明
		 * 1 - 机器人指令
		 * 2 - 被转接
		 * 3 - 取消订阅
		 * 4 - 坐席主动结束
		 * 5 - 会话超时
		 * 6 - 微信多客服接管了
		 * 7 - 服务重启中断
		 */
		String reason = "未知";
		switch (reasonCode) {
		case 0:
			reason = "未知原因";
			break;
		case 1:
			reason = "机器人结束指令";
			break;
		case 2:
			reason = "转接给其他坐席";
			break;
		case 3:
			reason = "客户已取消订阅";
			break;
		case 4:
			reason = "手动结束";
			break;
		case 5:
			reason = "超时自动结束";
			break;
		case 6:
			reason = "微信多客服接管";
			break;
		case 7:
			reason = "服务重启会话中断";
			break;
		}
		return reason;
	}
	
	public static void setStatusTextInfo(int status, TextView statusTv) {
		switch (status) {
    	case QAODefine.STATUS_ONLINE:
    		statusTv.setText(R.string.status_online);
    		statusTv.setTextColor(UITools.getColor(R.color.status_online_color));
    		break;
    	case QAODefine.STATUS_BUSY:
    		statusTv.setText(R.string.status_busy);
    		statusTv.setTextColor(UITools.getColor(R.color.status_busy_color));
    		break;
    	case QAODefine.STATUS_LEAVE:
    		statusTv.setText(R.string.status_leave);
    		statusTv.setTextColor(UITools.getColor(R.color.status_leave_color));
    		break;
    	case QAODefine.STATUS_HIDE:
    	case QAODefine.STATUS_OFFLINE:
    		statusTv.setText(R.string.status_offline);
    		statusTv.setTextColor(UITools.getColor(R.color.status_offline_color));
    		break;
    	}
	}
	
	public static void setDrawerStatusSpinnerBg(int status, View v) {
		switch (status) {
    	case QAODefine.STATUS_ONLINE:
    		v.setBackgroundResource(R.drawable.md2x_drawer_status_online);
    		break;
    	case QAODefine.STATUS_BUSY:
    		v.setBackgroundResource(R.drawable.md2x_drawer_status_busy);
    		break;
    	case QAODefine.STATUS_LEAVE:
    		v.setBackgroundResource(R.drawable.md2x_drawer_status_leave);
    		break;
    	case QAODefine.STATUS_HIDE:
    	case QAODefine.STATUS_OFFLINE:
    		v.setBackgroundResource(R.drawable.md2x_drawer_status_offline);
    		break;
    	}
	}
	
	public static void setInterfaceInfo(int iface, TextView ifaceTv, ImageView ifaceIv) {
		if ((iface & 0x00ff) == 0) {
			iface = iface >> 8;
		}
		switch (iface) {
    	case QAODefine.CSTM_IF_WEIXIN:
    		ifaceTv.setText(R.string.iface_weixin);
    		ifaceIv.setImageResource(R.drawable.v5_iface_weixin);
    		break;
    	case QAODefine.CSTM_IF_YIXIN:
    		ifaceTv.setText(R.string.iface_yixin);
    		ifaceIv.setImageResource(R.drawable.v5_iface_weixin);
    		break;
    	case QAODefine.CSTM_IF_WEB:
    		ifaceTv.setText(R.string.iface_web);
    		ifaceIv.setImageResource(R.drawable.v5_iface_web);
    		break;
    	case QAODefine.CSTM_IF_OPEN:
    		ifaceTv.setText(R.string.iface_open);
    		ifaceIv.setImageResource(R.drawable.v5_iface_web);
    		break;
    	case QAODefine.CSTM_IF_WXQY:
    		ifaceTv.setText(R.string.iface_inc);
    		ifaceIv.setImageResource(R.drawable.v5_iface_inc);
    		break;
    	case QAODefine.CSTM_IF_QQ:
    		ifaceTv.setText(R.string.iface_qq);
    		ifaceIv.setImageResource(R.drawable.v5_iface_qq);
    		break;
    	case QAODefine.CSTM_IF_SINA:
    		ifaceTv.setText(R.string.iface_sina);
    		ifaceIv.setImageResource(R.drawable.v5_iface_sina);
    		break;
    	case QAODefine.CSTM_IF_ALIPAY:
    		ifaceTv.setText(R.string.iface_alipay);
    		ifaceIv.setImageResource(R.drawable.v5_iface_alipay);
    		break;
    	case QAODefine.CSTM_IF_APP:
    		ifaceTv.setText(R.string.iface_app);
    		ifaceIv.setImageResource(R.drawable.v5_iface_app);
    		break;
    	default:
    		ifaceTv.setText(R.string.iface_open);
    		ifaceIv.setImageResource(R.drawable.v5_iface_open);
    		break;
    	}
	}
	
	public static Drawable getDrawable(Context context, int resId) {
		return context.getResources().getDrawable(resId);
	}
	
	/**
	 * 获得color.xml的颜色值
	 * @param resId
	 * @param context
	 * @return
	 */
	@TargetApi(23) 
	public static int getColor(Context context, int resId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return context.getColor(resId);
		} else {
			return context.getResources().getColor(resId);
		}
	}

	/**
	 * 获得color.xml的颜色值
	 * @param resId
	 * @return
	 */
	public static int getColor(int resId) {
		return getColor(CustomApplication.getContext(), resId);
	}
	
	/**
	 * 小米设置状态栏字体颜色
	 * @param darkmode
	 * @param activity
	 */
	public static void setStatusBarDarkModeOfMIUI(boolean darkmode, Activity activity) {
	    Class<? extends Window> clazz = activity.getWindow().getClass();
	    try {
	        int darkModeFlag = 0;
	        Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
	        Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
	        darkModeFlag = field.getInt(layoutParams);
	        Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
	        extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
	    } catch (Exception e) {
	    	Logger.e("MIUI", "setStatusBarDarkIcon: failed");
	        e.printStackTrace();
	    }
	}
	
	/**
	 * 魅族设置状态栏字体颜色
	 * @param window
	 * @param dark
	 * @return
	 */
	public static boolean setStatusBarDarkIconOfFlyme(Window window, boolean dark) {
	    boolean result = false;
	    if (window != null) {
	        try {
	            WindowManager.LayoutParams lp = window.getAttributes();
	            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
	            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
	            darkFlag.setAccessible(true);
	            meizuFlags.setAccessible(true);
	            int bit = darkFlag.getInt(null);
	            int value = meizuFlags.getInt(lp);
	            if (dark) {
	                value |= bit;
	            } else {
	                value &= ~bit;
	            }
	            meizuFlags.setInt(lp, value);
	            window.setAttributes(lp);
	            result = true;
	        } catch (Exception e) {
	            Logger.e("MeiZu", "setStatusBarDarkIcon: failed");
	        }
	    }
	    return result;
	}
	
	/**
	 * 得到设备屏幕的宽度
	 */
	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	/**
	 * 得到设备屏幕的高度
	 */
	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}
	
	/**
	 * 将px值转换为dip或dp值，保证尺寸大小不变
	 * 
	 * @param pxValue
	 * @param scale
	 *            （DisplayMetrics类中属性density）
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 将dip或dp值转换为px值，保证尺寸大小不变
	 * 
	 * @param dipValue
	 * @param scale
	 *            （DisplayMetrics类中属性density）
	 * @return
	 */
	public static float dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (dipValue * scale + 0.5f);
	}

	/**
	 * 将px值转换为sp值，保证文字大小不变
	 * 
	 * @param pxValue
	 * @param fontScale
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static float px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * 将sp值转换为px值，保证文字大小不变
	 * 
	 * @param spValue
	 * @param fontScale
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
	
	/**
	 * 优先加载本地图片路径，然后返回缩略图URL
	 * @param imageMessage
	 * @param siteId
	 * @return
	 */
	public static String getThumbnailUrlOfImage(V5ImageMessage imageMessage, String siteId) {
		String picUrl = imageMessage.getFilePath();
		if (!TextUtils.isEmpty(picUrl)) { // 优先判断本地图片，否则加载网络图片
			return picUrl;
		}

		picUrl = imageMessage.getPic_url();
		if (!TextUtils.isEmpty(picUrl)) {
			if (Config.USE_THUMBNAIL && picUrl.contains("image.myqcloud.com/")) { // 来自万象优图
				picUrl = picUrl + "/thumbnail";
			} else if (Config.USE_THUMBNAIL && 
					(picUrl.contains("mmbiz.qpic.cn/mmbiz/") || picUrl.contains("chat.v5kf.com/"))) {
				picUrl = String.format(Config.APP_PIC_V5_THUMBNAIL_FMT, siteId, imageMessage.getMessage_id());
			}
		} else if (!TextUtils.isEmpty(imageMessage.getMedia_id())) {
			imageMessage.setPic_url(String.format(Config.APP_RESOURCE_V5_FMT, siteId, imageMessage.getMessage_id()));
			picUrl = String.format(Config.USE_THUMBNAIL ? Config.APP_PIC_V5_THUMBNAIL_FMT : Config.APP_RESOURCE_V5_FMT, siteId, imageMessage.getMessage_id());
		}
		
		return picUrl;
	}
	
	/**
     * 矫正拍照返回图片的角度
     * @param path
     */
    public static void correctBitmapAngle(String path) {
    	int degree = getBitmapDegree(path);
    	Logger.d("UIUtil", "[correctBitmapAngle] degree:" + degree);
    	if (degree == 0) {
    		return;
    	}
    	Bitmap bmp = rotateBitmapByDegree(BitmapFactory.decodeFile(path), degree);
    	if (null == bmp) {
    		return;
    	}
    	File file = new File(path);
        try {
        	if (!file.exists()) {
            	file.createNewFile();
            }
        	FileOutputStream fos = new FileOutputStream(file);
			if (null != fos) {
	        	bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
	        	fos.flush();
	        	fos.close();
	        }
		} catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    /**
     * 读取图片的旋转的角度
     *
     * @param path
     *            图片绝对路径
     * @return 图片的旋转角度
     */
    private static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }
    
    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm
     *            需要旋转的图片
     * @param degree
     *            旋转角度
     * @return 旋转后的图片
     */
    private static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
    	if (bm == null) {
    		Logger.w("v5kf-UIUtil", "rotateBitmap failed: src bitmap null");
    		return null;
    	}
        Bitmap returnBm = null;
      
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }
    
    /**
     * 使图片失去饱和度变灰
     * @param iv
     */
    public static void grayImageView(ImageView iv) {
    	if (iv instanceof CircleImageView) {
    		((CircleImageView)iv).setBorderColor(UITools.getColor(R.color.transparent));
    		((CircleImageView)iv).setBorderWidth(0);
    	}
    	Logger.d("DisplayUtil", "[grayImageView] ImageView:" + iv + " drawable:" + iv.getDrawable());
    	Bitmap bmp = ((BitmapDrawable)iv.getDrawable()).getBitmap();
    	iv.setImageBitmap(getGrayBitmap(bmp));
//    	Drawable drawable = iv.getDrawable(); 
////    	iv.setDrawingCacheEnabled(true);
////    	iv.setColorFilter(Color.GRAY, android.graphics.PorterDuff.Mode.MULTIPLY);
//        drawable.mutate();  
//        ColorMatrix cm = new ColorMatrix();  
//        cm.setSaturation(0);       
//        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(cm);       
//        drawable.setColorFilter(cf);  
//        iv.setImageDrawable(drawable);
    }
    
  	/**
  	 * 图片圆角处理  
  	 * @return Bitmap
  	 */
    public static Bitmap getRoundedBitmap(Bitmap mBitmap) {
    	if (null == mBitmap) {
    		return null;
    	}
        //创建新的位图  
        Bitmap bgBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);  
        //把创建的位图作为画板  
        Canvas mCanvas = new Canvas(bgBitmap);  
          
        Paint mPaint = new Paint();  
        Rect mRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());  
        RectF mRectF = new RectF(mRect);  
        //设置圆角半径为20  
        float roundPx = 15;  
        mPaint.setAntiAlias(true);  
        //先绘制圆角矩形  
        mCanvas.drawRoundRect(mRectF, roundPx, roundPx, mPaint);  
          
        //设置图像的叠加模式  
        mPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));  
        //绘制图像  
        mCanvas.drawBitmap(mBitmap, mRect, mRect, mPaint);  
          
        return bgBitmap;  
    }
    
  	/**
  	 * 图片灰化处理  
  	 * @return
  	 */
    public static Bitmap getGrayBitmap(Bitmap mBitmap) {
    	if (null == mBitmap) {
    		return null;
    	}
        Bitmap mGrayBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);  
        Canvas mCanvas = new Canvas(mGrayBitmap);  
        Paint mPaint = new Paint();  
          
        //创建颜色变换矩阵  
        ColorMatrix mColorMatrix = new ColorMatrix();  
        //设置灰度影响范围  
        mColorMatrix.setSaturation(0);  
        //创建颜色过滤矩阵  
        ColorMatrixColorFilter mColorFilter = new ColorMatrixColorFilter(mColorMatrix);  
        //设置画笔的颜色过滤矩阵  
        mPaint.setColorFilter(mColorFilter);  
        //使用处理后的画笔绘制图像  
        mCanvas.drawBitmap(mBitmap, 0, 0, mPaint);  
          
        return mGrayBitmap;           
    }
    
    /**
     * 图片旋转
     * @param mBitmap
     * @param degree
     * @return
     */
    public static Bitmap getRotatedBitmap(Bitmap mBitmap, float degree) {
    	if (null == mBitmap) {
    		return null;
    	}
        int width = mBitmap.getWidth();  
        int height = mBitmap.getHeight();  
          
        Matrix matrix = new Matrix();  
        matrix.preRotate(degree);  
        Bitmap mRotateBitmap = Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, true);  
        return mRotateBitmap;  
    }

}
