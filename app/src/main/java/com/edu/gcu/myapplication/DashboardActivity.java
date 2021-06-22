package com.edu.gcu.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.edu.gcu.myapplication.Fragments.ChatListFragment;
import com.edu.gcu.myapplication.Fragments.JobFragment;
import com.edu.gcu.myapplication.Fragments.ProfileFragment;
import com.edu.gcu.myapplication.Fragments.UserFragment;
import com.edu.gcu.myapplication.databinding.ActivityDashboardBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class DashboardActivity extends AppCompatActivity {

    ActionBar actionBar;

    FirebaseAuth firebaseAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        //Actionbar and its type

        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        ProfileFragment profileFragment = new ProfileFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content,profileFragment,"");
        ft1.commit();

        //init
        firebaseAuth = FirebaseAuth.getInstance();

        //bottom Navigation
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);



    }
    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
              switch (item.getItemId()){
                  case R.id.nav_profile:
                      //home fragment transaction

                      actionBar.setTitle("Profile");
                      ProfileFragment profileFragment = new ProfileFragment();
                      FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                      ft1.replace(R.id.content,profileFragment,"");
                      ft1.commit();
                      return true;
                  case R.id.nav_job:
                      //home fragment transaction
                      actionBar.setTitle("Looking for Jobs");
                      JobFragment jobFragment = new JobFragment();
                      FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                      ft2.replace(R.id.content,jobFragment,"");
                      ft2.commit();
                      return true;
                  case R.id.nav_user:
                      //users fragment transaction
                      actionBar.setTitle("Users");
                      UserFragment userFragment = new UserFragment();
                      FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                      ft3.replace(R.id.content,userFragment,"");
                      ft3.commit();
                      return true;

                  case R.id.nav_chat:
                      //users fragment transaction
                      actionBar.setTitle("Chats");
                      ChatListFragment chatListFragment = new ChatListFragment();
                      FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                      ft4.replace(R.id.content,chatListFragment,"");
                      ft4.commit();
                      return true;


              }
                    return false;
                }
            };
    private void checkUserStatus(){

        FirebaseUser firebaseUser =firebaseAuth.getCurrentUser();

        if(firebaseUser != null){

        }
        else{
            startActivity(new Intent(DashboardActivity.this,LoginActivity.class));
            finish();
        }
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }
    @Override
    protected  void onStart(){
        checkUserStatus();
        super.onStart();
    }


}