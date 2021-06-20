package com.edu.gcu.myapplication.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.gcu.myapplication.Models.ModelPost;
import com.edu.gcu.myapplication.R;
import com.edu.gcu.myapplication.ThereProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder>{

        Context context;
        List<ModelPost> postList;

        public AdapterPosts(Context context,List<ModelPost> postList){
            this.context=context;
            this.postList=postList;
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

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(Long.parseLong(pTimeSamp));
        String pTime = DateFormat.format("dd/MM/yyyy aa",cal).toString();

        //set Data
        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescr);

        try{
            Picasso.get().load(uDp).placeholder(R.drawable.ic_person_img).into(holder.uPictureIv);
        }
        catch (Exception e){

        }

        //Set Post image

        if(pImage.equals("noImage")){
            //hideimageView
            holder.pImageIv.setVisibility(View.GONE);

        }
        else {
            try {
                Picasso.get().load(pImage).into(holder.pImageIv);
            } catch (Exception e) {

            }
        }
        //handle Button clicks
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"More",Toast.LENGTH_SHORT).show();
            }
        });
        holder.interestedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Interested",Toast.LENGTH_SHORT).show();
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
