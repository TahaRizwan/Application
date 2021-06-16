package com.edu.gcu.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.edu.gcu.myapplication.Fragments.JobFragment;
import com.edu.gcu.myapplication.Fragments.ProfileFragment;
import com.edu.gcu.myapplication.Fragments.UserFragment;
import com.edu.gcu.myapplication.databinding.ActivityDashboardBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivity extends AppCompatActivity {

    ActivityDashboardBinding binding;

    ActionBar actionBar;

    FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //Actionbar and its type

        actionBar = getSupportActionBar();
        actionBar.setTitle("Looking for Jobs");

        JobFragment jobFragment = new JobFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content,jobFragment,"");
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
                  case R.id.nav_job:
                      //home fragment transaction

                      actionBar.setTitle("Looking for Jobs");
                      JobFragment jobFragment = new JobFragment();
                      FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                      ft1.replace(R.id.content,jobFragment,"");
                      ft1.commit();
                      return true;
                  case R.id.nav_profile:
                      //home fragment transaction
                      actionBar.setTitle("Profile");
                      ProfileFragment profileFragment = new ProfileFragment();
                      FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                      ft2.replace(R.id.content,profileFragment,"");
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


              }
                    return false;
                }
            };
}