package com.example.asktherabbi.message.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.example.asktherabbi.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String latitude;
    private String longitude;
    double lat;
    double lon;

    FirebaseUser firebaseUser;
    DatabaseReference reference2;

    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        intent=getIntent();
        String message=intent.getStringExtra("message");
        int indexFirst=message.indexOf(":");
        int indexSecond=message.indexOf(",");
        int indexLast=message.length();
        latitude=message.substring(indexFirst+1,indexSecond);
        longitude=message.substring(indexSecond+1,indexLast);

        lat=Double.parseDouble(latitude);
        lon=Double.parseDouble(longitude);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng location = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(location));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
//        mMap.getMaxZoomLevel();
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