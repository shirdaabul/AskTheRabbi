package com.example.asktherabbi.message.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asktherabbi.R;
import com.example.asktherabbi.message.Adapter.GroupChatAdapter;
import com.example.asktherabbi.message.Model.APIService;
import com.example.asktherabbi.message.Model.GroupChat;
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
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupChatActivity extends AppCompatActivity {

    private static  final int CAMERA_REQUEST_CODE=100;
    private static  final int STORAGE_REQUEST_CODE=200;
    private static  final int IMAGE_PICK_CAMERA_CODE=300;
    private static  final int IMAGE_PICK_GALLERY_CODE=400;
    private static  final int SELECT_PDF_CODE=500;
    private static   final int LOCATION_PERMISSION_REQUEST = 600;

    RecyclerView recyclerView;
    GroupChatAdapter groupChatAdapter;
    List<GroupChat> mchat;

    private Toolbar mToolBar;
    private ImageButton SendMessageButton, btn_attach;
    private EditText userMessageInput;
    private String currentGroupName;
    private TextView groupName;
   // private String currentUserID;
    private String currenUserName;
    private String currentDate;
    private String currenTime;
    String inputMsg;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef, NotificationRef, GroupFavRef;
    FirebaseUser fuser;

    //send stuff
    String[] cameraPermissions;
    String[] storagePermissions;
    String[] locationPermissions;
    Uri image_rui=null;
    DatabaseReference reference;
    private StorageTask uploadTask;
    Uri pdfUri;
    FirebaseStorage storage;
    FirebaseDatabase database;
    FusedLocationProviderClient client;
    String latitude,longitude;

    APIService apiService;

    FirebaseUser firebaseUser;
    DatabaseReference reference2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        currentGroupName = getIntent().getExtras().get("groupName").toString();

        mAuth = FirebaseAuth.getInstance();
//        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("GroupChat").child(currentGroupName);
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("GroupNotification");
        GroupFavRef = FirebaseDatabase.getInstance().getReference().child("GroupFavoritesList").child(currentGroupName);
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        InitializeFields();

        GetUserInfo();

        cameraPermissions=new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE };
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        locationPermissions=new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        storage = FirebaseStorage.getInstance();
        database= FirebaseDatabase.getInstance();

        btn_attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fuser.isAnonymous())
                {
                    Toast.makeText(GroupChatActivity.this,getResources().getString(R.string.regist_to_start_chat), Toast.LENGTH_SHORT).show();
                }
                else
                    showPopupWindow(view);
            }
        });

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputMsg = userMessageInput.getText().toString();
                if(!inputMsg.equals("")) {
                    GroupFavRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);

                                if(!user.getId().equals(fuser.getUid()))
                                    sendNotifiaction(user.getId(), currenUserName, inputMsg);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                    SaveMessageInfoToData();
                }
                if(fuser.isAnonymous())
                {
                    Toast.makeText(GroupChatActivity.this,getResources().getString(R.string.regist_to_start_chat), Toast.LENGTH_SHORT).show();
                }
                else {
                    if (TextUtils.isEmpty(userMessageInput.getText().toString())) {
                        String message = userMessageInput.getText().toString();
                        mchat.add(new GroupChat(currenUserName, currentDate, currenTime, message));

                        groupChatAdapter = new GroupChatAdapter(GroupChatActivity.this, mchat);
                        recyclerView.setAdapter(groupChatAdapter);
                    }
                }

                userMessageInput.setText("");
            }
        });

    }

    private void InitializeFields(){
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // and this
                // startActivity(new Intent(MessageActivity.this,
                // MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }

        });

        SendMessageButton = (ImageButton) findViewById(R.id.btn_send);
        btn_attach = (ImageButton) findViewById(R.id.btn_attach);
        userMessageInput = (EditText) findViewById(R.id.text_send);

        groupName = (TextView) findViewById(R.id.groupname_chat);

        groupName.setText(currentGroupName);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        mchat = new ArrayList<>();

        readMesagges();

        if(fuser!=null){
        if(fuser.isAnonymous())
        {
            userMessageInput.setEnabled(false);
            userMessageInput.setHint(getResources().getString(R.string.regist_to_start_chat));
        }}
    }

    private void GetUserInfo(){
        UsersRef.child(fuser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currenUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SaveMessageInfoToData(){
        String message = userMessageInput.getText().toString();
        String messageKey = GroupNameRef.push().getKey();

        if(TextUtils.isEmpty(message)){
            Toast.makeText(this,getResources().getString(R.string.empty_mess), Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar ccalForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(ccalForDate.getTime());

            Calendar ccalForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm");
            currenTime = currentTimeFormat.format(ccalForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(messageKey);

            HashMap<String, Object> MessageInfoMap = new HashMap<>();
            MessageInfoMap.put("name", currenUserName);
            MessageInfoMap.put("message", message);
            MessageInfoMap.put("date", currentDate);
            MessageInfoMap.put("time", currenTime);
            MessageInfoMap.put("userid", fuser.getUid());
            MessageInfoMap.put("type", "text");
            GroupMessageKeyRef.updateChildren(MessageInfoMap);
        }
    }

    private void readMesagges(){

        GroupNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    GroupChat chat = snapshot.getValue(GroupChat.class);
                        mchat.add(chat);

                    groupChatAdapter = new GroupChatAdapter(GroupChatActivity.this, mchat);
                    recyclerView.setAdapter(groupChatAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void showPopupWindow(View view) {
        PopupMenu popup = new PopupMenu(GroupChatActivity.this, view);
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
                        if (ContextCompat.checkSelfPermission(GroupChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                            pickFile();
                        }
                        else {
                            ActivityCompat.requestPermissions(GroupChatActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},SELECT_PDF_CODE
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

    private boolean checkCameraPermission(){

        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);

        return result&&result1;

    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions, CAMERA_REQUEST_CODE);

    }

    private boolean checkStoragePermission(){

        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result;

    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions, STORAGE_REQUEST_CODE);

    }

    private  boolean checkLocationPermission(){
        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==
                (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,locationPermissions,LOCATION_PERMISSION_REQUEST);
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

    private void sendImageMessage(Uri image_rui) throws IOException {
        //how ?? it should receive bitmap in case of camera pic
        //Toast.makeText(getApplicationContext(),"SendImageFun",Toast.LENGTH_LONG).show();

        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.uploading));
        progressDialog.show();

        String timeStamp=""+ System.currentTimeMillis();
        String filenameAndPath="ChatImages/"+"post_"+timeStamp;


        Bitmap bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(),image_rui);
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
                            String messageKey = GroupNameRef.push().getKey();

                            Calendar ccalForDate = Calendar.getInstance();
                            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
                            currentDate = currentDateFormat.format(ccalForDate.getTime());

                            Calendar ccalForTime = Calendar.getInstance();
                            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm");
                            currenTime = currentTimeFormat.format(ccalForTime.getTime());

                            HashMap<String, Object> groupMessageKey = new HashMap<>();
                            GroupNameRef.updateChildren(groupMessageKey);

                            GroupMessageKeyRef = GroupNameRef.child(messageKey);

                            HashMap<String, Object> MessageInfoMap = new HashMap<>();

                            MessageInfoMap.put("date", currentDate);
                            MessageInfoMap.put("name", currenUserName );
                            MessageInfoMap.put("message", dowlandUri);
                            MessageInfoMap.put("time", currenTime);
                            MessageInfoMap.put("type", "image");
                            MessageInfoMap.put("userid", fuser.getUid());

                            GroupMessageKeyRef.updateChildren(MessageInfoMap);

                            GroupFavRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        User user = snapshot.getValue(User.class);

                                        if(!user.getId().equals(fuser.getUid()))
                                            sendNotifiaction(user.getId(), currenUserName, getResources().getString(R.string.sent_photo_mess));

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.failed), Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });

    }

    private void uploadFile(Uri pdfUri) {

        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.uploading));
        progressDialog.show();

        final String filename= System.currentTimeMillis()+"";
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
                            String messageKey = GroupNameRef.push().getKey();

                            Calendar ccalForDate = Calendar.getInstance();
                            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
                            currentDate = currentDateFormat.format(ccalForDate.getTime());

                            Calendar ccalForTime = Calendar.getInstance();
                            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm");
                            currenTime = currentTimeFormat.format(ccalForTime.getTime());

                            HashMap<String, Object> groupMessageKey = new HashMap<>();
                            GroupNameRef.updateChildren(groupMessageKey);

                            GroupMessageKeyRef = GroupNameRef.child(messageKey);

                            HashMap<String, Object> MessageInfoMap = new HashMap<>();

                            MessageInfoMap.put("date", currentDate);
                            MessageInfoMap.put("name", currenUserName );
                            MessageInfoMap.put("message", url);
                            MessageInfoMap.put("time", currenTime);
                            MessageInfoMap.put("type", "file");
                            MessageInfoMap.put("userid", fuser.getUid());

                            GroupMessageKeyRef.updateChildren(MessageInfoMap);
                            GroupFavRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        User user = snapshot.getValue(User.class);

                                        if(!user.getId().equals(fuser.getUid()))
                                            sendNotifiaction(user.getId(), currenUserName, getResources().getString(R.string.sent_file_mess));

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.failed), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void sendLocation(String longitude, String latitude){
        String messageKey = GroupNameRef.push().getKey();

        Calendar ccalForDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        currentDate = currentDateFormat.format(ccalForDate.getTime());

        Calendar ccalForTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm");
        currenTime = currentTimeFormat.format(ccalForTime.getTime());

        HashMap<String, Object> groupMessageKey = new HashMap<>();
        GroupNameRef.updateChildren(groupMessageKey);

        GroupMessageKeyRef = GroupNameRef.child(messageKey);

        HashMap<String, Object> MessageInfoMap = new HashMap<>();

        MessageInfoMap.put("date", currentDate);
        MessageInfoMap.put("name", currenUserName );
        MessageInfoMap.put("message", "geo:"+latitude+","+longitude);
        MessageInfoMap.put("time", currenTime);
        MessageInfoMap.put("type", "location");
        MessageInfoMap.put("userid", fuser.getUid());

        GroupMessageKeyRef.updateChildren(MessageInfoMap);

        GroupFavRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);

                    if(!user.getId().equals(fuser.getUid()))
                        sendNotifiaction(user.getId(), currenUserName, getResources().getString(R.string.sent_location_mess));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        /*DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

// Uri.parse("geo:" + getString(R.string.latitude) + "," + getString(R.string.longitude)));
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("sender", fuser.getUid());
        hashMap.put("receiver", userid );
        hashMap.put("message","geo:"+latitude+","+longitude );
        hashMap.put("isseen", false);
        hashMap.put("type", "location");

        reference.child("Chats").push().setValue(hashMap);

        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

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
        });*/



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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]== PackageManager.PERMISSION_GRANTED;
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
                    boolean writeStorageAccepted=grantResults[0]== PackageManager.PERMISSION_GRANTED;
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
                    boolean readStorageAccepted=grantResults[0]== PackageManager.PERMISSION_GRANTED;
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
                    boolean getLoacationAccepted=grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    if(getLoacationAccepted){
                        startLocation();
                    }
                    else
                        Toast.makeText(this, getResources().getString(R.string.appropriate_permissions), Toast.LENGTH_SHORT).show();
                }
            }break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        if (requestCode == IMAGE_PICK_GALLERY_CODE && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
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


    //notifications
    private void sendNotifiaction(final String receiver, final String username, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.drawable.ic_launcher_foreground, username+": "+message, currentGroupName,
                            receiver, "yes");

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
                                            //Toast.makeText(GroupChatActivity.this, "www", Toast.LENGTH_SHORT).show();
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

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
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
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseUser!=null && !firebaseUser.isAnonymous()) {
            status("offline");
        }
    }
}

