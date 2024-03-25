// Import statements
package com.example.hremp;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ViewattFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_viewatt, container, false);

        // Find the CalendarView in your layout
        CalendarView calendarView = view.findViewById(R.id.calendarView);

        // Set a listener to be notified about selected date changes
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int year, int month, int dayOfMonth) {
                // Construct the selected date in the required format
                String selectedDate = formatDate(year, month, dayOfMonth);

                // Construct the Firebase path for the selected date
                String firebasePath = "adminhr/useratt/1001/" + selectedDate;

                // Get a reference to the Firebase database
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(firebasePath);

                // Check if the selected date exists in the database
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("FirebaseData", "onDataChange triggered");

                        if (dataSnapshot.exists()) {
                            // Retrieve the data of overall_att from the matching node
                            String overallAtt = dataSnapshot.child("overall_att").getValue(String.class);

                            // Display the overall_att data using a custom dialog
                            showAttendanceDialog(selectedDate, overallAtt);
                        } else {
                            // Handle the case where the selected date does not exist in the database
                            Toast.makeText(getContext(), "No data found for " + selectedDate, Toast.LENGTH_SHORT).show();
                            Log.d("FirebaseData", "No data found for " + selectedDate);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle potential errors
                        String errorMessage = "Error: " + databaseError.getMessage();
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("FirebaseError", errorMessage, databaseError.toException());
                    }
                });
            }
        });

        return view;
    }

    private String formatDate(int year, int month, int dayOfMonth) {
        // Format the date in "yy-MM-dd" format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd", Locale.getDefault());
        Date date = new Date(year - 1900, month, dayOfMonth);
        return dateFormat.format(date);
    }

    private void showAttendanceDialog(String date, String overallAtt) {
        Log.d("AttendanceDialog", "Date: " + date + ", OverallAtt: " + overallAtt);

        // Create a custom dialog
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_attendance);

        // Find the TextViews in the custom dialog layout
        TextView attendanceMessageTextView = dialog.findViewById(R.id.attendanceMessageTextView);
        TextView overallAttTextView = dialog.findViewById(R.id.overallAttTextView);

        // Set the text to display the overall_att data
        attendanceMessageTextView.setText("Attendance Information");
        overallAttTextView.setText("Attendance: " + overallAtt);

        // Show the dialog
        dialog.show();
    }
}
