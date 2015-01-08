package com.dhn.marrysocial.fragment;

import java.util.ArrayList;

import com.dhn.marrysocial.adapter.ChatMsgListAdapter;
import com.dhn.marrysocial.base.ChatMsgItem;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.utils.Utils;
import com.dhn.marrysocial.R;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class ChatMsgFragment extends Fragment {

    @SuppressWarnings("unused")
    private static final String TAG = "ChatMsgFragment";

    private static final int UPDATE_CHAT_MSG = 100;

    private static final String[] BRIEF_CHAT_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_CHAT_ID,
            MarrySocialDBHelper.KEY_NIKENAME,
            MarrySocialDBHelper.KEY_CHAT_CONTENT,
            MarrySocialDBHelper.KEY_ADDED_TIME };

    private ListView mListView;
    private ChatMsgListAdapter mListViewAdapter;

    private MarrySocialDBHelper mDBHelper;
    private ArrayList<BriefChatItem> mBriefChatItems = new ArrayList<BriefChatItem>();

    private DataSetChangeObserver mChangeObserver;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case UPDATE_CHAT_MSG: {
                loadBriefChatMsgsFromBriefChatDB();
                mListViewAdapter.notifyDataSetChanged();
                break;
            }
            default:
                break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChangeObserver = new DataSetChangeObserver(mHandler);
        this.getActivity()
                .getContentResolver()
                .registerContentObserver(CommonDataStructure.BRIEFCHATURL, true,
                        mChangeObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.chat_msg_fragment_layout,
                container, false);
        mListView = (ListView) view.findViewById(R.id.chat_msg_listView);
        TextView emptyView = (TextView) view.findViewById(R.id.chat_list_empty);
        mListViewAdapter = new ChatMsgListAdapter(getActivity());
        mListViewAdapter.setDataSource(mBriefChatItems);
        mListView.setAdapter(mListViewAdapter);
        mListView.setEmptyView(emptyView);

        mDBHelper = MarrySocialDBHelper.newInstance(getActivity());
//        insertBriefChatMsgToBriefChatDB();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBriefChatMsgsFromBriefChatDB();
        mListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.getActivity().getContentResolver()
                .unregisterContentObserver(mChangeObserver);
    }

    private long mExitTime = 0;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "nannan ChatMsgFragment onKeyDown......");
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Utils.showMesage(getActivity(), R.string.exit_confirm);
            mExitTime = System.currentTimeMillis();
        } else {
            getActivity().finish();
        }
        return true;
    }

    private void loadBriefChatMsgsFromBriefChatDB() {

        mBriefChatItems.clear();
        Cursor cursor = null;

        try {
            String orderBy = MarrySocialDBHelper.KEY_ADDED_TIME + " ASC";

            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE,
                    BRIEF_CHAT_PROJECTION, null, null, null, null, orderBy,
                    null);

            if (cursor == null) {
                Log.e(TAG,
                        "nannan loadBriefChatMsgsFromBriefChatDB()..  cursor == null");
                return;
            }

            while (cursor.moveToNext()) {
                BriefChatItem item = new BriefChatItem();
                item.uId = cursor.getString(0);
                item.chatId = cursor.getString(1);
                item.nikename = cursor.getString(2);
                item.chatContent = cursor.getString(3);
                String chat_time = cursor.getString(4);
                // item.addTime = chat_time.substring(0, chat_time.length() -
                // 6);
                item.addTime = chat_time;
                mBriefChatItems.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static class BriefChatItem {
        public String uId;
        public String chatId;
        public String nikename;
        public String chatContent;
        public String addTime;
    }

    private void insertBriefChatMsgToBriefChatDB() {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_UID, "3");
        insertValues.put(MarrySocialDBHelper.KEY_CHAT_ID, "3_4");
        insertValues.put(MarrySocialDBHelper.KEY_NIKENAME, "nan");
        insertValues.put(MarrySocialDBHelper.KEY_CHAT_CONTENT, "你个大傻逼。。。");
        String addtime = Long.toString(System.currentTimeMillis() * 1000);
        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME, addtime);

        mDBHelper.insert(MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE,
                insertValues);
    }

    private class DataSetChangeObserver extends ContentObserver {

        private Handler handler;

        public DataSetChangeObserver(Handler handler) {
            super(handler);
            this.handler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            handler.sendEmptyMessage(UPDATE_CHAT_MSG);
            Log.e(TAG, "nannan onChange()..");
        }
    }
}
