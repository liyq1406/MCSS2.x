package com.v5kf.mcss.ui.fragment.md2x;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chyrain.fragment.LazyFragment;
import com.chyrain.irecyclerview.RefreshRecyclerView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.v5kf.client.lib.V5HttpUtil;
import com.v5kf.client.lib.callback.HttpResponseHandler;
import com.v5kf.client.lib.entity.V5ArticleBean;
import com.v5kf.client.lib.entity.V5ArticlesMessage;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MusicMessage;
import com.v5kf.mcss.CustomApplication;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.entity.AppInfoKeeper;
import com.v5kf.mcss.ui.activity.md2x.ActivityBase;
import com.v5kf.mcss.ui.activity.md2x.BaseChatActivity;
import com.v5kf.mcss.ui.adapter.MaterialRecyclerAdapter;
import com.v5kf.mcss.ui.view.V5RefreshLayout;
import com.v5kf.mcss.utils.DevUtils;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.cache.URLCache;

public class MaterialBaseFragment extends LazyFragment {

	protected static final String TAG = "MaterialBaseFragment";
	protected ActivityBase mParentActivity;
	protected CustomApplication mApplication;
	protected AppInfoKeeper mAppInfo;
	protected FragmentHandler mHandler;
	protected int mIndex;
	
	/* 素材类型 */
	public static final int TYPE_IMG = 1;
	public static final int TYPE_NEWS = 2;
	public static final int TYPE_MUSIC = 3;
	protected static final int ITEMS_PER_PAGE = 20;
	
	protected static final int HDL_STOP_REFRESH = 11;
	protected static final int HDL_STOP_LOAD = 12;
	protected static final int HDL_UPDATE_UI = 13;
	protected static final int HDL_TIME_OUT = 14;
	
	
	private int mMaterialType;
	private int mCurPage;
	private int mTotal = 0;
	private int mCount = 0;
	private boolean mHasMore = true;
	
	/* 瀑布流列表 */
	private RefreshRecyclerView mRefreshRecyclerView;
	/* 瀑布流适配器 */
	private MaterialRecyclerAdapter mAdapter;
	/* 数据源 */
	private List<V5Message> mDatas;
	
	private StaggeredGridLayoutManager mLayoutManager;
	
	private TextView mEmptyTipsTv;
	private ProgressBar mLoadingPb;
	
	/* 刷新标识 */
	private boolean isLoadingMore = false;
	
	public MaterialBaseFragment() {
		// TODO Auto-generated constructor stub
		this.mApplication = (CustomApplication) mParentActivity.getApplication();
		this.mAppInfo = mApplication.getAppInfo();
		this.mHandler = new FragmentHandler(this);
	}
	
	public MaterialBaseFragment(ActivityBase activity, int index) {
		this.mParentActivity = activity;
		this.mIndex = index;
		this.mMaterialType = index + 1;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		
	}
	
	@Override
	protected void onCreateViewLazy(Bundle savedInstanceState) {
		super.onCreateViewLazy(savedInstanceState);
		setContentView(R.layout.fragment_md2x_material);
		
		// 注册event对象
        EventBus.getDefault().register(this);
        
        findView();
		initView();
		initData();
		loadData();
	}
	
	@Override
	protected void onDestroyViewLazy() {
		super.onDestroyViewLazy();
		
		// 注销event对象
        EventBus.getDefault().unregister(this);
	}
	
	protected void onRefreshTimeOut() {
		mParentActivity.ShowToast(R.string.on_refresh_time_out);
	}
	
	/**
	 * 使用Handler传递消息
	 * @param msg
	 */
	public void sendHandlerMessage(Message msg) {
		mHandler.sendMessage(msg);
	}
	
	/**
	 * 静态Handler类
	 * @author V5KF_MBP
	 *
	 */
	static class FragmentHandler extends Handler {
		
		WeakReference<MaterialBaseFragment> mFragment;
		
		public FragmentHandler(MaterialBaseFragment fragment) {
			mFragment = new WeakReference<MaterialBaseFragment>(fragment);
		}
		
		public void handleMessage(Message msg) {
			super.handleMessage(msg);			
			mFragment.get().handleMessage(msg, mFragment.get().mParentActivity);
		};
	}
	
	private void initData() {
		mCurPage = 1;
	}

	private void findView() {
		mRefreshRecyclerView = (RefreshRecyclerView) findViewById(R.id.id_material_recycler);
		mLoadingPb = (ProgressBar)findViewById(R.id.id_loading_progress);
	}

	private void initView() {
		initRecyclerView();
		
		if (mRefreshRecyclerView.getEmptyView() != null) {
			mEmptyTipsTv = (TextView) mRefreshRecyclerView.getEmptyView().findViewById(R.id.layout_container_tv);
		}
        /* 空白按钮 */
		if (mEmptyTipsTv != null) {
			mEmptyTipsTv.setText(R.string.material_empty_tips);
			mEmptyTipsTv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					loadData();
				}
			});
		}
	}
	
	private void initRecyclerView() {
        /* 适配器初始化 */
        mDatas = new ArrayList<V5Message>();
        mAdapter = new MaterialRecyclerAdapter(this, mParentActivity, mDatas);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        
        mRefreshRecyclerView.setLayoutManager(mLayoutManager);
    	mRefreshRecyclerView.getRefreshableView().setItemAnimator(new DefaultItemAnimator());
    	mRefreshRecyclerView.setAdapter(mAdapter);
    	mRefreshRecyclerView.getRefreshableView().setScrollbarFadingEnabled(true);
    	mRefreshRecyclerView.getRefreshableView().setScrollBarStyle(RecyclerView.SCROLLBAR_POSITION_RIGHT);
    	mRefreshRecyclerView.setHasPullUpFriction(false); // 没有上拉阻力
    	mRefreshRecyclerView.setFooterLayout(new V5RefreshLayout(mApplication, Mode.PULL_FROM_END));
    	mRefreshRecyclerView.setOnRefreshListener(new OnRefreshListener2<RecyclerView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
//				onRefresh();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<RecyclerView> refreshView) {
				if (isLoadingMore) {
                	// 正在刷新中取消执行
                } else {
                	isLoadingMore = true;
                	if (mHasMore) {
                    	loadData();
                		mHandler.sendEmptyMessageDelayed(HDL_STOP_LOAD, 5000);
                	} else {
                		mHandler.sendEmptyMessage(HDL_STOP_LOAD);
                		mParentActivity.ShowShortToast(R.string.no_more);
                	}
                	Logger.i(TAG, "上拉加载 ...");
                }
			}
		});
	}
	
	private void showProgress() {
		if (mLoadingPb != null) {
			mLoadingPb.setVisibility(View.VISIBLE);
		}
	}
	
	private void dismissProgress() {
		if (mLoadingPb != null) {
			mLoadingPb.setVisibility(View.GONE);
		}
	}
	
	private static String getAbsoluteUrl(int type, int page, int numPerPage) {
		switch (type) {
		case TYPE_IMG:
			return String.format(Locale.CHINESE, Config.GET_IMAGE_URL, Config.SITE_ID, page, numPerPage);
		case TYPE_NEWS:
			return String.format(Locale.CHINESE, Config.GET_NEWS_URL, Config.SITE_ID, page, numPerPage);
		case TYPE_MUSIC:
			return String.format(Locale.CHINESE, Config.GET_MUSIC_URL, Config.SITE_ID, page, numPerPage);
		default:
			return String.format(Locale.CHINESE, Config.GET_IMAGE_URL, Config.SITE_ID, page, numPerPage);
		}
	}
	
	private void loadData() {
		showProgress();
		
		final String url = getAbsoluteUrl(mMaterialType, mCurPage, ITEMS_PER_PAGE);
		// 1.先获取对应的url缓存
		final URLCache urlCache = new URLCache();
		String responseString = urlCache.get(url);
		if (null != responseString) {
			try {
				Logger.d(TAG, "[loadData]:" + responseString + " of url:" + url);
				JSONObject resp = new JSONObject(responseString);
				if (resp.getString("state").equals("ok")) {
					mTotal = resp.getInt("total");
					JSONArray items = resp.getJSONArray("items");
					addDataGroup(items);						
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			mHandler.sendEmptyMessage(HDL_STOP_LOAD);
			return;
		}
		
		// 2.无缓存则发起GET请求
		V5HttpUtil.get(url, new HttpResponseHandler(mParentActivity) {
			@Override
			public void onSuccess(int statusCode, String responseString) {
				try {
					responseString = DevUtils.decodeUnicode(responseString);
					urlCache.put(url, responseString);
					Logger.d(TAG, "[MaterialHttpClient]:" + responseString);
					JSONObject resp = new JSONObject(responseString);
					if (resp.getString("state").equals("ok")) {
						mTotal = resp.getInt("total");
						JSONArray items = resp.getJSONArray("items");
						addDataGroup(items);						
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				mHandler.sendEmptyMessage(HDL_STOP_LOAD);
			}

			@Override
			public void onFailure(int statusCode, String responseString) {
				mParentActivity.ShowToast("获取素材资源失败");
				mHandler.sendEmptyMessage(HDL_STOP_LOAD);
			}
		});
		
//		MaterialHttpClient.get(mMaterialType, mCurPage, new TextHttpResponseHandler() {
//			
//			@Override
//			public void onSuccess(int statusCode, Header[] headers,
//					String responseString) {
//				try {
//					responseString = DevUtils.decodeUnicode(responseString);
//					Logger.d(TAG, "[MaterialHttpClient]:" + responseString);
//					JSONObject resp = new JSONObject(responseString);
//					if (resp.getString("state").equals("ok")) {
//						mTotal = resp.getInt("total");
//						JSONArray items = resp.getJSONArray("items");
//						addDataGroup(items);						
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//				mHandler.sendEmptyMessage(HDL_STOP_REFRESH);
//				mHandler.sendEmptyMessage(HDL_STOP_LOAD);
//			}
//			
//			@Override
//			public void onFailure(int statusCode, Header[] headers,
//					String responseString, Throwable throwable) {
//				ShowToast("获取素材资源失败");
//				mHandler.sendEmptyMessage(HDL_STOP_REFRESH);
//			}
//		});
		
		// 转到下一页
		mCurPage++;
	}
	
	protected void addDataGroup(JSONArray items) throws JSONException {
		if (null == items) {
			return;
		}
		
		if (items.length() > 0) {
			int i = 0;
			for (i = 0; i < items.length(); i++) {
				JSONObject item = items.getJSONObject(i);
				V5Message msgContent = null;
				switch (mMaterialType) {
				case TYPE_IMG:
					msgContent = new V5ImageMessage();
					((V5ImageMessage)msgContent).setMessage_type(QAODefine.MSG_TYPE_IMAGE);
					((V5ImageMessage)msgContent).setTitle(item.getString("title"));
					((V5ImageMessage)msgContent).setPic_url(item.getString("url"));
					((V5ImageMessage)msgContent).setMedia_id(item.getString("media_id"));
					break;
				
				case TYPE_MUSIC:
					msgContent = new V5MusicMessage();
					msgContent.setMessage_type(QAODefine.MSG_TYPE_MUSIC);
					((V5MusicMessage)msgContent).setTitle(item.getString("title"));
					((V5MusicMessage)msgContent).setDescription(item.getString("desc"));
					((V5MusicMessage)msgContent).setMusic_url(item.getString("url"));
					((V5MusicMessage)msgContent).setHq_music_url(item.getString("url"));
					break;
					
				case TYPE_NEWS:
					msgContent = new V5ArticlesMessage();
					msgContent.setMessage_type(QAODefine.MSG_TYPE_NEWS);
					V5ArticleBean article = new V5ArticleBean();
					article.setTitle(item.getString("title"));
					article.setDescription(item.getString("desc"));
					article.setPic_url(item.getString("picurl"));
					article.setUrl(item.getString("link"));
					ArrayList<V5ArticleBean> multi_article = new ArrayList<V5ArticleBean>();
					multi_article.add(article);
					((V5ArticlesMessage)msgContent).setArticles(multi_article);
					break;
				}
				msgContent.setDirection(QAODefine.MSG_DIR_TO_CUSTOMER);
				//msgContent.setCreate_time(DateUtil.stringDateToLong(item.getString("create_time")) / 1000);
				mDatas.add(msgContent);
			}
			mCount += i;
			if (mCount >= mTotal) {
				mHasMore = false;
			}
		} else {
			mParentActivity.ShowToast(R.string.no_more);
		}
		
		mHandler.obtainMessage(HDL_UPDATE_UI).sendToTarget();
	}

	protected void handleMessage(Message msg, ActivityBase activityBase) {
		switch (msg.what) {
		case BaseChatActivity.HDL_WHAT_CANDIDATE_SEND:
			Bundle bundle = msg.getData();
			int pos = bundle.getInt(BaseChatActivity.MSG_KEY_POSITION);
			V5Message msgContent = mDatas.get(pos);
			Intent data = new Intent();
			Bundle extra = new Bundle();
			extra.putSerializable("message_content", msgContent);
			data.putExtras(extra);
			mParentActivity.setResult(Config.RESULT_CODE_MATERIAL_SEND, data);
			mParentActivity.finishActivity();
			break;
			
		case HDL_STOP_REFRESH: // 停止刷新
//			if (mRefreshRecyclerView.isRefreshing()) {
//				mRefreshRecyclerView.onRefreshComplete();
//			}
//			break;
			
		case HDL_STOP_LOAD: // 停止加载
			if (isLoadingMore) {
				isLoadingMore = false;
			}
			if (mRefreshRecyclerView.isRefreshing()) {
				mRefreshRecyclerView.onRefreshComplete();
			}
//			break;
		case HDL_UPDATE_UI: // 停止加载
			mAdapter.notifyDataSetChanged();
			dismissProgress();
			break;
		}
	}
}
