package com.example.dtlattendance;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class HomeFragment extends Fragment {

    Button buttonStartSession,buttonStopSession;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        buttonStopSession = view.findViewById(R.id.buttonStopSession);
        buttonStartSession = view.findViewById(R.id.buttonStartSession);

        //On click listener for Stop Button
        buttonStopSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Stop", Toast.LENGTH_SHORT).show();
            }
        });

        //On click listener for Start Button
        buttonStartSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Start", Toast.LENGTH_SHORT).show();
                startService(v);
            }
        });
        return view;
    }

    public void startService(View view){
        //String input = "Hehe";
        //serviceIntent.putExtra("inputExtra",input);
        Intent serviceIntent = new Intent(getActivity(),WifiService.class);
        getActivity().startService(serviceIntent);
    }

    public void stopService(Intent serviceIntent){
        getActivity().stopService(serviceIntent);
    }
}
