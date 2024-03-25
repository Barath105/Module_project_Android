package com.example.hremp;

import com.google.firebase.database.DatabaseReference;

public class PermissionRequest {
    private String userId;
    private String session;
    private String date;
    private String fileName;
    private String reason;
    private String status;
    private String fileUrl;
    private String permissionId; // New field for unique leave ID
    private String filePath; // Add filePath field


    // Reference to the Firebase Database node
    private transient DatabaseReference ref;

    // Required default constructor for Firebase
    public PermissionRequest() {
    }

    // Constructor with parameters
    public PermissionRequest(String userId, String session, String date, String fileName, String reason, String status, String fileUrl, String filePath) {
        this.userId = userId;
        this.session = session;
        this.date = date;
        this.fileName = fileName;
        this.reason = reason;
        this.status = status;
        this.fileUrl = fileUrl;
        this.filePath = filePath; // Initialize filePath
        this.permissionId = generatePermissionId(userId);
    }

    // Generate leave ID based on user ID and timestamp
    private String generatePermissionId(String userId) {
        long timestamp = System.currentTimeMillis();
        return userId + "_" + timestamp;
    }

    // Getter and setter methods
    public String getUserId() {
        return userId;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public String getSession() {
        return session;
    }

    public String getDate() {
        return date;
    }

    public String getFileName() {
        return fileName;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFilePath() { // Getter method for filePath
        return filePath;
    }

    // Getter and setter for DatabaseReference
    public DatabaseReference getRef() {
        return ref;
    }

    public void setRef(DatabaseReference ref) {
        this.ref = ref;
    }
}
