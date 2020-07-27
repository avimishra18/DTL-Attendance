package com.example.dtlattendance.Activity.User;

import android.content.Intent;
import androidx.annotation.NonNull;

import com.example.dtlattendance.Activity.Common.LoginActivity;
import com.example.dtlattendance.Model.External;
import com.example.dtlattendance.Model.User;
import com.example.dtlattendance.R;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    //Declaration
    EditText editTextRegisterUsername,editTextRegisterEmail,editTextRegisterPassword,editTextRegisterRePassword;
    TextInputLayout textInputRegisterPassword,textInputRegisterRePassword;
    ProgressBar progressBarRegister;
    Button buttonRegister,buttonSwitchToLogin;

    //Declaring an instance of FireBase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Setting up the UI elements
        editTextRegisterEmail = findViewById(R.id.editTextRegisterEmail);
        editTextRegisterUsername= findViewById(R.id.editTextRegisterUsername);
        editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword);
        editTextRegisterRePassword = findViewById(R.id.editTextRegisterRePassword);
        textInputRegisterPassword = findViewById(R.id.textInputRegisterPassword);
        textInputRegisterRePassword = findViewById(R.id.textInputRegisterRePassword);
        progressBarRegister = findViewById(R.id.progressBarRegister);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonSwitchToLogin = findViewById(R.id.buttonSwitchToLogin);

        //Initialisation of FireBase mAuth
        mAuth = FirebaseAuth.getInstance();

        // Register Button
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        // Switches the Intent Back to Log In Activity
        buttonSwitchToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Main Create Account Function
    public void createAccount(){
        final String username = editTextRegisterUsername.getText().toString();
        final String email = editTextRegisterEmail.getText().toString();
        final String password = editTextRegisterPassword.getText().toString();
        final String repassword = editTextRegisterRePassword.getText().toString();

        if(email.isEmpty())
            editTextRegisterEmail.setError("Please enter EMAIL");
        else if(!email.toLowerCase().contains("@kiit.ac.in"))
            editTextRegisterEmail.setError("Enter KIIT email only");
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            editTextRegisterEmail.setError("Enter a valid EMAIL");
        else if(username.length()<3)
            editTextRegisterUsername.setError("Enter a valid username");
        else if(password.length()<6)
            textInputRegisterPassword.setError("Enter a valid Password");
        else if(!password.equals(repassword))
            textInputRegisterRePassword.setError("Must match the previous entry");
        //If all conditions are true then
        else{
            //Progress Bar Visible
            buttonRegister.setVisibility(View.GONE);
            buttonSwitchToLogin.setVisibility(View.GONE);
            progressBarRegister.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                //We will store the additional fields in FireBase Database

                                //Creating a new object of User
                                User userNew = new User(email,username,mAuth.getUid());

                                //Storing it in the Database
                                FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()) // Under User new Child with name UID
                                    .setValue(userNew) //Sets the value of the child as user
                                    .addOnCompleteListener(new OnCompleteListener<Void>() { //On complete listener for custom fields
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent intentSwitchLogin = new Intent(RegisterActivity.this,LoginActivity.class);
                                                intentSwitchLogin.putExtra("email",email);
                                                startActivity(intentSwitchLogin);
                                                finish();
                                            }
                                            else{
                                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                //Progress Bar Gone
                                                progressBarRegister.setVisibility(View.GONE);
                                                buttonRegister.setVisibility(View.VISIBLE);
                                                buttonSwitchToLogin.setVisibility(View.VISIBLE);
                                            }

                                        }
                                    });

                                External newExternal = new External(0);
                                FirebaseDatabase.getInstance().getReference("External").child(FirebaseAuth.getInstance().getUid()).setValue(newExternal);
                            }
                            else{
                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                //Progress Bar Gone
                                progressBarRegister.setVisibility(View.GONE);
                                buttonRegister.setVisibility(View.VISIBLE);
                                buttonSwitchToLogin.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }


}
