package ru.ifmo.md.lesson5;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RSSPager extends FragmentActivity {
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;
    private static final String[] feeds = {"http://stackoverflow.com/feeds/tag/android",
                                           "http://feeds.bbci.co.uk/news/rss.xml",
                                           "http://echo.msk.ru/interview/rss-fulltext.xml",
                                           "http://bash.im/rss/"};
    private static final String[] feedsNames = {"StackOverflow/Android",
            "BBC News",
            "Эхо Москвы",
            "Bash"};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_demo);

        mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager());
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
    }

    public static class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {

        public DemoCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new DemoObjectFragment();
            Bundle args = new Bundle();
            args.putString(DemoObjectFragment.ARG_URL, feeds[i]);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return feeds.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return feedsNames[position];
        }
    }

    public static class DemoObjectFragment extends Fragment {

        public static final String ARG_URL = "RSS_url";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_collection_object, container, false);
            Bundle args = getArguments();
            return rootView;
        }
    }
}
