package com.pappiotc.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.pappiotc.R;
import com.pappiotc.adapters.ViewPagerAdapter;
import com.pappiotc.controller.SalesTrackerController;
import com.pappiotc.fragments.CheckInFragment;
import com.pappiotc.fragments.LogFragment;
import com.pappiotc.helper.Constants;
import com.pappiotc.service.StartAlarmsService;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SalesTrackerActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        ResultCallback<LocationSettingsResult> {

//    private static final int PERIOD = 15000; // 15 seconds
//    private static final int INITIAL_DELAY = 1000; // 1 second

    @InjectView(R.id.sales_tracker_tablayout)
    TabLayout tabLayout;

    @InjectView(R.id.sales_tracker_viewpager)
    public ViewPager viewPager;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    private final int PERMISSION_ACCESS_COARSE_LOCATION = 10;

    MenuItem searchItem;
    MenuItem refreshButton;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    protected static final String TAG = "GettingUserLocation";
    public static Location userLocation;
    private Boolean called = false;

    public SalesTrackerController salesTrackerController;
    public CheckInFragment checkInFragment;
    public LogFragment logFragment;
    private Boolean fromLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_tracker);
        ButterKnife.inject(this);
        setupViewPager(viewPager);
        fromLogin = getIntent().getBooleanExtra("fromLogin", false);
//        if (fromLogin)
        Intent serviceIntent = new Intent(getApplicationContext(), StartAlarmsService.class);
//        serviceIntent.setAction("com.mobilesalestracker.service.StartAlarmsService");
        startService(serviceIntent);
//            Util.scheduleAlarm(getApplicationContext());
        salesTrackerController = new SalesTrackerController(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        } else {
            callLocationSettings();
        }

        tabLayout.setupWithViewPager(viewPager);
//        Toast.makeText(this, RESTClient.doctorsArrayList.size(), Toast.LENGTH_LONG).show();
        // Set the tab layout with custom view to show separator
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            RelativeLayout relativeLayout = (RelativeLayout)
                    LayoutInflater.from(this).inflate(R.layout.tab_layout, tabLayout, false);
            TextView tabTextView = (TextView) relativeLayout.findViewById(R.id.tab_title);
            tabTextView.setTypeface(null, Typeface.BOLD);
            tabTextView.setText(tab.getText());
            View v = relativeLayout.findViewById(R.id.vertical_view);

            // Remove last separator view to have only one in middle
            if (i == 1)
                v.setVisibility(View.GONE);
            tab.setCustomView(relativeLayout);
            tab.select();
        }
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.login_app_name));
        viewPager.setCurrentItem(0);
    }

    /**
     * callLocationSettings: Location settings request
     */
    private void callLocationSettings() {
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        checkLocationSettings();
    }

    /**
     * setupViewPager: Adding the two tab fragments: Home, Log View
     *
     * @param viewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        checkInFragment = new CheckInFragment();
        logFragment = new LogFragment();
        adapter.addFragment(checkInFragment, getString(R.string.home));
        adapter.addFragment(logFragment, getString(R.string.log_view));
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                    callLocationSettings();
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Get the refresh button to animate it
        refreshButton = (MenuItem) menu.getItem(1);
        searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);
        if (viewPager.getCurrentItem() == 0)
            searchItem.setVisible(false);
        else
            searchItem.setVisible(true);
        return true;
    }


    /**
     * onOptionsItemSelected: Handle action bar item clicks
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            // Start async task to animate the refresh icon
            refreshAnimation();
            salesTrackerController.callRefreshService(this, Constants.token);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * refreshAnimation: Animate the refresh button
     */
    public void refreshAnimation() {
        // Attach a rotating ImageView to the refresh item as an ActionView
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.clockwise_refresh);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);
        refreshButton.setActionView(iv);
    }

    /**
     * resetUpdating: is used to stop animating the refresh button
     */
    public void resetUpdating() {
        // Get our refresh item from the menu
        if (refreshButton.getActionView() != null) {
            // Remove the animation.
            refreshButton.getActionView().clearAnimation();
            refreshButton.setActionView(null);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        LogFragment.logViewListAdapter.getFilter().filter(newText);
        return false;
    }

    /**
     * buildGoogleApiClient: Initialize Google API client
     */
    protected void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.setAlwaysShow(true);
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        showCanNotCompleteDialog();
                        break;
                }
                break;
        }
    }

    private void showCanNotCompleteDialog() {
        new android.support.v7.app.AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle(getString(R.string.loc_settings_error))
                .setMessage(getString(R.string.loc_settings_refuse_error))
                .setIcon(R.drawable.ic_launcher)
                .setCancelable(false)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
//                        createLocationRequest();
//                        buildLocationSettingsRequest();
                        checkLocationSettings();
                        dialog.dismiss();
                    }
                }).show();
    }

    private void showEnableNetworkDialog() {
        new android.support.v7.app.AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle(getString(R.string.conn_failed))
                .setMessage(getString(R.string.please_connect))
                .setIcon(R.drawable.ic_launcher)
                .setCancelable(false)
                .setPositiveButton("ok", null)
                .show();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (userLocation == null) {
            userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//            if (userLocation != null && fromLogin)
//                if (!called) {
//                    if (!Util.isNetworkAvailable(this)) {
//                        showEnableNetworkDialog();
//                        new UpdateTask(getApplicationContext()).execute();
//                    } else
//                        salesTrackerController.callLocationService(Constants.token, userLocation.getLatitude() + "", userLocation.getLongitude() + "");
//                    called = true;
//                } else
//                    Toast.makeText(this, getString(R.string.enable_location_services), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(SalesTrackerActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }


    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        );

    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        if (mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient,
                    this
            );
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, getString(R.string.conn_failed), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        userLocation = location;
       /* if (!called && userLocation != null && Util.isNetworkAvailable(this) && fromLogin) {
            salesTrackerController.callLocationService(Constants.token, userLocation.getLatitude() + "", userLocation.getLongitude() + "");
            called = true;
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();
//        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }
}
