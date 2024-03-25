package com.example.hremp;
import android.os.Bundle;
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

public class PermissionHistory extends Fragment {

    private DatabaseReference hrPermissionReference;
    private int serialNumber = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permission_pending, container, false);

        hrPermissionReference = FirebaseDatabase.getInstance().getReference().child("adminhr").child("hrpermission");

        // Retrieve data with "Pending" status
        retrievePendingPermissionData(view);

        return view;
    }

    private void retrievePendingPermissionData(View view) {
        hrPermissionReference.orderByChild("status").startAt("Approved").endAt("Rejected")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Clear existing data if needed
                        // ...

                        TableLayout tableLayout = view.findViewById(R.id.tableLayout);

                        // Variable to track if any data is found
                        boolean dataFound = false;

                        for (DataSnapshot permissionSnapshot : dataSnapshot.getChildren()) {
                            PermissionRequest permissionRequest = permissionSnapshot.getValue(PermissionRequest.class);
                            if (permissionRequest != null && !permissionRequest.getStatus().equals("Pending")) {
                                addRowToTable(tableLayout, permissionRequest);
                                dataFound = true; // Data is found
                            }
                        }

                        // If no data is found, display "No Data Found" message at the center
                        if (!dataFound) {
                            showNoDataFoundMessage(tableLayout);
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

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    private void addRowToTable(TableLayout tableLayout, PermissionRequest permissionRequest) {
        TableRow tableRow = new TableRow(getContext());

        // Increment the serial number for each row added
        serialNumber++;

        TextView serialNoTextView = createTextView(String.valueOf(serialNumber), 1f, Gravity.CENTER);  // Replace with appropriate value
        TextView userIdTextView = createTextView(permissionRequest.getUserId(), 1f, Gravity.CENTER);
        TextView SessionTextView = createTextView(permissionRequest.getSession(), 1f, Gravity.CENTER);
        TextView DateTextView = createTextView(permissionRequest.getDate(), 1f, Gravity.CENTER);
        TextView reasonTextView = createTextView(permissionRequest.getReason(), 1f, Gravity.CENTER);
        TextView fileNameTextView = createTextView(permissionRequest.getFileName(), 1f, Gravity.CENTER);
        TextView statusTextView = createTextView(permissionRequest.getStatus(), 1f, Gravity.CENTER);

        // Set text color based on the status
        int textColor = getStatusColor(permissionRequest.getStatus());
        statusTextView.setTextColor(textColor);

        tableRow.addView(serialNoTextView);
        tableRow.addView(userIdTextView);
        tableRow.addView(SessionTextView);
        tableRow.addView(DateTextView);
        tableRow.addView(reasonTextView);
        tableRow.addView(fileNameTextView);
        tableRow.addView(statusTextView);

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
