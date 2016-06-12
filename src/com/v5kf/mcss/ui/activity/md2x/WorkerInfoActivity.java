package com.v5kf.mcss.ui.activity.md2x;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.v5kf.client.lib.V5HttpUtil;
import com.v5kf.client.lib.callback.HttpResponseHandler;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.ArchWorkerBean;
import com.v5kf.mcss.entity.WorkerBean;
import com.v5kf.mcss.eventbus.EventTag;
import com.v5kf.mcss.manage.RequestManager;
import com.v5kf.mcss.qao.request.WorkerRequest;
import com.v5kf.mcss.ui.widget.ActionSheetDialog;
import com.v5kf.mcss.ui.widget.ActionSheetDialog.OnSheetItemClickListener;
import com.v5kf.mcss.ui.widget.ActionSheetDialog.SheetItemColor;
import com.v5kf.mcss.ui.widget.CircleImageView;
import com.v5kf.mcss.utils.FileUtil;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.cache.ImageLoader;

public class WorkerInfoActivity extends BaseToolbarActivity implements OnClickListener {
	
	private static final String TAG = "WorkerInfoActivity";
	protected static final int HDL_UPLOAD_PHOTO_FAILED = 1;
	protected static final int HDL_UPLOAD_PHOTO_OK = 2;
	protected static final int HDL_SET_NICKNAME_OK = 3;
	
	protected String w_id;
	protected WorkerBean mWorker;
	protected ArchWorkerBean mCoWorker;
	
	private CircleImageView mHeadIv;
	@SuppressWarnings("unused")
	private RelativeLayout mHeadphotoRl, mStatusRl, mModeRl, mSiteRl, mNicknameRl, mRealnameRl, mGenderRl, 
		mPhoneRl, mEmailRl, mWeixinRl, mQQRl;
	@SuppressWarnings("unused")
	private TextView mStatusTv, mModeTv, mSiteTv, mNicknameTv, mRealnameTv, mGenderTv, mPhoneTv, mEmailTv, 
		mWeixinTv, mQQTv;
	private View mGenderDiv, mPhoneDiv, mEmailDiv, mWeixinDiv, mQQDiv;

	// 拍照、图片
	private String mImageFileName;
	
	private String myPhoto;
	private String myNickname;
	
	protected void handleIntent() {
		Intent intent = getIntent();
		int type = intent.getIntExtra(Config.EXTRA_KEY_INTENT_TYPE, Config.EXTRA_TYPE_NULL);
		if (Config.EXTRA_TYPE_ACTIVITY_START == type) {
			w_id = intent.getStringExtra(Config.EXTRA_KEY_W_ID);
			Logger.d(TAG, "BaseChatActivity -> Intent -> WorkerInfoActivity\n w_id:" + w_id);
		}
		
		if (null != w_id) { // CSTM_ACTIVE
			if (mAppInfo.getUser().getW_id() != null && mAppInfo.getUser().getW_id().equals(w_id)) {
				mWorker = mAppInfo.getUser();
			} else {
				mCoWorker = mAppInfo.getCoWorker(w_id);
			}
		}
		
		if (mWorker == null && mCoWorker == null) {
			Logger.e(TAG, "Worker(null) of w_id not found");
        	finishActivity();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_md2x_worker_info);
		
		handleIntent();
		findView();
		initView();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (this.myNickname != null || this.myPhoto != null) {
			try {
				WorkerRequest wReq = (WorkerRequest)RequestManager.getRequest(QAODefine.O_TYPE_WWRKR, getApplicationContext());
				wReq.setWorkerInfo(mWorker);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			EventBus.getDefault().post(mWorker, EventTag.ETAG_UPDATE_USER_INFO);
			EventBus.getDefault().post(mAppInfo, EventTag.ETAG_ARCH_WORKER_CHANGE);
		}
	}

	private void findView() {
		mHeadIv = (CircleImageView) findViewById(R.id.id_photo_iv);
		
		mHeadphotoRl = (RelativeLayout) findViewById(R.id.layout_photo);
		mStatusRl = (RelativeLayout) findViewById(R.id.layout_status);
		mModeRl = (RelativeLayout) findViewById(R.id.layout_mode);
		mSiteRl = (RelativeLayout) findViewById(R.id.layout_site);
		mNicknameRl = (RelativeLayout) findViewById(R.id.layout_nickname);
		mRealnameRl = (RelativeLayout) findViewById(R.id.layout_realname);
		mGenderRl = (RelativeLayout) findViewById(R.id.layout_gender);
		mPhoneRl = (RelativeLayout) findViewById(R.id.layout_phone);
		mEmailRl = (RelativeLayout) findViewById(R.id.layout_email);
		mWeixinRl = (RelativeLayout) findViewById(R.id.layout_weixin);
		mQQRl = (RelativeLayout) findViewById(R.id.layout_qq);
		
		mStatusTv = (TextView) findViewById(R.id.id_status_tv);
		mModeTv = (TextView) findViewById(R.id.id_mode_tv);
		mSiteTv = (TextView) findViewById(R.id.id_site_tv);
		mNicknameTv = (TextView) findViewById(R.id.id_nickname_tv);
		mRealnameTv = (TextView) findViewById(R.id.id_realname_tv);
		mGenderTv = (TextView) findViewById(R.id.id_gender_tv);
		mPhoneTv = (TextView) findViewById(R.id.id_phone_tv);
		mEmailTv = (TextView) findViewById(R.id.id_email_tv);
		mWeixinTv = (TextView) findViewById(R.id.id_weixin_tv);
		mQQTv = (TextView) findViewById(R.id.id_qq_tv);
		
		mGenderDiv = findViewById(R.id.divider_gender);
		mPhoneDiv = findViewById(R.id.divider_phone);
		mEmailDiv = findViewById(R.id.divider_email);
		mWeixinDiv = findViewById(R.id.divider_weixin);
		mQQDiv = findViewById(R.id.divider_qq);
	}

	private void initView() {
		//initTopBarForLeftBack(R.string.mine_info);
		initFirstLayout();    	
		initLayoutWithTextview();
//		addListener();
		
		mHeadIv.setOnClickListener(this);
		
		if (mWorker != null) { // 个人资料页，点击设置头像和昵称
			mHeadphotoRl.setOnClickListener(this);
			mNicknameRl.setOnClickListener(this);
		}
	}
	
	
	private void initFirstLayout() {
		ImageLoader imgLoader = new ImageLoader(this, true, R.drawable.v5_photo_default, null);
		if (mWorker != null) {
			initTopBarForOnlyTitle(R.string.mine_info);
			imgLoader.DisplayImage(mWorker.getPhoto(), mHeadIv);
		} else if (mCoWorker != null) {
			initTopBarForOnlyTitle(R.string.worker_info);
			imgLoader.DisplayImage(mCoWorker.getPhoto(), mHeadIv);
		}
		
		// 昵称
		String nickname = null;
		if (mWorker != null) {
			nickname = mWorker.getNickname();
		} else if (mCoWorker != null) {
			nickname = mCoWorker.getName();
		}
		if (nickname != null && !nickname.isEmpty()) {
			mNicknameRl.setVisibility(View.VISIBLE);
			mNicknameTv.setText(nickname);
		} else {
			mNicknameRl.setVisibility(View.GONE);
		}
		
		// 状态
		int status = 0, mode = 0, accepts = 0, connects = 0;
		if (mWorker != null) {
			status = mWorker.getStatus();
			mode = mWorker.getMode();
			accepts = mWorker.getAccepts();
			connects = mWorker.getConnects();
		} else if (mCoWorker != null) {
			status = mCoWorker.getStatus();
			mode = mCoWorker.getMode();
			accepts = mCoWorker.getAccepts();
			connects = mCoWorker.getConnects();
		}
		setStatusTvText(status);
		setModeTvText(mode, accepts, connects);
		mSiteTv.setText(mApplication.getWorkerSp().readSiteName());
	}
	
	private void setStatusTvText(int status) {
    	switch (status) {
    	case QAODefine.STATUS_OFFLINE:
    		mStatusTv.setText(R.string.status_offline);
    		break;
    		
    	case QAODefine.STATUS_ONLINE:
    		mStatusTv.setText(R.string.status_online);
    		break;
    		
    	case QAODefine.STATUS_HIDE:
    		mStatusTv.setText(R.string.status_hide);
    		break;
    		
    	case QAODefine.STATUS_LEAVE:
    		mStatusTv.setText(R.string.status_leave);
    		break;
    		
    	case QAODefine.STATUS_BUSY:
    		mStatusTv.setText(R.string.status_busy);
    		break;    		
    	}
    }
    
    private void setModeTvText(int mode, int accepts, int connects) {
    	if (mode == QAODefine.MODE_AUTO) { // 自动接入
    		mModeTv.setText(getString(R.string.set_mode_auto)
    				+ "(" + accepts + ")");
    	} else if (mode == QAODefine.MODE_SWITCH_ONLY) { // 仅转接
    		mModeTv.setText(getString(R.string.set_mode_switchable)
    				+ "(" + connects + ")");
    	}
    }

	private void initLayoutWithTextview() {
		boolean isFirst = true;
		
		String realname = null;
		int gender = 0;
		String phone = null;
		String email = null;
		String qq = null;
		if (mWorker != null) {
			realname = mWorker.getRealname();
			gender = mWorker.getGender();
			phone = mWorker.getPhone();
			email = mWorker.getEmail();
			qq = mWorker.getQq();
		} else if (mCoWorker != null) {
			realname = mCoWorker.getRealname();
			gender = mCoWorker.getSex();
		}
		
		// 真实姓名
		if (realname != null && !realname.isEmpty()) {
			mRealnameRl.setVisibility(View.VISIBLE);
			mRealnameTv.setText(realname);
			isFirst = false;
		} else {
			mRealnameRl.setVisibility(View.GONE);
			if (isFirst) {
				mGenderDiv.setVisibility(View.GONE);
			}
		}
		// 性别
		if (gender == 1) {
			mGenderRl.setVisibility(View.VISIBLE);
			mGenderTv.setText(R.string.male);
			isFirst = false;
		} else if(gender == 2) {
			mGenderRl.setVisibility(View.VISIBLE);
			mGenderTv.setText(R.string.female);
			isFirst = false;
		} else {
			mGenderRl.setVisibility(View.GONE);
			if (isFirst) {
				mPhoneDiv.setVisibility(View.GONE);
			}
		}
		// 电话
		if (phone != null && !phone.isEmpty()) {
			mPhoneRl.setVisibility(View.VISIBLE);
			mPhoneTv.setText(phone);
			isFirst = false;
		} else {
			mPhoneRl.setVisibility(View.GONE);
			if (isFirst) {
				mEmailDiv.setVisibility(View.GONE);
			}
		}
		// 邮箱
		if (email != null && !email.isEmpty()) {
			mEmailRl.setVisibility(View.VISIBLE);
			mEmailTv.setText(email);
			isFirst = false;
		} else {
			mEmailRl.setVisibility(View.GONE);
			if (isFirst) {
				mWeixinDiv.setVisibility(View.GONE);
			}
		}
		// 微信
		mWeixinRl.setVisibility(View.GONE);
		mQQDiv.setVisibility(View.GONE);
		// QQ
		if (qq != null && !qq.isEmpty()) {
			mQQRl.setVisibility(View.VISIBLE);
			mQQTv.setText(qq);
			isFirst = false;
		} else {
			mQQRl.setVisibility(View.GONE);
		}
		
		if (isFirst) {
			findViewById(R.id.divider_second_layout_start).setVisibility(View.GONE);
			findViewById(R.id.divider_second_layout_end).setVisibility(View.GONE);
		} else {
			findViewById(R.id.divider_second_layout_start).setVisibility(View.VISIBLE);
			findViewById(R.id.divider_second_layout_end).setVisibility(View.VISIBLE);
		}
	}

//	private void addListener() {
//		mHeadphotoRl.setOnClickListener(this);
//		mStatusRl.setOnClickListener(this);
//		mModeRl.setOnClickListener(this);
//		mSiteRl.setOnClickListener(this);
//		mNicknameRl.setOnClickListener(this);
//		mRealnameRl.setOnClickListener(this);
//		mGenderRl.setOnClickListener(this);
//		mPhoneRl.setOnClickListener(this);
//		mEmailRl.setOnClickListener(this);
//		mWeixinRl.setOnClickListener(this);
//		mQQRl.setOnClickListener(this);
//	}
	
	@Override
	public void onClick(View v) {
		Logger.d(TAG, "onClick：" + v.getId());
		switch (v.getId()) {
		case R.id.layout_photo:
			new ActionSheetDialog(this)
				.builder()
				.setTitle("修改头像")
				.setCancelable(false)
				.setCanceledOnTouchOutside(true)
				.addSheetItem(getString(R.string.picture), SheetItemColor.Blue,
						new OnSheetItemClickListener() {
							@Override
							public void onClick(int which) {
								systemPhoto();
								showProgressDialog();
							}
						})
				.addSheetItem(getString(R.string.camera), SheetItemColor.Blue,
						new OnSheetItemClickListener() {
							@Override
							public void onClick(int which) {
								cameraPhoto();
								showProgressDialog();
							}
						}).show();
			break;
		case R.id.layout_nickname: {
			final EditText editText = new EditText(WorkerInfoActivity.this);
			editText.setText(mNicknameTv.getText());
			editText.setSelection(mNicknameTv.getText().length());
			
			new com.v5kf.mcss.ui.widget.AlertDialog(this)
				.builder()
				.setTitle(R.string.edit_nickname)
				.setView(editText)
				.setPositiveButton(R.string.confirm, new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if (!TextUtils.isEmpty(editText.getText())) {
							myNickname = editText.getText().toString();
							mNicknameTv.setText(myNickname);
							mWorker.setNickname(myNickname);
							if (mAppInfo.getCoWorker(w_id) != null) {
								mAppInfo.getCoWorker(w_id).setName(myNickname);
							}
							mHandler.obtainMessage(HDL_SET_NICKNAME_OK).sendToTarget();
						}
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
		}
			break;
		case R.id.id_photo_iv: // 头像图片
			if (mAppInfo.getCoWorker(w_id) != null && !TextUtils.isEmpty(mAppInfo.getCoWorker(w_id).getPhoto())) {
				gotoShowImageActivity(mAppInfo.getCoWorker(w_id).getPhoto());
			}
			break;
		default:
			break;
		}
	}
	
	private void gotoShowImageActivity(String pic_url) {
		this.gotoImageActivity(pic_url);
	}
	
	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case HDL_UPLOAD_PHOTO_FAILED:
			dismissProgressDialog();
			ShowToast("头像上传失败");
			break;
		case HDL_UPLOAD_PHOTO_OK:
			dismissProgressDialog();
			ShowToast("头像上传成功");
			initFirstLayout();
//			EventBus.getDefault().post(mWorker, EventTag.ETAG_UPDATE_USER_INFO);
			break;
		case HDL_SET_NICKNAME_OK:
			initFirstLayout();
//			EventBus.getDefault().post(mWorker, EventTag.ETAG_UPDATE_USER_INFO);
			break;
		default:
			break;
		}
	}

	/**
     * 打开系统相册
     */
    private void systemPhoto() {
    	mApplication.setAppForeground(); // 防止打开图库使得应用离线
    	
//        Intent intent = new Intent();
////		intent.setType("image/*");
//        intent.setDataAndType(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                "image/*");
//        intent.setAction(Intent.ACTION_PICK);
//        startActivityForResult(intent, Config.REQUEST_CODE_PHOTO);
        
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);//ACTION_OPEN_DOCUMENT  
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {                  
        	startActivityForResult(intent, Config.REQUEST_CODE_PHOTO_KITKAT);    
        } else {
        	startActivityForResult(intent, Config.REQUEST_CODE_PHOTO);   
        }
    }
 
    /**
     * 调用相机拍照
     */
    private void cameraPhoto() {
    	mApplication.setAppForeground(); // 防止打开图库使得应用离线
    	
        String sdStatus = Environment.getExternalStorageState();
        /* 检测sdcard是否可用 */
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "SD卡不可用！", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mImageFileName = FileUtil.getImageName("capture");        
        // 必须确保文件夹路径存在，否则拍照后无法完成回调 
        File vFile = new File(FileUtil.getImageSavePath(this), mImageFileName);
        File vDirPath = vFile.getParentFile();
        if(!vDirPath.exists()) {
        	vDirPath.mkdirs();
        }
        Uri uri = Uri.fromFile(vFile);
        Logger.d(TAG, "保存 Uri：" + FileUtil.getPath(this, uri));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, Config.REQUEST_CODE_CAMERA);
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if (requestCode == Config.REQUEST_CODE_CAMERA || 
				requestCode == Config.REQUEST_CODE_PHOTO_KITKAT ||
				requestCode == Config.REQUEST_CODE_PHOTO) {
			mApplication.setAppBackground(); // 防止打开图库使得应用离线
			
			if (data != null) {
				// 图库获取拍好的图片
				if (data.getData() != null) { //防止没有返回结果 
					Uri uri = data.getData();
					if (uri != null) {
						String filePath = FileUtil.getPath(getApplicationContext(), uri);
						Logger.i(TAG, "Photo:" + filePath);
						uploadPhoto(filePath);
						return;
					}
				}
			} else if (resultCode == RESULT_OK) {
				// 拍照返回
				String filePath = FileUtil.getImageSavePath(this) + "/" + mImageFileName;
				Logger.i(TAG, "Camera:" + filePath);
				uploadPhoto(filePath);
				return;
			}
			dismissProgressDialog();
		}
    }

	private void uploadPhoto(String filePath) {
		String auth = mApplication.getWorkerSp().readAuthorization();
		String url = Config.APP_PIC_AUTH_URL + auth;
		getPictureService(filePath, url, auth);
	}

	private void getPictureService(final String filePath, String url, String auth) {
		V5HttpUtil.getPicService(url, auth, new HttpResponseHandler(this) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				Logger.i(TAG, "getPictureService responseString:" + responseString);
				if (statusCode == 200) {
					try {
						JSONObject js = new JSONObject(responseString);
						if (js.has("url")) { // 成功返回signature
							String authorization = js.getString("authorization");
							String url = js.getString("url");
							postImageAndSend(filePath, url, authorization);
							return;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					mHandler.obtainMessage(HDL_UPLOAD_PHOTO_FAILED).sendToTarget();
				}
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "getPictureService statusCode:" + statusCode + " responseString:" + responseString);
				mHandler.obtainMessage(HDL_UPLOAD_PHOTO_FAILED).sendToTarget();
			}
		});
	}
	
	private void postImageAndSend(String filePath, String url, String authorization) {
		if (null == url || null == authorization) {
			mHandler.obtainMessage(HDL_UPLOAD_PHOTO_FAILED).sendToTarget();
			return;
		}
		V5HttpUtil.postLocalImage(url, filePath, authorization, null, new HttpResponseHandler(this) {
			
			@Override
			public void onSuccess(int statusCode, String responseString) {
				// 解析图片地址
				Logger.i(TAG, "[postLocalImage] success responseString:" + responseString);
				try {
					JSONObject js = new JSONObject(responseString);
					int code = js.getInt("code");
					JSONObject data = js.optJSONObject("data");
					if (null != data && 0 == code) {
						String download_url = data.optString("download_url");
						if (null != download_url) {
							myPhoto = download_url;
							mWorker.setPhoto(myPhoto);
							if (mAppInfo.getCoWorker(w_id) != null) {
								mAppInfo.getCoWorker(w_id).setPhoto(myPhoto);
							}
							// 退出时发送
//							WorkerRequest wReq = (WorkerRequest)RequestManager.getRequest(QAODefine.O_TYPE_WWRKR, getContext());
//							wReq.setWorkerInfo(mWorker);
							mApplication.getWorkerSp().saveWorkerPhoto(myPhoto);
							mHandler.obtainMessage(HDL_UPLOAD_PHOTO_OK).sendToTarget();
							return;
						} else {
							mHandler.obtainMessage(HDL_UPLOAD_PHOTO_FAILED).sendToTarget();
							return;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				mHandler.obtainMessage(HDL_UPLOAD_PHOTO_FAILED).sendToTarget();
			}
			
			@Override
			public void onFailure(int statusCode, String responseString) {
				Logger.e(TAG, "[postLocalImage] failure(" + statusCode + 
						") responseString:" + responseString);
				mHandler.obtainMessage(HDL_UPLOAD_PHOTO_FAILED).sendToTarget();
			}
		});
	}
	
	/***** event *****/
	
	@Subscriber(tag = EventTag.ETAG_CONNECTION_CHANGE, mode = ThreadMode.MAIN)
	private void connectionChange(Boolean isConnect) {
		if (isConnect) {
			
		} else {
			finishActivity();
		}
	}

}
