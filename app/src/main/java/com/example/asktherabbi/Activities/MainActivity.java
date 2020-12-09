package com.example.asktherabbi.Activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.asktherabbi.Fragments.GroupsFragment;
import com.example.asktherabbi.Fragments.TorahLessonFragment;
import com.example.asktherabbi.Fragments.UsersFragment;
import com.example.asktherabbi.R;
import com.example.asktherabbi.Adapters.ViewPagerAdapter;
import com.example.asktherabbi.message.Fragments.Fragment_Chat;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;
    DatabaseReference reference;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

//        actionBar=getSupportActionBar();
//        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0D5D98")));


        tabLayout=(TabLayout)findViewById(R.id.tablayout_id);
        viewPager=(ViewPager)findViewById(R.id.viewPager_id);
        adapter= new ViewPagerAdapter((getSupportFragmentManager()));

        //Add fragment

        adapter.AddFragment(new TorahLessonFragment(),getResources().getString(R.string.torhaLesson) );
        adapter.AddFragment(new GroupsFragment(),getResources().getString(R.string.groups) );
        adapter.AddFragment(new UsersFragment(),getResources().getString(R.string.users) );
        adapter.AddFragment(new Fragment_Chat(),getResources().getString(R.string.chat) );


        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_school); //Lessons
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_users); //groups
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_profile_black_24dp); //users
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_chat);  //chat
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //status
    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
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
