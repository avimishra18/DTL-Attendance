package com.example.dtlattendance;

public class AttendanceSession {
    String startDate,endDate,time;

    public AttendanceSession() {
    }

    public AttendanceSession(String startDate, String endDate, String time) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.time = time;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
