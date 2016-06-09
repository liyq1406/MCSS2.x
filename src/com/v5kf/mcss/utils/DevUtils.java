package com.v5kf.mcss.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.v5kf.client.lib.Logger;
import com.v5kf.mcss.CustomApplication;

public class DevUtils {

	// 记录屏幕的高度、宽度、密度等信息。
	public static int screenH;
	public static int screenW;
	public static float screenDensity; // 屏幕密度（0.75 / 1.0 / 1.5）
	public static int screenDensityDpi; // 屏幕密度DPI（120 / 160 / 240）
	public static int statusBarHeight; // 状态栏高度

	/**
     * The context.
     */
    private static Context mContext = CustomApplication.getInstance();

    /**
     * dp转 px.
     *
     * @param value the value
     * @return the int
     */
    public static int dp2px(float value) {
        final float scale = mContext.getResources().getDisplayMetrics().densityDpi;
        return (int) (value * (scale / 160) + 0.5f);
    }

    /**
     * dp转 px.
     *
     * @param value   the value
     * @param context the context
     * @return the int
     */
    public static int dp2px(float value, Context context) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (int) (value * (scale / 160) + 0.5f);
    }

    /**
     * px转dp.
     *
     * @param value the value
     * @return the int
     */
    public static int px2dp(float value) {
        final float scale = mContext.getResources().getDisplayMetrics().densityDpi;
        return (int) ((value * 160) / scale + 0.5f);
    }

    /**
     * px转dp.
     *
     * @param value   the value
     * @param context the context
     * @return the int
     */
    public static int px2dp(float value, Context context) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (int) ((value * 160) / scale + 0.5f);
    }

    /**
     * sp转px.
     *
     * @param value the value
     * @return the int
     */
    public static int sp2px(float value) {
        Resources r;
        if (mContext == null) {
            r = Resources.getSystem();
        } else {
            r = mContext.getResources();
        }
        float spvalue = value * r.getDisplayMetrics().scaledDensity;
        return (int) (spvalue + 0.5f);
    }

    /**
     * sp转px.
     *
     * @param value   the value
     * @param context the context
     * @return the int
     */
    public static int sp2px(float value, Context context) {
        Resources r;
        if (context == null) {
            r = Resources.getSystem();
        } else {
            r = context.getResources();
        }
        float spvalue = value * r.getDisplayMetrics().scaledDensity;
        return (int) (spvalue + 0.5f);
    }

    /**
     * px转sp.
     *
     * @param value the value
     * @return the int
     */
    public static int px2sp(float value) {
        final float scale = mContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (value / scale + 0.5f);
    }

    /**
     * px转sp.
     *
     * @param value   the value
     * @param context the context
     * @return the int
     */
    public static int px2sp(float value, Context context) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (value / scale + 0.5f);
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
	 * 得到设备的密度
	 */
	public static float getScreenDensity(Context context) {
		return context.getResources().getDisplayMetrics().density;
	}

	/**
	 * 把密度转换为像素
	 */
	public static int dp2px(Context context, float px) {
		final float scale = getScreenDensity(context);
		return (int) (px * scale + 0.5);
	}
	
	// 获取屏幕的高度和宽度
	public static int getScreenW(Activity mActivity) {
		if (screenW == 0) {
			DisplayMetrics metric = new DisplayMetrics();
			mActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
			screenW = metric.widthPixels; // 屏幕宽度（像素）
			screenH = metric.heightPixels; // 屏幕高度（像素）
			screenDensity = metric.density; // 屏幕密度（0.75 / 1.0 / 1.5）
			screenDensityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 /
		}
		return screenW;
	}

	public static int getScreenH(Activity mActivity) {
		if (screenH == 0) {
			DisplayMetrics metric = new DisplayMetrics();
			mActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
			screenW = metric.widthPixels; // 屏幕宽度（像素）
			screenH = metric.heightPixels; // 屏幕高度（像素）
			screenDensity = metric.density; // 屏幕密度（0.75 / 1.0 / 1.5）
			screenDensityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 /
		}
		return screenH;
	}


	/**
	 * bitmap转byte数组
	 * 
	 * @param bmp
	 * @param needRecycle
	 * @return
	 */
	public static byte[] bmpToByteArray(final Bitmap bmp,
			final boolean needRecycle) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.PNG, 100, output);
		if (needRecycle) {
			bmp.recycle();
		}

		byte[] result = output.toByteArray();
		try {
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 实现文本复制功能 add by lif
	 * 
	 * @param content
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi") 
	public static void copy(String content, Context context) {
		// 得到剪贴板管理器
		ClipboardManager cmb = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
//		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
//			ClipData clip = ClipData.newPlainText("", content);
//			cmb.setPrimaryClip(clip);
//		} else {
//			cmb.setText(content.trim());
//		}
		cmb.setText(content.trim());
	}

	/**
	 * 实现粘贴功能 add by lif
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi") 
	public static String paste(Context context) {
		// 得到剪贴板管理器
		ClipboardManager cmb = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
		String result = "";
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
			ClipData clipData = cmb.getPrimaryClip();
            int count = clipData.getItemCount();

            for (int i = 0; i < count; ++i) {
                ClipData.Item item = clipData.getItemAt(i);
                CharSequence str = item
                        .coerceToText(context);
                result += str;
            }
		} else {
			result = cmb.getText().toString().trim();
		}
		
		return result;
	}

	/**
	 * 隐藏软键盘
	 */
	public static void hideSoftInputMethod(Activity act) {
		View view = act.getWindow().peekDecorView();
		if (view != null) {
			// 隐藏虚拟键盘
			InputMethodManager inputmanger = (InputMethodManager) act
					.getSystemService(Activity.INPUT_METHOD_SERVICE);
			inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	/**
	 * 切换软件盘 显示隐藏
	 */
	public static void switchSoftInputMethod(Activity act) {
		// 方法一(如果输入法在窗口上已经显示，则隐藏，反之则显示)
		InputMethodManager imm = (InputMethodManager) act
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	/**
	 * 验证是否手机号码
	 * 
	 * @param mobiles
	 * @return
	 */
	public static boolean isMobileNO(String mobiles) {
		Pattern p = Pattern
				.compile("^((13[0-9])|(15[^4,//D])|(18[0,5-9]))//d{8}$");
		Matcher m = p.matcher(mobiles);
		System.out.println(m.matches() + "---");
		return m.matches();
	}

	/**
	 * 中文识别
	 * 
	 */
	public static boolean hasChinese(String source) {
		String reg_charset = "([\\u4E00-\\u9FA5]*+)";
		Pattern p = Pattern.compile(reg_charset);
		Matcher m = p.matcher(source);
		boolean hasChinese = false;
		while (m.find()) {
			if (!"".equals(m.group(1))) {
				hasChinese = true;
			}
		}
		return hasChinese;
	}

	/**
	 * 用户名规则判断
	 * 
	 * @param uname
	 * @return
	 */
	public static boolean isAccountStandard(String uname) {
		Pattern p = Pattern.compile("[A-Za-z0-9_]+");
		Matcher m = p.matcher(uname);
		return m.matches();
	}

	// java 合并两个byte数组
	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
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
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * 将px值转换为sp值，保证文字大小不变
	 * 
	 * @param pxValue
	 * @param fontScale
	 *            （DisplayMetrics类中属性scaledDensity）
	 * @return
	 */
	public static int px2sp(Context context, float pxValue) {
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
     * 
     * unicode 转换成 中文
     * @param theString
     * @return
     */
    public static String decodeUnicode(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        
        for (int x = 0; x < len;) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            value = (value << 4) + aChar - '0';
                            break;
                        case 'a':
                        case 'b':
                        case 'c':
                        case 'd':
                        case 'e':
                        case 'f':
                            value = (value << 4) + 10 + aChar - 'a';
                            break;
                        case 'A':
                        case 'B':
                        case 'C':
                        case 'D':
                        case 'E':
                        case 'F':
                            value = (value << 4) + 10 + aChar - 'A';
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    "Malformed   \\uxxxx   encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
            	outBuffer.append(aChar);
        	}
        return outBuffer.toString();
    }
    
    /** 检查SD卡是否存在 */
	public static boolean checkSdCard() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED))
			return true;
		else
			return false;
	}
	
//	/**
//	 * 修改wifi设置，需要系统级权限！
//	 * @param WifiNeverDormancy DevUtils 
//	 * @return void
//	 * @param mContext
//	 */
//	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) 
//	public static void WifiNeverDormancy(Context mContext) { 
//		ContentResolver resolver = mContext.getContentResolver();
//		int value = 0;
//		if (Build.VERSION.SDK_INT < 17) {
//			value = Settings.System.getInt(resolver, Settings.System.WIFI_SLEEP_POLICY,  Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
//		} else {
//			value = Settings.System.getInt(resolver, Settings.Global.WIFI_SLEEP_POLICY,  Settings.Global.WIFI_SLEEP_POLICY_DEFAULT);
//		}
//		 
//		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);	    
//		Editor editor = prefs.edit(); 
//		editor.putInt(mContext.getString(R.string.wifi_sleep_policy_default), value);	 
//		editor.commit();
//		
//		if (Build.VERSION.SDK_INT < 17) {
//			if(Settings.System.WIFI_SLEEP_POLICY_NEVER != value) { 
//				Settings.System.putInt(resolver, Settings.System.WIFI_SLEEP_POLICY, Settings.System.WIFI_SLEEP_POLICY_NEVER); 
//			}
//		} else {
//			if(Settings.Global.WIFI_SLEEP_POLICY_NEVER != value) { 
//				Settings.Global.putInt(resolver, Settings.Global.WIFI_SLEEP_POLICY, Settings.Global.WIFI_SLEEP_POLICY_NEVER); 
//			}
//		}
//		Logger.d("DevUtils", "wifi value:" + value); 
//	}
//	
//	/**
//	 * 修改wifi设置，需要系统级权限！
//	 * @param restoreWifiDormancy DevUtils 
//	 * @return void
//	 * @param mContext
//	 */
//	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) 
//	public static void restoreWifiDormancy(Context mContext) {
//		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
//		if (Build.VERSION.SDK_INT < 17) {
//			int defaultPolicy = prefs.getInt(mContext.getString(R.string.wifi_sleep_policy_default), Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
//			Settings.System.putInt(mContext.getContentResolver(), Settings.System.WIFI_SLEEP_POLICY, defaultPolicy);
//		} else {
//			int defaultPolicy = prefs.getInt(mContext.getString(R.string.wifi_sleep_policy_default), Settings.Global.WIFI_SLEEP_POLICY_DEFAULT);
//			Settings.System.putInt(mContext.getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY, defaultPolicy);
//		}
//	}
	
	/**
	 * 压缩Bitmap图片
	 * @param image
	 * @return
	 */
	public static Bitmap compressBitmap(Bitmap image) {
	    while (true) {
	    	float width = image.getWidth();
	      	float height = image.getHeight();
	      	float max = width > height ? width : height;
	      	float minSize = 1024.0F / max;
	      	if ((width <= 1024.0F) && (height <= 1024.0F))
	    	  	break;
	      	Matrix matrix = new Matrix();
	
	      	matrix.postScale(minSize, minSize);
	      	image = Bitmap.createBitmap(image, 0, 0, (int)width, (int)height, matrix, true);
	    }
	
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
	    int options = 80;
	    while (bos.toByteArray().length / 1024 > 150) {
	    	bos.reset();
	      	image.compress(Bitmap.CompressFormat.JPEG, options, bos);
	      	options -= 20;
	    }
	    ByteArrayInputStream isBm = new ByteArrayInputStream(bos.toByteArray());
	    return BitmapFactory.decodeStream(isBm);
	}
	
	/** 
     * Compress image by pixel, this will modify image width/height.  
     * Used to get thumbnail 
     *  
     * @param imgPath image path 
     * @param pixelW target pixel of width 
     * @param pixelH target pixel of height 
     * @return 
     */  
    public static Bitmap ratio(String imgPath, float pixelW, float pixelH) {
    	Logger.d("DevUtil", "ratio--> w:" + pixelW + " h:" + pixelH);
        BitmapFactory.Options newOpts = new BitmapFactory.Options();    
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true，即只读边不读内容  
        newOpts.inJustDecodeBounds = true;  
        newOpts.inPreferredConfig = Config.RGB_565;
        // Get bitmap info, but notice that bitmap is null now    
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, newOpts);  
            
        newOpts.inJustDecodeBounds = false;    
        int w = newOpts.outWidth;    
        int h = newOpts.outHeight;    
        // 想要缩放的目标尺寸
        float hh = pixelH; // 设置高度为240f时，可以明显看到图片缩小了  
        float ww = pixelW; // 设置宽度为120f，可以明显看到图片缩小了  
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可    
        int be = 1;//be=1表示不缩放    
        if (w > h && w > ww) { //如果宽度大的话根据宽度固定大小缩放    
            be = (int) (newOpts.outWidth / ww);    
        } else if (w < h && h > hh) { //如果高度高的话根据宽度固定大小缩放    
            be = (int) (newOpts.outHeight / hh);    
        }    
        if (be <= 0) be = 1;    
        newOpts.inSampleSize = be; //设置缩放比例  
        // 开始压缩图片，注意此时已经把options.inJustDecodeBounds 设回false了  
        bitmap = BitmapFactory.decodeFile(imgPath, newOpts);  
        return bitmap;
    }
    
    /**
     * 获得当前进程名
     * @param context
     * @return
     */
    String getCurProcessName(Context context) {
    	int pid = android.os.Process.myPid();
    	ActivityManager mActivityManager = (ActivityManager) context
    			.getSystemService(Context.ACTIVITY_SERVICE);
    	for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
    			.getRunningAppProcesses()) {
    		if (appProcess.pid == pid) {
    			return appProcess.processName;
    		}
    	}
    	return null;
	}
}
