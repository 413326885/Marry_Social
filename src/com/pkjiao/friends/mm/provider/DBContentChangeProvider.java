package com.pkjiao.friends.mm.provider;

import com.pkjiao.friends.mm.database.MarrySocialDBHelper;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class DBContentChangeProvider extends ContentProvider {

    private static final String TAG = "DataSetProvider";

    private static final int KEY_COMMENTS = 1;
    private static final int KEY_BRAVOS = 2;
    private static final int KEY_REPLYS = 3;
    private static final int KEY_IAMGES = 4;
    private static final int KEY_CHATS = 5;
    private static final int KEY_BRIEFCHATS = 6;
    private static final int KEY_HEADPICS = 7;
    private static final int KEY_HEADBACKGROUND = 8;

    public static final String AUTHORITY = "com.pkjiao.friends.mm.provider";

    private MarrySocialDBHelper mDBHelper;
    private UriMatcher mMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Override
    public boolean onCreate() {
        mDBHelper = MarrySocialDBHelper.newInstance(this.getContext());
        mMatcher.addURI(AUTHORITY, MarrySocialDBHelper.DATABASE_COMMENTS_TABLE, KEY_COMMENTS);
        mMatcher.addURI(AUTHORITY, MarrySocialDBHelper.DATABASE_BRAVOS_TABLE, KEY_BRAVOS);
        mMatcher.addURI(AUTHORITY, MarrySocialDBHelper.DATABASE_REPLYS_TABLE, KEY_REPLYS);
        mMatcher.addURI(AUTHORITY, MarrySocialDBHelper.DATABASE_IMAGES_TABLE, KEY_IAMGES);
        mMatcher.addURI(AUTHORITY, MarrySocialDBHelper.DATABASE_CHATS_TABLE, KEY_CHATS);
        mMatcher.addURI(AUTHORITY, MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE, KEY_BRIEFCHATS);
        mMatcher.addURI(AUTHORITY, MarrySocialDBHelper.DATABASE_HEAD_PICS_TABLE, KEY_HEADPICS);
        mMatcher.addURI(AUTHORITY, MarrySocialDBHelper.DATABASE_HEAD_BACKGROUND_PICS_TABLE, KEY_HEADBACKGROUND);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return "";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch(mMatcher.match(uri)){
            case KEY_COMMENTS: {
                long rowId = mDBHelper.insert(MarrySocialDBHelper.DATABASE_COMMENTS_TABLE, values);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, rowId);
            }
            case KEY_BRAVOS: {
                long rowId = mDBHelper.insert(MarrySocialDBHelper.DATABASE_BRAVOS_TABLE, values);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, rowId);
            }
            case KEY_REPLYS: {
                long rowId = mDBHelper.insert(MarrySocialDBHelper.DATABASE_REPLYS_TABLE, values);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, rowId);
            }
            case KEY_IAMGES: {
                long rowId = mDBHelper.insert(MarrySocialDBHelper.DATABASE_IMAGES_TABLE, values);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, rowId);
            }
            case KEY_CHATS: {
                long rowId = mDBHelper.insert(MarrySocialDBHelper.DATABASE_CHATS_TABLE, values);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, rowId);
            }
            case KEY_BRIEFCHATS: {
                long rowId = mDBHelper.insert(MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE, values);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, rowId);
            }
            case KEY_HEADPICS: {
                long rowId = mDBHelper.insert(MarrySocialDBHelper.DATABASE_HEAD_PICS_TABLE, values);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, rowId);
            }
            case KEY_HEADBACKGROUND: {
                long rowId = mDBHelper.insert(MarrySocialDBHelper.DATABASE_HEAD_BACKGROUND_PICS_TABLE, values);
                this.getContext().getContentResolver().notifyChange(uri, null);
                return ContentUris.withAppendedId(uri, rowId);
            }
            default:
                break;
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch(mMatcher.match(uri)){
        case KEY_COMMENTS: {
            int rowId = mDBHelper.delete(MarrySocialDBHelper.DATABASE_COMMENTS_TABLE, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        case KEY_BRAVOS: {
            int rowId = mDBHelper.delete(MarrySocialDBHelper.DATABASE_BRAVOS_TABLE, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        case KEY_REPLYS: {
            int rowId = mDBHelper.delete(MarrySocialDBHelper.DATABASE_REPLYS_TABLE, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        case KEY_IAMGES: {
            int rowId = mDBHelper.delete(MarrySocialDBHelper.DATABASE_IMAGES_TABLE, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        case KEY_CHATS: {
            int rowId = mDBHelper.delete(MarrySocialDBHelper.DATABASE_CHATS_TABLE, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        case KEY_BRIEFCHATS: {
            int rowId = mDBHelper.delete(MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        case KEY_HEADPICS: {
            int rowId = mDBHelper.delete(MarrySocialDBHelper.DATABASE_HEAD_PICS_TABLE, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        case KEY_HEADBACKGROUND: {
            int rowId = mDBHelper.delete(MarrySocialDBHelper.DATABASE_HEAD_BACKGROUND_PICS_TABLE, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        default:
            break;
    }
        return -1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        switch(mMatcher.match(uri)){
        case KEY_COMMENTS: {
            int rowId = mDBHelper.update(MarrySocialDBHelper.DATABASE_COMMENTS_TABLE, values, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        case KEY_BRAVOS: {
            int rowId = mDBHelper.update(MarrySocialDBHelper.DATABASE_BRAVOS_TABLE, values, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        case KEY_REPLYS: {
            int rowId = mDBHelper.update(MarrySocialDBHelper.DATABASE_REPLYS_TABLE, values, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        case KEY_IAMGES: {
            int rowId = mDBHelper.update(MarrySocialDBHelper.DATABASE_IMAGES_TABLE, values, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        case KEY_CHATS: {
            int rowId = mDBHelper.update(MarrySocialDBHelper.DATABASE_CHATS_TABLE, values, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        case KEY_BRIEFCHATS: {
            int rowId = mDBHelper.update(MarrySocialDBHelper.DATABASE_BRIEF_CHAT_TABLE, values, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        case KEY_HEADPICS: {
            int rowId = mDBHelper.update(MarrySocialDBHelper.DATABASE_HEAD_PICS_TABLE, values, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        case KEY_HEADBACKGROUND: {
            int rowId = mDBHelper.update(MarrySocialDBHelper.DATABASE_HEAD_BACKGROUND_PICS_TABLE, values, selection, selectionArgs);
            this.getContext().getContentResolver().notifyChange(uri, null);
            return rowId;
        }
        default:
            break;
    }
        return -1;
    }

}
