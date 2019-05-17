package com.example.dtlattendance;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import static com.example.dtlattendance.App.Channel_ID;

public class WifiService extends Service {

    //When the service starts
    @Override
    public void onCreate() {
        super.onCreate();
    }

    //When the service gets destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //On start of activity
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //final String input = intent.getStringExtra("inputExtra");

        //You can't run this service on the main thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent notificationIntent = new Intent(getApplicationContext(),HomeFragment.class);
                PendingIntent pendingIntent =  PendingIntent.getActivity(getApplicationContext(),
                        0, notificationIntent, 0);

                Notification notification = new NotificationCompat.Builder(getApplicationContext(),Channel_ID)
                        .setContentTitle("Example Service")
                        .setContentText("input")
                        .setSmallIcon(R.drawable.ic_wifi_black_24dp)
                        .setContentIntent(pendingIntent)
                        .build();

                startForeground(1,notification);
            }
        }).start();

        //if work is done
        //stopSelf();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
