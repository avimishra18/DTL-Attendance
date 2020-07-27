package com.example.dtlattendance.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.dtlattendance.Adapter.LeaderBoardList;
import com.example.dtlattendance.Model.User;
import com.example.dtlattendance.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderBoardFragment extends Fragment {

    //Declarations
    List<User> leaderBoardList;
    ListView leaderBoardSession;
    String TAG = "LeaderBoard";


    private View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null) {
            view = inflater.inflate(R.layout.fragment_leader_board, container, false);
            leaderBoardSession = view.findViewById(R.id.listViewLeaderBoard);
            leaderBoardList = new ArrayList<>();
        }
        else{
            ((ViewGroup) view.getParent()).removeView(view);
        }
        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"Started");
        FirebaseDatabase.getInstance().getReference("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        leaderBoardList.clear();
                        Log.d(TAG,"FireBase OnDataChanged");
                        for(DataSnapshot sessionSnapShot: dataSnapshot.getChildren()){
                            User user = sessionSnapShot.getValue(User.class);
                            user.total = String.valueOf(Long.valueOf(user.getTotal())/60000);
                            if(!user.getAdmin().equals("1"))
                                leaderBoardList.add(user);
                        }
                        try {
                            Log.d(TAG,"Try code execution ");
                            Collections.sort(leaderBoardList,User.userTotal);
                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("MaxTotal", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("max",Integer.valueOf(Collections.min(leaderBoardList,User.userTotal).getTotal()));
                            editor.apply();
                            LeaderBoardList adapter = new LeaderBoardList(getActivity(),leaderBoardList);
                            leaderBoardSession.setAdapter(adapter);
                        }catch (Exception e){
                            Log.d(TAG,"Error = "+e.getMessage());
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        Log.d(TAG,"User Loading Screen");
        User loadingScreen = new User("","Loading...","","","-1","");
        leaderBoardList.add(loadingScreen);
        LeaderBoardList adapter = new LeaderBoardList(getActivity(),leaderBoardList);
        leaderBoardSession.setAdapter(adapter);
    }
}