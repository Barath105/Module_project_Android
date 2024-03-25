package com.example.hremp;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class PrivacyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_privacy, container, false);

        // Find the TextView by ID
        TextView privacyTextView = view.findViewById(R.id.privacy); // Replace with the actual TextView ID

        // Set an OnClickListener for the TextView
        privacyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Replace the current fragment with the PrivacyFragment
                loadFragment(new PrivacyFragment());
            }
        });

        return view;
    }

    // Method to replace the current fragment with a new fragment
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment); // Replace with the actual container ID
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
