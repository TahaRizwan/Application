package com.edu.gcu.myapplication.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.gcu.myapplication.Models.ModelQuestion;
import com.edu.gcu.myapplication.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterQuestions extends RecyclerView.Adapter<AdapterQuestions.myHolder>{

    boolean mQuestion =false;

    Context context;
    List<ModelQuestion> questionList;
    String myUid,postId;

    public AdapterQuestions(Context context, List<ModelQuestion> questionList, String myUid, String postId) {
        this.context = context;
        this.questionList = questionList;
        this.myUid = myUid;
        this.postId = postId;
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
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //check if this comment is by currently signed in user or not
                    if(myUid.equals(uid)){
                        //mine comment
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                        builder.setTitle("Delete");
                        builder.setMessage("Are you sure to delete this question? ");
                        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //delete
                                deleteQuestion(cid);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss
                            }
                        });

                        builder.create().show();
                    }
                    else{
                        //not mine comment
                        Toast.makeText(context,"Can't delete other's comment...",Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }



    }

    private void deleteQuestion(String cid) {
        mQuestion=true;
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Jobs").child(postId);
        ref.child("Questions").child(cid).removeValue();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mQuestion) {
                    String questions = "" + snapshot.child("pQuestions").getValue();
                    int newQuestionVal = Integer.parseInt(questions) - 1;
                    ref.child("pQuestions").setValue("" + newQuestionVal);
                    mQuestion = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
