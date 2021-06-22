package com.edu.gcu.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //image pick constant
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    //permission Array
    String[] cameraPermissions;
    String[] storagePermissions;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    ActionBar actionBar;

    //views
    EditText titleEt , descriptionEt;
    ImageView imageIv;
    Button uploadBtn;

    //user info
    String name , email, uid,dp;

    //info of post to be edited
    String editTitle, editDescription, editImage;

    //image picked will be saved in the uri
    Uri image_uri = null;

    //Progress Dialod
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        //enable back button in actionBar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init permission Arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd = new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();
        checkUserStatus();


        //init views
        titleEt= findViewById(R.id.pTitleEt);
        imageIv = findViewById(R.id.pImageIv);
        uploadBtn = findViewById(R.id.pUploadBtn);
        descriptionEt = findViewById(R.id.pDescriptionEt);


        //get Data through Intent from Previous activities Adapter
        Intent intent = getIntent();
        String isUpdateKey = ""+intent.getStringExtra("key");
        String editPostId = ""+intent.getStringExtra("editPostId");

        //Validate if we came here to update post i.e Came from AdapterPost
        if(isUpdateKey.equals("editPost")){
            //update
            actionBar.setTitle("Update Job");
            uploadBtn.setText("Update");
            loadPostData(editPostId);
        }
        else{
            //add
            actionBar.setTitle("Add New Job");
            uploadBtn.setText("Upload");
        }


        actionBar.setSubtitle(uid);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Query query = databaseReference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    name = ""+ds.child("name").getValue();
                    email = ""+ds.child("email").getValue();
                    dp = ""+ds.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        //get image from camera/gallery on click
        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialog
                showImagePickDialog();
            }
        });
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();
                if(TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this,"Enter title....",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this,"Enter Description....",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(isUpdateKey.equals("editPost")){
                    beginUpdate(title,description,editPostId);
                }
                else{

                    uploadData(title,description);
                }
            }
        });


    }

    private void beginUpdate(String title, String description, String editPostId) {
        pd.setMessage("Updating Job....");
        pd.show();

        if(!editImage.equals("noImage")){
            //with Image
            updateWasWithImage(title,description,editPostId);

            }
            else if (imageIv.getDrawable() != null){
                //with image
            updateWithNowImage(title,description,editPostId);
            }
            else{
                //without image
            updateWithoutImage(title,description,editPostId);
        }

        }

    private void updateWithoutImage(String title, String description, String editPostId) {
        HashMap<String,Object> hashMap = new HashMap<>();
        //put post info
        hashMap.put("uid",uid);
        hashMap.put("uName",name);
        hashMap.put("uEmail",email);
        hashMap.put("uDp",dp);
        hashMap.put("pTitle",title);
        hashMap.put("pDescr",description);
        hashMap.put("pImage","noImage");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Jobs");
        reference.child(editPostId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this,"Updated....",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateWasWithImage(String title, String description, String editPostId) {
    //post is with image,so first of All delete Pevious Image First
        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //image deleted,upload new Image
                //for post Image name,post-id,publish-time
                String timeStamp = String.valueOf(System.currentTimeMillis());
                String filePathAndName = "Jobs/"+"job_"+timeStamp;

                //get Image from ImageView
                Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //image compress
                bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                byte[] data = baos.toByteArray();

                StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
                ref.putBytes(data)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //image uploaded get its url
                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while(!uriTask.isSuccessful());

                                String downloadUri = uriTask.getResult().toString();
                                if(uriTask.isSuccessful()){
                                    //uri is received,upload to firebase database
                                    HashMap<String,Object> hashMap = new HashMap<>();
                                    //put post info
                                    hashMap.put("uid",uid);
                                    hashMap.put("uName",name);
                                    hashMap.put("uEmail",email);
                                    hashMap.put("uDp",dp);
                                    hashMap.put("pTitle",title);
                                    hashMap.put("pDescr",description);
                                    hashMap.put("pImage",downloadUri);

                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Jobs");
                                    reference.child(editPostId)
                                            .updateChildren(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    pd.dismiss();
                                                    Toast.makeText(AddPostActivity.this,"Updated....",Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //image not uploaded
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
              Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWithNowImage(String title, String description, String editPostId) {

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Jobs/"+"job_"+timeStamp;

        //get Image from ImageView
        Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //image compress
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data = baos.toByteArray();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded get its url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();
                        if(uriTask.isSuccessful()){
                            //uri is received,upload to firebase database
                            HashMap<String,Object> hashMap = new HashMap<>();
                            //put post info
                            hashMap.put("uid",uid);
                            hashMap.put("uName",name);
                            hashMap.put("uEmail",email);
                            hashMap.put("uDp",dp);
                            hashMap.put("pTitle",title);
                            hashMap.put("pDescr",description);
                            hashMap.put("pImage",downloadUri);

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Jobs");
                            reference.child(editPostId)
                                    .updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this,"Updated....",Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //image not uploaded
                pd.dismiss();
                Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Jobs");
        //get detail of post using id of post
        Query fquery = ref.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    //get Data
                    editTitle = ""+ds.child("pTitle").getValue();
                    editDescription = ""+ds.child("pDescr").getValue();
                    editImage = ""+ds.child("pImage").getValue();

                    //set data to view
                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);

                    //set Image
                    if(!editImage.equals("noImage")){
                        try{
                            Picasso.get().load(editImage).into(imageIv);
                        }
                        catch(Exception e){

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uploadData(String title, String description) {
        pd.setMessage("Publishing Job....");
        pd.show();

        //for post_image,post_name,post_id
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/"+"Post__" + timeStamp;

        if(imageIv.getDrawable()!=null){
            //get Image from ImageView
            Bitmap bitmap = ((BitmapDrawable)imageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //image compress
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
            byte[] data = baos.toByteArray();

            //post with image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            storageReference.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());

                            String downloadUri = uriTask.getResult().toString();

                            if(uriTask.isSuccessful()){
                                HashMap<Object,String> hashMap = new HashMap<>();
                                //put post info
                                hashMap.put("uid",uid);
                                hashMap.put("uName",name);
                                hashMap.put("uEmail",email);
                                hashMap.put("uDp",dp);
                                hashMap.put("pId",timeStamp);
                                hashMap.put("pTitle",title);
                                hashMap.put("pDescr",description);
                                hashMap.put("pImage",downloadUri);
                                hashMap.put("pTime",timeStamp);
                                hashMap.put("pInterested","0");
                                hashMap.put("pQuestions","0");

                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Jobs");

                                databaseReference.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this,"Post Published",Toast.LENGTH_SHORT).show();

                                                titleEt.setText("");
                                                descriptionEt.setText("");
                                                imageIv.setImageURI(null);
                                                image_uri = null;
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }else {
            HashMap<Object,String> hashMap = new HashMap<>();
            //put post info
            hashMap.put("uid",uid);
            hashMap.put("uName",name);
            hashMap.put("uEmail",email);
            hashMap.put("uDp",dp);
            hashMap.put("pId",timeStamp);
            hashMap.put("pTitle",title);
            hashMap.put("pDescr",description);
            hashMap.put("pImage","noImage");
            hashMap.put("pTime",timeStamp);
            hashMap.put("pInterested","0");
            hashMap.put("pQuestions","0");


            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Jobs");

            databaseReference.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this,"Post Published",Toast.LENGTH_SHORT).show();


                            titleEt.setText("");
                            descriptionEt.setText("");
                            imageIv.setImageURI(null);
                            image_uri = null;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
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

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus(){

        FirebaseUser firebaseUser =firebaseAuth.getCurrentUser();

        if(firebaseUser != null){

            email = firebaseUser.getEmail();
            uid = firebaseUser.getUid();

        }
        else{
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
        @Override
        public boolean onSupportNavigateUp(){
            onBackPressed();//goto previous Activity
            return super.onSupportNavigateUp();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
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

                imageIv.setImageURI(image_uri);
            }
            else if(requestCode==IMAGE_PICK_CAMERA_CODE){
                imageIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}



















