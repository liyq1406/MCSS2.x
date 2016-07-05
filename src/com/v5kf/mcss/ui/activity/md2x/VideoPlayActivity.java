package com.v5kf.mcss.ui.activity.md2x;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.v5kf.mcss.R;
import com.v5kf.mcss.ui.view.ActionItem;
import com.v5kf.mcss.ui.view.TitlePopup;
import com.v5kf.mcss.ui.view.TitlePopup.OnItemOnClickListener;
import com.v5kf.mcss.utils.DevUtils;
import com.v5kf.mcss.utils.FileUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.UITools;

public class VideoPlayActivity extends BaseToolbarActivity implements View.OnClickListener {
	
	private static final String TAG = "VideoPlayActivity";
	private VideoView mVideoView;
	private ImageView mVideoControlIv;
	private ImageView mVideoBgIv;
	
	private MediaController mMediaController;
	
	private String mFilePath;
	private int mTitleId;
	
	private AppBarLayout mAppBarLayout;
	
	// 标题栏弹窗
	private TitlePopup mTitlePopup;
	private ImageView mMoreIv;
	private View mRLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_md2x_video_view);
		
		handleIntent();
		findView();
		initView();
		
		// Toolbar 设置
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			// 启用home as up, 改变返回按钮颜色为main_color_accent
		    Drawable upArrow = getResources().getDrawable(R.drawable.v5_action_bar_back);
		    upArrow.setColorFilter(getResources().getColor(R.color.white), android.graphics.PorterDuff.Mode.SRC_ATOP);
		    actionBar.setHomeAsUpIndicator(upArrow);
		    
		    actionBar.setDisplayHomeAsUpEnabled(true);
		    actionBar.setHomeButtonEnabled(true);
		    actionBar.setDisplayShowHomeEnabled(true);
		}
		getToolbar().setTitleTextColor(UITools.getColor(R.color.white));
		getToolbar().setBackgroundColor(UITools.getColor(R.color.transparent));
	}
	
	@Override
	public void onBackPressed() {
		stopPlay();
		finishActivity();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		resetPlayer();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem itemSave = menu.add(0, 1, Menu.NONE, R.string.save_vedio);
		itemSave.setIcon(R.drawable.v5_action_bar_ok);
		itemSave.setShortcut('0', 's');
		itemSave.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		MenuItem itemOpen = menu.add(0, 2, Menu.NONE, R.string.open_by_vedioplayer);
		itemOpen.setIcon(R.drawable.v5_action_bar_ok);
		itemOpen.setShortcut('0', 'o');
		itemOpen.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Logger.d("WebViewActivity", "保存本地");
			if (DevUtils.hasPermission(VideoPlayActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
				String des = saveFile();
				if (des != null) {
					ShowToast(String.format(getString(R.string.on_video_saveed_fmt), des));
				} else {
					ShowToast(R.string.on_video_saveed_failed);
				}
			} else {
				showAlertDialog(R.string.v5_permission_photo_deny, null);
			}
			break;
		case 2:
			Logger.d("WebViewActivity", "点击在浏览器中打开");
			Intent intent = new Intent(Intent.ACTION_VIEW);   
	        intent.setAction("android.intent.action.VIEW");    
	        Uri content_url = Uri.parse(mFilePath);   
	        intent.setDataAndType(content_url, "video/*");
	        startActivity(intent);
		}
		
		return super.onOptionsItemSelected(item);
	}

	private void handleIntent() {
		Intent intent = getIntent();
		mFilePath = intent.getStringExtra("file_path");
		if (null == mFilePath || mFilePath.isEmpty()) {
			Logger.w(TAG, "Got null url.");
			finishActivity();
			return;
		}
		Logger.d(TAG, "Got url:" + mFilePath);
	}

	private void findView() {
		mVideoView = (VideoView) findViewById(R.id.id_video_view);
		mVideoControlIv = (ImageView) findViewById(R.id.id_video_control_img);
		mVideoBgIv = (ImageView) findViewById(R.id.id_video_bg);
		mMoreIv = (ImageView) findViewById(R.id.more_iv);
		
		mRLayout = findViewById(R.id.id_video_layout);
		mAppBarLayout = (AppBarLayout) findViewById(R.id.id_app_bar_layout);
	}

	@SuppressLint("NewApi") 
	private void initView() {
		initTitleBar();
		mMoreIv.setVisibility(View.GONE);
		
		// 获得cover fram Bitmap
		MediaMetadataRetriever mediaDataRet = new MediaMetadataRetriever();
		mediaDataRet.setDataSource(mFilePath);
		Bitmap bitmap = mediaDataRet.getFrameAtTime(0);
		mVideoBgIv.setImageBitmap(bitmap);
		
		mMoreIv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mTitlePopup.show(v);
			}
		});
		
		mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				Logger.d(TAG, "[onCompletion]");
				resetPlayer();
			}
		});
		
		mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Logger.d(TAG, "[onError]");
				return false;
			}
		});
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
				
				@Override
				public boolean onInfo(MediaPlayer mp, int what, int extra) {
					Logger.d(TAG, "[onInfo]");
					return false;
				}
			});
		}
		
		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			
			@Override
			public void onPrepared(MediaPlayer mp) {
				Logger.d(TAG, "[onPrepared]");
				mVideoControlIv.setVisibility(View.GONE);
				mVideoBgIv.setVisibility(View.GONE);
				if (mAppBarLayout.getVisibility() != View.VISIBLE) {
					mRLayout.setSystemUiVisibility(View.INVISIBLE);
				}
			}
		});
		
		mVideoControlIv.setOnClickListener(this);
//		mVideoControlIv.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Logger.d(TAG, "[onClick] - start");
//					mVideoView.setVisibility(View.VISIBLE);
//					Uri uri = Uri.parse(mFilePath);  
//					mVideoView.setMediaController(new MediaController(VideoPlayActivity.this));    
//					mVideoView.setVideoURI(uri);
//					mVideoView.start();    
//					mVideoView.requestFocus();
//			}
//		});
		
		findViewById(R.id.content_layout).setOnClickListener(this);
		findViewById(R.id.id_video_bg).setOnClickListener(this);
//		findViewById(R.id.id_video_view).setOnClickListener(this);
	}

	private void initTitleBar() {
		initPopupMenu();
		if (mTitleId == 0) {
			mTitleId = R.string.video_play;
		}
		initTopBarForLeftBack(mTitleId);
		// toolbar右侧按钮
//		initTopBarForLeftImageAndRightImage(
//				mTitleId, 
//				R.drawable.v5_baritem_back, 
//				R.drawable.v5_action_bar_more, 
//				new onLeftImageButtonClickListener() {
//			
//					@Override
//					public void onClick(View v) {
//						if (mWebView.canGoBack()) {
//							mWebView.goBack();
//						} else {
//							finishActivity();
//						}
//					}
//				}, 
//				new onRightImageButtonClickListener() {
//					
//					@Override
//					public void onClick(View arg0) {
//						mTitlePopup.show(arg0);
//					}
//				});
	}
	
	/**
     * 初始化弹出菜单栏
     * @param initPopupMenu MainTabActivity 
     * @return void
     */
    private void initPopupMenu(){
    	// 实例化标题栏弹窗
    	mTitlePopup = new TitlePopup(this, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	
    	mTitlePopup.addAction(new ActionItem(this, R.string.save_vedio, 0));
    	mTitlePopup.addAction(new ActionItem(this, R.string.open_by_vedioplayer, 0));
    	
    	mTitlePopup.setItemOnClickListener(new OnItemOnClickListener() {
			
			@Override
			public void onItemClick(ActionItem item, int position) {
				switch (position) {
				case 0:
					Logger.d("WebViewActivity", "保存本地");
					if (DevUtils.hasPermission(VideoPlayActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
						String des = saveFile();
						if (des != null) {
							ShowToast(String.format(getString(R.string.on_video_saveed_fmt), des));
						} else {
							ShowToast(R.string.on_video_saveed_failed);
						}
					} else {
						showAlertDialog(R.string.v5_permission_photo_deny, null);
					}
					break;
				case 1:
					Logger.d("WebViewActivity", "点击在浏览器中打开");
					Intent intent = new Intent(Intent.ACTION_VIEW);   
			        intent.setAction("android.intent.action.VIEW");    
			        Uri content_url = Uri.parse(mFilePath);   
			        intent.setDataAndType(content_url, "video/*");
			        startActivity(intent);
				}
			}
		});
	}

    protected String saveFile() {
		if (mFilePath == null) {
			return null;
		}
    	String filepath = null;
        // 图片存储路径
        String saveDir = FileUtil.getVideoSavePath(this);
        // 保存Bitmap
        try {
            File path = new File(saveDir);
            // 文件
            String fileName = FileUtil.getVideoName("save", null);
            filepath = saveDir + "/" + fileName;
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
            	InputStream fosfrom = new FileInputStream(mFilePath);
                byte bt[] = new byte[1024];
                int c;
                while ((c = fosfrom.read(bt)) > 0) {
                	fos.write(bt, 0, c);
                }
                fosfrom.close();
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
        	return null;
        }
        return filepath;
	}

	private void stopPlay() {
		// TODO Auto-generated method stub
	}
	
	private void resetPlayer() {
		mVideoControlIv.setVisibility(View.VISIBLE);
		mVideoBgIv.setVisibility(View.VISIBLE);
		mVideoView.setVisibility(View.GONE);
	}
    
	@Override
	protected void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}

	@SuppressLint("InlinedApi") @Override
	public void onClick(View v) {
		Logger.d(TAG, "onClick:" + v.getId());
		switch (v.getId()) {
		case R.id.id_video_control_img:
			mVideoView.setVisibility(View.VISIBLE);
			Uri uri = Uri.parse(mFilePath);
			if (mMediaController == null) {
				mMediaController = new MediaController(VideoPlayActivity.this);
				mMediaController.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Logger.d(TAG, "onClick MediaController:" + v.getId());
					}
				});
			}
			mVideoView.setMediaController(mMediaController);    
			mVideoView.setVideoURI(uri);
			mVideoView.start();    
			mVideoView.requestFocus();
			break;
		
		case R.id.id_video_bg:
//		case R.id.id_video_view:
		case R.id.content_layout:
			if (mAppBarLayout.getVisibility() == View.VISIBLE) {
				mAppBarLayout.setVisibility(View.GONE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					mRLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
				} else {
					mRLayout.setSystemUiVisibility(View.INVISIBLE);
				}
				//mRLayout.setSystemUiVisibility(View.INVISIBLE);
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
			} else {
//				mRLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
//				mRLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
				mRLayout.setSystemUiVisibility(View.VISIBLE);
				mAppBarLayout.setVisibility(View.VISIBLE);
//				getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//显示状态栏
			}
			break;
		}
	}

}
