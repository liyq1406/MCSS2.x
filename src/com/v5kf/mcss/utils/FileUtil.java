package com.v5kf.mcss.utils;

import java.io.File;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;


public class FileUtil { // [修改]修改保存位置，区分外置存储和内置存储

	@TargetApi(Build.VERSION_CODES.KITKAT) 
	public static String getPath(final Context context, final Uri uri) {

	    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

	    // DocumentProvider
	    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
	        // ExternalStorageProvider
	        if (isExternalStorageDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            if ("primary".equalsIgnoreCase(type)) {
	                return Environment.getExternalStorageDirectory() + "/" + split[1];
	            }

	            // TODO handle non-primary volumes
	        }
	        // DownloadsProvider
	        else if (isDownloadsDocument(uri)) {

	            final String id = DocumentsContract.getDocumentId(uri);
	            final Uri contentUri = ContentUris.withAppendedId(
	                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

	            return getDataColumn(context, contentUri, null, null);
	        }
	        // MediaProvider
	        else if (isMediaDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            Uri contentUri = null;
	            if ("image".equals(type)) {
	                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	            } else if ("video".equals(type)) {
	                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	            } else if ("audio".equals(type)) {
	                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	            }

	            final String selection = "_id=?";
	            final String[] selectionArgs = new String[] {
	                    split[1]
	            };

	            return getDataColumn(context, contentUri, selection, selectionArgs);
	        }
	    }
	    // MediaStore (and general)
	    else if ("content".equalsIgnoreCase(uri.getScheme())) {

	        // Return the remote address
	        if (isGooglePhotosUri(uri))
	            return uri.getLastPathSegment();

	        return getDataColumn(context, uri, null, null);
	    }
	    // File
	    else if ("file".equalsIgnoreCase(uri.getScheme())) {
	        return uri.getPath();
	    }

	    return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
	        String[] selectionArgs) {

	    Cursor cursor = null;
	    final String column = "_data";
	    final String[] projection = {
	            column
	    };

	    try {
	        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
	                null);
	        if (cursor != null && cursor.moveToFirst()) {
	            final int index = cursor.getColumnIndexOrThrow(column);
	            return cursor.getString(index);
	        }
	    } finally {
	        if (cursor != null)
	            cursor.close();
	    }
	    return null;
	}


	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
	    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
	    return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
	    return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}
	
	/**
	 * 保存图片的路径(外置存储)
	 * @param context
	 * @return
	 */
	public static String getImageSavePath(Context context) {
		return getExternalV5KFPath(context) + "/image";
	}

	/**
	 * 内部图片缓存路径[新增]
	 * @param context
	 * @return
	 */
	public static String getImageCachePath(Context context) {
//		return getOldImageCachePath(context);
		return getPackageCachePath(context) + "/imagecache";
	}
	
	/**
	 * 语音等媒体文件缓存路径
	 * @param context
	 * @return
	 */
	public static String getMediaCachePath(Context context) {
//		return getOldImageCachePath(context);
		return getPackageCachePath(context) + "/mediacache";
	}
	/**
	 * 之前版本的图片缓存目录[旧路径]
	 * @param context
	 * @return
	 */
	public static String getOldImageCachePath(Context context) {
		return getExternalPackagePath(context) + "/imagecache";
	}
	

	/**
	 * 外置存储卡上应用数据保存路径
	 * @param context
	 * @return
	 */
	public static String getExternalV5KFPath(Context context) {
		return Environment.getExternalStorageDirectory() + "/V5KF";
	}
	/**
	 * 外置存储的包目录
	 * @param context
	 * @return
	 */
	public static String getExternalPackagePath(Context context) {
		return Environment.getExternalStorageDirectory() + "/" + context.getPackageName();
	}

	/**
	 * data内置存储应用数据保存路径
	 * @param context
	 * @return
	 */
	public static String getPackagePath(Context context) {
//		return context.getApplicationContext().getFilesDir().getAbsolutePath();
		return Environment.getDataDirectory() + "/" + context.getPackageName();
	}
	
	/**
	 * 内置存储的包缓存路径: data/data/com.v5kf.mcss/cache
	 * @param context
	 * @return
	 */
//	public static String getPackageFilePath(Context context) {
//		return context.getFilesDir().getAbsolutePath();
//	}
	public static String getPackageCachePath(Context context) {
		return context.getCacheDir().getAbsolutePath();
	}

	/**
	 * Crash日志文件路径(已取消日志记录)
	 * @param context
	 * @return
	 */
	public static String getCrashLogPath(Context context) {
		return getExternalV5KFPath(context) + "/crash";
	}
	
	/**
	 * 获得图片保存名称
	 * @return
	 */
	public static String getImageName(String tag) {
		return "v5kf_" + tag + DateUtil.getCurrentLongTime() + ".jpg";
	}

	public static String getImageName() {
		return "v5kf" + DateUtil.getCurrentLongTime() + ".jpg";
	}
	
//	/**
//	 * 媒体文件名格式
//	 * @param tag  自定标签
//	 * @param suffix 文件扩展名
//	 * @return
//	 */
//	public static String getVoiceFileName(String tag) {
//		return "v5kf_" + tag + DateUtil.getCurrentLongTime() + ".amr";
//	}
//	
//	/**
//	 * 媒体文件名格式
//	 * @param tag  自定标签
//	 * @param suffix 文件扩展名
//	 * @return
//	 */
//	public static String getMediaFileName(String tag, String suffix) {
//		return "v5kf_" + tag + DateUtil.getCurrentLongTime() + "." + suffix;
//	}
//
//	/**
//	 * 
//	 * @param suffix
//	 * @return
//	 */
//	public static String getMediaFileName(String suffix) {
//		return "v5kf" + DateUtil.getCurrentLongTime() + "." + suffix;
//	}
	
	
	/**   
     * 获取文件夹大小   
     * @param file File实例   
     * @return long      
     */     
    public static long getFolderSize(File file){    
        long size = 0;    
        try {  
            File[] fileList = file.listFiles();
            if (null == fileList) {
            	return size;
            }
            for (int i = 0; i < fileList.length; i++) {     
                if (fileList[i].isDirectory()) {     
                    size = size + getFolderSize(fileList[i]);    
                } else {     
                    size = size + fileList[i].length();
                }     
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }     
       //return size/1048576;    
        return size;    
    }    
      
    /**   
     * 删除指定目录下文件及目录    
     * @param deleteThisPath   
     * @param filepath   
     * @return    
     */     
    public static void deleteFolderFile(String filePath, boolean deleteThisPath) {     
        if (!TextUtils.isEmpty(filePath)) {     
            try {  
                File file = new File(filePath);     
                if (file.isDirectory()) {// 处理目录     
                    File files[] = file.listFiles();     
                    for (int i = 0; i < files.length; i++) {     
                        deleteFolderFile(files[i].getAbsolutePath(), true);     
                    }
                }     
                if (deleteThisPath) {     
                    if (!file.isDirectory()) {// 如果是文件，删除     
                        file.delete();     
                    } else {// 目录     
                    	if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除     
                            file.delete();     
                        }     
                    }     
                }  
            } catch (Exception e) {  
                e.printStackTrace();  
            }     
        }     
    }
    
    /**
     * 删除路径，如是目录也全部删除
     * @param filePath
     */
    public static void deleteFile(String filePath) {
    	if (!TextUtils.isEmpty(filePath)) {     
            try {  
                File file = new File(filePath);     
                if (file.isDirectory()) { // 处理目录     
                    File files[] = file.listFiles();     
                    for (int i = 0; i < files.length; i++) {     
                    	deleteFile(files[i].getAbsolutePath());     
                    }
                    file.delete();
                } else { // 如果是文件，删除
                	file.delete();
                } 
            } catch (Exception e) {  
                e.printStackTrace();  
            }     
        }
    }
    
    public static boolean isFolderExists(String strFolder) {
        File file = new File(strFolder);        
        if (!file.exists()) {
            if (file.mkdirs()) {                
                return true;
            } else {
                return false;

            }
        }
        return true;
    }

    public static boolean isFileExists(String strPath) {
    	File file = new File(strPath);        
    	return file.exists();
    }
}
