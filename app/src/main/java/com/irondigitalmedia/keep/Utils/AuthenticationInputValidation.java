package com.irondigitalmedia.keep.Utils;

import android.content.Context;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;


public class AuthenticationInputValidation {

    public static final String TAG = AuthenticationInputValidation.class.getSimpleName();
    public Context mContext;
    public String nameOfCurrentMethod;
    private boolean mCheckInput = false;

    public boolean CheckInputIsEmpty(Context context, String Email, String Pass){

        this.mContext = context.getApplicationContext();

        nameOfCurrentMethod = Thread.currentThread().getStackTrace()[2].getMethodName();

        Log.i(TAG, nameOfCurrentMethod + " - Checking to see if the fields are emtpy");
        if(Email.isEmpty()){
            // Email field is empty
            Log.i(TAG,   nameOfCurrentMethod + " - Email field is empty. Checking password field.");
            if(Pass.isEmpty()){
                // Password field is empty
                Log.i(TAG, nameOfCurrentMethod + " - Email & Password fields are empty.");

                // Default boolean is false which means fields are emtpy.

                Toast.makeText(mContext, "Email & Password field are empty.", Toast.LENGTH_SHORT).show();
            } else{
                // Password field is not empty, but email field is.
                Log.i(TAG, nameOfCurrentMethod + " - Email field is empty, but Password field is not empty.");
                Toast.makeText(mContext, "Email field is empty.", Toast.LENGTH_SHORT).show();
            }
        } else{
            // Email field is not empty
            Log.i(TAG, nameOfCurrentMethod + " - Email field is not empty.");

            // Check to make sure the email is an actual email

            if(!ValidateEmail(Email)){
                // Email entered is not a correct email format.
                Log.i(TAG, nameOfCurrentMethod + " - Email entered is not a correct email format.");
                Toast.makeText(mContext, "Please enter a proper email such as: greatrecipes@keep.com", Toast.LENGTH_SHORT).show();
            } else{
                Log.i(TAG,  nameOfCurrentMethod +" - Email entered is in the correct format");
                if(Pass.isEmpty()){
                    // Password field is empty
                    Log.i(TAG, nameOfCurrentMethod + " - Email & Password fields are empty.");
                    Toast.makeText(mContext, "Password field is empty.", Toast.LENGTH_SHORT).show();
                } else{
                    // Password field is not empty
                    Log.i(TAG, nameOfCurrentMethod + " - Email & Password fields are not empty.");
                    if(Pass.length()<8){
                        Log.i(TAG, nameOfCurrentMethod + " - Password is less than 8 characters.");
                        Toast.makeText(mContext, "Please enter a password with at least 8 characters.", Toast.LENGTH_SHORT).show();
                    }
                    mCheckInput = true;
                }
            }
        }

        return mCheckInput;
    }

    public boolean ValidateEmail(String email){
        Log.i(TAG, nameOfCurrentMethod + " - Validating the email entered.");
        if(email == null){
            return false;
        } else {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }
}
