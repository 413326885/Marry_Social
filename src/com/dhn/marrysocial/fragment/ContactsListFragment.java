package com.dhn.marrysocial.fragment;

import java.util.ArrayList;

import com.dhn.marrysocial.adapter.ContactsListAdapter;
import com.dhn.marrysocial.base.ContactsInfo;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.utils.Utils;
import com.dhn.marrysocial.R;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class ContactsListFragment extends Fragment {

    @SuppressWarnings("unused")
    private static final String TAG = "ContactsListFragment";

    private String mAuthorUid;

    private ListView mListView;
    private ContactsListAdapter mListViewAdapter;
    private ArrayList<ContactsInfo> mContactMembers = new ArrayList<ContactsInfo>();

    private static final String[] PROJECTION = { MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_PHONE_NUM,
            MarrySocialDBHelper.KEY_NICKNAME, MarrySocialDBHelper.KEY_REALNAME,
            MarrySocialDBHelper.KEY_FIRST_DIRECT_FRIEND,
            MarrySocialDBHelper.KEY_DIRECT_FRIENDS,
            MarrySocialDBHelper.KEY_INDIRECT_ID,
            MarrySocialDBHelper.KEY_DIRECT_FRIENDS_COUNT,
            MarrySocialDBHelper.KEY_HEADPIC, MarrySocialDBHelper.KEY_GENDER,
            MarrySocialDBHelper.KEY_ASTRO, MarrySocialDBHelper.KEY_HOBBY };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getActivity().getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT,
                getActivity().MODE_PRIVATE);
        mAuthorUid = prefs.getString(CommonDataStructure.UID, "");

        mContactMembers.clear();
        mContactMembers.addAll(loadContactsFromDB());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.contacts_list_fragment_layout,
                container, false);
        mListView = (ListView) view.findViewById(R.id.contacts_listview);
        TextView emptyView = (TextView) view
                .findViewById(R.id.contacts_list_empty);
        mListViewAdapter = new ContactsListAdapter(getActivity());
        mListViewAdapter.setDataSource(mContactMembers);
        mListView.setAdapter(mListViewAdapter);
        mListView.setEmptyView(emptyView);
        return view;
    }

    private long mExitTime = 0;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "nannan ContactsListFragment onKeyDown......");
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Utils.showMesage(getActivity(), R.string.exit_confirm);
            mExitTime = System.currentTimeMillis();
        } else {
            getActivity().finish();
        }
        return true;
    }

    private ArrayList<ContactsInfo> loadContactsFromDB() {

        ArrayList<ContactsInfo> contactMembers = new ArrayList<ContactsInfo>();

        MarrySocialDBHelper dbHelper = MarrySocialDBHelper
                .newInstance(getActivity());
        String whereClause = MarrySocialDBHelper.KEY_UID + " != " + mAuthorUid;
        Cursor cursor = dbHelper.query(
                MarrySocialDBHelper.DATABASE_CONTACTS_TABLE, PROJECTION,
                whereClause, null, null, null, null, null);
        if (cursor == null) {
            Log.w(TAG, "nannan query fail!");
            return contactMembers;
        }
        Log.e(TAG, "nannan count = " + cursor.getCount());

        try {
            while (cursor.moveToNext()) {
                String uid = cursor.getString(0);
                String phoneNum = cursor.getString(1);
                String nickname = cursor.getString(2);
                String realname = cursor.getString(3);
                String firstDirectFriend = cursor.getString(4);
                String directFriends = cursor.getString(5);
                String indirectId = cursor.getString(6);
                int directFriendsCount = cursor.getInt(7);
                int avatar = Integer.valueOf(cursor.getInt(8));
                int gender = Integer.valueOf(cursor.getInt(9));
                int astro = Integer.valueOf(cursor.getInt(10));
                int hobby = Integer.valueOf(cursor.getInt(11));

                ContactsInfo contactItem = new ContactsInfo();
                contactItem.setUid(uid);
                contactItem.setPhoneNum(phoneNum);
                contactItem.setNickName(nickname);
                contactItem.setRealName(realname);
                contactItem.setHeadPic(avatar);
                contactItem.setGender(gender);
                contactItem.setAstro(astro);
                contactItem.setHobby(hobby);
                contactItem.setIndirectId(indirectId);
                contactItem.setFirstDirectFriend(firstDirectFriend);
                contactItem.setDirectFriends(directFriends);
                contactItem.setDirectFriendsCount(directFriendsCount);

                contactMembers.add(contactItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contactMembers;
    }
}