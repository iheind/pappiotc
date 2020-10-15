package com.pappiotc.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pappiotc.App;
import com.pappiotc.R;
import com.pappiotc.controller.SalesTrackerController;
import com.pappiotc.helper.Constants;
import com.pappiotc.model.CheckIn;
import com.pappiotc.model.Client;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LoginActivity extends AppCompatActivity implements Validator.ValidationListener {

    @InjectView(R.id.login_login_button)
    Button loginButton;

    @InjectView(R.id.login_forgot_password_textview)
    TextView forgotPasswordTextView;

    @InjectView(R.id.login_username_edittext)
    @NotEmpty(messageResId = R.string.err_msg_username)
    EditText userNameEditText;

    @InjectView(R.id.login_password_edittext)
    @Password(min = 4, messageResId = R.string.err_msg_password)
    EditText passwordEditText;

    private Validator loginValidator;
    SharedPreferences sharedPreferences;
    int MY_PERMISSIONS_REQUEST_STATE = 10;
    private String IMEINumber;


    private SalesTrackerController salesTrackerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        context = this;
//        sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        sharedPreferences = App.get().getSharedPreferences();


        ButterKnife.inject(this);
        salesTrackerController = new SalesTrackerController(LoginActivity.this);

        //Auto-login if this is not the first time to login
        if (!sharedPreferences.getBoolean("firstLaunch", true)) {
            Constants.token = sharedPreferences.getString("token", "");
            Constants.userName = sharedPreferences.getString("userName", "");
            String doctorsJson = sharedPreferences.getString("doctors", "");
            String pharmaciesJson = sharedPreferences.getString("pharmacies", "");
            String logsJson = sharedPreferences.getString("logs", "");

            Type type = new TypeToken<ArrayList<Client>>() {
            }.getType();
            Gson gson = new Gson();
            Constants.doctorsArrayList = gson.fromJson(doctorsJson, type);
            Constants.pharmaciesArrayList = gson.fromJson(pharmaciesJson, type);

            Type logType = new TypeToken<ArrayList<CheckIn>>() {
            }.getType();
            Constants.logsArrayList = gson.fromJson(logsJson, logType);

            Intent salesTrackerIntent = new Intent(this, SalesTrackerActivity.class);
            salesTrackerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(salesTrackerIntent);
        }

        // For Validation
        loginValidator = new Validator(this);
        loginValidator.setValidationListener(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginValidator.validate();
            }

        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resetPasswordIntent = new Intent(getApplicationContext(), ResetPasswordActivity.class);
                startActivity(resetPasswordIntent);
            }
        });
    }

    /**
     * onValidationSucceeded: Do the logic of login after validating user data
     */
    @Override
    public void onValidationSucceeded() {
        //trigger 'loadIMEI'
        loadIMEI();
    }

    /**
     * onValidationFailed: Display error message that results from the validation process
     *
     * @param errors
     */
    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        // Show errors
        for (int i = 0; i < errors.size(); i++) {
            ValidationError validationError = errors.get(i);
            String message = validationError.getCollatedErrorMessage(this);
            if (validationError.getView() instanceof EditText) {
                validationError.getView().requestFocus();
                ((EditText) validationError.getView()).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * loadIMEI: check for permission and ask user if not granted.
     */
    public void loadIMEI() {
        // Check if the READ_PHONE_STATE permission is already available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //  permissions has not been granted.
            requestReadPhoneStatePermission(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        } else {
            // permissions is already been granted.
            doPermissionGrantedStuffs();
        }
    }


    /**
     * requestReadPhoneStatePermission: Requests the READ_PHONE_STATE permission.
     * If the permission has been denied previously, a dialog will prompt the user to grant the
     * permission, otherwise it is requested directly.
     *
     * @param permissions
     */
    private void requestReadPhoneStatePermission(final String[] permissions) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_PHONE_STATE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            new AlertDialog.Builder(this)
                    .setTitle("Permission Request")
                    .setMessage(getString(R.string.permission_state_rationale))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //re-request
                            ActivityCompat.requestPermissions(LoginActivity.this, permissions,
                                    MY_PERMISSIONS_REQUEST_STATE);
                        }
                    })
                    .setIcon(R.drawable.ic_launcher)
                    .show();
        } else {
            // READ_PHONE_STATE permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, permissions,
                    MY_PERMISSIONS_REQUEST_STATE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_STATE) {
            // Received permission result for READ_PHONE_STATE permission.est.");
            // Check if the only required permission has been granted
            if (grantResults.length == 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                // READ_PHONE_STATE permission has been granted, proceed with displaying IMEI Number
                //alertAlert(getString(R.string.permision_available_read_phone_state));
                doPermissionGrantedStuffs();
            } else {
                alertAlert(getString(R.string.permissions_not_granted_state));
            }
        }
    }

    private void alertAlert(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Permission Request")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do somthing here
                    }
                })
                .setIcon(R.drawable.ic_launcher)
                .show();
    }

    /**
     * doPermissionGrantedStuffs: handle action (get IMEI) when permission is granted
     */
    public void doPermissionGrantedStuffs() {
        //Have an  object of TelephonyManager
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Get IMEI Number of Phone
        //IMEINumber = tm.getDeviceId();
        IMEINumber = "3454543434243";
        if (IMEINumber != null) {
            // All is OK and have the IMEI so call login
            salesTrackerController.callLoginService(userNameEditText.getText().toString(), passwordEditText.getText().toString(), IMEINumber);
        } else
            Toast.makeText(this, "Can not get IMEI", Toast.LENGTH_LONG).show();
    }
}
