package com.example.dtlattendance;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    //Splash Screen Handler
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            relativeLayoutSplash.setVisibility(View.GONE);
            relativeLayoutLogin.setVisibility(View.VISIBLE);
        }
    };

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

        //Splash Screen
        handler.postDelayed(runnable,2500);

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
                finish();
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        //Check if user is signed in (non-null) and update UI accordingly.
        if (mAuth.getCurrentUser() != null) {

            //Sends the user to userhome
            SharedPreferences pref = getSharedPreferences("User",MODE_PRIVATE);
            //String storedUsername =     pref.getString("username",null);
            //String storedemail =     pref.getString("email",null);
            String storedAdmin =  pref.getString("admin",null);
            String storedUID = pref.getString("uid","0");

            if(storedAdmin != null)
                if(storedAdmin.equals("0")){
                    Intent userIntent = new Intent(LoginActivity.this,UserHomeActivity.class);
                    startActivity(userIntent);
                    finish();
                }
                else if(storedAdmin.equals("1")){
                    Intent adminIntent = new Intent(LoginActivity.this,AdminHomeActivity.class);
                    startActivity(adminIntent);
                    finish();
                }
        }
    }

    //Main Login Function
    public void login(final String email, final String pass){

        progressBarVisible();   //Progress Bar Visible

        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    //Login is already successful, now we check whether the operator is USER/ADMIN

                    //Redirecting the admin to the admin or the user part
                    //Retrieving the custom files from the FireBase Database
                    FirebaseDatabase.getInstance().getReference().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //Data Snapshot takes the snapshot of the whole database & we iterate and get the one which we want
                            for(DataSnapshot ds: dataSnapshot.getChildren()) {

                                //Shared Preference
                                SharedPreferences pref = getSharedPreferences("User", MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                if (mAuth.getUid() != null) {

                                    try {
                                        editor.putString("email", ds.child(mAuth.getUid()).getValue(User.class).getEmail());
                                        editor.putString("username", ds.child(mAuth.getUid()).getValue(User.class).getUsername());
                                        String admin = ds.child(mAuth.getUid()).getValue(User.class).getAdmin();
                                        editor.putString("admin", admin);
                                        editor.putString("total", ds.child(mAuth.getUid()).getValue(User.class).getTotal());
                                        editor.putString("uid",mAuth.getUid());
                                        editor.apply();

                                        if (admin != null)
                                            if (admin.equals("0")) {
                                                Intent userIntent = new Intent(LoginActivity.this, UserHomeActivity.class);
                                                startActivity(userIntent);
                                                finish();
                                            } else if (admin.equals("1")) {
                                                Intent adminIntent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                                                startActivity(adminIntent);
                                                finish();
                                            }
                                    }catch (Exception e){
                                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            //Toast if Login is unsuccessful
                            Toast.makeText(LoginActivity.this,databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
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
