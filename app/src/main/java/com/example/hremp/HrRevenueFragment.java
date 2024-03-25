package com.example.hremp;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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

import java.util.Objects;

public class HrRevenueFragment extends Fragment {

    private DatabaseReference databaseRef;
    private TableLayout tableLayout;
    private TextView textViewTotalBudget;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hr_fragment_hr_revenue, container, false);
        tableLayout = rootView.findViewById(R.id.tableLayout);
        textViewTotalBudget = rootView.findViewById(R.id.textViewTotalRevenueValue);
        databaseRef = FirebaseDatabase.getInstance().getReference().child("adminhr/hrproduct");
        fetchProjectDetails();
        fetchAndDisplayTotalBudget();
        return rootView;


    }


    private void fetchAndDisplayTotalBudget() {
        // Retrieve totalBudget values from Firebase
        FirebaseDatabase.getInstance().getReference().child("adminhr/hrproduct")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        double totalBudget = 0; // Initialize total budget

                        // Loop through each hrproduct node
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Check if the totalBudget field exists
                            if (snapshot.hasChild("totalBudget")) {
                                // Get the totalBudget for the current hrproduct node
                                String budgetStr = snapshot.child("totalBudget").getValue(String.class);
                                if (budgetStr != null && !budgetStr.isEmpty()) {
                                    // Convert the budget string to double
                                    try {
                                        double budget = Double.parseDouble(budgetStr);
                                        // Add the budget to the totalBudget sum
                                        totalBudget += budget;
                                    } catch (NumberFormatException e) {
                                        // Handle conversion errors
                                        Log.e("BudgetConversion", "Error converting budget string to double: " + budgetStr);
                                    }
                                }
                            }
                        }

                        // Set the totalBudget sum to TextView
                        textViewTotalBudget.setText(String.valueOf(totalBudget));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    private void fetchProjectDetails() {
        // Attach a listener to read the data
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through all nodes under "hrproduct"
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    // Get the values of projectID, projectDescription, and totalBudget
                    String projectID = Objects.requireNonNull(productSnapshot.child("projectID").getValue()).toString();
                    String projectDescription = Objects.requireNonNull(productSnapshot.child("projectDescription").getValue()).toString();
                    double totalBudget = Double.parseDouble(Objects.requireNonNull(productSnapshot.child("totalBudget").getValue()).toString());

                    // Display the fetched data in the table
                    addRowToTable(projectID, projectDescription, totalBudget);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private int rowCount = 0; // Variable to keep track of row count

    private void addRowToTable(String projectID, String projectDescription, double totalBudget) {
        // Increment row count
        rowCount++;

        // Create a new row
        TableRow row = new TableRow(requireContext());
        // Create TextViews for each column
        TextView textViewSerialNumber = createTextView(String.valueOf(rowCount));
        TextView textViewProjectID = createTextView(projectID);
        TextView textViewProjectDescription = createTextView(projectDescription);
        TextView textViewTotalBudget = createTextView(String.valueOf(totalBudget));
        // Add TextViews to the row
        row.addView(textViewSerialNumber);
        row.addView(textViewProjectID);
        row.addView(textViewProjectDescription);
        row.addView(textViewTotalBudget);
        // Add the row to the TableLayout
        tableLayout.addView(row);
    }



    private TextView createTextView(String text) {
        TextView textView = new TextView(requireContext());
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        textView.setPadding(22, 100, 22, 22);
        textView.setTextColor(getResources().getColor(R.color.black)); // Set text color to black
        textView.setGravity(Gravity.CENTER); // Set gravity to center

        // Format the text to wrap after 22 characters
        String[] words = text.split(" ");
        StringBuilder formattedText = new StringBuilder();
        int wordsCount = 0;
        for (String word : words) {
            if (wordsCount + word.length() > 22) { // If adding this word exceeds 22 characters
                formattedText.append("\n"); // Start a new line
                wordsCount = 0; // Reset word count for the new line
            }
            formattedText.append(word).append(" ");
            wordsCount += word.length() + 1; // Add word length and space to the word count
        }
        textView.setText(formattedText.toString().trim()); // Set the formatted text



        return textView;
    }

}
