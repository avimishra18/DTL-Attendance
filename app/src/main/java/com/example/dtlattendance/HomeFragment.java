package com.example.dtlattendance;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class HomeFragment extends Fragment {

    //String of the WIFI SSID
    final String SSID="SARA";
    private boolean connect=false;

    //FireBase
    DatabaseReference databaseReferenceUser;

    public ToggleButton buttonStartSession;
    AnimationDrawable wifiAnimation;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        buttonStartSession = view.findViewById(R.id.buttonStartSession);

        //Media player for button sound effect
        final MediaPlayer buttonSoundOn = MediaPlayer.create(getActivity(),R.raw.wifi_onn);
        final MediaPlayer buttonSoundOff = MediaPlayer.create(getActivity(),R.raw.wifi_off);

        //If Service is not running then button gets disabled
        if (isMyServiceRunning(WifiService.class)) {
            buttonStartSession.setChecked(true);
        }

        //On click listener for Start/Stop Wifi Button
        buttonStartSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonStartSession.isChecked()){
                    if(isLocationEnabled(getContext())){
                        if((isConnectedTo(SSID,getActivity()))&&connect){
                            if (!isMyServiceRunning(WifiService.class)) {
                                //Animation of the Wi-Fi Button
                                buttonStartSession.setBackgroundResource(R.drawable.animation_wifi);
                                wifiAnimation = (AnimationDrawable) buttonStartSession.getBackground();
                                wifiAnimation.setOneShot(true);

                                Toast.makeText(getActivity(), "Start", Toast.LENGTH_SHORT).show();
                                wifiAnimation.start();
                                buttonSoundOn.start();
                                startService(v);
                            }
                            else {
                                buttonStartSession.setChecked(false);
                                Toast.makeText(getContext(), "Service already running", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            //If not enabled we uncheck the button
                            buttonStartSession.setChecked(false);
                            showDialog(1);
                        }
                    }
                    else if(!isLocationEnabled(getContext())){
                        //If not enabled we uncheck the button
                        buttonStartSession.setChecked(false);
                        showDialog(0);
                    }
                }
                else if(!buttonStartSession.isChecked()){
                    Toast.makeText(getActivity(), "Stop", Toast.LENGTH_SHORT).show();
                    buttonSoundOff.start();
                    buttonStartSession.setBackgroundResource(R.drawable.wifi_selector);
                    stopService(v);
                }
            }
        });
        return view;
    }

    public void startService(View view){
        Intent serviceIntent = new Intent(getActivity(),WifiService.class);
        getActivity().startService(serviceIntent);

    }

    public void stopService(View view){
        Intent serviceIntent = new Intent(getActivity(),WifiService.class);
        getActivity().stopService(serviceIntent);
        /*
        //Shared Preference to get other details
        SharedPreferences pref = getActivity().getSharedPreferences("User",Context.MODE_PRIVATE);
        String storedUsername =     pref.getString("username",null);
        String storedemail =     pref.getString("email",null);
        String storedAdmin =  pref.getString("admin",null);

        //Marking the User as offline
        User activeUser = new User(storedemail,storedUsername,storedAdmin,"0");
        databaseReferenceUser.setValue(activeUser);
        */
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
    //To get WiFi SSID
    public boolean isConnectedTo(String ssid, Context context) {
        boolean retVal = false;
        ssid="\""+ssid+"\"";
        String currentConnectedSSID;
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifi.getConnectionInfo();
        if (wifiInfo != null) {
            currentConnectedSSID = wifiInfo.getSSID();
            if (currentConnectedSSID != null && ssid.equals(currentConnectedSSID)) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(wifiStateReceiverHome,intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(wifiStateReceiverHome);

    }

    //To check wether the location is enabled
    public static Boolean isLocationEnabled(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);

        }
    }

    //Broadcast Receiver which detects change in WiFi State
    private BroadcastReceiver wifiStateReceiverHome = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_UNKNOWN);

            switch (wifiStateExtra){
                case WifiManager.WIFI_STATE_DISABLED:
                    buttonStartSession.setChecked(false);
                    buttonStartSession.setBackgroundResource(R.drawable.wifi_selector);
                    connect=false;
                    stopService(new View(getContext()));
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    connect=true;
                    break;
            }
        }
    };

    //To show the dialog
    public void showDialog(int alertType){
        if(alertType==0){
            LocationDialog locationDialog = new LocationDialog();
            locationDialog.show(getFragmentManager(),"Location Dialog");
        }
        else if(alertType==1){
            WifiDialog wifiDialog = new WifiDialog();
            wifiDialog.show(getFragmentManager(),"Wifi Dialog");
        }

    }
}