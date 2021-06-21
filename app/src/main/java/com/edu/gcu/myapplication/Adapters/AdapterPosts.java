package com.edu.gcu.myapplication.Adapters;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.widget.PopupMenuCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.gcu.myapplication.AddPostActivity;
import com.edu.gcu.myapplication.Models.ModelPost;
import com.edu.gcu.myapplication.R;
import com.edu.gcu.myapplication.ThereProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder>{

        Context context;
        List<ModelPost> postList;
        String myUid;

        private DatabaseReference interestedRef; // or interested database node
        private DatabaseReference postsRef;

        boolean mProcessInterested = false;




        public AdapterPosts(Context context,List<ModelPost> postList){
            this.context=context;
            this.postList=postList;
            myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            interestedRef = FirebaseDatabase.getInstance().getReference().child("Interested");
            postsRef = FirebaseDatabase.getInstance().getReference().child("Jobs");
        }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, viewGroup,false);
            return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int i) {
            String uid = postList.get(i).getUid();
            String uEmail = postList.get(i).getuEmail();
            String uName = postList.get(i).getuName();
            String uDp = postList.get(i).getuDp();
            String pId = postList.get(i).getpId();
            String pTitle = postList.get(i).getpTitle();
            String pDescr= postList.get(i).getpDescr();
            String pImage = postList.get(i).getpImage();
            String pTimeSamp = postList.get(i).getpTime();
            String pInterested = postList.get(i).getpInterested();//contain number of interested person

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(Long.parseLong(pTimeSamp));
        String pTime = DateFormat.format("dd/MM/yyyy aa",cal).toString();

        //set Data
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescr);
        holder.pInterestedTv.setText(pInterested+" Interested");//e.g 100 Interested

        setInterested(holder,pId);

        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_person_img).into(holder.uPictureIv);
        }
        catch (Exception e){

        }

        //Set Post image

        if(pImage.equals("noImage")){
            //hide image View
            holder.pImageIv.setVisibility(View.GONE);

        }
        else {
            holder.pImageIv.setVisibility(View.VISIBLE);

            try {
                Picasso.get().load(pImage).into(holder.pImageIv);
            } catch (Exception e) {

            }
        }
        //handle Button clicks
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn,uid,myUid,pId,pImage);
            }
        });
        holder.interestedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get total number of interested or this post,whose interested button clicked
                //if current user not liked it before,so after increase by 1
                int pInterested = Integer.parseInt(postList.get(i).getpInterested());
                mProcessInterested = true;
                //get id of post clicked
                String postIde = postList.get(i).getpId();
                interestedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(mProcessInterested){
                            if(snapshot.child(postIde).hasChild(myUid)){
                                //already interested so remove interested
                                postsRef.child(postIde).child("pInterested").setValue(""+(pInterested-1));
                                interestedRef.child(postIde).child(myUid).removeValue();
                                mProcessInterested=false;
                            }
                            else{
                                //not liked,like it
                                postsRef.child(postIde).child("pInterested").setValue(""+(pInterested+1));
                                interestedRef.child(postIde).child(myUid).setValue("Interested");
                                mProcessInterested = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        holder.questionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Question",Toast.LENGTH_SHORT).show();
            }
        });
        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
            }
        });
    }

    private void setInterested(MyHolder holder,String postKey){
            interestedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child(postKey).hasChild(myUid)){
                        //user Interested in this job
                        /*To indicate that the post is liked by this (SignedIn) user
                        Change Drawable left icon of like button
                        Change text of interested button from "Interested" to "InterestedIn"
                        * */
                        holder.interestedBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_interested_in,0,0,0);
                        holder.interestedBtn.setText("InterestedIn");

                    }
                    else{
                        //user has not interestedIn this post
                        /*To indicate that the post is liked by this (SignedIn) user
                        Change Drawable left icon of like button
                        Change text of interested button from "InterestedIn" to "Interested"
                        * */
                        holder.interestedBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_interested,0,0,0);
                        holder.interestedBtn.setText("Interested");

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
            //creating Pop Up Menu currently having Option Delete,we will add more options later on.
        PopupMenu popupMenu = new PopupMenu(context,moreBtn, Gravity.END);

        if(uid.equals(myUid)){
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
                    beginDelete(pId,pImage);
                }
                else if(id==1){
                    //Edit is Clicked
                    //Start AddPostActivity with key "edit Post" and the id of the post Clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //show Menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {
            //post can be with image or without Image
        if(pImage.equals("noImage")){
            //post is without Image
            deleteWithoutImage(pId);
        }
        else{
            deleteWithImage(pId,pImage);
        }
    }

    private void deleteWithoutImage(String pId) {
        //Progress Bar
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Jobs").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    ds.getRef().removeValue();//remove values from firebase where pid matches
                }
                //deleted
                Toast.makeText(context,"Deleted Successfully",Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
}


    private void deleteWithImage(String pId,String pImage){
            //Progress Bar
        ProgressDialog pd = new ProgressDialog(context);
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
                Query fquery = FirebaseDatabase.getInstance().getReference("Jobs").orderByChild("pId").equalTo(pId);
                fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            ds.getRef().removeValue();//remove values from firebase where pid matches
                        }
                        //deleted
                        Toast.makeText(context,"Deleted Successfully",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {

            return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView uPictureIv,pImageIv;
        TextView uNameTv,pTimeTv,pTitleTv,pDescriptionTv,pInterestedTv;
        ImageButton moreBtn;
        Button interestedBtn,questionBtn;
        LinearLayout profileLayout;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            uPictureIv =  itemView.findViewById(R.id.uPicureIv);
            pImageIv =  itemView.findViewById(R.id.pImageIv);
            uNameTv =  itemView.findViewById(R.id.uNameTv);
            pTimeTv =  itemView.findViewById(R.id.pTimeTv);
            pTitleTv =  itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv =  itemView.findViewById(R.id.pDescriptionTv);
            pInterestedTv =  itemView.findViewById(R.id.pInterestedTv);
            moreBtn =  itemView.findViewById(R.id.moreBtn);
            interestedBtn =  itemView.findViewById(R.id.interestedBtn);
            questionBtn =  itemView.findViewById(R.id.questionBtn);
            profileLayout =  itemView.findViewById(R.id.profileLayout);

        }
    }
}
