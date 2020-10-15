package com.pappiotc.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.pappiotc.App;
import com.pappiotc.R;
import com.pappiotc.controller.SalesTrackerController;
import com.pappiotc.fragments.LogFragment;
import com.pappiotc.helper.Constants;
import com.pappiotc.model.CheckIn;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LogViewListAdapter extends ArrayAdapter<CheckIn> implements Filterable {
    public Context context;
    public int resource;
    SalesTrackerController salesTrackerController;
    SharedPreferences sharedPreferences;
    public ArrayList<CheckIn> filteredList;
    private LogFilter logFilter;
    private int selected;

    public LogViewListAdapter(Context context, int resource) {
        super(context, resource, Constants.logsArrayList);
        salesTrackerController = new SalesTrackerController(context);
//        sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        sharedPreferences = App.get().getSharedPreferences();
        this.context = context;
        this.resource = resource;
        this.filteredList = Constants.logsArrayList;
        getFilter();
    }

    /**
     * Get size of user list
     *
     * @return userList size
     */
    @Override
    public int getCount() {
        return filteredList.size();
    }

    /**
     * Get specific item from user list
     *
     * @param i item index
     * @return list item
     */
    @Override
    public CheckIn getItem(int i) {
        return filteredList.get(i);
    }

    /**
     * Get user list item id
     *
     * @param i item index
     * @return current item id
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Create list row view
     *
     * @param position    index
     * @param convertView current list item view
     * @param parent      parent
     * @return view
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final LogHolder holder;
        Log.e("SalesTracker", "view " + position);
        if (convertView == null) {
            LayoutInflater viewInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = viewInflater.inflate(resource, null);
            holder = new LogHolder();

            // set the holder contents
            holder.clientTextview = (TextView) convertView.findViewById(R.id.list_item_client_textview);
            holder.noteEditText = (EditText) convertView.findViewById(R.id.list_item_note_edittext);
            holder.editImageview = (ImageView) convertView.findViewById(R.id.list_item_edit_imageview);
            holder.daysTextView = (TextView) convertView.findViewById(R.id.list_item_days_textview);
            holder.saveButton = (Button) convertView.findViewById(R.id.list_item_save_button);
            holder.cancelButton = (Button) convertView.findViewById(R.id.list_item_cancel_button);
            holder.saveButton.setVisibility(View.GONE);
            holder.cancelButton.setVisibility(View.GONE);
            holder.noteEditText.setBackgroundColor(Color.TRANSPARENT);
            holder.noteEditText.setEnabled(false);
            convertView.setTag(holder);
        } else {
            holder = (LogHolder) convertView.getTag();
        }


        // refresh contents data
        holder.clientTextview.setText(filteredList.get(position).getClientName());
        holder.clientTextview.setTag(filteredList.get(position).getId());
        holder.noteEditText.setText(filteredList.get(position).getNote());
        Date createDate;
        if (filteredList.get(position).getCreateDate() == 0)
            createDate = new Date();
        else
            createDate = new Date(filteredList.get(position).getCreateDate());
        String diff = getDateDiff(createDate);
        holder.daysTextView.setText(diff);
        if (position != selected) {
            holder.cancelButton.setVisibility(View.GONE);
            holder.saveButton.setVisibility(View.GONE);
            holder.noteEditText.setBackgroundColor(Color.TRANSPARENT);
            holder.noteEditText.setEnabled(false);
        }
        holder.editImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = position;
                // show buttons and make note editable
                holder.saveButton.setVisibility(View.VISIBLE);
                holder.cancelButton.setVisibility(View.VISIBLE);
                holder.noteEditText.setBackgroundResource(R.drawable.note_border);
                holder.noteEditText.requestFocus();
                holder.noteEditText.setEnabled(true);
                final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(holder.noteEditText, InputMethodManager.SHOW_IMPLICIT);
                LogFragment.logListView.setSelection(position);
            }
        });

        holder.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hide buttons and make note uneditable
                holder.cancelButton.setVisibility(View.GONE);
                holder.saveButton.setVisibility(View.GONE);
                holder.noteEditText.setEnabled(false);
                holder.noteEditText.setBackgroundColor(Color.TRANSPARENT);
                salesTrackerController.callUpdateCheckinService(Constants.token, holder.clientTextview.getTag().toString(), holder.noteEditText.getText().toString());
                filteredList.get(position).setNote(holder.noteEditText.getText().toString());
                notifyDataSetChanged();
            }
        });

        holder.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = -1;
                // hide buttons and make note uneditable
                holder.cancelButton.setVisibility(View.GONE);
                holder.saveButton.setVisibility(View.GONE);
                holder.noteEditText.setBackgroundColor(Color.TRANSPARENT);
                holder.noteEditText.setEnabled(false);
                holder.noteEditText.setText(filteredList.get(position).getNote());

            }
        });
        return convertView;
    }

    /**
     * Get custom filter
     *
     * @return filter
     */
    @Override
    public Filter getFilter() {
        if (logFilter == null) {
            logFilter = new LogFilter();
        }

        return logFilter;
    }

    /**
     * getDateDiff: To display how old the log record is
     *
     * @param createDate
     * @return
     */

    private String getDateDiff(Date createDate) {
        SimpleDateFormat format = new SimpleDateFormat(Constants.dateFormat);
        Date now = null;
        long diffDays = 0, diffHours = 0, diffSeconds = 0, diffMinutes = 0;
        try {
            now = new Date();

            //in milliseconds
            long diff = now.getTime() - createDate.getTime();

            diffSeconds = diff / 1000 % 60;
            diffMinutes = diff / (60 * 1000) % 60;
            diffHours = diff / (60 * 60 * 1000) % 24;
            diffDays = diff / (24 * 60 * 60 * 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return diffDays + "d " + diffHours + "h";
    }

    static class LogHolder {
        TextView clientTextview;
        EditText noteEditText;
        ImageView editImageview;
        TextView daysTextView;
        Button saveButton;
        Button cancelButton;
    }

    /**
     * Custom filter for list
     * Filter content in list according to the search text
     */
    private class LogFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                ArrayList<CheckIn> tempList = new ArrayList<CheckIn>();

                // search content in check in list
                for (CheckIn checkIn : Constants.logsArrayList) {
                    if (checkIn.getClientName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(checkIn);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = Constants.logsArrayList.size();
                filterResults.values = Constants.logsArrayList;
            }

            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         *
         * @param constraint text
         * @param results    filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<CheckIn>) results.values;
            notifyDataSetChanged();
        }
    }

}

