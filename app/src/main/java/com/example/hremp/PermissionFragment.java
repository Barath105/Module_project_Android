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
import androidx.fragment.app.FragmentManager;

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

public class PermissionFragment extends Fragment {

    private DatabaseReference databaseReference;
    private DatabaseReference hrPermissionReference;

    private AutoCompleteTextView autoCompleteTextViewSession;
    private TextInputEditText editTextDate;
    private TextInputEditText editTextReason;
    private TextInputEditText editTextFileName;

    // Set userId as static "1001"
    private static final String userId = "1001";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permission, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        hrPermissionReference = databaseReference.child("adminhr").child("hrpermission").child("1");

        String[] types = new String[]{"FN", "AN"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.drop_down_item,
                types
        );

        autoCompleteTextViewSession = view.findViewById(R.id.filled_exposed);
        autoCompleteTextViewSession.setAdapter(adapter);

        autoCompleteTextViewSession.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(requireContext(), autoCompleteTextViewSession.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        editTextDate = view.findViewById(R.id.editTextDate);
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
                requestPermission();
            }
        });

        Button pendingButton = view.findViewById(R.id.pending);
        pendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new PermissionPending())
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
                        .replace(R.id.fragment_container, new PermissionHistory())
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private void setupTextWatchers() {
        editTextDate.setFocusable(false);
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(editTextDate);
            }
        });

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
                },
                year, month, day
        );

        datePickerDialog.show();
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

    private void requestPermission() {
        String session = autoCompleteTextViewSession.getText().toString();
        String date = editTextDate.getText().toString();
        String reason = editTextReason.getText().toString();
        String fileName = editTextFileName.getText().toString();

        if (session.isEmpty() || date.isEmpty() || reason.isEmpty() || fileName.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference idCounterRef = hrPermissionReference.child("idCounter");

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

            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                } else if (committed) {
                    long newId;
                    if (currentData.getValue() == null) {
                        newId = 1;
                    } else {
                        long currentId = (long) currentData.getValue();
                        newId = currentId + 1;
                    }

                    DatabaseReference newPermissionReference = databaseReference.child("adminhr").child("hrpermission").child(String.valueOf(newId));

                    savePermissionRequestToFirebase(String.valueOf(newId), session, date, reason, fileName, newPermissionReference);
                } else {
                    Toast.makeText(requireContext(), "Failed to generate ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void savePermissionRequestToFirebase(String permissionRequestId, String session, String date, String reason, String fileName, DatabaseReference permissionReference) {
        PermissionRequest permissionRequest = new PermissionRequest(userId, session, date, reason, fileName);

        permissionReference.setValue(permissionRequest);

        clearFormFields();

        Toast.makeText(requireContext(), "Permission request submitted successfully", Toast.LENGTH_SHORT).show();
    }

    private void clearFormFields() {
        autoCompleteTextViewSession.setText("");
        editTextDate.setText("");
        editTextReason.setText("");
        editTextFileName.setText("");
    }

    public static class PermissionRequest {
        public String userId;
        public String session;
        public String date;
        public String reason;
        public String fileName;
        public String status;
        public String permissionId;

        public PermissionRequest() {
            this.status = "Pending";
        }

        public PermissionRequest(String userId, String session, String date, String reason, String fileName) {
            this.userId = userId;
            this.session = session;
            this.date = date;
            this.reason = reason;
            this.fileName = fileName;
            this.status = "Pending";
            this.permissionId = generatePermissionId(userId);
        }

        private String generatePermissionId(String userId) {
            long timestamp = System.currentTimeMillis();
            return userId + "_" + timestamp;
        }
    }
}
