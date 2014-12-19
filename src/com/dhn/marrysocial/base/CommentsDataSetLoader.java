package com.dhn.marrysocial.base;

import java.util.ArrayList;

import com.dhn.marrysocial.database.MarrySocialDBHelper;

import android.content.Context;
import android.database.Cursor;
import android.os.Process;
import android.util.Log;

public class CommentsDataSetLoader {

    private static final String TAG = "AlbumSetDataAdapter";

    // public static interface DataListener {
    // public void onContentChanged(int index);
    //
    // public void onSizeChanged(int size);
    // }
    //
    // private DataListener mDataListener;

    private MarrySocialDBHelper mDBHelper;
    private Context mContext;
    private ReloadTask mReloadTask;
    private ArrayList<CommentsItem> mCommentEntries = new ArrayList<CommentsItem>();

    public CommentsDataSetLoader(Context context) {
        mContext = context;
        mDBHelper = MarrySocialDBHelper.newInstance(mContext);
    }
    
    public void pause() {
        mReloadTask.terminate();
        mReloadTask = null;
    }

    public void resume() {
        mReloadTask = new ReloadTask();
        mReloadTask.start();
    }

    public ArrayList<CommentsItem> getCommentEntries() {
        return mCommentEntries;
    }

    // TODO: load active range first
    private class ReloadTask extends Thread {

        private volatile boolean mActive = true;
        private volatile boolean mDirty = true;

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            boolean updateComplete = false;
            while (mActive) {
                synchronized (this) {
                    if (mActive && !mDirty) {

                        waitWithoutInterrupt(this);
                        continue;
                    }
                }
                mDirty = false;
            }
        }

        public synchronized void notifyDirty() {
            mDirty = true;
            notifyAll();
        }

        public synchronized void terminate() {
            mActive = false;
            notifyAll();
        }
    }

    public static void waitWithoutInterrupt(Object object) {
        try {
            object.wait();
        } catch (InterruptedException e) {
            Log.w(TAG, "unexpected interrupt: " + object);
        }
    }
    
    private final String[] COMMENTS_PROJECTION = {
        MarrySocialDBHelper.KEY_UID, MarrySocialDBHelper.KEY_BUCKET_ID,
        MarrySocialDBHelper.KEY_CONTENTS, MarrySocialDBHelper.KEY_ADDED_TIME };
    
    private ArrayList<CommentsItem> loadCommentsItemFromDB(Context context) {
        ArrayList<CommentsItem> commentEntrys = new ArrayList<CommentsItem>();
        Cursor cursor = null;

        try {
            String whereclause = MarrySocialDBHelper.KEY_CURRENT_STATUS + " = "
                    + MarrySocialDBHelper.NEED_UPLOAD_TO_CLOUD;
            cursor = mDBHelper.query(
                    MarrySocialDBHelper.DATABASE_COMMENTS_TABLE,
                    COMMENTS_PROJECTION, null, null, null, null, null,
                    null);
            if (cursor == null) {
                return commentEntrys;
            }
            while (cursor.moveToNext()) {
                CommentsItem comment = new CommentsItem();
                String uId = cursor.getString(0);
                String bucketId = cursor.getString(1);
                String contents = cursor.getString(2);
                String added_time = cursor.getString(3);
                comment.setUid(uId);
                comment.setBucketId(bucketId);
                comment.setContents(contents);
                comment.setAddTime(added_time);
                comment.setIsBravo(true);
                commentEntrys.add(comment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }
        return commentEntrys;
    }
}
