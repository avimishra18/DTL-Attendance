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
    private long count=0;
    private String startTime, endTime,sessionID;
    private boolean connected=true;
    private static final String targetBSSID = "02:15:b2:00:01:00";
    //private static final String targetBSSID = "00:23:68:17:65:d0";
    private boolean isTargetBSSID_InRange = false;

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

                //FireBase Session
                databaseReferenceSession = FirebaseDatabase.getInstance()
                        .getReference("Sessions")
                        .child(FirebaseAuth.getInstance().getUid());

                //Generation of UID
                sessionID = databaseReferenceSession.push().getKey();

                //FireBase to show if user is online
                databaseReferenceUser = FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(FirebaseAuth.getInstance().getUid());

                //Shared Preference to get other details
                SharedPreferences pref = getSharedPreferences("User",MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                String storedUsername =     pref.getString("username",null);
                String storedemail =     pref.getString("email",null);
                String storedAdmin =  pref.getString("admin",null);
                String storedTotal = pref.getString("total",null);
                String storeduid = pref.getString("uid",null);


                //While our broadcast receiver is true
                while (connected){

                    try {
                        //Notification Manager
                        Intent notificationIntent = new Intent(getApplicationContext(),HomeFragment.class);
                        PendingIntent pendingIntent =  PendingIntent.getActivity(getApplicationContext(),
                                0, notificationIntent, 0);

                        Integer minutes = Math.round(count/60);
                        Notification notification = new NotificationCompat.Builder(getApplicationContext(),Channel_ID)
                                .setContentTitle("Active Session")
                                .setContentText("Current session time: " +minutes+" minutes "+count%60+" seconds")
                                .setOnlyAlertOnce(true)
                                .setSmallIcon(R.drawable.ic_wifi_black_24dp)
                                .setContentIntent(pendingIntent)
                                .build();

                        startForeground(1,notification);

                        //Updating the endTime regularly if in case OnDestroy not called.
                        endTime = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

                        //FireBase Update (End-Time & Total Time) Only
                        AttendanceSession attendanceSession = new AttendanceSession(startTime,endTime,Long.toString(count));
                        if(sessionID != null) {

                            //Marking the User as online & updating total time
                            Long total = count+Integer.valueOf(storedTotal);
                            editor.putString("total",String.valueOf(total));
                            editor.apply();
                            User activeUser = new User(storedemail,storedUsername,storedAdmin,"1",String.valueOf(total),storeduid);
                            if (count % 11 == 0) {
                                //Updating the session
                                databaseReferenceSession
                                        .child(sessionID)
                                        .setValue(attendanceSession)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                    Log.d("Info","Successful");
                                            }
                                        });
                                databaseReferenceUser.setValue(activeUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                            Log.d("Info","Successful");
                                    }
                                });;
                            }
                        }

                        //Thread sleeps for 1 second so that is equal to 1s of actual time spent
                        Thread.sleep(1000);
                        count+=1;

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                //Code when Wi-Fi gets disconnected (i.e. connection=false)
                //Notification Manager
                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

                Integer minutes = Math.round(count/60);
                Notification finalNotification = new NotificationCompat.Builder(getApplicationContext(),Channel_ID)
                        .setContentTitle("Session Ended")
                        .setContentText("Total session time: " +minutes+" minutes "+count%60+" seconds")
                        .setSmallIcon(R.drawable.ic_wifi_black_24dp)
                        .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
                        .build();

                notificationManagerCompat.notify(1,finalNotification);
                try {
                } catch(IllegalArgumentException e) {
                    e.printStackTrace();
                }

                //FireBase
                AttendanceSession attendanceSession = new AttendanceSession(startTime,endTime,Long.toString(count));
                if(sessionID != null)
                    databaseReferenceSession.child(sessionID).setValue(attendanceSession);
                sessionID=null;

                //Marking the User as offline & updating scores
                String storedTotalUpdated = pref.getString("total",null);
                User activeUser = new User(storedemail,storedUsername,storedAdmin,"0",storedTotalUpdated,storeduid);
                databaseReferenceUser.setValue(activeUser);

            }
        }).start();
        //When Job is Finished
        jobFinished(params,true);
        Log.d(TAG,"Job Finished");
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG,"Job cancelled before completion");
        endTime = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        //FireBase Database
        databaseReferenceSession = FirebaseDatabase.getInstance()
                .getReference("Sessions")
                .child(FirebaseAuth.getInstance().getUid());

        //Code when Wi-Fi gets disconnected

        //Final Notification Generator
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        Integer minutes = Math.round(count/60);
        Notification finalNotification = new NotificationCompat.Builder(getApplicationContext(),Channel_ID)
                .setContentTitle("Session Ended")
                .setContentText("Total time: " +minutes+" minutes "+count%60+" seconds")
                .setSmallIcon(R.drawable.ic_wifi_black_24dp)
                .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
                .build();
        notificationManagerCompat.notify(1,finalNotification);
        //FireBase
        AttendanceSession attendanceSession = new AttendanceSession(startTime,endTime,Double.toString(count));
        databaseReferenceSession.child(sessionID).setValue(attendanceSession);
        sessionID=null;

        //Shared Preference to get other details
        SharedPreferences pref = getSharedPreferences("User",MODE_PRIVATE);
        String storedUsername =     pref.getString("username",null);
        String storedemail =     pref.getString("email",null);
        String storedAdmin =  pref.getString("admin",null);
        String storedTotal = pref.getString("total",null);
        String storeduid = pref.getString("uid",null);

        //Marking the User as offline & updating scores
        User activeUser = new User(storedemail,storedUsername,storedAdmin,"0",storedTotal,storeduid);
        databaseReferenceUser.setValue(activeUser);
        return true;
    }


    //If scan successful
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

