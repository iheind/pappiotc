package com.pappiotc.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.pappiotc.R;
import com.pappiotc.adapters.LogViewListAdapter;
import com.pappiotc.helper.Constants;

public class LogFragment extends Fragment {
    public static LogViewListAdapter logViewListAdapter;
    public static ListView logListView;

    public LogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        logListView = (ListView) view.findViewById(R.id.log_listview);

        // Setting the log list adapter
        logViewListAdapter = new LogViewListAdapter(getContext(), R.layout.list_item_log);
        logListView.setAdapter(logViewListAdapter);
        return view;
    }

    //
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void refresh() {
        logViewListAdapter.filteredList = Constants.logsArrayList;
        logViewListAdapter.notifyDataSetChanged();
    }
}
