package com.pappiotc.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.pappiotc.helper.Util;

/**
 * Created by AAshour on 8/15/2016.
 */

public class StartAlarmsService extends Service {

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(final Intent pIntent, int flags, int startId) {
        Util.scheduleAlarm(StartAlarmsService.this.getApplicationContext());
        return super.onStartCommand(pIntent, flags, startId);
    }
}
