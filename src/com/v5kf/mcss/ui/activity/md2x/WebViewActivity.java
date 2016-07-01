package com.v5kf.mcss.ui.activity.md2x;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.v5kf.mcss.R;
import com.v5kf.mcss.ui.view.ActionItem;
import com.v5kf.mcss.ui.view.TitlePopup;
import com.v5kf.mcss.ui.view.TitlePopup.OnItemOnClickListener;
import com.v5kf.mcss.utils.Logger;

public class WebViewActivity extends BaseToolbarActivity {
	
	private WebView mWebView;
	private String mUrl;
	private int mTitleId;
//	private LinearLayout mLoadingLl;
	private SwipeRefreshLayout mSwipeLayout;
	
	// 标题栏弹窗
	private TitlePopup mTitlePopup;
	private ImageView mMoreIv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_md2x_web_view);
		
		handleIntent();
		findView();
		initView();
//		mLoadingLl.setVisibility(View.VISIBLE);
		showProgressDialog();
	}
	
	@Override
	public void onBackPressed() {
		if (mWebView != null && mWebView.canGoBack()) {
			mWebView.goBack();
		} else {
			finishActivity();
		}
	}

	private void handleIntent() {
		Intent intent = getIntent();
		mUrl = intent.getStringExtra("url");
		mTitleId = intent.getIntExtra("title", 0);
		if (null == mUrl || mUrl.isEmpty()) {
			Logger.w("webViewActivity", "Got null url.");
			finishActivity();
			return;
		}
		Logger.w("webViewActivity", "Got url:" + mUrl);
	}

	private void findView() {
		mWebView = (WebView) findViewById(R.id.id_web_view);
//		mLoadingLl = (LinearLayout) findViewById(R.id.layout_container_empty);
		mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
		mMoreIv = (ImageView) findViewById(R.id.more_iv);
	}

	private void initView() {
		initTitleBar();
		
		mSwipeLayout.setColorSchemeColors(R.color.green, R.color.red,  
        	    R.color.blue, R.color.yellow);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {  
              
            @Override  
            public void onRefresh() {  
                //重新刷新页面  
            	mWebView.reload();  
            }  
        });
        
        WebChromeClient wvcc = new WebChromeClient() {  
            @Override  
            public void onReceivedTitle(WebView view, String title) {  
                super.onReceivedTitle(view, title);  
                Logger.d("ANDROID_LAB", "TITLE=" + title); 
                if (mTitleId == 0 && title != null && !title.isEmpty()) {
                	getToolbar().setTitle(title);  
                }
            }  
        };  
        // 设置setWebChromeClient对象  
        mWebView.setWebChromeClient(wvcc);
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
//				mSwipeLayout.setVisibility(View.GONE);
//				mLoadingLl.setVisibility(View.VISIBLE);
				showProgressDialog();
		        return true;
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
//				mSwipeLayout.setVisibility(View.VISIBLE);
//				mLoadingLl.setVisibility(View.GONE);
				dismissProgressDialog();
				mSwipeLayout.setRefreshing(false);
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				view.loadUrl("file:///android_asset/404.html");
				mSwipeLayout.setRefreshing(false);
			}
		});
		mWebView.loadUrl(mUrl);
		
		mMoreIv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mTitlePopup.show(v);
			}
		});
	}

	private void initTitleBar() {
		initPopupMenu();
		if (mTitleId == 0) {
			initTopBarForLeftBack(R.string.app_name);
		} else {
			initTopBarForLeftBack(mTitleId);
		}
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
    	
    	mTitlePopup.addAction(new ActionItem(this, R.string.app_refresh, R.drawable.v5_popmenu_refresh));
    	mTitlePopup.addAction(new ActionItem(this, R.string.open_by_browser, R.drawable.v5_popmenu_browser));
    	
    	mTitlePopup.setItemOnClickListener(new OnItemOnClickListener() {
			
			@Override
			public void onItemClick(ActionItem item, int position) {
				switch (position) {
				case 0:
					Logger.d("WebViewActivity", "点击刷新");
					mWebView.reload();
					break;
				case 1:
					Logger.d("WebViewActivity", "点击在浏览器中打开");
					Intent intent = new Intent();   
			        intent.setAction("android.intent.action.VIEW");    
			        Uri content_url = Uri.parse(mUrl);   
			        intent.setData(content_url);
			        startActivity(intent);
				}
			}
		});
	}

	@Override
	protected void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		
	}

}
