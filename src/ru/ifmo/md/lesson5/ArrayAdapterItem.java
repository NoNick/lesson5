package ru.ifmo.md.lesson5;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import java.util.ArrayList;

public class ArrayAdapterItem extends ArrayAdapter<Entry> {

    Context mContext;
    int layoutResourceId;
    ArrayList<Entry> data = null;

    public ArrayAdapterItem(Context mContext, int layoutResourceId, ArrayList<Entry> data) {

        super(mContext, layoutResourceId, data);

        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        TextView text1 = ((TwoLineListItem) convertView).getText1();
        TextView text2 = ((TwoLineListItem) convertView).getText2();
        text1.setText(Html.fromHtml(data.get(position).title));
        text1.setTag(data.get(position).link);
        text2.setText(Html.fromHtml(data.get(position).description));

        return convertView;

    }

}
