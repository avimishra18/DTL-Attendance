package com.example.dtlattendance;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    //Declaration of UI elements
    RelativeLayout relativeLayoutSplash,relativeLayoutLogin;
    EditText editTextLoginEmail,editTextLoginPassword;
    TextInputLayout textInputLoginPassword;
    Button buttonLogin,buttonForgotPassword;
    ImageButton buttonSwitchToRegister;
    ProgressBar progressBarLogin;

    //Declaring an instance of FireBase Auth
    private FirebaseAuth mAuth;

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

        // Initialization FireBase Auth
        mAuth = FirebaseAuth.getInstance();

        //Extra Intent for Login
        Intent intentSwitchLogin = getIntent();
        String email = intentSwitchLogin.getStringExtra("email");
        editTextLoginEmail.setText(email);
        editTextLoginPassword.requestFocus();

        //Log in Button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v) {
                final String email = editTextLoginEmail.getText().toString();
                final String password = editTextLoginPassword.getText().toString();
                if(email.isEmpty())
                    editTextLoginEmail.setError("Please enter EMAIL");
                else if(!email.toLowerCase().contains("@kiit.ac.in"))
                    editTextLoginEmail.setError("Enter KIIT email only");
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                    editTextLoginEmail.setError("Please enter a valid EMAIL");
                else if(password.length()<6)
                    textInputLoginPassword.setError("Enter a valid Password");
                else
                    login(email,password);
            }
        });

        //Forgot Password
        buttonForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = editTextLoginEmail.getText().toString();
                if(email.isEmpty())
                    editTextLoginEmail.setError("Please enter EMAIL");
                else if(!email.toLowerCase().contains("@kiit.ac.in"))
                    editTextLoginEmail.setError("Enter KIIT email only");
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                    editTextLoginEmail.setError("Please enter a valid EMAIL");
                else {
                    progressBarVisible();   //Progress Bar Visible
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressBarGone();
                            if(task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, "Mail sent to "+email, Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(LoginActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
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

    @Override
    public void onStart() {
        super.onStart();
        //Check if user is signed in (non-null) and update UI accordingly.
        if (mAuth.getCurrentUser() != null) {
            //Sends the user to userhome
            Intent intent = new Intent(LoginActivity.this,UserHomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    //Main Login Function
    public void login(final String email, final String pass){

        progressBarVisible();   //Progress Bar Visible

        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    //Sends the user to userhome
                    Intent intent = new Intent(LoginActivity.this,UserHomeActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Toast.makeText(LoginActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressBarGone();
                }
            }
        });
    }

    public void progressBarGone(){
        //Progress Bar Gone
        progressBarLogin.setVisibility(View.GONE);
        buttonForgotPassword.setVisibility(View.VISIBLE);
        buttonLogin.setVisibility(View.VISIBLE);
    }

    public void progressBarVisible(){
        //Progress Bar Visible
        progressBarLogin.setVisibility(View.VISIBLE);
        buttonForgotPassword.setVisibility(View.GONE);
        buttonLogin.setVisibility(View.GONE);
    }
}
