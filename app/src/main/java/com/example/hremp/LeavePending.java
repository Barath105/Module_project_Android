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








public class LeavePending extends Fragment {

    private DatabaseReference hrLeaveReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leave_pending, container, false);

        // Initialize Firebase Database
        hrLeaveReference = FirebaseDatabase.getInstance().getReference().child("adminhr").child("hrleave");

        // Retrieve data with "Pending" status
        retrievePendingLeaveData(view);

        return view;
    }

    private void retrievePendingLeaveData(View view) {
        hrLeaveReference.orderByChild("status").equalTo("Pending").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear existing data if needed
                // ...

                TableLayout tableLayout = view.findViewById(R.id.tableLayout);

                // Variable to track if any data is found
                boolean dataFound = false;

                for (DataSnapshot leaveSnapshot : dataSnapshot.getChildren()) {
                    LeaveRequest leaveRequest = leaveSnapshot.getValue(LeaveRequest.class);
                    if (leaveRequest != null) {

                        addRowToTable(tableLayout, leaveRequest);
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




    private void showNoDataFoundMessage(TableLayout tableLayout) {
        TableRow tableRow = new TableRow(getContext());
        TextView noDataTextView = createTextView("No Data Found", 1f, Gravity.CENTER);

        // Set gravity of the TableRow to center
        tableRow.setGravity(Gravity.CENTER);

        tableRow.addView(noDataTextView);
        tableLayout.addView(tableRow);
    }


    private int rowCounter = 0; // Add this variable

    private void addRowToTable(TableLayout tableLayout, LeaveRequest leaveRequest) {
        TableRow tableRow = new TableRow(getContext());

        // Increment the row counter for each new row
        rowCounter++;

        // Create TextViews for each column with layout parameters
        TextView serialNoTextView = createTextView(String.valueOf(rowCounter), 1f, Gravity.CENTER);
        TextView userIdTextView = createTextView(leaveRequest.getUserId(), 1f, Gravity.CENTER);
        TextView leaveTypeTextView = createTextView(leaveRequest.getLeaveType(), 1f, Gravity.CENTER);
        TextView startDateTextView = createTextView(leaveRequest.getStartDate(), 1f, Gravity.CENTER);
        TextView endDateTextView = createTextView(leaveRequest.getEndDate(), 1f, Gravity.CENTER);
        TextView reasonTextView = createTextView(leaveRequest.getReason(), 1f, Gravity.CENTER);
        TextView fileNameTextView = createTextView(leaveRequest.getFileName(), 1f, Gravity.CENTER);
        TextView statusTextView = createTextView(leaveRequest.getStatus(), 1f, Gravity.CENTER);

        // Set an OnClickListener for the fileNameTextView
        fileNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Trigger the download action
                downloadFile(leaveRequest.getFileName(), leaveRequest.getFilePath());
            }
        });

        // Set text color based on the status
        int textColor = getStatusColor(leaveRequest.getStatus());
        statusTextView.setTextColor(textColor);

        // Add TextViews to the TableRow
        tableRow.addView(serialNoTextView);
        tableRow.addView(userIdTextView);
        tableRow.addView(leaveTypeTextView);
        tableRow.addView(startDateTextView);
        tableRow.addView(endDateTextView);
        tableRow.addView(reasonTextView);
        tableRow.addView(fileNameTextView);
        tableRow.addView(statusTextView);

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
    private void downloadFile(String fileName, String fileUrl) {
        try {
            // Attempt to create a valid URL
            URL url = new URL(fileUrl);

            // Create a DownloadManager request
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url.toString()));
            request.setTitle(fileName);
            request.setDescription("Downloading");

            // Set the destination file path
            String destination = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;
            request.setDestinationUri(Uri.parse("file://" + destination));

            // Get DownloadManager service
            DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

            // Enqueue the download and get download ID
            long downloadId = manager.enqueue(request);

            // Optionally, you can register a BroadcastReceiver to receive notifications on download completion
            // This is useful to notify the user when the download is complete
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    // Handle download completion, if needed
                    // For example, show a notification or perform additional actions
                }
            };

            getActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        } catch (MalformedURLException e) {
            // Handle the exception, e.g., log an error or show a message
            e.printStackTrace();
            // You might want to inform the user that the URL is not valid
            // or handle it in an appropriate way based on your app's requirements
        }
    }




}
