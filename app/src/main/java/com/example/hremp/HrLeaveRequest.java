package com.example.hremp;

import com.google.firebase.database.DatabaseReference;
public class HrLeaveRequest {
    private String leaveId;
    private String userId;
    private String leaveType;
    private String startDate;
    private String endDate;
    private String reason;
    private String fileName;
    private String filePath;
    private String status;
    private transient DatabaseReference ref;


    // Default constructor required for Firebase
    public HrLeaveRequest() {
    }

    public HrLeaveRequest(String leaveId, String userId, String leaveType, String startDate,
                          String endDate, String reason, String fileName, String filePath, String status) {
        this.leaveId = leaveId;
        this.userId = userId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.fileName = fileName;
        this.filePath = filePath;
        this.status = status;
    }

    public void setRef(DatabaseReference ref) {
        this.ref = ref;
    }

    public String getLeaveId() {
        return leaveId;
    }

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

    public String getReason() {
        return reason;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getStatus() {
        return status;
    }

    // Add other getter and setter methods as needed

    public DatabaseReference getRef() {
        return ref;
    }

}

