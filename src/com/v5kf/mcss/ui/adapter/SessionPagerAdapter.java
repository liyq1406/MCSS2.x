package com.v5kf.mcss.ui.adapter;

import java.util.Map;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.v5kf.mcss.ui.fragment.BaseFragment;

/**
 * @author chenhy
 * @email chenhy@v5kf.com
 * @version v1.0 2015-7-2 下午11:44:40
 * @package com.v5kf.mcss.ui.adapter.TabPagerAdapter.java
 * @description
 *
 */
public class SessionPagerAdapter extends FragmentPagerAdapter{
//	private static final String TAG = "SessionPagerAdapter";
	private Map<Integer, BaseFragment> mFragmentMap;
	
	public SessionPagerAdapter(FragmentManager fm, Map<Integer, BaseFragment> fragmentMap) {
		super(fm);
		mFragmentMap = fragmentMap;
	}

	@Override
    public Fragment getItem(int position) {        
		return mFragmentMap.get(position);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return mFragmentMap.size();
    }
}
