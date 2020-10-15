package com.pappiotc.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.pappiotc.App;
import com.pappiotc.model.CheckIn;
import com.pappiotc.model.Client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Constants {
    public static String userName, token;
    public static ArrayList<Client> doctorsArrayList = new ArrayList<Client>();
    public static ArrayList<Client> pharmaciesArrayList = new ArrayList<Client>();
    public static ArrayList<CheckIn> logsArrayList = new ArrayList<CheckIn>();
    public static String dateFormat = "dd-MM-yyyy hh:mm:ss";

    public static void nullifyData(Context context) {
        userName = null;
        token = null;
        doctorsArrayList = null;
        pharmaciesArrayList = null;
        logsArrayList = null;

        SharedPreferences sharedPreferences = App.get().getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("firstLaunch", true);
        editor.remove("token");
        editor.remove("userName");
        editor.remove("doctors");
        editor.remove("pharmacies");
        editor.remove("logs");
        editor.apply();
    }

    public static void setLogs(ArrayList<CheckIn> logs){
        Collections.sort(logs, new Comparator<CheckIn>() {
            public int compare(CheckIn o1, CheckIn o2) {
                if (o1.getCreateDate() < o2.getCreateDate()) {
                    return 1;
                }
                if (o1.getCreateDate() == o2.getCreateDate()) {
                    return 0;
                }
                return -1;
//                return new Date(o1.getCreateDate()).compareTo(new Date(o2.getCreateDate()));
            }
        });
        logsArrayList = logs;
    }
}
