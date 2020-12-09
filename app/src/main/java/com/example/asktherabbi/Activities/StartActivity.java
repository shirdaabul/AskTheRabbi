package com.example.asktherabbi.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.asktherabbi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    Button loginBT, registerBt, guestBT;
    FirebaseUser firebaseUser;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser=mAuth.getCurrentUser();

        //check if user is connect
        if (firebaseUser != null){
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        //////** intro for first time ** ///////

        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);

        if (isFirstRun) {
            //show intro slides
            startActivity(new Intent(StartActivity.this, AppIntroActivity.class));
        }


        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).commit();

        updateUI(firebaseUser);
    }

    private void updateUI(FirebaseUser firebaseUser) {

        if (firebaseUser==null){
            mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        FirebaseUser user=mAuth.getCurrentUser();
                        updateUI(user);
                    }
                    else{
                        updateUI(null);
                    }
                }
            });
        }
        else{

        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        loginBT=findViewById(R.id.loginBTN);
        registerBt=findViewById(R.id.registerBTN);
        guestBT=findViewById(R.id.guestBTN);

        loginBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        registerBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        guestBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent guestIntent = new Intent(StartActivity.this,MainActivity.class);
                guestIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(guestIntent);
            }
        });

    }
}
