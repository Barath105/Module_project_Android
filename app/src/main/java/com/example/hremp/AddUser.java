package com.example.hremp;
public class AddUser {
    private String name;
    private String userId;
    private String dob;

    private String gender;
    private String email;
    private String role;
    private String phone;
    private String address;

    public AddUser() {
        // Default constructor required for Firebase
    }

    public AddUser(String name, String userId, String dob, String gender, String email, String role, String phone, String address) {
        this.name = name;
        this.userId = userId;
        this.dob = dob;
        this.gender=gender;
        this.email = email;
        this.role = role;
        this.phone = phone;
        this.address = address;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }



    public String getgender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
