package com.edu.gcu.myapplication.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.gcu.myapplication.Models.ModelQuestion;
import com.edu.gcu.myapplication.R;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterQuestions extends RecyclerView.Adapter<AdapterQuestions.myHolder>{

    Context context;
    List<ModelQuestion> questionList;

    public AdapterQuestions(Context context, List<ModelQuestion> questionList) {
        this.context = context;
        this.questionList = questionList;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        //bind the row_questions.xml layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_questions,viewGroup,false);
        return new myHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int i) {
        //get the data
        String uid = questionList.get(i).getUid();
        String name = questionList.get(i).getuName();
        String email = questionList.get(i).getuEmail();
        String image = questionList.get(i).getuDp();
        String cid = questionList.get(i).getcId();
        String question = questionList.get(i).getQuestion();
        String timeStamp = questionList.get(i).getTimeStamp();

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String pTime = DateFormat.format("dd/MM/yyyy aa",cal).toString();

        //set the data
        holder.nameTv.setText(name);
        holder.questionsTv.setText(question);
        holder.timeTv.setText(pTime);
        //set user dp
        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_person_img).into(holder.avatarIv);
        }
        catch (Exception e){

        }



    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    class myHolder extends RecyclerView.ViewHolder{

        //views
        ImageView avatarIv;
        TextView nameTv,questionsTv,timeTv;

        public myHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            questionsTv = itemView.findViewById(R.id.questionsTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
