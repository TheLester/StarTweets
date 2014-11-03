package com.steelkiwi.startweets.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.steelkiwi.startweets.R;
import com.steelkiwi.startweets.twitter.TwitterTweet;

import java.util.List;

/**
 * Created by lester on 03.11.14.
 */
public class TweetsAdapter extends ArrayAdapter<TwitterTweet> {
    private List<TwitterTweet> tweets;
    private Context context;
    public TweetsAdapter(Context context, int resource, List<TwitterTweet> tweets) {
        super(context, resource, tweets);
        this.tweets = tweets;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.twitter_tweets_list, null);
        }
        TwitterTweet tweet = tweets.get(position);
        if (tweet != null) {
            TextView message = (TextView) v.findViewById(R.id.tweetTextView);
            TextView date = (TextView) v.findViewById(R.id.dateTextView);

            if (date != null) {
                date.setText(tweet.getCreatedAt());
            }

            if (message != null) {
                message.setText(tweet.getText());
            }
        }
        return v;
    }

}
