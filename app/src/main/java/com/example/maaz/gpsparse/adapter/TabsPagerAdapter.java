package com.example.maaz.gpsparse.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.maaz.gpsparse.FindPhoneFragment;
import com.example.maaz.gpsparse.SignInFragment;
import com.example.maaz.gpsparse.SignUpFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {

	public TabsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {

		switch (index) {
		case 0:
			// Top Rated fragment activity
			return new SignInFragment();
		case 1:
			// Games fragment activity
			return new FindPhoneFragment();
		case 2:
			// Movies fragment activity
			return new SignUpFragment();
		}

		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 3;
	}

}
