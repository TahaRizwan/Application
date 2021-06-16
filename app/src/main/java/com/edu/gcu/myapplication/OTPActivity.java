package com.edu.gcu.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.edu.gcu.myapplication.databinding.ActivityLoginBinding;
import com.edu.gcu.myapplication.databinding.ActivityOTPBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    private ActivityOTPBinding binding;

    private PhoneAuthProvider.ForceResendingToken forceResendingToken;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String verificationId;

    private ActionBar actionBar;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog pd;

    private static final String TAG = "MAIN_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityOTPBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());


        //configure ActionBar

        actionBar = getSupportActionBar();
        actionBar.setTitle("OTP Verification ");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        binding.phoneLl.setVisibility(View.VISIBLE);
        binding.codeLl.setVisibility(View.GONE);


        firebaseAuth = FirebaseAuth.getInstance();

        //init progress Dialog
        pd = new ProgressDialog(this);
        pd.setTitle("Please wait......");
        pd.setCanceledOnTouchOutside(false);

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                pd.dismiss();
                Toast.makeText(OTPActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
            @Override
            public void onCodeSent(@NonNull String mVerificationId,@NonNull PhoneAuthProvider.ForceResendingToken token){
                super.onCodeSent(mVerificationId,forceResendingToken);

                Log.d(TAG,"onCodeSent: "+verificationId);
                verificationId = mVerificationId;
                forceResendingToken = token;
                pd.dismiss();

                binding.phoneLl.setVisibility(View.GONE);
                binding.codeLl.setVisibility(View.VISIBLE);

                Toast.makeText(OTPActivity.this,"Code Sent....",Toast.LENGTH_SHORT).show();

                binding.codeSentDescription.setText("Please type the verification code  we sent \n to "+binding.phoneEt.getText().toString().trim());
            }
        };

        binding.phoneContinueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = binding.phoneEt.getText().toString().trim();
                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(OTPActivity.this,"Please enter Phone Number....",Toast.LENGTH_SHORT).show();
                }
                else {
                    startPhoneNumberVerification(phone);
                }

            }
        });

        binding.resendcodeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = binding.phoneEt.getText().toString().trim();
                if(TextUtils.isEmpty(phone)){
                    Toast.makeText(OTPActivity.this,"Please enter Phone Number....",Toast.LENGTH_SHORT).show();
                }
                else {
                    resendVerificationCode(phone,forceResendingToken);
                }

            }
        });

        binding.codeSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = binding.codeEt.getText().toString().trim();
                if(TextUtils.isEmpty(code)){
                    Toast.makeText(OTPActivity.this,"Please enter Verification Code....",Toast.LENGTH_SHORT).show();
                }
                else{
                    verifyPhoneNumberWithCode(verificationId,code);
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();//got at previous activity when back  arrow pressed
        return super.onSupportNavigateUp();
    }


    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        pd.setMessage("Verifying Code");
        pd.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,code);
        signInWithPhoneAuthCredential(credential);

    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        pd.setMessage("Getting Ready");
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        pd.dismiss();
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                        String phone = firebaseAuth.getCurrentUser().getPhoneNumber();
                        
                        String uid = firebaseUser.getUid();

                        HashMap<Object,String> hashMap = new HashMap<>();

                        //put info in hashMap
                        hashMap.put("email","");
                        hashMap.put("uid",uid);
                        hashMap.put("name","");//will add later
                        hashMap.put("phone",phone);//will add later
                        hashMap.put("image","");//will add later

                        //firebase database instance

                        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

                        //path to store user data named Users

                        DatabaseReference reference = firebaseDatabase.getReference("Users");

                        reference.child(uid).setValue(hashMap);

                        Toast.makeText(OTPActivity.this,"GET STARTED",Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(OTPActivity.this,MainActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(OTPActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void resendVerificationCode(String phone,PhoneAuthProvider.ForceResendingToken token) {
        pd.setMessage("Resending Code");
        pd.show();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void startPhoneNumberVerification(String phone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}