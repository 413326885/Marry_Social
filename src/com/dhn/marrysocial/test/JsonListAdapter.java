package com.dhn.marrysocial.test;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dhn.marrysocial.R;

public class JsonListAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = "JsonListAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<String> mData;

    public JsonListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setDataSource(ArrayList<String> source) {
        mData = source;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item,
                    parent, false);
            holder = new ViewHolder();
            holder.json_text = (TextView) convertView
                    .findViewById(R.id.json_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.json_text.setText(mData.get(position));
        return convertView;
    }

    class ViewHolder {
        TextView json_text;
    }

}
