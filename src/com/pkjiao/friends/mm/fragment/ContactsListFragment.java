package com.pkjiao.friends.mm.fragment;

import java.util.ArrayList;

import com.pkjiao.friends.mm.R;
import com.pkjiao.friends.mm.adapter.ContactsExpandableListAdapter;
import com.pkjiao.friends.mm.adapter.ContactsListAdapter;
import com.pkjiao.friends.mm.base.ContactsInfo;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;
import com.pkjiao.friends.mm.pingyin.AssortView;
import com.pkjiao.friends.mm.pingyin.AssortView.OnTouchAssortButtonListener;
import com.pkjiao.friends.mm.services.DownloadIndirectFriendsIntentServices;
import com.pkjiao.friends.mm.utils.Utils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class ContactsListFragment extends Fragment {

    @SuppressWarnings("unused")
    private static final String TAG = "ContactsListFragment";

    private String mAuthorUid;

    private AssortView mAssortView;
    private PopupWindow mPopupWindow;
    private ExpandableListView mListView;
    private ContactsExpandableListAdapter mListViewAdapter;
    private ArrayList<ContactsInfo> mContactMembers = new ArrayList<ContactsInfo>();

    private static final String[] PROJECTION = { MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_PHONE_NUM,
            MarrySocialDBHelper.KEY_NICKNAME, MarrySocialDBHelper.KEY_REALNAME,
            MarrySocialDBHelper.KEY_FIRST_DIRECT_FRIEND,
            MarrySocialDBHelper.KEY_DIRECT_FRIENDS,
            MarrySocialDBHelper.KEY_INDIRECT_ID,
            MarrySocialDBHelper.KEY_DIRECT_FRIENDS_COUNT,
            MarrySocialDBHelper.KEY_HEADPIC, MarrySocialDBHelper.KEY_GENDER,
            MarrySocialDBHelper.KEY_ASTRO, MarrySocialDBHelper.KEY_HOBBY,
            MarrySocialDBHelper.KEY_IS_NEW };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getActivity().getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT,
                getActivity().MODE_PRIVATE);
        mAuthorUid = prefs.getString(CommonDataStructure.UID, "");

        downloadUserContacts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.contacts_list_fragment_layout,
                container, false);
        mListView = (ExpandableListView) view
                .findViewById(R.id.contacts_listview);
        TextView emptyView = (TextView) view
                .findViewById(R.id.contacts_list_empty);
        mListViewAdapter = new ContactsExpandableListAdapter(getActivity());
        mContactMembers.clear();
        mContactMembers.addAll(loadContactsFromDB());
        mListViewAdapter.setDataSource(mContactMembers);
        mListView.setAdapter(mListViewAdapter);
        mListView.setEmptyView(emptyView);

        mAssortView = (AssortView) view.findViewById(R.id.assort_view);
        mAssortView.setOnTouchAssortListener(new OnTouchAssortButtonListener() {

            View layoutView = LayoutInflater.from(getActivity()).inflate(
                    R.layout.contacts_list_show_assort_char_layout, null);
            TextView text = (TextView) layoutView
                    .findViewById(R.id.assort_char);

            public void onTouchAssortButtonListener(String str) {
                int index = mListViewAdapter.getAssort().getHashList()
                        .indexOfKey(str);
                if (index != -1) {
                    mListView.setSelectedGroup(index);
                }
                if (mPopupWindow != null) {
                    text.setText(str);
                } else {
                    int width = Utils.dp2px(getActivity(), 80);
                    int height = Utils.dp2px(getActivity(), 80);
                    mPopupWindow = new PopupWindow(layoutView, width, height, false);
                    mPopupWindow.showAtLocation(getActivity().getWindow()
                            .getDecorView(), Gravity.CENTER, 0, 0);
                }
                text.setText(str);
            }

            public void onTouchAssortButtonUP() {
                if (mPopupWindow != null)
                    mPopupWindow.dismiss();
                mPopupWindow = null;
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        mContactMembers.clear();
//        mContactMembers.addAll(loadContactsFromDB());
//        mListViewAdapter.setDataSource(mContactMembers);
        mListViewAdapter.notifyDataSetChanged();
        for (int i = 0, length = mListViewAdapter.getGroupCount(); i < length; i++) {
            mListView.expandGroup(i);
        }
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
                int isNewContact = Integer.valueOf(cursor.getInt(12));

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
                contactItem.setNewContact(isNewContact);

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

    private void downloadUserContacts() {
        Intent serviceIntent = new Intent(getActivity(),
                DownloadIndirectFriendsIntentServices.class);
        getActivity().startService(serviceIntent);
    }

    public void dismissPopupWindow() {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }

        mPopupWindow = null;
        mAssortView.clear();

    }
}