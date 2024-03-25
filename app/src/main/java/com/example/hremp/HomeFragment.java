package com.example.hremp;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private PieChart pieChart;
    private List<PieEntry> pieEntryList;
    private boolean isFragmentAttached = false; // Flag to track fragment attachment

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        pieEntryList = new ArrayList<>();
        pieChart = view.findViewById(R.id.chart);
        // Set width and height programmatically
        ViewGroup.LayoutParams params = pieChart.getLayoutParams();
        params.width = 1000; // Set your desired width in pixels
        params.height = 1000; // Set your desired height in pixels
        pieChart.setLayoutParams(params);

        // Check if fragment is attached before fetching data
        if (isFragmentAttached) {
            fetchAttendanceData();
        }

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        isFragmentAttached = true; // Set the flag to true when the fragment is attached
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isFragmentAttached = false; // Set the flag to false when the fragment is detached
    }

    private void setUpChart() {
        if (!isFragmentAttached) {
            return; // Return early if the fragment is not attached
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntryList, "Pie Chart");
        pieDataSet.setColors(getColors());
        pieDataSet.setValueTextColor(getResources().getColor(android.R.color.white));
        pieDataSet.setValueTextSize(12f);

        PieData pieData = new PieData(pieDataSet);

        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private ArrayList<Integer> getColors() {
        if (!isFragmentAttached) {
            return new ArrayList<>(); // Return empty list if the fragment is not attached
        }

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.colorPresent));
        colors.add(getResources().getColor(R.color.colorAbsent));
        colors.add(getResources().getColor(R.color.colorLate));
        return colors;
    }

    private void fetchAttendanceData() {
        DatabaseReference attendanceRef = FirebaseDatabase.getInstance().getReference()
                .child("adminhr").child("useratt").child("1001");

        attendanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isFragmentAttached) {
                    return; // Return early if the fragment is not attached
                }

                int presentCount = 0;
                int absentCount = 0;
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String overallAtt = dateSnapshot.child("overall_att").getValue(String.class);
                    if ("Present".equals(overallAtt)) {
                        presentCount++;
                    } else if ("Absent".equals(overallAtt)) {
                        absentCount++;
                    }
                }
                pieEntryList.add(new PieEntry(presentCount, "Present"));
                pieEntryList.add(new PieEntry(absentCount, "Absent"));
                pieEntryList.add(new PieEntry(1, "Late"));

                setUpChart(); // Call setUpChart after updating data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                if (isFragmentAttached) {
                    Toast.makeText(requireContext(), "Error fetching data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
