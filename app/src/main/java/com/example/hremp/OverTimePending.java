package com.example.hremp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.text.TextUtils;
import android.content.Intent;
import android.net.Uri;
import java.net.URL;

import android.view.Gravity;
import android.app.DownloadManager;
import android.os.Environment;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import java.net.MalformedURLException;

public class OverTimePending extends Fragment {

    private DatabaseReference overtimeReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overtime_pending, container, false);

        // Initialize Firebase Database
        overtimeReference = FirebaseDatabase.getInstance().getReference().child("adminhr").child("hrovertime");

        // Retrieve data with "Pending" status
        retrievePendingOvertimeData(view);

        return view;
    }

    private void retrievePendingOvertimeData(View view) {
        overtimeReference.orderByChild("status").equalTo("Pending").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear existing data if needed
                // ...

                TableLayout tableLayout = view.findViewById(R.id.tableLayout);


                // Check if the dataSnapshot is empty
                if (!dataSnapshot.exists()) {
                    // If no data is available, display a message
                    addNoDataRow(tableLayout);
                    return;
                }

                for (DataSnapshot overtimeSnapshot : dataSnapshot.getChildren()) {
                    OverTimeRequest overtimeRequest = overtimeSnapshot.getValue(OverTimeRequest.class);
                    if (overtimeRequest != null) {
                        addRowToTable(tableLayout, overtimeRequest);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }


    private void addNoDataRow(TableLayout tableLayout) {
        TableRow row = new TableRow(getContext());
        TextView textView = new TextView(getContext());

        textView.setText("No data found");
        textView.setTextColor(getResources().getColor(R.color.black));
        textView.setPadding(10, 100, 10, 10); // Set padding

        // Add styling if needed

        row.addView(textView);
        tableLayout.addView(row);
    }

    private int rowCounter = 0; // Add this variable

    private void addRowToTable(TableLayout tableLayout, OverTimeRequest overtimeRequest) {
        TableRow tableRow = new TableRow(getContext());

        // Increment the row counter for each new row
        rowCounter++;

        // Create TextViews for each column with layout parameters
        TextView serialNoTextView = createTextView(String.valueOf(rowCounter), 1f, Gravity.CENTER);
        TextView userIdTextView = createTextView(overtimeRequest.userId, 1f, Gravity.CENTER);
        TextView nameTextView = createTextView(overtimeRequest.name, 1f, Gravity.CENTER);
        TextView dateTextView = createTextView(overtimeRequest.date, 1f, Gravity.CENTER);
        TextView statusTextView = createTextView(overtimeRequest.status, 1f, Gravity.CENTER);
        // Add more columns as needed

        // Set text color based on the status
        int textColor = getStatusColor(overtimeRequest.status);
        statusTextView.setTextColor(textColor);

        // Add TextViews to the TableRow
        tableRow.addView(serialNoTextView);
        tableRow.addView(userIdTextView);
        tableRow.addView(nameTextView);
        tableRow.addView(dateTextView);
        tableRow.addView(statusTextView);
        // Add more TextViews to the TableRow

        // Add TableRow to the TableLayout
        tableLayout.addView(tableRow);
    }




    private int getStatusColor(String status) {
        if ("Pending".equals(status)) {
            return getResources().getColor(R.color.yellow); // Use the color for Approved (e.g., green)
        } else {
            return getResources().getColor(R.color.black); // Default color or any other color you prefer
        }
    }

    private TextView createTextView(String text, float weight, int gravity) {
        TextView textView = new TextView(getContext());
        textView.setPadding(10, 10, 10, 10);
        textView.setTextColor(getResources().getColor(R.color.black));

        // Set layout parameters with weight and gravity
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        params.width = 100; // Set width to 100dp
        params.gravity = gravity;

        textView.setLayoutParams(params);

        // Set maximum width and ellipsize if needed
        int maxTextLength = 22; // You can adjust this based on your preference

        if (text.length() > maxTextLength) {
            // If it exceeds the maximum length, split into lines
            String[] words = text.split(" ");
            StringBuilder line = new StringBuilder();

            for (String word : words) {
                if (line.length() + word.length() + 1 <= maxTextLength) {
                    // If adding the next word doesn't exceed the limit, append it
                    if (line.length() > 0) {
                        line.append(" ");
                    }
                    line.append(word);
                } else {
                    // If adding the next word exceeds the limit, create a new line
                    addTextLine(textView, line.toString());
                    line = new StringBuilder(word);
                }
            }

            // Add the last line if there are remaining words
            if (line.length() > 0) {
                addTextLine(textView, line.toString());
            }
        } else {
            // If the text doesn't exceed the maximum length, simply set it
            textView.setText(text);
        }

        return textView;
    }

    private void addTextLine(TextView textView, String line) {
        // Add a new line to the TextView
        textView.append(line);
        textView.append("\n");
    }

    // ... (Add more methods as needed, e.g., for downloadFile)

    public static class OverTimeRequest {
        public String userId;
        public String name;
        public String date;
        public String status;
        // Add more fields as needed
        // Default constructor required for Firebase
        public OverTimeRequest() {}

        public OverTimeRequest(String userId, String name, String date) {
            this.userId = userId;
            this.name = name;
            this.date = date;
            // Initialize additional fields
        }

        // Add getters and setters as needed
    }
}
