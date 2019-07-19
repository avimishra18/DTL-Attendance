package com.example.dtlattendance;

import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static com.example.dtlattendance.NotificationDTL.Channel_ID;

public class AttendanceJobScheduler extends JobService {

    //Declarations
    private String startTime,endTime,sessionID;
    private boolean isTargetBSSID_InRange = false;
    private long total;
    private NotificationManagerCompat notificationManager;

    private static final String targetBSSID = "02:15:b4:00:01:00";
    private static final String targetBSSIDICT08 = "00:23:68:17:65:d0";

    //FireBase
    DatabaseReference databaseReferenceSession,databaseReferenceUser;

    private static final String TAG = "AttendanceJobScheduler";
    @Override
    public boolean onStartJob(JobParameters params) {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        int wifi = 1;
        if (!wifiManager.isWifiEnabled()) {
            Log.d(TAG, "Wifi not enabled.");
            wifiScan();
            wifiManager.setWifiEnabled(true);
            wifi = 0;
        }
        wifiScan();

        Log.d(TAG, "Job Started");
        if(wifi == 0){
            wifiManager.setWifiEnabled(false);
        }
        if (FirebaseAuth.getInstance().getUid() == null) {
            jobFinished(params, true);
            Log.d(TAG, "Job Finished: User Logged Out");

            return false;
        }
        else if (isTargetBSSID_InRange) {
            startMarkingAttendance(params);
            Log.d(TAG, "Target BSSID in Range");
            return true;
        }
        else {
            Log.d(TAG, "Target BSSID NOT in range");
            makeUserOffline(params);
            return false;
        }
    }

    public void makeUserOffline(JobParameters params){
        //Marking the user as offline if WiFi not in range
        databaseReferenceUser = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getUid());

        //Shared Preference to get USER details
        SharedPreferences pref = getSharedPreferences("User", MODE_PRIVATE);
        String storedUsername = pref.getString("username", null);
        String storedemail = pref.getString("email", null);
        String storedAdmin = pref.getString("admin", null);
        String storedTotal = pref.getString("total", null);
        String storeduid = pref.getString("uid", null);

        if(!storeduid.isEmpty()){

            User offlineUser = new User(storedemail, storedUsername, storedAdmin, "0", String.valueOf(storedTotal), storeduid);
            //Making user Online
            databaseReferenceUser.setValue(offlineUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                        Log.d(TAG, "FireBase: User Status Update Successful");
                }
            });
        }

        //Notification Manager
        notificationManager = NotificationManagerCompat.from(getApplicationContext());

        //Sending Notification
        Notification notification = new NotificationCompat.Builder(getApplicationContext(),Channel_ID)
                .setContentTitle("Attendance not marked.")
                .setContentText("Make sure wifi,location is enabled.")
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_wifi_black_24dp)
                .build();
        notificationManager.notify(1,notification);
        Log.d(TAG,"Notification Sent");


        //When Job is Finished
        jobFinished(params,true);
        Log.d(TAG,"Job Finished: User Offline");
    }

    public void startMarkingAttendance(JobParameters params){

        //We shouldn't run a service on the main thread.
        new Thread(new Runnable() {
            @Override
            public void run() {

                //Declerations
                Long timeSpent=1L;
                AttendanceSession attendanceSession;

                //FireBase Session (Making folder in session by USER UID)
                databaseReferenceSession = FirebaseDatabase.getInstance()
                        .getReference("Sessions")
                        .child(FirebaseAuth.getInstance().getUid());

                //FireBase User to add total time
                databaseReferenceUser = FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(FirebaseAuth.getInstance().getUid());

                //Notification Manager
                notificationManager = NotificationManagerCompat.from(getApplicationContext());

                //Shared Preference to get USER details
                SharedPreferences pref = getSharedPreferences("User", MODE_PRIVATE);
                final SharedPreferences.Editor editor = pref.edit();
                final String storedUsername = pref.getString("username", null);
                final String storedemail = pref.getString("email", null);
                final String storedAdmin = pref.getString("admin", null);
                final String storeduid = pref.getString("uid", null);

                //Updating the endTime regularly.
                endTime = String.valueOf(System.currentTimeMillis());

                //To check if previous session exists

                //1. First we get the end time of the previous session
                SharedPreferences preferencesSession = getSharedPreferences("Sessions", MODE_PRIVATE);
                SharedPreferences.Editor editorSession = preferencesSession.edit();
                String sessionID = preferencesSession.getString("uid",null);
                String storedSessionStartTime = preferencesSession.getString("start",null);
                Long storedLastTime = preferencesSession.getLong("end",0);

                //If previous session exists
                if(System.currentTimeMillis()<10*60*1000+storedLastTime){

                    //FireBase Update (End-Time & Total Time) Only
                    editorSession.putLong("end",System.currentTimeMillis());
                    editorSession.apply();
                    attendanceSession = new AttendanceSession(Long.valueOf(storedSessionStartTime),Long.valueOf(endTime));

                    Log.d(TAG,"Updating "+sessionID);
                    //Marking the User as online & updating total time

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

                        //Calculating Total Time
                        FirebaseDatabase.getInstance().getReference("Sessions")
                                .child(storeduid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        for (DataSnapshot sessionSnapShot : dataSnapshot.getChildren()) {
                                            AttendanceSession attendanceSession = sessionSnapShot.getValue(AttendanceSession.class);
                                            total += attendanceSession.getTotalTime();
                                            editor.putString("total",String.valueOf(total));
                                            User activeUserTotal = new User(storedemail, storedUsername, storedAdmin, "1", String.valueOf(total), storeduid);

                                            //Making user Online & Updating Total
                                            databaseReferenceUser.setValue(activeUserTotal).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                        Log.d(TAG, "FireBase: User Status Update Successful");
                                                }
                                            });

                                            int timeSpent = Math.round(attendanceSession.totalTime/60000);
                                            //Sending Notification
                                            Notification notification = new NotificationCompat.Builder(getApplicationContext(),Channel_ID)
                                                    .setContentTitle("Active Session")
                                                    .setContentText("Session Time: "+timeSpent+" minutes")
                                                    .setOnlyAlertOnce(true)
                                                    .setSmallIcon(R.drawable.ic_wifi_black_24dp)
                                                    .build();
                                            notificationManager.notify(1,notification);
                                            Log.d(TAG,"Notification Sent");
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.d(TAG, "FireBase: User Status Update Failed.");
                                    }
                        });
                    }

                }

                //If previous session doesn't exist
                else{

                    //Clearing previous Shared Preference
                    editorSession.clear();
                    editorSession.apply();

                    //Generation of new UID for Session
                    sessionID = databaseReferenceSession.push().getKey();
                    Log.d(TAG,"Generating "+sessionID);

                    //Shared Preference to get SESSION details
                    editorSession.putString("uid",sessionID);
                    editorSession.putString("start",endTime);
                    editorSession.putLong("end",System.currentTimeMillis());
                    editorSession.apply();

                    //FireBase Update (End-Time & Total Time) Only
                    attendanceSession = new AttendanceSession(Long.valueOf(endTime),Long.valueOf(endTime));

                    FirebaseDatabase.getInstance().getReference("Sessions")
                            .child(storeduid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    for (DataSnapshot sessionSnapShot : dataSnapshot.getChildren()) {
                                        AttendanceSession attendanceSession = sessionSnapShot.getValue(AttendanceSession.class);
                                        total += attendanceSession.getTotalTime();
                                        editor.putString("total",String.valueOf(total));
                                        User activeUserTotal = new User(storedemail, storedUsername, storedAdmin, "1", String.valueOf(total), storeduid);
                                        //Making user Online
                                        databaseReferenceUser.setValue(activeUserTotal).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                    Log.d(TAG, "FireBase: User Status Update Successful");
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d(TAG, "FireBase: User Status Update Failed.");
                                }
                            });
                }

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

                    //Sending Notification
                    Notification notification = new NotificationCompat.Builder(getApplicationContext(),Channel_ID)
                            .setContentTitle("Active Session")
                            .setContentText("Session Time: "+timeSpent+" second")
                            .setOnlyAlertOnce(true)
                            .setSmallIcon(R.drawable.ic_wifi_black_24dp)
                            .build();
                    notificationManager.notify(1,notification);
                    Log.d(TAG,"Notification Sent");
                }
            }
        }).start();

        //When Job is Finished
        jobFinished(params,true);
        Log.d(TAG,"Job Finished: Data sent to FireBase");
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
            else if (BSSID.toLowerCase().equals(targetBSSIDICT08.toLowerCase())) {
                isTargetBSSID_InRange = true;
                position=i;
            }
        }
        //Removes the selected SSID
        if(position!=-1)
            results.remove(position);
    }
}