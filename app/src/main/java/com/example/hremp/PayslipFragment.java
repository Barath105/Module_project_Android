package com.example.hremp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.Calendar;

public class PayslipFragment extends Fragment {

    private DatabaseReference databaseReference;
    private DatabaseReference hrPayslipReference;
    private TextInputEditText editTextUserId;
    private TextInputEditText editTextName;
    private TextInputEditText editTextDate;

    // Declare class-level fields for userId, name, and date
    private String userId;
    private String name;
    private String date;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_payslip, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        hrPayslipReference = databaseReference.child("adminhr").child("hrpayslip").child("1");

        editTextUserId = rootView.findViewById(R.id.editTextUserId);
        editTextName = rootView.findViewById(R.id.editTextName);
        editTextDate = rootView.findViewById(R.id.editTextDate);

        // Find the TextInputLayout and TextInputEditText in your fragment layout
        TextInputLayout textInputDate = rootView.findViewById(R.id.textInputDate);
        editTextDate = rootView.findViewById(R.id.editTextDate);

        // Set clickable to false on TextInputLayout
        textInputDate.setClickable(false);

        // Set up the text watcher for the "Start Date" field
        setupTextWatchers();

        Button buttonRequest = rootView.findViewById(R.id.buttonRequest);
        buttonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPayslip();
            }
        });

        // Click Pending Button
        Button pendingButton = rootView.findViewById(R.id.pending);
        pendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Replace the current fragment with the PayslipPending fragment
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new PayslipPending())
                        .addToBackStack(null)  // Add to the back stack to allow back navigation
                        .commit();
            }
        });

        // Click History Button
        Button historyButton = rootView.findViewById(R.id.history);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Replace the current fragment with the PayslipHistory fragment
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new PayslipHistory())
                        .addToBackStack(null)  // Add to the back stack to allow back navigation
                        .commit();
            }
        });

        return rootView;
    }

    private void setupTextWatchers() {
        // "Start Date" field - opens the calendar directly
        editTextDate.setFocusable(false);
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    editTextDate.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void requestPayslip() {
        // Remove the declaration of userId, name, and date here

        if (editTextUserId == null || editTextName == null || editTextDate == null) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Assign values to class-level fields
        userId = editTextUserId.getText().toString();
        name = editTextName.getText().toString();
        date = editTextDate.getText().toString();

        DatabaseReference idCounterRef = hrPayslipReference.child("idCounter");

        idCounterRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) {
                    currentData.setValue(1);
                } else {
                    long idCounter = (long) currentData.getValue();
                    currentData.setValue(idCounter + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                handleTransactionResult(error, committed, currentData);
            }
        });
    }

    private void handleTransactionResult(DatabaseError error, boolean committed, DataSnapshot currentData) {
        if (error != null) {
            Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        } else if (committed) {
            long newId;
            if (currentData == null || currentData.getValue() == null) {
                newId = 1;
            } else {
                long currentId = (long) currentData.getValue();
                newId = currentId + 1;
            }

            DatabaseReference newPayslipReference = databaseReference.child("adminhr").child("hrpayslip").child(String.valueOf(newId));

            savePayslipRequestToFirebase(String.valueOf(newId), userId, name, date, newPayslipReference);
        } else {
            Toast.makeText(requireContext(), "Failed to generate ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePayslipRequestToFirebase(String payslipRequestId, String userId, String name, String date, DatabaseReference payslipReference) {
        PayslipRequest payslipRequest = new PayslipRequest(userId, name, date);
        payslipReference.setValue(payslipRequest);
        clearFormFields();
        Toast.makeText(requireContext(), "Payslip request submitted successfully", Toast.LENGTH_SHORT).show();
    }

    private void clearFormFields() {
        editTextUserId.setText("");
        editTextName.setText("");
        editTextDate.setText("");
    }

    public static class PayslipRequest {
        public String userId;
        public String name;
        public String date;
        public String status;

        public PayslipRequest() {
            // Default constructor required for Firebase
            this.status = "Pending";
        }

        public PayslipRequest(String userId, String name, String date) {
            this.userId = userId;
            this.name = name;
            this.date = date;
            this.status = "Pending";
        }
    }
}
