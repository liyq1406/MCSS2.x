package com.v5kf.mcss.ui.activity.info;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easemob.chatuidemo.widget.photoview.PhotoView;
import com.easemob.chatuidemo.widget.photoview.PhotoViewAttacher.OnPhotoTapListener;
import com.easemob.chatuidemo.widget.photoview.PhotoViewAttacher.OnViewTapListener;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.ui.activity.BaseActivity;
import com.v5kf.mcss.ui.widget.CustomOptionDialog;
import com.v5kf.mcss.ui.widget.CustomOptionDialog.OptionDialogListener;
import com.v5kf.mcss.utils.FileUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.cache.BitmapHelper;

public class PhotoPreviewActivity extends BaseActivity implements OnClickListener {

	private static final String TAG = "PhotoPreviewActivity";
	private static final int CODE_RST_CANCEL = 0;
	private static final int HDL_UPDATE_IMG = 1;
	/* 标题栏 */
	private TextView mTitleTv;
	private Button mSendBtn;

	private PhotoView mPhotoIv;
	private RelativeLayout mCropLl;
	private RelativeLayout mCancelLl;
	
	private String mImgPath; // 传入的文件(带路径)
	private int mReqCode; // 传入的请求码
	private Bitmap mBitmap;
	private String mFileName; // 最终处理后的文件名(带路径)
	private String mSaveName; // 长按保存的文件名(带路径)
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_preview);
		
		handleIntent();
		findView();
		initView();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (null == mImgPath || mImgPath.isEmpty()) {
			Logger.w(TAG, "Got null imgPath.");
			finishActivity();
			return;
		}
	}

	private void handleIntent() {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
		mReqCode = intent.getIntExtra("reqCode", 0);
		mImgPath = intent.getStringExtra("imgPath");
		mFileName = mImgPath;
		Logger.d(TAG, "Uri -> mImagePath:" + mImgPath + " mReqCode:" + mReqCode);
		if (null == mImgPath || mImgPath.isEmpty()) {
			Logger.w(TAG, "Got null imgPath.");
			finishActivity();
			return;
		}
	}
	
	private void findView() {
		// TODO Auto-generated method stub
		mTitleTv = (TextView) findViewById(R.id.header_htv_subtitle);
		mSendBtn = (Button) findViewById(R.id.btn_send);
		mPhotoIv = (PhotoView) findViewById(R.id.id_image);
		mCropLl = (RelativeLayout) findViewById(R.id.id_ll_left);
		mCancelLl = (RelativeLayout) findViewById(R.id.id_ll_right);
	}

	private void initView() {
		// TODO Auto-generated method stub
		mTitleTv.setText(R.string.preview);
		mCropLl.setOnClickListener(this);
		mCancelLl.setOnClickListener(this);
		mSendBtn.setOnClickListener(this);
		findViewById(R.id.header_layout_leftview_container).setOnClickListener(this);
	
		mPhotoIv.setOnPhotoTapListener(new OnPhotoTapListener() {
			
			@Override
			public void onPhotoTap(View view, float x, float y) {
				// TODO Auto-generated method stub
				Logger.d("ShowImageActivity", "[onPhotoTap]");
			}
		});
		
		mPhotoIv.setOnViewTapListener(new OnViewTapListener() {
			
			@Override
			public void onViewTap(View view, float x, float y) {
				// TODO Auto-generated method stub
				Logger.d("ShowImageActivity", "[onViewTap]");
//				finishActivity();
			}
		});
		
		mPhotoIv.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				Logger.d("ShowImageActivity", "[onLongClick]");
				// 保存文件到本地
				showSaveImgOptionDialog();
				return false;
			}
		});
		
		if (mImgPath != null && !mImgPath.isEmpty()) {
			showProgressDialog();
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					// 压缩图片
					Bitmap bmp = BitmapFactory.decodeFile(mImgPath);
					File file = new File(mImgPath);
					Logger.d(TAG, "Uri file Bytes:" + file.length());
					Logger.d(TAG, "Uri bitmap Bytes:" + bmp.getByteCount());
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					bmp.compress(Bitmap.CompressFormat.JPEG, 90, baos);
					Logger.d(TAG, "Uri bitmap length Bytes:" + baos.toByteArray().length);
					
					mBitmap = BitmapHelper.compressBitmap(mImgPath, 500, 500);
					Logger.d(TAG, "Uri press.bitmap Bytes:" + mBitmap.getByteCount());
					baos = new ByteArrayOutputStream();
					mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);  
					Logger.d(TAG, "Uri press.bitmap length Bytes:" + baos.toByteArray().length);
					mHandler.sendEmptyMessage(HDL_UPDATE_IMG);
				}
			}).start();
		}
	}
	
	/**
     * 裁剪原始的图片
     */
	public void cropRawPhoto(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		
		// 设置裁剪
		intent.putExtra("crop", "true");
		
		// aspectX , aspectY :宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		
		// outputX , outputY : 裁剪图片宽高
		intent.putExtra("outputX", 500);
		intent.putExtra("outputY", 500);
		intent.putExtra("return-data", true);
		
		startActivityForResult(intent, Config.REQUEST_CODE_CROP);
	}
	

	protected void showSaveImgOptionDialog() {
		// TODO Auto-generated method stub
		showOptionDialogInChat(
				CustomOptionDialog.DISPLAY_TYPE_SHOW_IMAGE_ONE_BTN, 
				new OptionDialogListener() {
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				switch (view.getId()) {
				case R.id.btn_dialog_only_option:	// 保存图片
					saveImage();
					ShowToast(String.format(getString(R.string.on_image_saveed), mSaveName));
					break;
				}
			}

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
//				mMsg.setBackgroundResource(R.drawable.list_to_textview_bg);
			}
		});
	}

	protected void saveImage() {
		// TODO Auto-generated method stub
		mPhotoIv.setDrawingCacheEnabled(true);
		Bitmap tempBmp = Bitmap.createBitmap(mPhotoIv.getDrawingCache());
		mPhotoIv.setDrawingCacheEnabled(false);
		mSaveName = saveBitmap2File(tempBmp);
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
	
	@Override
	protected void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch (msg.what) {
		case HDL_UPDATE_IMG:
			Logger.d(TAG, "Uri HDL_UPDATE_IMG");
			if (mBitmap != null) {
				mPhotoIv.setImageBitmap(mBitmap);
			}
			dismissProgressDialog();
			break;
		}
	}

	@Override
	protected void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.id_ll_left:
			cropRawPhoto(Uri.fromFile(new File(mImgPath)));
			break;
		
		case R.id.header_layout_leftview_container:
		case R.id.id_ll_right:
			setResult(PhotoPreviewActivity.CODE_RST_CANCEL);
			finishActivity();
			break;
		
		case R.id.btn_send:
			// 发送图片，获得MediaId和PicUrl
			showProgressDialog();
			
			
			
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == Config.REQUEST_CODE_CROP) {
			if(resultCode == RESULT_OK){  
                // 拿到剪切数据  
                Bitmap bmap = data.getParcelableExtra("data");  
                  
                // 显示剪切的图像  
                mPhotoIv.setImageBitmap(bmap);  
                  
                // 图像保存到文件中  
                FileOutputStream foutput = null;  
                try {
                	mFileName = FileUtil.getImageSavePath(this) + "/" + FileUtil.getImageName("crop");
                	Logger.d(TAG, "onActivityResult mFileName:" + mFileName);
                    foutput = new FileOutputStream(mFileName);
                    bmap.compress(Bitmap.CompressFormat.JPEG, 90, foutput);
                    Logger.d(TAG, "Uri crop.bitmap Bytes:" + bmap.getByteCount());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();  
                } finally{  
                    if(null != foutput){
                        try {
                            foutput.close();
                        } catch (IOException e) {  
                            e.printStackTrace();  
                        }
                    }
                }
            }
		}
	}

}
