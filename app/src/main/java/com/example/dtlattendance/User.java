package com.example.dtlattendance;

public class User {
    public String email,username,admin,online;

    public User() {
    }

    public User(String email, String username, String admin, String online) {
        this.email = email;
        this.username = username;
        this.admin = admin;
        this.online = online;
    }

    public User(String email, String username) {
        this.email = email;
        this.username = username;
        this.admin="0";
        this.online="0";
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
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
