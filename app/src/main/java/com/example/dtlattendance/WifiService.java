package com.example.dtlattendance;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.dtlattendance.App.Channel_ID;

public class WifiService extends Service {

    //Declarations
    private long count=0;
    private String startTime, endTime,sessionID;
    private boolean connected=true;

    //FireBase
    DatabaseReference databaseReferenceSession,databaseReferenceUser;

    //Broadcast Receiver which detects change in WiFi State
    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_UNKNOWN);

            switch (wifiStateExtra){
                case WifiManager.WIFI_STATE_ENABLED:
                    connected=true;
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    //Shared Preference to get other details
                    SharedPreferences pref = getSharedPreferences("User",Context.MODE_PRIVATE);
                    String storedUsername =     pref.getString("username",null);
                    String storedemail =     pref.getString("email",null);
                    String storedAdmin =  pref.getString("admin",null);
                    String storedTotal = pref.getString("total",null);
                    //Marking the User as offline
                    User activeUser = new User(storedemail,storedUsername,storedAdmin,"0",storedTotal);
                    databaseReferenceUser = FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(FirebaseAuth.getInstance().getUid());
                    databaseReferenceUser.setValue(activeUser);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    connected=false;
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        endTime = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        //FireBase Database
        databaseReferenceSession = FirebaseDatabase.getInstance()
                .getReference("Sessions")
                .child(FirebaseAuth.getInstance().getUid());

        //Code when Wi-Fi gets disconnected

        //Final Notification Generator
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());

        Notification finalNotification = new NotificationCompat.Builder(getApplicationContext(),Channel_ID)
                .setContentTitle("Session Ended")
                .setContentText("Total session time : " + Long.toString(count)+" seconds")
                .setSmallIcon(R.drawable.ic_wifi_black_24dp)
                .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
                .build();
        notificationManagerCompat.notify(1,finalNotification);
        try {
            unregisterReceiver(wifiStateReceiver);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
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

        //Marking the User as offline & updating scores
        User activeUser = new User(storedemail,storedUsername,storedAdmin,"0",storedTotal);
        databaseReferenceUser.setValue(activeUser);

        stopSelf();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        //We shouldn't run a service on the main thread.
        new Thread(new Runnable() {
            @Override
            public void run() {

                IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
                registerReceiver(wifiStateReceiver,intentFilter);

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

                //While our broadcast receiver is true
                while (connected){

                    try {
                        //Notification Manager
                        Intent notificationIntent = new Intent(getApplicationContext(),HomeFragment.class);
                        PendingIntent pendingIntent =  PendingIntent.getActivity(getApplicationContext(),
                                0, notificationIntent, 0);

                        Notification notification = new NotificationCompat.Builder(getApplicationContext(),Channel_ID)
                                .setContentTitle("Active Session")
                                .setContentText("Current session time: " + Long.toString(count)+" seconds")
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
                            //Updating the session
                            databaseReferenceSession.child(sessionID).setValue(attendanceSession);

                            //Marking the User as online & updating total time
                            Long total = count+Integer.valueOf(storedTotal);
                            editor.putString("total",String.valueOf(total));
                            editor.apply();
                            User activeUser = new User(storedemail,storedUsername,storedAdmin,"1",String.valueOf(total));
                            databaseReferenceUser.setValue(activeUser);
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

                Notification finalNotification = new NotificationCompat.Builder(getApplicationContext(),Channel_ID)
                        .setContentTitle("Session Ended")
                        .setContentText("Total session time : " + Long.toString(count)+" seconds")
                        .setSmallIcon(R.drawable.ic_wifi_black_24dp)
                        .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
                        .build();

                notificationManagerCompat.notify(1,finalNotification);
                try {
                    unregisterReceiver(wifiStateReceiver);
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
                User activeUser = new User(storedemail,storedUsername,storedAdmin,"0",storedTotalUpdated);
                databaseReferenceUser.setValue(activeUser);

                //Stop Self
                stopSelf();
            }
        }).start();
        return START_REDELIVER_INTENT;
    }

    //When the service gets created
    @Override
    public void onCreate() {
        startTime = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
