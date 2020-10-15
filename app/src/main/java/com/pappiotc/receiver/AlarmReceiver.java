package com.pappiotc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pappiotc.helper.Util;
import com.pappiotc.service.LocationService;
import com.pappiotc.service.RefreshService;

import java.text.ParseException;

public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final int REQUEST_CODE2 = 123456;

    /**
     * onReceive: Triggered by the Alarm periodically (starts the service to run task)
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra("refresh", false)) {
            Intent i = new Intent(context, RefreshService.class);
            context.startService(i);
        } else {
            Boolean between = false;
            try {
                between = Util.isTimeBetweenTwoTime("06:00:00", "23:59:59");
                Log.e("Betweeeen", "" + between);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (between) {
                Intent i = new Intent(context, LocationService.class);
                context.startService(i);
            }
        }
    }
}
