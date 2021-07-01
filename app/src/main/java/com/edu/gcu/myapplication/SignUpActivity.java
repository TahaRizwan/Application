package com.edu.gcu.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.edu.gcu.myapplication.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //configure ActionBar
    //actionbar
    private ActionBar actionBar;

    private String email = "",password="",age="",name="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //configure ActionBar

        actionBar = getSupportActionBar();
        actionBar.setTitle("SignUp");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //Init Firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Creating your Account....");
        progressDialog.setCanceledOnTouchOutside(false);


        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();//got at previous activity when back  arrow pressed
        return super.onSupportNavigateUp();
    }

    private void validateData() {
        //get Data
        name = binding.nameEt.getText().toString().trim();
        age = binding.AgeEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        //validate Data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //Invalid Email ,Don't Proceed
            binding.emailEt.setError("Invalid Email");
        }
        else if(TextUtils.isEmpty(password)){
            //no password is entered
            binding.passwordEt.setError("Enter Password");
        }

        else if(TextUtils.isEmpty(age)){
            //no age is entered
            binding.passwordEt.setError("Enter Age");
        }

        else if(TextUtils.isEmpty(name)){
            //no name is entered
            binding.passwordEt.setError("Enter Name");
        }

        else if(TextUtils.isEmpty(email)){
            //no email is entered
            binding.passwordEt.setError("Enter Email");
        }

        else if(password.length()<6){
            binding.emailEt.setError("Password must be at least 6 character long");
        }
        else{
            firebaseSignUp();
        }
    }

    private void firebaseSignUp() {
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                progressDialog.dismiss();
                //getUserInfo
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                String email = firebaseUser.getEmail();
                String uid = firebaseUser.getUid();

                HashMap<Object,String> hashMap = new HashMap<>();

                //put info in hashMap
                hashMap.put("email",email);
                hashMap.put("uid",uid);
                hashMap.put("name",name);//will add later
                hashMap.put("onlineStatus","online");
                hashMap.put("typingTo","noOne");
                hashMap.put("age",age);
                hashMap.put("phone","");//will add later
                hashMap.put("image","");//will add later
                hashMap.put("cover","");//will add later

                //firebase database instance

                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

                //path to store user data named Users

                DatabaseReference reference = firebaseDatabase.getReference("Users");

                reference.child(uid).setValue(hashMap);


                Toast.makeText(SignUpActivity.this,"Account Created\n"+email,Toast.LENGTH_SHORT).show();
                //signup success

                //open Profile Acivity
                startActivity(new Intent(SignUpActivity.this,DashboardActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //sign up failed
                progressDialog.dismiss();
                Toast.makeText(SignUpActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }
}











