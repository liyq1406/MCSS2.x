package com.v5kf.mcss.ui.fragment;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.v5kf.mcss.R;
import com.v5kf.mcss.config.Config;
import com.v5kf.mcss.ui.activity.BaseActivity;
import com.v5kf.mcss.ui.adapter.SessionPagerAdapter;
import com.v5kf.mcss.ui.widget.SegmentView;
import com.v5kf.mcss.ui.widget.SegmentView.onSegmentViewClickListener;
import com.v5kf.mcss.utils.Logger;

/**
 * @author chenhy
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-2 下午11:24:47
 * @package com.v5kf.mcss.ui.fragment.SessionFragment.java
 * @description
 *
 */
public class SessionFragment extends BaseFragment {
	
	private static final String TAG = "SessionFragment";
	public static final int WHAT_SWITCG_TO_SEGMENT_1 = 1;
	public static final int WHAT_SWITCG_TO_SEGMENT_2 = 2;
	public static final int WHAT_INIT_SEGMENT = 3;
	
	private View mView;
	
	private SegmentView mSegmentView;
	private ViewPager mSessionPager;
	
	private SessionPagerAdapter mAdapter;
	private int mCurrentPager;
	
	private Map<Integer, BaseFragment> mFragmentMap;
	
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SessionFragment newInstance(int sectionNumber) {
    	SessionFragment fragment = new SessionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("UseSparseArrays") 
    public SessionFragment() {
    	mFragmentMap = new HashMap<Integer, BaseFragment>();
	}

    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	Logger.d(TAG, "onAttach mActivity:" + mActivity);
    }    
    

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.d(TAG, "onCreate");
	}
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {    	
    	if(mView != null){
            return mView;
        }
    	Logger.d(TAG, "onCreateView");    	
    	mView = inflater.inflate(R.layout.fragment_viewpager, container, false);
    	
    	return mView;
    }
	

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);    	
    	Logger.d(TAG, "onActivityCreated");
		
    	initData();
    	findView();
    	initView();
    	loadData();
    }
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	
	private void initData() {
    	mCurrentPager = 0;
    	BaseFragment fragment0 = getFragment(0);
		mFragmentMap.put(0, fragment0);
		BaseFragment fragment1 = getFragment(1);
		mFragmentMap.put(1, fragment1);
		
		mAdapter = new SessionPagerAdapter(getChildFragmentManager(), mFragmentMap);
	}
	
	private BaseFragment getFragment(int position){
		BaseFragment fragment = mFragmentMap.get(position) ;
        if(fragment == null){
        	Logger.d(TAG, "fragment new" + position);
            switch (position){
                case 0:
                    fragment = ServingSessionFragment.newInstance(this);
                    
                    break;
                case 1:
                    fragment = WaitingSessionFragment.newInstance(this);
                    break;
            }
            mFragmentMap.put(position, fragment);
        }
        return fragment;
    }
	
	private void findView() {
    	mSessionPager = (ViewPager) mView.findViewById(R.id.fragment_pager);
	}
	

	/**
     * 初始化界面Adapter和RecycleView
     */
    private void initView() {
    	/* 初始化段选择器监听 */
    	mSegmentView = mActivity.getSegmentViewFromTitle();    	
    	mActivity.initTopBarForSegment(
			R.string.seg_title_serving, 
			R.string.seg_title_waiting, 
			new onSegmentViewClickListener() {
				
				@Override
				public void onSegmentViewClick(View v, int position) {
					Logger.w(TAG, "onSegmentViewClick position:" + position + " mCurrentPager:" + mCurrentPager);
					scrollToPager(position);
				}
			});
    	mHandler.sendEmptyMessageDelayed(WHAT_INIT_SEGMENT, 1000);
    	
    	mSessionPager.setAdapter(mAdapter);
    	mSessionPager.addOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				Logger.d(TAG, "onPageSelected position:" + position);
				mCurrentPager = position;
				selectSegment(position);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
    }
    
    
    protected void scrollToPager(int position) {
    	if (position == mCurrentPager) {
			return;
		}
		Logger.w(TAG, "onSegmentViewClick setCurrentItem:" + position);
    	mSessionPager.setCurrentItem(position, true);
    	mCurrentPager = position;
	}

	private void loadData() {
    	mActivity.setSegmentBadge(mAppInfo.getServingCustomerCount(), 0);
    	mActivity.setSegmentBadge(mAppInfo.getWaitingCustomerCount(), 1);
    	Logger.i(TAG, "CustomerCount:" + mAppInfo.getServingCustomerCount() + 
    			"  WaitingCount:" + mAppInfo.getWaitingCustomerCount());
    }
    
    
    private void selectSegment(int position) {
    	Logger.d(TAG, "selectSegment position:" + position + " mCurrentPager:" + mCurrentPager);
    	if (mSegmentView == null) {
    		Logger.w(TAG, "[selectSegment] warnning didn't initView");
    		return;
    	}
    	mSegmentView.setSelected(position);
    	if (mCurrentPager != position) {
    		mSessionPager.setCurrentItem(position);
    	}
    	mFragmentMap.get(position).onFragmentSelected();
    }
    
  	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Logger.d(TAG, "onActivityResult REQ_code:" + requestCode + " resultCOde:" + resultCode);
		
		if (requestCode == Config.REQUEST_CODE_WAITING_SESSION_FRAGMENT) {
			if (resultCode == Config.RESULT_CODE_PICKUP_CSTM) {
				selectSegment(0);
			}
			getFragment(1).onActivityResult(requestCode, resultCode, data);
		}
		getFragment(0).onActivityResult(requestCode, resultCode, data);
	}
	
	
	@Override
	protected void handleMessage(Message msg, BaseActivity baseActivity) {
		switch (msg.what) {
		case WHAT_SWITCG_TO_SEGMENT_1:
			selectSegment(0);
			break;
			
		case WHAT_SWITCG_TO_SEGMENT_2:
			selectSegment(1);
			break;
			
		case WHAT_INIT_SEGMENT:
			onFragmentSelected();
			break;
		}
	}
	
	@Override
	public void onFragmentSelected() {
		selectSegment(mCurrentPager);
		mSegmentView.setOnSegmentViewClickListener(new onSegmentViewClickListener() {
			
			@Override
			public void onSegmentViewClick(View v, int position) {
				Logger.i(TAG, "onSegmentViewClick position:" + position + " mCurrentPager:" + mCurrentPager);
				scrollToPager(position);
			}
		});
		mActivity.setSegmentBadge(mAppInfo.getServingCustomerCount(), 0);
		mActivity.setSegmentBadge(mAppInfo.getWaitingCustomerCount(), 1);
	}
}
