package com.example.dtlattendance;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.dtlattendance.App.Channel_ID;

public class WifiService extends Service {

    //Declarations
    private long count=0;
    private String startTime, endTime;

    //When the service gets created
    @Override
    public void onCreate() {

        //Shared Preference
        SharedPreferences pref = getSharedPreferences("time",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        //Main Code of the App
        startTime = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
        editor.putString("startTime",startTime);
        editor.apply();

        super.onCreate();
    }

    //When the service gets destroyed
    @Override
    public void onDestroy() {
        endTime = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        //Shared Preference End Time
        SharedPreferences pref = getSharedPreferences("time",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("endTime",endTime);
        editor.apply();
        //String storedStartTime = pref.getString("startTime",null);
        super.onDestroy();
    }


    //On start of activity
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String input = intent.getStringExtra("inputExtra");

        //You can't run this service on the main thread.
        new Thread(new Runnable() {
            @Override
            public void run() {

                count++;

                //While name of WI-FI is the same.
                while (true){

                    try {
                        count+=1;

                        //Notification Manager
                        Intent notificationIntent = new Intent(getApplicationContext(),HomeFragment.class);
                        PendingIntent pendingIntent =  PendingIntent.getActivity(getApplicationContext(),
                                0, notificationIntent, 0);

                        Notification notification = new NotificationCompat.Builder(getApplicationContext(),Channel_ID)
                                .setContentTitle("DTL Attendance")
                                .setContentText("Totoal Time =" + Long.toString(count))
                                .setOnlyAlertOnce(true)
                                .setSmallIcon(R.drawable.ic_wifi_black_24dp)
                                .setContentIntent(pendingIntent)
                                .build();

                        startForeground(1,notification);

                        //Updating the endTime regularly if in case OnDestroy is not called
                        endTime = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                        //Shared Preference End Time
                        SharedPreferences pref = getSharedPreferences("time",MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("endTime",endTime);
                        editor.apply();

                        //Thread sleeps for 1 second so that is equal to 1s of actual time spent
                        Thread.sleep(1000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

        //if work is done
        //stopSelf();
        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
