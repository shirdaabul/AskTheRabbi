package com.example.asktherabbi.message.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.asktherabbi.R;
import com.example.asktherabbi.message.Adapter.MessageAdapter;
import com.example.asktherabbi.message.Model.APIService;
import com.example.asktherabbi.message.Model.Chat;
import com.example.asktherabbi.message.Model.User;
import com.example.asktherabbi.message.Notifications.Client;
import com.example.asktherabbi.message.Notifications.Data;
import com.example.asktherabbi.message.Notifications.MyResponse;
import com.example.asktherabbi.message.Notifications.Sender;
import com.example.asktherabbi.message.Notifications.Token;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity  {

    CircleImageView profile_image;
    TextView username;
    String userid;
    ImageView imageIv;
    FirebaseUser fuser;
    DatabaseReference reference;
    FirebaseDatabase database;
    FirebaseAuth mAuth;

    Intent intent;
    Bitmap photo;


    MessageAdapter messageAdapter;
    List<Chat> mchat;
    ValueEventListener seenListener;
    ImageButton btn_send;
    EditText text_send;
    RecyclerView recyclerView;

    //notify
    boolean notify = false;
    APIService apiService;

    //send image to chat part
    ImageButton btn_attach;
    private StorageTask uploadTask;
    Uri pdfUri;

    private static  final int CAMERA_REQUEST_CODE=100;
    private static  final int STORAGE_REQUEST_CODE=200;
    private static  final int IMAGE_PICK_CAMERA_CODE=300;
    private static  final int IMAGE_PICK_GALLERY_CODE=400;
    private static  final int SELECT_PDF_CODE=500;
    private static   final int LOCATION_PERMISSION_REQUEST = 600;
    Uri image_rui=null;

    String[] cameraPermissions;
    String[] storagePermissions;
    String[] locationPermissions;
    //new one
    private File filePathImageCamera;
    FirebaseStorage storage;

    String latitude,longitude;
    FusedLocationProviderClient client;

    String currentName;

    FirebaseUser firebaseUser;
    DatabaseReference reference2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_chat);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // and this
                // startActivity(new Intent(MessageActivity.this,
                // MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }

        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //
        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE };
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        locationPermissions=new String[]{Manifest.permission.ACCESS_FINE_LOCATION};


        //pointers
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        imageIv=findViewById(R.id.messageIv);

        //part of send files to chats
        btn_attach = findViewById(R.id.btn_attach);
        storage = FirebaseStorage.getInstance();

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        currentName = intent.getStringExtra("username");


        mAuth = FirebaseAuth.getInstance();
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        database=FirebaseDatabase.getInstance();



        btn_attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;

                String msg = text_send.getText().toString();

                if (!msg.equals("")) {
                    sendMessage(fuser.getUid(), userid, msg);
                    //
                } else {
                    Toast.makeText(MessageActivity.this, getResources().getString(R.string.empty_mess), Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");


            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getName());
                if (user.getImageUrl().equals("default")) {
                    profile_image.setImageResource(R.drawable.ic_person_profile_24dp);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageUrl()).into(profile_image);
                }

                readMesagges(fuser.getUid(), userid, user.getImageUrl());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userid);
    }





    void showPopupWindow(View view) {
        PopupMenu popup = new PopupMenu(MessageActivity.this, view);
        try {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        popup.getMenuInflater().inflate(R.menu.attach_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.item_one:
                        if (!checkCameraPermission()){
                            requestCameraPermission();
                        }
                        else{
                            pickFromCamera();
                        }
                        return true;
                    case R.id.item_two:
                        if (!checkStoragePermission()){
                            requestStoragePermission();
                        }
                        else {
                            pickFromGallery();
                        }
                        return true;
                    case R.id.item_three:
                        if (ContextCompat.checkSelfPermission(MessageActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                            pickFile();
                        }
                        else {
                            ActivityCompat.requestPermissions(MessageActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},SELECT_PDF_CODE
                            );
                        }
                        return true;
                    case R.id.item_four:
                        if(!checkLocationPermission()){
                            requestLocationPermission();
                        }
                        else {
                            startLocation();
                        }
                        return true;
                }
                return true;
            }
        });
        popup.show();
    }


    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "camera",
                "gallery",
                "pdf"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                if (!checkCameraPermission()){
                                    requestCameraPermission();
                                }
                                else{
                                    pickFromCamera();
                                }
                                break;
                            case 1:
                                if (!checkStoragePermission()){
                                    requestStoragePermission();
                                }
                                else {
                                    pickFromGallery();
                                }
                            case 2:{
                                if (ContextCompat.checkSelfPermission(MessageActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                                    pickFile();
                                }
                                else {
                                    ActivityCompat.requestPermissions(MessageActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},SELECT_PDF_CODE
                                    );
                                }
                            }
                            break;
                        }
                    }
                });
        pictureDialog.show();
    }

    private void pickFile() {
        Intent intent=new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,SELECT_PDF_CODE );

    }

    private void pickFromCamera() {
        ContentValues cv=new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Desr");
        image_rui=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_rui);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
        /*
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);}*/

    }


    private void pickFromGallery() {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        if (requestCode == IMAGE_PICK_GALLERY_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            image_rui = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()) {
                //Toast.makeText(getApplicationContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    // imageIv.setImageURI(image_rui);
                    sendImageMessage(image_rui);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //works!!!
        else if (requestCode == IMAGE_PICK_CAMERA_CODE &&resultCode == RESULT_OK) {

            try {
                sendImageMessage(image_rui);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //works!!!
        else if (requestCode==SELECT_PDF_CODE) {
            pdfUri=data.getData();

            if (pdfUri!=null) {
                uploadFile(pdfUri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }



    private void uploadFile(Uri pdfUri) {

        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.uploading));
        progressDialog.show();

        final String filename=System.currentTimeMillis()+"";
        StorageReference storageReference=storage.getReference();
        storageReference.child("Files").child(filename).putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();

                        //i think problem here
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String url=uriTask.getResult().toString();
                        DatabaseReference reference=database.getReference();
                        reference.child(filename).setValue(url);

                        if (uriTask.isSuccessful()){
                            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();

                            HashMap<String, Object> hashMap = new HashMap<>();

                            hashMap.put("sender", fuser.getUid());
                            hashMap.put("receiver", userid );
                            hashMap.put("message", url);
                            hashMap.put("isseen", false);
                            hashMap.put("type", "file");

                            databaseReference.child("Chats").push().setValue(hashMap);

                            sendNotifiaction(userid, currentName, getResources().getString(R.string.sent_file_mess));

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.failed),Toast.LENGTH_LONG).show();
            }
        });

        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted&&storageAccepted){
                        pickFromCamera();
                    }
                    else{
                        Toast.makeText(this, getResources().getString(R.string.appropriate_permissions), Toast.LENGTH_LONG);
                    }
                }
                else {

                }

            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean writeStorageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted)
                    {
                        pickFromGallery();
                    }
                    else
                        Toast.makeText(this, getResources().getString(R.string.appropriate_permissions), Toast.LENGTH_LONG);

                }
            }break;
            case SELECT_PDF_CODE:{
                if (grantResults.length>0){
                    boolean readStorageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(readStorageAccepted)
                    {
                        pickFile();
                    }
                    else
                        Toast.makeText(this, getResources().getString(R.string.appropriate_permissions), Toast.LENGTH_LONG);

                }
            }break;
            case LOCATION_PERMISSION_REQUEST:{
                if(grantResults.length>0){
                    boolean getLoacationAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(getLoacationAccepted){
                        startLocation();
                    }
                    else
                        Toast.makeText(this, getResources().getString(R.string.appropriate_permissions), Toast.LENGTH_SHORT).show();
                }
            }break;
        }

    }


    //storage
    private boolean checkStoragePermission(){

        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result;

    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions, STORAGE_REQUEST_CODE);

    }

    //camera
    private boolean checkCameraPermission(){

        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);

        return result&&result1;

    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions, CAMERA_REQUEST_CODE);

    }

    private  boolean checkLocationPermission(){
        boolean result=ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,locationPermissions,LOCATION_PERMISSION_REQUEST);
    }


    //main functions!!!
    private void sendImageMessage(Uri image_rui) throws IOException {
        //how ?? it should receive bitmap in case of camera pic
        //Toast.makeText(getApplicationContext(),"SendImageFun",Toast.LENGTH_LONG).show();

        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.uploading_photo));
        progressDialog.show();

        String timeStamp=""+System.currentTimeMillis();
        String filenameAndPath="ChatImages/"+"post_"+timeStamp;


        Bitmap bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),image_rui);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data=baos.toByteArray();
        StorageReference ref= FirebaseStorage.getInstance().getReference().child(filenameAndPath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        progressDialog.dismiss();
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String dowlandUri=uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){
                            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();

                            HashMap<String, Object> hashMap = new HashMap<>();

                            hashMap.put("sender", fuser.getUid());
                            hashMap.put("receiver", userid );
                            hashMap.put("message", dowlandUri);
                            hashMap.put("isseen", false);
                            hashMap.put("type", "image");

                            databaseReference.child("Chats").push().setValue(hashMap);

                            sendNotifiaction(userid, currentName, getResources().getString(R.string.sent_photo_mess));

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.failed),Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });

        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //function to send text messages only
    private void sendMessage(String sender, final String receiver, String message) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        hashMap.put("type", "text");

        reference.child("Chats").push().setValue(hashMap);

        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotifiaction(receiver, user.getName(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void readMesagges(final String myid, final String userid, final String imageurl) {
        mchat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {
                        mchat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void startLocation() {

        client = LocationServices.getFusedLocationProviderClient(this);

        LocationCallback callback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location lastLocation = locationResult.getLastLocation();
                Log.d("TAG",lastLocation.getLatitude() + " , " + lastLocation.getLongitude());
                // text_send.setText(lastLocation.getLatitude() + " , " + lastLocation.getLongitude());
                latitude=lastLocation.getLatitude()+"";
                longitude=lastLocation.getLongitude()+"";

                sendLocation(longitude,latitude);
                // locationTv.setText(lastLocation.getLatitude() + " , " + lastLocation.getLongitude());
            }
        };

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(1000);
        request.setFastestInterval(500);
        request.setNumUpdates(1);

        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            client.requestLocationUpdates(request,callback,null);
        else if(Build.VERSION.SDK_INT<=22)
            client.requestLocationUpdates(request,callback,null);
    }

    private void sendLocation( String longitude,String latitude){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

// Uri.parse("geo:" + getString(R.string.latitude) + "," + getString(R.string.longitude)));
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("sender", fuser.getUid());
        hashMap.put("receiver", userid );
        hashMap.put("message","geo:"+latitude+","+longitude );
        hashMap.put("isseen", false);
        hashMap.put("type", "location");

        reference.child("Chats").push().setValue(hashMap);

        sendNotifiaction(userid, currentName, getResources().getString(R.string.sent_location_mess));

        // add user to chat fragment
        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    //notifications
    private void sendNotifiaction(String receiver, final String username, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.drawable.question_sign, username+": "+message, getResources().getString(R.string.new_message),
                            userid, "no");

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
                                            //Toast.makeText(MessageActivity.this, getResources().getString(R.string.oops), Toast.LENGTH_SHORT).show();
                                            System.out.println("try");
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        if(firebaseUser!=null && !firebaseUser.isAnonymous()) {
            status("online");
            //currentUser(userid);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseUser!=null && !firebaseUser.isAnonymous()) {
            reference2.removeEventListener(seenListener);
            status("offline");

            //currentUser(userid);
        }
    }

}
