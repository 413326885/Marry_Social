package com.dhn.marrysocial.services;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dhn.marrysocial.common.CommonDataStructure;
import com.dhn.marrysocial.database.MarrySocialDBHelper;
import com.dhn.marrysocial.utils.Utils;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.dhn.marrysocial.R;

public class ReadContactsIntentService extends IntentService {

    private static final String TAG = "ReadContactsIntentService";

    private static final String[] DIRECT_PROJECTION = {
            MarrySocialDBHelper.KEY_PHONE_NUM,
            MarrySocialDBHelper.KEY_REALNAME,
            MarrySocialDBHelper.KEY_DIRECT_ID,
            MarrySocialDBHelper.KEY_DIRECT_UID };

    public static final String FULL_NAME = "fullname";
    public static final String PHONE_NUM = "phone";
    public static final String UID = "uid";

    private String mUid;
    private MarrySocialDBHelper mDBHelper;

    public ReadContactsIntentService() {
        super(TAG);
    }

    public ReadContactsIntentService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = this.getSharedPreferences(
                CommonDataStructure.PREFS_LAIQIAN_DEFAULT, MODE_PRIVATE);
        mUid = prefs.getString(CommonDataStructure.UID, "");
        mDBHelper = MarrySocialDBHelper.newInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (!Utils.isActiveNetWorkAvailable(this)) {
            Toast.makeText(this, R.string.network_not_available, 1000).show();
            return;
        }

        ArrayList<CommonDataStructure.ContactEntry> contacts = getAllContactsInfo();
        if (contacts == null || contacts.size() == 0) {
            return;
        }

        ArrayList<CommonDataStructure.ContactEntry> resultEntry = Utils
                .uploadUserContacts(CommonDataStructure.URL_UPLOAD_CONTACTS,
                        mUid, contacts);
        if (resultEntry == null || resultEntry.size() == 0) {
            return;
        }
        for (CommonDataStructure.ContactEntry entry : resultEntry) {
            if (!isPhoneNumExistInDirectDB(entry.contact_phone_number)) {
                insertContactsToDirectDB(entry);
            } else {
                updateDirectIdToDirectDB(entry);
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private ArrayList<CommonDataStructure.ContactEntry> getAllContactsInfo() {
        ArrayList<CommonDataStructure.ContactEntry> contactMembers = new ArrayList<CommonDataStructure.ContactEntry>();
        Cursor cursor = null;

        try {
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            // 获取联系人表的电话里的信息 包括：名字，名字拼音，联系人id,电话号码；
            // 然后在根据"sort-key"排序
            cursor = this.getContentResolver().query(
                    uri,
                    new String[] { "display_name", "sort_key", "contact_id",
                            "data1" }, null, null, "sort_key");

            while (cursor.moveToNext()) {
                CommonDataStructure.ContactEntry contact = new CommonDataStructure.ContactEntry();
                String name = cursor.getString(0);
                String sortKey = getSortKey(cursor.getString(1));
                String contact_phone = cursor
                        .getString(cursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int contact_id = cursor
                        .getInt(cursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                contact.contact_name = name;
                contact.contact_sortKey = sortKey;
                contact.contact_phone_number = contact_phone;
                contact.contact_id = contact_id;
                if (name != null && isPhoneNumber(contact_phone)) {
                    contactMembers.add(contact);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return contactMembers;
    }

    private static String getSortKey(String sortKeyString) {
        String key = sortKeyString.substring(0, 1).toUpperCase();
        if (key.matches("[A-Z]")) {
            return key;
        }
        return "#";
    }

    private static boolean isPhoneNumber(String input) {

        if (input == null) {
            return false;
        }

        String regex = "1([\\d]{10})|((\\+[0-9]{2,4})?\\(?[0-9]+\\)?-?)?[0-9]{7,8}";
        Pattern p = Pattern.compile(regex);
        return p.matcher(input).matches();
    }

    private void insertContactsToDirectDB(
            CommonDataStructure.ContactEntry contact) {

        ContentValues insertValues = new ContentValues();
        insertValues.put(MarrySocialDBHelper.KEY_PHONE_NUM,
                contact.contact_phone_number);
        insertValues
                .put(MarrySocialDBHelper.KEY_REALNAME, contact.contact_name);
        insertValues.put(MarrySocialDBHelper.KEY_DIRECT_ID, contact.direct_id);
        insertValues
                .put(MarrySocialDBHelper.KEY_DIRECT_UID, contact.direct_uid);

        try {
            mDBHelper.insert(MarrySocialDBHelper.DATABASE_DIRECT_TABLE,
                    insertValues);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    private void updateDirectIdToDirectDB(
            CommonDataStructure.ContactEntry contact) {

        ContentValues values = new ContentValues();
        values.put(MarrySocialDBHelper.KEY_PHONE_NUM,
                contact.contact_phone_number);
        values.put(MarrySocialDBHelper.KEY_REALNAME, contact.contact_name);
        values.put(MarrySocialDBHelper.KEY_DIRECT_ID, contact.direct_id);
        values.put(MarrySocialDBHelper.KEY_DIRECT_UID, contact.direct_uid);

        String whereClause = MarrySocialDBHelper.KEY_PHONE_NUM + " = " + '"'
                + contact.contact_phone_number + '"';

        try {
            mDBHelper.update(MarrySocialDBHelper.DATABASE_DIRECT_TABLE, values,
                    whereClause, null);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    public boolean isPhoneNumExistInDirectDB(String phone) {
        Cursor cursor = null;
        try {
            String whereclause = MarrySocialDBHelper.KEY_PHONE_NUM + " = "
                    + '"' + phone + '"';
            cursor = mDBHelper.query(MarrySocialDBHelper.DATABASE_DIRECT_TABLE,
                    DIRECT_PROJECTION, whereclause, null, null, null, null,
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
}
