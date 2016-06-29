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
import com.v5kf.mcss.utils.DevUtils;
import com.v5kf.mcss.utils.FileUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;

public class MediaLoader {
	private static final String TAG = "MediaLoader";
	public static final int VIDEO_COVER_MIN_WH = 140; //dp
	public static final int VIDEO_COVER_MAX_WH = 200; //dp
	
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
	 *            涓婁笅鏂囧璞�
	 * @param flag
	 *            true涓簊ource璧勬簮锛宖alse涓篵ackground璧勬簮
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
	 * 鍔犺浇濯掍綋(鏈湴璺緞銆佺綉缁滆矾寰�)
	 * @param url
	 */
	public void loadMedia(String url, V5Message message, MediaLoaderListener listener) {
		mTryTimes++;
		if (listener != null) {
			this.mListener = listener;
		}
		this.mUrl = url;
//		this.mMessage = message;
		// 鍙栨秷url缂栫爜
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
			File f = fileCache.getFile(url); // amr鏂囦欢
			
			// 浠巗d鍗¤幏鍙�
			if (f.exists()) {
				Logger.d(TAG, "From FileCache:" + url);
				return getMediaCacheFromFile(f, message);
			} else { // 鍒ゆ柇鏄惁鏈湴璺緞
				File path = new File(url);
				if (path.exists()) {
					Logger.d(TAG, "From LocalFile:" + url);
					return getMediaCacheFromFile(path, message);
				}
			}
			
			// 浠庣綉缁�
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
				// 鑾峰緱cover fram Bitmap
				MediaMetadataRetriever mediaDataRet = new MediaMetadataRetriever();
				mediaDataRet.setDataSource(f.getAbsolutePath());
				Bitmap bitmap = mediaDataRet.getFrameAtTime();
				Logger.d(TAG, UITools.dip2px(mContext, VIDEO_COVER_MIN_WH) + "ratio 放大前视频缩略图:" + bitmap.getWidth() +" "+ bitmap.getHeight());
//				if (bitmap.getWidth() < UITools.dip2px(mContext, VIDEO_COVER_MIN_WH) || bitmap.getHeight() < UITools.dip2px(mContext, VIDEO_COVER_MIN_WH)) {
//					float scale1 = UITools.dip2px(mContext, VIDEO_COVER_MIN_WH) / bitmap.getWidth();
//					float scale2 = UITools.dip2px(mContext, VIDEO_COVER_MIN_WH) / bitmap.getHeight();
//					bitmap = DevUtils.ratio(bitmap, scale1 > scale2 ? scale1 : scale2);
//					Logger.d(TAG, UITools.dip2px(mContext, VIDEO_COVER_MIN_WH) + "ratio 放大后视频缩略图:" + bitmap.getWidth() +" "+ bitmap.getHeight());
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
	 * 浠诲姟闃熷垪
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
			Activity a = (Activity) mContext;
			a.runOnUiThread(md);
		}
	}
	
	/**
	 * 鏄剧ず浣嶅浘鍦║I绾跨▼
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
			} else { // 鑾峰彇澶辫触
				FileUtil.deleteFile(fileCache.getFile(photoToLoad.url).getAbsolutePath());
				if (mListener != null) {
					mListener.onFailure(MediaLoader.this, photoToLoad.mMessage, mObj);
				}
			}
		}
	}

	/**
	 * 鍦ㄩ�傚綋鐨勬椂鏈烘竻鐞嗗浘鐗囩紦瀛�
	 */
	public void clearCache() {
		mediaCaches.clear();
		fileCache.clear();
	}

	/**
	 * 鍦ㄩ�傚綋鐨勬椂鏈烘竻鐞嗗唴瀛樺浘鐗囩紦瀛�
	 */
	public static void clearMemoryCache() {
		mediaCaches.clear();
	}
	
	/**
	 * 缂撳瓨Bitmap鍥剧墖锛宨d鍙负url
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
		File f = cache.getFile(url); // 缂撳瓨鐨刟mr鏂囦欢
		src.renameTo(f);
	}
}
