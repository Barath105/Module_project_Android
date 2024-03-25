package com.example.hremp;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
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

import java.net.MalformedURLException;
import java.net.URL;

public class OdHistory extends Fragment {

    private DatabaseReference hrOdReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_od_history, container, false);

        // Initialize Firebase Database
        hrOdReference = FirebaseDatabase.getInstance().getReference().child("adminhr").child("hrod");

        // Retrieve data with "Approved" or "Rejected" status
        retrieveApprovedRejectedOdData(view);

        return view;
    }

    private void retrieveApprovedRejectedOdData(View view) {
        hrOdReference.orderByChild("status").startAt("Approved").endAt("Rejected")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Clear existing data if needed
                        // ...

                        TableLayout tableLayout = view.findViewById(R.id.tableLayout);

                        // Variable to track if any data is found
                        boolean dataFound = false;

                        for (DataSnapshot odSnapshot : dataSnapshot.getChildren()) {
                            OdRequest odRequest = odSnapshot.getValue(OdRequest.class);
                            if (odRequest != null && !odRequest.getStatus().equals("Pending")) {
                                addRowToTable(tableLayout, odRequest);
                                dataFound = true; // Data is found
                            }
                        }

                        // If no data is found, display "No Data Found" message at the center
                        if (!dataFound) {
                            showNoDataFoundMessage(tableLayout);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    private int rowCounter = 0; // Add this variable

    private void addRowToTable(TableLayout tableLayout, OdRequest odRequest) {
        TableRow tableRow = new TableRow(getContext());

        rowCounter++;
        // Create TextViews for each column with layout parameters

        TextView serialNoTextView = createTextView(String.valueOf(rowCounter), 1f, Gravity.CENTER);
        TextView userIdTextView = createTextView(odRequest.getUserId(), 1f, Gravity.CENTER);
        TextView startDateTextView = createTextView(odRequest.getStartDate(), 1f, Gravity.CENTER);
        TextView endDateTextView = createTextView(odRequest.getEndDate(), 1f, Gravity.CENTER);
        TextView reasonTextView = createTextView(odRequest.getReason(), 1f, Gravity.CENTER);
        TextView fileNameTextView = createTextView(odRequest.getFileName(), 1f, Gravity.CENTER);
        TextView statusTextView = createTextView(odRequest.getStatus(), 1f, Gravity.CENTER);


        // Set text color based on the status
        int textColor = getStatusColor(odRequest.getStatus());
        statusTextView.setTextColor(textColor);
        // Add TextViews to the TableRow
        tableRow.addView(serialNoTextView);
        tableRow.addView(userIdTextView);
        tableRow.addView(startDateTextView);
        tableRow.addView(endDateTextView);
        tableRow.addView(reasonTextView);
        tableRow.addView(fileNameTextView);
        tableRow.addView(statusTextView);

        // Add TableRow to the TableLayout
        tableLayout.addView(tableRow);
    }

    private int getStatusColor(String status) {
        if ("Approved".equals(status)) {
            return getResources().getColor(R.color.Green); // Use the color for Approved (e.g., green)
        } else if ("Rejected".equals(status)) {
            return getResources().getColor(R.color.Red); // Use the color for Rejected (e.g., red)
        } else {
            return getResources().getColor(R.color.black); // Default color or any other color you prefer
        }
    }

    private void showNoDataFoundMessage(TableLayout tableLayout) {
        TableRow tableRow = new TableRow(getContext());
        TextView noDataTextView = createTextView("No Data Found", 1f, Gravity.CENTER);

        // Set gravity of the TableRow to center
        tableRow.setGravity(Gravity.CENTER);

        tableRow.addView(noDataTextView);
        tableLayout.addView(tableRow);
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

    private void downloadFile(String fileName, String fileUrl) {
        // Implement the download file logic here
    }
}
