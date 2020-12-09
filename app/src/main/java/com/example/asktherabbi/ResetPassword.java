package com.example.asktherabbi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.asktherabbi.Activities.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ResetPassword extends AppCompatActivity {
    EditText send_email;
    Button btn_reset;

    FirebaseAuth firebaseAuth;

    FirebaseUser firebaseUser;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_reset_password);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("Reset Password");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        send_email = findViewById(R.id.send_email);
        btn_reset = findViewById(R.id.btn_reset);

        firebaseAuth = FirebaseAuth.getInstance();

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = send_email.getText().toString();

                if (email.equals("")){
                    Toast.makeText(ResetPassword.this, getResources().getString(R.string.all_fileds_are_required), Toast.LENGTH_SHORT).show();
                } else {
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ResetPassword.this, getResources().getString(R.string.please_checkyoumail), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPassword.this, LoginActivity.class));
                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(ResetPassword.this, error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }



    //status
    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }
    @Override
    public void onResume() {
        super.onResume();
        if(firebaseUser!=null && !firebaseUser.isAnonymous())
            status("online");
    }

    @Override
    public void onPause() {
        super.onPause();
        if(firebaseUser!=null && !firebaseUser.isAnonymous())
            status("offline");
    }
}