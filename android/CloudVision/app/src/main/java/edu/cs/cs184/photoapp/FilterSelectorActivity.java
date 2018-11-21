package edu.cs.cs184.photoapp;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class FilterSelectorActivity extends AppCompatActivity {

    //Daniel: adapted from my homework 3 implementation

    public static CustomFragmentsPagerAdapter customFragmentsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private ArrayList<FilterFragment> mFilterFragments;

    public static int getDisplayWidth= 1;
    public static int getDisplayHeight =1;
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            savedInstanceState.getBundle("main");

            mViewPager.setCurrentItem(savedInstanceState.getInt("current", 1));
            mTabLayout.getTabAt(savedInstanceState.getInt("tab",1)).select();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // todo: create a dialogfragment that displays a nicely formatted list of the features

        setContentView(R.layout.activity_filter_selector);
        updateWidth(this);


        customFragmentsPagerAdapter = new CustomFragmentsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.fragmentsPager);
        mTabLayout = (TabLayout) findViewById(R.id.fragmentTabs);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        
        // todo: fix tab selection listener - it currently causes the app to reopen?
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                mViewPager.setCurrentItem(tab.getPosition());
                tab.getIcon().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                tab.select();

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                //tab.select();


            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                try{mTabLayout.getTabAt(i).select();}catch (Exception e){}
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });


        setupViewPager(mViewPager);


    }

    private void setupViewPager(ViewPager pager) {
        CustomFragmentsPagerAdapter pagerAdapter = new CustomFragmentsPagerAdapter(getSupportFragmentManager());
        mFilterFragments = new ArrayList<>();
        /*todo: generate the list of filters to apply, then apply them on each instance of a fragment.
        / todo: We could also generate an array of them right after the result is received in main activity.*/

        // Daniel: taken from my hw 4 implementation.
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        MainActivity.myPhoto.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        for(int i=0; i<MainActivity.features.size(); i++)
        {

            /*Bundle args = new Bundle();
            args.putString(ARG_PARAM1, MainActivity.features.get(i));
            args.putDouble(ARG_PARAM2, MainActivity.percentCertainties.get(i));
            args.putByteArray(ARG_PARAM3,byteArray);
*/
            //instantiate new fragment, add it to the list
            FilterFragment currentFrag = FilterFragment.newInstance(MainActivity.features.get(i),MainActivity.percentCertainties.get(i),byteArray);
            mFilterFragments.add(currentFrag);
            pagerAdapter.addFragment(currentFrag);

            //create a transaction to create the view of the fragment
            /*FragmentTransaction ft =  getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in  ,android.R.anim.fade_out);
            ft.add(R.id.selector_layout,currentFrag).commit();*/

            mTabLayout.addTab(mTabLayout.newTab().setText(MainActivity.features.get(i).substring(0,1).toUpperCase()+MainActivity.features.get(i).substring(1)));
            //Fragment.instantiate(this, FilterFragment.class.getName(), args);


            //todo: set new ontablistener to select the right fragment
            //todo: make the tabs scrollable correctly
        }

        /*for(FilterFragment f: mFilterFragments) {
            pagerAdapter.addFragment(f, "test fragment", Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888), "test filter", 88.8);
        }*/
        pager.setAdapter(pagerAdapter);

    }




    public void updateWidth(Context context)
    {
        final WindowManager w = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        final Display d = w.getDefaultDisplay();
        final DisplayMetrics m = new DisplayMetrics();
        d.getMetrics(m);
        getDisplayWidth = m.widthPixels;
        getDisplayHeight = m.heightPixels;
        //Log.e(",","updated width to: " + getDisplayWidth);
    }

    public static int getWidth()
    {
        return getDisplayWidth;
    }
    public static int getHeight(){return getDisplayHeight;}




    // a function to retrieve the package name
    public static String getPackageName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        return applicationInfo.packageName;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_main);
        updateWidth(this);
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("current",mViewPager.getCurrentItem());
        state.putInt("tab",mTabLayout.getSelectedTabPosition());
        state.putBundle("main",state);


    }




}

