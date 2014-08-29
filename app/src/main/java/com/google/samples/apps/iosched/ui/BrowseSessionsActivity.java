package com.google.samples.apps.iosched.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.samples.apps.iosched.R;


public class BrowseSessionsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_sessions);
    }


    @Override
    public void onPlusInfoLoaded(String accountName) {

    }

    @Override
    public void onAuthSuccess(String accountName, boolean newlyAuthenticated) {

    }

    @Override
    public void onAuthFailure(String accountName) {

    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
