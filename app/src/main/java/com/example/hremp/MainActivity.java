package com.example.hremp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;



import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private TextView loginRedirectText;
    private LinearLayout employeeLoginForm;
    private LinearLayout hrLoginForm;

    private Button employeeButton;
    private Button hrButton;

    private EditText employeeUsernameEditText;
    private EditText employeePasswordEditText;
    private Button employeeLoginButton;
    private TextView forgotPasswordText;

    private EditText hrUsernameEditText;
    private EditText hrPasswordEditText;
    private Button hrLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth=FirebaseAuth.getInstance();
        employeeLoginForm = findViewById(R.id.employeeLoginForm);
        hrLoginForm = findViewById(R.id.hrLoginForm);

        employeeButton = findViewById(R.id.employeeButton);
        hrButton = findViewById(R.id.hrButton);

        employeeUsernameEditText = findViewById(R.id.employeeUsernameEditText);
        employeePasswordEditText = findViewById(R.id.employeePasswordEditText);
        employeeLoginButton = findViewById(R.id.employeeLoginButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);

        hrUsernameEditText = findViewById(R.id.hrUsernameEditText);
        hrPasswordEditText = findViewById(R.id.hrPasswordEditText);
        hrLoginButton = findViewById(R.id.hrLoginButton);

        employeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEmployeeLoginForm();
            }
        });

        hrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHRLoginForm();
            }
        });

        // Employee login
        employeeLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = employeeUsernameEditText.getText().toString();
                String password = employeePasswordEditText.getText().toString();

                if (!email.isEmpty() && !password.isEmpty()) {
                    // Check if the email is for an employee
                    if (email.endsWith("@company.com")) {
                        // Perform Firebase authentication for employee login
                        auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(MainActivity.this, task -> {
                                    if (task.isSuccessful()) {
                                        showToast("Employee Login Successful");
                                        navigateToHrActivity();
                                    } else {
                                        showToast("Invalid Employee Email or Password");
                                    }
                                });
                    } else {
                        showToast("Invalid Employee Email");
                    }
                } else {
                    showToast("Email and password are required");
                }
            }
        });

// HR login
        // HR login
        hrLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = hrUsernameEditText.getText().toString();
                String password = hrPasswordEditText.getText().toString();

                if (!email.isEmpty() && !password.isEmpty()) {
                    // Check if the email is for HR
                    if (email.endsWith("@hr.company.com")) {
                        // Perform Firebase authentication for HR login
                        auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(MainActivity.this, task -> {
                                    if (task.isSuccessful()) {
                                        showToast("HR Login Successful");
                                        navigateToHrmain(); // Corrected method name
                                    } else {
                                        showToast("Invalid HR Email or Password");
                                    }
                                });
                    } else {
                        showToast("Invalid HR Email");
                    }
                } else {
                    showToast("Email and password are required");
                }
            }
        });




        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToForgotPasswordPage();
            }
        });

        // Show the Employee login form by default
        showEmployeeLoginForm();
    }

    private void showEmployeeLoginForm() {
        employeeLoginForm.setVisibility(View.VISIBLE);
        hrLoginForm.setVisibility(View.GONE);

        // Change the title
        ((TextView) findViewById(R.id.titleTextView)).setText("Employee Login");
    }

    private void navigateToHrmain() {
        Intent intent = new Intent(this, Hrmain.class);
        startActivity(intent);
        finish(); // Finish the current activity
    }


    private void showHRLoginForm() {
        employeeLoginForm.setVisibility(View.GONE);
        hrLoginForm.setVisibility(View.VISIBLE);

        // Change the title
        ((TextView) findViewById(R.id.titleTextView)).setText("HR Login");
    }

    private void loginEmployee() {
        String username = employeeUsernameEditText.getText().toString();
        String password = employeePasswordEditText.getText().toString();

        if (username.equals("employee") && password.equals("employee123")) {
            showToast("Employee Login Successful");
            // Perform employee-related actions
        } else {
            showToast("Invalid Employee Username or Password");
        }
    }

    private void loginHR() {
        String username = hrUsernameEditText.getText().toString();
        String password = hrPasswordEditText.getText().toString();

        if (username.equals("hr") && password.equals("hr123")) {
            showToast("HR Login Successful");
            // Perform HR-related actions
        } else {
            showToast("Invalid HR Username or Password");
        }
    }

    private boolean isEmployeeLoginSuccessful() {
        // Replace this with your actual logic to check if the login is successful
        String username = employeeUsernameEditText.getText().toString();
        String password = employeePasswordEditText.getText().toString();

        return username.equals("employee") && password.equals("employee123");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showForgotPasswordDialog() {
        // Implement the logic for the forgot password dialog for Employee
        showToast("Forgot Password feature for Employee");
    }

    private void navigateToForgotPasswordPage() {
        // Create an Intent to start the ForgotPasswordActivity
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);

    }


    private void navigateToHrActivity() {
        String email = auth.getCurrentUser().getEmail(); // Get the current user's email

        if (email != null) {
            if (email.endsWith("@company.com")) {
                // It's an employee login, navigate to HomeActivity
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            } else if (email.endsWith("@hr.company.com")) {
                // It's an HR login, navigate to HrHomeActivity
                Intent intent = new Intent(this, Hrmain.class);
                startActivity(intent);
            }
        } else {
            showToast("Unable to determine user type.");
        }
    }


}

