package com.irondigitalmedia.keep.Authentication;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.irondigitalmedia.keep.Activities.MainActivity;
import com.irondigitalmedia.keep.R;
import com.irondigitalmedia.keep.Utils.AuthenticationInputValidation;

public class SignUp extends AppCompatActivity implements View.OnClickListener {

    // TAG
    private static final String TAG = SignUp.class.getSimpleName();


    // Views
    private EditText mEmail_ET, mPass_ET;
    private Button mSignUp_BT;
    private TextView resendVerification;

    // Firebase Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser mFBUser;
    private Context mContext;
    private MainActivity mainActivity;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_signup);
        // Views
        mEmail_ET = findViewById(R.id.signup_et_email);
        mPass_ET = findViewById(R.id.signup_et_password);
        mSignUp_BT = findViewById(R.id.signup_button_signup);
        resendVerification = findViewById(R.id.signup_tv_resendVerification);
        resendVerification.setOnClickListener(this);
        mSignUp_BT.setOnClickListener(this);

        // Initializing Firebase
        mAuth = FirebaseAuth.getInstance();
        mFBUser = mAuth.getCurrentUser();


    }

    private void SignUpUser() {
        Log.i(TAG, "SignUpUser: ............Method");
        String Email = mEmail_ET.getText().toString().trim();
        String Pass = mPass_ET.getText().toString().trim();

        AuthenticationInputValidation authenticationInputValidation = new AuthenticationInputValidation();
        if(authenticationInputValidation.CheckInputIsEmpty(this,Email,Pass)){
            Log.i(TAG, "SignUpUser: CheckInput Result = " + authenticationInputValidation.CheckInputIsEmpty(this, Email, Pass));
            GoToProfileSetup(Email,Pass);
        } else {
            Log.i(TAG, "SignUpUser: CheckInput Result = " + authenticationInputValidation.CheckInputIsEmpty(this, Email, Pass));
        }

    }

    private void GoToLogin() {
        Intent intent = new Intent(SignUp.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void GoToProfile() {
        Intent intent = new Intent(SignUp.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void GoToProfileSetup(String Email, String Pass) {
        Intent intent = new Intent(SignUp.this,ProfileSetup.class);
        intent.putExtra("email",Email);
        intent.putExtra("pass",Pass);
        startActivity(intent);
        mPass_ET.setText("");
        mEmail_ET.setText("");
    }


    private void sendVerificationEmail() {
        Log.i(TAG, "sendVerificationEmail: Sending verification email....");
        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, " Email Verification Sent.");
                Toast.makeText(getApplicationContext(), "Email Verification Sent.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onItemClick: ............has been clicked");
        switch (v.getId()){
            case R.id.signup_button_signup:
                Log.i(TAG, "onItemClick: ............SignUp Button has been clicked");
                SignUpUser();
                break;
            case R.id.signup_tv_resendVerification:
                Log.i(TAG, "onItemClick: ............SignUp Resend Verification");
                ResendVerification();
        }
    }

    private void ResendVerification() {
        Log.i(TAG, "ResendVerification: Sending verification email...");
        mAuth.getCurrentUser()
                .sendEmailVerification()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(SignUp.this, "Verification Email Sent.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Activity Session Handlers


    @Override
    protected void onStart() {
        super.onStart();

        if(mFBUser!=null){
            mFBUser = mAuth.getCurrentUser();
        }

    }
}
