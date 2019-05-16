package com.example.dtlattendance;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class UserHomeActivity extends AppCompatActivity {

    //Declaration UI elements
    Button buttonUserLogOut;

    //Declaring an instance of FireBase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        //Linking up the XML & Java objects
        buttonUserLogOut = findViewById(R.id.buttonUserLogOut);
        
        // Initialization FireBase Auth
        mAuth = FirebaseAuth.getInstance();

        //On click listener for log out
        buttonUserLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(UserHomeActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

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
