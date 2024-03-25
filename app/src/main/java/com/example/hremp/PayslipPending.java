package com.example.hremp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.view.Gravity;


public class PayslipPending extends Fragment {

    private DatabaseReference hrPayslipReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payslip_pending, container, false);

        // Initialize Firebase Database
        hrPayslipReference = FirebaseDatabase.getInstance().getReference().child("adminhr").child("hrpayslip");

        // Retrieve data with "Pending" status
        retrievePendingPayslipData(view);

        return view;
    }

    private void retrievePendingPayslipData(View view) {
        hrPayslipReference.orderByChild("status").equalTo("Pending").addListenerForSingleValueEvent(new ValueEventListener() {
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

                for (DataSnapshot payslipSnapshot : dataSnapshot.getChildren()) {
                    PayslipFragment.PayslipRequest payslipRequest = payslipSnapshot.getValue(PayslipFragment.PayslipRequest.class);
                    if (payslipRequest != null) {
                        addRowToTable(tableLayout, payslipRequest);
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

    private void addRowToTable(TableLayout tableLayout, PayslipFragment.PayslipRequest payslipRequest) {
        TableRow tableRow = new TableRow(getContext());

        // Increment the row counter for each new row
        rowCounter++;

        // Create TextViews for each column with layout parameters
        TextView serialNoTextView = createTextView(String.valueOf(rowCounter), 1f, Gravity.CENTER);
        TextView userIdTextView = createTextView(payslipRequest.userId, 1f, Gravity.CENTER);
        TextView nameTextView = createTextView(payslipRequest.name, 1f, Gravity.CENTER);
        TextView dateTextView = createTextView(payslipRequest.date, 1f, Gravity.CENTER);
        TextView statusTextView = createTextView(payslipRequest.status, 1f, Gravity.CENTER);
        // Add more columns as needed

        // Set text color based on the status
        int textColor = getStatusColor(payslipRequest.status);
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
}
