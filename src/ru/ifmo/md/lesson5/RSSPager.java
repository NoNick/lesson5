package ru.ifmo.md.lesson5;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class RSSPager extends FragmentActivity {
    FeedMenuAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;
    private static boolean online;
    ArrayList<String> feeds = new ArrayList<String>(Arrays.asList("http://stackoverflow.com/feeds/tag/android",
                                           "http://feeds.bbci.co.uk/news/rss.xml",
                                           "http://echo.msk.ru/interview/rss-fulltext.xml",
                                           "http://bash.im/rss/"));
    ArrayList<String> feedsNames = new ArrayList<String>(Arrays.asList("StackOverflow/Android",
            "BBC News",
            "Эхо Москвы",
            "Bash"));
    private int currPage;

    class NewFeedListener implements DialogInterface.OnClickListener {
        Handler mainThread;
        RSSPager p;
        EditText nameField, linkField;
        public NewFeedListener(Handler h, RSSPager pg, EditText n, EditText l) {
            mainThread = h;
            p = pg;
            nameField = n;
            linkField = l;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            final String name = nameField.getText().toString();
            final String link = linkField.getText().toString();
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    p.addNewFeed(link, name);
                }
            });
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_demo);

        mDemoCollectionPagerAdapter = new FeedMenuAdapter(getSupportFragmentManager());
        for (int i = 0; i < 4; i++) {
            mDemoCollectionPagerAdapter.add(feeds.get(i), feedsNames.get(i));
        }
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
        mViewPager.setOffscreenPageLimit(feeds.size());
        currPage = 0;
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                currPage = position;
            }

            public void onPageSelected(int position) {
                currPage = position;
            }
        });

        online = isOnline();
        if (!online) {
            Toast t = Toast.makeText(this, getString(R.string.notOnline), Toast.LENGTH_LONG);
            t.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void addNewFeed(String link, String name) {
        if (name == null || link == null || !mDemoCollectionPagerAdapter.add(link, name)) {
            Toast t = Toast.makeText(this, getString(R.string.noNewFeed), Toast.LENGTH_LONG);
            t.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_feed:
                mDemoCollectionPagerAdapter.refresh(currPage);
                return true;
            case R.id.add_feed:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.newFeedTitle));
                final EditText nameField = new EditText(this);
                nameField.setText("Unnamed");
                final EditText linkField = new EditText(this);
                linkField.setText("http://");
                builder.setView(nameField);
                builder.setView(linkField);
                LinearLayout ll = new LinearLayout(this);
                ll.setOrientation(LinearLayout.VERTICAL);
                ll.addView(nameField);
                ll.addView(linkField);
                builder.setView(ll);
                builder.setPositiveButton("OK", new NewFeedListener(new Handler(), this, nameField, linkField));
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

                return true;
            case R.id.del_feed:
                mDemoCollectionPagerAdapter.del(currPage);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static class FeedMenuAdapter extends FragmentStatePagerAdapter {
        ArrayList<Fragment> feedFragments = new ArrayList<Fragment>();
        ArrayList<String> feeds = new ArrayList<String>();
        ArrayList<String> feedNames = new ArrayList<String>();
        int initSize;

        public FeedMenuAdapter(FragmentManager fm) {
            super(fm);
        }

        // false if there's no new feed
        public boolean add(String url, String name) {
            if (feeds.indexOf(url) == -1) {
                feeds.add(url);
                feedNames.add(name);
                notifyDataSetChanged();
                return true;
            }
            return false;
        }

        public void refresh(int pos) {
            ((FeedMenu) feedFragments.get(pos)).refresh();
        }

        public void del(int pos) {
            feedFragments.remove(pos);
            feeds.remove(pos);
            feedNames.remove(pos);
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int i) {
            if (feedFragments.size() >= i) {
                Fragment fragment = new FeedMenu();
                Bundle args = new Bundle();
                args.putString(FeedMenu.ARG_URL, feeds.get(i));
                args.putBoolean(FeedMenu.ARG_ONLINE, online);
                fragment.setArguments(args);
                feedFragments.add(fragment);
            }
            return feedFragments.get(i);
        }

        @Override
        public int getItemPosition(Object object){
            FeedMenu fm = (FeedMenu) object;
            ArrayList<String> urls = new ArrayList();
            for (int i = 0; i < feedFragments.size(); i++) {
                urls.add(((FeedMenu) feedFragments.get(i)).url);
            }
            int result = urls.indexOf(fm.url);
            if (result == -1)
                return FragmentStatePagerAdapter.POSITION_NONE;
            return result;
        }

        @Override
        public int getCount() {
            return feeds.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return feedNames.get(position);
        }
    }

    public static class FeedMenu extends Fragment {

        public static final String ARG_URL = "RSS_url", ARG_ONLINE = "RSS_online";
        String url;
        private boolean online;
        ArrayList<Entry> items = new ArrayList<Entry>();
        ListView lv;
        ArrayAdapter<Entry> mAdapter;
        View rootView;

        public void refresh() {
            if (online) {
                FetchRSS f = new FetchRSS((LinearLayout) rootView.findViewById(R.id.linlaHeaderProgress), url, items);
                f.execute();
            }
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_collection_object, container, false);
            lv = (ListView) rootView.findViewById(R.id.list);
            Bundle args = getArguments();
            url = args.getString(ARG_URL);
            online = args.getBoolean(ARG_ONLINE);
            mAdapter = new ArrayAdapterItem(getActivity(), R.layout.list_item, items);
            lv.setAdapter(mAdapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(view.getContext(), WebActivity.class);
                    intent.putExtra(WebActivity.REQUEST_URL, (String)((TwoLineListItem) view).getText1().getTag());
                    intent.putExtra(WebActivity.REQUEST_TITLE, ((TwoLineListItem) view).getText1().getText().toString());
                    startActivity(intent);
                }
            });

            refresh();

            return rootView;
        }
    }
}


class Entry {
    public String title, description, link;
    Entry(String t, String d, String l) {
        title = t;
        description = d;
        link = l;
    }
}