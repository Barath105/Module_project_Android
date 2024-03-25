package com.example.hremp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

public class HrproductFragment extends Fragment {

    private EditText clientNameEditText, emailEditText, phoneNumberEditText, addressEditText,
            projectIDEditText, projectNameEditText, projectDescriptionEditText,
            totalBudgetEditText, advancePaidEditText, remainingAmountEditText,
            assignTaskEditText, dueDateEditText;

    private TextInputEditText editTextdueDate;
    private Button submitButton;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;
    private TextWatcher textWatcher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hr_fragment_product, container, false);

        // Initialize Firebase components
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("adminhr").child("hrproduct");

        // Get current user
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // Initialize EditText fields
        clientNameEditText = view.findViewById(R.id.clientNameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        phoneNumberEditText = view.findViewById(R.id.phoneNumberEditText);
        addressEditText = view.findViewById(R.id.addressEditText);
        projectIDEditText = view.findViewById(R.id.projectIDEditText);
        projectNameEditText = view.findViewById(R.id.projectNameEditText);
        projectDescriptionEditText = view.findViewById(R.id.projectDescriptionEditText);
        totalBudgetEditText = view.findViewById(R.id.totalBudgetEditText);
        advancePaidEditText = view.findViewById(R.id.advancePaidEditText);
        remainingAmountEditText = view.findViewById(R.id.remainingAmountEditText);
        assignTaskEditText = view.findViewById(R.id.assignTaskEditText);
        dueDateEditText = view.findViewById(R.id.dueDateEditText);
        submitButton = view.findViewById(R.id.submitButton);
         // Assuming dueDateEditText is the ID of your TextInputEditText in the layout file

        setupTextWatchers();

        // Make remainingAmountEditText read-only
        remainingAmountEditText.setEnabled(false);

        // TextWatcher for totalBudgetEditText and advancePaidEditText
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Calculate remaining amount when text changes
                calculateRemainingAmount();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        // Add TextWatcher to calculate remaining amount dynamically
        totalBudgetEditText.addTextChangedListener(textWatcher);
        advancePaidEditText.addTextChangedListener(textWatcher);

        // Set onClickListener for submitButton
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToFirebase();
            }
        });

        return view;
    }
    private void setupTextWatchers() {
        dueDateEditText.setFocusable(false);
        dueDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(dueDateEditText);
            }
        });
    }


    private void showDatePickerDialog(final EditText editText) {
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




    // Method to calculate remaining amount
    private void calculateRemainingAmount() {
        String totalBudgetStr = totalBudgetEditText.getText().toString().trim();
        String advancePaidStr = advancePaidEditText.getText().toString().trim();

        if (!totalBudgetStr.isEmpty() && !advancePaidStr.isEmpty()) {
            double totalBudget = Double.parseDouble(totalBudgetStr);
            double advancePaid = Double.parseDouble(advancePaidStr);
            double remainingAmount = totalBudget - advancePaid;

            // Update remainingAmountEditText with the calculated value
            remainingAmountEditText.setText(String.valueOf(remainingAmount));
        }
    }

    private void saveDataToFirebase() {
        // Get input values
        String clientName = clientNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String projectID = projectIDEditText.getText().toString().trim();
        String projectName = projectNameEditText.getText().toString().trim();
        String projectDescription = projectDescriptionEditText.getText().toString().trim();
        String totalBudget = totalBudgetEditText.getText().toString().trim();
        String advancePaid = advancePaidEditText.getText().toString().trim();
        String remainingAmount = remainingAmountEditText.getText().toString().trim();
        String assignTask = assignTaskEditText.getText().toString().trim();
        String dueDate = dueDateEditText.getText().toString().trim();

        // Validate fields
        if (clientName.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || address.isEmpty() ||
                projectID.isEmpty() || projectName.isEmpty() || projectDescription.isEmpty() ||
                totalBudget.isEmpty() || advancePaid.isEmpty() || remainingAmount.isEmpty() ||
                assignTask.isEmpty() || dueDate.isEmpty()) {
            // Display error message if any field is empty
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Incremental ID
        final int[] nextId = {1};

        // Retrieve the current maximum numeric value of nodes under "adminhr/userinfo"
        databaseReference.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    count = Long.parseLong(snapshot.getKey());
                }

                // Increment the count to determine the numeric value for the key of the new node
                count++;

                // Create a new node under hrproduct with the calculated ID
                DatabaseReference newProductRef = databaseReference.child(String.valueOf(count));
                String uniqueId = databaseReference.push().getKey();

                // Create a HashMap to store the data
                HashMap<String, Object> productData = new HashMap<>();
                productData.put("uniqueId", uniqueId); // Store the unique ID
                productData.put("clientName", clientName);
                productData.put("email", email);
                productData.put("phoneNumber", phoneNumber);
                productData.put("address", address);
                productData.put("projectID", projectID);
                productData.put("projectName", projectName);
                productData.put("projectDescription", projectDescription);
                productData.put("totalBudget", totalBudget);
                productData.put("advancePaid", advancePaid);
                productData.put("remainingAmount", remainingAmount);
                productData.put("assignTask", assignTask);
                productData.put("dueDate", dueDate);
                productData.put("status", "Pending"); // Add status as "Pending"

                // Save data to Firebase
                newProductRef.setValue(productData)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    clientNameEditText.setText("");
                                    emailEditText.setText("");
                                    phoneNumberEditText.setText("");
                                    addressEditText.setText("");
                                    projectIDEditText.setText("");
                                    projectNameEditText.setText("");
                                    projectDescriptionEditText.setText("");
                                    totalBudgetEditText.setText("");
                                    advancePaidEditText.setText("");
                                    remainingAmountEditText.setText("");
                                    assignTaskEditText.setText("");
                                    dueDateEditText.setText("");

                                    Toast.makeText(getContext(), "Product added successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Failed to add data", Toast.LENGTH_SHORT).show();
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
