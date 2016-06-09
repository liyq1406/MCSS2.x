package com.v5kf.mcss.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PullRefreshRecyclerView extends LinearLayout {
	static final int VERTICAL = LinearLayoutManager.VERTICAL;
	static final int HORIZONTAL = LinearLayoutManager.HORIZONTAL;
	private RecyclerView recyclerView;
	private SwipeRefreshLayout swipeRfl;
	private LinearLayoutManager layoutManager;
	private OnRefreshListener mRefreshListener;
	private RecyclerView.OnScrollListener mScrollListener;
//	private RecyclerView.Adapter mAdapter;
	private RefreshLoadMoreListener mRefreshLoadMoreListner;
	private LinearLayout mExceptView;
	private boolean hasMore = true;
	private boolean isRefresh = false;
	private boolean isLoadMore = false;

	/**
	 * 异常图片控件
	 */
	private static ImageView exceptIv;
	
	/**
	 * 异常内容文本控件
	 */
	private static TextView exceptTv;

	public PullRefreshRecyclerView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public PullRefreshRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LinearLayout rootLl = new LinearLayout(context);
		rootLl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mExceptView = initExceptionView(context);
		mExceptView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mExceptView.setVisibility(View.GONE);
		swipeRfl = new SwipeRefreshLayout(context);
		swipeRfl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		FrameLayout bootLl = new FrameLayout(context);
		bootLl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		recyclerView = new RecyclerView(context);
		recyclerView.setVerticalScrollBarEnabled(true);
		recyclerView.setHorizontalScrollBarEnabled(true);
		recyclerView.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		bootLl.addView(recyclerView);
		bootLl.addView(mExceptView);
		swipeRfl.addView(bootLl);
		rootLl.addView(swipeRfl);
		this.addView(rootLl);
		// 导入布局
		// LayoutInflater.from(context).inflate(
		// R.layout.pulltorefreshrecyclerview, this, true);
		// swipeRfl = (SwipeRefreshLayout) findViewById(R.id.all_swipe);
		// recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
		// 加载颜色是循环播放的，只要没有完成刷新就会一直循环，color1>color2>color3>color4
		// swipeRfl.setColorScheme(Color.BLUE, Color.GREEN, Color.RED,
		// Color.YELLOW);

		/**
		 * 监听上拉至底部滚动监听
		 */
		mScrollListener = new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				int lastVisibleItem = layoutManager
						.findLastVisibleItemPosition();
				int firstVisibleItem = layoutManager
						.findFirstCompletelyVisibleItemPosition();
				if (firstVisibleItem == 0) {
					setPullRefreshEnable(true);
				} else {
					setPullRefreshEnable(false);
				}

				int totalItemCount = layoutManager.getItemCount();
				// lastVisibleItem >= totalItemCount - 4 表示剩下4个item自动加载
				// dy>0 表示向下滑动
				/*
				 * if (hasMore && (lastVisibleItem >= totalItemCount - 1) && dy
				 * > 0 && !isLoadMore) { isLoadMore = true; loadMore(); }
				 */
				/**
				 * 无论水平还是垂直
				 */
				if (hasMore && (lastVisibleItem >= totalItemCount - 1)
						&& !isLoadMore) {
					isLoadMore = true;
					loadMore();
				}

			}
		};

		/**
		 * 下拉至顶部刷新监听
		 */
		mRefreshListener = new OnRefreshListener() {

			@Override
			public void onRefresh() {
				if (!isRefresh) {
					isRefresh = true;
					refresh();
				}
			}
		};
		swipeRfl.setOnRefreshListener(mRefreshListener);
		recyclerView.setHasFixedSize(true);
		layoutManager = new LinearLayoutManager(context);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setOnScrollListener(mScrollListener);
	}
	
	public void setRecyclerOnTouchListener(OnTouchListener listener) {
		recyclerView.setOnTouchListener(listener);
	}

	public void setOrientation(int orientation) {
		if (orientation != 0 && orientation != 1) {
			layoutManager.setOrientation(VERTICAL);
		} else {
			layoutManager.setOrientation(orientation);
		}
	}

	public int getOrientation() {
		return layoutManager.getOrientation();
	}

	public void setVertical() {
		layoutManager.setOrientation(VERTICAL);
	}

	public void setHorizontal() {
		layoutManager.setOrientation(HORIZONTAL);
	}

	public void setPullLoadMoreEnable(boolean enable) {
		this.hasMore = enable;
	}

	public boolean getPullLoadMoreEnable() {
		return hasMore;
	}

	public void setPullRefreshEnable(boolean enable) {
		swipeRfl.setEnabled(enable);
	}

	public boolean getPullRefreshEnable() {
		return swipeRfl.isEnabled();
	}
	
	public void addItemDecoration(ItemDecoration decor) {
		recyclerView.addItemDecoration(decor);
	}

	public void setLoadMoreListener() {
		recyclerView.setOnScrollListener(mScrollListener);
	}

	public void loadMore() {
		if (mRefreshLoadMoreListner != null && hasMore) {
			mRefreshLoadMoreListner.onLoadMore();

		}
	}

	/**
	 * 加载更多完毕,为防止频繁网络请求,isLoadMore为false才可再次请求更多数据
	 */
	public void setLoadMoreCompleted() {
		isLoadMore = false;
	}

	public void stopRefresh() {
		isRefresh = false;
		swipeRfl.setRefreshing(false);
	}

	public void setRefreshLoadMoreListener(RefreshLoadMoreListener listener) {
		mRefreshLoadMoreListner = listener;
	}

	public void refresh() {
		recyclerView.setVisibility(View.VISIBLE);
		mExceptView.setVisibility(View.INVISIBLE);
		if (mRefreshLoadMoreListner != null) {
			mRefreshLoadMoreListner.onRefresh();
		}
	}

	public void scrollToTop() {
		recyclerView.scrollToPosition(0);
	}
	
	public void scrollToPosition(int position) {
		recyclerView.scrollToPosition(position);
	}

	public void setAdapter(RecyclerView.Adapter<?> adapter) {
		if (adapter != null)
			recyclerView.setAdapter(adapter);
	}

	private LinearLayout initExceptionView(Context context) {
		LinearLayout rootLl = new LinearLayout(context);
		rootLl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		rootLl.setOrientation(VERTICAL);
		rootLl.setGravity(Gravity.CENTER);
		exceptIv = new ImageView(context);
		exceptIv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 点击图片刷新
				if (!isRefresh) {
					swipeRfl.setRefreshing(true);
					isRefresh = true;
					refresh();
				}
			}
		});
		//底部边距
		LinearLayout.LayoutParams ll = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		ll.setMargins(0, 0, 0, 32);
		exceptIv.setLayoutParams(ll);
		exceptTv = new TextView(context);
		exceptTv.setLayoutParams(ll);
		exceptTv.setTextSize(20.0f);
		exceptTv.setTextColor(Color.DKGRAY);
		//提示操作文本
		TextView alertTv = new TextView(context);
		alertTv.setLayoutParams(ll);
		alertTv.setLayoutParams(ll);
		alertTv.setTextSize(18.0f);
		alertTv.setTextColor(Color.LTGRAY);
		alertTv.setText("点击图片刷新");
		rootLl.addView(exceptIv);
		rootLl.addView(exceptTv);
		rootLl.addView(alertTv);
		return rootLl;
	}

	/*
	 * 无数据
	 */
	public void customExceptView(int drawableId, String exceptStr) {
		recyclerView.setVisibility(View.INVISIBLE);
		mExceptView.setVisibility(View.VISIBLE);
		// exceptView(drawableId,exceptStr);
		exceptIv.setImageResource(drawableId);
		exceptTv.setText(exceptStr);
	}

	// /*
	// * 无网络
	// */
	// public void noNet(){
	// recyclerView.setVisibility(View.GONE);
	// mExceptView.setVisibility(View.VISIBLE);
	// mExceptView.exceptView(0);
	// }
	// /*
	// * 无活动
	// */
	// public void noAct(){
	// recyclerView.setVisibility(View.GONE);
	// mExceptView.setVisibility(View.VISIBLE);
	// mExceptView.exceptView(2);
	// }
	// /*
	// * 其他异常
	// */
	// public void otherException(){
	// recyclerView.setVisibility(View.GONE);
	// mExceptView.setVisibility(View.VISIBLE);
	// mExceptView.exceptView(3);
	// }

	public interface RefreshLoadMoreListener {
		public void onRefresh();

		public void onLoadMore();
	}

}
