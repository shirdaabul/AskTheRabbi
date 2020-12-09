package com.example.asktherabbi.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.asktherabbi.R;
import com.example.asktherabbi.message.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView userPhoto;
    EditText username;
    TextView useremail;
    Button saveBT, logoutBT;
    ImageButton exitBT;
    int GALLERY_REQUEST=1;
    int CAMERA_REQUEST=2;

    Uri imageUri;
    String mUri="default";
    String email;

    StorageTask uploadTask;

    FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
    //String userid= firebaseUser.getUid();

    DatabaseReference databaseReference,reference2;

    Bitmap photo;

    User user;

    Button loginBT, registerBt, guestBT;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            if (!firebaseUser.isAnonymous()) {
                setContentView(R.layout.activity_profile);

                userPhoto = findViewById(R.id.user_profile_im);
                username = findViewById(R.id.username_profile);
                useremail = findViewById(R.id.email_profile);
                saveBT = findViewById(R.id.save_profile_bt);
                logoutBT = findViewById(R.id.logout_profile_bt);
                exitBT = findViewById(R.id.exit_profile_bt);

                exitBT.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });

                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                email = firebaseUser.getEmail();

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        user = dataSnapshot.getValue(User.class);

                        username.setText(user.getName());
                        useremail.setText(email);

                        if (user.getImageUrl().equals("default")) {
                            userPhoto.setImageResource(R.drawable.ic_person_profile_24dp);
                        } else
                            Glide.with(getApplicationContext()).load(user.getImageUrl()).into(userPhoto);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                userPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        photoDialog();
                    }
                });

                saveBT.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        save_profile();
                    }
                });

                logoutBT.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(ProfileActivity.this, StartActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            }
        else
        {
            setContentView(R.layout.activity_start);

            loginBT=findViewById(R.id.loginBTN);
            registerBt=findViewById(R.id.registerBTN);
            guestBT=findViewById(R.id.guestBTN);

//            guestBT.setVisibility(View.INVISIBLE);
            guestBT.setText(getResources().getString(R.string.back));
            guestBT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            loginBT.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent loginIntent = new Intent(ProfileActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                }
            });

            registerBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent registerIntent = new Intent(ProfileActivity.this, RegisterActivity.class);
                    startActivity(registerIntent);
                }
            });
        }

    }

    private void save_profile() {
        String newUsername=username.getText().toString();
        if(!newUsername.isEmpty()) {
            databaseReference.child("name").setValue(newUsername)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ProfileActivity.this, getResources().getString(R.string.change_details_sucsses), Toast.LENGTH_SHORT).show();
                            username.setText(user.getName());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, getResources().getString(R.string.failed), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }


    private void photoDialog(){
        AlertDialog.Builder photoDialog = new AlertDialog.Builder(this);
        photoDialog.setTitle(getResources().getString(R.string.select_choice));
        String[] photoDialogItems = {
                getResources().getString(R.string.from_gallery),
                getResources().getString(R.string.from_camera)};
        photoDialog.setItems(photoDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                galleryImage();
                                break;
                            case 1:
                                if(Build.VERSION.SDK_INT>=23)
                                {
                                    int hasCameraPermisson=checkSelfPermission(Manifest.permission.CAMERA);
                                    int hasWriteExternalPermisson=checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                    if(hasCameraPermisson==PackageManager.PERMISSION_GRANTED && hasWriteExternalPermisson==PackageManager.PERMISSION_GRANTED)
                                        cameraImage();
                                    else
                                        requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE },CAMERA_REQUEST);
                                }
                                else
                                    cameraImage();
                                break;
                        }
                    }
                });
        photoDialog.show();
    }

    //image from gallery
    private void galleryImage() {
        Intent galleryIntent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    //image from camera
    private void cameraImage () {
        ContentValues cv=new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Desr");
        imageUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);

    }


    public String getFileExtention(Uri uri){
        ContentResolver contentResolver=this.getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    public void uploadImageGallery(){
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.uploading_photo));
        progressDialog.show();

        if(imageUri!=null){
            userPhoto.setImageURI(imageUri);

            final StorageReference fileReference= FirebaseStorage.getInstance().getReference().child("users_gallery_images").child(System.currentTimeMillis()+"."+getFileExtention(imageUri));

            uploadTask=fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    else
                        Toast.makeText(ProfileActivity.this, getResources().getString(R.string.image_sucsses), Toast.LENGTH_SHORT).show();

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        mUri=downloadUri.toString();

                        DatabaseReference databaseReferenceImage= FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("imageUrl");
                        databaseReferenceImage.setValue(mUri);

                        progressDialog.dismiss();
                    }
                    else {
                        Toast.makeText(ProfileActivity.this,  getResources().getString(R.string.failed), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }
        else
            Toast.makeText(ProfileActivity.this, getResources().getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show();
    }


    public void uploadImageCamera(){

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.uploading_photo));
        progressDialog.show();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        byte[] b = stream.toByteArray();

        StorageReference fileReference= FirebaseStorage.getInstance().getReference().child("users_camera_images").child(System.currentTimeMillis()+".");

        fileReference.putBytes(b).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String dowlandUri=uriTask.getResult().toString();

                if (uriTask.isSuccessful()){
                    DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("imageUrl");
                    databaseReference.setValue(dowlandUri);
                    Toast.makeText(ProfileActivity.this, getResources().getString(R.string.image_sucsses), Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this,getResources().getString(R.string.failed),Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //for gallery
        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK &&data!=null && data.getData()!=null) {
            imageUri=data.getData();

            if(uploadTask!=null && uploadTask.isInProgress()){
                Toast.makeText(this, getResources().getString(R.string.upload_in_progress), Toast.LENGTH_SHORT).show();
            }
            else
                uploadImageGallery();
        }

        //for camera
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            try {
                photo=MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
                uploadImageCamera();
            } catch (IOException e) {
                e.printStackTrace();
            }

//            photo = (Bitmap) data.getExtras().get("data");
        }
    }
    //status
    private void status(String status) {
        reference2 = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference2.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(firebaseUser!=null && !firebaseUser.isAnonymous())
            status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseUser!=null && !firebaseUser.isAnonymous())
            status("offline");
    }

}
