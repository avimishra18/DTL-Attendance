package com.example.dtlattendance;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class RegisterActivity extends AppCompatActivity {

    EditText editTextLoginUsername,editTextLoginEmail,editTextLoginPassword,editTextLoginRePassword;
    TextInputLayout textInputLoginPassword,textInputLoginRePassword;
    ProgressBar progressBarLogin;
    Button buttonRegister,buttonSwitchToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextLoginEmail = findViewById(R.id.editTextLoginEmail);
        editTextLoginUsername= findViewById(R.id.editTextLoginUsername);
        editTextLoginPassword = findViewById(R.id.editTextLoginPassword);
        editTextLoginRePassword = findViewById(R.id.editTextLoginRePassword);
        textInputLoginPassword = findViewById(R.id.textInputLoginPassword);
        textInputLoginRePassword = findViewById(R.id.textInputLoginRePassword);
        progressBarLogin = findViewById(R.id.progressBarLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonSwitchToLogin = findViewById(R.id.buttonSwitchToLogin);

        // Register Button
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // Switches the Intent Back to Log In Activity
        buttonSwitchToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
