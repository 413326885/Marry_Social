package com.dhn.marrysocial.activity;

import java.util.ArrayList;

import com.dhn.marrysocial.R;
import com.dhn.marrysocial.adapter.ChatMsgViewAdapter;
import com.dhn.marrysocial.adapter.ChatMsgViewAdapter.IMsgViewType;
import com.dhn.marrysocial.base.ChatMsgItem;
import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.fragment.ChatMsgFragment.BriefChatItem;
import com.dhn.marrysocial.utils.Utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChatMsgActivity extends Activity implements OnClickListener {

    private static final String TAG = "ChatMsgActivity";

    private final String[] CHAT_PROJECTION = { MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_CHAT_ID, MarrySocialDBHelper.KEY_FROM_UID,
            MarrySocialDBHelper.KEY_TO_UID,
            MarrySocialDBHelper.KEY_CHAT_CONTENT,
            MarrySocialDBHelper.KEY_MSG_TYPE,
            MarrySocialDBHelper.KEY_ADDED_TIME,
            MarrySocialDBHelper.KEY_CURRENT_STATUS };

    private static final String[] BRIEF_CHAT_PROJECTION = {
        MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_CHAT_ID };

    private static final String[] CONTACTS_PROJECTION = {
        MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_NIKENAME };

    private static final int START_TO_UPLOAD_CHAT_MSG = 100;
    private static final int UPLOAD_CHAT_MSG_FINISH = 101;

    private ListView mListView;
    private ChatMsgViewAdapter mListViewAdapter;
    private RelativeLayout mChatReturnBtn;
    private ImageView mChatSendBtn;
    private EditText mChatContent;
    private TextView mEmptyView;

    private String mUid;
    private String mAuthorName;
    private String mToUid;
    private String mChatId;

    private MarrySocialDBHelper mDBHelper;
    private ArrayList<ChatMsgItem> mChatMsgList = new ArrayList<ChatMsgItem>();

    private ArrayList<ChatMsgSmallItem> mChatMsgQueue;
    private UploadChatMsgsTask mUploadChatMsgsTask;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case START_TO_UPLOAD_CHAT_MSG: {
                mListViewAdapter.notifyDataSetChanged();
                Utils.hideSoftInputMethod(mChatContent);
                mChatContent.setText(null);
                mListView.setSelection(mListView.getCount() - 1);
                break;
            }
            case UPLOAD_CHAT_MSG_FINISH: {
                ChatMsgSmallItem task = (ChatMsgSmallItem) msg.obj;
                // todo something
                break;
            }
            default:
                break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.chat_msg_layout);

        Intent data = getIntent();
        mChatId = data.getStringExtra(MarrySocialDBHelper.KEY_CHAT_ID);
        String[] ids = mChatId.split("_");
        mToUid = ids[1];

        mChatReturnBtn = (RelativeLayout) this
                .findViewById(R.id.chat_msg_return);
        mChatSendBtn = (ImageView) this.findViewById(R.id.chat_msg_chat_send);
        mChatContent = (EditText) this
                .findViewById(R.id.chat_msg_chat_contents);
        mEmptyView = (TextView) this.findViewById(R.id.chat_msg_no_content);
        mChatReturnBtn.setOnClickListener(this);
        mChatSendBtn.setOnClickListener(this);

        mListView = (ListView) this.findViewById(R.id.chat_msg_listview);
        mListViewAdapter = new ChatMsgViewAdapter(this);
        mListViewAdapter.setChatDataSource(mChatMsgList);
        mListView.setAdapter(mListViewAdapter);
        mListView.setEmptyView(mEmptyView);

        mDBHelper = MarrySocialDBHelper.newInstance(this);
        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, this.MODE_PRIVATE);
        mUid = prefs.getString(CommonDataStructure.UID, "");
        mAuthorName = prefs.getString(CommonDataStructure.AUTHOR_NAME, "");

        mChatMsgQueue = new ArrayList<ChatMsgSmallItem>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChatMsgsFromChatsDB(mChatId);
        mUploadChatMsgsTask = new UploadChatMsgsTask();
        mUploadChatMsgsTask.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUploadChatMsgsTask.terminate();
        mUploadChatMsgsTask = null;

        updateBriefChatDBWhenQuit();

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
        case R.id.chat_msg_return: {
            Utils.hideSoftInputMethod(mChatContent);
            this.finish();
            break;
        }
        case R.id.chat_msg_chat_send: {
            String chatContents = mChatContent.getText().toString();
            if (chatContents != null && chatContents.length() != 0) {
                String chatId = mUid + "_" + mToUid;
                ChatMsgItem chatMsg = new ChatMsgItem();
                chatMsg.setUid(mUid);
                chatMsg.setChatId(chatId);
                chatMsg.setFromUid(mUid);
                chatMsg.setToUid(mToUid);
                chatMsg.setChatContent(chatContents);
                chatMsg.setMsgType(IMsgViewType.IMVT_TO_MSG);
                chatMsg.setAddedTime(Long.toString(System.currentTimeMillis())
                        + "000");
                chatMsg.setCurrentStatus(MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD);
                int mDBId = insertChatMsgToChatsDB(chatMsg);
                chatMsg.setDBId(mDBId);
                mChatMsgList.add(chatMsg);
                mHandler.sendEmptyMessage(START_TO_UPLOAD_CHAT_MSG);
                addChatMsgToQueue(chatMsg);
                if (!isChatIdExistInBriefChatDB(chatId)) {
                    insertBriefChatMsgToBriefChatDB(chatMsg);
                }
            }
            break;
        }
        default:
            break;
        }
    }

    private int insertChatMsgToChatsDB(ChatMsgItem chat) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_UID, chat.getUid());
        insertValues.put(MarrySocialDBHelper.KEY_CHAT_ID, chat.getChatId());
        insertValues.put(MarrySocialDBHelper.KEY_FROM_UID, chat.getFromUid());
        insertValues.put(MarrySocialDBHelper.KEY_TO_UID, chat.getToUid());
        insertValues.put(MarrySocialDBHelper.KEY_CHAT_CONTENT,
                chat.getChatContent());
        insertValues.put(MarrySocialDBHelper.KEY_MSG_TYPE, chat.getMsgType());
        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                chat.getAddedTime());
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                chat.getCurrentStatus());

        long rowId = mDBHelper.insert(MarrySocialDBHelper.DATABASE_CHATS_TABLE,
                insertValues);
        return (int) (rowId);
    }

    private void loadChatMsgsFromChatsDB(String chat_id) {

        mChatMsgList.clear();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_CHAT_ID + " = "
                    + chat_id;
            String orderBy = MarrySocialDBHelper.KEY_ADDED_TIME + " ASC";

            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_CHATS_TABLE,
                    CHAT_PROJECTION, whereclause, null, null, null, orderBy,
                    null);

            if (cursor == null) {
                Log.e(TAG, "nannan loadChatMsgsFromChatsDB()..  cursor == null");
                return;
            }

            while (cursor.moveToNext()) {
                ChatMsgItem item = new ChatMsgItem();
                item.setUid(cursor.getString(0));
                item.setChatId(cursor.getString(1));
                item.setFromUid(cursor.getString(2));
                item.setToUid(cursor.getString(3));
                item.setChatContent(cursor.getString(4));
                item.setMsgType(cursor.getInt(5));
                String time = cursor.getString(6);
                String chat_time = time.substring(0, time.length() - 4);
                item.setAddedTime(Utils.getAddedTimeTitle(this, chat_time));
                item.setCurrentStatus(cursor.getInt(7));

                mChatMsgList.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public interface ImageCallback {
        void loadImage();
    }

    class ChatMsgSmallItem {
        ChatMsgItem msg;
        ImageCallback callback;
        String chatTime;
    }

    class UploadChatMsgsTask extends Thread {

        private volatile boolean isActive = true;
        private volatile boolean isDirty = true;
        private volatile boolean isUploading = false;

        public boolean isUploading() {
            return isUploading;
        }

        @Override
        public void run() {

            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            while (isActive) {
                synchronized (this) {
                    if (isActive && !isDirty) {
                        Utils.waitWithoutInterrupt(this);
                        continue;
                    }
                }

                isUploading = true;

                while (isActive && mChatMsgQueue.size() > 0) {
                    ChatMsgSmallItem smallItem = mChatMsgQueue.remove(0);
                    smallItem.chatTime = Utils.uploadChatMsg(
                            CommonDataStructure.URL_CHAT_TEXT,
                            smallItem.msg.getFromUid(),
                            smallItem.msg.getToUid(),
                            smallItem.msg.getChatContent());
                    if (smallItem.chatTime != null
                            && smallItem.chatTime.length() != 0) {
                        updateChatMsgStatusToChatsDB(smallItem);
                    }
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage();
                        msg.what = UPLOAD_CHAT_MSG_FINISH;
                        msg.obj = smallItem;
                        mHandler.sendMessage(msg);
                    }
                }

                isDirty = false;
                isUploading = false;

            }
        }

        public synchronized void notifyDirty() {
            isDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            isActive = false;
            notifyAll();
        }
    }

    private void updateChatMsgStatusToChatsDB(ChatMsgSmallItem task) {
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_ADDED_TIME, task.chatTime);
        values.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                MarrySocialDBHelper.UPLOAD_TO_CLOUD_SUCCESS);
        String whereClause = MarrySocialDBHelper.KEY_CHAT_ID + " = "
                + task.msg.getChatId() + " AND " + MarrySocialDBHelper.KEY_ID
                + " = " + task.msg.getDBId();
        mDBHelper.update(MarrySocialDBHelper.DATABASE_CHATS_TABLE, values,
                whereClause, null);
    }

    private void addChatMsgToQueue(ChatMsgItem msg) {
        ChatMsgSmallItem smallItem = new ChatMsgSmallItem();
        smallItem.msg = msg;
        mChatMsgQueue.add(smallItem);
        synchronized (mUploadChatMsgsTask) {
            mUploadChatMsgsTask.notifyDirty();
        }
    }

    public boolean isChatIdExistInBriefChatDB(String chatId) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_CHAT_ID + " = "
                    + chatId;
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE,
                    BRIEF_CHAT_PROJECTION, whereclause, null, null, null, null,
                    null);
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return true;
    }

    private void insertBriefChatMsgToBriefChatDB(ChatMsgItem chat) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_UID, chat.getUid());
        insertValues.put(MarrySocialDBHelper.KEY_CHAT_ID, chat.getChatId());
        insertValues.put(MarrySocialDBHelper.KEY_NIKENAME, mAuthorName);
        insertValues.put(MarrySocialDBHelper.KEY_CHAT_CONTENT,
                chat.getChatContent());
        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                chat.getAddedTime());

        mDBHelper.insert(MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE,
                insertValues);
    }

    private void updateLatestBriefChatMsgToBriefChatDB(BriefChatItem chat) {
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_UID, chat.uId);;
        values.put(MarrySocialDBHelper.KEY_NIKENAME, chat.nikename);
        values.put(MarrySocialDBHelper.KEY_CHAT_CONTENT,
                chat.chatContent);
        values.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                chat.addTime);

        String whereClause = MarrySocialDBHelper.KEY_CHAT_ID + " = " + chat.chatId;
        mDBHelper.update(MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE, values, whereClause, null);
    }

    private String queryNikenameFromContactsDB(String uId) {
        String nikename = null;
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_UID + " = "
                    + uId;
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_CONTACTS_TABLE,
                    CONTACTS_PROJECTION, whereclause, null, null, null, null,
                    null);
            if (cursor == null || cursor.getCount() == 0) {
                return nikename;
            }
            cursor.moveToNext();
            nikename = cursor.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return nikename;
    }

    private void updateBriefChatDBWhenQuit() {
        ChatMsgItem lastChatMsg = mChatMsgList.get(mChatMsgList.size() - 1);
        BriefChatItem briefChat = new BriefChatItem();
        if (lastChatMsg.getMsgType() == IMsgViewType.IMVT_COM_MSG) {
            briefChat.uId = mToUid;
        } else {
            briefChat.uId = mUid;
        }
        briefChat.chatId = mChatId;
        briefChat.nikename = queryNikenameFromContactsDB(briefChat.uId);
        briefChat.chatContent = lastChatMsg.getChatContent();
        briefChat.addTime = lastChatMsg.getAddedTime();
        updateLatestBriefChatMsgToBriefChatDB(briefChat);
    }
}
