package com.esgi.jabin.twitterapi;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.TweetTimelineRecyclerViewAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TweetsListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private String query;

    @BindView(R.id.recycler_view) RecyclerView tweetsRecyclerView;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.floating_action_button) FloatingActionButton floatingActionButton;

    @OnClick(R.id.floating_action_button)
    void openInBrowser() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.search_query, query)));
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweets_list);
        ButterKnife.bind(this);

        tweetsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        searchView.requestFocus();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logOut() {
        TwitterCore.getInstance().getSessionManager().clearActiveSession();
        showLoginActivity();
    }

    private void showLoginActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchTweets(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return false;
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) {
            return;
        }

        View currentFocus = getCurrentFocus();
        if (currentFocus == null) {
            return;
        }

        inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    private void searchTweets(String query) {
        hideSoftKeyboard();
        this.query = query;

        if (TextUtils.isEmpty(query)) {
            return;
        }

        floatingActionButton.setVisibility(View.VISIBLE);

        final SearchTimeline searchTimeline = new SearchTimeline.Builder()
                .query(query)
                .maxItemsPerRequest(50)
                .build();

        final TweetTimelineRecyclerViewAdapter adapter = new TweetTimelineRecyclerViewAdapter.Builder(this)
                .setTimeline(searchTimeline)
                .setViewStyle(R.style.tw__TweetLightWithActionsStyle)
                .build();

        tweetsRecyclerView.setAdapter(adapter);


        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            adapter.refresh(new Callback<TimelineResult<Tweet>>() {
                @Override
                public void success(Result<TimelineResult<Tweet>> result) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void failure(TwitterException exception) {
                    swipeRefreshLayout.setRefreshing(false);
                    exception.printStackTrace();
                }
            });
        });



    }
}