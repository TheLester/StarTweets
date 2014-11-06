package com.steelkiwi.startweets.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Photo;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnPublishListener;
import com.steelkiwi.startweets.R;
import com.steelkiwi.startweets.adapter.TweetsAdapter;
import com.steelkiwi.startweets.async.TwitterAsyncTask;
import com.steelkiwi.startweets.db.TweetsDataSource;
import com.steelkiwi.startweets.util.AndroidNetworkUtility;

import java.io.IOException;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


public class MainActivity extends Activity implements OnRefreshListener {
    private final static String TAG = "MainActivity";
    public static final String TWITTER_SH_PREF_KEY = "twitterStarName";
    private static final int PICK_IMAGE = 1;

    private SharedPreferences prefs;
    private Menu menu;
    private String twitterScreenName;
    private AndroidNetworkUtility androidNetworkUtility;
    private PullToRefreshLayout mPullToRefreshLayout;

    private SimpleFacebook mSimpleFacebook;
    private String tweetText;
    private Permission[] permissions = new Permission[]{
            Permission.USER_PHOTOS,
            Permission.EMAIL,
            Permission.PUBLISH_ACTION
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter_tweets_list);
        prefs = this.getSharedPreferences(
                "com.steelkiwi.startweets", Context.MODE_PRIVATE);
        twitterScreenName = prefs.getString(TWITTER_SH_PREF_KEY, "ladygaga");

        mSimpleFacebook = SimpleFacebook.getInstance(this);
        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId(getResources().getString(R.string.facebook_app_id))
                .setNamespace("dogardmitri")
                .setPermissions(permissions)
                .build();
        SimpleFacebook.setConfiguration(configuration);
        mSimpleFacebook.login(onLoginListener);
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
    protected void onResume() {
        super.onResume();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
        mSimpleFacebook = SimpleFacebook.getInstance(this);
        twitterScreenName = prefs.getString(TWITTER_SH_PREF_KEY, "ladygaga");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onRefreshStarted(View view) {
        if (androidNetworkUtility.isConnected(this)) {
            TwitterAsyncTask twitterAsyncTask = new TwitterAsyncTask();
            twitterAsyncTask.execute(twitterScreenName, this);
        } else {
            Toast.makeText(this, "No connection", Toast.LENGTH_LONG).show();
            resetUpdating();
        }
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
            case R.id.action_change_name:
                Intent intent = new Intent(this, SetTwitterActivity.class);
                startActivity(intent);
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

    public void shareOnClickHandler(View v) {
        RelativeLayout vwParentRow = (RelativeLayout) v.getParent().getParent();
        tweetText = ((TextView) vwParentRow.getChildAt(0)).getText().toString();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select picture to share"), PICK_IMAGE);
    }

    private OnLoginListener onLoginListener = new OnLoginListener() {
        @Override
        public void onLogin() {
            // change the state of the button or do whatever you want
            Log.i(TAG, "Logged in");
        }

        @Override
        public void onNotAcceptingPermissions(Permission.Type type) {
            // user didn't accept READ or WRITE permission
            Log.w(TAG, String.format("You didn't accept %s permissions", type.name()));
        }

        @Override
        public void onThinking() {

        }

        @Override
        public void onException(Throwable throwable) {

        }

        @Override
        public void onFail(String s) {

        }
    };
    private OnPublishListener onPublishListener = new OnPublishListener() {
        @Override
        public void onComplete(String id) {
            Log.i(TAG, "Published successfully. id = " + id);
        }

        @Override
        public void onFail(String reason) {
            super.onFail(reason);
            Log.i(TAG, "fail " + reason);
        }

        @Override
        public void onException(Throwable throwable) {
            super.onException(throwable);
            Log.i(TAG, throwable.toString());
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && data != null && data.getData() != null) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    Photo photo = new Photo.Builder()
                            .setImage(bitmap)
                            .setName(tweetText)
                            .build();

                    mSimpleFacebook.publish(photo, false, onPublishListener);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}