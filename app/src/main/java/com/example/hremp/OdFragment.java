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
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class OdFragment extends Fragment {

    private DatabaseReference databaseReference;
    private DatabaseReference hrOdReference;

    private TextInputEditText editTextStartDate;
    private TextInputEditText editTextEndDate;
    private TextInputEditText editTextDays;
    private TextInputEditText editTextReason;
    private TextInputEditText editTextFileName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_od, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        hrOdReference = databaseReference.child("adminhr").child("hrod");

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
                requestOd();
            }
        });

        Button pendingButton = view.findViewById(R.id.pending);
        pendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new OdPending())
                        .addToBackStack(null)
                        .commit();
            }
        });

        MaterialButton historyButton = view.findViewById(R.id.history);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new OdHistory())
                        .addToBackStack(null)
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

    private void requestOd() {
        String startDate = editTextStartDate.getText().toString();
        String endDate = editTextEndDate.getText().toString();
        String days = editTextDays.getText().toString();
        String reason = editTextReason.getText().toString();
        String fileName = editTextFileName.getText().toString();

        if (startDate.isEmpty() || endDate.isEmpty() || days.isEmpty() || reason.isEmpty() || fileName.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference idCounterRef = hrOdReference.child("idCounter");

        idCounterRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                long idCounter = (currentData.getValue() != null) ? (long) currentData.getValue() : 0;
                currentData.setValue(idCounter + 1);
                return Transaction.success(currentData);
            }

            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null || !committed || currentData == null) {
                    Toast.makeText(requireContext(), "Failed to generate ID", Toast.LENGTH_SHORT).show();
                } else {
                    long newId = (currentData.getValue() != null) ? (long) currentData.getValue() : 0;
                    DatabaseReference newOdReference = hrOdReference.child(String.valueOf(newId));
                    saveOdRequestToFirebase(startDate, endDate, days, reason, fileName, newOdReference);
                }
            }
        });
    }

    private void saveOdRequestToFirebase(String startDate, String endDate, String days, String reason, String fileName, DatabaseReference odReference) {
        OdRequest odRequest = new OdRequest("1001", startDate, endDate, days, reason, fileName); // Set userId to "1001"
        odReference.setValue(odRequest);
        clearFormFields();
        Toast.makeText(requireContext(), "Od request submitted successfully", Toast.LENGTH_SHORT).show();
    }

    private void clearFormFields() {
        editTextStartDate.setText("");
        editTextEndDate.setText("");
        editTextDays.setText("");
        editTextReason.setText("");
        editTextFileName.setText("");
    }

    public static class OdRequest {
        public String userId;
        public String startDate;
        public String endDate;
        public String days;
        public String reason;
        public String fileName;
        public String status;

        public OdRequest() {
            this.status = "Pending";
        }

        public OdRequest(String userId, String startDate, String endDate, String days, String reason, String fileName) {
            this.userId = userId; // Set userId here
            this.startDate = startDate;
            this.endDate = endDate;
            this.days = days;
            this.reason = reason;
            this.fileName = fileName;
            this.status = "Pending";
        }
    }
}