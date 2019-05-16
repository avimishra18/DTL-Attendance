package com.example.dtlattendance;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class AdminHomeActivity extends AppCompatActivity {

    //Declaring an instance of FireBase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        // Initialization FireBase Auth
        mAuth = FirebaseAuth.getInstance();
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
