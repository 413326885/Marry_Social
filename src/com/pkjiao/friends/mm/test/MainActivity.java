package com.pkjiao.friends.mm.test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.pkjiao.friends.mm.R;

public class MainActivity extends Activity implements OnClickListener{

    private static final String TAG = "MainActivity";
    private ListView mListView;
    private Button mButton;
    private JsonListAdapter mListAdapter;
    private ArrayList<String> mDataSource = new ArrayList<String>();
    RequestQueue mQueue;

    public static final String requestlist = "http://www.pkjiao.com/verify/post/eyJpdiI6IlZhVmZHNF"
            + "JPckVHQmVVYnBQdHJcL2FCbWlWWjJmR2VySGxoTFBlTVFaWHNZPSIsInZhbHVlIjoiOUxPSVwveDB2c0dTUFpwUjcz"
            + "VVBnaURCdFVrXC9oMCs5ZVZrSlFKYitUbXVjPSIsIm1hYyI6IjM3MzJiZTk1YzM2MGVjOWY3NTQyOWY0MTNlNDEyZDA"
            + "3NzE2MzJlYzA3ZDM0YTE0OGM3ZThkNWFhNTlkN2ZiNmMifQ==";

//    public static final String requestlist = "http://pipes.yahooapis.com/pipes/pipe.run?_id=giWz8Vc33BG6rQEQo_NLYQ&_render=json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.listview);
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(this);
        mListAdapter = new JsonListAdapter(this);
        mListAdapter.setDataSource(mDataSource);
        mListView.setAdapter(mListAdapter);
        mQueue = Volley.newRequestQueue(this
                .getApplicationContext());
//        initDataSource();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
//        initDataSource();
    }
    private void initDataSource() {
        // mDataSource.add("nannan");
        // mListAdapter.notifyDataSetChanged();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObject01 = new JSONObject();
//        JSONArray jsonArray = new JSONArray();
        try {
            jsonObject.put("uid", "2");
//            jsonArray.put(2);
//            jsonArray.put(5);
//            jsonArray.put(6);
            jsonObject.put("indirectuids", "2,5,6");
            try {
                jsonObject01.put("jsondata", URLEncoder.encode(jsonObject.toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Method.POST,
                requestlist, jsonObject01,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "nannan 000= "+ response.toString());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "nannan 111= "+ error.getMessage(), error);
                    }
                });
        mQueue.add(jsonObjectRequest);
    }

    @Override
    public void onClick(View v) {
        initDataSource();
        Log.e(TAG, "nannan initDataSource ");
    }
}
