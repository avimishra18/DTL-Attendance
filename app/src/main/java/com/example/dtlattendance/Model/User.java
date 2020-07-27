package com.example.dtlattendance.Model;

import java.util.Comparator;

public class User {
    public String admin,email,online,total,uid,username;

    public static Comparator<User> userTotal = new Comparator<User>() {

        public int compare(User user1, User user2) {

            int total1 = Integer.valueOf(user1.getTotal());
            int total2 = Integer.valueOf(user2.getTotal());

            //For descending order
            return total2-total1;
        }};

    public User() {
    }

    public User(String email, String username, String admin, String online, String total, String uid) {
        this.admin = admin;
        this.email = email;
        this.online = online;
        this.total = total;
        this.uid = uid;
        this.username = username;
    }

    public User(String email, String username, String uid) {
        this.admin="0";
        this.email = email;
        this.online="0";
        this.total ="0";
        this.uid = uid;
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
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

