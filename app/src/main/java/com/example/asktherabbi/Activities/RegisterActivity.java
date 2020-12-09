package com.example.asktherabbi.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.asktherabbi.R;
import com.example.asktherabbi.message.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

public class RegisterActivity extends AppCompatActivity {

    MaterialEditText emailEt, usernameEt, password1Et,password2Et;
    Button registerBT;

    FirebaseAuth auth=FirebaseAuth.getInstance();
    DatabaseReference reference;

    String usernameST;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEt=findViewById(R.id.email_register);
        usernameEt=findViewById(R.id.username_register);
        password1Et=findViewById(R.id.password1_register);
        password2Et=findViewById(R.id.password2_register);
        registerBT=findViewById(R.id.btn_register);

        registerBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailST=emailEt.getText().toString();
                usernameST=usernameEt.getText().toString();
                String password1ST=password1Et.getText().toString();
                String password2ST=password2Et.getText().toString();

                if(TextUtils.isEmpty(emailST)||TextUtils.isEmpty(usernameST)||TextUtils.isEmpty(password1ST)||TextUtils.isEmpty(password2ST))
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.all_fileds_are_required), Toast.LENGTH_SHORT).show();
                else
                if(password1ST.length()<6)
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.password_must_be), Toast.LENGTH_SHORT).show();
                else
                if (password1ST.equals(password2ST))
                    register(emailST,password1ST);
                else
                    Toast.makeText(RegisterActivity.this,getResources().getString(R.string.passwords_not_match) , Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void register(String email, String password){
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete( Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser firebaseUser=auth.getCurrentUser();
                    assert firebaseUser != null;
                    String userid= firebaseUser.getUid();

                    reference= FirebaseDatabase.getInstance().getReference("Users").child(userid);

                    //create new user object:
                    User newUser=new User(userid,usernameST,"default","","");

                    reference.setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this,getResources().getString(R.string.registered_success), Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                            else
                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
