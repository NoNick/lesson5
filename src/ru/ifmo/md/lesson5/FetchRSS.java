package ru.ifmo.md.lesson5;

import android.os.AsyncTask;
import android.view.View;
import android.widget.LinearLayout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

class FetchRSS extends AsyncTask<Void, Void, Void> {
    LinearLayout progressBarLayout;
    String url;
    ArrayList<Entry> items;
    private final String RSSTag = "item", AtomTag = "entry";

    FetchRSS(LinearLayout l, String u, ArrayList<Entry> it) {
        progressBarLayout = l;
        url = u;
        items = it;
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
