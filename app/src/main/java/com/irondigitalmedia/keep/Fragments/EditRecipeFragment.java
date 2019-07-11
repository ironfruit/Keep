package com.irondigitalmedia.keep.Fragments;


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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.irondigitalmedia.keep.Adapters.EditIngredientAdapter;
import com.irondigitalmedia.keep.Adapters.EditStepsAdapter;
import com.irondigitalmedia.keep.BaseFragment;
import com.irondigitalmedia.keep.MainActivity;
import com.irondigitalmedia.keep.Model.Ingredient;
import com.irondigitalmedia.keep.Model.Instruction;
import com.irondigitalmedia.keep.Model.Recipe;
import com.irondigitalmedia.keep.Model.TempPhoto;
import com.irondigitalmedia.keep.Model.User;
import com.irondigitalmedia.keep.R;
import com.irondigitalmedia.keep.Utils.Constants;
import com.shawnlin.numberpicker.NumberPicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditRecipeFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = EditRecipeFragment.class.getSimpleName();


    // Views
    private EditText mIngreds_ET,mSteps_ET,mTitle_Et, mDesc_ET;
    private Bitmap bitmap;
    private ImageView mRecipePhoto;
    private Button mIngredAdd,mIngredRemove,mStepAdd,mStepRemove,mPrepTime;
    private Switch mPrivacySwitch;
    private Boolean privacy;
    private RecyclerView mIngredRV,mStepRV;
    private NumberPicker numberPicker1,numberPicker2;
    private Uri imageFileURI;

    private List<Ingredient> mIngredsList;
    private List<Instruction> mStepsList;
    private LinearLayoutManager mIngredManager,mStepsManager;
    private EditIngredientAdapter mIngredientsAdapter;
    private EditStepsAdapter mEditStepsAdapter;

    // Objects
    private Recipe recipe;


    public static final int MEDIA_TYPE_IMAGE = 100;
    public static final int MEDIA_TYPE_CHOOSE_IMAGE = 200;

    private String mCurrentPhotoPath;
    private String cloudStoragePath;
    private String users = "users";
    private String recipes = "recipes";
    private String recipeId;
    private String ingredientsId;
    private String instructionsId;
    private String mUsername;

    // Firebase
    private DatabaseReference mDatabase;
    private DatabaseReference mIngredientDatabaseRef;
    private DatabaseReference mInstructionsDatabaseRef;
    private StorageReference mStorage;
    private String userUID;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;

    private AlertDialog alertdialog;

    private File DRP;

    private int prep_time;
    private int num;

    private Instruction instruction;
    private Ingredient ingredient;

    private Context mContext = getContext();

    private MainActivity mainActivity;
    private Toolbar toolbar;

    public EditRecipeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_add_edit, container, false);

        mContext = view.getContext();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getActivity().setTitle("Edit Recipe");

        mainActivity = (MainActivity) view.getContext();
        mainActivity.mMainNav.setSelectedItemId(R.id.nav_edit_recipe);

        toolbar = mainActivity.findViewById(R.id.main_toolbar);
        mainActivity.setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.edit_recipe);

        InitializeFirebaseCloud();

        InitializeFirebaseAuth();

        recipeId = mDatabase.child(Constants.DATABASE_ROOT_RECIPES).push().getKey();
        Log.i(TAG, "onCreateView: Recipe ID = " + recipeId);
        mIngredientDatabaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.DATABASE_RECIPE_INGREDIENT).child(recipeId);
        mInstructionsDatabaseRef = FirebaseDatabase.getInstance().getReference().child(Constants.DATABASE_RECIPE_INSTRUCTION).child(recipeId);


        // Views
        mRecipePhoto = view.findViewById(R.id.recipe_add_imagview);
        mTitle_Et = view.findViewById(R.id.recipe_add_title_et);
        mDesc_ET = view.findViewById(R.id.recipe_add_desc_et);
        mPrepTime = view.findViewById(R.id.recipe_add_prepTime_et);
        mPrepTime.setOnClickListener(this);
        mRecipePhoto.setOnClickListener(this);
        mPrivacySwitch = view.findViewById(R.id.recipe_add_edit_privacy_switch);
        mPrivacySwitch.setChecked(false);
        mPrivacySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mPrivacySwitch.isChecked()){
                    mPrivacySwitch.setText(R.string.recipe_add_edit_privacy_private);
                }else{
                    mPrivacySwitch.setText(R.string.recipe_add_edit_privacy_public);
                }
            }
        });
        // Ingredients Recyclerview
        mIngreds_ET = view.findViewById(R.id.recipe_add_et_ingred);
        mIngredAdd = view.findViewById(R.id.recipe_add_edit_et_ingred_add);
        mIngredRemove = view.findViewById(R.id.recipe_add_edit_et_ingred_remove);
        mIngredAdd.setOnClickListener(this);
        mIngredRemove.setOnClickListener(this);
        mIngredRV = view.findViewById(R.id.recipe_rv_ingredients);
        mIngredsList = new ArrayList<>();
        mIngredRV.setItemAnimator(new DefaultItemAnimator());
        mIngredManager = new LinearLayoutManager(getContext());
        mIngredRV.setLayoutManager(mIngredManager);
        mIngredRV.setItemAnimator(new DefaultItemAnimator());
        // mIngredRV.addItemDecoration(new DividerItemDecoration((getContext()), DividerItemDecoration.VERTICAL));


        // Steps Recyclerview
        mSteps_ET = view.findViewById(R.id.recipe_add_et_step);
        mStepAdd = view.findViewById(R.id.recipe_add_edit_et_steps_add);
        mStepRemove = view.findViewById(R.id.recipe_add_edit_et_steps_remove);
        mStepAdd.setOnClickListener(this);
        mStepRemove.setOnClickListener(this);
        mStepRV = view.findViewById(R.id.recipe_rv_instructions);
        mStepsList = new ArrayList<>();
        num = mStepsList.size() + 1;
        mStepRV.setItemAnimator(new DefaultItemAnimator());
        mStepsManager = new LinearLayoutManager(getContext());
        mStepRV.setLayoutManager(mStepsManager);
        mStepRV.setItemAnimator(new DefaultItemAnimator());
       // mStepRV.addItemDecoration(new DividerItemDecoration((getContext()), DividerItemDecoration.VERTICAL));

        CheckPermissions();

        // Inflate the layout for this fragment
        return view;
    }

    private void InitializeFirebaseCloud() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
    }

    // Create method to choose photo
    public void ChoosePhoto(){

        Intent choosePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(choosePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            choosePictureIntent.setAction(Intent.ACTION_GET_CONTENT);
            choosePictureIntent.setType("image/*");
            startActivityForResult(choosePictureIntent, MEDIA_TYPE_CHOOSE_IMAGE);
        }

    }

    // Send photo taken to gallery
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
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
                imageFileURI = FileProvider.getUriForFile(getActivity().getApplicationContext(),"com.irondigitalmedia.fileprovider",imageFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageFileURI);
                startActivityForResult(pictureIntent,MEDIA_TYPE_IMAGE);
            }

        }

    }

    // Get Image File
    private File getImageFile() throws IOException {
        Log.i(TAG, "getImageFile: method has ran");

        String imageName = recipeId;
        Log.i(TAG, "getImageFile: imageName = " + imageName);
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        Log.i(TAG, "getImageFile: creating temp file...");
        File imageFile = File.createTempFile(imageName,".jpg",storageDir);
        mCurrentPhotoPath = imageFile.getAbsolutePath();
        Log.i(TAG, "getImageFile: file created... " + mCurrentPhotoPath);
        return imageFile;
    }

    // Upload recipe to the cloud
    public void UploadRecipeObjectToCloud(){

        final String title = mTitle_Et.getText().toString().trim();
        final String desc = mDesc_ET.getText().toString().trim();
        final String prepTime = mPrepTime.getText().toString().trim();

        if(TextUtils.isEmpty(title)){
            Toast.makeText(mContext,"Enter a Recipe Title.",Toast.LENGTH_SHORT).show();
        }else{
            if(TextUtils.isEmpty(desc)){
                Toast.makeText(mContext,"Enter a Recipe Description.",Toast.LENGTH_SHORT).show();
            }else{
                if(TextUtils.isEmpty(prepTime)){
                    Toast.makeText(mContext,"Enter a Recipe Prep Time.",Toast.LENGTH_SHORT).show();
                }else{
                    if(mIngredientsAdapter.getItemCount()==0){
                        Toast.makeText(mContext,"Enter an Ingredient.",Toast.LENGTH_SHORT).show();
                    }else{
                        if(mEditStepsAdapter.getItemCount()==0){
                            Toast.makeText(mContext,"Enter an Instruction.",Toast.LENGTH_SHORT).show();
                        }else{
                            if(imageFileURI==null){
                                Toast.makeText(mContext,"Select an Image to Upload.",Toast.LENGTH_SHORT).show();
                            }else {
                                InitializeFirebaseAuth();
                                if (mUser != null) {
                                    userUID = mUser.getUid();
                                    cloudStoragePath = users + "/" + userUID + "/" + recipes + "/" + recipeId + "/";

                                    mDatabase.child(userUID).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            User user = dataSnapshot.getValue(User.class);
                                            if(user!=null){
                                                mUsername = user.getUsername();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }

                                final StorageReference filepath = mStorage.child(cloudStoragePath).child(recipeId);
                                filepath.putFile(imageFileURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        Toast.makeText(getContext(), "Upload Successful!", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {


                                                String url = uri.toString();


                                                Recipe recipe = new Recipe(userUID, recipeId, title, desc, prepTime, url);
                                                Map<String, Object> recipeValues = recipe.toMap();
                                                Map<String, Object> childUpdates = new HashMap<>();
                                                childUpdates.put(Constants.DATABASE_ROOT_RECIPES + recipeId, recipeValues);
                                                childUpdates.put(Constants.DATABASE_ROOT_USERS_RECIPES + userUID + "/" + recipeId, recipeValues);
                                                mDatabase.updateChildren(childUpdates);


                                            }
                                        });


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "Upload Failed!", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        GoToSearch();
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }

    }

    // send user back to the recipe fragment
    public void GoToSearch(){
        HomeFragment sf = new HomeFragment();
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(R.id.main_frame, sf);
        ft.addToBackStack(null);
        ft.commit();
    }

    // Check Permissions at fragment create
    private void CheckPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.i("Add New Recipe", "CheckPermissions: Permissions are not granted ask for them.");
            ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE }, 0);
        } else{
            Log.i("Add New Recipe", "CheckPermissions: Permissions are granted.");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MEDIA_TYPE_IMAGE && resultCode == RESULT_OK) {

            Bitmap bitmap= null;
            try {
                bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(imageFileURI));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            galleryAddPic();
            mRecipePhoto.setImageBitmap(bitmap);
        }else if(requestCode == MEDIA_TYPE_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            Bitmap bitmap= null;
            imageFileURI = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),imageFileURI);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecipePhoto.setImageBitmap(bitmap);

        }else {
            Toast.makeText(getActivity(), "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.action_add_recipe_check,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.recipe_menu_add_submit){

                UploadRecipeObjectToCloud();
        }
        if(item.getItemId()==R.id.recipe_menu_add_submit_fakedata){
            try {
                SendFakeData();
            } catch (IOException e) {
                e.printStackTrace();
            }
            GoToSearch();

        }
        return super.onOptionsItemSelected(item);
    }

    private void SendFakeData() throws IOException {
        mStorage = FirebaseStorage.getInstance().getReference();
        // Retrieve template photos for fake recipes.
        Log.i(TAG, "SendFakeData: getting template photos if they don't exist");
        mDatabase.child("recipe-temp-photos").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TempPhoto tempPhotoName = dataSnapshot.getValue(TempPhoto.class);
                ArrayList<TempPhoto> filenames = new ArrayList<>();
                filenames.add(tempPhotoName);
                Log.i(TAG, "onChildAdded: starting for loop to get all the files downloaded.");
                for(int i = 0;i<filenames.size();i++) {
                    String name = filenames.get(i).getName();
                    StorageReference templatePhotos = mStorage.child(Constants.DATABASE_ROOT_TEMP_PHOTOS).child(name);
                    File destination = new File(Environment.getExternalStorageDirectory() + "/Keep/Files/Photos/template-photos/");
                    if(!destination.exists()){
                        destination.mkdirs();
                    }

                    File photo = new File(destination.toString() + "/" + name);

                    if(!photo.exists()){
                        templatePhotos.getFile(photo).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                Log.i(TAG, "onSuccess: " + taskSnapshot.getBytesTransferred());
                            }
                        });
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        String[] titles = {"BBQ","Spaghetti","Steak","Chocolate Chip Cookies", "Spaghetti and Meatballs","BBQ Cabobs","Beef Tacos"};
        String[] descriptions = {"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque sit amet scelerisque.","Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque sit amet scelerisque.","Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque sit amet scelerisque.","Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque sit amet scelerisque.","Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque sit amet scelerisque.","Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque sit amet scelerisque.","Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque sit amet scelerisque."};
        String[] prepTimes = {"30m","45m","20m","1h 30m", "1h", "1h 15m", "15m","25m"};
        String[] creators = {"IamArnold","IronFuturist","IamBANANA","IamIRONman","IamBatman","IamSuperman","Greenlanturd","Deadpool","OhhiPizza"};

        // Establish new random
        Random random = new Random();

        if(mUser!=null && mAuth!=null){
            InitializeFirebaseCloud();
            InitializeFirebaseAuth();
            userUID = mAuth.getUid();
            cloudStoragePath = users + "/" + userUID + "/" + recipes + "/" + recipeId + "/";
        }
        final String ingredientsId = mDatabase.child(Constants.DATABASE_RECIPE_INGREDIENT).push().getKey();
        final String instructionsId = mDatabase.child(Constants.DATABASE_RECIPE_INSTRUCTION).push().getKey();
        final StorageReference filepath = mStorage.child(cloudStoragePath).child(recipeId);
        Log.i(TAG, "SendFakeData: FILE PATH TO STORAGE = " + filepath.toString());
        final String recipeTitle = titles[random.nextInt(titles.length)];
        final String recipeDesc = descriptions[random.nextInt(descriptions.length)];
        final String recipePrepTime = prepTimes[random.nextInt(prepTimes.length)];
        final String recipeCreator = creators[random.nextInt(creators.length)];

        Log.i(TAG, "getUriFromFile: Fake Data = " + recipeId);
        Log.i(TAG, "getUriFromFile: Fake Data = " + ingredientsId);
        Log.i(TAG, "getUriFromFile: Fake Data = " + instructionsId);
        Log.i(TAG, "getUriFromFile: Fake Data = " + filepath);
        Log.i(TAG, "getUriFromFile: Fake Data = " + recipeTitle);
        Log.i(TAG, "getUriFromFile: Fake Data = " + recipeDesc);
        Log.i(TAG, "getUriFromFile: Fake Data = " + recipePrepTime);
        Log.i(TAG, "getUriFromFile: Fake Data = " + recipeCreator);

        String randompicturesDIR;

        // Get sourceRandomPhoto for files
        File sourceRandomPhoto = new File(Environment.getExternalStorageDirectory() + "/Keep/Files/Photos/template-photos/");
        if(!sourceRandomPhoto.exists()){
            sourceRandomPhoto.mkdirs();
        }



        randompicturesDIR = sourceRandomPhoto.toString();
        Log.i(TAG, "getUriFromFile: sourceRandomPhotos = " + randompicturesDIR);

        // Create a list of those files
        File[] files = sourceRandomPhoto.listFiles();

        // Check to see if that sourceRandomPhoto exists.
        if(sourceRandomPhoto.exists()) {

            // Now randomly select a file


            // Source file is rando
            File rando = files[random.nextInt(files.length)];
            Log.i(TAG, "getUriFromFile: Random File = " + rando);

            File newFolder = new File(Environment.getExternalStorageDirectory() + "/Keep/Files/Photos/temp");

            if (!newFolder.exists()) {
                newFolder.mkdirs();
                Log.i(TAG, "getUriFromFile: New Folder Created = " + newFolder.getPath());
            } else {
                Log.i(TAG, "getUriFromFile: Folder already exists.");
            }

            // Destination is temp/randomphoto
            File destination = new File(Environment.getExternalStorageDirectory() + "/Keep/Files/Photos/Recipes/" + recipeId + "/");
            if(destination.exists()){
                DRP = new File(destination.toString() + "/" + recipeId + ".jpg");
                // Copy that file to a sub folder called "/temp"
                try (InputStream in = new FileInputStream(rando)) {
                    try (OutputStream out = new FileOutputStream(DRP)) {
                        // Transfer bytes from in to out
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    }
                }

                // Rename file to new name with the Recipe Id
                Log.i(TAG, "getUriFromFile: New Temp Path = " + newFolder);

                Log.i(TAG, "getUriFromFile: New File Dir = " + DRP.getPath());

                String newTempPath = (newFolder + "/" + recipeId + ".jpg");
                File tempPhoto = new File(newTempPath);
                DRP.renameTo(tempPhoto);

                Log.i(TAG, "getUriFromFile: New Updated File Name in Temp Dir = " + tempPhoto.getName());

                // Save reference to this file as the imageURI

                imageFileURI = FileProvider.getUriForFile(getContext(), "com.irondigitalmedia.fileprovider", tempPhoto);
            }else {
                destination.mkdirs();
                DRP = new File(destination.toString() + recipeId + ".jpg");
                // Copy that file to a sub folder called "/temp"
                try (InputStream in = new FileInputStream(rando)) {
                    try (OutputStream out = new FileOutputStream(DRP)) {
                        // Transfer bytes from in to out
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    }
                }

                // Rename file to new name with the Recipe Id
                Log.i(TAG, "getUriFromFile: New Temp Path = " + newFolder);

                Log.i(TAG, "getUriFromFile: New File Dir = " + DRP.getPath());

                String newTempPath = (newFolder + "/" + recipeId + ".jpg");
                File tempPhoto = new File(newTempPath);
                DRP.renameTo(tempPhoto);

                Log.i(TAG, "getUriFromFile: New Updated File Name in Temp Dir = " + tempPhoto.getName());

                // Save reference to this file as the imageURI
                imageFileURI = FileProvider.getUriForFile(getContext(), "com.irondigitalmedia.fileprovider", tempPhoto);
                Log.i(TAG, "getUriFromFile: imageFileUri = " + imageFileURI.getPath());
            }


        } else{
            Log.e(TAG, "getFakeUriFromFile: Directory does not exist.");
        }


        filepath.putFile(imageFileURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Toast.makeText(getContext(), "Upload Successful!", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        String url = uri.toString();

                        Ingredient ingredient = new Ingredient(ingredientsId,"1 Pack small corn or flour tortillas");

                        Instruction instruction = new Instruction(instructionsId,"1. ", "Cook beef");

                        // Create Map for recipe
                        // upload to root RECIPES && USER-RECIPES
                        // UPLOAD INGREDIENTS && INSTRUCTIONS TO THEIR OWN ROOT AS WELL
                        // INSTRUCTS USE THIS PATH //ROOT/RECIPE-INSTRUCTION/recipeId/InstructionId:{values}
                        // INGREDS USE THIS PATH //ROOT/RECIPE-INGREDIENTS/recipeId/IngredientId:{values}


                        Recipe recipe = new Recipe(userUID,recipeId,recipeTitle,recipeDesc,recipePrepTime,url);
                        Map<String,Object> recipeValues = recipe.toMap();
                        Map<String,Object> childUpdates = new HashMap<>();
                        childUpdates.put(Constants.DATABASE_ROOT_RECIPES + "/" + recipeId, recipeValues);
                        childUpdates.put(Constants.DATABASE_ROOT_USERS_RECIPES + userUID + "/" + recipeId, recipeValues);
                        mDatabase.updateChildren(childUpdates);
                        for(int i = 0; i < 2; i++){
                            mDatabase.child(Constants.DATABASE_RECIPE_INGREDIENT).child(recipeId).push().setValue(ingredient);
                            mDatabase.child(Constants.DATABASE_RECIPE_INSTRUCTION).child(recipeId).push().setValue(instruction);
                        }


                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Upload Failed!", Toast.LENGTH_SHORT).show();
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                GoToSearch();
            }
        });


        Log.i(TAG, "SendFakeData: recipe-ingredients key  = " + "recipe-ingredients/" + ingredientsId);
        Log.i(TAG, "SendFakeData: recipe-instructions key = " + "recipe-instructions/" + instructionsId);
        Log.i(TAG, "SendFakeData: recipe key              = " + "recipes/" + recipeId);
        Log.i(TAG, "SendFakeData: user-recipe key         = " + "user-recipes/" + getUid() + "/" + recipeId);

    }

    private void getFakeUriFromFile() {

        // for testing code

    }

    private void InitializeFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.recipe_add_imagview:
                SelectDialog();
                break;
            case R.id.recipe_add_prepTime_et:
                SelectTimeDialog();
                break;
            case R.id.recipe_add_edit_et_ingred_add:
                postIngredient();
                break;
            case R.id.recipe_add_edit_et_ingred_remove:
                removeIngredient();
                break;
            case R.id.recipe_add_edit_et_steps_add:
                postInstruction();
                break;
            case R.id.recipe_add_edit_et_steps_remove:
                removeInstruction();
                break;
            default:
                break;
        }
    }

    private void postIngredient() {
        String ingredientText = mIngreds_ET.getText().toString().trim();
        if(TextUtils.isEmpty(ingredientText)){
            Toast.makeText(mContext,"Enter an Ingredient.", Toast.LENGTH_SHORT).show();
        }else{
            Log.i(TAG, "postIngredient: Posting Ingredient | Ingredient ID = " +ingredientsId);
            ingredientsId = mDatabase.child(Constants.DATABASE_RECIPE_INGREDIENT).push().getKey();
            // Create new comment object
            ingredient = new Ingredient(ingredientsId, ingredientText);

            // Push the comment, it will appear in the list
            mIngredientDatabaseRef.child(ingredientsId).setValue(ingredient);
            Log.i(TAG, "postIngredient: Ingredient posted = " + ingredient.uid);

            // Clear the field
            mIngreds_ET.setText(null);

        }
    }

    private void removeIngredient() {
        Log.e(TAG, "removeIngredient: Removing Ingredient | Recipe ID = " +ingredientsId);
        int pos = mIngredsList.size() -1;
        DatabaseReference recipeIngredientsRef = mDatabase.child(Constants.DATABASE_RECIPE_INGREDIENT).child(recipeId).child(mIngredsList.get(pos).uid);
        Log.i(TAG, "removeIngredient: Ref location = " + recipeIngredientsRef.toString());
        recipeIngredientsRef.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Toast.makeText(getContext(), "Ingredient Deleted.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postInstruction() {
        String instructionText = mSteps_ET.getText().toString().trim();
        if(TextUtils.isEmpty(instructionText)){
            Toast.makeText(mContext, "Enter an Instruction.", Toast.LENGTH_SHORT).show();
        }else{
            Log.i(TAG, "postInstruction: Posting Instruction | Instruction ID = " +instructionsId);
            instructionsId = mDatabase.child(Constants.DATABASE_RECIPE_INSTRUCTION).push().getKey();
            num = mEditStepsAdapter.getItemCount() + 1;
            final String newNum = num + ". ";
            // Create new comment object
            instruction = new Instruction(instructionsId,newNum,instructionText);

            // Push the comment, it will appear in the list
            mInstructionsDatabaseRef.child(instructionsId).setValue(instruction);
            Log.i(TAG, "postInstruction: Instruction Posted = " + instruction.uid);

            // Clear the field
            mSteps_ET.setText(null);
        }
    }

    private void removeInstruction() {
        int pos = mStepsList.size() - 1;
        Log.e(TAG, "removeInstruction: Removing Instruction | Recipe ID = " +instructionsId);
        DatabaseReference recipeInstructionsRef = mDatabase.child(Constants.DATABASE_RECIPE_INSTRUCTION).child(recipeId).child(mStepsList.get(pos).uid);
        Log.i(TAG, "removeInstruction: Ref location = " + recipeInstructionsRef.toString());
        recipeInstructionsRef.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Toast.makeText(getContext(), "Instruction Deleted.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void SelectTimeDialog() {
        Log.i(TAG, "onClick: pick time option Button Clicked");

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v1 = inflater.inflate(R.layout.fragment_recipe_add_numpicker, null);
        numberPicker1 = v1.findViewById(R.id.MyNunPicker1);
        numberPicker2 = v1.findViewById(R.id.MyNunPicker2);

        numberPicker1.setMaxValue(12);
        numberPicker1.setMinValue(0);
        numberPicker1.setValue(0);
        numberPicker1.setWrapSelectorWheel(true);
        numberPicker1.setDividerColor(getResources().getColor(R.color.colorPrimary));


        numberPicker2.setMaxValue(59);
        numberPicker2.setMinValue(0);
        numberPicker2.setValue(1);
        numberPicker2.setWrapSelectorWheel(true);
        numberPicker2.setDividerColor(getResources().getColor(R.color.colorPrimary));

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setView( v1 );
        builder.setTitle("Select Prep Time");
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                int NumVal1 = numberPicker1.getValue();
                int NumVal2 = numberPicker2.getValue();

                Log.i(TAG, "onClick: prep time = " + NumVal1 + "h" + " " + NumVal2 + "m");
                if(NumVal1==0){
                    if(NumVal2==0){
                        dialog.dismiss();
                        Toast.makeText(mContext, "Please enter prep time.", Toast.LENGTH_SHORT).show();
                    }
                    if(NumVal2>0){
                        mPrepTime.setText(new StringBuilder().append(NumVal2).append("m"));
                    }
                }
                if(NumVal1>0){
                    if(NumVal2==0){
                        mPrepTime.setText(new StringBuilder().append(NumVal1).append("h"));
                    }
                    if(NumVal2>0){
                        mPrepTime.setText(new StringBuilder().append(NumVal1).append("h").append(" ").append(NumVal2).append("m"));
                    }
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertdialog.dismiss();

            }
        });

        alertdialog = builder.create();
        alertdialog.show();
    }

    private void SelectDialog() {
        Log.i(TAG, "onClick: pick image option Button Clicked");
        if(getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
    public void onResume() {
        super.onResume();

        CheckPermissions();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Listen for comments
        mIngredientsAdapter = new EditIngredientAdapter(getContext(),mIngredientDatabaseRef);
        mIngredRV.setAdapter(mIngredientsAdapter);
        mEditStepsAdapter = new EditStepsAdapter(getContext(),mInstructionsDatabaseRef);
        mStepRV.setAdapter(mEditStepsAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Clean up comments listener
        mIngredientsAdapter.cleanupListener();
        mEditStepsAdapter.cleanupListener();
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}


