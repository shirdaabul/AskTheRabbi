package com.example.asktherabbi.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.asktherabbi.R;
import com.example.asktherabbi.ResetPassword;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

public class LoginActivity extends AppCompatActivity {

    MaterialEditText email, password;
    Button btn_login;
    TextView forgot_password;

    FirebaseAuth auth=FirebaseAuth.getInstance();;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email=findViewById(R.id.email_login);
        password=findViewById(R.id.password_login);

        forgot_password = findViewById(R.id.forgot_password);

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ResetPassword.class));
            }
        });


        btn_login=findViewById(R.id.btn_login);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailST = email.getText().toString();
                String passwordST = password.getText().toString();

                if (TextUtils.isEmpty(emailST) || TextUtils.isEmpty(passwordST))
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.all_fileds_are_required), Toast.LENGTH_SHORT).show();
                else
                {
                    auth.signInWithEmailAndPassword(emailST, passwordST)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });


    }
}
