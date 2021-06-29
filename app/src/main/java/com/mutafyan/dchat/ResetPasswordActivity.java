package com.mutafyan.dchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ResetPasswordActivity extends AppCompatActivity {

    Toolbar toolbar;
    MaterialEditText send_email;
    MaterialButton resetButton;
    FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        send_email  = findViewById(R.id.send_email);
        resetButton = findViewById(R.id.reset_button);
        fAuth       = FirebaseAuth.getInstance();

        // Toolbar configuration
        toolbar   = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Reset your password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEmailValid(send_email))
                {
                    String email = send_email.getText().toString();
                    fAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(ResetPasswordActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                finish();
                            } else {
                                Toast.makeText(ResetPasswordActivity.this, "Error! " + task.getException()
                                        .getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    // Checking if email is ok
    private boolean isEmailValid(EditText emailET)
    {
        String email = emailET.getText().toString();
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

}