package com.dhn.marrysocial.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.activity.ContactsInfoActivity;
import com.dhn.marrysocial.base.ContactsInfo;

public class ContactsListAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = "ContactsListAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<ContactsInfo> mData;

    public ContactsListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setDataSource(ArrayList<ContactsInfo> source) {
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
            convertView = mInflater.inflate(R.layout.contacts_list_item_layout,
                    parent, false);
            holder = new ViewHolder();
            holder.contacts_item_entry = (RelativeLayout) convertView
                    .findViewById(R.id.contacts_item_entry);
            holder.person_pic = (ImageView) convertView
                    .findViewById(R.id.contacts_person_pic);
            holder.person_name = (TextView) convertView
                    .findViewById(R.id.contacts_person_name);
            holder.person_description = (TextView) convertView
                    .findViewById(R.id.contacts_person_description);
            holder.person_description_more = (CheckBox) convertView
                    .findViewById(R.id.contacts_person_description_more);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.contacts_item_entry.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View arg0) {
                startToViewContactsInfo();
            }
        });
        holder.person_name.setText(mData.get(position).getNikeName());
        holder.person_pic.setImageResource(R.drawable.person_default_small_pic);
        holder.person_description.setText(String.format(mContext.getResources()
                .getString(R.string.contacts_detail), mData.get(position).getFirstDirectFriend()));
        holder.person_description_more.setChecked(false);

        final ViewHolder holder_temp = holder;
        final String description = (mData.get(position).getFirstDirectFriend());
        final String description_more = mData.get(position).getDirectFriends();

        holder.person_description_more
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (holder_temp.person_description_more.isChecked()) {
                            holder_temp.person_description.setText(String
                                    .format(mContext.getResources().getString(
                                            R.string.contacts_detail_more),
                                            description_more));
                        } else {
                            holder_temp.person_description.setText(String
                                    .format(mContext.getResources().getString(
                                            R.string.contacts_detail),
                                            description));
                        }

                    }
                });
        return convertView;
    }

    class ViewHolder {
        RelativeLayout contacts_item_entry;
        ImageView person_pic;
        TextView person_name;
        TextView person_description;
        CheckBox person_description_more;
    }

    private void startToViewContactsInfo() {
        Intent intent = new Intent(mContext, ContactsInfoActivity.class);
        mContext.startActivity(intent);
    }
//    private String transArray2String(String[] friends) {
//        String friend = "";
//        for (String str : friends) {
//            friend = friend + str + ", ";
//        }
//
//        friend = friend.substring(0, friend.length() - 2);
//        return friend;
//    }
}
