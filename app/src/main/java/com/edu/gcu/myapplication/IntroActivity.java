package com.edu.gcu.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.edu.gcu.myapplication.Adapters.AdapterIntroViewPager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private ViewPager screenPager;
    AdapterIntroViewPager adapterIntroViewPager;
    TabLayout tabIndicator;
    Button btn_next;
    int position = 0;
    Button btnGetStarted;
    Animation btnAnim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        getSupportActionBar().hide();

        //init views
        btn_next = findViewById(R.id.btn_next);
        tabIndicator = findViewById(R.id.tabLayout);
        btnGetStarted = findViewById(R.id.btn_getStarted);
        btnAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_animator);

        //fill list screen
        List<screenItem> list = new ArrayList<>();
        list.add(new screenItem("Register","",R.drawable.register));
        list.add(new screenItem("Job Posting","",R.drawable.job_post));
        list.add(new screenItem("In app Chatting","",R.drawable.chat));

        //setup viewpager
        screenPager = findViewById(R.id.screen_viewPager);

        adapterIntroViewPager = new AdapterIntroViewPager(this,list);
       screenPager.setAdapter(adapterIntroViewPager);

       tabIndicator.setupWithViewPager(screenPager);

       //next button click Listener
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = screenPager.getCurrentItem();
                if(position<list.size()){
                    position++;
                    screenPager.setCurrentItem(position);
                }
                if(position==list.size()-1){
                    //When we reach on last screen
                    loadLastScreen();
                }
            }

        });

        //tablayout add change Listener

        tabIndicator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==list.size()-1){
                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //getStarted Button Click Listener
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open main activity
                Intent intent =new Intent(getApplicationContext(),DashboardActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadLastScreen() {
        btn_next.setVisibility(View.INVISIBLE);
        btnGetStarted.setVisibility(View.VISIBLE);
        tabIndicator.setVisibility(View.INVISIBLE);

        //Setup Animation
        btnGetStarted.setAnimation(btnAnim);
    }
}