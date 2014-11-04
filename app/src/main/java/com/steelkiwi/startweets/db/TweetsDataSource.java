package com.steelkiwi.startweets.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.steelkiwi.startweets.twitter.TwitterTweet;
import com.steelkiwi.startweets.twitter.TwitterUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by lester on 04.11.14.
 */
public class TweetsDataSource {
    private SQLiteDatabase database;
    private TweetSQLiteHelper dbHelper;
    private String[] allColumns = {TweetSQLiteHelper.COLUMN_ID,
            TweetSQLiteHelper.COLUMN_AUTHOR, TweetSQLiteHelper.COLUMN_DATE, TweetSQLiteHelper.COLUMN_TWEET_TEXT};

    public TweetsDataSource(Context context) {
        dbHelper = new TweetSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void insertTweet(TwitterTweet tweet) {
        ContentValues values = new ContentValues();
        values.put(TweetSQLiteHelper.COLUMN_ID, tweet.getId());
        values.put(TweetSQLiteHelper.COLUMN_TIMESTAMP, getTwitterDate(tweet.getCreatedAt()));
        values.put(TweetSQLiteHelper.COLUMN_DATE, tweet.getCreatedAt());
        values.put(TweetSQLiteHelper.COLUMN_AUTHOR, tweet.getTwitterUser().toString());
        values.put(TweetSQLiteHelper.COLUMN_TWEET_TEXT, tweet.getText());
        database.insert(TweetSQLiteHelper.TABLE_TWEETS, null,
                values);
    }

    public void deleteAllTweets(String auhtor) {
        database.delete(TweetSQLiteHelper.TABLE_TWEETS, TweetSQLiteHelper.COLUMN_AUTHOR
                + " = ?", new String[]{auhtor});
    }

    public void deleteTweet(long tweet_id) {
        database.delete(TweetSQLiteHelper.TABLE_TWEETS, TweetSQLiteHelper.COLUMN_ID
                + " = ?", new String[]{String.valueOf(tweet_id)});
    }

    public List<TwitterTweet> getAllTweetsByAuthor(final String author) {
        List<TwitterTweet> tweets = new ArrayList<TwitterTweet>();

        Cursor cursor = database.query(TweetSQLiteHelper.TABLE_TWEETS,
                allColumns, TweetSQLiteHelper.COLUMN_AUTHOR + " = ?", new String[]{
                        author
                }, null, null, TweetSQLiteHelper.COLUMN_DATE+" DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TwitterTweet tweet = cursorToTweet(cursor);
            tweets.add(tweet);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return tweets;
    }
    private long getTwitterDate(String date)  {

        final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH);
        sf.setLenient(true);
        Date dateObj = null;
        try {
            dateObj = sf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateObj.getTime();
    }
    private TwitterTweet cursorToTweet(Cursor cursor) {
        TwitterTweet tweet = new TwitterTweet();
        tweet.setId(cursor.getLong(cursor.getColumnIndex(TweetSQLiteHelper.COLUMN_ID)));
        TwitterUser user = new TwitterUser();
        user.setScreenName(cursor.getString(cursor.getColumnIndex(TweetSQLiteHelper.COLUMN_AUTHOR)));
        tweet.setTwitterUser(user);
        tweet.setCreatedAt(cursor.getString(cursor.getColumnIndex(TweetSQLiteHelper.COLUMN_DATE)));
        tweet.setText(cursor.getString(cursor.getColumnIndex(TweetSQLiteHelper.COLUMN_TWEET_TEXT)));
        return tweet;
    }
}
