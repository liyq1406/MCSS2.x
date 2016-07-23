package com.v5kf.mcss.utils.cache;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.v5kf.client.lib.V5Util;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MessageDefine;
import com.v5kf.client.lib.entity.V5MusicMessage;
import com.v5kf.client.lib.entity.V5VideoMessage;
import com.v5kf.client.lib.entity.V5VoiceMessage;
import com.v5kf.mcss.utils.FileUtil;
import com.v5kf.mcss.utils.Logger;

public class MediaLoader {
	private static final String TAG = "MediaLoader";
	public static final int VIDEO_MIN_WH = 180; //dp
	public static final int VIDEO_MAX_WH = 240; //dp
	
	private FileCache fileCache;
	private ExecutorService executorService;
	private static Map<String, MediaCache> mediaCaches = Collections
			.synchronizedMap(new WeakHashMap<String, MediaCache>());
	
//	private V5Message mMessage;
	private String mUrl;
	private int mTryTimes = 0;
	private MediaLoaderListener mListener;
	private Context mContext;
	private Object mObj;
	
	public interface MediaLoaderListener {
		public void onSuccess(V5Message msg, Object obj, MediaCache media);
		public void onFailure(MediaLoader mediaLoader, V5Message msg, Object obj);
	}

	/**
	 * @param context
	 *            上下文对豿
	 * @param flag
	 *            true为source资源，false为background资源
	 */
	public MediaLoader(Context context, Object obj, MediaLoaderListener listener) {
		fileCache = new FileCache(context, FileUtil.getMediaCachePath(context));
		executorService = Executors.newFixedThreadPool(5);
		this.mListener = listener;
		this.mContext = context;
		this.mObj = obj;
	}

	public MediaLoader(Context context) {
		this(context, null, null);
		this.mContext = context;
	}

	/**
	 * 加载媒体(本地路径、网络路徿)
	 * @param url
	 */
	public void loadMedia(String url, V5Message message, MediaLoaderListener listener) {
		mTryTimes++;
		if (listener != null) {
			this.mListener = listener;
		}
		this.mUrl = url;
//		this.mMessage = message;
		// 取消url编码
//		String u1 = url.substring(0, url.lastIndexOf("/") + 1);
//		String u2 = url.substring(url.lastIndexOf("/") + 1);
//		try {
//			u2 = URLEncoder.encode(u2, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//		url = u1 + u2;
		MediaCache media = mediaCaches.get(url);
		if (media != null) {
			loadMediaCache(media, message);
			Logger.i(TAG, "From MemoryCache:" + url);
			if (mListener != null) {
				mListener.onSuccess(message, mObj, media);
			}
		} else {
			queueMedia(url, message);
		}
	}

	private void queueMedia(String url, V5Message message) {
		PhotoToLoad p = new PhotoToLoad(url, message);
		executorService.submit(new PhotosLoader(p));
	}

	private MediaCache getMediaData(String url, V5Message message) {
		Logger.w(TAG, "MediaLoader-->getMediaData:" + url);
		if (null == url) {
			return null;
		}
		try {
			File f = fileCache.getFile(url); // amr文件
			
			// 从sd卡获叿
			if (f.exists()) {
				Logger.d(TAG, "From FileCache:" + url);
				return getMediaCacheFromFile(f, message);
			} else { // 判断是否本地路径
				File path = new File(url);
				if (path.exists()) {
					Logger.d(TAG, "From LocalFile:" + url);
					return getMediaCacheFromFile(path, message);
				}
			}
			
			// 从网绿
			HttpUtil.CopyStream(url, f);
			
			Logger.d(TAG, "MediaLoader-->download:" + url);
			return getMediaCacheFromFile(f, message);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	private MediaCache getMediaCacheFromFile(File f, V5Message msg) {
		MediaCache media = new MediaCache();
		media.setLocalPath(f.getAbsolutePath());
		
		switch (msg.getMessage_type()) {
		case V5MessageDefine.MSG_TYPE_VOICE:
			((V5VoiceMessage)msg).setFilePath(f.getAbsolutePath());
			try {
				long duration = V5Util.getMediaDuration(f);
				media.setDuration(duration);
				((V5VoiceMessage)msg).setDuration(duration);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case V5MessageDefine.MSG_TYPE_SHORT_VIDEO:
		case V5MessageDefine.MSG_TYPE_VIDEO:
			((V5VideoMessage)msg).setFilePath(f.getAbsolutePath());
			try {
				// 获得cover fram Bitmap
				MediaMetadataRetriever mediaDataRet = new MediaMetadataRetriever();
				mediaDataRet.setDataSource(f.getAbsolutePath());
				Bitmap bitmap = mediaDataRet.getFrameAtTime(0);
//				Logger.d(TAG, UITools.dip2px(mContext, VIDEO_COVER_MIN_WH) + "ratio ؅ճǰ˓Ƶ̵Ôͼ:" + bitmap.getWidth() +" "+ bitmap.getHeight());
//				if (bitmap.getWidth() < UITools.dip2px(mContext, VIDEO_COVER_MIN_WH) || bitmap.getHeight() < UITools.dip2px(mContext, VIDEO_COVER_MIN_WH)) {
//					float scale1 = UITools.dip2px(mContext, VIDEO_COVER_MIN_WH) / bitmap.getWidth();
//					float scale2 = UITools.dip2px(mContext, VIDEO_COVER_MIN_WH) / bitmap.getHeight();
//					bitmap = DevUtils.ratio(bitmap, scale1 > scale2 ? scale1 : scale2);
//					Logger.d(TAG, UITools.dip2px(mContext, VIDEO_COVER_MIN_WH) + "ratio ؅ճ۳˓Ƶ̵Ôͼ:" + bitmap.getWidth() +" "+ bitmap.getHeight());
//				}
				media.setCoverFrame(bitmap);
				((V5VideoMessage)msg).setCoverFrame(bitmap);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case V5MessageDefine.MSG_TYPE_MUSIC:
			((V5MusicMessage)msg).setFilePath(f.getAbsolutePath());
			break;
		}
		
		return media;
	}
	
	private void loadMediaCache(MediaCache media, V5Message msg) {
		switch (msg.getMessage_type()) {
		case V5MessageDefine.MSG_TYPE_VOICE:
			((V5VoiceMessage)msg).setFilePath(media.getLocalPath());
			((V5VoiceMessage)msg).setDuration(media.getDuration());
			break;
		case V5MessageDefine.MSG_TYPE_SHORT_VIDEO:
		case V5MessageDefine.MSG_TYPE_VIDEO:
			((V5VideoMessage)msg).setFilePath(media.getLocalPath());
			((V5VideoMessage)msg).setCoverFrame(media.getCoverFrame());
			break;
		case V5MessageDefine.MSG_TYPE_MUSIC:
			((V5MusicMessage)msg).setFilePath(media.getLocalPath());
			break;
		}
	}

	/**
	 * 任务队列
	 * 
	 * @author Scorpio.Liu
	 * 
	 */
	private class PhotoToLoad {
		public String url;
		public V5Message mMessage;

		public PhotoToLoad(String u, V5Message message) {
			url = u;
			mMessage = message;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			MediaCache media = getMediaData(photoToLoad.url, photoToLoad.mMessage);
			if (media != null) {
				mediaCaches.put(photoToLoad.url, media);
				Logger.d(TAG, "memoryCache put:" + photoToLoad.url);
			}
			
			MediaDisplayer md = new MediaDisplayer(media, photoToLoad);
			if (mContext instanceof Activity) {
				Activity a = (Activity) mContext;
				a.runOnUiThread(md);
			} else {
				if (media != null) {
					if (mListener != null) {
						mListener.onSuccess(photoToLoad.mMessage, mObj, media);
					}
				} else { // 获取失败
					FileUtil.deleteFile(fileCache.getFile(photoToLoad.url).getAbsolutePath());
					if (mListener != null) {
						mListener.onFailure(MediaLoader.this, photoToLoad.mMessage, mObj);
					}
				}
			}
		}
	}
	
	/**
	 * 显示位图在UI线程
	 * 
	 * @author Scorpio.Liu
	 * 
	 */
	class MediaDisplayer implements Runnable {
		MediaCache media;
		PhotoToLoad photoToLoad;

		public MediaDisplayer(MediaCache m, PhotoToLoad p) {
			media = m;
			photoToLoad = p;
		}

		public void run() {
			if (media != null) {
				if (mListener != null) {
					mListener.onSuccess(photoToLoad.mMessage, mObj, media);
				}
			} else { // 获取失败
				FileUtil.deleteFile(fileCache.getFile(photoToLoad.url).getAbsolutePath());
				if (mListener != null) {
					mListener.onFailure(MediaLoader.this, photoToLoad.mMessage, mObj);
				}
			}
		}
	}

	/**
	 * 在鿂当的时机清理图片缓孿
	 */
	public void clearCache() {
		mediaCaches.clear();
		fileCache.clear();
	}

	/**
	 * 在鿂当的时机清理内存图片缓孿
	 */
	public static void clearMemoryCache() {
		mediaCaches.clear();
	}
	
	/**
	 * 缓存Bitmap图片，id可为url
	 * @param bmp
	 * @param id
	 * @throws IOException
	 */
	public void saveMedia(MediaCache media, String id) throws IOException {
		mediaCaches.put(id, media);
	}

	public int getmTryTimes() {
		return mTryTimes;
	}

	public void setmTryTimes(int mTryTimes) {
		this.mTryTimes = mTryTimes;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}
	
	public static void copyPathToFileCche(Context context, File src, String url) {
		FileCache cache = new FileCache(context, FileUtil.getMediaCachePath(context));
		File f = cache.getFile(url); // 缓存的amr文件
		src.renameTo(f);
	}
}
