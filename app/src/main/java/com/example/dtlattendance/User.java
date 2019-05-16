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
}
