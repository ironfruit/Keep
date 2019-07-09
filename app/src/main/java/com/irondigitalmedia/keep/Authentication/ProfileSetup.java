package com.irondigitalmedia.keep.Authentication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.irondigitalmedia.keep.MainActivity;
import com.irondigitalmedia.keep.Model.User;
import com.irondigitalmedia.keep.R;
import com.irondigitalmedia.keep.Utils.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSetup extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ProfileSetup.class.getSimpleName();

    // Firebase Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser mFBuser;
    private String userUID;
    private String user_email;
    private String user_password;

    // Firebase Database Reference
    private StorageReference mStorage;
    private StorageReference mStorage2;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private String users = "users";
    private String profile = "profile";
    private String name,username,about,profilePhotoURL,cloudStoragePath;

    // Views
    private EditText mName, mUsername, mAbout;
    private Button mSave, mCancel;
    private CircleImageView mProfileImage, mAddProfileImage;
    private ProgressBar mProgressBar;

    // Files
    private String pictureFilePath;
    private String deviceIdentifier;

    // Permissions
    private int PERMISSION_ALL = 1;
    private String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    // Camera
    public static final int MEDIA_TAKE_IMAGE = 100;
    public static final int MEDIA_CHOOSE_IMAGE = 200;
    private static String imageStoragePath;
    private Uri filePath;
    private Bitmap bitmap;
    private Bitmap rotatedBitmap;

    // Gallery directory name to store the images or videos
    public static final String GALLERY_DIRECTORY_NAME = "Keep_Profile_Data";

    // key to store image path in savedInstance state
    public static final String KEY_IMAGE_STORAGE_PATH = "image_path";
    private String profilePhoto = "_PROFILE_PHOTO.jpg";
    private String mCurrentPhotoPath;
    private String uid;

    private boolean mGoogleSignInBoolean = false;

    public ProfileSetup() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        GetUserCredentials();

        // Initialize Views
        InitializeViews();

        // Request Permissions
        RequestPermissions();

        //Initialize FirebaseAuth
        initializeFirebase();

        // Set OnClickListeners
        SeOnClickListeners();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mFBuser = mAuth.getCurrentUser();
    }

    private void SeOnClickListeners() {
        mSave.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mProfileImage.setOnClickListener(this);
        mAddProfileImage.setOnClickListener(this);
    }

    private void InitializeViews() {
        mName = findViewById(R.id.profile_setup_name_et);
        mUsername = findViewById(R.id.profile_setup_username_et);
        mAbout = findViewById(R.id.profile_setup_about_et);
        mProfileImage = findViewById(R.id.profile_setup_image);
        mAddProfileImage = findViewById(R.id.profile_setup_image_add);
        mSave = findViewById(R.id.profile_setup_button_save);
        mCancel = findViewById(R.id.profile_setup_button_cancel);
        mProgressBar = findViewById(R.id.profile_setup_progressbar);
        mProgressBar.setVisibility(View.GONE);
    }

    private void SaveUserInfo() {
        name = mName.getText().toString().trim();
        username = mUsername.getText().toString().trim();
        about = mAbout.getText().toString().trim();

        SendInfoToTheCloud(name, user_email, username, about);
    }

    private void GoToMain() {
        Intent intent = new Intent(ProfileSetup.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void SendInfoToTheCloud(final String name,final String email, final String username, final String about) {
        SignInUserWithEmail(name,email, username, about);
    }

    private void SignInUserWithEmail(final String name,final String email, final String username, final String about) {
        Log.i(TAG, "SignInUserWithEmail: user email " + user_email);
        mAuth.createUserWithEmailAndPassword(user_email, user_password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.i(TAG, "onSuccess: I think it's working.............." + authResult);
                        FinishUploads(name, user_email, username, about);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: For some reason this shit sucks... and didn't work... " + e.getMessage());
            }
        });
    }

    private void GetUserCredentials() {
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            user_email = bundle.getString("email");
            user_password = bundle.getString("pass");

        } else {
            Log.i(TAG, "SendInfoToTheCloud: Info didn't send over to ProfileSetup Activity");
        }
    }

    private void FinishUploads(final String name,final String email, final String username,final String about) {
        Log.i(TAG, "FinishUploads: Completing user info uploads to cloud");
        userUID = mAuth.getUid();
        cloudStoragePath = users + "/" + userUID + "/" + profile + "/" + profilePhoto;
        database = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();

        if(filePath!=null) {

            final ProgressDialog progressDialog = new ProgressDialog(getApplicationContext());
            Log.i(TAG, "FinishUploads: URI " + filePath);
            final String toFilePath = cloudStoragePath;
            Log.i(TAG, "FinishUploads: uploading photo " + toFilePath + " to storage.");

            mStorage2 = mStorage.child(toFilePath);
            mStorage2.putFile(filePath)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Log.i(TAG, "onComplete: Photo has been uploaded to cloud storage. ");
                    // Update user url
                    Log.i(TAG, "onComplete: updating user object with photo URL");
                    mFBuser = mAuth.getCurrentUser();
                    if(mFBuser!=null){
                        userUID = mFBuser.getUid();
                        cloudStoragePath = users + "/"  + userUID + "/" + profile + "/" + profilePhoto;
                        mStorage2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String baseURL = uri.toString();
                                Log.i(TAG, "onSuccess: BASE URL IS " + baseURL);
                                Log.i(TAG, "onComplete: uploading the user object to the cloud database");
                                // Upload user data
                                UploadUserObject(name,email, baseURL,username,about);
                            }
                        });
                    }



                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");


                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Log.e(TAG, "onFailure: Failed to upload image to cloud storage here's why " + e.getMessage());
                }
            });
        }

    }

    private void UploadUserObject(String name, String email, String photoURL, String username, String about) {
        Log.i(TAG, "UploadUserObject: updating user object in the cloud: " + name +" "+email+" "+photoURL+" "+username+" "+about);
        mFBuser = mAuth.getCurrentUser();
        if(mFBuser!=null){
            String mUserUID = mFBuser.getUid();
            User user = new User(name,username,about,email,photoURL, false);
            myRef = database.getReference();
            myRef.child(users).child(mUserUID).setValue(user)
                    .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "onFailure: " + e.getMessage());
                }
            }).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.i(TAG, "onComplete: " + task.getResult());
                  GoToMain();
                }
            });
        }

    }

    private void CancelSavingUserInfo() {
        Log.i(TAG, "CancelSavingUserInfo: Cancelled Profile Setup");
        GoToSignUp();
    }

    private void GoToSignUp() {
        Intent intent = new Intent(ProfileSetup.this, SignUp.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void ChoosePhoto() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), MEDIA_CHOOSE_IMAGE);

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
                filePath = FileProvider.getUriForFile(getApplicationContext(),"com.irondigitalmedia.fileprovider",imageFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,filePath);
                startActivityForResult(pictureIntent,MEDIA_TAKE_IMAGE);
            }

        }

    }

    // Get Image File
    private File getImageFile() throws IOException {
        Log.i(TAG, "getImageFile: method has ran");
        if(mFBuser!=null){
            uid = mFBuser.getUid();
        }
        String imageName = uid + "_PROFILE_PHOTO";
        Log.i(TAG, "getImageFile: imageName = " + imageName);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        Log.i(TAG, "getImageFile: creating temp file...");
        File imageFile = File.createTempFile(imageName,".jpg",storageDir);
        mCurrentPhotoPath = imageFile.getAbsolutePath();
        Log.i(TAG, "getImageFile: file created... " + mCurrentPhotoPath);
        return imageFile;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.profile_setup_button_save:
                SaveUserInfo();
                break;
            case R.id.profile_setup_button_cancel:
                CancelSavingUserInfo();
                break;
            case R.id.profile_setup_image:{
                SelectPictureDialog();
                break;
                }
            default:
                break;
        }
    }

    private void SelectPictureDialog() {
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select a Picture");
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MEDIA_TAKE_IMAGE && resultCode == RESULT_OK) {

            Bitmap bitmap= null;
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(filePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            mProfileImage.setImageBitmap(bitmap);
            galleryAddPic();
        }else if(requestCode == MEDIA_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            Bitmap bitmap= null;
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mProfileImage.setImageBitmap(bitmap);
            galleryAddPic();
        }else {
            Toast.makeText(this, "You haven't picked Image",Toast.LENGTH_LONG).show();
        }

    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

}













