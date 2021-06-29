package com.mutafyan.dchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static android.content.ContentValues.TAG;

public class LoginActivity extends AppCompatActivity {

    Toolbar toolbar;
    MaterialEditText emailET, passwordET;
    Button loginButton;
    FirebaseAuth fAuth;
    MaterialTextView backToReg, resetPassword;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        fAuth           = FirebaseAuth.getInstance();
        emailET         = findViewById(R.id.email);
        passwordET      = findViewById(R.id.password);
        progressBar     = findViewById(R.id.progressBar);
        loginButton     = findViewById(R.id.login);
        backToReg       = findViewById(R.id.createTW);
        resetPassword   = findViewById(R.id.forgot_password);
        toolbar         = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Login to D-Chat");

        // Create new account - go back to Registration activity
        backToReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            }
        });

        // Start ResePasswordActivity to reset the password
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ResetPasswordActivity.class));
            }
        });

        // Login Button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Objects.requireNonNull(emailET.getText()).toString();
                String password = Objects.requireNonNull(passwordET.getText()).toString();
                progressBar.setVisibility(View.VISIBLE);
                if(data_ok(email, password)){ // data was ok -- proceed with signing in
                    fAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Logged in successfully", Toast.LENGTH_SHORT).show();
                            // Display debug info
                            Log.d(TAG, "LoginSuccess: Logged in user " + Objects.requireNonNull(fAuth.getCurrentUser()).getUid());
                            Intent main = new Intent(getApplicationContext(), MainActivity.class);
                            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(main);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "Login failed! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d(TAG,"LoginFailure: " + e.toString());
                        }
                    });

                } else { // data was not ok
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }
            }
        });
    }

    private boolean data_ok(String email, String password)
    {
        if(isEmailValid(email) && isPasswordValid(password)) {
            return true;
        } else {
            return false;
        }
    }

    // Checking if email is ok
    private boolean isEmailValid(String email)
    {
        boolean is_ok = true;
        if(TextUtils.isEmpty(email))
        {
            emailET.setError("Email is Required!");
            is_ok = false;
        }

        // if the email doesn't contain '@' and '.' and if it is not a blank space only
        else if(!TextUtils.isEmpty(email.trim()) && !email.contains("@") && !email.contains("."))
        {
            emailET.setError("Enter a valid Email");
            is_ok = false;
        }

        return is_ok;
    }

    // Check password
    private boolean isPasswordValid(String password)
    {
        boolean is_ok = true;
        if(TextUtils.isEmpty(password))
        {
            passwordET.setError("Password is Required!");
            is_ok = false;
        }
        else if(password.length() < 5)
        {
            passwordET.setError("Password must be at least 5 characters");
            is_ok = false;
        }

        return is_ok;
    }
}