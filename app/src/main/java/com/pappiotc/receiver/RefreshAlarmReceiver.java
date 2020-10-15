package com.pappiotc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pappiotc.service.RefreshService;

public class RefreshAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 123456;

    /**
     * onReceive: Triggered by the Alarm periodically (starts the service to run task)
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, RefreshService.class);
        context.startService(i);

    }
}
