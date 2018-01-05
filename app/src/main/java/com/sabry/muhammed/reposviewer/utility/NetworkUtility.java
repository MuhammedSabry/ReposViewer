package com.sabry.muhammed.reposviewer.utility;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.sabry.muhammed.reposviewer.models.GitModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.sabry.muhammed.reposviewer.MainActivity.BROADCAST_ACTION;
import static com.sabry.muhammed.reposviewer.MainActivity.DATA_STATUS;
import static com.sabry.muhammed.reposviewer.MainActivity.GIT_API_URL;
import static com.sabry.muhammed.reposviewer.MainActivity.GIT_ARRAY_LIST;

public class NetworkUtility extends IntentService {

    public NetworkUtility() {
        super("NetworkUtility");
    }

    void setRequest(String url) {
        Log.d("NetworkUtility", "setRequest method");

        Cache cache = new DiskBasedCache(getApplicationContext().getCacheDir(), 1024 * 1024);
        final Network network = new BasicNetwork(new HurlStack());
        RequestQueue queue = new RequestQueue(cache, network);

        queue.start();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET
                , url
                , null
                , new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                setList(response);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(NetworkUtility.this, "Error Retrieving data", Toast.LENGTH_LONG).show();
                returnData(null);
            }
        });
        queue.add(jsonArrayRequest);

    }

    private void setList(JSONArray jsonArray) {

        Log.d("NetworkUtility", "setList method");

        String name = "repo name not found", description = "No description", repoURL = "", userName = "Unkown author", ownerURL = "";
        boolean flag = false;
        ArrayList<GitModel> arrayList = new ArrayList<>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject masterObject = jsonArray.getJSONObject(i);

                name = masterObject.getString("name");
                description = masterObject.getString("description");
                flag = masterObject.getBoolean("fork");
                repoURL = masterObject.getString("html_url");

                JSONObject ownerObject = masterObject.getJSONObject("owner");
                userName = ownerObject.getString("login");
                ownerURL = ownerObject.getString("html_url");

            } catch (JSONException e) {
                arrayList.clear();
                break;
            }
            GitModel data = new GitModel(name, userName, description, repoURL, ownerURL, flag);
            arrayList.add(data);
        }

        returnData(arrayList);


    }

    private void returnData(ArrayList<GitModel> arrayList) {
        Intent localIntent =
                new Intent(BROADCAST_ACTION)
                        // Puts the status into the Intent
                        .putExtra(DATA_STATUS, true)
                        .putExtra(GIT_ARRAY_LIST, arrayList);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d("NetworkUtility", "onHandleIntent");
        String url = intent.getStringExtra(GIT_API_URL);
        setRequest(url);
    }
}
