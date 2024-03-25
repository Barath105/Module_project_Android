package com.example.hremp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.view.Gravity;


public class ClientInfoFragment extends Fragment {

    private DatabaseReference databaseRef;
    private TableLayout tableLayout;
    private int rowCount = 0; // Counter for serial number

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hr_fragment_client_info, container, false);
        tableLayout = rootView.findViewById(R.id.tableLayout);

        // Initialize database reference to adminhr/userinfo path
        databaseRef = FirebaseDatabase.getInstance().getReference().child("adminhr").child("hrproduct");

        // Fetch client data
        fetchClientData();

        return rootView;
    }

    private void fetchClientData() {
        // Attach a listener to read the data
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through all numbered nodes (e.g., 1, 2, 3, ...)
                for (DataSnapshot clientSnapshot : dataSnapshot.getChildren()) {
                    // Get client data from each node
                    String clientName = clientSnapshot.child("clientName").getValue(String.class);
                    String email = clientSnapshot.child("email").getValue(String.class);
                    String phoneNumber = clientSnapshot.child("phoneNumber").getValue(String.class);

                    // Add client data to the table with serial number
                    addRowToTable(clientName, email, phoneNumber);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void addRowToTable(String clientName, String email, String phoneNumber) {
        // Increment row count for serial number
        rowCount++;

        // Create a new row
        TableRow row = new TableRow(requireContext());

        // Create TextViews for each column
        TextView textViewSerialNumber = createTextView(String.valueOf(rowCount));
        TextView textViewClientName = createTextView(clientName);
        TextView textViewEmail = createTextView(email);
        TextView textViewPhoneNumber = createTextView(phoneNumber);

        // Add TextViews to the row
        row.addView(textViewSerialNumber);
        row.addView(textViewClientName);
        row.addView(textViewEmail);
        row.addView(textViewPhoneNumber);

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
        textView.setText(text);



       //added
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
}
