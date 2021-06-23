package com.edu.gcu.myapplication.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.gcu.myapplication.ChatActivity;
import com.edu.gcu.myapplication.Models.ModelUsers;
import com.edu.gcu.myapplication.R;
import com.edu.gcu.myapplication.ThereProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    //for getting current user
    FirebaseAuth firebaseAuth;
    String myUid;

    Context context;
    List<ModelUsers> usersList;

    //constructor
    public AdapterUsers(Context context,List<ModelUsers> usersList){
        this.context = context;
        this.usersList = usersList;
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();
    }
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout(row_user.xml)

        View view = LayoutInflater.from(context).inflate(R.layout.row_users, viewGroup,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {

        final String hisUID = usersList.get(i).getUid();
        String userImage = usersList.get(i).getImage();
        String userName = usersList.get(i).getName();
        final String userEmail = usersList.get(i).getEmail();

        //set Data
        myHolder.nameTv.setText(userName);
        myHolder.emailTv.setText(userEmail);
        try{
            Picasso.get().load(userImage).placeholder(R.drawable.ic_person_img).into(myHolder.avatarIv);
        }
        catch (Exception e){

        }
        myHolder.blockIv.setImageResource(R.drawable.ic_unblock_green);
        //check if user is blocked or not
        checkIsBlocked(hisUID,myHolder,i);

        //handle item Click
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            //Profile Clicked

                            Intent intent = new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uid",hisUID);
                            context.startActivity(intent);
                        }
                        if(which==1){
                            //Chat Clicked

                            //Click User from user list to start chatting/messaging
                          imBlockedOrNot(hisUID);

                        }
                    }
                });
                builder.create().show();
            }
        });
        //click to block/unblock user
        myHolder.blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(usersList.get(i).isBlocked()){
                    unBlockUser(hisUID);
                }
                else {
                    blockUser(hisUID);

                }
            }
        });
    }
    private void imBlockedOrNot(String hisUID){
        //First check if sender is in blockList or not
        //if not blocked then simply start chat activity
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                       for (DataSnapshot ds:snapshot.getChildren()){
                      if(ds.exists()){
                           Toast.makeText(context,"You're blocked by that user,can't send message",Toast.LENGTH_SHORT).show();
                           //blocked,don't proceed further
                           return; }
                       }

                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("hisUid",hisUID);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void checkIsBlocked(String hisUID, MyHolder myHolder, int i) {
        //check each user,if blocked or not
        //if uid of the user exists in "BlockedUsers" than that user is blocked,otherwise not

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if(ds.exists()){
                                myHolder.blockIv.setImageResource(R.drawable.ic_block_red);
                                usersList.get(i).setBlocked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser(String hisUID) {
        //block the user,by adding uid to current user's BlockUsers node

        //put values in hashMap to put i db
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("uid",hisUID);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUID).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //blocked Successfully
                        Toast.makeText(context,"Blocked Successfully",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed to block
                Toast.makeText(context,"Failed due to "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unBlockUser(String hisUID){
        //unblock the user,by removing uid to current user's BlockUsers node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            if(ds.exists()){
                                ds.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //unblock successfully
                                                Toast.makeText(context,"Unblocked Successfully",Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed to unblock
                                        Toast.makeText(context,"Failed due to "+e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView avatarIv,blockIv;
        TextView nameTv,emailTv;


        public MyHolder(@NonNull View itemView){
            super(itemView);

            blockIv = itemView.findViewById(R.id.blockIv);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
        }
    }
}
