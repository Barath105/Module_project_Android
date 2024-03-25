package com.example.hremp;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.view.Gravity;


import java.net.MalformedURLException;
import java.net.URL;

public class OdPending extends Fragment {

    private DatabaseReference hrOdReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_od_pending, container, false);

        hrOdReference = FirebaseDatabase.getInstance().getReference().child("adminhr").child("hrod");

        // Retrieve data with "Pending" status
        retrievePendingOdData(view);

        return view;
    }

    private void retrievePendingOdData(View view) {
        hrOdReference.orderByChild("status").equalTo("Pending").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                TableLayout tableLayout = view.findViewById(R.id.tableLayout);

                // Check if the dataSnapshot is empty
                if (!dataSnapshot.exists()) {
                    // If no data is available, display a message
                    addNoDataRow(tableLayout);
                    return;
                }



                for (DataSnapshot odSnapshot : dataSnapshot.getChildren()) {
                    OdRequest odRequest = odSnapshot.getValue(OdRequest.class);
                    if (odRequest != null) {
                        addRowToTable(tableLayout, odRequest);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(requireContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

    private int rowCounter = 0;

    private void addRowToTable(TableLayout tableLayout, OdRequest odRequest) {
        TableRow tableRow = new TableRow(getContext());
        rowCounter++;

        TextView serialNoTextView = createTextView(String.valueOf(rowCounter), 1f, Gravity.CENTER);
        TextView userIdTextView = createTextView(odRequest.getUserId(), 1f, Gravity.CENTER);
        TextView startDateTextView = createTextView(odRequest.getStartDate(), 1f, Gravity.CENTER);
        TextView endDateTextView = createTextView(odRequest.getEndDate(), 1f, Gravity.CENTER);
        TextView reasonTextView = createTextView(odRequest.getReason(), 1f, Gravity.CENTER);
        TextView fileNameTextView = createTextView(odRequest.getFileName(), 1f, Gravity.CENTER);
        TextView statusTextView = createTextView(odRequest.getStatus(), 1f, Gravity.CENTER);

        fileNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadFile(odRequest.getFileName(), odRequest.getFileUrl());
            }
        });


        // Set text color based on the status
        int textColor = getStatusColor(odRequest.getStatus());
        statusTextView.setTextColor(textColor);

        tableRow.addView(serialNoTextView);
        tableRow.addView(userIdTextView);
        tableRow.addView(startDateTextView);
        tableRow.addView(endDateTextView);
        tableRow.addView(reasonTextView);
        tableRow.addView(fileNameTextView);
        tableRow.addView(statusTextView);

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

        TableRow.LayoutParams params = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        params.width = 100;
        params.gravity = gravity;

        textView.setLayoutParams(params);

        int maxTextLength = 22;

        if (text.length() > maxTextLength) {
            String[] words = text.split(" ");
            StringBuilder line = new StringBuilder();

            for (String word : words) {
                if (line.length() + word.length() + 1 <= maxTextLength) {
                    if (line.length() > 0) {
                        line.append(" ");
                    }
                    line.append(word);
                } else {
                    addTextLine(textView, line.toString());
                    line = new StringBuilder(word);
                }
            }

            if (line.length() > 0) {
                addTextLine(textView, line.toString());
            }
        } else {
            textView.setText(text);
        }

        return textView;
    }

    private void addTextLine(TextView textView, String line) {
        textView.append(line);
        textView.append("\n");
    }

    private void downloadFile(String fileName, String fileUrl) {
        try {
            URL url = new URL(fileUrl);

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url.toString()));
            request.setTitle(fileName);
            request.setDescription("Downloading");

            String destination = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;
            request.setDestinationUri(Uri.parse("file://" + destination));

            DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

            long downloadId = manager.enqueue(request);

            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    // Handle download completion, if needed
                    // For example, show a notification or perform additional actions
                }
            };

            getActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error: Invalid URL", Toast.LENGTH_SHORT).show();
        }
    }
}
