package ru.ifmo.md.lesson5;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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

public class RSSPager extends FragmentActivity {
    FeedMenuAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;
    private static boolean online;
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

        mDemoCollectionPagerAdapter = new FeedMenuAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
        mViewPager.setOffscreenPageLimit(feeds.length);

        online = isOnline();
        if (!online) {
            Toast t = Toast.makeText(this, getString(R.string.notOnline), Toast.LENGTH_LONG);
            t.show();
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

    public static class FeedMenuAdapter extends FragmentPagerAdapter {

        public FeedMenuAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new FeedMenu();
            Bundle args = new Bundle();
            args.putString(FeedMenu.ARG_URL, feeds[i]);
            args.putBoolean(FeedMenu.ARG_ONLINE, online);
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

    public static class FeedMenu extends Fragment {

        public static final String ARG_URL = "RSS_url", ARG_ONLINE = "RSS_online";
        private String url;
        private boolean online;
        ArrayList<Entry> items = new ArrayList<Entry>();
        ListView lv;
        ArrayAdapter<Entry> mAdapter;

        public static class Entry {
            public String title, description, link;
            Entry(String t, String d, String l) {
                title = t;
                description = d;
                link = l;
            }
        }

        class FetchRSS extends AsyncTask <Void, Void, Void> {
            LinearLayout progressBarLayout;
            String url;
            private final String RSSTag = "item", AtomTag = "entry";

            FetchRSS(LinearLayout l, String u) {
                progressBarLayout = l;
                url = u;
            }

            @Override
            protected void onPreExecute() {
                progressBarLayout.setVisibility(View.VISIBLE);

                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    URL url_ = new URL(url);
                    Document xmlResponse = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                                                                                  (InputStream) url_.getContent());
                    xmlResponse.getDocumentElement().normalize();
                    NodeList list = xmlResponse.getElementsByTagName(RSSTag);
                    if (url.contains("stackoverflow")) {
                        list = xmlResponse.getElementsByTagName(AtomTag);
                    }

                    items.clear();
                    for (int i = 0; i < list.getLength(); i++) {
                        Node node = list.item(i);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element e = (Element) node;
                            if (url.contains("stackoverflow")) {
                                items.add(new Entry(e.getElementsByTagName("title").item(0).getFirstChild().getNodeValue(),
                                        e.getElementsByTagName("name").item(0).getFirstChild().getNodeValue(),
                                        e.getElementsByTagName("id").item(0).getFirstChild().getNodeValue()));
                            }
                            else {
                                items.add(new Entry(e.getElementsByTagName("title").item(0).getFirstChild().getNodeValue(),
                                        e.getElementsByTagName("description").item(0).getFirstChild().getNodeValue(),
                                        e.getElementsByTagName("link").item(0).getFirstChild().getNodeValue()));
                            }
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                progressBarLayout.setVisibility(View.GONE);
                super.onPostExecute(result);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_collection_object, container, false);
            lv = (ListView) rootView.findViewById(R.id.list);
            Bundle args = getArguments();
            url = args.getString(ARG_URL);
            online = args.getBoolean(ARG_ONLINE);
            if (online) {
                FetchRSS f = new FetchRSS((LinearLayout) rootView.findViewById(R.id.linlaHeaderProgress), url);
                f.execute();
            }
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
            return rootView;
        }
    }
}
