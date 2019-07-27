package com.example.dtlattendance;

import android.Manifest;
import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;


public class HomeFragment extends Fragment {

    //String of the WIFI BSSID
    private static final String targetBSSIDDTL = "f8:1a:67:97:a5:8e";
    //private static final String targetBSSID = "00:23:68:17:65:d0";
    private static final String targetBSSID = "02:15:b2:00:01:00";
    private static final String TAG = "Home Fragment";
    private boolean isTargetBSSID_InRange = false;
    WifiManager wifiManager;

    //FireBase
    DatabaseReference databaseReferenceUser;
    User activeUserLogOut;

    public ToggleButton buttonStartSession;
    AnimationDrawable wifiAnimation;
    long total =0L;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        buttonStartSession = view.findViewById(R.id.buttonStartSession);
        FloatingActionButton floatingActionButtonLogOut = view.findViewById(R.id.floatingActionButtonLogOut);

        //Explicitly calling
        startService();

        //Initialization of Wifi Manager
        wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //If user is online then only button is on
        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            User user = dataSnapshot.getValue(User.class);
                            if(user.getOnline().equals("1")) {
                                buttonStartSession.setChecked(true);
                            }
                            else{
                                buttonStartSession.setChecked(false);
                                Log.d(TAG,"Button Off. User is offline.");
                                //Marking User as offline if service not running
                                //Shared Preference to get other details
                                /*
                                SharedPreferences pref = getContext().getSharedPreferences("User", Context.MODE_PRIVATE);
                                String storedUsername = pref.getString("username", null);
                                String storedemail = pref.getString("email", null);
                                String storedAdmin = pref.getString("admin", null);
                                String storedTotal = pref.getString("total", null);
                                String storedUid = pref.getString("uid", null);

                                //Marking the User as offline
                                User activeUser = new User(storedemail, storedUsername, storedAdmin, "0", storedTotal, storedUid);
                                databaseReferenceUser = FirebaseDatabase.getInstance()
                                        .getReference("Users")
                                        .child(FirebaseAuth.getInstance().getUid());
                                databaseReferenceUser.setValue(activeUser);
                                */
                            }
                        }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG,"Can't read whether user is online or offline.");

                    }
                });
        /*
        //If Service is not running then button gets disabled
        if (isMyServiceRunning(AttendanceJobScheduler.class)) {
            buttonStartSession.setChecked(true);
        } else {
            //Marking User as offline if service not running
            //Shared Preference to get other details
            SharedPreferences pref = getContext().getSharedPreferences("User", Context.MODE_PRIVATE);
            String storedUsername = pref.getString("username", null);
            String storedemail = pref.getString("email", null);
            String storedAdmin = pref.getString("admin", null);
            String storedTotal = pref.getString("total", null);
            String storedUid = pref.getString("uid", null);

            //Marking the User as offline
            User activeUser = new User(storedemail, storedUsername, storedAdmin, "0", storedTotal, storedUid);
            databaseReferenceUser = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(FirebaseAuth.getInstance().getUid());
            databaseReferenceUser.setValue(activeUser);
        }
        */
        //Start WiFi Scan
        wifiScan();

        //On click listener for Start/Stop Wifi Button
        buttonStartSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonStartSession.isChecked()) {
                    if (isLocationEnabled(getContext())) {
                        if (wifiManager.isWifiEnabled()) {
                            if (!isMyServiceRunning(AttendanceJobScheduler.class)&& isTargetBSSID_InRange) {
                                //Animation of the Wi-Fi Button
                                buttonStartSession.setBackgroundResource(R.drawable.animation_wifi);
                                wifiAnimation = (AnimationDrawable) buttonStartSession.getBackground();
                                wifiAnimation.setOneShot(true);

                                wifiAnimation.start();
                                startService();
                            } else {
                                //If not WiFi is not scanned, we display the same as message.
                                buttonStartSession.setChecked(false);
                                showDialog(2);
                            }
                        } else {
                            //If WiFi disabled, we display the same as message.
                            buttonStartSession.setChecked(false);
                            showDialog(1);
                        }
                    } else if (!isLocationEnabled(getContext())) {
                        //If location disabled, we display the same as message.
                        buttonStartSession.setChecked(false);
                        showDialog(0);
                    }
                }
                //Marking User As Offline
                else if (!buttonStartSession.isChecked())
                {
                    buttonStartSession.setBackgroundResource(R.drawable.wifi_selector);
                    stopService();

                    //Shared Preference to get USER details
                    SharedPreferences pref = getActivity().getSharedPreferences("User",Context.MODE_PRIVATE);
                    final SharedPreferences.Editor editor = pref.edit();
                    final String storedUsername = pref.getString("username", null);
                    final String storedemail = pref.getString("email", null);
                    final String storedAdmin = pref.getString("admin", null);
                    final String storeduid = pref.getString("uid", null);

                    //Calculating Total Time
                    FirebaseDatabase.getInstance().getReference("Sessions")
                            .child(storeduid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    for (DataSnapshot sessionSnapShot : dataSnapshot.getChildren()) {
                                        AttendanceSession attendanceSession = sessionSnapShot.getValue(AttendanceSession.class);
                                        total += attendanceSession.getTotalTime();
                                    }
                                    Log.d(TAG,"Total 1 = "+total);
                                    activeUserLogOut = new User(storedemail, storedUsername, storedAdmin, "0",String.valueOf(total), storeduid);


                                    if(FirebaseAuth.getInstance().getUid()!=null)
                                        databaseReferenceUser = FirebaseDatabase.getInstance()
                                                .getReference("Users")
                                                .child(FirebaseAuth.getInstance().getUid());

                                    //Making user Online & Updating Total
                                    if(FirebaseAuth.getInstance().getUid()!=null)
                                        databaseReferenceUser.setValue(activeUserLogOut).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                    Log.d(TAG, "FireBase: User Status Update Successful.");
                                                else
                                                    Log.d(TAG,"FireBase: Update failed.");
                                            }
                                        });
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d(TAG, "FireBase: User Status Update Failed.");
                                }
                            });

                    //New Code
                    //If Log Out then external = 0
                    External newExternal = new External(0);
                    FirebaseDatabase.getInstance().getReference("External").child(FirebaseAuth.getInstance().getUid()).setValue(newExternal);
                }
            }
        });

        floatingActionButtonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //If Log Out then external = 0
                External newExternal = new External(0);
                FirebaseDatabase.getInstance().getReference("External").child(FirebaseAuth.getInstance().getUid()).setValue(newExternal);

                if (isMyServiceRunning(AttendanceJobScheduler.class)) {
                    buttonStartSession.setBackgroundResource(R.drawable.wifi_selector);
                    stopService();
                }

                //Shared Preference to get USER details
                SharedPreferences pref = getActivity().getSharedPreferences("User",Context.MODE_PRIVATE);
                final SharedPreferences.Editor editor = pref.edit();
                final String storedUsername = pref.getString("username", null);
                final String storedemail = pref.getString("email", null);
                final String storedAdmin = pref.getString("admin", null);
                final String storeduid = pref.getString("uid", null);
                //Calculating Total Time
                FirebaseDatabase.getInstance().getReference("Sessions")
                        .child(storeduid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot sessionSnapShot : dataSnapshot.getChildren()) {
                                    AttendanceSession attendanceSession = sessionSnapShot.getValue(AttendanceSession.class);
                                    total += attendanceSession.getTotalTime();
                                }
                                Log.d(TAG,"Total 1 = "+total);
                                activeUserLogOut = new User(storedemail, storedUsername, storedAdmin, "0",String.valueOf(total), storeduid);


                                if(FirebaseAuth.getInstance().getUid()!=null)
                                    databaseReferenceUser = FirebaseDatabase.getInstance()
                                            .getReference("Users")
                                            .child(FirebaseAuth.getInstance().getUid());

                                //Making user Online & Updating Total
                                if(FirebaseAuth.getInstance().getUid()!=null)
                                    databaseReferenceUser.setValue(activeUserLogOut).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                                Log.d(TAG, "FireBase: User Status Update Successful.");
                                            else
                                                Log.d(TAG,"FireBase: Update failed.");
                                        }
                                    });

                                //Clearing data from Shared Preference
                                editor.clear();
                                editor.apply();

                                SharedPreferences preferences = getActivity().getSharedPreferences("Sessions",Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor1 = preferences.edit();
                                editor1.clear();
                                editor1.apply();

                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                                //editor.putString("total",String.valueOf(total));
                                //editor.apply();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d(TAG, "FireBase: User Status Update Failed.");
                            }
                        });

                //String storedTotal = pref.getString("total",null);
                //Log.d(TAG,"Stored Total = "+storedTotal);
                Log.d(TAG,"Total 2 = "+total);
            }
        });
        return view;
    }

    //If scan successful
    private void wifiScan() {
        List<ScanResult> results = wifiManager.getScanResults();
        int position=-1;

        if(results.size()==0)
            Log.d(TAG,"No connection found in range");

        for (int i = 0; i < results.size(); i++) {
            String SSID = results.get(i).SSID;
            String BSSID = results.get(i).BSSID;
            Log.d(TAG,i+". "+SSID+": "+BSSID);

            if (BSSID.toLowerCase().equals(targetBSSID.toLowerCase())) {
                isTargetBSSID_InRange = true;
                position=i;
                Log.d(TAG,"Connection found in range");
            }
            else if (BSSID.toLowerCase().equals(targetBSSIDDTL.toLowerCase())) {
                isTargetBSSID_InRange = true;
                Log.d(TAG,"Connection found in range");
                position=i;
            }
        }

        //Removes the selected SSID
        if(position!=-1)
            results.remove(position);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 87);
            }
        }
    }

    //Starts the JOB (service)
    public void startService() {
        ComponentName componentName = new ComponentName(getActivity(),AttendanceJobScheduler.class);
        JobInfo info = new JobInfo.Builder(123,componentName)
                .setPersisted(true)
                .setPeriodic(15*60*1000)
                .build();

        JobScheduler scheduler = (JobScheduler) getActivity().getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if(resultCode == JobScheduler.RESULT_SUCCESS){
            Log.d(TAG,"Job Scheduled");
        }
        else{
            Log.d(TAG,"Job Scheduled Failed");
        }
    }

    //End service function
    public void stopService() {
        JobScheduler scheduler = (JobScheduler) getActivity().getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(123);
        Log.d(TAG,"Job Cancelled");
    }

    //To check if the service is running
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    //To check whether the location is enabled
    public static Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    //To show the dialog
    public void showDialog(int alertType) {
        if (alertType == 0) {
            LocationDialog locationDialog = new LocationDialog();
            locationDialog.show(getFragmentManager(), "Location Dialog");
        } else if (alertType == 1) {
            WifiDialog wifiDialog = new WifiDialog();
            wifiDialog.show(getFragmentManager(), "Wifi Dialog");
        }
        else if (alertType == 2) {
            NotMarkedDialog notMarkedDialog = new NotMarkedDialog();
            notMarkedDialog.show(getFragmentManager(), "Not Marked Dialog");
        }
    }
}
