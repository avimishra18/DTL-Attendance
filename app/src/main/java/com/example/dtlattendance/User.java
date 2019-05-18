package com.example.dtlattendance;

public class User {
    public String email,username,admin;

    public User() {
    }

    public User(String email, String username, String admin) {
        this.email = email;
        this.username = username;
        this.admin = admin;
    }

    public User(String email, String username) {
        this.email = email;
        this.username = username;
        this.admin="0";
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }
}
