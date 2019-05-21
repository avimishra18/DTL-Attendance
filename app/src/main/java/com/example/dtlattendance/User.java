package com.example.dtlattendance;

import java.util.Comparator;

public class User {
    public String email,username,admin,online,total;

    //Compare to method
    public int compareTo(User compareUser) {
        int total = Integer.valueOf(compareUser.getTotal());
        int compareTotal=total;
        /* For Ascending order*/
        return total-compareTotal;

        /* For Descending order do like this */
        //return compareage-this.studentage;
    }

    public static Comparator<User> userTotal = new Comparator<User>() {

        public int compare(User user1, User user2) {

            int total1 = Integer.valueOf(user1.getTotal());
            int total2 = Integer.valueOf(user2.getTotal());

            //For ascending order
            //return total2-total1;
            //For descending order
            return total2-total1;
        }};

    public User() {
    }

    public User(String email, String username, String admin, String online) {
        this.email = email;
        this.username = username;
        this.admin = admin;
        this.online = online;
        this.total ="0";
    }

    public User(String email, String username) {
        this.email = email;
        this.username = username;
        this.admin="0";
        this.online="0";
        this.total ="0";
    }

    public User(String email, String username, String admin, String online, String total) {
        this.email = email;
        this.username = username;
        this.admin = admin;
        this.online = online;
        this.total = total;
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

