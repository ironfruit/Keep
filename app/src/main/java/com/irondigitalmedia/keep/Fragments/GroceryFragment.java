package com.irondigitalmedia.keep.Fragments;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.irondigitalmedia.keep.Adapters.GroceryAdapter;
import com.irondigitalmedia.keep.Model.Grocery;
import com.irondigitalmedia.keep.Model.Recipe;
import com.irondigitalmedia.keep.R;
import com.irondigitalmedia.keep.Utils.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroceryFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = GroceryFragment.class.getSimpleName();

    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private String UserUid;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private FirebaseDatabase mDatabase;
    private StorageReference groceryRef;

    private RecyclerView groceryview;
    private List<Grocery> mGroceryList;
    private List<String> mGroceryIds;
    private LinearLayoutManager llm;
    private GroceryAdapter adapter;
    private View view;
    private Grocery mGrocery;
    private String dataSnapShotKey;

    private ImageView mGroceryPhoto;
    private TextView mGroceryName;
    private EditText mGroceryNameInput;
    private String GroceryInput;

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

    private String mCurrentPhotoPath;


    public GroceryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_grocery, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getActivity().setTitle("Grocery Items");
        setHasOptionsMenu(true);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mGroceryList = new ArrayList<>();
        mGroceryIds = new ArrayList<>();
        groceryview = view.findViewById(R.id.grocery_frag_recyclerView);
        groceryview.setItemAnimator(new DefaultItemAnimator());
        llm = new LinearLayoutManager(getContext());
        groceryview.setLayoutManager(llm);
        groceryview.setItemAnimator(new DefaultItemAnimator());
        adapter = new GroceryAdapter(getContext(),mGroceryList);
        groceryview.setAdapter(adapter);
        mDatabase = FirebaseDatabase.getInstance();
        mDatabase.getReference().child(Constants.DATABASE_ROOT_GROCERY)
                .child(mUser.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                dataSnapShotKey = dataSnapshot.getKey();
                // A new comment has been added, add it to the displayed list
                mGrocery = dataSnapshot.getValue(Grocery.class);


                // Update RecyclerView
                mGroceryIds.add(dataSnapshot.getKey());
                mGroceryList.add(mGrocery);
                adapter.notifyItemInserted(mGroceryList.size() - 1);
                groceryview.scrollToPosition(mGroceryList.lastIndexOf(mGrocery));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Grocery newGrocery = dataSnapshot.getValue(Grocery.class);
                String groceryKey = dataSnapshot.getKey();
                // [START_EXCLUDE]
                int groceryIndex = mGroceryIds.indexOf(groceryKey);
                if (groceryIndex > -1) {
                    // Replace with the new data
                    mGroceryList.set(groceryIndex, newGrocery);

                    // Update the RecyclerView
                    adapter.notifyItemChanged(groceryIndex);
                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + groceryKey);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String groceryKey = dataSnapshot.getKey();

                // [START_EXCLUDE]
                int groceryIndex = mGroceryIds.indexOf(groceryKey);
                if (groceryIndex > -1) {
                    // Remove data from the list
                    mGroceryList.remove(groceryIndex);
                    mGroceryIds.remove(groceryIndex);

                    // Update the RecyclerView
                    adapter.notifyItemRemoved(groceryIndex);
                } else {
                    Log.w(TAG, "onChildRemoved:unknown_child:" + groceryKey);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabase.getReference().keepSynced(true);

        // Inflate the layout for this fragment
        return view;
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
        if(pictureIntent.resolveActivity(getActivity().getPackageManager())!=null){
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
                filePath = FileProvider.getUriForFile(getActivity().getApplicationContext(),"com.irondigitalmedia.fileprovider",imageFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,filePath);
                startActivityForResult(pictureIntent,MEDIA_TAKE_IMAGE);
            }

        }

    }

    public void AddNewGroceryName(){

        // setup the alert builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View builderView = inflater.inflate(R.layout.grocery_alertdialog_add,null);
        mGroceryNameInput = builderView.findViewById(R.id.grocery_alert_add_grocery_name);
                builder.setView(builderView)
                .setTitle("Enter Name")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        GroceryInput = mGroceryNameInput.getText().toString().trim();
                        dialogInterface.dismiss();
                        SelectPictureDialog();
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

    private void SelectPictureDialog() {
        if(getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
    public void onStart() {
        super.onStart();
        mUser = mAuth.getCurrentUser();
        groceryRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public void onResume() {
        super.onResume();
        mUser = mAuth.getCurrentUser();
    }


    // Get Image File
    private File getImageFile() throws IOException {
        Log.i(TAG, "getImageFile: method has ran");
        if(mUser!=null){
            UserUid = mUser.getUid();
        }
        String imageName = UserUid + "_PROFILE_PHOTO";
        Log.i(TAG, "getImageFile: imageName = " + imageName);
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        Log.i(TAG, "getImageFile: creating temp file...");
        File imageFile = File.createTempFile(imageName,".jpg",storageDir);
        mCurrentPhotoPath = imageFile.getAbsolutePath();
        Log.i(TAG, "getImageFile: file created... " + mCurrentPhotoPath);
        return imageFile;
    }

    public void UploadGroceryItem(final String groceryInput){
        final String input = groceryInput;
        Log.i(TAG, "UploadGroceryItem: Input = " + GroceryInput);
        Log.i(TAG, "UploadGroceryItem: Photo bitmap = " + bitmap);


        final String key = mDatabase.getReference().push().getKey();
        final StorageReference itemRef = groceryRef.child(Constants.DATABASE_ROOT_USERS).child(mUser.getUid()).child(Constants.DATABASE_ROOT_GROCERY).child(key);
        itemRef.putFile(filePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                itemRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {


                        String url = uri.toString();


                        Grocery item = new Grocery(key,input,url);
                        mDatabase.getReference().child(Constants.DATABASE_ROOT_GROCERY).child(mUser.getUid()).child(key).setValue(item);
                    }
                });
                Toast.makeText(getContext(),"Grocery Item Added.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MEDIA_TAKE_IMAGE && resultCode == RESULT_OK) {

            Bitmap bitmap= null;
            try {
                bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(filePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            UploadGroceryItem(GroceryInput);
        }else if(requestCode == MEDIA_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            Bitmap bitmap= null;
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            UploadGroceryItem(GroceryInput);
        }else {
            Toast.makeText(getActivity(), "You haven't picked Image",Toast.LENGTH_LONG).show();
        }

    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            // Set buttons
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.grocery_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.grocery_menu_item_add_new){

            // Add new Grocery photo then name
            AddNewGroceryName();

        }
        return super.onOptionsItemSelected(item);
    }
}
