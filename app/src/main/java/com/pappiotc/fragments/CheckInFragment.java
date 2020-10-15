package com.pappiotc.fragments;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.pappiotc.App;
import com.pappiotc.R;
import com.pappiotc.activities.SalesTrackerActivity;
import com.pappiotc.controller.SalesTrackerController;
import com.pappiotc.helper.Constants;
import com.pappiotc.model.Client;
import com.pappiotc.model.Type;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CheckInFragment extends Fragment {

    private TextView dateTextView;
    private Spinner typeSpinner;
    private Spinner clientSpinner;
    private Button checkInButton;
    ArrayAdapter<Client> doctorSpinnerArrayAdapter;
    ArrayAdapter<Client> pharmacySpinnerArrayAdapter;
    SharedPreferences sharedPreferences;
    SalesTrackerController salesTrackerController;


    public CheckInFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_check_in, container, false);
        salesTrackerController = new SalesTrackerController(getContext());

        // Get the fields
        dateTextView = (TextView) view.findViewById(R.id.checkin_date_textview);
        typeSpinner = (Spinner) view.findViewById(R.id.checkin_type_spinner);
        clientSpinner = (Spinner) view.findViewById(R.id.checkin_client_spinner);
        checkInButton = (Button) view.findViewById(R.id.checkin_checkin_button);

        // Set the check in date with current date and time
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm a");
        String strDate = sdf.format(date);
        dateTextView.setText(strDate);

        // Set the type list adapter
        List typeList = new ArrayList<Type>();
        typeList.add(new Type("1", "Doctor"));
        typeList.add(new Type("2", "Pharmacy"));

        ArrayAdapter<String> typeSpinnerArrayAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, typeList);
        typeSpinner.setAdapter(typeSpinnerArrayAdapter);

        doctorSpinnerArrayAdapter = new ArrayAdapter<Client>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, Constants.doctorsArrayList);
        pharmacySpinnerArrayAdapter = new ArrayAdapter<Client>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, Constants.pharmaciesArrayList);
        clientSpinner.setAdapter(doctorSpinnerArrayAdapter);

        // change the client list according to the type selected
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("Position ", position + "");
                if (position == 0) {
                    clientSpinner.setAdapter(doctorSpinnerArrayAdapter);
                } else if (position == 1) {
                    clientSpinner.setAdapter(pharmacySpinnerArrayAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.e("onNothingSelected", "onNothingSelected");
            }
        });

        Integer resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (resultCode == ConnectionResult.SUCCESS) {
            //Do what you want
            checkInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get the token value
                    sharedPreferences = App.get().getSharedPreferences();
                    String userToken = Constants.token;
                    // call Check in service
                    Location location = SalesTrackerActivity.userLocation;
                    if (location != null) {
                        salesTrackerController.callCheckinService(((Type) typeSpinner.getSelectedItem()).getId(), ((Client) clientSpinner.getSelectedItem()).getId(),
                                userToken, location.getLatitude() + "", location.getLongitude() + "");
                    } else {
                        Toast.makeText(getContext(), "Can not get your Location", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 0);
            if (dialog != null) {
                //This dialog will help the user update to the latest GooglePlayServices
                dialog.show();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        getActivity().finish();
                    }
                });
            }
        }

        return view;
    }

    public void refresh() {
        if (doctorSpinnerArrayAdapter != null)
            doctorSpinnerArrayAdapter.notifyDataSetChanged();
        if (pharmacySpinnerArrayAdapter != null)
            pharmacySpinnerArrayAdapter.notifyDataSetChanged();

    }

}
