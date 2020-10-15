package com.pappiotc.helper;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.pappiotc.R;
import com.pappiotc.model.CheckIn;
import com.pappiotc.model.Client;
import com.pappiotc.receiver.AlarmReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

public class Util {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
            return true;
        else {
//            Toast.makeText(context, R.string.no_connection_error_msg, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static void requestFailed(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.error_title)
                .setMessage(context.getResources().getString(R.string.error_title))
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        }
                )
                .show();
    }


    public static ArrayList<CheckIn> jsonArrayToLogsList(JSONArray jsonLogs) {
        ArrayList<CheckIn> logs = new ArrayList<CheckIn>();
        for (int i = 0; i < jsonLogs.length(); i++) {
            CheckIn checkIn = new CheckIn();
            try {
                JSONObject logJsonObject = (JSONObject) jsonLogs.get(i);
                checkIn.setId(logJsonObject.optInt("id"));
                checkIn.setClientId(logJsonObject.optInt("clientId"));
                checkIn.setClientName(logJsonObject.optString("clientName"));
                checkIn.setType(logJsonObject.optString("type"));
                String note = logJsonObject.optString("note");
                checkIn.setCreateDate(logJsonObject.optLong("createDate"));
                if (note.equalsIgnoreCase("null"))
                    note = "";
                checkIn.setNote(note);
                logs.add(checkIn);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return logs;
    }

    public static ArrayList<Client> jsonArrayToPharmaciesList(JSONArray jsonPharmacies) {
        ArrayList<Client> pharmacies = new ArrayList<Client>();
        for (int i = 0; i < jsonPharmacies.length(); i++) {
            Client pharmacy = new Client();
            try {
                pharmacy.setId(((JSONObject) jsonPharmacies.get(i)).optString("id"));
                pharmacy.setTitle(((JSONObject) jsonPharmacies.get(i)).optString("title"));
//                pharmacy.setType("2");
                pharmacies.add(pharmacy);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return pharmacies;
    }

    public static ArrayList<Client> jsonArrayToDoctorsList(JSONArray jsonDoctors) {
        ArrayList<Client> doctors = new ArrayList<Client>();
        for (int i = 0; i < jsonDoctors.length(); i++) {
            Client doctor = new Client();
            try {
                doctor.setId(((JSONObject) jsonDoctors.get(i)).optString("id"));
                doctor.setTitle(((JSONObject) jsonDoctors.get(i)).optString("title"));
//                doctor.setType("1");
                doctors.add(doctor);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return doctors;
    }

    /**
     * scheduleAlarm: Setup a recurring alarm every 15 mins + the daily refresh alarm
     */

    public static void scheduleAlarm(Context context) {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("refresh", false);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        // Construct an intent that will execute the AlarmReceiver
        Intent intent2 = new Intent(context, AlarmReceiver.class);
        intent2.putExtra("refresh", true);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, AlarmReceiver.REQUEST_CODE2,
                intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar firingCal = Calendar.getInstance();
        Calendar currentCal = Calendar.getInstance();
        int minute = currentCal.get(Calendar.MINUTE);
        int hour = currentCal.get(currentCal.HOUR_OF_DAY);
        if (minute >= 45) {
            currentCal.set(Calendar.MINUTE, 00);
            if (hour == 0) {
                currentCal.add(Calendar.DAY_OF_MONTH, 1);
                currentCal.set(Calendar.HOUR_OF_DAY, 01);
            } else if (hour == 23) {
                currentCal.set(Calendar.HOUR_OF_DAY, 00);
            } else {
                currentCal.add(Calendar.HOUR_OF_DAY, 1);
            }
        } else if (minute < 15)
            currentCal.set(Calendar.MINUTE, 15);
        else if (minute < 30)
            currentCal.set(Calendar.MINUTE, 30);
        else if (minute < 45)
            currentCal.set(Calendar.MINUTE, 45);
        currentCal.set(Calendar.SECOND, 00);
        Log.e("CallAlarm", currentCal.get(Calendar.HOUR_OF_DAY) + " " + currentCal.get(Calendar.MINUTE) + " " + currentCal.get(Calendar.SECOND));
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, currentCal.getTimeInMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);

        firingCal.set(Calendar.HOUR_OF_DAY, 06); // At the hour you wanna fire
        firingCal.set(Calendar.MINUTE, 00); // Particular minute
        firingCal.set(Calendar.SECOND, 00); // particular second

        long intendedTime = firingCal.getTimeInMillis();
        long currentTime = Calendar.getInstance().getTimeInMillis();

        if (intendedTime < currentTime) {
            // set from next day
            // you might consider using calendar.add() for adding one day to the current day
            firingCal.add(Calendar.DAY_OF_MONTH, 1);
            intendedTime = firingCal.getTimeInMillis();
        }
        alarmManager.setRepeating(AlarmManager.RTC, intendedTime, AlarmManager.INTERVAL_DAY, pendingIntent2);
    }

    public static void stopAlarms(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, AlarmReceiver.REQUEST_CODE, intent, 0);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, AlarmReceiver.REQUEST_CODE2, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.cancel(pendingIntent2);
    }

    public static boolean isTimeBetweenTwoTime(String initialTime, String finalTime) throws ParseException {
        String reg = "^([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");
        String currentTime = sdfTime.format(new Date());
        if (initialTime.matches(reg) && finalTime.matches(reg) && currentTime.matches(reg)) {
            boolean valid = false;
            //Start Time
            java.util.Date inTime = sdfTime.parse(initialTime);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(inTime);
            //Current Time
            java.util.Date checkTime = sdfTime.parse(currentTime);
            Calendar calendar3 = Calendar.getInstance();
            calendar3.setTime(checkTime);
            //End Time
            java.util.Date finTime = sdfTime.parse(finalTime);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(finTime);
            if (finalTime.compareTo(initialTime) < 0) {
                calendar2.add(Calendar.DATE, 1);
                calendar3.add(Calendar.DATE, 1);
            }
            java.util.Date actualTime = calendar3.getTime();
            if ((actualTime.after(calendar1.getTime()) || actualTime.compareTo(calendar1.getTime()) == 0) && actualTime.before(calendar2.getTime())) {
                valid = true;
            }
            return valid;
        } else {
            throw new IllegalArgumentException("Not a valid time, expecting HH:MM:SS format");
        }
    }
}
