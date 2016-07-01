package com.v5kf.mcss.ui.activity.md2x;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.easemob.chatuidemo.widget.photoview.PhotoView;
import com.easemob.chatuidemo.widget.photoview.PhotoViewAttacher.OnPhotoTapListener;
import com.easemob.chatuidemo.widget.photoview.PhotoViewAttacher.OnViewTapListener;
import com.v5kf.client.lib.V5ClientAgent;
import com.v5kf.mcss.R;
import com.v5kf.mcss.ui.activity.info.BaseActivity;
import com.v5kf.mcss.ui.widget.CustomOptionDialog;
import com.v5kf.mcss.ui.widget.CustomOptionDialog.OptionDialogListener;
import com.v5kf.mcss.utils.DevUtils;
import com.v5kf.mcss.utils.FileUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.cache.ImageLoader;
import com.v5kf.mcss.utils.cache.ImageLoader.ImageLoaderListener;

public class ShowImageActivity extends BaseActivity implements ImageLoaderListener {

	private String mUrl;
	private PhotoView mPhotoIv;	
	private String mFileName;
	private ProgressBar mLoadingPb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_image);
		
//		setSwipeBackEnable(false);
//		/* 在API19以上改变状态栏颜色 */
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//			setTranslucentStatus(true);
//			SystemBarTintManager tintManager = new SystemBarTintManager(this);  
//			tintManager.setStatusBarTintEnabled(true);  
//			tintManager.setStatusBarTintResource(R.color.transparent);
//		}
		
		setNavigationBarColor(UITools.getColor(R.color.transparent));
		handleIntent();
		initView();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (V5ClientAgent.getInstance().isForeground()) {
    		V5ClientAgent.getInstance().onStart();
    	}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (V5ClientAgent.getInstance().isForeground()) {
    		V5ClientAgent.getInstance().onStop();
    	}
	}
	
	private void initView() {
		mPhotoIv = (PhotoView) findViewById(R.id.id_image);
		mLoadingPb = (ProgressBar) findViewById(R.id.id_loading_progress);
		
		mPhotoIv.setOnPhotoTapListener(new OnPhotoTapListener() {
			
			@Override
			public void onPhotoTap(View view, float x, float y) {
				Logger.d("ShowImageActivity", "[onPhotoTap]");
//				int v = getWindow().getAttributes().flags;  
//		        // 全屏 66816 - 非全屏 65792
//				if(v != WindowManager.LayoutParams.FLAG_FULLSCREEN) { //非全屏
//		        	getWindow().setFlags(
//	                    WindowManager.LayoutParams.FLAG_FULLSCREEN,  
//	                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		        } else { //取消全屏
//		            getWindow().clearFlags(  
//		            	WindowManager.LayoutParams.FLAG_FULLSCREEN);  
//		        }
				finishActivity(); // [修改]
			}
		});
		
		mPhotoIv.setOnViewTapListener(new OnViewTapListener() {
			
			@Override
			public void onViewTap(View view, float x, float y) {
				Logger.d("ShowImageActivity", "[onViewTap]");
				finishActivity();
			}
		});
		
		mPhotoIv.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				Logger.d("ShowImageActivity", "[onLongClick]");
				// 保存文件到本地
				showSaveImgOptionDialog();
				return false;
			}
		});
		
		ImageLoader imgLoader = new ImageLoader(this, true, R.drawable.v5_img_src_loading, this);
    	imgLoader.DisplayImage(mUrl, mPhotoIv);
	}

	protected void showSaveImgOptionDialog() {
		showOptionDialogInChat(
				CustomOptionDialog.DISPLAY_TYPE_SHOW_IMAGE_ONE_BTN, 
				new OptionDialogListener() {
			@Override
			public void onClick(View view) {
				switch (view.getId()) {
				case R.id.btn_dialog_only_option:	// 保存图片
					if (DevUtils.hasPermission(ShowImageActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
						if (saveImage()) {
							ShowToast(String.format(getString(R.string.on_image_saveed), mFileName));
						} else {
							ShowToast(R.string.on_image_saveed_failed);
						}
					} else {
						showAlertDialog(R.string.v5_permission_photo_deny, null);
					}
					break;
				}
			}

			@Override
			public void onDismiss(DialogInterface dialog) {
//				mMsg.setBackgroundResource(R.drawable.list_to_textview_bg);
			}
		});
	}

	protected boolean saveImage() {
		mPhotoIv.setDrawingCacheEnabled(true);
		Bitmap tempBmp = Bitmap.createBitmap(mPhotoIv.getDrawingCache());
		mPhotoIv.setDrawingCacheEnabled(false);
		mFileName = saveBitmap2File(tempBmp);
		if (null == mFileName) {
			return false;
		} else {
			return true;
		}
	}
	
	public String saveBitmap2File(Bitmap bitmap) {
        String filepath = null;
        // 图片存储路径
        String savePath = FileUtil.getImageSavePath(this);
        // 保存Bitmap
        try {
            File path = new File(savePath);
            // 文件
            String fileName = FileUtil.getImageName("save");
            filepath = savePath + "/" + fileName;
            File file = new File(filepath);
            if (!path.exists()) {
            	path.mkdirs();
            }
            if (!file.exists()) {
            	file.createNewFile();
            }
            FileOutputStream fos = null;
            fos = new FileOutputStream(file);
            if (null != fos) {
            	bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            	fos.flush();
            	fos.close();
            }
            // 通知系统刷新相册，否则点击相册后，找不到该文件，除非mount SD卡
            Uri localUri = Uri.fromFile(file);
            Intent localIntent = new Intent(
            		Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
            sendBroadcast(localIntent);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return filepath;
	}

	private void handleIntent() {
		Intent intent = getIntent();
		mUrl = intent.getStringExtra("pic_url");
		if (null == mUrl || mUrl.isEmpty()) {
			Logger.w("ShowImageActivity", "Got null pic_url.");
			finishActivity();
			return;
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSuccess(String url, ImageView imageView) {
		mLoadingPb.setVisibility(View.GONE);
	}

	@Override
	public void onFailure(ImageLoader imageLoader, String url, ImageView imageView) {
		mLoadingPb.setVisibility(View.GONE);
	}

}
