package com.example.dtlattendance;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

public class HomeFragment extends Fragment {

    ToggleButton buttonStartSession;
    AnimationDrawable wifiAnimation;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        buttonStartSession = view.findViewById(R.id.buttonStartSession);

        //Media player for button sound effect
        final MediaPlayer buttonSoundOn = MediaPlayer.create(getActivity(),R.raw.wifi_onn);
        final MediaPlayer buttonSoundOff = MediaPlayer.create(getActivity(),R.raw.wifi_off);

        if (isMyServiceRunning(WifiService.class)) {
            buttonStartSession.setChecked(true);
        }

        //On click listener for Start/Stop Wifi Button
        buttonStartSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonStartSession.isChecked()){
                    //Animation of the Wi-Fi Button
                    buttonStartSession.setBackgroundResource(R.drawable.animation_wifi);
                    wifiAnimation = (AnimationDrawable) buttonStartSession.getBackground();
                    wifiAnimation.setOneShot(true);

                    Toast.makeText(getActivity(), "Start", Toast.LENGTH_SHORT).show();
                    wifiAnimation.start();
                    buttonSoundOn.start();
                    startService(v);
                }
                else{
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
        String input = "HE HE HE";
        Intent serviceIntent = new Intent(getActivity(),WifiService.class);
        serviceIntent.putExtra("inputExtra",input);
        getActivity().startService(serviceIntent);
    }

    public void stopService(View view){
        Intent serviceIntent = new Intent(getActivity(),WifiService.class);
        getActivity().stopService(serviceIntent);
        //Shared Preference
        SharedPreferences pref = getActivity().getSharedPreferences("time", Context.MODE_PRIVATE);
        String storedStartTime = pref.getString("startTime",null);
        String storedEndTime = pref.getString("endTime",null);
        Toast.makeText(getActivity(), storedStartTime+"\n"+storedEndTime, Toast.LENGTH_SHORT).show();
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

}
