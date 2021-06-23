package com.edu.gcu.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.drm.DrmUtils;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.edu.gcu.myapplication.Adapters.AdapterChat;
import com.edu.gcu.myapplication.Adapters.AdapterUsers;
import com.edu.gcu.myapplication.Models.ModelChat;
import com.edu.gcu.myapplication.Models.ModelUsers;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    ValueEventListener seenListener;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    List<ModelChat> chatList;
    AdapterChat adapterChat;
    DatabaseReference userReForSeen;

    //views
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv,blockIv;
    TextView nameTv,userStatusTv;
    EditText messageEt;
    ImageButton sendBtn,attachBtn;




    String hisUid;

    String myUid;

    String hisImage;

    boolean isBlocked= false;


    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //image pick constant
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    //permission Array
    String[] cameraPermissions;
    String[] storagePermissions;


    //image picked will be saved in the uri
    Uri image_uri = null;
    private boolean notify;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        recyclerView =  findViewById(R.id.chat_recycler);
        profileIv = findViewById(R.id.profileIv);
        blockIv = findViewById(R.id.blockIv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        attachBtn = findViewById(R.id.attachBtn);


        //init permission Arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        firebaseAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        hisUid =intent.getStringExtra("hisUid");

        firebaseDatabase = FirebaseDatabase.getInstance();

        databaseReference = firebaseDatabase.getReference("Users");

        Query userQuery = databaseReference.orderByChild("uid").equalTo(hisUid);

        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    hisImage = ""+ds.child("image").getValue();
                    String typingStatus = ""+ds.child("typingTo").getValue();

                    if(typingStatus.equals(myUid)){
                        userStatusTv.setText("typing...");
                    }
                    else{

                    String onlineStatus = ""+ds.child("onlineStatus").getValue();
                    if(onlineStatus.equals("online")){
                        userStatusTv.setText(onlineStatus);
                    }
                    else{
                        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                        cal.setTimeInMillis(Long.parseLong(onlineStatus));
                        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
                        userStatusTv.setText("Last seen at "+dateTime); }
                    }

                    nameTv.setText(name);
                    try{
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_person_white).into(profileIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_person_white).into(profileIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEt.getText().toString().trim();

                if(TextUtils.isEmpty(message)){
                    Toast.makeText(ChatActivity.this,"Cannot send this empty message",Toast.LENGTH_SHORT).show();
                }
                else {
                    sendMessage(message);
                }
            }
        });
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialog
                showImagePickDialog();
            }
        });
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0){
                    checkTypingStatus("noOne");
                }
                else{
                    checkTypingStatus(hisUid);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        blockIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBlocked){
                    unBlockUser();
                }
                else {
                    blockUser();

                }
            }
        });

        readMessage();

        checkIsBlocked();

        seenMessage();


    }

    private void checkIsBlocked() {
        //check each user,if blocked or not
        //if uid of the user exists in "BlockedUsers" than that user is blocked,otherwise not

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            if(ds.exists()){
                                blockIv.setImageResource(R.drawable.ic_block_red);
                                isBlocked = true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void blockUser() {
        //block the user,by adding uid to current user's BlockUsers node

        //put values in hashMap to put i db
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("uid",hisUid);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //blocked Successfully
                        Toast.makeText(ChatActivity.this,"Blocked Successfully",Toast.LENGTH_SHORT).show();
                        blockIv.setImageResource(R.drawable.ic_block_red);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed to block
                Toast.makeText(ChatActivity.this,"Failed due to "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unBlockUser(){
        //unblock the user,by removing uid to current user's BlockUsers node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
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
                                                Toast.makeText(ChatActivity.this,"Unblocked Successfully",Toast.LENGTH_SHORT).show();

                                                blockIv.setImageResource(R.drawable.ic_unblock_green);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed to unblock
                                        Toast.makeText(ChatActivity.this,"Failed due to "+e.getMessage(),Toast.LENGTH_SHORT).show();
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

    private void showImagePickDialog() {
        //options(camera,gallery) to show in dialog
        String[] options = {"Camera","Gallery"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image from");
        //set options to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    //camera clicked
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                if(which==1){
                    //gallery clicked
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else{
                        pickFromGallery();
                    }

                }
            }
        });
        builder.create().show();
    }

    private void pickFromCamera() {

        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }


    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private  void  requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }


    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result&&result1;
    }

    private  void  requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_CODE);
    }


    private void seenMessage() {
        userReForSeen = FirebaseDatabase.getInstance().getReference("Chats");

        seenListener = userReForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUid)&&chat.getSender().equals(hisUid)){
                        HashMap<String,Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen",true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage() {
        chatList = new ArrayList<>();
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chats");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUid)&&chat.getSender().equals(hisUid)||
                    chat.getReceiver().equals(hisUid)&&chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }
                    adapterChat = new AdapterChat(ChatActivity.this,chatList,hisImage);
                    adapterChat.notifyDataSetChanged();

                    recyclerView.setAdapter(adapterChat);

                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String,Object> hashMap = new HashMap<>();

        hashMap.put("sender",myUid);

        hashMap.put("receiver",hisUid);

        hashMap.put("message",message);

        hashMap.put("timeStamp",timeStamp);

        hashMap.put("isSeen",false);
        hashMap.put("type","text");

        databaseReference.child("Chats").push().setValue(hashMap);
        
        messageEt.setText("");

        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlists")
                .child(myUid).child(hisUid);

        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlists")
                .child(hisUid).child(myUid);

        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void sendImageMessage(Uri image_uri) throws IOException {
        notify=true;

        //progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending image....");
        progressDialog.show();

        String timeStamp = ""+System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/"+"post_"+System.currentTimeMillis();

        //Chats node will be created that will ccontain all images sent via chat

        //get bitmap from image url
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),image_uri);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data = baos.toByteArray();//convert image to bytes
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image uploaded
                progressDialog.dismiss();
                //get url of uploaded img
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String downloadUri = uriTask.getResult().toString();

                if(uriTask.isSuccessful()){
                    //add image uri and other info to database
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                    //setup required data
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("sender",myUid);
                    hashMap.put("receiver",hisUid);
                    hashMap.put("message",downloadUri);
                    hashMap.put("timeStamp",timeStamp);
                    hashMap.put("type","image");
                    hashMap.put("isSeen","false");

                    //put this data to firebase
                    databaseReference.child("Chats").push().setValue(hashMap);


                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed
                progressDialog.dismiss();
            }
        });

        DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlists")
                .child(myUid).child(hisUid);

        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef1.child("id").setValue(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlists")
                .child(hisUid).child(myUid);

        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef2.child("id").setValue(myUid);
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
            myUid = firebaseUser.getUid();

        }
        else{
            startActivity(new Intent(ChatActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus",status);
        //Update value of onlineStatus of current user
        databaseReference.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("typingTo",typing);
        //Update value of onlineStatus of current user
        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onStart(){
        checkUserStatus();
        //set Online.
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        //get Time Stamp
        //set Offline with last seen Stamp.
        checkOnlineStatus(timeStamp);
        checkTypingStatus("noOne");
        userReForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    //handle  Permission Results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted&&storageAccepted){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(this,"Camera & Storage both Permission Necessary",Toast.LENGTH_SHORT).show();
                    }
                }
                else{

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(this,"Storage permission Necessary",Toast.LENGTH_SHORT).show();
                    }
                }
                else{

                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();

                //use this image uri to upload to firebase storage
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(requestCode==IMAGE_PICK_CAMERA_CODE){
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);
        //hide search view,add post,as we don't need here
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}