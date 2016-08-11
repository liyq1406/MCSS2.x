package com.v5kf.mcss.ui.activity.md2x;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.v5kf.client.lib.V5ClientAgent;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.ui.fragment.md2x.ImageDetailFragment;
import com.v5kf.mcss.ui.widget.CustomOptionDialog;
import com.v5kf.mcss.ui.widget.HackyViewPager;
import com.v5kf.mcss.utils.DevUtils;
import com.v5kf.mcss.utils.FileUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;
import com.v5kf.mcss.utils.cache.ImageLoader;

public class ShowImageGallaryActivity extends ActivityBase {

	private static final String TAG = "ShowImageGallaryActivity";
	private List<V5Message> mList;
	private int mCurPos; // 初始化计算当前图片位置
	private String mFileName;
	
	private HackyViewPager mPager;
	private TextView mIndicatorTv;
	private int pagerPosition;
	private static final String STATE_POSITION = "STATE_POSITION";
	
	private ArrayList<View> mViews = new ArrayList<>();;
	private ArrayList<String> mImageUrls = new ArrayList<>();
	
	@Override
    public void onBackPressed() {
		Logger.i("ActivityBase", "onBackPressed");
		finish();
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_md2x_image_gallary);
		handleIntent();
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
		
		initData();
		initView();
		
		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}
		mPager.setCurrentItem(pagerPosition);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, mPager.getCurrentItem());
	}
	
	private void initData() {
		int j = 0;
		if (mList != null && mList.size() > 0) {
//			for (int i = mList.size() - 1; i >= 0; i--) {
//				V5Message msg = mList.get(i);
//				if (msg.getMessage_type() == QAODefine.MSG_TYPE_IMAGE) {
//					if ((mList.size()-1-i) == mCurPos) {
//						mIndex = j;
//					}
//					j++;
//					mImageUrls.add(((V5ImageMessage)msg).getPic_url());
//					mViews.add(new PhotoView(getApplicationContext()));
//				}
//			}
			for (int i =  0; i < mList.size(); i++) {
				V5Message msg = mList.get(i);
				if (msg.getMessage_type() == QAODefine.MSG_TYPE_IMAGE) {
					if (i == mCurPos) {
						pagerPosition = j;
					}
					j++;
					mImageUrls.add(((V5ImageMessage)msg).getPic_url());
					mViews.add(new ImageView(getApplicationContext()));
				}
			}
		}
		Logger.d(TAG, "mCurPos=" + mCurPos + " pagerPosition=" + pagerPosition + " size:" + mImageUrls.size());
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
	
	@SuppressLint({ "InlinedApi", "DefaultLocale" }) 
	private void initView() {
		mIndicatorTv = (TextView) findViewById(R.id.id_indicator);
		mPager = (HackyViewPager) findViewById(R.id.id_gallary_viewpager);
		mPager.setAdapter(new ImagePagerAdapter(getSupportFragmentManager(), mImageUrls));
		
    	// 全屏显示
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
    		mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
		} else {
			mPager.setSystemUiVisibility(View.INVISIBLE);
		}
		//mRLayout.setSystemUiVisibility(View.INVISIBLE);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
		
		CharSequence text = String.format("%d/%d", pagerPosition + 1, mPager.getAdapter().getCount());
		mIndicatorTv.setText(text);
		// 更新下标
		mPager.addOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				CharSequence text = String.format("%d/%d", arg0 + 1, mPager.getAdapter().getCount());
				mIndicatorTv.setText(text);
			}

		});
	}

	public void showSaveImgOptionDialog(final String url) {
		showOptionDialogInChat(
				CustomOptionDialog.DISPLAY_TYPE_SHOW_IMAGE_ONE_BTN, 
				new CustomOptionDialog.OptionDialogListener() {
					@Override
					public void onClick(View view) {
						switch (view.getId()) {
						case R.id.btn_dialog_only_option:	// 保存图片
							if (DevUtils.hasPermission(ShowImageGallaryActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
								if (saveImage(url)) {
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
	
	protected void showSaveImgOptionDialog(final View v) {
		showOptionDialogInChat(
				CustomOptionDialog.DISPLAY_TYPE_SHOW_IMAGE_ONE_BTN, 
				new CustomOptionDialog.OptionDialogListener() {
			@Override
			public void onClick(View view) {
				switch (view.getId()) {
				case R.id.btn_dialog_only_option:	// 保存图片
					if (DevUtils.hasPermission(ShowImageGallaryActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
						if (saveImage((ImageView)v)) {
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

	protected boolean saveImage(ImageView iv) {
		iv.setDrawingCacheEnabled(true);
		Bitmap tempBmp = Bitmap.createBitmap(iv.getDrawingCache());
		iv.setDrawingCacheEnabled(false);
		mFileName = saveBitmap2File(tempBmp);
		if (null == mFileName) {
			return false;
		} else {
			return true;
		}
	}

	protected boolean saveImage(String url) {
		Bitmap tempBmp = ImageLoader.getBitmap(getApplicationContext(), url);
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

	@SuppressWarnings("unchecked")
	private void handleIntent() {
		Intent intent = getIntent();
		mList = (List<V5Message>)intent.getSerializableExtra("message_list");
		mCurPos = intent.getIntExtra("position", 0);
		if (null == mList || mList.isEmpty()) { // CSTM_ACTIVE
        	Logger.e(TAG, "List(null) empty");
        	finish();
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

//	class ImagePagerAdapter extends PagerAdapter {
//			
//		@Override
//		public Object instantiateItem(ViewGroup container, final int position) {
//			ImageView iv = (ImageView) mViews.get(position);
//			//iv.setScaleType(ScaleType.FIT_CENTER);
//			final PhotoViewAttacher attacher = new PhotoViewAttacher(iv);
//			
//			attacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
//
//				@Override
//				public void onPhotoTap(View arg0, float arg1, float arg2) {
//					finish();
//				}
//
//			});
//			attacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
//				
//				@Override
//				public void onViewTap(View view, float x, float y) {
//					finish();
//				}
//			});
//			attacher.setOnLongClickListener(new View.OnLongClickListener() {
//				
//				@Override
//				public boolean onLongClick(View v) {
//					showSaveImgOptionDialog(position);
//					return true;
//				}
//			});
//			
////			container.setOnClickListener(new View.OnClickListener() {
////				
////				@Override
////				public void onClick(View v) {
////					finish();
////				}
////			});
////			iv.setOnLongClickListener(new View.OnLongClickListener() {
////				
////				@Override
////				public boolean onLongClick(View v) {
////					showSaveImgOptionDialog(position);
////					return true;
////				}
////			});
////			iv.setOnPhotoTapListener(new OnPhotoTapListener() {
////				
////				@Override
////				public void onPhotoTap(View view, float x, float y) {
////					finish();
////				}
////			});
//			
//			ImageLoader imgLoader = new ImageLoader(ShowImageGallaryActivity.this, true, R.drawable.v5_img_src_loading, new ImageLoader.ImageLoaderListener() {
//				
//				@Override
//				public void onSuccess(String url, ImageView imageView, Bitmap bmp) {
//					attacher.update();
//				}
//				
//				@Override
//				public void onFailure(ImageLoader imageLoader, String url,
//						ImageView imageView) {
//					attacher.update();
//				}
//			});
//	    	imgLoader.DisplayImage(mImageUrls.get(position), iv);
//			
//			container.addView(iv);
//			return iv;
//		}
//		
//		@Override
//		public void destroyItem(ViewGroup container, int position,
//				Object object) {
////			super.destroyItem(container, position, object);
//			container.removeView(mViews.get(position));
//		}
//		
//		@Override
//		public boolean isViewFromObject(View arg0, Object arg1) {
//			return arg0 == arg1;
//		}
//		
//		@Override
//		public int getCount() {
//			return mImageUrls.size();
//		}
//	}
	
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {

		public ArrayList<String> fileList;

		public ImagePagerAdapter(FragmentManager fm, ArrayList<String> fileList) {
			super(fm);
			this.fileList = fileList;
		}

		@Override
		public int getCount() {
			return fileList == null ? 0 : fileList.size();
		}

		@Override
		public Fragment getItem(int position) {
			String url = fileList.get(position);
			return ImageDetailFragment.newInstance(url);
		}

	}
}
