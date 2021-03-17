package com.pappiotc.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.pappiotc.R;
import com.pappiotc.controller.SalesTrackerController;

public class HttpUtils {
    private static final String BASE_URL = "https://pappi.puspapharma.com/puspa/web/api/";
    private static AsyncHttpClient client = new AsyncHttpClient();
    private static SyncHttpClient syncClient = new SyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        if (isNetworkAvailable(context))
            client.post(getAbsoluteUrl(url), params, responseHandler);
        else {
            if (SalesTrackerController.loading.isShowing())
                SalesTrackerController.loading.dismiss();
        }

    }

    public static void postSync(Context context, String url, RequestParams params, JsonHttpResponseHandler responseHandler) {
        if (Util.isNetworkAvailable(context))
            syncClient.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void getByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static void postByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(url, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
            return true;
        else {
            Toast.makeText(context, R.string.no_connection_error_msg, Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
