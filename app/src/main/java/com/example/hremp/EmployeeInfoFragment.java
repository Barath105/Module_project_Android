package com.example.hremp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EmployeeInfoFragment extends Fragment {

    private TableLayout tableLayout;
    private TextView editTextView;
    private int rowCount = 1; // Counter for serial numbers

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_employee_info, container, false);
        tableLayout = rootView.findViewById(R.id.tableLayout);

        // Fetch userinfo from Firebase
        fetchUserInfoFromFirebase();

        return rootView;
    }

    private void fetchUserInfoFromFirebase() {
        FirebaseDatabase.getInstance().getReference().child("adminhr/userinfo")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String userId = userSnapshot.getKey(); // Get the user ID (unique key)
                            String name = userSnapshot.child("name").getValue(String.class);
                            String email = userSnapshot.child("email").getValue(String.class);
                            String phone = userSnapshot.child("phone").getValue(String.class);
                            String dob = userSnapshot.child("dob").getValue(String.class);
                            String gender = userSnapshot.child("gender").getValue(String.class);
                            String role = userSnapshot.child("role").getValue(String.class);
                            String address = userSnapshot.child("address").getValue(String.class);

                            TableRow row = new TableRow(requireContext());

                            // Add spacing between rows
                            TableRow.LayoutParams params = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(5, 5, 5, getResources().getDimensionPixelSize(R.dimen.row_spacing)); // Adjust spacing as needed
                            row.setLayoutParams(params);

                            addTextViewToRow(row, String.valueOf(rowCount), true); // Incremented rowCount used here
                            addTextViewToRow(row, userId, false);
                            addTextViewToRow(row, name, false);
                            addTextViewToRow(row, email, false);
                            addTextViewToRow(row, phone, false);
                            addTextViewToRow(row, dob, false);
                            addTextViewToRow(row, gender, false);
                            addTextViewToRow(row, role, false);
                            addTextViewToRow(row, address, false);

                            // Add Edit column
                            editTextView = createActionButton("Edit", R.color.yellow);
                            editTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Open EditUserFragment with data of the selected row
                                    EditUserFragment editUserFragment = new EditUserFragment();

                                    // Pass data to the EditUserFragment
                                    Bundle bundle = new Bundle();
                                    bundle.putString("userId", userId);
                                    bundle.putString("name", name);
                                    bundle.putString("email", email);
                                    bundle.putString("phone", phone);
                                    bundle.putString("dob", dob);
                                    bundle.putString("gender", gender);
                                    bundle.putString("role", role);
                                    bundle.putString("address", address);
                                    editUserFragment.setArguments(bundle);

                                    FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                    transaction.replace(R.id.fragment_container, editUserFragment);
                                    transaction.addToBackStack(null);
                                    transaction.commit();
                                }
                            });
                            row.addView(editTextView);

                            // Add Terminate column
                            TextView terminateTextView = createActionButton("Terminate", R.color.Red);
                            terminateTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // Handle termination action
                                    confirmTermination(userId);
                                }
                            });
                            row.addView(terminateTextView);

                            tableLayout.addView(row);
                            rowCount++; // Increment rowCount here, after adding the row
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                    }
                });
    }

    private TextView createActionButton(String text, int color) {
        TextView textView = new TextView(requireContext());
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        textView.setText(text);
        textView.setTextColor(getResources().getColor(color));
        textView.setPadding(10, 100, 10, 10);
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

    private void addTextViewToRow(TableRow row, String text, boolean isSerialNumber) {
        TextView textView = new TextView(requireContext());
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        textView.setText(text);
        textView.setPadding(22, 22, 22, 22);
        textView.setTextColor(getResources().getColor(R.color.black)); // Set text color to black
        textView.setGravity(Gravity.CENTER); // Set gravity to center

        // Add the TextView to the row
        row.addView(textView);

        // Increment serial number only if it's not already a serial number
    }

    private void confirmTermination(String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Terminate user?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                terminateUser(userId);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void terminateUser(String userId) {
        FirebaseDatabase.getInstance().getReference().child("adminhr/userinfo").child(userId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Data successfully deleted
                        Log.d("TAG", "User deleted successfully");
                        Toast.makeText(requireContext(), "User terminated successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle error
                        Log.e("TAG", "Error deleting user: " + e.getMessage());
                    }
                });
    }
}
