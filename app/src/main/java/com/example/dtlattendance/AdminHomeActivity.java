package com.example.dtlattendance;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class AdminHomeActivity extends AppCompatActivity {

    //Declaration UI elements
    Button buttonAdminLogOut;

    //Declaring an instance of FireBase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        //Linking up the XML & Java objects
        buttonAdminLogOut = findViewById(R.id.buttonAdminLogOut);

        // Initialization FireBase Auth
        mAuth = FirebaseAuth.getInstance();

        //On click listener for log out
        buttonAdminLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(AdminHomeActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
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
