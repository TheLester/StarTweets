package com.steelkiwi.startweets.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.steelkiwi.startweets.R;
import com.steelkiwi.startweets.adapter.TweetsAdapter;
import com.steelkiwi.startweets.async.TwitterAsyncTask;
import com.steelkiwi.startweets.db.TweetsDataSource;
import com.steelkiwi.startweets.util.AndroidNetworkUtility;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


public class MainActivity extends Activity implements OnRefreshListener {
    private final static String TAG = "MainActivity";
    private Menu menu;
    private static String twitterScreenName = "BBCNews";
    private AndroidNetworkUtility androidNetworkUtility;
    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter_tweets_list);
        androidNetworkUtility = new AndroidNetworkUtility();
        if (androidNetworkUtility.isConnected(this)) {
            new com.steelkiwi.startweets.async.TwitterAsyncTask().execute(twitterScreenName, this);
        } else {
            Log.v(TAG, "Network not Available!");
            TweetsDataSource dataSource = new TweetsDataSource(this);
            dataSource.open();
            TweetsAdapter adapter =
                    new TweetsAdapter(this, R.layout.twitter_tweets_list, dataSource.getAllTweetsByAuthor(twitterScreenName));
            final ListView listview = (ListView) findViewById(R.id.tweets_cont);
            listview.setAdapter(adapter);
            dataSource.close();
        }
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(MainActivity.this)
                .allChildrenArePullable()
                .listener(MainActivity.this)
                .setup(mPullToRefreshLayout);
    }

    @Override
    public void onRefreshStarted(View view) {
        if (androidNetworkUtility.isConnected(this)) {
            TwitterAsyncTask twitterAsyncTask = new TwitterAsyncTask();
            twitterAsyncTask.execute(twitterScreenName, this);
        } else Toast.makeText(this, "No connection", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        this.menu = menu;
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (androidNetworkUtility.isConnected(this)) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    ImageView iv = (ImageView) inflater.inflate(R.layout.iv_refresh, null);
                    Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
                    rotation.setRepeatCount(Animation.INFINITE);
                    iv.startAnimation(rotation);
                    item.setActionView(iv);
                    TwitterAsyncTask twitterAsyncTask = new TwitterAsyncTask();
                    twitterAsyncTask.execute(twitterScreenName, this);
                } else Toast.makeText(this, "No connection", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void resetUpdating() {
        // Get our refresh item from the menu
        MenuItem m = menu.findItem(R.id.action_refresh);
        if (m.getActionView() != null) {
            // Remove the animation.
            m.getActionView().clearAnimation();
            m.setActionView(null);

        }
        if (mPullToRefreshLayout != null) mPullToRefreshLayout.setRefreshComplete();
        Toast.makeText(this, "Done updating", Toast.LENGTH_LONG).show();
    }

}