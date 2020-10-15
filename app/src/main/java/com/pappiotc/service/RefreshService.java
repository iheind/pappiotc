package com.pappiotc.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.pappiotc.App;
import com.pappiotc.controller.SalesTrackerController;

public class RefreshService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    SalesTrackerController salesTrackerController;
    SharedPreferences sharedPreferences;

    public RefreshService() {
        super("RefreshService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        salesTrackerController = new SalesTrackerController(getApplicationContext());
        sharedPreferences = App.get().getSharedPreferences();
        Log.e("Background", "Refresh.onHandleIntent");
        salesTrackerController.callRefreshBgService(getApplicationContext(), sharedPreferences.getString("token", ""));
    }
}
