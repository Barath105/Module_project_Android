// OdRequest.java

package com.example.hremp;

public class OdRequest {
    private String userId;
    private String startDate;
    private String endDate;
    private String fileName;
    private String reason;

    private String status;
    private String fileUrl;

    // Required default constructor for Firebase
    public OdRequest() {
    }

    public OdRequest(String userId, String startDate, String endDate, String fileName, String reason, String fileUrl) {
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fileName = fileName;
        this.reason = reason;
        this.fileUrl = fileUrl;  // Initialize the field
    }

    public String getUserId() {
        return userId;
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
    public String getStatus() {
        return status;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    // Getters and setters for other fields
    // ...
}
