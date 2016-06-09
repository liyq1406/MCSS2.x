package com.v5kf.mcss.ui.activity.info;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.View.OnClickListener;

import com.v5kf.client.lib.V5HttpUtil;
import com.v5kf.client.lib.callback.HttpResponseHandler;
import com.v5kf.client.lib.entity.V5ArticleBean;
import com.v5kf.client.lib.entity.V5ArticlesMessage;
import com.v5kf.client.lib.entity.V5ImageMessage;
import com.v5kf.client.lib.entity.V5Message;
import com.v5kf.client.lib.entity.V5MusicMessage;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.QAODefine;
import com.v5kf.mcss.ui.activity.BaseActivity;
import com.v5kf.mcss.ui.activity.BaseChatActivity;
import com.v5kf.mcss.ui.adapter.MaterialRecyclerAdapter;
import com.v5kf.mcss.utils.DateUtil;
import com.v5kf.mcss.utils.DevUtils;
import com.v5kf.mcss.utils.Logger;
import com.v5kf.mcss.utils.cache.URLCache;

public class MaterialResActivity extends BaseActivity implements OnRefreshListener {
	private static final String TAG = "MaterialResActivity";
	/* 素材类型 */
	public static final int TYPE_IMG = 1;
	public static final int TYPE_NEWS = 2;
	public static final int TYPE_MUSIC = 3;
	
	protected static final int HDL_STOP_REFRESH = 11;
	protected static final int HDL_STOP_LOAD = 12;
	
	private static final int ITEMS_PER_PAGE = 20;
	private int mMaterialType;
	private int mCurPage;
	private int mTotal = 0;
	private int mCount = 0;
	private boolean mHasMore = true;
	
	/* 瀑布流列表 */
	private RecyclerView mRecyclerView;
	/* 瀑布流适配器 */
	private MaterialRecyclerAdapter mAdapter;
	/* 数据源 */
	private List<V5Message> mDatas;
	
	private StaggeredGridLayoutManager mLayoutManager;
	
	/* 刷新控件 */
	private SwipeRefreshLayout mSwipeRefresh;
	private boolean isLoadingMore = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_material_res);
		handleIntent();
		findView();
		initView();
		initData();
		loadData();
	}

	private void handleIntent() {
		Intent i = getIntent();
		mMaterialType = i.getIntExtra("type", 0);
		if (0 == mMaterialType) { // 不传type默认为图片
			mMaterialType = TYPE_IMG;
		}
	}

	private void initData() {
		mCurPage = 1;
	}

	private void findView() {
		mRecyclerView = (RecyclerView) findViewById(R.id.id_material_list);
		mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
	}

	private void initView() {
		initTopBarForLeftBack(R.string.title_material);
		initRecyclerView();
		
		/* 刷新控件 */
        if (null == mSwipeRefresh) {
        	mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        }
        mSwipeRefresh.setOnRefreshListener(this);
        mSwipeRefresh.setColorSchemeColors(R.color.green, R.color.red,  
        	    R.color.blue, R.color.yellow);
        mSwipeRefresh.setEnabled(false);
        
        /* 空白按钮 */
        findViewById(R.id.layout_container_tv).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				onRefresh();
			}
		});
        
        /* 上拉监听 */
        mRecyclerView.addOnScrollListener(new OnScrollListener() {
        	@Override
        	public void onScrollStateChanged(RecyclerView recyclerView,
        			int newState) {
        		Logger.i(TAG, "onScrollStateChanged...");
        		super.onScrollStateChanged(recyclerView, newState);
        		int[] visibleItems = mLayoutManager.findLastVisibleItemPositions(null);
        		int lastitem = Math.max(visibleItems[0], visibleItems[1]);
        		if (newState == RecyclerView.SCROLL_STATE_IDLE && lastitem + 1 == mAdapter.getItemCount()
        				&& mAdapter.getItemCount() >= ITEMS_PER_PAGE) {
        			Logger.i(TAG, "onScrollStateChanged...");
                    if (isLoadingMore) {
                    	// 正在刷新中取消执行
                    } else {
                    	isLoadingMore = true;
                    	if (mHasMore) {
                        	setLoadMoreVisible(true);
                        	loadData();
                    		mHandler.sendEmptyMessageDelayed(HDL_STOP_LOAD, 5000);
                    	} else {
                    		mHandler.sendEmptyMessage(HDL_STOP_LOAD);
                    		ShowShortToast(R.string.no_more);
                    	}
                    	Logger.i(TAG, "上拉加载 ...");
                    }
                }
        	}
        	
        	@Override
        	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        		super.onScrolled(recyclerView, dx, dy);        		        		
        	}
		});
	}
	
	private void initRecyclerView() {
        /* 适配器初始化 */
        mDatas = new ArrayList<V5Message>();
        mAdapter = new MaterialRecyclerAdapter(this, mDatas);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
	}
	
	private static String getAbsoluteUrl(int type, int page) {
		switch (type) {
		case TYPE_IMG:
			return String.format(Locale.CHINESE, Config.GET_IMAGE_URL, Config.SITE_ID, page);
		case TYPE_NEWS:
			return String.format(Locale.CHINESE, Config.GET_NEWS_URL, Config.SITE_ID, page);
		case TYPE_MUSIC:
			return String.format(Locale.CHINESE, Config.GET_MUSIC_URL, Config.SITE_ID, page);
		default:
			return String.format(Locale.CHINESE, Config.GET_IMAGE_URL, Config.SITE_ID, page);
		}
	}
	
	private void loadData() {
		final String url = getAbsoluteUrl(mMaterialType, mCurPage);
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
			mHandler.sendEmptyMessage(HDL_STOP_REFRESH);
			mHandler.sendEmptyMessage(HDL_STOP_LOAD);
			return;
		}
		
		// 2.无缓存则发起GET请求
		V5HttpUtil.get(url, new HttpResponseHandler(this) {
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
				mHandler.sendEmptyMessage(HDL_STOP_REFRESH);
				mHandler.sendEmptyMessage(HDL_STOP_LOAD);
			}

			@Override
			public void onFailure(int statusCode, String responseString) {
				ShowToast("获取素材资源失败");
				mHandler.sendEmptyMessage(HDL_STOP_REFRESH);
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
				msgContent.setCreate_time(DateUtil.stringDateToLong(item.getString("create_time")) / 1000);
				mDatas.add(msgContent);
			}
			mCount += i;
			if (mCount >= mTotal) {
				mHasMore = false;
			}
		} else {
			ShowToast("没有更多了");
		}
		
		mAdapter.notifyDataSetChanged();
		checkListEmpty();
	}

	private void checkListEmpty() {
    	if (null == mRecyclerView) {
    		mRecyclerView = (RecyclerView) findViewById(R.id.id_material_list);
    	}
    	if (mDatas.size() == 0) {
//    		mRecyclerView.setVisibility(View.GONE);
    		mSwipeRefresh.setVisibility(View.GONE);
			findViewById(R.id.layout_container_empty).setVisibility(View.VISIBLE);
		} else {
//			mRecyclerView.setVisibility(View.VISIBLE);
			mSwipeRefresh.setVisibility(View.VISIBLE);
			findViewById(R.id.layout_container_empty).setVisibility(View.GONE);
		}
    }
	
	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case BaseChatActivity.HDL_WHAT_CANDIDATE_SEND:
			Bundle bundle = msg.getData();
			int pos = bundle.getInt(BaseChatActivity.MSG_KEY_POSITION);
			V5Message msgContent = mDatas.get(pos);
			Intent data = new Intent();
			Bundle extra = new Bundle();
			extra.putSerializable("message_content", msgContent);
			data.putExtras(extra);
			setResult(Config.RESULT_CODE_MATERIAL_SEND, data);
			finishActivity();
			break;
			
		case HDL_STOP_REFRESH: // 停止刷新
			if (mSwipeRefresh.isRefreshing()) {
				mSwipeRefresh.setRefreshing(false);
			}
			break;
			
		case HDL_STOP_LOAD: // 停止加载
			if (isLoadingMore) {
				isLoadingMore = false;
				setLoadMoreVisible(false);
			}
			break;
		}
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
//		mCurPage = 1;
//		mDatas.clear();
//		loadData();
	}
	
	private void setLoadMoreVisible(boolean visible) {
		if (visible) {
			findViewById(R.id.load_more_layout).setVisibility(View.VISIBLE);
			mRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
		} else {
			findViewById(R.id.load_more_layout).setVisibility(View.GONE);
//			mRecyclerView.scrollToPosition(mRecyclerAdapter.getItemCount() - 1);
		}
	}
}
