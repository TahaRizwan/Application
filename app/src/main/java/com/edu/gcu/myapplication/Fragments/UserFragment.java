package com.edu.gcu.myapplication.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.edu.gcu.myapplication.Adapters.AdapterUsers;
import com.edu.gcu.myapplication.LoginActivity;
import com.edu.gcu.myapplication.Models.ModelUsers;
import com.edu.gcu.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UserFragment extends Fragment {

    RecyclerView recyclerView;

    AdapterUsers adapterUsers;

    List<ModelUsers> usersList;

    FirebaseAuth firebaseAuth;

    public UserFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.users_recyclerView);

        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        firebaseAuth = FirebaseAuth.getInstance();

        //init user list
        usersList = new ArrayList<>();

        getAllUsers();
        return view;
    }

    private void getAllUsers() {
        //get current User
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //get path of database named "Users"
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //get all data from path
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    //get All Users except Current signed In User
                    if(!modelUsers.getUid().equals(firebaseUser.getUid())){
                        usersList.add(modelUsers);
                    }
                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(),usersList);

                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void searchUsers(String query) {
        //get current User
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //get path of database named "Users"
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        //get all data from path
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    //Condition to fulfill Search
                    //1.User not current user
                    //2.The user name or email contain text entered

                    //get All Searched Users except Current signed In User
                    if(!modelUsers.getUid().equals(firebaseUser.getUid())){

                        if(modelUsers.getName().toLowerCase().contains(query.toLowerCase())||
                                modelUsers.getEmail().toLowerCase().contains(query.toLowerCase())){
                            usersList.add(modelUsers);
                        }
                    }
                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(),usersList);

                    adapterUsers.notifyDataSetChanged();

                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void checkUserStatus(){

        FirebaseUser firebaseUser =firebaseAuth.getCurrentUser();

        if(firebaseUser != null){

        }
        else{
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        setHasOptionsMenu(true);//to show menu option in fragment
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){

        inflater.inflate(R.menu.menu_main,menu);

        //hide add post icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);


        MenuItem item = menu.findItem(R.id.action_search);

        SearchView searchView =(SearchView) MenuItemCompat.getActionView(item);

        //search listener

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!TextUtils.isEmpty(s.trim())){
                    searchUsers(s);
                }
                else {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!TextUtils.isEmpty(s.trim())){
                    searchUsers(s);
                }
                else {
                    getAllUsers();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,inflater);

    }


    @Override
    public  boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();

        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

}