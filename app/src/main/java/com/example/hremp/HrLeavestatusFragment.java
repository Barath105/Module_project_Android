package com.example.hremp;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HrLeavestatusFragment extends Fragment {

    private DatabaseReference hrLeaveReference;
    private int rowCounter = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hr_leave_fragment, container, false);

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
                TableLayout tableLayout = view.findViewById(R.id.tableLayout);

                // Check if the dataSnapshot is empty
                if (!dataSnapshot.exists()) {
                    // If no data is available, display a message
                    addNoDataRow(tableLayout);
                    return;
                }

                for (DataSnapshot hrleaveSnapshot : dataSnapshot.getChildren()) {
                    HrLeaveRequest hrleaveRequest = hrleaveSnapshot.getValue(HrLeaveRequest.class);
                    if (hrleaveRequest != null) {
                        // Set the DatabaseReference for each HrLeaveRequest
                        hrleaveRequest.setRef(hrleaveSnapshot.getRef());

                        addRowToTable(tableLayout, hrleaveRequest);
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

    private void addRowToTable(TableLayout tableLayout, HrLeaveRequest hrleaveRequest) {
        TableRow tableRow = new TableRow(getContext());

        // Increment the row counter for each new row
        rowCounter++;

        // Create TextViews for each column with layout parameters
        TextView serialNoTextView = createTextView(String.valueOf(rowCounter), 1f, Gravity.CENTER);
        TextView userIdTextView = createTextView(hrleaveRequest.getUserId(), 1f, Gravity.CENTER);
        TextView leaveTypeTextView = createTextView(hrleaveRequest.getLeaveType(), 1f, Gravity.CENTER);
        TextView startDateTextView = createTextView(hrleaveRequest.getStartDate(), 1f, Gravity.CENTER);
        TextView endDateTextView = createTextView(hrleaveRequest.getEndDate(), 1f, Gravity.CENTER);
        TextView reasonTextView = createTextView(hrleaveRequest.getReason(), 1f, Gravity.CENTER);
        TextView fileNameTextView = createTextView(hrleaveRequest.getFileName(), 1f, Gravity.CENTER);

        // Add approve and reject icons
        ImageView approveIcon = createImageView(R.drawable.tick, 1f, Gravity.CENTER);
        ImageView rejectIcon = createImageView(R.drawable.cancel, 1f, Gravity.CENTER);

        // Set onClickListeners for the approve and reject icons
        approveIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the approve action
                updateLeaveStatus(hrleaveRequest, "Approved");
            }
        });

        rejectIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the reject action
                updateLeaveStatus(hrleaveRequest, "Rejected");
            }
        });

        // Set an OnClickListener for the fileNameTextView
        fileNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Trigger the download action
                downloadFile(hrleaveRequest.getFileName(), hrleaveRequest.getFilePath());
            }
        });

        // Add TextViews and icons to the TableRow
        tableRow.addView(serialNoTextView);
        tableRow.addView(userIdTextView);
        tableRow.addView(leaveTypeTextView);
        tableRow.addView(startDateTextView);
        tableRow.addView(endDateTextView);
        tableRow.addView(reasonTextView);
        tableRow.addView(fileNameTextView);
        tableRow.addView(approveIcon);
        tableRow.addView(rejectIcon);

        // Set margins for the TableRow
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
        );
        int margin = 16; // Adjust the margin as needed
        tableRowParams.setMargins(margin, margin, margin, margin);
        tableRow.setLayoutParams(tableRowParams);

        // Add TableRow to the TableLayout
        tableLayout.addView(tableRow);
    }

    private ImageView createImageView(int drawableId, float weight, int gravity, int width, int height) {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(drawableId);
        imageView.setPadding(10, 0, 10, 10);

        // Set layout parameters with weight, gravity, width, and height
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        params.width = width;
        params.height = height;
        params.gravity = gravity;
        imageView.setLayoutParams(params);

        return imageView;
    }

    private ImageView createImageView(int drawableId, float weight, int gravity) {
        // Provide default width and height if not specified
        int defaultWidth = 100; // Set your default width
        int defaultHeight = 100; // Set your default height

        return createImageView(drawableId, weight, gravity, defaultWidth, defaultHeight);
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

    private void updateLeaveStatus(HrLeaveRequest hrleaveRequest, String newStatus) {
        DatabaseReference leaveRef = hrleaveRequest.getRef();

        Log.d("UpdateLeaveStatus", "Leave ID: " + hrleaveRequest.getLeaveId());
        Log.d("UpdateLeaveStatus", "Reference: " + leaveRef.toString());
        Log.d("UpdateLeaveStatus", "New Status: " + newStatus);

        // Create a Map to update only the 'status' field
        Map<String, Object> updateStatus = new HashMap<>();
        updateStatus.put("status", newStatus);

        // Update the leave status in Firebase Database
        leaveRef.updateChildren(updateStatus)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Update successful
                        // You can perform any additional actions here
                        Log.d("UpdateLeaveStatus", "Status updated successfully");

                        // Notify the user about the status change
                        Toast.makeText(getContext(), "Leave status updated to: " + newStatus, Toast.LENGTH_SHORT).show();

                        // Optionally, you can refresh the UI or perform other actions after the status update
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Log.e("UpdateLeaveStatus", "Error updating status", e);
                        // Notify the user about the failure
                        Toast.makeText(getContext(), "Failed to update leave status", Toast.LENGTH_SHORT).show();
                    }
                });
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
