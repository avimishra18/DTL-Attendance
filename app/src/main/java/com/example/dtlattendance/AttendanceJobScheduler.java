package com.example.dtlattendance;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

import static com.example.dtlattendance.App.Channel_ID;

public class AttendanceJobScheduler extends JobService {

    //Declarations
    private String startTime,endTime,sessionID;
    private boolean isTargetBSSID_InRange = false;
    private long total;
    private NotificationManagerCompat notificationManager;


    //private static final String targetBSSID = "02:15:b2:00:01:00";
    private static final String targetBSSID = "00:23:68:17:65:d0";

    //FireBase
    DatabaseReference databaseReferenceSession,databaseReferenceUser;

    private static final String TAG = "AttendanceJobScheduler";
    @Override
    public boolean onStartJob(JobParameters params) {
        wifiScan();
        Log.d(TAG,"Job Started");
        if(isTargetBSSID_InRange){
            startMarkingAttendance(params);
            Log.d(TAG,"Target BSSID in Range");
            return true;
        }
        else {
            Log.d(TAG, "Target BSSID NOT in range");
            return false;
        }
    }

    public void startMarkingAttendance(JobParameters params){

        //We shouldn't run a service on the main thread.
        new Thread(new Runnable() {
            @Override
            public void run() {

                //Declerations
                Long timeSpent=1L;
                AttendanceSession attendanceSession;
                User activeUser;


                //FireBase Session (Making folder in session by USER UID)
                databaseReferenceSession = FirebaseDatabase.getInstance()
                        .getReference("Sessions")
                        .child(FirebaseAuth.getInstance().getUid());

                //FireBase User to show if he is online
                databaseReferenceUser = FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(FirebaseAuth.getInstance().getUid());

                //Notification Manager
                notificationManager = NotificationManagerCompat.from(getApplicationContext());

                //Shared Preference to get USER details
                SharedPreferences pref = getSharedPreferences("User", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                String storedUsername = pref.getString("username", null);
                String storedemail = pref.getString("email", null);
                String storedAdmin = pref.getString("admin", null);
                String storedTotal = pref.getString("total", null);
                String storeduid = pref.getString("uid", null);

                //Updating the endTime regularly.
                endTime = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

                //To check if previous session exists

                //1. First we get the end time of the previous session
                SharedPreferences preferencesSession = getSharedPreferences("Session", MODE_PRIVATE);
                SharedPreferences.Editor editorSession = preferencesSession.edit();
                Long storedLastTime = preferencesSession.getLong("time",0);

                //If previous session exists
                if(System.currentTimeMillis()<20*60*1000+storedLastTime){

                    String sessionID = preferencesSession.getString("uid",null);
                    String storedSessionStartTime = preferencesSession.getString("start",null);

                    //FireBase Update (End-Time & Total Time) Only
                    timeSpent = (System.currentTimeMillis()-storedLastTime)/1000;
                    attendanceSession = new AttendanceSession(storedSessionStartTime,endTime,String.valueOf(timeSpent));

                    Log.d(TAG,"Updating "+sessionID);
                    //Marking the User as online & updating total time
                    total = Long.valueOf(storedTotal)+timeSpent;
                    editor.putString("total", String.valueOf(total));
                    editor.apply();
                    activeUser = new User(storedemail, storedUsername, storedAdmin, "1", String.valueOf(total), storeduid);

                    //Common FB code for both cases
                    if(sessionID != null) {

                        //Updating the session
                        databaseReferenceSession
                                .child(sessionID)
                                .setValue(attendanceSession)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                            Log.d(TAG, "FireBase: Session Update Successful");
                                    }
                                });

                        //Making user Online
                        databaseReferenceUser.setValue(activeUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                    Log.d(TAG, "FireBase: User Status Update Successful");
                            }
                        });


                        //Sending Notification
                        Notification notification = new NotificationCompat.Builder(getApplicationContext(),Channel_ID)
                                .setContentTitle("Active Session")
                                .setContentText("Session Time: "+timeSpent+" seconds")
                                .setOnlyAlertOnce(true)
                                .setSmallIcon(R.drawable.ic_wifi_black_24dp)
                                .build();
                        notificationManager.notify(1,notification);
                        Log.d(TAG,"Notification Sent");
                    }

                }

                //If previous session doesn't exist
                else{

                    //Generation of new UID for Session
                    sessionID = databaseReferenceSession.push().getKey();
                    Log.d(TAG,"Generating "+sessionID);

                    //Shared Preference to get SESSION details
                    editorSession.putString("uid",sessionID);
                    editorSession.putString("start",endTime);
                    editorSession.putString("end",endTime);
                    editorSession.putLong("time",System.currentTimeMillis());
                    editorSession.putLong("totaltime",0);
                    editorSession.apply();

                    //FireBase Update (End-Time & Total Time) Only
                    attendanceSession = new AttendanceSession(endTime,endTime,"0");

                    //Marking the User as online & updating total time
                    total = Long.valueOf(storedTotal);
                    editor.putString("total", String.valueOf(total));
                    editor.apply();

                    activeUser = new User(storedemail, storedUsername, storedAdmin, "1", String.valueOf(total), storeduid);
                    //Common FB code for both cases
                    if(sessionID != null) {

                        //Updating the session
                        databaseReferenceSession
                                .child(sessionID)
                                .setValue(attendanceSession)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                            Log.d(TAG, "FireBase: Session Update Successful");
                                    }
                                });

                        //Making user Online
                        databaseReferenceUser.setValue(activeUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                    Log.d(TAG, "FireBase: User Status Update Successful");
                            }
                        });


                        //Sending Notification
                        Notification notification = new NotificationCompat.Builder(getApplicationContext(),Channel_ID)
                                .setContentTitle("Active Session")
                                .setContentText("Session Time: "+timeSpent+" seconds")
                                .setOnlyAlertOnce(true)
                                .setSmallIcon(R.drawable.ic_wifi_black_24dp)
                                .build();
                        notificationManager.notify(1,notification);
                        Log.d(TAG,"Notification Sent");
                    }
                }
            }
        }).start();

        //When Job is Finished
        jobFinished(params,true);
        Log.d(TAG,"Job Finished");
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG,"Job cancelled before completion");
        return true;
    }

    //WiFi scan for BSSID
    private void wifiScan() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> results = wifiManager.getScanResults();
        int position=-1;

        if(results.size()==0)
            Log.d("WiFi Scan Result","No connection found in range");

        for (int i = 0; i < results.size(); i++) {
            String SSID = results.get(i).SSID;
            String BSSID = results.get(i).BSSID;
            Log.d("WiFi Scan Result",i+". "+SSID+": "+BSSID);

            if (BSSID.toLowerCase().equals(targetBSSID.toLowerCase())) {
                isTargetBSSID_InRange = true;
                position=i;
            }
        }
        //Removes the selected SSID
        if(position!=-1)
            results.remove(position);
    }
}