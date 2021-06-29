package com.mutafyan.dchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;


public class RegisterActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    MaterialEditText nameET, emailET, passwordET;
    Button registerButton;
    FirebaseAuth fAuth;
    FirebaseDatabase database;
    ProgressBar progressBar;
    Toolbar toolbar;
    MaterialTextView login;

    @Override
    protected void onStart() {
        super.onStart();
        /*
         * This method will be called every time activity is becoming visible to user,
         * So we should check if the user account is still logged in, and if he isn't ->
         * Take him  to Login screen
         */
        fAuth           = FirebaseAuth.getInstance();
        if(fAuth.getCurrentUser() != null)
        {
            Intent main = new Intent(getApplicationContext(), MainActivity.class);
            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            Toast.makeText(getApplicationContext(), "Welcome!", Toast.LENGTH_SHORT).show();
            startActivity(main);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameET          = findViewById(R.id.full_name);
        emailET         = findViewById(R.id.e_mail);
        passwordET      = findViewById(R.id.password);
        progressBar     = findViewById(R.id.progressBar);
        registerButton  = findViewById(R.id.register_button);
        login           = findViewById(R.id.sign_in_tw);
        fAuth           = FirebaseAuth.getInstance();
        database        = FirebaseDatabase.getInstance();
        toolbar         = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Register in D-Chat");




        // take user to login page because he already has an account
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = Objects.requireNonNull(nameET.getText()).toString();
                String email = Objects.requireNonNull(emailET.getText()).toString();
                String password = Objects.requireNonNull(passwordET.getText()).toString();
                if(all_Ok(name, email, password)){
                    Register(name, email, password);
                } else {
                    return;
                }
            }
        });
    }

    private void Register(String name, String email, String password) {
        progressBar.setVisibility(View.VISIBLE); // Indicate that the registration process started
        // Create new User with email and password
        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    FirebaseUser currentUser = fAuth.getCurrentUser();
                    assert currentUser != null;
                    String userID = currentUser.getUid();
                    // Current users new collection of data under his UID
                    DatabaseReference users = database.getReference("Users");
                    // Create hashmap with user's data
                    HashMap<String, String> userinfo = new HashMap<>();

                    userinfo.put("id", userID);
                    userinfo.put("username", name);
                    userinfo.put("email", email);
                    userinfo.put("imageURL", "default");
                    userinfo.put("status", "offline");

                    // Upload the data to the database and set a listener
                    users.child(userID).setValue(userinfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if(task.isSuccessful()){
                                // If successful - move to main activity
                                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                                main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(main);
                                // Toast welcome message
                                Toast.makeText(getApplicationContext(), "User Created. Welcome!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                // Log error message
                                Log.d(TAG, task.getException().getMessage());
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                }
                else {
                    // Toast error message
                    Toast.makeText(getApplicationContext(), "Error! " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }


    // Check if user input was valid
    public boolean all_Ok(String name, String email, String password)
    {
        if(isNameValid(name) && isEmailValid(email) && isPasswordValid(password)) {
            return true;
        } else {
            return false;
        }
    }

    // If Person's Name is ok
    private boolean isNameValid(String name)
    {
        // Characters to be banned in "Name"
        char[] invalidChars = {'@', '/', '!', '.', '>',
                '<', ',', '#', '$', '%',
                '^', ';', ':', '&', '*'};

        // flag that will become false if there is any reason later in the function
        boolean is_ok = true;

        if(TextUtils.isEmpty(name)) {
            nameET.setError("Name is Required!");
            is_ok = false;
        } else { // != null
            // Loop through all the banned characters and if name
            // contains any of them - return false
            for (char checkChar : invalidChars) {
                if (name.contains(String.valueOf(checkChar))) {
                    nameET.setError("Name cannot contain " + checkChar + "!");
                    is_ok = false;
                }
            }
        }
        return is_ok;
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
        else if(password.length() < 6)
        {
            passwordET.setError("Password must be at least 6 characters");
            is_ok = false;
        }

        return is_ok;
    }

}