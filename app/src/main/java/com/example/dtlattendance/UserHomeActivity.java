package com.example.dtlattendance;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;

public class UserHomeActivity extends AppCompatActivity {

    //Declaration UI elements
    BottomNavigationView bottomNavigationUser;

    //Declaring an instance of FireBase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        //Linking up the XML & Java objects
        bottomNavigationUser = findViewById(R.id.bottomNavigationUser);

        // Initialization FireBase Auth
        mAuth = FirebaseAuth.getInstance();

        //Tint to null
        bottomNavigationUser.setItemIconTintList(null);

        //Setting up default fragment
        getSupportFragmentManager().beginTransaction().add(R.id.frameLayoutUser,new HomeFragment()).commit();
        bottomNavigationUser.setSelectedItemId(R.id.navigationHome);

        //Setting a listener for BottomNavigation
        bottomNavigationUser.setOnNavigationItemSelectedListener(navListener);
    }

    //Bottom Navigation Changing View Listener switching to different Fragments
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;

                    switch (menuItem.getItemId()){
                        case R.id.navigationHome:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.navigationHistory:
                            selectedFragment = new HistoryFragment();
                            break;
                        case R.id.navigationLeaderBoard:
                            selectedFragment = new LeaderBoardFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutUser,selectedFragment).commit();
                    return true;
                }
            };

    // Sends the user back to Log In Screen If Not Logged In
    @Override
    public void onStart() {
        super.onStart();
        //Check if user is not signed in and update UI accordingly.
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(UserHomeActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }

    }


}
