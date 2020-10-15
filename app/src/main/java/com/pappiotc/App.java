package com.pappiotc;

import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;

import com.pappiotc.helper.acra.ACRAReportSender;
import com.securepreferences.SecurePreferences;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;


@ReportsCrashes(
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class App extends MultiDexApplication {
    protected static App instance;
    private SecurePreferences mSecurePrefs;

    public App() {
        super();
        instance = this;
    }

    public static App get() {
        return instance;
    }

    /**
     * Single point for the app to get the secure prefs object
     *
     * @return
     */
    public SharedPreferences getSharedPreferences() {
        if (mSecurePrefs == null) {
            mSecurePrefs = new SecurePreferences(this, "", "my_prefs.xml");
            SecurePreferences.setLoggingEnabled(true);
        }
        return mSecurePrefs;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        ACRA.init(this);

        // instantiate the report sender with the email credentials.
        // these will be used to send the crash report
        ACRAReportSender reportSender = new ACRAReportSender("aurora.crash.report@gmail.com", "tgbyhn@23");

        // register it with ACRA.
        ACRA.getErrorReporter().setReportSender(reportSender);

    }
}
