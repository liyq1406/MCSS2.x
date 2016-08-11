package com.v5kf.mcss.utils.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import com.v5kf.mcss.R;
import com.v5kf.mcss.ui.widget.CircleImageView;
import com.v5kf.mcss.utils.FileUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.V5Size;

public class ImageLoader {
	public static final int IMAGE_MIN_WH = 40; //dp
	public static final int IMAGE_MAX_WH = 220; //dp
	public static final int IMAGE_BASE_WH = 120; //dp
	
	/**
	 * 限定最小最大宽高进行缩放，类似微信图片显示
	 * @param context
	 * @param w
	 * @param h
	 * @return
	 */
	public static V5Size getScaledSize(Context context, int w, int h) {
		// 想要缩放的目标尺寸
		float rW = w;
		float rH = h;
		float wh = UITools.dip2px(context, IMAGE_BASE_WH);
		float max = UITools.dip2px(context, IMAGE_MAX_WH);
		float min = UITools.dip2px(context, IMAGE_MIN_WH);
		Logger.d("ImageLoader", "[getScale] ratio min:" + wh + " max:" + wh);
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可    
		float scale = 1.0f;//scale=1表示不缩放    
		scale = (float) Math.sqrt((wh*wh)/(w*h));
		Logger.d("ImageLoader", "ratio [getScale] :" + scale);
		if (scale <= 0) scale = 1.0f;
				
		rW = (int)(w * scale);
		rH = (int)(h * scale);
		if (rW < rH) {
			if (rW > min && rH > max) {
				rW = rW * (max / rH);
				if (rW < min) {
					rW = min;
				}
				rH = (int)max;
			} else if (rW < min && rH < max) {
				rH = rH * (min / rW);
				if (rH > max) {
					rH = max;
				}
				rW = (int)min;
			} else if (rW < min && rH > max) {
				rW = (int)min;
				rH = (int)max;
			}
		} else if (rW > rH) {
			if (rW < max && rH < min) {
				rW = rW * (min / rH);
				if (rW > max) {
					rW = max;
				}
				rH = (int)min;
			} else if (rW > max && rH > min) {
				rH = rH * (max / rW);
				if (rH < min) {
					rH = min;
				}
				rW = (int)max;
			} else if (rW > max && rH < min) {
				rW = (int)max;
				rH = (int)min;
			}
		}
		// image scale type: centerCrop
		return new V5Size((int)rW, (int)rH);
	}

	public static float getScale(Context context, int w, int h) {
        // 想要缩放的目标尺寸
//        float min = UITools.dip2px(context, IMAGE_MIN_WH);
//        float max = UITools.dip2px(context, IMAGE_MAX_WH);
        float wh = UITools.dip2px(context, IMAGE_BASE_WH);
        float max = UITools.dip2px(context, IMAGE_MAX_WH);
        Logger.d("ImageLoader", "[getScale] ratio min:" + wh + " max:" + wh);
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可    
        float scale = 1.0f;//scale=1表示不缩放    
//        if (w > h) { //如果宽度大的话根据宽度固定大小缩放 [修改]按照宽度缩放
//        	if (w < wh) {
//        		scale = (wh / w); 
//        	} else if (w > wh) {
//        		scale = (wh / w);
//        	}
//        } else if (w < h) { //如果高度高的话根据宽度固定大小缩放
//        	if (h < min) {
//        		scale = (min / h); 
//        	} else if (h > max) {
//        		scale = (h / max);
//        	}
//        }
        scale = (float) Math.sqrt((wh*wh)/(w*h));
        Logger.d("ImageLoader", "ratio [getScale] :" + scale);
        if (scale <= 0) scale = 1.0f;
        if (h * scale > max) {
        	scale = max/h;
        }
        return scale;
	}
	
	private ImageCache memoryCache;
	private FileCache fileCache;
	private static Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	private static Map<ImageView, String> queueViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	private ExecutorService executorService;
	private boolean isSrc;	// src 还是 background
	
	private int mDefaultImg;
	private Context mContext;
	private String mUrl;
	private ImageView mImageView;
	private int mTryTimes = 0;
	private ImageLoaderListener mListener;
	
	public interface ImageLoaderListener {
		public void onSuccess(String url, ImageView imageView, Bitmap bmp);
		public void onFailure(ImageLoader imageLoader, String url, ImageView imageView);
	}

	/**
	 * @param context
	 *            上下文对象
	 * @param flag
	 *            true为source资源，false为background资源
	 */
	public ImageLoader(Context context, boolean srcFlag, int defaultImg, ImageLoaderListener listener) {
		memoryCache = new ImageCache();
		fileCache = new FileCache(context, FileUtil.getImageCachePath(context));
		executorService = Executors.newFixedThreadPool(5);
		isSrc = srcFlag;
		this.mContext = context;
		this.mListener = listener;
		this.mDefaultImg = defaultImg;
	}

	public ImageLoader(Context context, boolean srcFlag, int defaultImg) {
		this(context, srcFlag, defaultImg, null);
	}

	public void DisplayImage(String url, ImageView imageView) {
		mTryTimes++;
		if (null == imageView) {
			if (mListener != null) {
				mListener.onFailure(this, url, imageView);
			}
			return;
		}
		mUrl = url;
		mImageView = imageView;
		if (null == url || url.isEmpty()) {
			if (isSrc)
				imageView.setImageResource(mDefaultImg);
			else
				imageView.setBackgroundResource(mDefaultImg);
			
			checkImageBorder(imageView);
			if (mListener != null) {
				mListener.onFailure(this, url, imageView);
			}
			return;
		} else {
			resetImageBorder(imageView);
		}
		// 取消url编码
//		String u1 = url.substring(0, url.lastIndexOf("/") + 1);
//		String u2 = url.substring(url.lastIndexOf("/") + 1);
//		try {
//			u2 = URLEncoder.encode(u2, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//		url = u1 + u2;
		if (alreadyInQueue(imageView, url)) {
			return;
		}
		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {
//			Logger.i("ImageLoader", "From MemoryCache:" + url);
			if (isSrc) {
				imageView.setImageBitmap(bitmap);
			} else {
				imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}
			if (mListener != null) {
				mListener.onSuccess(url, imageView, bitmap);
			}
		} else {
			queuePhoto(url, imageView);
			if (isSrc)
				imageView.setImageResource(mDefaultImg);
			else
				imageView.setBackgroundResource(mDefaultImg);
			Logger.d("ImageLoader", "DisplayImage -> queuePhoto -> 临时图片");
			checkImageBorder(imageView);
		}
	}
	
	/**
	 * 给默认头像加外边框
	 * @param imageView
	 */
	private void checkImageBorder(ImageView imageView) {
		if (imageView instanceof CircleImageView) {
			if (mDefaultImg == R.drawable.v5_photo_default) {
				((CircleImageView)imageView).setBorderColor(UITools.getColor(R.color.default_phpto_border_green)); // 坐席蓝、绿边框
				((CircleImageView)imageView).setBorderWidth(3);
			} else if (mDefaultImg == R.drawable.v5_photo_default_cstm) {
				((CircleImageView)imageView).setBorderColor(UITools.getColor(R.color.default_phpto_border_blue)); // 坐席蓝、绿边框
				((CircleImageView)imageView).setBorderWidth(3);
			} else {
				((CircleImageView)imageView).setBorderColor(UITools.getColor(R.color.transparent));
				((CircleImageView)imageView).setBorderWidth(0);
			}
		}
	}
	private void resetImageBorder(ImageView imageView) {
		if (imageView instanceof CircleImageView) {
			((CircleImageView)imageView).setBorderColor(UITools.getColor(R.color.transparent));
			((CircleImageView)imageView).setBorderWidth(0);
		}
	}

	private void queuePhoto(String url, ImageView imageView) {
		queueViews.put(imageView, url);
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url) {
		try {
			File f = fileCache.getFile(url);
			
			// 从sd卡
			Bitmap b = onDecodeFile(f);
			if (b != null) {
				Logger.d("ImageLoader", "From FileCache:" + url);
				return b;
			} else { // 判断是否本地路径
//				Bitmap localBmp = DevUtils.ratio(
//						url, 
//						UITools.dip2px(mContext, IMAGE_MIN_WH), 
//						UITools.dip2px(mContext, IMAGE_MAX_WH)); // 压缩宽高
				Bitmap localBmp = BitmapFactory.decodeFile(url);
				if (localBmp != null) {
					Logger.d("ImageLoader", "From localFile:" + url);
					return localBmp;
				}
			}
			
			// 从网络
			Bitmap bitmap = null;
			Logger.d("ImageLoader", "ImageLoader-->download:" + url);
			HttpUtil.CopyStream(url, f);
			
			// 图片角度矫正
			UITools.correctBitmapAngle(f.getAbsolutePath());
			
			bitmap = onDecodeFile(f);

			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static Bitmap onDecodeFile(File f) {
		try {
			return BitmapFactory.decodeStream(new FileInputStream(f));
		} catch (FileNotFoundException e) {
//			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解码图像用来减少内存消耗
	 * 
	 * @param f
	 * @return
	 */
	public Bitmap decodeFile(File f) {
		try {
			// 解码图像大小
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);
			// 找到正确的刻度值，它应该是2的幂。
			final int REQUIRED_SIZE = 70;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	/**
	 * 任务队列
	 * 
	 * @author Scorpio.Liu
	 * 
	 */
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp = getBitmap(photoToLoad.url);
			if (bmp != null) {
				memoryCache.put(photoToLoad.url, bmp);
				Logger.d("ImageLoader", "262 memoryCache put:" + photoToLoad.url);
			} else {
				Logger.w("ImageLoader", "264 getBitmap return null");
			}
			if (imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			Activity a = null;
			try {
				if (mContext instanceof Activity) {
					a = (Activity)mContext;
				} else if (photoToLoad.imageView.getContext() instanceof Activity) {
					a = (Activity)photoToLoad.imageView.getContext();
				} else {
					
				}
			} catch (Exception e) {
				Logger.w("ImageLoader", "photoToLoad:" + e.toString());
				e.printStackTrace();
			}
			if (a != null) {
				a.runOnUiThread(bd);
			} else {
				boolean rst = photoToLoad.imageView.post(bd);
				Logger.i("ImageLoader", "photoToLoad.imageView.post:" + rst);
			}
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url)) {
			Logger.w("ImageLoader", "DisplayImage -> imageViewReused");
			return true;
		}
		return false;
	}

	boolean alreadyInQueue(ImageView imageView, String url) {
		String tag = queueViews.get(imageView);
		if (tag != null && tag.equals(url)) {
			Logger.w("ImageLoader", "DisplayImage -> alreadyInQueue");
			return true;
		}
		return false;
	}

	/**
	 * 显示位图在UI线程
	 * 
	 * @author Scorpio.Liu
	 * 
	 */
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null) {
				if (isSrc)
					photoToLoad.imageView.setImageBitmap(bitmap);
				else
					photoToLoad.imageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
				resetImageBorder(photoToLoad.imageView);
				if (mListener != null) {
					mListener.onSuccess(mUrl, mImageView, bitmap);
				}
			}  else { // 获取图片失败
				if (isSrc)
					photoToLoad.imageView.setImageResource(R.drawable.v5_img_src_error);
				else
					photoToLoad.imageView.setBackgroundResource(R.drawable.v5_img_src_error);
				
				resetImageBorder(photoToLoad.imageView);
				if (mListener != null) {
					mListener.onFailure(ImageLoader.this, mUrl, mImageView);
				}
			}
			queueViews.remove(photoToLoad.imageView);
		}
	}

	/**
	 * 在适当的时机清理图片缓存
	 */
	public void clearCache() {
		ImageCache.clear();
		fileCache.clear();
	}

	/**
	 * 在适当的时机清理内存图片缓存
	 */
	public static void clearMemoryCache() {
		ImageCache.clear();
	}
	
	/**
	 * 缓存Bitmap图片，id可为url
	 * @param bmp
	 * @param id
	 * @throws IOException
	 */
	public void saveImage(Bitmap bmp, String id) throws IOException {
		memoryCache.put(id, bmp);
		File f = fileCache.getFile(id);
		Bitmap b = onDecodeFile(f);
		if (b != null) {
			Logger.d("ImageLoader", "[saveImage] Already in FileCache:" + id);
			return;
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
		    out.flush();
		    out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public int getmTryTimes() {
		return mTryTimes;
	}

	public void setmTryTimes(int mTryTimes) {
		this.mTryTimes = mTryTimes;
	}

	public static Bitmap getBitmap(Context context, String url) {
		ImageCache memoryCache = new ImageCache();
		Bitmap bitmapmm = memoryCache.get(url);
		if (bitmapmm != null) {
			return bitmapmm;
		}
		
		FileCache fileCache = new FileCache(context, FileUtil.getImageCachePath(context));
		try {
			File f = fileCache.getFile(url);
			
			// 从sd卡
			Bitmap b = onDecodeFile(f);
			if (b != null) {
				Logger.d("ImageLoader", "From FileCache:" + url);
				return b;
			} else { // 判断是否本地路径
//				Bitmap localBmp = DevUtils.ratio(
//						url, 
//						UITools.dip2px(mContext, IMAGE_MIN_WH), 
//						UITools.dip2px(mContext, IMAGE_MAX_WH)); // 压缩宽高
				Bitmap localBmp = BitmapFactory.decodeFile(url);
				if (localBmp != null) {
					Logger.d("ImageLoader", "From localFile:" + url);
					return localBmp;
				}
			}
			
			// 从网络
			Bitmap bitmap = null;
			Logger.d("ImageLoader", "ImageLoader-->download:" + url);
			HttpUtil.CopyStream(url, f);
			
			// 图片角度矫正
			UITools.correctBitmapAngle(f.getAbsolutePath());
			
			bitmap = onDecodeFile(f);

			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
