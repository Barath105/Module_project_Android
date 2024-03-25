package com.example.hremp;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;



public class EditUserFragment extends Fragment {

    private String uniqueKey;
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String dob;
    private String gender;
    private String role;
    private String address;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.hr_fragment_edit_user, container, false);

        // Retrieve arguments from Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            uniqueKey = bundle.getString("uniqueKey");
            userId = bundle.getString("userId");
            name = bundle.getString("name");
            email = bundle.getString("email");
            phone = bundle.getString("phone");
            dob = bundle.getString("dob");
            gender = bundle.getString("gender");
            role = bundle.getString("role");
            address = bundle.getString("address");

            // Now you have all the data, you can populate your EditText fields or other views with this data
            EditText nameEditText = rootView.findViewById(R.id.editTextname);
            nameEditText.setText(name);

            EditText dobEditText = rootView.findViewById(R.id.editTextdob);
            dobEditText.setText(dob);

            EditText emailEditText = rootView.findViewById(R.id.editTextmail);
            emailEditText.setText(email);

            EditText roleEditText = rootView.findViewById(R.id.editTextrole);
            roleEditText.setText(role);

            EditText phoneEditText = rootView.findViewById(R.id.editTextphone);
            phoneEditText.setText(phone);

            EditText addressEditText = rootView.findViewById(R.id.editTextaddress);
            addressEditText.setText(address);
        }

        return rootView;
    }

}