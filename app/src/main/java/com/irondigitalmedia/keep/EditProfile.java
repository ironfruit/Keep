package com.irondigitalmedia.keep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.irondigitalmedia.keep.Model.User;
import com.irondigitalmedia.keep.Utils.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = EditProfile.class.getSimpleName();

    // Firebase Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    // Firebase Database Reference
    private StorageReference mStorage;
    private StorageReference mStorage2;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private String cloudStoragePath, mCurrentPhotoPath,mChangeEmail_Str,mChangePass_Str,mChangePass_OldPass_str;
    private ToggleButton mUserProfilePrivacy;
    private EditText mOldPassword,mChangePass_ET,mChangeEmail_ET;
    private User user;


    // Permissions
    private int PERMISSION_ALL = 1;
    private String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE};


    private Bitmap bitmap;
    public static final int MEDIA_TYPE_IMAGE = 100;
    public static final int MEDIA_TYPE_CHOOSE_IMAGE = 200;
    private Uri imageFileURI;
    private boolean mUserPrivacy;


    private EditText mName, mUsername, mAbout;
    private Button mChangeEmail_BT, mChangePassword_BT,mResetPassword_Bt;
    private CircleImageView mProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        RequestPermissions();

        // Firebase
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mProfileImage = findViewById(R.id.frag_profile_edit_image);
        mProfileImage.setOnClickListener(this);
        mName = findViewById(R.id.frag_profile_edit_name);
        mUsername = findViewById(R.id.frag_profile_edit_username);
        mAbout = findViewById(R.id.frag_profile_edit_about);
        mChangeEmail_BT = findViewById(R.id.frag_profile_edit_button_email);
        mChangeEmail_BT.setOnClickListener(this);
        mChangePassword_BT = findViewById(R.id.frag_profile_edit_button_password);
        mChangePassword_BT.setOnClickListener(this);
        mUserProfilePrivacy = findViewById(R.id.frag_profile_edit_privacy_toggle);
        mUserProfilePrivacy.setOnClickListener(this);
        if(getUid()!=null){
            myRef = database.getReference().child(Constants.DATABASE_ROOT_USERS).child(getUid());
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                    mName.setText(user.name);
                    mUsername.setText(user.username);
                    mAbout.setText(user.about);
                    Glide.with(getApplicationContext()).load(user.getUrl()).centerCrop().into(mProfileImage);
                    if(user.privacy){
                        mUserProfilePrivacy.setChecked(true);
                    }else{
                        mUserProfilePrivacy.setChecked(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void SelectPhoto() {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(getResources().getStringArray(R.array.photo_import_options),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "onClick: int " + which);
                        switch (which) {
                            case 0:{
                                // take a photo
                                try {
                                    takePicture();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                            case 1:{
                                // choose a photo
                                ChoosePhoto();
                                break;
                            }
                            default:
                                break;
                        }
                    }
                });
// create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop: Fragment Stopped");
    }

    private void ChoosePhoto() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), MEDIA_TYPE_CHOOSE_IMAGE);

    }

    // start Take Photo Intent
    private void takePicture() throws IOException {
        Log.i(TAG, "TakePhoto: method has ran");
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(pictureIntent.resolveActivity(getPackageManager())!=null){
            File imageFile = null;

            try{
                Log.i(TAG, "TakePhoto: getting file from getImageFile() method.");
                imageFile = getImageFile();
                Log.i(TAG, "TakePhoto: imageFile has new file " + imageFile.getName());
            } catch (IOException e){
                e.printStackTrace();
            }
            if(imageFile!=null){
                Log.i(TAG, "ChoosePhoto: starting the activity for result.");
                imageFileURI = FileProvider.getUriForFile(this,"com.irondigitalmedia.fileprovider",imageFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageFileURI);
                startActivityForResult(pictureIntent,MEDIA_TYPE_IMAGE);
            }

        }

    }

    // Get Image File
    private File getImageFile() throws IOException {
        Log.i(TAG, "getImageFile: method has ran");

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        String imageName = "PROFILE_" + getUid() + "_JPG_"+timeStamp+"_";
        Log.i(TAG, "getImageFile: imageName = " + imageName);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        Log.i(TAG, "getImageFile: creating temp file...");
        File imageFile = File.createTempFile(imageName,".jpg",storageDir);
        mCurrentPhotoPath = imageFile.getAbsolutePath();
        Log.i(TAG, "getImageFile: file created... " + mCurrentPhotoPath);
        return imageFile;
    }

    private void RequestPermissions() {
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void UploadProfilePhoto(){
        Log.i(TAG, "UploadProfilePhoto: Upload Profile Photo...");
        cloudStoragePath = Constants.DATABASE_ROOT_USERS + "/" + getUid() + "/" + Constants.USER_PROFILE + "/" + Constants.USER_PROFILE_PHOTO;
        InitializeFirebaseStorageDatabase();

        if(imageFileURI!=null) {
            Log.i(TAG, "UploadProfilePhoto: filePath is not Null");
            Log.i(TAG, "FinishUploads: URI " + imageFileURI);
            final String toFilePath = cloudStoragePath;
            Log.i(TAG, "FinishUploads: uploading photo " + toFilePath + " to storage.");

            mStorage2 = mStorage.child(toFilePath);
            mStorage2.putFile(imageFileURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Log.i(TAG, "onComplete: Photo has been uploaded to cloud storage. ");

                    // Update user url
                    Log.i(TAG, "onComplete: updating user object with photo URL");
                    mUser = mAuth.getCurrentUser();
                    if(mUser !=null){
                        cloudStoragePath = Constants.DATABASE_ROOT_USERS + "/"  + getUid() + "/" + Constants.USER_PROFILE + "/" + Constants.USER_PROFILE_PHOTO;
                        mStorage2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String baseURL = uri.toString();
                                Log.i(TAG, "onSuccess: BASE URL IS " + baseURL);
                                UpdateUserProfilePhotoURL(baseURL);
                                Log.i(TAG, "updateUserObjectPhotoUrl: User Photo is located at " + baseURL);
                            }
                        });
                    } else{
                        Log.e(TAG, "onComplete: User is some how null....");
                    }
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    // ADD PROGRESS BAR


                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // DISMISS PROGRESS BAR
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // DISMISS PROGRESS BAR
                    Log.e(TAG, "onFailure: Failed to upload image to cloud storage here's why " + e.getMessage());
                }
            });
        } else{
            Log.e(TAG, "UploadProfilePhoto: filePath is some how null");
        }
    }

    private void InitializeFirebaseStorageDatabase() {
        database = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
    }

    private void UpdateUserProfilePhotoURL(String profilePhotoURL) {
        Log.i(TAG, "UpdateUserProfilePhotoURL: updating user object in the cloud: " + profilePhotoURL);
        mUser = mAuth.getCurrentUser();
        if(mUser !=null){
            myRef = FirebaseDatabase.getInstance().getReference();
            myRef.child(Constants.DATABASE_ROOT_USERS).child(getUid()).child(Constants.USER_PROPTERY_URL).setValue(profilePhotoURL)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "onSuccess: Successful.");
                        }
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: doing some photo stuff");
        if (requestCode == MEDIA_TYPE_IMAGE && resultCode == RESULT_OK) {

            Bitmap bitmap= null;
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageFileURI));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            UploadProfilePhoto();
            mProfileImage.setImageBitmap(bitmap);
        }else if(requestCode == MEDIA_TYPE_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            Bitmap bitmap= null;
            imageFileURI = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imageFileURI);
            } catch (IOException e) {
                e.printStackTrace();
            }
            UploadProfilePhoto();
            mProfileImage.setImageBitmap(bitmap);

        }else {
            Toast.makeText(this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_profile,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.edit_profile_sumbit){

            // Submit updated user profile info + photo
            SubmitUserProfileUpdates();

        }
        return super.onOptionsItemSelected(item);
    }

    private void SubmitUserProfileUpdates() {
        if(getUid()!=null){
            String name = mName.getText().toString().trim();
            String username = mUsername.getText().toString().trim();
            String about = mAbout.getText().toString().trim();
            myRef = database.getReference().child(Constants.DATABASE_ROOT_USERS).child(getUid());
            Map<String,Object> childupdates = new HashMap<>();
            childupdates.put(Constants.USER_PROPERTY_NAME,name);
            childupdates.put(Constants.USER_PROPERTY_USERNAME,username);
            childupdates.put(Constants.USER_PROPERTY_ABOUT,about);
            myRef.updateChildren(childupdates);
            onBackPressed();
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mName.getWindowToken(), 0);
            InputMethodManager imm1 = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm1.hideSoftInputFromWindow(mUsername.getWindowToken(), 1);
            InputMethodManager imm2 = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm2.hideSoftInputFromWindow(mAbout.getWindowToken(), 2);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.frag_profile_edit_image:
                // update photo and upload it to storage
                SelectPhoto();
                break;
            case R.id.frag_profile_edit_button_email:
                ChangeEmail();
                break;
            case R.id.frag_profile_edit_button_password:
                ChangePassword();
                break;
            case R.id.frag_profile_edit_privacy_toggle:
                SetPrivacy();
                break;
        }
    }

    private void ChangePassword() {
        mUser = mAuth.getCurrentUser();
        // setup the alert builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password")
                .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String email = mUser.getEmail();
                        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(EditProfile.this, "Password Reset Email Sent.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void ChangeEmail() {
        // setup the alert builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View builderView = inflater.inflate(R.layout.edit_profile_dialog_email,null);
        mChangeEmail_ET = builderView.findViewById(R.id.edit_profile_dialog_email_ET);
        builder.setView(builderView)
                .setTitle("Change Email")
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mChangeEmail_Str = mChangeEmail_ET.getText().toString().trim();
                        mUser.updateEmail(mChangeEmail_Str).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(),"Email Update Successful.",Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "onSuccess: Email Updated.");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                                Log.e(TAG, "onFailure: " + e.getMessage());
                            }
                        });
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void SetPrivacy() {
        Log.i(TAG, "SetPrivacy: Privacy = " + mUserProfilePrivacy.isChecked());
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if(mUserProfilePrivacy.isChecked()){
            if(getUid()!=null){
                myRef = database.getReference().child(Constants.DATABASE_ROOT_USERS).child(getUid()).child(Constants.USER_PROPERTY_PRIVACY);
                myRef.setValue(true);
            }else{
                Log.i(TAG, "SetPrivacy: is null...");
            }
        }else {
            if(getUid()!=null){
                myRef = database.getReference().child(Constants.DATABASE_ROOT_USERS).child(getUid()).child(Constants.USER_PROPERTY_PRIVACY);
                myRef.setValue(false);
            }else{
                Log.i(TAG, "SetPrivacy: is null...");
            }
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
