package edu.cs.cs184.photoapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class CustomFragmentsPagerAdapter extends FragmentStatePagerAdapter {

    // Daniel: taken from my hw 3 implementation.
    private final List<FilterFragment> fragmentList = new ArrayList<>();


    public CustomFragmentsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(FilterFragment fragment)
    {
        fragmentList.add(fragment);

    }

    @Override
    public Fragment getItem(int i) {
        return fragmentList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }


}
