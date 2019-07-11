package com.irondigitalmedia.keep.Authentication;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.irondigitalmedia.keep.Activities.MainActivity;
import com.irondigitalmedia.keep.R;
import com.irondigitalmedia.keep.Utils.AuthenticationInputValidation;
import com.irondigitalmedia.keep.Utils.Constants;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

public class Login extends AppCompatActivity implements View.OnClickListener {

    // TAG
    private static final String TAG = Login.class.getSimpleName();

    // Views
    private EditText mEmail_ET, mPass_ET;
    private Button mLogin_BT, mSignUp_BT;
    private GoogleSignInButton mGoogleSignInButton;
    private TextView mForgotPass;

    // Firebase Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser mFBUser;
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 12;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        // Firebase Initialize
        mAuth = FirebaseAuth.getInstance();
        mFBUser = mAuth.getCurrentUser();

        // Views
        InitializeViews();


        // Initialize Google
        InitializeGoogle();
        CheckCurrentUser();

    }

    private void InitializeViews() {
        mAuth = FirebaseAuth.getInstance();
        mFBUser = mAuth.getCurrentUser();
        mEmail_ET = findViewById(R.id.login_et_email);
        mPass_ET = findViewById(R.id.login_et_password);
        mLogin_BT = findViewById(R.id.login_button_login);
        mLogin_BT.setOnClickListener(this);
        mGoogleSignInButton = findViewById(R.id.login_button_google);
        mGoogleSignInButton.setOnClickListener(this);
        mSignUp_BT = findViewById(R.id.login_button_signup);
        mSignUp_BT.setOnClickListener(this);
        mForgotPass = findViewById(R.id.login_password_reset);
        mForgotPass.setOnClickListener(this);
    }

    private void CheckCurrentUser() {
        if(mAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }
    }

    private void InitializeGoogle() {
        // Configure Google Sign In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(),gso);
    }

    private void ResetPassword() {
        Log.i(TAG, "ResetPassword:........ Method");
    }

    private void SignupUser() {
        Log.i(TAG, "SignupUser:........ Method ");

        Intent intent = new Intent(Login.this, SignUp.class);
        startActivity(intent);
    }

    private void LoginUser() {
        Log.i(TAG, "LoginUser:........ Method ");
        String Email = mEmail_ET.getText().toString().trim();
        String Pass = mPass_ET.getText().toString().trim();

        AuthenticationInputValidation authenticationInputValidation = new AuthenticationInputValidation();
        if(authenticationInputValidation.CheckInputIsEmpty(this,Email, Pass)){
            Log.i(TAG, "LoginUser: CheckInput is " + authenticationInputValidation.CheckInputIsEmpty(this, Email, Pass));

            mAuth.signInWithEmailAndPassword(Email,Pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            GoToProfile();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Log.i(TAG, "onSuccess: User successfully logged in.");

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Log.i(TAG, "LoginUser: CheckInput is " + authenticationInputValidation.CheckInputIsEmpty(this, Email, Pass));

        }


    }

    private void GoToProfile() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onItemClick: ..... Item has been clicked");
        switch(v.getId()){
            case R.id.login_button_login:
                Log.i(TAG, "onItemClick: ..... Login Button Clicked");
                LoginUser();
                break;
            case R.id.login_button_signup:
                Log.i(TAG, "onItemClick: ..... Signup Button Clicked");
                SignupUser();
                break;
            case R.id.login_button_google:
                GoogleSignIn();
                break;
            case R.id.login_password_reset:
                Log.i(TAG, "onItemClick: ..... Forgot Password Button Clicked");
                ResetPassword();
                break;
            default:
                break;
        }
    }

    private void GoogleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void GoToProfileSetup(String Email, GoogleSignInAccount acct) {
        Log.i(TAG, "GoToProfileSetup: Google Account Info: " + acct);
        Intent intent = new Intent(Login.this,ProfileSetup.class);
        intent.putExtra(Constants.USER_PROPERTY_EMAIL,Email);
        intent.putExtra(Constants.GOOGLE_ACCOUNT,acct);
        startActivity(intent);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            mFBUser = mAuth.getCurrentUser();
                            if(task.getResult().getAdditionalUserInfo().isNewUser()){
                                if(mFBUser!=null){
                                    // Check if user's account has already been made.
                                    Log.i(TAG, "onComplete: user email is = " + mFBUser.getEmail());
                                    GoToProfileSetup(mFBUser.getEmail(),acct);
                                }else{
                                    Log.e(TAG, "onComplete: User is Null");
                                }
                            }else{
                                GoToProfile();
                            }
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.main_frame), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }
}
