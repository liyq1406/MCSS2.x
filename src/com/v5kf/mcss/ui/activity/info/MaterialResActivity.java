package com.v5kf.mcss.ui.activity.info;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chyrain.view.indicator.FragmentListPageAdapter;
import com.chyrain.view.indicator.IndicatorViewPager;
import com.chyrain.view.indicator.IndicatorViewPager.IndicatorFragmentPagerAdapter;
import com.chyrain.view.indicator.ScrollIndicatorView;
import com.chyrain.view.indicator.slidebar.ColorBar;
import com.chyrain.view.indicator.transition.OnTransitionTextListener;
import com.v5kf.mcss.R;
import com.v5kf.mcss.ui.activity.md2x.BaseToolbarActivity;
import com.v5kf.mcss.ui.fragment.md2x.MaterialBaseFragment;
import com.v5kf.mcss.utils.UITools;

public class MaterialResActivity extends BaseToolbarActivity {
	private static final String TAG = "MaterialResActivity";
	/* 素材类型 */
	public static final int TYPE_IMG = 1;
	public static final int TYPE_NEWS = 2;
	public static final int TYPE_MUSIC = 3;
	
	protected static final int HDL_STOP_REFRESH = 11;
	protected static final int HDL_STOP_LOAD = 12;
	protected static final int HDL_UPDATE_UI = 13;

	private IndicatorViewPager indicatorViewPager;
	private LayoutInflater inflate;
	private String[] names = { "图片", "图文", "音频" };
	private ScrollIndicatorView indicator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_md2x_material_res);

		initView();
	}

	private void initView() {
		initTopBarForLeftBack(R.string.title_material);
		
		ViewPager viewPager = (ViewPager) findViewById(R.id.id_material_viewPager);
		indicator = (ScrollIndicatorView) findViewById(R.id.id_material_indicator);
		indicator.setScrollBar(new ColorBar(this, UITools.getColor(R.color.md2x_blue), 5));

		// 设置滚动监听
		int selectColorId = R.color.tab_top_text_2;
		int unSelectColorId = R.color.tab_top_text_1;
		indicator.setOnTransitionListener(new OnTransitionTextListener().setColorId(this, selectColorId, unSelectColorId));

		viewPager.setOffscreenPageLimit(names.length);
		indicatorViewPager = new IndicatorViewPager(indicator, viewPager);
		inflate = LayoutInflater.from(getApplicationContext());
		indicatorViewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
	}
	
	
	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
//		case BaseChatActivity.HDL_WHAT_CANDIDATE_SEND:
//			Bundle bundle = msg.getData();
//			int pos = bundle.getInt(BaseChatActivity.MSG_KEY_POSITION);
//			V5Message msgContent = mDatas.get(pos);
//			Intent data = new Intent();
//			Bundle extra = new Bundle();
//			extra.putSerializable("message_content", msgContent);
//			data.putExtras(extra);
//			setResult(Config.RESULT_CODE_MATERIAL_SEND, data);
//			finishActivity();
//			break;
		}
	}
	
	private class MyAdapter extends IndicatorFragmentPagerAdapter {

		public MyAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public int getCount() {
			return names.length;
		}

		@Override
		public View getViewForTab(int position, View convertView, ViewGroup container) {
			if (convertView == null) {
				convertView = inflate.inflate(R.layout.md2x_material_tab_top, container, false);
			}
			TextView textView = (TextView) convertView;
			textView.setText(names[position % names.length]);
			textView.setPadding(20, 0, 20, 0);
			return convertView;
		}

		@Override
		public Fragment getFragmentForPage(int position) {
			MaterialBaseFragment fragment = new MaterialBaseFragment(MaterialResActivity.this, position);
			return fragment;
		}

		@Override
		public int getItemPosition(Object object) {
			return FragmentListPageAdapter.POSITION_NONE;
		}

	};
}
