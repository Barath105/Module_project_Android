package com.example.hremp;

import com.google.firebase.database.DatabaseReference;

public class PayslipRequest {
    private String userId;

    private String payslipId;
    private String name;
    private String date;
    private String status;

    private DatabaseReference ref; // Add DatabaseReference field

    // Default constructor required for Firebase
    public PayslipRequest() {}

    public PayslipRequest(String payslipId,String userId, String name, String date) {
        this.payslipId = payslipId;
        this.userId = userId;
        this.name = name;
        this.date = date;
        this.status = "Pending"; // Assuming a default value for status
    }

    public DatabaseReference getRef() {
        return ref;
    }

    public void setRef(DatabaseReference ref) {
        this.ref = ref;
    }

    public String getPayslipId() {
        return payslipId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getdate() {
        return date;
    }

    public void setPayslipdate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
