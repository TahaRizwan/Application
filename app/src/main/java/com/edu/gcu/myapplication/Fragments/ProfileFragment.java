package com.edu.gcu.myapplication.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.edu.gcu.myapplication.Adapters.AdapterPosts;
import com.edu.gcu.myapplication.AddPostActivity;
import com.edu.gcu.myapplication.LoginActivity;
import com.edu.gcu.myapplication.Models.ModelPost;
import com.edu.gcu.myapplication.R;
import com.edu.gcu.myapplication.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
public class ProfileFragment extends Fragment {


    ProgressDialog pd;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    //Storage
    StorageReference storageReference ;

    //path where images of profile and cover will be  stored
    String storagePath = "Users_Profile_Cover_Imgs/";


    ImageView coverIv,avatarIv;
    FloatingActionButton fab;
    TextView nameTv,emailTv,phoneTv;
    RecyclerView postsRecyclerView;

    //permission constants
    private  static final int CAMERA_REQUEST_CODE = 100;
    private  static final int STORAGE_REQUEST_CODE = 200;
    private  static final int IMAGE_PICK_GALLERY_CODE = 300;
    private  static final int IMAGE_PICK_CAMERA_CODE = 400;

    //arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    //uri of picked Image

    Uri image_uri;
    String profileOrCoverPhoto;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        //init arrays of  permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init views
        avatarIv = view.findViewById(R.id.avatarIv);
        coverIv = view.findViewById(R.id.coverIv);
        nameTv = view.findViewById(R.id.nameTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        emailTv = view.findViewById(R.id.emailTv);
        fab = view.findViewById(R.id.fab);
        postsRecyclerView = view.findViewById(R.id.recyclerview_posts);

        pd = new ProgressDialog(getActivity());

        Query query = databaseReference.orderByChild("uid").equalTo(firebaseUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Checked Until Required Data get
                for(DataSnapshot ds:snapshot.getChildren()){
                    String name = ""+ ds.child("name").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String phone = ""+ ds.child("phone").getValue();
                    String image = ""+ ds.child("image").getValue();
                    String cover = ""+ ds.child("cover").getValue();

                    //set data

                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    try{
                        Picasso.get().load(image).into(avatarIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_person_white).into(avatarIv);
                    }
                    try{
                        Picasso.get().load(cover).into(coverIv);

                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_person_white).into(coverIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }});


        postList = new ArrayList<>();

        checkUserStatus();
        loadMyPosts();
        return  view;

    }

    private void loadMyPosts() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest Posts First,for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Jobs");

        //query to load
        Query query = databaseReference.orderByChild("uid").equalTo(uid);
        //get all data from this reference
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(myPosts);

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(),postList);

                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(),""+error.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void searchMyPosts(String searchQuery) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest Posts First,for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Jobs");

        //query to load
        Query query = databaseReference.orderByChild("uid").equalTo(uid);
        //get all data from this reference
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if(myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
                        //add to list
                        postList.add(myPosts);
                    }
                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(),postList);

                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(),""+error.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }

    private  boolean checkStoragePermission(){
        //check for permission
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        //request runtime storage permission
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }
    private  boolean checkCameraPermission(){
        //check for permission
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result&&result1;
    }
    private void requestCameraPermission(){
        //request runtime storage permission
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        /*Show Dialog conaining Options
        1) Edit Profile Picture
        2) Edit  Cover Picture
        3) Edit Name
        4) Edit Phone
        5) Edit Email
        * */

        String options[]={"Edit Profile Picture","Edit  Cover Picture","Edit Name","Edit Phone","Edit Email"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //set Title
        builder.setTitle("Choose Action");

        //set item to dialog
        builder.setItems(options, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog,int which){
                //handle dialog items click
                if(which == 0){
                    //edit Profile clicked
                    pd.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();
                }
                else if(which==1){
                    //edit Cover clicked
                    pd.setMessage("Updating Cover Picture");
                    profileOrCoverPhoto = "cover";
                    showImagePicDialog();
                }
                else if(which==2){
                    //edit Name clicked
                    pd.setMessage("Updating Name");

                    showNamePhoneEmailDialog("name");
                }
                else if(which==3){
                    //edit Phone clicked
                    pd.setMessage("Updating Phone");

                    showNamePhoneEmailDialog("phone");
                }
                else if(which==4){
                    //edit Email clicked
                    pd.setMessage("Updating Email");

                    showNamePhoneEmailDialog("email");
                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void showNamePhoneEmailDialog(String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key);

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        EditText editText = new EditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString().trim();

                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object> result = new HashMap<>();

                    result.put(key,value);

                    databaseReference.child(firebaseUser.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(),"Updated...",Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });

                    //if user edit his name,Also change it fom his posts

                    if(key.equals("name")){
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Jobs");
                        Query query  =databaseReference.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot ds:snapshot.getChildren()){
                                    String child = ds.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds:snapshot.getChildren()){
                                    String child = ds.getKey();
                                    if(snapshot.child(child).hasChild("Questions")){
                                        String child1 = ""+snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Jobs").child(child1).child("Questions").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for(DataSnapshot ds: snapshot.getChildren()){
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

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


                }
                else{
                    Toast.makeText(getActivity(),"Please enter "+key,Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }

    private void showImagePicDialog() {
        String options[]={"Camera","Gallery"};

        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //set Title
        builder.setTitle("Pick Image From");

        //set item to dialog
        builder.setItems(options, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog,int which){
                //handle dialog items click
                if(which == 0){

                    //camera clicked
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else{
                        pickFromCamera();
                    }
                }
                else if(which==1){
                    //Gallery clicked
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else{
                        pickFromGallery();
                    }
                }
            }
        });
        //create and show dialog
        builder.create().show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{

                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(getActivity(),"Please Enable Camera and Storage Permission",Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;

            case STORAGE_REQUEST_CODE:{

                if(grantResults.length>0){
                    boolean writeStorageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted){
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(getActivity(),"Please Enable Storage Permission",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

        }





        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){

            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //image picked from gallery,get uri of image
                image_uri = data.getData();
                uploadProfileCoverPhoto(image_uri);
            }
            if(requestCode==IMAGE_PICK_CAMERA_CODE){
                //image picked from camera,get uri of image
                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        pd.show();
        //path and name of image to be stored in firebase storage
        String filePathAndName = storagePath+""+profileOrCoverPhoto+"_"+firebaseUser.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //Check if image is uploaded or not uri
                        if(uriTask.isSuccessful()){
                            HashMap<String,Object> results = new HashMap<>();
                            results.put(profileOrCoverPhoto,downloadUri.toString());

                            databaseReference.child(firebaseUser.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(),"Image Updated",Toast.LENGTH_SHORT).show();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(),"Error in Updating Image ",Toast.LENGTH_SHORT).show();
                                }
                            });


                            //if user edit his name,Also change it fom his posts

                            if(profileOrCoverPhoto.equals("name")){
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Jobs");
                                Query query  =databaseReference.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot ds:snapshot.getChildren()){
                                            String child = ds.getKey();
                                            snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                //update user image  in current users comments on posts
                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds:snapshot.getChildren()){
                                            String child = ds.getKey();
                                            if(snapshot.child(child).hasChild("Questions")){
                                                String child1 = ""+snapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Jobs").child(child1).child("Questions").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for(DataSnapshot ds: snapshot.getChildren()){
                                                            String child = ds.getKey();
                                                            snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

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

                    }
                        else {
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Some Error Occurred",Toast.LENGTH_SHORT).show();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(getActivity(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

        }


    private void pickFromCamera() {
        //intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        //put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);

    }@Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        setHasOptionsMenu(true);//to show menu option in fragment
        super.onCreate(savedInstanceState);
    }
    private void checkUserStatus(){

        FirebaseUser firebaseUser =firebaseAuth.getCurrentUser();

        if(firebaseUser != null){
            uid=firebaseUser.getUid();

        }
        else{
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){

        inflater.inflate(R.menu.menu_main,menu);

        MenuItem item = menu.findItem(R.id.action_search);
        //searchview of search user specific posts
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!TextUtils.isEmpty(s)){
                    searchMyPosts(s);
                }else{
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!TextUtils.isEmpty(s)){
                    searchMyPosts(s);
                }
                else{
                    loadMyPosts();
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
        if(id==R.id.action_add_post){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


}