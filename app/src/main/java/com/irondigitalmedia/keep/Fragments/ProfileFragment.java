package com.irondigitalmedia.keep.Fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.irondigitalmedia.keep.Adapters.ProfileRecipeAdapter;
import com.irondigitalmedia.keep.Authentication.Login;
import com.irondigitalmedia.keep.MainActivity;
import com.irondigitalmedia.keep.Model.Recipe;
import com.irondigitalmedia.keep.Model.User;
import com.irondigitalmedia.keep.R;
import com.irondigitalmedia.keep.Utils.Constants;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ProfileFragment";

    // Firebase Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String userUID;

    // Firebase Database Reference
    private StorageReference mStorage;
    private StorageReference mStorage2;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private DatabaseReference RecipeCountRef;
    private DatabaseReference FollowersCountRef;
    private String users = "users";

    // Views
    private Button mSettings, mEditProfile;
    private CircleImageView mProfileImage;
    public TextView mProfName, mProfAbout, mFollowers, mRecipeCount;
    private RecyclerView mProfRecycler;
    private List<Recipe> mRecipesPhotos;
    private List<String> mRecipeIds;
    private GridLayoutManager glm;
    private ProfileRecipeAdapter PRA;
    private LinearLayout userOptions,followOptions;
    private FirebaseDatabase mDatabase;
    private ToggleButton mFollowUserButton;

    // Object
    private User user;
    private Context mContext;
    private Recipe mRecipe;
    private View view;

    private MainActivity mainActivity;
    private Toolbar toolbar;

    // Boolean
    private boolean UserAvailable = false;

    public ProfileFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Create View and inflate it
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        mContext = view.getContext();

        mainActivity = (MainActivity) view.getContext();
        mainActivity.mMainNav.setSelectedItemId(R.id.nav_profile);

        toolbar = mainActivity.findViewById(R.id.main_toolbar);
        mainActivity.setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.profile);
        // inside your activity (if you did not enable transitions in your theme)


        if(CheckUserLoggedIn()){

            // User is available continue operations
            InitializeViews(view);

        } else {

            // Send user to login screen to login.
            GoToLogin();

        }

        // Inflate the layout for this fragment
        return view;
    }


    private void UpdateProfile() {
        database = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        myRef = database.getReference();
        RecipeCountRef = database.getReference().child(Constants.DATABASE_ROOT_USERS_RECIPES);
        FollowersCountRef = database.getReference().child(Constants.DATABASE_ROOT_FOLLOWERS);
        Log.i(TAG, "UpdateProfile: updating the profile views with user data...");
        if (mUser != null) {
            myRef = database.getReference().child(users).child(mUser.getUid());
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i(TAG, "onDataChange: Getting user profile info.");
                    user = dataSnapshot.getValue(User.class);
                    Glide.with(getActivity())
                            .load(user.url)
                            .centerCrop()
                            .into(mProfileImage);
                    mProfName.setText(user.name);
                    mProfAbout.setText(user.about);
                    getActivity().setTitle(user.username);


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i(TAG, "onCancelled: Value Event Listener Cancelled for profile update info.");
                }
            });
            Log.i(TAG, "UpdateProfile: getting recipe count");
            myRef.child(Constants.DATABASE_ROOT_FOLLOWERS)
                    .child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Check follower count
                    mFollowers.setText(dataSnapshot.getChildrenCount() + "");
                    Log.i(TAG, "onDataChange: Recipe Count " + dataSnapshot.getChildrenCount());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            Log.i(TAG, "UpdateProfile: getting follower count");
            myRef.child(Constants.DATABASE_ROOT_USERS_RECIPES)
                    .child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // Check Recipe Count
                    mRecipeCount.setText(dataSnapshot.getChildrenCount() + "");
                    Log.i(TAG, "onDataChange: Recipe Count " + dataSnapshot.getChildrenCount());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            myRef.keepSynced(true);

            // USER LOGGED IN
        } else {


        }

    }

    private void LoadUserRecipePhotoGrid() {
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if(mUser!=null){
            database.getReference().child(Constants.DATABASE_ROOT_USERS_RECIPES)
                    .child(mUser.getUid()).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    mRecipe = dataSnapshot.getValue(Recipe.class);
                    mRecipesPhotos.add(mRecipe);
                    PRA.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    mRecipe = dataSnapshot.getValue(Recipe.class);
                    mRecipesPhotos.add(mRecipe);
                    PRA.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    mRecipe = dataSnapshot.getValue(Recipe.class);
                    mRecipesPhotos.remove(mRecipe);
                    PRA.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            database.getReference().child(Constants.DATABASE_ROOT_USERS_RECIPES)
                    .child(mUser.getUid()).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    mRecipe = dataSnapshot.getValue(Recipe.class);
                    mRecipesPhotos.add(mRecipe);
                    PRA.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    mRecipe = dataSnapshot.getValue(Recipe.class);
                    mRecipesPhotos.add(mRecipe);
                    PRA.notifyDataSetChanged();
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    mRecipe = dataSnapshot.getValue(Recipe.class);
                    mRecipesPhotos.remove(mRecipe);
                    PRA.notifyDataSetChanged();
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            database.getReference().keepSynced(true);
        }
    }

    private void UpdateUserClickedProfile() {
        Log.i(TAG, "UpdateUserClickedProfile: Clicked User Method");
        database = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        myRef = database.getReference();
        RecipeCountRef = database.getReference().child(Constants.DATABASE_ROOT_USERS_RECIPES);
        FollowersCountRef = database.getReference().child(Constants.DATABASE_ROOT_FOLLOWERS);
        mDatabase = database;
        // USER CLICKED
        Log.i(TAG, "UpdateProfile: No User Is Available Checking for clicked user");
        myRef = database.getReference().child(users).child(userUID);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: Getting user profile info." + " " + userUID);
                user = dataSnapshot.getValue(User.class);
                Glide.with(getActivity())
                        .load(user.url)
                        .centerCrop()
                        .into(mProfileImage);
                mProfName.setText(user.name);
                mProfAbout.setText(user.about);
                getActivity().setTitle(user.username);
                mAuth = FirebaseAuth.getInstance();

                mDatabase.getReference().child(Constants.DATABASE_ROOT_FOLLOWING)
                        .child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(userUID)){
                            mFollowUserButton.setChecked(true);
                            Log.i(TAG, "onDataChange: Following User at " + mDatabase.getReference().child(Constants.DATABASE_ROOT_FOLLOWING).child(mAuth.getCurrentUser().getUid()).child(userUID));
                        }else{
                            Log.i(TAG, "onDataChange: Not following user...");
                            mFollowUserButton.setChecked(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "onCancelled: Value Event Listener Cancelled for profile update info.");
            }
        });

        Log.i(TAG, "UpdateProfile: getting recipe count");
        myRef.child(Constants.DATABASE_ROOT_FOLLOWERS)
                .child(userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check follower count
                mFollowers.setText(dataSnapshot.getChildrenCount() + "");
                Log.i(TAG, "onDataChange: Recipe Count " + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.i(TAG, "UpdateProfile: getting follower count");
        myRef.child(Constants.DATABASE_ROOT_USERS_RECIPES)
                .child(userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check Recipe Count
                mRecipeCount.setText(dataSnapshot.getChildrenCount() + "");
                Log.i(TAG, "onDataChange: Recipe Count " + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void LoadUserClickedRecipeGrid() {
        Log.i(TAG, "LoadUserClickedRecipeGrid: Clicked User Method");
        database = FirebaseDatabase.getInstance();
        database.getReference().child(Constants.DATABASE_ROOT_USERS_RECIPES)
                .child(userUID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mRecipe = dataSnapshot.getValue(Recipe.class);
                mRecipesPhotos.add(mRecipe);
                PRA.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mRecipe = dataSnapshot.getValue(Recipe.class);
                mRecipesPhotos.add(mRecipe);
                PRA.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                mRecipe = dataSnapshot.getValue(Recipe.class);
                mRecipesPhotos.remove(mRecipe);
                PRA.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        }

    private void InitializeViews(View view) {
        // Setup Views
        mFollowUserButton = view.findViewById(R.id.profile_button_follow);
        mFollowUserButton.setOnClickListener(this);
        Drawable buttonDrawable = mFollowUserButton.getBackground();
        buttonDrawable = DrawableCompat.wrap(buttonDrawable);
        DrawableCompat.setTintList(buttonDrawable,getContext().getResources().getColorStateList(R.color.follow_colors));
        mSettings = view.findViewById(R.id.profile_button_settings);
        mSettings.setOnClickListener(this);
        mEditProfile = view.findViewById(R.id.profile_button_editprofile);
        mEditProfile.setOnClickListener(this);
        mProfileImage = view.findViewById(R.id.frag_profile_image);
        mProfileImage.setOnClickListener(this);
        mProfName = view.findViewById(R.id.frag_profile_name);
        mProfAbout = view.findViewById(R.id.frag_profile_about);
        mFollowers = view.findViewById(R.id.frag_profile_recipes_followers_count);
        mRecipeCount = view.findViewById(R.id.frag_profile_recipes_count);
        mRecipesPhotos = new ArrayList<>();
        mProfRecycler = view.findViewById(R.id.profile_user_recipes_rv);
        mProfRecycler.setItemAnimator(new DefaultItemAnimator());
        glm = new GridLayoutManager(getContext(),3);
        mProfRecycler.setLayoutManager(glm);
        mProfRecycler.setItemAnimator(new DefaultItemAnimator());
        PRA = new ProfileRecipeAdapter(mContext,mRecipesPhotos);
        mProfRecycler.setAdapter(PRA);
    }

    private void GoToLogin() {
        Intent intent = new Intent(getContext(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_button_settings:
                GoToSettings();
                break;
            case R.id.profile_button_editprofile:
                GoToEditProfile();
                break;
            case R.id.profile_button_follow:
                FollowUser();
                break;
            default:
                break;
        }
    }

    private void GoToEditProfile() {
        Log.i(TAG, "---------------GOTOEDITPROFILEMETHOD--------------");
        EditProfileFragment EPF = new EditProfileFragment();
        FragmentTransaction ft = mainActivity.getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_frame,EPF, Constants.FRAGMENT_TAG_EDIT_PROFILE);
        ft.addToBackStack(Constants.FRAGMENT_TAG_EDIT_PROFILE);
        ft.setCustomAnimations(R.anim.left_to_right, R.anim.right_to_left);
        ft.commit();
    }

    public void GoToSettings(){
        Log.i(TAG, "---------------GOTOSETTINGSMETHOD--------------");
        SettingsFragment s = new SettingsFragment();
        FragmentTransaction ft = mainActivity.getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_frame,s, Constants.FRAGMENT_TAG_SETTINGS);
        ft.addToBackStack(Constants.FRAGMENT_TAG_SETTINGS);
        ft.setCustomAnimations(R.anim.left_to_right, R.anim.right_to_left);
        ft.commit();
    }

    private boolean CheckUserLoggedIn() {
        Bundle bundle = getArguments();
        if(bundle!=null){
            userUID = bundle.getString(Constants.EXTRA_USER_UID);
            Log.i(TAG, "CheckUserLoggedIn: User Id From Bundle = " + userUID);
            UserAvailable = true;
            userOptions = view.findViewById(R.id.profile_linear_user_options);
            followOptions = view.findViewById(R.id.profile_linear_follow);
            userOptions.setVisibility(View.GONE);
            followOptions.setVisibility(View.VISIBLE);
            UpdateUserClickedProfile();
            LoadUserClickedRecipeGrid();
        }else{
            Log.i(TAG, "CheckUserLoggedIn: CHECKING IF USER IS AVAILABLE");
            mAuth = FirebaseAuth.getInstance();
            mUser = mAuth.getCurrentUser();
            if(mUser != null && mAuth != null){
                Log.i(TAG, "CheckUserLoggedIn: User Available ..............." + mUser.getEmail());
                mUser = mAuth.getCurrentUser();
                UserAvailable = true;
                LoadUserRecipePhotoGrid();
                UpdateProfile();
            } else {
                UserAvailable = false;
                Log.e(TAG, "onStart: mUser is NULL sending to login...");
                Intent intent = new Intent(getContext(), Login.class);
                startActivity(intent);
            }
        }

        return UserAvailable;
    }

    private void FollowUser() {
        Log.i(TAG, "FollowUser: Following = " + mFollowUserButton.isChecked());
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if(mFollowUserButton.isChecked()){
            if(userUID!=null){
                mDatabase.getReference().child(Constants.DATABASE_ROOT_FOLLOWING).child(mAuth.getCurrentUser().getUid()).child(userUID).setValue(true);
            }else{
                Log.i(TAG, "FollowUser: Recipe Creator Id is null...");
            }
        }else {
            if(userUID!=null){
                mDatabase.getReference().child(Constants.DATABASE_ROOT_FOLLOWING).child(mAuth.getCurrentUser().getUid()).child(userUID).removeValue();
            }else{
                Log.i(TAG, "FollowUser: Recipe Creator Id is null...");
            }
        }
    }


}
