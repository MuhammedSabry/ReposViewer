package com.sabry.muhammed.reposviewer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sabry.muhammed.reposviewer.adapter.Adapter;
import com.sabry.muhammed.reposviewer.models.GitModel;
import com.sabry.muhammed.reposviewer.utility.NetworkUtility;
import com.sabry.muhammed.reposviewer.utility.ObjectSerializer;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity
        extends AppCompatActivity {

    public static final String BROADCAST_ACTION =
            "gitAction";
    public static final String DATA_STATUS =
            "STATUS";
    public static final String GIT_API_URL =
            "GitURL";
    public static final String GIT_ARRAY_LIST =
            "GIT Array List";
    public final String SHARED_PREFS_FILE = "Repos_Viewer";
    private ArrayList<GitModel> gitModelArrayList;

    private final int PAGE_SIZE = 10;

    SwipeRefreshLayout refreshLayout;
    DownloadStateReceiver receiver;
    ContentLoadingProgressBar progressBar;
    Adapter mAdapter;
    TextView emptyView;
    private int Page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //finding the views
        RecyclerView recyclerView = findViewById(R.id.mainActivityRecyclerView);
        emptyView = findViewById(R.id.emptyTag);
        progressBar = findViewById(R.id.contentLoadingProgressBar);
        refreshLayout = findViewById(R.id.refresherLayout);

        //loading cached data once activity starts
        Log.d("Main Activity", "trying to read from cache");
        if (readCachedFile()) {
            Page = gitModelArrayList.size() / 10;
            Log.d("Main Activity", "read Successful!");
        }


        receiver = new DownloadStateReceiver();

        //instantiate the arraylist if it's empty or no items
        if (gitModelArrayList == null || gitModelArrayList.size() == 0) {
            gitModelArrayList = new ArrayList<>(10);
            Page = 1;
        }

        //setting up the recycler view
        mAdapter = new Adapter(this, gitModelArrayList);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this
                , LinearLayoutManager.VERTICAL
                , false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        //checking internet connectivity
        if (isConnected())
            getRepos();
        else
            Toast.makeText(this, "No Network Connection!", Toast.LENGTH_LONG).show();

        //handling on swipe refresher event
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isConnected()) {
                    Page = 1;
                    getRepos();
                } else
                    refreshLayout.setRefreshing(false);
            }
        });

        //scrolling infinitely!
        RecyclerView.OnScrollListener listener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1) && layoutManager.getItemCount() == gitModelArrayList.size()) {
                    if (isConnected()) {
                        Page++;
                        getRepos();
                    }
                }
            }
        };
        recyclerView.addOnScrollListener(listener);
    }

    //method that build the api request url
    private String buildUrl() {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("https")
                .authority("api.github.com")
                .appendPath("users")
                .appendPath("square")
                .appendPath("repos")
                .appendQueryParameter("page", String.valueOf(Page))
                .appendQueryParameter("per_page", String.valueOf(PAGE_SIZE));
        return uriBuilder.build().toString();
    }

    //sending the request to the IntentService
    private void getRepos() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.animate();
        Intent intent = new Intent(this, NetworkUtility.class);
        intent.putExtra(GIT_API_URL
                , buildUrl());
        startService(intent);
    }


    //unregister the receiver
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    //registering the receiver
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
                new IntentFilter(BROADCAST_ACTION));
    }

    //the broadcast receiver that handles setting the adapter data
    public class DownloadStateReceiver extends BroadcastReceiver {
        // Prevents instantiation
        private DownloadStateReceiver() {
        }

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("mainActivity", "broadcast OnRecieve");

            progressBar.setVisibility(View.GONE);
            if (intent.getAction() == BROADCAST_ACTION) {
                if (intent.getBooleanExtra(DATA_STATUS, false)) {
                    ArrayList<GitModel> list = (ArrayList<GitModel>)
                            intent.getSerializableExtra(GIT_ARRAY_LIST);
                    if (list == null || list.isEmpty()) {
                        if (gitModelArrayList.size() <= 0) {
                            refreshLayout.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        }
                    }
                    //in case of fire!
                    else {
                        emptyView.setVisibility(View.GONE);
                        refreshLayout.setVisibility(View.VISIBLE);
                        gitModelArrayList.addAll(list);
                        mAdapter.notifyDataSetChanged();
                        cacheFiles(false);
                    }
                    //resetting the data when refreshing
                    if (refreshLayout.isRefreshing()) {
                        cacheFiles(true);
                        gitModelArrayList.clear();
                        refreshLayout.setRefreshing(false);
                    }
                }
            }
        }
    }

    //handling the data caching
    public void cacheFiles(boolean delete) {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        if (delete) {
            editor.clear();
        } else {
            try {
                editor.putString(GIT_ARRAY_LIST, ObjectSerializer.serialize(gitModelArrayList));
            } catch (IOException e) {
                e.printStackTrace();
            }
            editor.apply();
        }

    }

    //reading the data from the sharedpreference
    public boolean readCachedFile() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_FILE, Context.MODE_PRIVATE);

        try {
            gitModelArrayList = (ArrayList<GitModel>) ObjectSerializer.deserialize(prefs.getString(GIT_ARRAY_LIST, ObjectSerializer.serialize(new ArrayList<GitModel>())));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    //checking if there is an internet connection
    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        }
        return false;
    }
}
