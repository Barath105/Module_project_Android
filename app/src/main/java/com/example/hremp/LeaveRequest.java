package com.example.hremp;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LeaveRequest {
    private String userId;
    private String leaveType;
    private String startDate;
    private String endDate;
    private String days;
    private String reason;
    private String fileName;
    private String status;
    private String leaveId; // New field for unique leave ID
    private String filePath;

    // Default constructor required for calls to DataSnapshot.getValue(LeaveRequest.class)
    public LeaveRequest() {
    }

    public LeaveRequest(String userId, String leaveType, String startDate, String endDate, String days, String reason, String fileName, String status, String filePath) {
        this.userId = userId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.days = days;
        this.reason = reason;
        this.fileName = fileName;
        this.status = status;
        this.filePath = filePath;

        // Generate unique leave ID
        this.leaveId = generateLeaveId(userId);
    }

    // Generate leave ID based on user ID and timestamp
    private String generateLeaveId(String userId) {
        long timestamp = System.currentTimeMillis();
        return userId + "_" + timestamp;
    }

    // Add getter for leaveId
    public String getLeaveId() {
        return leaveId;
    }

    // Add getter methods for other fields...
    public String getUserId() {
        return userId;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getDays() {
        return days;
    }

    public String getReason() {
        return reason;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStatus() {
        return status;
    }

    public String getFilePath() {
        return filePath;
    }

    // Example of updating leave status
    public void updateLeaveStatus(DatabaseReference hrLeaveReference, String newStatus) {
        // Update only the 'status' field in the database
        Map<String, Object> updateStatus = new HashMap<>();
        updateStatus.put("status", newStatus);

        // Update the leave status in Firebase Database
        hrLeaveReference.child(leaveId).updateChildren(updateStatus);
    }
}
