package com.example.hremp;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class OverTimeRequest {
    private String userId;
    private String name;

    private DatabaseReference ref; // Add a DatabaseReference field
    private String date;
    private String status;

    private String overtimeId;

    // Default constructor required for calls to DataSnapshot.getValue(LeaveRequest.class)
    public OverTimeRequest() {
    }

    public OverTimeRequest(String userId, String name, String date, String status) {
        this.userId = userId;
        this.name = name;
        this.date = date;
        this.status = status;
        this.overtimeId = generateOvertimeId(userId);
    }


    private String generateOvertimeId(String userId) {
        long timestamp = System.currentTimeMillis();
        return userId + "_" + timestamp;
    }



    public DatabaseReference getRef() {
        return ref;
    }


    public String getOvertimeId() {
        return overtimeId;
    }


    // Add getter methods for each field
    public String getUserId() {
        return userId;
    }

    public String getname() {
        return name;
    }

    public String getdate() {
        return date;
    }



    public String getStatus() {
        return status;
    }


    public void updateOvertimeStatus(DatabaseReference overtimeReference, String newStatus) {
        // Update only the 'status' field in the database
        Map<String, Object> updateStatus = new HashMap<>();
        updateStatus.put("status", newStatus);

        // Update the leave status in Firebase Database
        overtimeReference.child(overtimeId).updateChildren(updateStatus);
    }

    public void setRef(DatabaseReference ref) {
        this.ref = ref;
    }


}
