package com.example.dtlattendance;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class LoginActivity extends AppCompatActivity {

    //Declaration of UI elements
    RelativeLayout relativeLayoutSplash,relativeLayoutLogin;
    EditText editTextLoginEmail,editTextLoginPassword;
    TextInputLayout textInputLoginPassword;
    Button buttonLogin,buttonForgotPassword;
    ImageButton buttonSwitchToRegister;
    ProgressBar progressBarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Linking the UI wiht XML file
        relativeLayoutSplash = findViewById(R.id.relativeLayoutSplash);
        relativeLayoutLogin = findViewById(R.id.relativeLayoutLogin);
        editTextLoginEmail = findViewById(R.id.editTextLoginEmail);
        editTextLoginPassword = findViewById(R.id.editTextLoginPassword);
        textInputLoginPassword = findViewById(R.id.textInputLoginPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonForgotPassword = findViewById(R.id.buttonForgotPassword);
        buttonSwitchToRegister = findViewById(R.id.buttonSwitchToRegister);
        progressBarLogin = findViewById(R.id.progressBarLogin);

        //Log in Button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //Forgot Password
        buttonForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //Switches the Intent To Register Activity
        buttonSwitchToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
