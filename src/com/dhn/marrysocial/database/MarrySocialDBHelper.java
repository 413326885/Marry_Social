package com.dhn.marrysocial.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MarrySocialDBHelper {

    private static final String TAG = "MarrySocialDBHelper";

    public static final String DATABASE_NAME = "marrysocial";
    public static final String DATABASE_CONTACTS_TABLE = "contacts";
    public static final String DATABASE_COMMENTS_TABLE = "comments";
    public static final String DATABASE_IMAGES_TABLE = "images";
    public static final String DATABASE_BRAVOS_TABLE = "bravos";
    public static final String DATABASE_REPLYS_TABLE = "replys";
    public static final String DATABASE_CHATS_TABLE = "chats";
    public static final String DATABASE_BRIEF_CHAT_TABLE = "briefchat";
    public static final String DATABASE_HEAD_PICS_TABLE = "headpics";
    public static final String DATABASE_HEAD_BACKGROUND_PICS_TABLE = "headbackgroundpics";
    
    public static final int DATABASE_VERSION = 1;

    public static final int UPLOAD_TO_CLOUD_FAIL = -1;
    public static final int UPLOAD_TO_CLOUD_SUCCESS = 0;
    public static final int NEED_UPLOAD_TO_CLOUD = 1;
    public static final int DOWNLOAD_FROM_CLOUD_SUCCESS = 2;
    public static final int NEED_DELETE_FROM_CLOUD = 3;
    public static final int NEED_DOWNLOAD_FROM_CLOUD = 4;

    public static final int BRAVO_CANCEL = 0;
    public static final int BRAVO_CONFIRM = 1;

    // for contacts table
    public static final String KEY_UID = "uid";
    public static final String KEY_PHONE_NUM = "phoneNum";
    public static final String KEY_HEADPIC = "avatar";
    public static final String KEY_NIKENAME = "nikename";
    public static final String KEY_REALNAME = "realname";
    public static final String KEY_HOBBY = "hobby";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_ASTRO = "astro";
    public static final String KEY_DIRECT_FRIENDS_COUNT = "directfriendscount";
    public static final String KEY_DIRECT_FRIENDS = "directfriends";
    public static final String KEY_FIRST_DIRECT_FRIEND = "firstdirectfriend";
    public static final String KEY_INDIRECT_ID = "indirect_id";
    public static final String KEY_HEADER_BACKGROUND_INDEX = "header_background_index";

    // for comments table
    public static final String KEY_ID = "_id";
    public static final String KEY_COMMENT_ID = "comment_id";
    public static final String KEY_BUCKET_ID = "bucket_id";
    public static final String KEY_ADDED_TIME = "added_time";
    public static final String KEY_CONTENTS = "contents";
    public static final String KEY_PHOTO_COUNT = "photo_count";
    public static final String KEY_BRAVO_COUNT = "bravo_count";
    public static final String KEY_BRAVO_STATUS = "bravo_status";
    public static final String KEY_CURRENT_STATUS = "current_status";
    public static final String KEY_AUTHOR_FULLNAME = "author_fullname";

    // for images table
    public static final String KEY_PHOTO_NAME = "photo_name";
    public static final String KEY_PHOTO_LOCAL_PATH = "photo_local_path";
    public static final String KEY_PHOTO_REMOTE_ORG_PATH = "photo_remote_org_path";
    public static final String KEY_PHOTO_REMOTE_THUMB_PATH = "photo_remote_thumb_path";
    public static final String KEY_PHOTO_POS = "photo_position";
    public static final String KEY_PHOTO_TYPE = "photo_type";
    public static final String KEY_PHOTO_ID = "photo_id";

    // for replys table
    public static final String KEY_REPLY_CONTENTS = "reply_contents";
    public static final String KEY_REPLY_ID = "reply_id";

    // for bravos table
    public static final String KEY_BRAVO_ID = "bravo_id";

    // for chats table
    public static final String KEY_CHAT_ID = "chat_id";
    public static final String KEY_FROM_UID = "from_uid";
    public static final String KEY_TO_UID = "to_uid";
    public static final String KEY_CHAT_CONTENT = "chat_content";
    public static final String KEY_MSG_TYPE = "msg_type";

    // for head pics table
    public static final String KEY_HEAD_PIC_BITMAP = "headpicbitmap";

    private static final String DATABASE_CREATE_CONTACTS = "create table contacts ( "
            + "_id integer PRIMARY KEY AUTOINCREMENT, uid text, phoneNum text, nikename text, realname text, "
            + "hobby integer, gender integer, astro integer, directfriendscount integer, "
            + "firstdirectfriend text, directfriends text, indirect_id text, avatar integer, header_background_index text )";

    private static final String DATABASE_CREATE_COMMENTS = "create table comments ( "
            + "_id integer PRIMARY KEY AUTOINCREMENT, uid text, bucket_id text, comment_id text, "
            + "added_time text, contents text, author_fullname text, photo_count integer, "
            + "bravo_count integer, bravo_status integer, current_status integer )";

    private static final String DATABASE_CREATE_IMAGES = "create table images ( "
            + "_id integer PRIMARY KEY AUTOINCREMENT, uid text, bucket_id text, comment_id text, "
            + "photo_id text, added_time text, photo_position integer, photo_type text, photo_name text, photo_local_path text, "
            + "photo_remote_org_path text, photo_remote_thumb_path text, current_status integer )";

    private static final String DATABASE_CREATE_BRAVOS = "create table bravos ("
            + "_id integer PRIMARY KEY AUTOINCREMENT, uid text, bucket_id text, comment_id text, "
            + "author_fullname text, added_time text, current_status integer )";

    private static final String DATABASE_CREATE_REPLYS = "create table replys ("
            + "_id integer PRIMARY KEY AUTOINCREMENT, uid text, bucket_id text, comment_id text, reply_id text, "
            + "author_fullname text, reply_contents text, added_time text, current_status integer )";

    private static final String DATABASE_CREATE_CHATS = "create table chats ("
            + "_id integer PRIMARY KEY AUTOINCREMENT, uid text, chat_id text, from_uid text, to_uid text, "
            + "chat_content text, msg_type integer, added_time text, current_status integer )";

    private static final String DATABASE_CREATE_BRIEF_CHAT = "create table briefchat ("
            + "_id integer PRIMARY KEY AUTOINCREMENT, to_uid text, chat_id text, nikename text, "
            + "chat_content text, added_time text )";

    private static final String DATABASE_CREATE_HEAD_PICS = "create table headpics ("
            + "_id integer PRIMARY KEY AUTOINCREMENT, uid text, headpicbitmap blob, "
            + "photo_remote_org_path text, photo_remote_thumb_path text, current_status integer )";

    private static final String DATABASE_CREATE_HEAD_BACKGROUND_PICS = "create table headbackgroundpics ("
            + "_id integer PRIMARY KEY AUTOINCREMENT, photo_name text, photo_local_path text, "
            + "photo_remote_org_path text, header_background_index text, current_status integer )";

    private final Context mContext;
    private DataBaseOpenHelper mDataBaseHelper;
    private SQLiteDatabase mDataBase;

    private static MarrySocialDBHelper mMarrySocialDBHelper = null;

    public static MarrySocialDBHelper newInstance(Context context) {
        if (mMarrySocialDBHelper == null) {
            mMarrySocialDBHelper = new MarrySocialDBHelper(context);
        }
        mMarrySocialDBHelper.open();
        return mMarrySocialDBHelper;
    }

    private MarrySocialDBHelper(Context context) {
        mContext = context;
        mDataBaseHelper = new DataBaseOpenHelper(mContext);
    }

    private static class DataBaseOpenHelper extends SQLiteOpenHelper {

        public DataBaseOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            Log.e(TAG, "nannan DataBaseOpenHelper");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_CONTACTS);
            db.execSQL(DATABASE_CREATE_COMMENTS);
            db.execSQL(DATABASE_CREATE_IMAGES);
            db.execSQL(DATABASE_CREATE_BRAVOS);
            db.execSQL(DATABASE_CREATE_REPLYS);
            db.execSQL(DATABASE_CREATE_CHATS);
            db.execSQL(DATABASE_CREATE_BRIEF_CHAT);
            db.execSQL(DATABASE_CREATE_HEAD_PICS);
            db.execSQL(DATABASE_CREATE_HEAD_BACKGROUND_PICS);
            Log.e(TAG, "nannan onCreate");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

    }

    public MarrySocialDBHelper open() {
        mDataBase = mDataBaseHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDataBaseHelper.close();
    }

    public Cursor query(String table, String[] columns, String selection,
            String[] selectionArgs, String groupBy, String having,
            String orderBy, String limit) {
        return mDataBase.query(table, columns, selection, selectionArgs,
                groupBy, having, orderBy);
    }

    public long insert(String table, ContentValues values) {
        return mDataBase.insert(table, null, values);
    }

    public int update(String table, ContentValues values, String whereClause,
            String[] whereArgs) {
        return mDataBase.update(table, values, whereClause, whereArgs);
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
        return mDataBase.delete(table, whereClause, whereArgs);
    }
}
