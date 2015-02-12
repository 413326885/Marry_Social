package com.pkjiao.friends.mm.activity;

import java.util.ArrayList;
import java.util.HashMap;

import com.dhn.marrysocial.R;
import com.pkjiao.friends.mm.adapter.ChatMsgViewAdapter;
import com.pkjiao.friends.mm.adapter.ChatMsgViewAdapter.IMsgViewType;
import com.pkjiao.friends.mm.base.ChatMsgItem;
import com.pkjiao.friends.mm.common.CommonDataStructure;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;
import com.pkjiao.friends.mm.fragment.ChatMsgFragment.BriefChatItem;
import com.pkjiao.friends.mm.roundedimageview.RoundedImageView;
import com.pkjiao.friends.mm.utils.Utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
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
            MarrySocialDBHelper.KEY_TO_UID, MarrySocialDBHelper.KEY_CHAT_ID };

    private static final String[] CONTACTS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_NICKNAME };

    private static final String[] HEAD_PICS_PROJECTION = {
            MarrySocialDBHelper.KEY_UID,
            MarrySocialDBHelper.KEY_HEAD_PIC_BITMAP };

    private static final int START_TO_UPLOAD_CHAT_MSG = 100;
    private static final int UPLOAD_CHAT_MSG_FINISH = 101;
    private static final int SHOW_LISTVIEW_FROM_BACK = 102;

    private static final int UPDATE_CHAT_MSG = 103;

    private static final int TOUCH_FING_UP = 110;
    private static final int TOUCH_FING_DOWN = 111;

    private ListView mListView;
    private ChatMsgViewAdapter mListViewAdapter;
    private RelativeLayout mChatReturnBtn;
    private RoundedImageView mChatPersonHeadPic;
    private TextView mChatPersonName;
    private ImageView mChatSendBtn;
    private EditText mChatContent;
    private TextView mEmptyView;

    private String mAuthorUid;
    private String mAuthorName;
    private String mToUid;
    private String mChatUserName;
    private String mChatId;

    private float mTouchDownY = 0.0f;
    private float mTouchMoveY = 0.0f;
    private boolean mIsFingUp = false;

    private MarrySocialDBHelper mDBHelper;
    private ArrayList<ChatMsgItem> mChatMsgList = new ArrayList<ChatMsgItem>();
    private HashMap<String, Bitmap> mHeadPics = new HashMap<String, Bitmap>();

    private ArrayList<ChatMsgSmallItem> mChatMsgQueue;
    private UploadChatMsgsTask mUploadChatMsgsTask;

    private DataSetChangeObserver mChangeObserver;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case START_TO_UPLOAD_CHAT_MSG: {
                mListViewAdapter.notifyDataSetChanged();
                // Utils.hideSoftInputMethod(mChatContent);
                mChatContent.setText(null);
                mListView.setSelection(mListView.getCount() - 1);
                break;
            }
            case UPLOAD_CHAT_MSG_FINISH: {
                ChatMsgSmallItem task = (ChatMsgSmallItem) msg.obj;
                // todo something
                break;
            }
            case TOUCH_FING_UP: {
                Utils.hideSoftInputMethod(mChatContent);
                break;
            }
            case TOUCH_FING_DOWN: {
                Utils.hideSoftInputMethod(mChatContent);
                break;
            }
            case SHOW_LISTVIEW_FROM_BACK: {
                mListView.setSelection(mListView.getCount() - 1);
                break;
            }
            case UPDATE_CHAT_MSG: {
                loadChatMsgsFromChatsDB(mChatId);
                mListViewAdapter.notifyDataSetChanged();
                mListView.setSelection(mListView.getCount() - 1);
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
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.chat_msg_layout);

        Intent data = getIntent();
        mChatId = data.getStringExtra(MarrySocialDBHelper.KEY_CHAT_ID);
        String[] ids = mChatId.split("_");
        mToUid = ids[1];

        mDBHelper = MarrySocialDBHelper.newInstance(this);
        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, this.MODE_PRIVATE);
        mAuthorUid = prefs.getString(CommonDataStructure.UID, "");
        mAuthorName = prefs.getString(CommonDataStructure.AUTHOR_NAME, "");

        mChatUserName = queryNikenameFromContactsDB(mToUid);

        mChatReturnBtn = (RelativeLayout) this
                .findViewById(R.id.chat_msg_return);
        mChatPersonHeadPic = (RoundedImageView) this
                .findViewById(R.id.chat_msg_person_pic);
        mChatPersonName = (TextView) this
                .findViewById(R.id.chat_msg_person_name);
        mChatPersonName.setText(mChatUserName);

        mChatSendBtn = (ImageView) this.findViewById(R.id.chat_msg_chat_send);
        mChatContent = (EditText) this
                .findViewById(R.id.chat_msg_chat_contents);
        mChatContent.setOnClickListener(this);

        mEmptyView = (TextView) this.findViewById(R.id.chat_msg_no_content);
        mChatReturnBtn.setOnClickListener(this);
        mChatSendBtn.setOnClickListener(this);

        mListView = (ListView) this.findViewById(R.id.chat_msg_listview);
        mListViewAdapter = new ChatMsgViewAdapter(this);
        mListViewAdapter.setChatDataSource(mChatMsgList);
        mListViewAdapter.setHeadPisDataSource(mHeadPics);
        mListView.setAdapter(mListViewAdapter);
        mListView.setEmptyView(mEmptyView);
        mListView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTouchDownY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mTouchMoveY = event.getY() - mTouchDownY;
                    if (Math.abs(mTouchMoveY) < 50) {
                        break;
                    }
                    if (mTouchMoveY < 0) {
                        if (!mIsFingUp) {
                            mIsFingUp = true;
                            mHandler.sendEmptyMessageDelayed(TOUCH_FING_UP, 50);
                        }
                    } else {
                        if (mIsFingUp) {
                            mIsFingUp = false;
                            mHandler.sendEmptyMessageDelayed(TOUCH_FING_DOWN,
                                    50);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
                default:
                    break;
                }
                return false;
            }
        });

        mChatMsgQueue = new ArrayList<ChatMsgSmallItem>();

        mChangeObserver = new DataSetChangeObserver(mHandler);
        this.getContentResolver().registerContentObserver(
                CommonDataStructure.CHATURL, true, mChangeObserver);

        getChatPersonPics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateReadStatusToChatsDB(mChatId);
        updateHasNewMsgStatusToBriefChatsDB(mChatId);
        loadChatMsgsFromChatsDB(mChatId);
        mListViewAdapter.notifyDataSetChanged();
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
    public void onDestroy() {
        super.onDestroy();
        this.getContentResolver().unregisterContentObserver(mChangeObserver);
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
                String chatId = mAuthorUid + "_" + mToUid;
                ChatMsgItem chatMsg = new ChatMsgItem();
                chatMsg.setUid(mAuthorUid);
                chatMsg.setChatId(chatId);
                chatMsg.setFromUid(mAuthorUid);
                chatMsg.setToUid(mToUid);
                chatMsg.setChatContent(chatContents);
                chatMsg.setMsgType(IMsgViewType.IMVT_TO_MSG);
                chatMsg.setAddedTime(Long.toString(System.currentTimeMillis() * 1000));
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
        case R.id.chat_msg_chat_contents: {
            mChatContent.setFocusable(true);
            mChatContent.requestFocus();
            // mHandler.sendEmptyMessageDelayed(SHOW_LISTVIEW_FROM_BACK, 3000);
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
        insertValues.put(MarrySocialDBHelper.KEY_READ_STATUS,
                MarrySocialDBHelper.MSG_READED);
        insertValues.put(MarrySocialDBHelper.KEY_CURRENT_STATUS,
                chat.getCurrentStatus());

        long rowId = 0;
        try {
            rowId = mDBHelper.insert(MarrySocialDBHelper.DATABASE_CHATS_TABLE,
                    insertValues);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

        return (int) (rowId);
    }

    private void loadChatMsgsFromChatsDB(String chat_id) {

        mChatMsgList.clear();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_CHAT_ID + " = " + '"'
                    + chat_id + '"';
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
                // String chat_time = time.substring(0, time.length() - 6);
                item.setAddedTime(time);
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
        String whereClause = MarrySocialDBHelper.KEY_CHAT_ID + " = " + '"'
                + task.msg.getChatId() + '"' + " AND "
                + MarrySocialDBHelper.KEY_ID + " = " + task.msg.getDBId();

        try {
            mDBHelper.update(MarrySocialDBHelper.DATABASE_CHATS_TABLE, values,
                    whereClause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

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
            String whereclause = MarrySocialDBHelper.KEY_CHAT_ID + " = " + '"'
                    + chatId + '"';
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE,
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
        insertValues.put(MarrySocialDBHelper.KEY_TO_UID, mToUid);
        insertValues.put(MarrySocialDBHelper.KEY_CHAT_ID, chat.getChatId());
        insertValues.put(MarrySocialDBHelper.KEY_NICKNAME, mChatUserName);
        insertValues.put(MarrySocialDBHelper.KEY_CHAT_CONTENT,
                chat.getChatContent());
        insertValues.put(MarrySocialDBHelper.KEY_ADDED_TIME,
                chat.getAddedTime());
        insertValues.put(MarrySocialDBHelper.KEY_HAS_NEW_MSG,
                MarrySocialDBHelper.HAS_NO_MSG);

        try {
            mDBHelper.insert(MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE,
                    insertValues);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    private void updateLatestBriefChatMsgToBriefChatDB(BriefChatItem chat) {
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_TO_UID, mToUid);
        values.put(MarrySocialDBHelper.KEY_NICKNAME, mChatUserName);
        values.put(MarrySocialDBHelper.KEY_CHAT_CONTENT, chat.chatContent);
        values.put(MarrySocialDBHelper.KEY_ADDED_TIME, chat.addTime);
        values.put(MarrySocialDBHelper.KEY_HAS_NEW_MSG,
                MarrySocialDBHelper.HAS_NO_MSG);

        String whereClause = MarrySocialDBHelper.KEY_CHAT_ID + " = " + '"'
                + chat.chatId + '"';

        try {
            mDBHelper.update(MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE,
                    values, whereClause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    private String queryNikenameFromContactsDB(String uId) {
        String nikename = null;
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_UID + " = " + uId;
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_CONTACTS_TABLE,
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
        if (mChatMsgList == null || mChatMsgList.size() == 0) {
            return;
        }
        ChatMsgItem lastChatMsg = mChatMsgList.get(mChatMsgList.size() - 1);
        BriefChatItem briefChat = new BriefChatItem();
        briefChat.toUid = mToUid;
        briefChat.nikename = mChatUserName;
        briefChat.chatId = mChatId;
        briefChat.chatContent = lastChatMsg.getChatContent();
        briefChat.addTime = lastChatMsg.getAddedTime();
        updateLatestBriefChatMsgToBriefChatDB(briefChat);
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

    private void getChatPersonPics() {
        mHeadPics.put(mToUid, getHeadPicBitmap(mToUid));
        mHeadPics.put(mAuthorUid, getHeadPicBitmap(mAuthorUid));
    }

    private Bitmap getHeadPicBitmap(String uid) {

        Bitmap headpic = null;

        String whereClause = MarrySocialDBHelper.KEY_UID + " = " + uid;
        Cursor cursor = mDBHelper
                .query(MarrySocialDBHelper.DATABASE_HEAD_PICS_TABLE,
                        HEAD_PICS_PROJECTION, whereClause, null, null, null,
                        null, null);

        if (cursor == null || cursor.getCount() == 0) {
            Log.w(TAG, "nannan query fail! Uid = " + uid);
            return null;
        }

        try {
            cursor.moveToFirst();
            byte[] in = cursor.getBlob(1);
            headpic = BitmapFactory.decodeByteArray(in, 0, in.length);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return headpic;
    }

    private void updateReadStatusToChatsDB(String mChatId) {
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_READ_STATUS,
                MarrySocialDBHelper.MSG_READED);
        String whereClause = MarrySocialDBHelper.KEY_CHAT_ID + " = " + '"'
                + mChatId + '"' + " AND " + MarrySocialDBHelper.KEY_READ_STATUS
                + " = " + MarrySocialDBHelper.MSG_NOT_READ;

        try {
            mDBHelper.update(MarrySocialDBHelper.DATABASE_CHATS_TABLE, values,
                    whereClause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    private void updateHasNewMsgStatusToBriefChatsDB(String mChatId) {
        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_HAS_NEW_MSG,
                MarrySocialDBHelper.HAS_NO_MSG);

        String whereClause = MarrySocialDBHelper.KEY_CHAT_ID + " = " + '"'
                + mChatId + '"' + " AND " + MarrySocialDBHelper.KEY_HAS_NEW_MSG
                + " = " + MarrySocialDBHelper.HAS_NEW_MSG;

        try {
            mDBHelper.update(MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE,
                    values, whereClause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }
}
