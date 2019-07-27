package com.example.dtlattendance;

import android.util.Log;

public class External {
    public long startTime;

    public External() {
    }

    public External(long startTime) {
        this.startTime = startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
