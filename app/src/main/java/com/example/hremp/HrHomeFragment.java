package com.example.hremp;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HrHomeFragment extends Fragment {

    private BarChart barChart;
    private PieChart pieChart;
    private TextView textViewCount;
    private TextView textViewProductCount;
    private TextView textViewTotalBudget;
    private TextView textViewClient;
    private DatabaseReference databaseRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.hr_fragment_home, container, false);
        barChart = rootView.findViewById(R.id.barChart);
        pieChart = rootView.findViewById(R.id.pieChart);
        textViewCount = rootView.findViewById(R.id.userCount);
        textViewProductCount = rootView.findViewById(R.id.productCount);
        textViewTotalBudget = rootView.findViewById(R.id.revenue);
        textViewClient = rootView.findViewById(R.id.clientcount);

        // Initialize databaseRef with the correct Firebase reference
        databaseRef = FirebaseDatabase.getInstance().getReference().child("adminhr/hrproduct");

        Button moreinfoButton = rootView.findViewById(R.id.employeeinfo);
        moreinfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EmployeeInfoFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        Button projectinfoButton = rootView.findViewById(R.id.projectinfo);
        projectinfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProductInfoFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        Button clientinfoButton = rootView.findViewById(R.id.clientinfo);
        clientinfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ClientInfoFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        Button revenuebtnButton = rootView.findViewById(R.id.revenuebtn);
        revenuebtnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HrRevenueFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Setup Firebase database reference and listeners
        setupFirebaseListeners();

        // Inside your onCreateView method
        ViewGroup.LayoutParams params = pieChart.getLayoutParams();
        params.width = 800; // Set your desired width in pixels
        params.height = 800; // Set your desired height in pixels
        pieChart.setLayoutParams(params);

        // Populate bar chart with dynamic data
        setupBarChart();

        // Populate pie chart with dynamic data
        setupPieChart();

        // Fetch and display user count
        fetchAndDisplayUserCount();
        fetchAndDisplayClientCount();
        fetchCompletedStatusCount();
        fetchAndDisplayTotalBudget();
        fetchAndDisplayProductCount();

        return rootView;
    }

    private void setupFirebaseListeners() {
        // Initialize databaseRef with the correct Firebase reference
        databaseRef = FirebaseDatabase.getInstance().getReference().child("adminhr/hrproduct");

        // Setup your Firebase listeners here and perform UI updates inside onDataChange()
        // Example:
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Update UI here
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void fetchAndDisplayUserCount() {
        // Retrieve count of userinfo from Firebase
        FirebaseDatabase.getInstance().getReference().child("adminhr/userinfo")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get count of userinfo
                        long count = dataSnapshot.getChildrenCount(); // This line retrieves the count of children nodes
                        // Set count to TextView
                        textViewCount.setText(String.valueOf(count)); // Convert the count to String and set it to the TextView
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                        Log.e("fetchUserCount", "Error fetching user count: " + databaseError.getMessage());
                    }
                });
    }

    private void fetchAndDisplayClientCount() {
        // Retrieve count of userinfo from Firebase
        FirebaseDatabase.getInstance().getReference().child("adminhr/hrproduct")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get count of userinfo
                        long count = dataSnapshot.getChildrenCount();
                        // Set count to TextView
                        textViewClient.setText(String.valueOf(count));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    private void fetchAndDisplayTotalBudget() {
        // Retrieve totalBudget values from Firebase
        FirebaseDatabase.getInstance().getReference().child("adminhr/hrproduct")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        double totalBudget = 0; // Initialize total budget

                        // Loop through each hrproduct node
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Check if the totalBudget field exists
                            if (snapshot.hasChild("totalBudget")) {
                                // Get the totalBudget for the current hrproduct node
                                String budgetStr = snapshot.child("totalBudget").getValue(String.class);
                                if (budgetStr != null && !budgetStr.isEmpty()) {
                                    // Convert the budget string to double
                                    try {
                                        double budget = Double.parseDouble(budgetStr);
                                        // Add the budget to the totalBudget sum
                                        totalBudget += budget;
                                    } catch (NumberFormatException e) {
                                        // Handle conversion errors
                                        Log.e("BudgetConversion", "Error converting budget string to double: " + budgetStr);
                                    }
                                }
                            }
                        }

                        // Set the totalBudget sum to TextView
                        textViewTotalBudget.setText(String.valueOf(totalBudget));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    private void fetchAndDisplayProductCount() {
        // Retrieve count of hrproduct nodes with status "Pending" from Firebase
        databaseRef.orderByChild("status").equalTo("Pending").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get count of hrproduct nodes
                long count = dataSnapshot.getChildrenCount();
                // Set count to TextView
                textViewProductCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.e("fetchProductCount", "Error fetching product count: " + databaseError.getMessage());
            }
        });
    }

    private void fetchCompletedStatusCount() {
        // Initialize count variable
        final int[] completedCount = {0}; // Initialize as an array to make it effectively final

        // Set the reference to the correct path
        databaseRef = FirebaseDatabase.getInstance().getReference().child("adminhr/hrproduct");

        // Attach a listener to read the data
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through all children nodes
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    // Get the value of the status field for each product
                    String status = productSnapshot.child("status").getValue(String.class);
                    // Check if the status is "Completed"
                    if (status != null && status.equals("Completed")) {
                        // Increment the count if status is "Completed"
                        completedCount[0]++;
                    }
                }

                // Set the completed count to the TextView
                TextView textViewProjectComplete = requireView().findViewById(R.id.projectComplete);
                textViewProjectComplete.setText(String.valueOf(completedCount[0]));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    private void setupBarChart() {
        // Retrieve userinfo nodes from Firebase and count the number of users for each role
        FirebaseDatabase.getInstance().getReference().child("adminhr/userinfo")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int webDeveloperCount = 0; // Initialize count for Web Developers
                        int uiUxCount = 0; // Initialize count for UI/UX
                        int softwareTestingCount = 0; // Initialize count for Software Testing
                        int backendCount = 0; // Initialize count for Backend

                        // Loop through each userinfo node
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Get the role for the current userinfo node
                            String role = snapshot.child("role").getValue(String.class);

                            // Increment count based on role
                            switch (role) {
                                case "Web Developer":
                                    webDeveloperCount++;
                                    break;
                                case "UI/UX":
                                    uiUxCount++;
                                    break;
                                case "Software Testing":
                                    softwareTestingCount++;
                                    break;
                                case "Backend":
                                    backendCount++;
                                    break;
                                default:
                                    // Handle other roles if needed
                                    break;
                            }
                        }

                        // Create entries for the bar chart
                        List<BarEntry> entries = new ArrayList<>();
                        entries.add(new BarEntry(0, webDeveloperCount)); // Web developer count
                        entries.add(new BarEntry(1, uiUxCount)); // UI/UX count
                        entries.add(new BarEntry(2, softwareTestingCount));  // Software testing count
                        entries.add(new BarEntry(3, backendCount)); // Backend count

                        BarDataSet dataSet = new BarDataSet(entries, "Department Counts");

                        // Set colors for each department
                        List<Integer> colors = new ArrayList<>();
                        colors.add(android.graphics.Color.parseColor("#30BEB6")); // Light blue
                        colors.add(android.graphics.Color.parseColor("#FF7F74")); // Light red
                        colors.add(android.graphics.Color.parseColor("#FFCB67")); // Light yellow
                        colors.add(android.graphics.Color.parseColor("#7E57C2")); // Light purple
                        dataSet.setColors(colors);

                        BarData barData = new BarData(dataSet);
                        barChart.setData(barData);

                        String[] labels = {"Web Developer", "UI/UX", "Software Testing", "Backend"};
                        XAxis xAxis = barChart.getXAxis();
                        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setGranularity(1f);
                        xAxis.setDrawGridLines(false);

                        barChart.getAxisLeft().setAxisMinimum(0f);
                        barChart.getAxisLeft().setGranularity(1f);

                        barChart.getAxisRight().setEnabled(false);

                        Legend legend = barChart.getLegend();
                        legend.setEnabled(false);

                        barChart.getDescription().setEnabled(false);
                        barChart.setFitBars(true);
                        barChart.invalidate();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }

    private void setupPieChart() {
        // Retrieve gender counts from Firebase
        FirebaseDatabase.getInstance().getReference().child("adminhr/userinfo")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int maleCount = 0; // Initialize count for Male
                        int femaleCount = 0; // Initialize count for Female

                        // Loop through each userinfo node
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // Get the gender for the current userinfo node
                            String gender = snapshot.child("gender").getValue(String.class);

                            // Increment count based on gender
                            if ("Male".equals(gender)) {
                                maleCount++;
                            } else if ("Female".equals(gender)) {
                                femaleCount++;
                            }
                        }

                        // Create entries for the pie chart
                        List<PieEntry> entries = new ArrayList<>();
                        entries.add(new PieEntry(maleCount, "Male"));
                        entries.add(new PieEntry(femaleCount, "Female"));

                        PieDataSet dataSet = new PieDataSet(entries, "Gender Distribution");

                        // Set colors for each gender
                        List<Integer> colors = new ArrayList<>();
                        colors.add(android.graphics.Color.parseColor("#64B5F6")); // Light blue for male
                        colors.add(android.graphics.Color.parseColor("#FFA726")); // Orange for female
                        dataSet.setColors(colors);

                        PieData pieData = new PieData(dataSet);
                        pieChart.setData(pieData);

                        pieChart.getDescription().setEnabled(false);

                        Legend legend = pieChart.getLegend();
                        legend.setEnabled(true); // Show legend

                        pieChart.invalidate();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error
                    }
                });
    }
}
