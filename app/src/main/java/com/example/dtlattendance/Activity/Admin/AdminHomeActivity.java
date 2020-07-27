package com.example.dtlattendance.Activity.Admin;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.dtlattendance.Fragments.HistoryFragment;
import com.example.dtlattendance.Adapter.LeaderBoardList;
import com.example.dtlattendance.Activity.Common.LoginActivity;
import com.example.dtlattendance.R;
import com.example.dtlattendance.Model.User;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminHomeActivity extends AppCompatActivity {

    //Declaration UI elements
    FloatingActionButton floatingActionButtonLogOut;
    RelativeLayout relativeLayoutAdminHistory,relativeLayoutAdminLeaderBoard;
    Button buttonGoBack,buttonAdminRefresh;
    List<User> leaderBoardList;
    ListView leaderBoardSession;

    //Declaring an instance of FireBase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        //Setting up the UI
        floatingActionButtonLogOut = findViewById(R.id.floatingActionButtonLogOut);
        leaderBoardSession = findViewById(R.id.listViewAdminLeaderBoard);
        relativeLayoutAdminHistory = findViewById(R.id.relativeLayoutAdminHistory);
        relativeLayoutAdminLeaderBoard = findViewById(R.id.relativeLayoutAdminLeaderBoard);
        buttonGoBack = findViewById(R.id.buttonGoBack);
        buttonAdminRefresh = findViewById(R.id.buttonAdminRefresh);
        leaderBoardList = new ArrayList<>();


        // Initialization FireBase Auth
        mAuth = FirebaseAuth.getInstance();

        //LeaderBoard Display
        FirebaseDatabase.getInstance().getReference("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        leaderBoardList.clear();

                        for(DataSnapshot sessionSnapShot: dataSnapshot.getChildren()){
                            User user = sessionSnapShot.getValue(User.class);
                            if(!user.getAdmin().equals("1"))
                                leaderBoardList.add(user);
                        }
                        try {
                            Collections.sort(leaderBoardList,User.userTotal);
                            SharedPreferences sharedPreferences = getSharedPreferences("MaxTotal", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("max",Integer.valueOf(Collections.min(leaderBoardList,User.userTotal).getTotal()));
                            editor.apply();
                            LeaderBoardList adapter = new LeaderBoardList(AdminHomeActivity.this,leaderBoardList);
                            leaderBoardSession.setAdapter(adapter);
                        }catch (Exception e){
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //On click listener for listView
        leaderBoardSession.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String uid = leaderBoardList.get(position).uid;
                //Toast.makeText(AdminHomeActivity.this, ""+uid, Toast.LENGTH_SHORT).show();
                relativeLayoutAdminLeaderBoard.setVisibility(View.GONE);
                relativeLayoutAdminHistory.setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction().add(R.id.frameLayoutAdmin,new HistoryFragment()).commit();
                SharedPreferences sharedPreferences = getSharedPreferences("history",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("uid",uid);
                editor.apply();
            }
        });

        buttonGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameLayoutAdmin);
                if(fragment != null)
                    getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                relativeLayoutAdminLeaderBoard.setVisibility(View.VISIBLE);
                relativeLayoutAdminHistory.setVisibility(View.GONE);
            }
        });

        //On click listener for log out
        floatingActionButtonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
                scheduler.cancel(123);

                //Clearing data from Shared Preference
                SharedPreferences pref = getSharedPreferences("User",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.commit();

                Intent intent = new Intent(AdminHomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //On click listener for refresh
        buttonAdminRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LeaderBoard Display
                FirebaseDatabase.getInstance().getReference("Users")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                leaderBoardList.clear();

                                for(DataSnapshot sessionSnapShot: dataSnapshot.getChildren()){
                                    User user = sessionSnapShot.getValue(User.class);
                                    if(!user.getAdmin().equals("1"))
                                        leaderBoardList.add(user);
                                }
                                try {
                                    Collections.sort(leaderBoardList,User.userTotal);
                                    SharedPreferences sharedPreferences = getSharedPreferences("MaxTotal", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt("max",Integer.valueOf(Collections.min(leaderBoardList,User.userTotal).getTotal()));
                                    editor.apply();
                                    LeaderBoardList adapter = new LeaderBoardList(AdminHomeActivity.this,leaderBoardList);
                                    leaderBoardSession.setAdapter(adapter);
                                }catch (Exception e){
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        //Check if user is not signed and update UI accordingly.
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(AdminHomeActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
