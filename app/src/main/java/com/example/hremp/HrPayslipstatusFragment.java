package com.example.hremp;

import android.os.Bundle;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import android.widget.LinearLayout;

public class HrPayslipstatusFragment extends Fragment {

    private DatabaseReference hrPayslipReference;
    private int rowCounter = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hr_payslipstatus_fragment, container, false);

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
                TableLayout tableLayout = view.findViewById(R.id.tableLayout);

                // Reset the rowCounter
                rowCounter = 0;

                // Check if the dataSnapshot is empty
                if (!dataSnapshot.exists()) {
                    // If no data is available, display a message
                    addNoDataRow(tableLayout);
                    return;
                }

                for (DataSnapshot hrpayslipSnapshot : dataSnapshot.getChildren()) {
                    PayslipRequest hrpayslipRequest = hrpayslipSnapshot.getValue(PayslipRequest.class);
                    if (hrpayslipRequest != null) {
                        // Set the DatabaseReference for each HrOdRequest
                        hrpayslipRequest.setRef(hrpayslipSnapshot.getRef());

                        addRowToTable(tableLayout, hrpayslipRequest);
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

    private void addRowToTable(TableLayout tableLayout, PayslipRequest hrpayslipRequest) {
        if (tableLayout == null) {
            Log.e("HrPayslipstatusFragment", "TableLayout is null, cannot add row");
            return;
        }

        TableRow tableRow = new TableRow(getContext());

        // Increment the row counter for each new row
        rowCounter++;

        // Create TextViews for each column with layout parameters
        TextView serialNoTextView = createTextView(String.valueOf(rowCounter), 1f, Gravity.CENTER);
        TextView userIdTextView = createTextView(hrpayslipRequest.getUserId(), 1f, Gravity.CENTER);
        TextView nameTextView = createTextView(hrpayslipRequest.getName(), 1f, Gravity.CENTER);
        TextView dateTextView = createTextView(hrpayslipRequest.getdate(), 1f, Gravity.CENTER);

        // Create a LinearLayout to hold the approve and reject icons horizontally
        LinearLayout iconsLayout = new LinearLayout(getContext());
        iconsLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Add approve and reject icons
        ImageView approveIcon = createImageView(R.drawable.tick, 0.5f, Gravity.CENTER);
        ImageView rejectIcon = createImageView(R.drawable.cancel, 0.5f, Gravity.CENTER);

        // Set onClickListeners for the approve and reject icons
        approveIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the approve action
                updatePayslipStatus(hrpayslipRequest, "Approved");
            }
        });

        rejectIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the reject action
                updatePayslipStatus(hrpayslipRequest, "Rejected");
            }
        });

        // Add approve and reject icons to the icons layout
        iconsLayout.addView(approveIcon);
        iconsLayout.addView(rejectIcon);

        // Create TextView for "Generate Payroll" with layout parameters
        TextView generatePayrollTextView = createTextView("Generate Payroll", 1f, Gravity.CENTER);

        // Set onClickListener for the "Generate Payroll" TextView
        generatePayrollTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click event to navigate to PayrollFragment
                navigateToPayrollFragment(hrpayslipRequest.getUserId(), hrpayslipRequest.getName());
            }
        });

        // Add TextViews and icons layout to the TableRow
        tableRow.addView(serialNoTextView);
        tableRow.addView(userIdTextView);
        tableRow.addView(nameTextView);
        tableRow.addView(dateTextView);
        tableRow.addView(generatePayrollTextView);
        tableRow.addView(iconsLayout);

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

    private void navigateToPayrollFragment(String userId, String name) {
        // Create instance of PayrollFragment
        PayrollFragment payrollFragment = new PayrollFragment();

        // Pass userId and name as arguments to PayrollFragment
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("name", name);
        payrollFragment.setArguments(args);

        // Get FragmentManager
        FragmentManager fragmentManager = getParentFragmentManager();

        // Begin FragmentTransaction
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace current fragment with PayrollFragment
        fragmentTransaction.replace(R.id.fragment_container, payrollFragment);

        // Add transaction to back stack (optional)
        fragmentTransaction.addToBackStack(null);

        // Commit the transaction
        fragmentTransaction.commit();
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

        // Set text color to yellow if it's "Generate Payroll", otherwise set it to black
        if ("Generate Payroll".equals(text)) {
            textView.setTextColor(getResources().getColor(R.color.yellow)); // Change to yellow color
        } else {
            textView.setTextColor(getResources().getColor(R.color.black));
        }

        // Set layout parameters with weight and gravity
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        params.width = 100; // Set width to 100dp
        params.gravity = gravity;
        textView.setLayoutParams(params);

        // Set maximum width and ellipsize if needed
        int maxTextLength = 22; // You can adjust this based on your preference

        if (text != null && text.length() > maxTextLength) {
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
            textView.setText(text != null ? text : "");  // Set the text to an empty string if it's null
        }

        return textView;
    }

    private void addTextLine(TextView textView, String line) {
        // Add a new line to the TextView
        textView.append(line);
        textView.append("\n");
    }

    private void updatePayslipStatus(PayslipRequest hrpayslipRequest, String newStatus) {
        DatabaseReference payslipRef = hrpayslipRequest.getRef();

        Log.d("UpdatePayslipStatus", "Payslip ID: " + hrpayslipRequest.getPayslipId());
        Log.d("UpdatePayslipStatus", "Reference: " + payslipRef.toString());
        Log.d("UpdatePayslipStatus", "New Status: " + newStatus);

        // Create a Map to update only the 'status' field
        Map<String, Object> updateStatus = new HashMap<>();
        updateStatus.put("status", newStatus);

        // Update the od status in Firebase Database
        payslipRef.updateChildren(updateStatus)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Update successful
                        // You can perform any additional actions here
                        Log.d("UpdatePayslipStatus", "Status updated successfully");

                        // Notify the user about the status change
                        Toast.makeText(getContext(), "Payslip status updated to: " + newStatus, Toast.LENGTH_SHORT).show();

                        // Optionally, you can refresh the UI or perform other actions after the status update
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Log.e("UpdatePayslipStatus", "Error updating status", e);
                        // Notify the user about the failure
                        Toast.makeText(getContext(), "Failed to update od status", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
