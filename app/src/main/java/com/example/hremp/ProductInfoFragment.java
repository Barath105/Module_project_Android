package com.example.hremp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.view.GestureDetector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProductInfoFragment extends Fragment {

    private DatabaseReference databaseReference;
    private TableLayout tableLayout;

    private int rowCounter = 0; // Add this variable

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hr_fragment_product_info, container, false);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference().child("adminhr").child("hrproduct");

        // Initialize TableLayout
        tableLayout = rootView.findViewById(R.id.tableLayout);

        fetchPendingProductDetails();

        return rootView;
    }

    private void fetchPendingProductDetails() {
        // Attach a listener to read the data at our database reference
        databaseReference.orderByChild("status").equalTo("Pending").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear existing rows from the table

                // Check if there is no data
                if (!dataSnapshot.exists()) {
                    addNoDataRow();
                    return;
                }

                // Iterate through each child node
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get product details
                    String projectId = snapshot.getKey(); // Get the project ID

                    // Check if a row with this project ID already exists
                    if (rowExists(projectId)) {
                        continue; // Skip adding duplicate row
                    }

                    String projectName = snapshot.child("projectName").getValue(String.class);
                    String projectDescription = snapshot.child("projectDescription").getValue(String.class);
                    String projectTotalAmount = snapshot.child("totalBudget").getValue(String.class);
                    String amountPaid = snapshot.child("advancePaid").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String remainingAmount = snapshot.child("remainingAmount").getValue(String.class); // Fetch remaining amount

                    // Check for null values
                    if (projectId != null && projectName != null && projectDescription != null
                            && projectTotalAmount != null && amountPaid != null && status != null && remainingAmount != null) {
                        // Calculate pending amount
                        double totalAmount = Double.parseDouble(projectTotalAmount);
                        double paidAmount = Double.parseDouble(amountPaid);
                        double pendingAmount = totalAmount - paidAmount;

                        // Add row to the table with fetched data
                        addRowToTable(projectId, projectName, projectDescription, projectTotalAmount, amountPaid, String.valueOf(pendingAmount), remainingAmount, status); // Pass remainingAmount to addRowToTable method
                    } else {
                        // Handle null values if needed
                        Log.e("ProductInfoFragment", "One or more values are null");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Log.e("ProductInfoFragment", "Error fetching product details: " + databaseError.getMessage());
            }
        });
    }

    private void addNoDataRow() {
        TableRow row = new TableRow(requireContext());
        TextView textView = new TextView(requireContext());
        textView.setText("No data found");
        textView.setTextColor(getResources().getColor(R.color.black)); // Set text color to black
        textView.setGravity(Gravity.CENTER); // Set gravity to center

        // Add styling if needed

        row.addView(textView);
        tableLayout.addView(row);
    }

    // Method to check if a row with the given project ID already exists in the table
    private boolean rowExists(String projectId) {
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View child = tableLayout.getChildAt(i);
            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;
                TextView textViewProjectId = (TextView) row.getChildAt(1); // Assuming project ID is the second column
                String existingProjectId = textViewProjectId.getText().toString();
                if (existingProjectId.equals(projectId)) {
                    return true; // Row with the same project ID exists
                }
            }
        }
        return false; // No row with the given project ID exists
    }


    private void addRowToTable(String projectId, String projectName, String projectDescription,
                               String projectTotalAmount, String amountPaid, String pendingAmount, String remainingAmount, String status) {
        // Create a new row
        TableRow row = new TableRow(requireContext()); // Using requireContext() to get the context of the fragment
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(layoutParams);
        layoutParams.setMargins(0, 0, 0, dpToPx(50)); // Set bottom margin to 50dp
        rowCounter++;

        // Create TextViews for each column
        TextView serialNoTextView = createTextView(String.valueOf(rowCounter));
        //TextView textViewProjectId = createTextView(projectId);
        TextView textViewProjectName = createTextView(projectName);
        TextView textViewProjectDescription = createTextView(projectDescription);
        TextView textViewTotalAmount = createTextView(projectTotalAmount);
        TextView textViewAmountPaid = createTextView(amountPaid);
        TextView textViewRemainingAmount = createTextView(remainingAmount); // Display remainingAmount
        TextView markCompleteTextView = createClickableTextView("Mark as Complete", projectId);

        // Add double-tap listener for updating remaining amount
        textViewRemainingAmount.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    // Handle double tap
                    showUpdateRemainingAmountDialog(projectId);
                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        // Add TextViews to the row
        row.addView(serialNoTextView);
        //row.addView(textViewProjectId);
        row.addView(textViewProjectName);
        row.addView(textViewProjectDescription);
        row.addView(textViewTotalAmount);
        row.addView(textViewAmountPaid);
        row.addView(textViewRemainingAmount);
        row.addView(markCompleteTextView); // Add button to the row

        // Add the row to the table layout
        tableLayout.addView(row);
    }

    private TextView createClickableTextView(String text, final String projectId) {
        TextView textView = new TextView(requireContext());
        textView.setText(text != null ? text : "");
        textView.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8)); // Set padding in pixels
        textView.setTextColor(getResources().getColor(R.color.Green)); // Set text color to white
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle text click (e.g., mark the project as complete)
                showRemainingAmountDialog(projectId);
            }
        });
        return textView;
    }

    private void showRemainingAmountDialog(final String projectId) {
        // Fetch remaining amount from Firebase
        DatabaseReference projectRef = databaseReference.child(projectId);
        projectRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get the remaining amount
                String remainingAmount = dataSnapshot.child("remainingAmount").getValue(String.class);

                // Convert remainingAmount to double for comparison
                double remaining = Double.parseDouble(remainingAmount != null ? remainingAmount : "0");

                if (remaining == 0) {
                    // Show dialog to confirm pending amount
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setMessage("Are you sure to mark as complete?");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Update status to "Completed"
                            updateStatusToCompleted(projectId);
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.create().show();
                } else {
                    // Show error dialog if remaining amount is not zero
                    AlertDialog.Builder errorBuilder = new AlertDialog.Builder(requireContext());
                    errorBuilder.setMessage("Attention!Pending Amount");
                    errorBuilder.setPositiveButton("OK", null);
                    errorBuilder.create().show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
                Log.e("ProductInfoFragment", "Error fetching remaining amount: " + databaseError.getMessage());
            }
        });
    }


    private void updateStatusToCompleted(String projectId) {
        // Update status to "Completed" in Firebase
        databaseReference.child(projectId).child("status").setValue("Completed");
        // You might want to add success/error handling here
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(requireContext()); // Using requireContext() to get the context of the fragment
        textView.setText(text != null ? text : "");
        textView.setPadding(10, 100, 10, 10);
        textView.setTextColor(getResources().getColor(R.color.black)); // Set text color to black
        textView.setGravity(Gravity.CENTER); // Set gravity to center

        // Split text into lines with maximum 22 words per line
        String[] words = text.split(" ");
        StringBuilder formattedText = new StringBuilder();
        int wordsCount = 0;
        for (String word : words) {
            if (wordsCount + word.length() > 22) { // If adding this word exceeds 22 words
                formattedText.append("\n"); // Start a new line
                wordsCount = 0; // Reset word count for the new line
            }
            formattedText.append(word).append(" ");
            wordsCount += word.length() + 1; // Add word length and space to the word count
        }
        textView.setText(formattedText.toString().trim()); // Set the formatted text

        return textView;
    }

    // Convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    // Method to show dialog to update pending amount
    private void showUpdateRemainingAmountDialog(final String projectId) {
        // Show dialog to update pending amount
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Update Pending Amount");
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newRemainingAmount = input.getText().toString();
                // Update pending amount in Firebase
                updateRemainingAmount(projectId, newRemainingAmount);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    // Method to update pending amount in Firebase
    private void updateRemainingAmount(String projectId, String newupdateRemainingAmount) {
        // Update pending amount in Firebase
        databaseReference.child(projectId).child("remainingAmount").setValue(newupdateRemainingAmount, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    // Reload the fragment or fetch data again
                    fetchPendingProductDetails();
                } else {
                    // Handle error
                    Log.e("ProductInfoFragment", "Error updating remaining amount: " + error.getMessage());
                }
            }
        });
    }

}
