package com.v5kf.mcss.ui.activity.md2x;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chyrain.view.indicator.Indicator;
import com.chyrain.view.indicator.IndicatorViewPager;
import com.chyrain.view.indicator.IndicatorViewPager.IndicatorPagerAdapter;
import com.chyrain.view.indicator.IndicatorViewPager.IndicatorViewPagerAdapter;
import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.config.Config.ExitFlag;
import com.v5kf.mcss.utils.WorkerSP;

public class GuideActivity extends BaseLoginActivity {
	
	private IndicatorViewPager mIndicatorViewPager;
	private LayoutInflater mInflate;
	private WorkerSP mWSP;

	@Override
	public void onBackPressed() {
		finish();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.AppTheme_Normal);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_md2x_guide);
		setSwipeBackEnable(false);
		
		initView();
		mWSP = mApplication.getWorkerSp();
	}

	private void initView() {
		ViewPager viewPager = (ViewPager) findViewById(R.id.guide_viewPager);
		Indicator indicator = (Indicator) findViewById(R.id.guide_indicator);
		mIndicatorViewPager = new IndicatorViewPager(indicator, viewPager);
		mInflate = LayoutInflater.from(getApplicationContext());
		mIndicatorViewPager.setAdapter(adapter);
	}
	
	private IndicatorPagerAdapter adapter = new IndicatorViewPagerAdapter() {

		@Override
		public View getViewForTab(int position, View convertView, ViewGroup container) {
			if (convertView == null) {
				convertView = mInflate.inflate(R.layout.md2x_guide_tab_bottom, container, false);
			}
			return convertView;
		}

		@Override
		public View getViewForPage(int position, View convertView, ViewGroup container) {
//			if (convertView == null) {
//				convertView = new View(getApplicationContext());
//				convertView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//			}
			ViewHolder holder = null;
//			if (convertView == null) {
				switch (position) {
				case 0:
				case 1:
				case 2:
					convertView = mInflate.inflate(R.layout.item_guide_0, container, false) ;
					break;
				case 3:
					convertView = mInflate.inflate(R.layout.item_guide_3, container, false) ;
					break;
				}
				holder = new ViewHolder(position, convertView);
				convertView.setTag(holder);
//	        } else {
//	        	holder = (ViewHolder) convertView.getTag();
//	        }
			
			switch (position) {
			case 0:
				holder.mCenterIv.setImageResource(R.drawable.ic_guide0_center);
				holder.mBottomIv.setImageResource(R.drawable.ic_guide0_bottom);
				break;
			case 1:
				holder.mCenterIv.setImageResource(R.drawable.ic_guide1_center);
				holder.mBottomIv.setImageResource(R.drawable.ic_guide1_bottom);
				break;
			case 2:
				holder.mCenterIv.setImageResource(R.drawable.ic_guide2_center);
				holder.mBottomIv.setImageResource(R.drawable.ic_guide2_bottom);
				break;
			case 3:
				break;
			}
			return convertView;
		}

		@Override
		public int getCount() {
			return 4;
		}
	};

	@Override
	protected void handleMessage(Message msg) {

	}
	
	class ViewHolder implements View.OnClickListener {
		
		public ImageView mCenterIv;
		public ImageView mBottomIv;
		
		public ViewHolder(int position, View itemView) {
			switch (position) {
			case 0:
			case 1:
			case 2:
				mCenterIv = (ImageView)itemView.findViewById(R.id.guide_center_iv);
				mBottomIv = (ImageView)itemView.findViewById(R.id.guide_bottom_iv);
				break;
			case 3:
				itemView.findViewById(R.id.guide_bottom_left_iv).setOnClickListener(this);
				itemView.findViewById(R.id.guide_bottom_right_iv).setOnClickListener(this);
				break;
			}
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.guide_bottom_left_iv:
				if (mWSP.readAuthorization() == null || !mWSP.readAutoLogin() || mWSP.readPassWord().isEmpty()
						|| mWSP.readExitFlag() == ExitFlag.ExitFlag_NeedLogin) {
					gotoActivityAndFinishThis(CustomLoginActivity.class);
				} else {
					gotoMainTabActivity();
					startUpdateService();
				}
				mApplication.getWorkerSp().saveBoolean("v5_inited", true);
				break;
			case R.id.guide_bottom_right_iv:
				// TODO 了解更多
				gotoWebViewActivity(Config.V5KF_COM, 0); //R.string.app_name
				break;
			}
		}
	}
}
