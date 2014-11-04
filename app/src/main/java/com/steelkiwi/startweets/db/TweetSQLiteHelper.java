package com.steelkiwi.startweets.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by lester on 04.11.14.
 */
public class TweetSQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_TWEETS = "tweets";
    public static final String COLUMN_ID = "tweet_id";
    public static final String COLUMN_AUTHOR = "screenname";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_DATE = "text_date";
    public static final String COLUMN_TWEET_TEXT = "text";


    private static final String DATABASE_NAME = "tweets_reader.db";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE = "create table "
            + TABLE_TWEETS + "(" + COLUMN_ID
            + " integer primary key , " + COLUMN_AUTHOR
            + " text not null, " + COLUMN_TIMESTAMP + " integer, " + COLUMN_DATE + " text, " + COLUMN_TWEET_TEXT + " text );";

    public TweetSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TweetSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TWEETS);
        onCreate(db);
    }
}
