package com.edu.gcu.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.edu.gcu.myapplication.Adapters.AdapterUsers;
import com.edu.gcu.myapplication.Models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostInterestedByActivity extends AppCompatActivity {

    String postId;
    private List<ModelUsers> usersList;
    private AdapterUsers adapterUsers;
    private RecyclerView recyclerView;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_interested_by);
        //actionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Liked by");
        //add back button
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail());

        recyclerView = findViewById(R.id.recyclerView);

        //get the post id
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        usersList = new ArrayList<>();

        //get the list of UIDs of users who are interestedIn
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Interested");
        ref.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    String hisUID =""+ds.getRef().getKey();
                    //get user info from each id
                    getUsers(hisUID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void getUsers(String hisUID){
        //get information of each user using uid
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                            usersList.add(modelUsers);
                        }
                        //set up Adapter
                        adapterUsers = new AdapterUsers(PostInterestedByActivity.this,usersList);
                        //set Adapter to recyclerView
                        recyclerView.setAdapter(adapterUsers);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}