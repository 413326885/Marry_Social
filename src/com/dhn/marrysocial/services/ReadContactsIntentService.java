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

import com.dhn.marrysocial.utils.Utils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.dhn.marrysocial.R;

public class ReadContactsIntentService extends IntentService {

    private static final String TAG = "ReadContactsIntentService";

    public static final String POST_URL = "http://182.92.215.1/direct/add";
    public static final String FULL_NAME = "fullname";
    public static final String PHONE_NUM = "phone";
    public static final String UID = "uid";

    public ReadContactsIntentService() {
        super(TAG);
    }
    
    public ReadContactsIntentService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (!Utils.isActiveNetWorkAvailable(this)) {
            Toast.makeText(this, R.string.network_not_available, 1000).show();
            return;
        }

        URL postUrl = null;
        try {
            postUrl = new URL(POST_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (postUrl == null)
            return;

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) postUrl.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (connection == null)
            return;

        connection.setDoOutput(true);
        connection.setDoInput(true);
        try {
            connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");

        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        DataOutputStream out = null;
        try {
            out = new DataOutputStream(connection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray contactList = new JSONArray();
        ArrayList<ContactEntry> oriContacts = getAllContactsInfo(this);
        for (ContactEntry entry : oriContacts) {
            JSONObject contact = new JSONObject();
            try {
                contact.put(FULL_NAME, entry.contact_name);
                contact.put(PHONE_NUM, entry.contact_phone_number);
                contact.put(UID, "1");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            contactList.put(contact);
        }


        String content = null;
        try {
            content = "contacts="
                    + URLEncoder.encode(contactList.toString(), "UTF-8");
            Log.e(TAG, "nannan content = " + content);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        if (content == null)
            return;
        try {
            out.writeBytes(content);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                Log.e(TAG,"nannan response = " + line);
//            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private ArrayList<ContactEntry> getAllContactsInfo(Context context) {
        ArrayList<ContactEntry> contactMembers = new ArrayList<ContactEntry>();
        Cursor cursor = null;

        try {
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            // 获取联系人表的电话里的信息 包括：名字，名字拼音，联系人id,电话号码；
            // 然后在根据"sort-key"排序
            cursor = context.getContentResolver().query(
                    uri,
                    new String[] { "display_name", "sort_key", "contact_id",
                            "data1" }, null, null, "sort_key");

            while (cursor.moveToNext()) {
                ContactEntry contact = new ContactEntry();
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

    static class ContactEntry {
        public String contact_name;
        public String contact_phone_number;
        public int contact_id;
        public String contact_sortKey;
    }
}
