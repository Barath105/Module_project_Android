package com.example.hremp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import androidx.fragment.app.FragmentManager;

public class LeaveFragment extends Fragment {

    private DatabaseReference databaseReference;
    private DatabaseReference hrLeaveReference;

    private AutoCompleteTextView autoCompleteTextViewLeaveType;
    private TextInputEditText editTextStartDate;
    private TextInputEditText editTextEndDate;
    private TextInputEditText editTextDays;
    private TextInputEditText editTextReason;
    private TextInputEditText editTextFileName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leave, container, false);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference();
        hrLeaveReference = databaseReference.child("adminhr").child("hrleave").child("1");

        String[] types = new String[]{"Personal Leave", "Sick Leave", "Casual Leave"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.drop_down_item,
                types
        );

        autoCompleteTextViewLeaveType = view.findViewById(R.id.filled_exposed);
        autoCompleteTextViewLeaveType.setAdapter(adapter);

        autoCompleteTextViewLeaveType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(requireContext(), autoCompleteTextViewLeaveType.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        editTextStartDate = view.findViewById(R.id.editTextStartDate);
        editTextEndDate = view.findViewById(R.id.editTextEndDate);
        editTextDays = view.findViewById(R.id.editTextDays);
        editTextReason = view.findViewById(R.id.editTextReason);
        editTextFileName = view.findViewById(R.id.editTextFileName);

        setupTextWatchers();

        Button btnUploadFile = view.findViewById(R.id.btnUploadFile);
        btnUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFilePicker();
            }
        });

        Button buttonRequest = view.findViewById(R.id.buttonRequest);
        buttonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLeave();
            }
        });

        // Add the following code for "Pending" button click
        Button pendingButton = view.findViewById(R.id.pending);
        pendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Replace the current fragment with the LeavePending fragment
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new LeavePending())
                        .addToBackStack(null)  // Add to the back stack to allow back navigation
                        .commit();
            }
        });

        // Click History Button
        MaterialButton historyButton = view.findViewById(R.id.history);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Replace the current fragment with the LeaveHistory fragment
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new LeaveHistory())  // Replace with the actual LeaveHistory fragment class
                        .addToBackStack(null)  // Add to the back stack to allow back navigation
                        .commit();
            }
        });

        return view;
    }

    private void setupTextWatchers() {
        editTextStartDate.setFocusable(false);
        editTextStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(editTextStartDate);
            }
        });

        editTextEndDate.setFocusable(false);
        editTextEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(editTextEndDate);
            }
        });

        editTextDays.setFocusable(false);
        editTextDays.setOnClickListener(null);

        editTextFileName.setFocusable(false);
        editTextFileName.setOnClickListener(null);
    }

    private void showDatePickerDialog(final TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    editText.setText(selectedDate);
                    updateDays();
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void updateDays() {
        String startDateString = editTextStartDate.getText().toString();
        String endDateString = editTextEndDate.getText().toString();

        if (!startDateString.isEmpty() && !endDateString.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                Date startDate = dateFormat.parse(startDateString);
                Date endDate = dateFormat.parse(endDateString);

                long differenceMillis = endDate.getTime() - startDate.getTime();
                int daysDifference = (int) (differenceMillis / (24 * 60 * 60 * 1000));

                editTextDays.setText(String.valueOf(daysDifference));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            editTextDays.setText("");
        }
    }

    public void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == getActivity().RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            String selectedFileName = getFileName(selectedFileUri);

            if (editTextFileName != null) {
                editTextFileName.setText(selectedFileName);
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void requestLeave() {
        String leaveType = autoCompleteTextViewLeaveType.getText().toString();
        String startDate = editTextStartDate.getText().toString();
        String endDate = editTextEndDate.getText().toString();
        String days = editTextDays.getText().toString();
        String reason = editTextReason.getText().toString();
        String fileName = editTextFileName.getText().toString();

        if (leaveType.isEmpty() || startDate.isEmpty() ||
                endDate.isEmpty() || days.isEmpty() || reason.isEmpty() || fileName.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference idCounterRef = hrLeaveReference.child("idCounter");

        idCounterRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                if (currentData.getValue() == null) {
                    // Set the default value if the node doesn't exist
                    currentData.setValue(1);
                } else {
                    long idCounter = (long) currentData.getValue();
                    currentData.setValue(idCounter + 1);
                }
                return Transaction.success(currentData);
            }

            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    // Handle the error
                    Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                } else if (committed) {
                    long newId;
                    if (currentData.getValue() == null) {
                        // If there's no existing data, set newId to 1
                        newId = 1;
                    } else {
                        // If there's existing data, get the current ID and increment it
                        long currentId = (long) currentData.getValue();
                        newId = currentId + 1;
                    }

                    // Generate dynamic node ID based on the incremented count
                    DatabaseReference newLeaveReference = databaseReference.child("adminhr").child("hrleave").child(String.valueOf(newId));

                    // Save leave request to the new ID
                    saveLeaveRequestToFirebase(String.valueOf(newId), leaveType, startDate, endDate, days, reason, fileName, newLeaveReference);
                } else {
                    Toast.makeText(requireContext(), "Failed to generate ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveLeaveRequestToFirebase(String leaveRequestId, String leaveType, String startDate,
                                            String endDate, String days, String reason, String fileName, DatabaseReference leaveReference) {
        // Create a new instance of LeaveRequest with the updated constructor
        LeaveRequest leaveRequest = new LeaveRequest(leaveType, startDate, endDate, days, reason, fileName);

        // Save leave request to Firebase
        leaveReference.setValue(leaveRequest);

        // Clear form fields after submission
        clearFormFields();

        // Show a success message
        Toast.makeText(requireContext(), "Leave request submitted successfully", Toast.LENGTH_SHORT).show();
    }

    private void clearFormFields() {
        autoCompleteTextViewLeaveType.setText("");
        editTextStartDate.setText("");
        editTextEndDate.setText("");
        editTextDays.setText("");
        editTextReason.setText("");
        editTextFileName.setText("");
    }

    public static class LeaveRequest {
        public String userId;
        public String leaveType;
        public String startDate;
        public String endDate;
        public String days;
        public String reason;
        public String fileName;
        public String status;
        public String leaveId;  // New field for unique leave ID

        public LeaveRequest() {
            // Default constructor required for calls to DataSnapshot.getValue(LeaveRequest.class)
            this.status = "Pending";
            this.userId="1001"; // Set userId to static value "1001"
        }

        public LeaveRequest(String leaveType, String startDate, String endDate, String days, String reason, String fileName) {
            this.userId = "1001"; // Set userId to static value "1001"
            this.leaveType = leaveType;
            this.startDate = startDate;
            this.endDate = endDate;
            this.days = days;
            this.reason = reason;
            this.fileName = fileName;
            this.status = "Pending";
            this.leaveId = generateLeaveId(userId);
        }

        private String generateLeaveId(String userId) {
            long timestamp = System.currentTimeMillis();
            return userId + "_" + timestamp;
        }
    }
}
