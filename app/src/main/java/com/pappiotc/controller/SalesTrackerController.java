package com.pappiotc.controller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pappiotc.App;
import com.pappiotc.R;
import com.pappiotc.activities.SalesTrackerActivity;
import com.pappiotc.helper.Constants;
import com.pappiotc.helper.HttpUtils;
import com.pappiotc.helper.Util;
import com.pappiotc.model.CheckIn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SalesTrackerController {
    Context context;
    public static ProgressDialog loading;

    public SalesTrackerController(Context context) {
        this.context = context;
    }

    /**
     * callFCMService: Register the device to FCM
     *
     * @param regId
     */
    public void callFCMService(String regId) {
        RequestParams rp = new RequestParams();
        rp.add("token", Constants.token);
        rp.add("fcm_regid", regId);

        HttpUtils.post(context, "fcm", rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                // If the response is JSONObject instead of expected JSONArray
                Log.d("FCM", "this is response : " + response);
                try {
                    Boolean error = response.getBoolean("error");
                    String errorMessage = response.getString("error_message");
                    JSONObject data = response.getJSONObject("data");
                    loading.dismiss();
                    if (error) {
                        // show the error message
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                    } else {
                        // Open the main app screen for checkin and log view
                        Intent salesTrackerIntent = new Intent(context, SalesTrackerActivity.class);
                        salesTrackerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        salesTrackerIntent.putExtra("fromLogin", true);
                        context.startActivity(salesTrackerIntent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                loading.dismiss();
                Util.requestFailed(context);

            }
        });
    }

    /**
     * callLoginService: Login the user using the provided credentials: username, password
     *
     * @param userName
     * @param password
     */
    public void callLoginService(String userName, String password, String imei) {
        loading = ProgressDialog.show(context, "Logging in", "Please wait...");
        RequestParams rp = new RequestParams();
        rp.add("username", userName);
        rp.add("password", password);
        rp.add("imei", /* imei "490154203237518"imei */ "357513066225419" );

        HttpUtils.post(context, "login", rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("Login", "this is response : " + response);
                try {
                    Boolean error = response.getBoolean("error");
                    String errorMessage = response.getString("error_message");
                    if (error) {
                        // show the error message
                        loading.dismiss();
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                    } else {
                        if (!response.isNull("data")) {
                            JSONObject data = response.getJSONObject("data");
                            // Make it auto login next time and save all data
                            SharedPreferences.Editor editor = App.get().getSharedPreferences().edit();
                            editor.putBoolean("firstLaunch", false);
                            editor.apply();
                            refreshData(data, false);

                            // Setup Firebase
                            String t = FirebaseInstanceId.getInstance().getToken();
                            if (t != null) {
                                callFCMService(t);
                            }
                            Log.e("ApplicationTag", "TOKEN " + t);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                loading.dismiss();
                Util.requestFailed(context);
            }
        });
    }

    /**
     * callResetPasswordService: Reset the password of the user by sending new password to his email
     *
     * @param email
     */
    public void callResetPasswordService(String email) {

        RequestParams rp = new RequestParams();
        rp.add("email", email);

        HttpUtils.post(context, "forgot", rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("ReserPassword", "this is response : " + response);
                try {
                    Boolean error = response.getBoolean("error");
                    String errorMessage = response.getString("error_message");
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Util.requestFailed(context);

            }
        });
    }

    /**
     * callLogoutService: Logout the user given his token
     *
     * @param token
     */
    public void callLogoutService(String token) {

        RequestParams rp = new RequestParams();
        rp.add("token", token);

        HttpUtils.postSync(context, "logout-callback", rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("token", "this is response : " + response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Util.requestFailed(context);

            }
        });
    }

    /**
     * callCheckinService: Check in with a client at certain location\
     *
     * @param type
     * @param clientId
     * @param token
     * @param latitude
     * @param longitude
     */
    public void callCheckinService(String type, String clientId, String token, String latitude, String longitude) {
        loading = ProgressDialog.show(context, "Check in", "Please wait...");
        RequestParams rp = new RequestParams();
        rp.add("token", token);
        rp.add("type", type);
        rp.add("clientId", clientId);
        rp.add("latitude", latitude);
        rp.add("longitude", longitude);

        HttpUtils.post(context, "checkin", rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {
                    loading.dismiss();
                    if (!response.getBoolean("error")) {
                        Toast.makeText(context, context.getString(R.string.data_saved), Toast.LENGTH_LONG).show();
                        // update the data
                        if (!response.isNull("data"))
                            refreshData(response.getJSONObject("data"), true);
                    } else {
                        if (response.isNull("error_message"))
                            Toast.makeText(context, context.getString(R.string.data_error_checkin), Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(context, response.getInt("error_message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Util.requestFailed(context);
            }
        });
    }

    /**
     * callLocationService: update the gps location
     *
     * @param token
     * @param latitude
     * @param longitude
     */
    public void callLocationService(String token, String latitude, String longitude) {
        RequestParams rp = new RequestParams();
        rp.add("token", token);
        rp.add("latitude", latitude);
        rp.add("longitude", longitude);
        Log.e("Location", "Send");
        HttpUtils.post(context, "location", rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
//                Toast.makeText(context, "Done", Toast.LENGTH_LONG).show();
                try {
                    Log.e("SendingLocation", response.getBoolean("error") + "");
//                    Toast.makeText(context, "Error: " + (!response.getBoolean("error") ? "false" : response.getString("error_message")), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                loading.dismiss();
                Util.requestFailed(context);
            }
        });
    }

    /**
     * callUpdateCheckinService: update Checkin form (Log)
     *
     * @param token
     * @param id
     * @param note
     */
    public void callUpdateCheckinService(String token, String id, String note) {
        RequestParams rp = new RequestParams();
        rp.add("token", token);
        rp.add("id", id);
        rp.add("note", note);

        HttpUtils.post(context, "checkin-update", rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {
                    if (!response.getBoolean("error")) {
                        Toast.makeText(context, context.getString(R.string.data_saved), Toast.LENGTH_LONG).show();
                        // update the data
                        if (!response.isNull("data"))
                            refreshData(response.getJSONObject("data"), true);
                    } else {
                        if (response.isNull("error_message"))
                            Toast.makeText(context, context.getString(R.string.data_error), Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(context, response.getInt("error_message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Util.requestFailed(context);

            }
        });
    }

    /**
     * callRefreshService: refresh the data
     *
     * @param token
     */
    public void callRefreshService(final Context context, String token) {
        if (HttpUtils.isNetworkAvailable(this.context)) {
            loading = ProgressDialog.show(context, "Refreshing", "Please wait...");
            RequestParams rp = new RequestParams();
            rp.add("token", token);

            HttpUtils.post(context, "me", rp, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    // If the response is JSONObject instead of expected JSONArray
                    Log.d("Refresh", "this is response : " + response);
                    try {
                        Boolean error = response.getBoolean("error");
                        String errorMessage = response.getString("error_message");
                        if (error) {
                            // show the error message
                            ((SalesTrackerActivity) context).resetUpdating();
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                        } else {
                            if (!response.isNull("data")) {
                                JSONObject data = response.getJSONObject("data");
                                // Make it auto login next time and save all data
                                refreshData(data, true);

                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    loading.dismiss();
                    ((SalesTrackerActivity) context).resetUpdating();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    ((SalesTrackerActivity) context).resetUpdating();
                    loading.dismiss();
                    Util.requestFailed(context);

                }
            });
        } else
            ((SalesTrackerActivity) context).resetUpdating();
    }

    /**
     * callRefreshBgService: refresh the data
     *
     * @param token
     */
    public void callRefreshBgService(final Context context, String token) {
        RequestParams rp = new RequestParams();
        rp.add("token", token);

        HttpUtils.postSync(context, "me", rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("Refresh", "this is response : " + response);
                try {
                    Boolean error = response.getBoolean("error");
                    String errorMessage = response.getString("error_message");
                    if (!error) {
                        if (!response.isNull("data")) {
                            JSONObject data = response.getJSONObject("data");
                            // Make it auto login next time and save all data
                            refreshData(data, false);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Util.requestFailed(context);
            }
        });
    }

    public void refreshData(JSONObject data, boolean notLogin) {
        try {
            SharedPreferences.Editor editor = App.get().getSharedPreferences().edit();
            Gson gson = new Gson();
            JSONObject jsonProfile = data.getJSONObject("jsonProfile");
            if (jsonProfile != null) {
                if (!(jsonProfile == null || jsonProfile.equals(""))) {
                    Constants.userName = jsonProfile.getString("username");
                    Constants.token = jsonProfile.getString("token");
                    editor.putString("token", Constants.token);
                    editor.putString("userName", Constants.userName);
                }
                JSONArray jsonDoctors = data.getJSONArray("jsonDoctors");
                if (!(jsonDoctors == null || jsonDoctors.equals(""))) {
                    Constants.doctorsArrayList = Util.jsonArrayToDoctorsList(jsonDoctors);
                    editor.putString("doctors", jsonDoctors.toString());

                }
                JSONArray jsonPharmacies = data.getJSONArray("jsonPharmacies");
                if (!(jsonPharmacies == null || jsonPharmacies.equals(""))) {
                    Constants.pharmaciesArrayList = Util.jsonArrayToPharmaciesList(jsonPharmacies);
                    editor.putString("pharmacies", jsonPharmacies.toString());
                }
                if (notLogin)
                    ((SalesTrackerActivity) context).checkInFragment.refresh();

                JSONArray jsonLogs = null;
                jsonLogs = data.getJSONArray("jsonLogs");

                if (!(jsonLogs == null || jsonLogs.equals(""))) {
                    ArrayList<CheckIn> logsArrayList = Util.jsonArrayToLogsList(jsonLogs);
                    Constants.setLogs(logsArrayList);
                    editor.putString("logs", new Gson().toJson(logsArrayList));
                }
                if (notLogin)
                    ((SalesTrackerActivity) context).logFragment.refresh();
            }
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
