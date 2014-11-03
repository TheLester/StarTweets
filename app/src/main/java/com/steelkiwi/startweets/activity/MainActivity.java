package com.steelkiwi.startweets.activity;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.steelkiwi.startweets.util.AndroidNetworkUtility;


public class MainActivity extends ListActivity {

    final static String twitterScreenName = "fouryearstrong";
    final static String TAG = "MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidNetworkUtility androidNetworkUtility = new AndroidNetworkUtility();
        if (androidNetworkUtility.isConnected(this)) {
            new com.steelkiwi.startweets.async.TwitterAsyncTask().execute(twitterScreenName, this);
        } else {
            Toast.makeText(this, "No connection", Toast.LENGTH_LONG).show();
            Log.v(TAG, "Network not Available!");
        }
    }
}