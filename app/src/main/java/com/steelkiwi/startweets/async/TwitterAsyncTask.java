package com.steelkiwi.startweets.async;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.widget.ListView;

import com.steelkiwi.startweets.R;
import com.steelkiwi.startweets.activity.MainActivity;
import com.steelkiwi.startweets.adapter.TweetsAdapter;
import com.steelkiwi.startweets.db.TweetsDataSource;
import com.steelkiwi.startweets.twitter.TwitterAPI;
import com.steelkiwi.startweets.twitter.TwitterTweet;

import java.util.ArrayList;

public class TwitterAsyncTask extends AsyncTask<Object, Void, ArrayList<TwitterTweet>> {
    private ListActivity callerActivity;
    private String author;
    final static String TWITTER_API_KEY = "gyhX4bh5wDMC7xpkho5cMIgup";
    final static String TWITTER_API_SECRET = "U7XDXQhjQXQmI6EREkabtLzZZHFDT8N6Qv0ZKN5UKHlJCBdBIN";

    @Override
    protected ArrayList<TwitterTweet> doInBackground(Object... params) {
        ArrayList<TwitterTweet> twitterTweets = null;
        callerActivity = (ListActivity) params[1];
        author = params[0].toString();
        if (params.length > 0) {
            TwitterAPI twitterAPI = new TwitterAPI(TWITTER_API_KEY, TWITTER_API_SECRET);
            twitterTweets = twitterAPI.getTwitterTweets(author);
        }
        return twitterTweets;
    }

    @Override
    protected void onPostExecute(ArrayList<TwitterTweet> twitterTweets) {
        TweetsDataSource dataSource = new TweetsDataSource(callerActivity);
        dataSource.open();
        //insert fresh tweets
        if (!twitterTweets.equals(dataSource.getAllTweetsByAuthor(author))) {
            dataSource.deleteAllTweets(author);
            for (TwitterTweet tweet : twitterTweets)
                dataSource.insertTweet(tweet);
        }

        TweetsAdapter adapter =
                new TweetsAdapter(callerActivity, R.layout.twitter_tweets_list, dataSource.getAllTweetsByAuthor(author));
        callerActivity.setListAdapter(adapter);
        ListView lv = callerActivity.getListView();
        lv.setDividerHeight(0);
        dataSource.close();
        ((MainActivity) callerActivity).resetUpdating();
    }


}
