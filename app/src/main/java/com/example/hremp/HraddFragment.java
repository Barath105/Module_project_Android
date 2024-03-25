package com.example.hremp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import java.util.Calendar;
import java.util.HashMap;

public class HraddFragment extends Fragment {
    private EditText nameEditText, userIdEditText, dobEditText, genderEditText, emailEditText, roleEditText, phoneEditText, addressEditText;
    private Button addButton;
    private DatabaseReference databaseReference;
    private TextInputEditText editTextdob;


    private AutoCompleteTextView autoCompleteTextViewGenderType;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hr_fragment_add, container, false);

        // Initialize Firebase components
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("adminhr").child("userinfo");

        TextInputLayout textInputdob = view.findViewById(R.id.textInputdob);
        editTextdob = view.findViewById(R.id.editTextdob);


        // Set clickable to false on TextInputLayout
        textInputdob.setClickable(false);

        // Set up the text watcher for the "Start Date" field
        setupTextWatchers();


        String[] types = new String[]{"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.drop_down_item,
                types
        );


        autoCompleteTextViewGenderType = view.findViewById(R.id.filled_exposed);
        autoCompleteTextViewGenderType.setAdapter(adapter);

        autoCompleteTextViewGenderType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(requireContext(), autoCompleteTextViewGenderType.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        // Initialize EditText fields
        nameEditText = view.findViewById(R.id.editTextname);
        userIdEditText = view.findViewById(R.id.editTextuserid);
        dobEditText = view.findViewById(R.id.editTextdob);
        emailEditText = view.findViewById(R.id.editTextmail);
        roleEditText = view.findViewById(R.id.editTextrole);
        phoneEditText = view.findViewById(R.id.editTextphone);
        addressEditText = view.findViewById(R.id.editTextaddress);
        addButton = view.findViewById(R.id.buttonRequest);

        // Set onClickListener for addButton
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserToFirebase();
            }
        });

        return view;
    }

    private void setupTextWatchers() {
        // "Start Date" field - opens the calendar directly
        editTextdob.setFocusable(false);
        editTextdob.setOnClickListener(new View.OnClickListener() {
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
                    editTextdob.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void saveUserToFirebase() {
        // Get input values
        String name = nameEditText.getText().toString().trim();
        String userId = userIdEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();
        String gender = autoCompleteTextViewGenderType.getText().toString().trim(); // Use AutoCompleteTextView for gender
        String email = emailEditText.getText().toString().trim();
        String role = roleEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        // Validate fields
        if (name.isEmpty() || userId.isEmpty() || dob.isEmpty() || gender.isEmpty() || email.isEmpty() || role.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            // Display error message if any field is empty
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get reference to the "adminhr/userinfo" node
        DatabaseReference userinfoRef = FirebaseDatabase.getInstance().getReference().child("adminhr").child("userinfo");

        // Retrieve the current maximum numeric value of nodes under "adminhr/userinfo"
        userinfoRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    count = Long.parseLong(snapshot.getKey());
                }

                // Increment the count to determine the numeric value for the key of the new node
                count++;

                // Create a new node under "userinfo" with the calculated count as the key
                DatabaseReference newUserRef = userinfoRef.child(String.valueOf(count));
                // Push generates a unique key based on timestamp

                // Get the unique key generated by push
                String uniqueKey = newUserRef.getKey();


                // Create a HashMap to store the user data
                HashMap<String, Object> userData = new HashMap<>();
                userData.put("uniqueKey", uniqueKey); // Store the unique key
                userData.put("name", name);
                userData.put("userId", userId);
                userData.put("dob", dob);
                userData.put("gender", gender);
                userData.put("email", email);
                userData.put("role", role);
                userData.put("phone", phone);
                userData.put("address", address);

                // Store the user data in the Firebase database under the generated key
                newUserRef.setValue(userData)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    nameEditText.setText("");
                                    userIdEditText.setText("");
                                    dobEditText.setText("");
                                    //genderEditText.setText(""); // Remove this line
                                    emailEditText.setText("");
                                    roleEditText.setText("");
                                    phoneEditText.setText("");
                                    addressEditText.setText("");
                                    Toast.makeText(getContext(), "User added successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to add user", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });
    }
}
