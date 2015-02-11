package com.dhn.marrysocial.adapter;

import java.util.ArrayList;

import android.content.ContentValues;
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

import com.dhn.marrysocial.MarrySocialApplication;
import com.dhn.marrysocial.R;
import com.dhn.marrysocial.activity.ContactsInfoActivity;
import com.dhn.marrysocial.base.AsyncHeadPicBitmapLoader;
import com.dhn.marrysocial.base.ContactsInfo;
import com.dhn.marrysocial.database.MarrySocialDBHelper;

public class ContactsListAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = "ContactsListAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<ContactsInfo> mData;
    private AsyncHeadPicBitmapLoader mHeadPicBitmapLoader;
    private MarrySocialDBHelper mDBHelper;

    public ContactsListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mHeadPicBitmapLoader = new AsyncHeadPicBitmapLoader(mContext);
        mDBHelper = MarrySocialDBHelper.newInstance(mContext);
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
            holder.new_contact_icon = (ImageView) convertView
                    .findViewById(R.id.contacts_new_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final int pos = position;
        holder.contacts_item_entry
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        startToViewContactsInfo(mData.get(pos).getUid());
                    }
                });

        holder.person_name.setText(mData.get(position).getNickName());
        mHeadPicBitmapLoader.loadImageBitmap(holder.person_pic,
                mData.get(position).getUid());
        holder.person_description.setText(String.format(mContext.getResources()
                .getString(R.string.contacts_detail_more), mData.get(position)
                .getFirstDirectFriend()));
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
                                            R.string.contacts_detail_more),
                                            description));
                        }

                    }
                });

        if (mData.get(position).isNewContact()) {
            holder.new_contact_icon.setVisibility(View.VISIBLE);
        } else {
            holder.new_contact_icon.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    class ViewHolder {
        RelativeLayout contacts_item_entry;
        ImageView person_pic;
        TextView person_name;
        TextView person_description;
        CheckBox person_description_more;
        ImageView new_contact_icon;
    }

    private void startToViewContactsInfo(String uid) {
        Intent intent = new Intent(mContext, ContactsInfoActivity.class);
        intent.putExtra(MarrySocialDBHelper.KEY_UID, uid);
        mContext.startActivity(intent);
        deleteNewContactsFlagFromContactsDB(uid);
    }

    private void deleteNewContactsFlagFromContactsDB(String uid) {

        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_IS_NEW, MarrySocialDBHelper.HAS_NO_MSG);

        String whereClause = MarrySocialDBHelper.KEY_UID + " = "
                + uid;

        try{
            mDBHelper.update(MarrySocialDBHelper.DATABASE_CONTACTS_TABLE,
                    insertValues, whereClause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }
    // private String transArray2String(String[] friends) {
    // String friend = "";
    // for (String str : friends) {
    // friend = friend + str + ", ";
    // }
    //
    // friend = friend.substring(0, friend.length() - 2);
    // return friend;
    // }
}
