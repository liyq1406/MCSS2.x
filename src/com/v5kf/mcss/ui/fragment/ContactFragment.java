package com.v5kf.mcss.ui.fragment;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import com.v5kf.mcss.ui.adapter.ContactPagerAdapter;
import com.v5kf.mcss.ui.widget.SegmentView;
import com.v5kf.mcss.ui.widget.SegmentView.onSegmentViewClickListener;
import com.v5kf.mcss.utils.Logger;

/**
 * @author chenhy
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-2 下午11:24:47
 * @package com.v5kf.mcss.ui.fragment.ChatFragment.java
 * @description
 *
 */
public class ContactFragment extends BaseFragment {
	
	private static final String TAG = "ContactFragment";
	private View mView ;
	
	private SegmentView mSegmentView;
	private ViewPager mContactPager;
	
	private ContactPagerAdapter mAdapter;
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
    public static ContactFragment newInstance(int sectionNumber) {
    	ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("UseSparseArrays") 
    public ContactFragment() {
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
    }
	

	private void initData() {
		BaseFragment fragment0 = getFragment(0);
		mFragmentMap.put(0, fragment0);
		BaseFragment fragment1 = getFragment(1);
		mFragmentMap.put(1, fragment1);
		
		mAdapter = new ContactPagerAdapter(getChildFragmentManager(), mFragmentMap);
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	
	private void findView() {
    	mContactPager = (ViewPager) mView.findViewById(R.id.fragment_pager);
	}
	
	
	/**
     * 初始化界面
     */
    @SuppressLint("ResourceAsColor") 
    private void initView() {
    	/* 初始化段选择器监听 */
    	mSegmentView = mActivity.getSegmentViewFromTitle();
    	mCurrentPager = 0;
    	
    	mContactPager.setAdapter(mAdapter);
    	mContactPager.addOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
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
    
    private BaseFragment getFragment(int position){
    	BaseFragment fragment = mFragmentMap.get(position) ;
        if(fragment == null){
        	Logger.d(TAG, "fragment new" + position);
            switch (position){
                case 0:
                	fragment = new WorkerListFragment();
                    break;
                case 1:
                    fragment = HistoryVisitorFragment.newInstance(position);
                    break;
            }
            mFragmentMap.put(position, fragment) ;
        }
        return fragment;
    }
    
    private void selectSegment(int position) {
    	if (null == mSegmentView) {
    		mSegmentView = mActivity.getSegmentViewFromTitle();
    	}
    	mSegmentView.setSelected(position);
    	mFragmentMap.get(position).onFragmentSelected();
    }
  	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Logger.d(TAG, "onActivityResult REQ_code:" + requestCode + " resultCOde:" + resultCode);
		if (requestCode == Config.REQUEST_CODE_WORKER_CONTACT_FRAGMENT) {
			getFragment(0).onActivityResult(requestCode, resultCode, data);
		} else if (requestCode == Config.REQUEST_CODE_CUSTOMER_CONTACT_FRAGMENT) {
			getFragment(1).onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	protected void handleMessage(Message msg, BaseActivity baseActivity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFragmentSelected() {
		selectSegment(mCurrentPager);
		mSegmentView.setOnSegmentViewClickListener(new onSegmentViewClickListener() {
			
			@Override
			public void onSegmentViewClick(View v, int position) {
				if (position == mCurrentPager) {
					return;
				}
				mContactPager.setCurrentItem(position, true);
			}
		});
		mActivity.setSegmentBadge(0, 0);
		mActivity.setSegmentBadge(0, 1);
	}

}
