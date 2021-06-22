package com.edu.gcu.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.edu.gcu.myapplication.Adapters.AdapterQuestions;
import com.edu.gcu.myapplication.Models.ModelQuestion;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {
    //to  get detail of user and post
    String myUid,myEmail,myName,myDp,postId,pInterested,hisDp,hisName,hisUid,pImage;


    boolean mProcessQuestion =false;
    boolean mProcessInterested = false;

    //Progress Bar
    ProgressDialog pd;


    //views
    ImageView uPictureIv,pImageIv;
    TextView uNameTv,pTimeTv,pTitleTv,pDescriptionTv,pInterestedTv,pQuestionsTv;
    ImageButton moreBtn;
    Button interestedBtn;
    LinearLayout profileLayout;
    RecyclerView recyclerView;
    List<ModelQuestion> questionList;

    AdapterQuestions adapterQuestions;


    EditText questionEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);


        //ActionBar and its properties
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Job Detail");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //get id of post using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        //init views
        pQuestionsTv = findViewById(R.id.pQuestionsTv);
        pTimeTv = findViewById(R.id.pTimeTv);
        uPictureIv = findViewById(R.id.uPicureIv);
        pImageIv = findViewById(R.id.pImageIv);
        uNameTv = findViewById(R.id.uNameTv);
        pTitleTv = findViewById(R.id.pTitleTv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pInterestedTv = findViewById(R.id.pInterestedTv);
        moreBtn = findViewById(R.id.moreBtn);
        interestedBtn = findViewById(R.id.interestedBtn);
        profileLayout = findViewById(R.id.profileLayout);
        questionEt = findViewById(R.id.questionEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);
        recyclerView = findViewById(R.id.recyclerView);

        loadPostInfo();

        checkUserStatus();

        loadUserInfo();

        setInterested();

        loadQuestion();

        //set subtitle of actionbar
        actionBar.setSubtitle("Signed as "+myEmail);

        //send question
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postQuestion();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });

        interestedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interestedPost();
            }
        });
    }

    private void loadQuestion() {
        //layout(Linear) for recyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        //set layout to recyclerview
        recyclerView.setLayoutManager(layoutManager);

        //init question list
        questionList = new ArrayList<>();

        //path of the post,to get its comment
         DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Jobs").child(postId).child("Questions");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questionList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelQuestion modelQuestion = ds.getValue(ModelQuestion.class);

                    questionList.add(modelQuestion);
                    //set up adapter

                    //pass myUid and postId as  parameter of constructor of comment Adapter

                    adapterQuestions = new AdapterQuestions(getApplicationContext(),questionList,myUid,postId);

                    //set Adapter
                    recyclerView.setAdapter(adapterQuestions);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showMoreOptions() {
        //creating Pop Up Menu currently having Option Delete,we will add more options later on.
        PopupMenu popupMenu = new PopupMenu(this,moreBtn, Gravity.END);

        if(hisUid.equals(myUid)){
            //Add items in Menu
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");
        }
        //Item Clicked listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(id==0){
                    //delete is clicked
                    beginDelete();
                }
                else if(id==1){
                    //Edit is Clicked
                    //Start AddPostActivity with key "edit Post" and the id of the post Clicked
                    Intent intent = new Intent(PostDetailActivity.this, AddPostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",postId);
                    startActivity(intent);
                }
                return false;
            }
        });
        //show Menu
        popupMenu.show();
    }

    private void beginDelete() {
        //post can be with image or without Image
        if(pImage.equals("noImage")){
            //post is without Image
            deleteWithoutImage();
        }
        else{
            deleteWithImage();
        }
    }

    private void deleteWithoutImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Jobs").orderByChild("pId").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    ds.getRef().removeValue();//remove values from firebase where pid matches
                }
                //deleted
                Toast.makeText(PostDetailActivity.this,"Deleted Successfully",Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private  void  deleteWithImage(){
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting...");
        /*
         * Steps:
         * 1) Delete Image using Url
         * 2) Delete from Database using Post id
         * */

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query fquery = FirebaseDatabase.getInstance().getReference("Jobs").orderByChild("pId").equalTo(postId);
                fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            ds.getRef().removeValue();//remove values from firebase where pid matches
                        }
                        //deleted
                        Toast.makeText(PostDetailActivity.this,"Deleted Successfully",Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed,can't go further
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setInterested() {
        DatabaseReference interestedRef = FirebaseDatabase.getInstance().getReference().child("Interested");

        interestedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postId).hasChild(myUid)){
                    //user Interested in this job
                        /*To indicate that the post is liked by this (SignedIn) user
                        Change Drawable left icon of like button
                        Change text of interested button from "Interested" to "InterestedIn"
                        * */
                    interestedBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_interested_in,0,0,0);
                    interestedBtn.setText("InterestedIn");

                }
                else{
                    //user has not interestedIn this post
                        /*To indicate that the post is liked by this (SignedIn) user
                        Change Drawable left icon of like button
                        Change text of interested button from "InterestedIn" to "Interested"
                        * */
                    interestedBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_interested,0,0,0);
                    interestedBtn.setText("Interested");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void interestedPost() {
        //Get total number of interested or this post,whose interested button clicked
        //if current user not liked it before,so after increase by 1
        mProcessInterested = true;
        //get id of post clicked
        DatabaseReference interestedRef = FirebaseDatabase.getInstance().getReference().child("Interested");
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Jobs");

        interestedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessInterested){
                    if(snapshot.child(postId).hasChild(myUid)){
                        //already interested so remove interested
                        postsRef.child(postId).child("pInterested").setValue(""+(Integer.parseInt(pInterested)-1));
                        interestedRef.child(postId).child(myUid).removeValue();
                        mProcessInterested=false;

                    }
                    else{
                        //not liked,like it
                        postsRef.child(postId).child("pInterested").setValue(""+(Integer.parseInt(pInterested)+1));
                        interestedRef.child(postId).child(myUid).setValue("Interested");
                        mProcessInterested = false;

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void postQuestion() {
        pd = new ProgressDialog(this);
        pd.setMessage("Adding Comment...");

        String question = questionEt.getText().toString().trim();

        if(TextUtils.isEmpty(question)){
            Toast.makeText(this,"Question is Empty....... ",Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());

        //each post have child
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Jobs").child(postId).child("Questions");

        HashMap<String,Object> hashMap = new HashMap<>();

        hashMap.put("cId",timeStamp);
        hashMap.put("Question",question);
        hashMap.put("timeStamp",timeStamp);
        hashMap.put("uid",myUid);
        hashMap.put("uEmail",myEmail);
        hashMap.put("uDp",myDp);
        hashMap.put("uName",myName);

        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(PostDetailActivity.this,"Question Added",Toast.LENGTH_SHORT).show();
                        questionEt.setText("");
                        updateQuestionCount();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateQuestionCount() {
        mProcessQuestion = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Jobs").child(postId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcessQuestion){
                    String questions =""+snapshot.child("pQuestions").getValue();
                    int newQuestionVal = Integer.parseInt(questions) + 1;
                    ref.child("pQuestions").setValue(""+newQuestionVal);
                    mProcessQuestion = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserInfo() {
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    myName = ""+ds.child("name").getValue();
                    myDp = ""+ds.child("image").getValue();

                    try{
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_person_img).into(cAvatarIv);
                    }
                    catch(Exception e){
                        Picasso.get().load(R.drawable.ic_person_img).into(cAvatarIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo() {
        //get Post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Jobs");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //keep checking the posts until get the required post
                for(DataSnapshot ds:snapshot.getChildren()){
                    //get Data
                    String pTitle = ""+ds.child("pTitle").getValue();
                    String pDescr = ""+ds.child("pDescr").getValue();
                    pInterested = ""+ds.child("pInterested").getValue();
                    String pTimeStamp = ""+ds.child("pTime").getValue();
                    pImage = ""+ds.child("pImage").getValue();
                    hisDp = ""+ds.child("uDp").getValue();
                    hisUid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEmail").getValue();
                    hisName = ""+ds.child("uName").getValue();
                    String questionCount = ""+ds.child("pQuestions").getValue();
                    //convert timestamp to dd//mm/yyyy hh:mm aa
                    Calendar cal = Calendar.getInstance(Locale.getDefault());
                    cal.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

                    //set data
                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pInterestedTv.setText(pInterested + "Interested");
                    pTimeTv.setText(pTime);
                    pQuestionsTv.setText(questionCount+" Questions");

                    uNameTv.setText(hisName);

                    if(pImage.equals("noImage")){
                        //hide image View
                        pImageIv.setVisibility(View.GONE);

                    }
                    else {
                        pImageIv.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage).into(pImageIv);
                        } catch (Exception e) {

                        }
                    }

                    //set user image in comment
                    try{
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_person_img).into(uPictureIv);
                    }
                    catch (Exception e){

                        Picasso.get().load(R.drawable.ic_person_img).into(uPictureIv);

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            myEmail = user.getEmail();
            myUid = user.getUid();
        }
        else{
            startActivity(new Intent(this,DashboardActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        //hide some menu items
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id==R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}