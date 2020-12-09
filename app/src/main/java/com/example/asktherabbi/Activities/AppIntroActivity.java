package com.example.asktherabbi.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.asktherabbi.R;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
public class AppIntroActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_app_intro);

        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.app_name), getResources().getString(R.string.ask_ques),
                R.drawable.quuestion_clean, ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.torhaLesson), getResources().getString(R.string.you_can),
                R.drawable.pic2, ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance(getResources().getString(R.string.build_profile), getResources().getString(R.string.build_profile_join),
                R.drawable.pic3, ContextCompat.getColor(getApplicationContext(), R.color.orangeLight)));

        setSkipText(getResources().getString(R.string.skip));
        setDoneText(getResources().getString(R.string.done));

    }


    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent intent = new Intent(AppIntroActivity.this, StartActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        Intent intent = new Intent(AppIntroActivity.this, StartActivity.class);
        startActivity(intent);
        finish();
    }
}
