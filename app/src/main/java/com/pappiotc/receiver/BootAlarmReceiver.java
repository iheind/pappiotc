package com.pappiotc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pappiotc.App;
import com.pappiotc.service.StartAlarmsService;


public class BootAlarmReceiver extends BroadcastReceiver {

    /**
     * onReceive: Triggered by the Alarm periodically (starts the service to run task)
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!App.get().getSharedPreferences().getBoolean("firstLaunch", true)) {
            Intent intent1 = new Intent(context.getApplicationContext(), StartAlarmsService.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(intent1);
        }
    }
}

