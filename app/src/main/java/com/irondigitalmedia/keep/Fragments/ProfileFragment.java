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
import com.irondigitalmedia.keep.Activities.MainActivity;
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

    private RecipeDetailsFragment.DataListener listener;


    private static final String TAG = "ProfileFragment";

    // Firebase Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String otherUserId;

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

    // Main Activity
    private MainActivity mainActivity;

    // Main Toolbar
    private Toolbar toolbar;

    // boolean
    public boolean otherUserAvailable;

    public ProfileFragment() {}

    public void setDataListener(String userId){

        mAuth = FirebaseAuth.getInstance();

        otherUserId = userId;
        Log.i(TAG, "setDataListener: " + otherUserId);
        if(otherUserId!=null){
            otherUserAvailable = true;
        }else{
            otherUserAvailable = false;
        }
    }

    public String getUserId(){return otherUserId;}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.e(TAG, "onCreateView: CREATE VIEW");

        // Create View and inflate it
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        mContext = view.getContext();
        mainActivity = (MainActivity) view.getContext();
        toolbar = mainActivity.findViewById(R.id.main_toolbar);
        mainActivity.setSupportActionBar(toolbar);
        mainActivity.mMainNav.setSelectedItemId(R.id.nav_profile);
        toolbar.setTitle(R.string.profile);

        // User is available continue operations
        InitializeViews(view);

        Log.e(TAG, "onCreateView: OTHER USER ID = " + getUserId());
        Log.e(TAG, "onCreateView: LOGGED IN USER ID = " + getUid());

        if(otherUserAvailable){
            if(getUserId().equals(getUid())){
                LoadLoggedInUser();
            }else{
                LoadOtherUser();
            }
        }else{
            LoadLoggedInUser();
        }

        // Inflate the layout for this fragment
        return view;
    }

    private void LoadOtherUser() {
        userOptions.setVisibility(View.GONE);
        followOptions.setVisibility(View.VISIBLE);
        UpdateUserClickedProfile();
        LoadUserClickedRecipeGrid();
    }

    private void LoadLoggedInUser() {
        Log.d(TAG, "CheckUserLoggedIn: CHECKING IF USER IS AVAILABLE");
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if(mUser != null && mAuth != null){
            Log.i(TAG, "CheckUserLoggedIn: User Available ..............." + mUser.getEmail());
            mUser = mAuth.getCurrentUser();
            LoadUserRecipePhotoGrid();
            UpdateProfile();
        } else {
            Log.e(TAG, "onStart: mUser is NULL sending to login...");
            Intent intent = new Intent(getContext(), Login.class);
            startActivity(intent);
        }
    }

    private void UpdateUserClickedProfile() {
        Log.i(TAG, "UpdateUserClickedProfile: Clicked User Method for " + otherUserId);
        database = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        myRef = database.getReference();
        RecipeCountRef = database.getReference().child(Constants.DATABASE_ROOT_USERS_RECIPES);
        FollowersCountRef = database.getReference().child(Constants.DATABASE_ROOT_FOLLOWERS);
        mDatabase = database;
        // USER CLICKED
        Log.i(TAG, "UpdateProfile: No User Is Available Checking for clicked user");
        myRef.child(Constants.DATABASE_ROOT_USERS).child(otherUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: Getting user profile info." + " " + otherUserId);
                user = dataSnapshot.getValue(User.class);
                Log.e(TAG, "onDataChange: User: " + user.getUsername());
                Glide.with(getActivity())
                        .load(user.url)
                        .centerCrop()
                        .into(mProfileImage);
                mProfName.setText(user.getName());
                mProfAbout.setText(user.getAbout());
                getActivity().setTitle(user.getUsername());
                mAuth = FirebaseAuth.getInstance();

                mDatabase.getReference().child(Constants.DATABASE_ROOT_FOLLOWING)
                        .child(getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(otherUserId)){
                            mFollowUserButton.setChecked(true);
                            Log.i(TAG, "onDataChange: Following User...");
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
        myRef.child(Constants.DATABASE_ROOT_USERS_RECIPES).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                for (DataSnapshot snap: dataSnapshot.getChildren()) {
                    Log.e(snap.getKey(),snap.getChildrenCount() + " - Follower");
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                for (DataSnapshot snap: dataSnapshot.getChildren()) {
                    Log.e(snap.getKey(),snap.getChildrenCount() + " - Follower");
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snap: dataSnapshot.getChildren()) {
                    Log.e(snap.getKey(),snap.getChildrenCount() + " - Follower");
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.i(TAG, "UpdateProfile: getting follower count");
        myRef.child(Constants.DATABASE_ROOT_FOLLOWERS).child(otherUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                for (DataSnapshot snap: dataSnapshot.getChildren()) {
                    Log.e(snap.getKey(),snap.getChildrenCount() + " - Follower");
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                for (DataSnapshot snap: dataSnapshot.getChildren()) {
                    Log.e(snap.getKey(),snap.getChildrenCount() + " - Follower");
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snap: dataSnapshot.getChildren()) {
                    Log.e(snap.getKey(),snap.getChildrenCount() + " - Follower");
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void LoadUserClickedRecipeGrid() {
        Log.i(TAG, "LoadUserClickedRecipeGrid: Clicked User Method for " + otherUserId);
        database = FirebaseDatabase.getInstance();
        database.getReference().child(Constants.DATABASE_ROOT_USERS_RECIPES)
                .child(otherUserId).addChildEventListener(new ChildEventListener() {
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

    private void UpdateProfile() {
        database = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference();
        myRef = database.getReference();
        RecipeCountRef = database.getReference().child(Constants.DATABASE_ROOT_USERS_RECIPES);
        FollowersCountRef = database.getReference().child(Constants.DATABASE_ROOT_FOLLOWERS);
        Log.i(TAG, "UpdateProfile: updating the profile views with user data...");
        if (mUser != null) {
            myRef.child(Constants.DATABASE_ROOT_USERS).child(getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i(TAG, "onDataChange: Getting user profile info.");
                    user = dataSnapshot.getValue(User.class);
                    Glide.with(getActivity())
                            .load(user.getUrl())
                            .centerCrop()
                            .into(mProfileImage);
                    mProfName.setText(user.getName());
                    mProfAbout.setText(user.getAbout());
                    getActivity().setTitle(user.getUsername());


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i(TAG, "onCancelled: Value Event Listener Cancelled for profile update info.");
                }
            });
            Log.i(TAG, "UpdateProfile: getting recipe count");
            myRef.child(Constants.DATABASE_ROOT_FOLLOWERS).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if(dataSnapshot.hasChild(getUid())){
                        Log.e(TAG, "onChildAdded: " + dataSnapshot.child(getUid()).getChildrenCount());
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
            Log.i(TAG, "UpdateProfile: getting follower count");
            myRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
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
            myRef.keepSynced(true);

            // USER LOGGED IN
        }
    }

    private void LoadUserRecipePhotoGrid() {
        database = FirebaseDatabase.getInstance();
        database.getReference().child(Constants.DATABASE_ROOT_USERS_RECIPES)
                .child(getUid()).addChildEventListener(new ChildEventListener() {
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
        userOptions = view.findViewById(R.id.profile_linear_user_options);
        followOptions = view.findViewById(R.id.profile_linear_follow);
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

    private void FollowUser() {
        Log.i(TAG, "FollowUser: Following = " + mFollowUserButton.isChecked());
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if(mFollowUserButton.isChecked()){
            if(otherUserId !=null){
                mDatabase.getReference().child(Constants.DATABASE_ROOT_FOLLOWING).child(getUid()).child(otherUserId).setValue(true);
                mDatabase.getReference().child(Constants.DATABASE_ROOT_FOLLOWERS).child(otherUserId).child(getUid()).setValue(true);
            }else{
                Log.i(TAG, "FollowUser: Recipe Creator Id is null...");
            }
        }else {
            if(otherUserId !=null){
                mDatabase.getReference().child(Constants.DATABASE_ROOT_FOLLOWERS).child(otherUserId).child(getUid()).removeValue();
                mDatabase.getReference().child(Constants.DATABASE_ROOT_FOLLOWING).child(getUid()).child(otherUserId).removeValue();
            }else{
                Log.i(TAG, "FollowUser: Recipe Creator Id is null...");
            }
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

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        setDataListener(null);
        Log.e(TAG, "onDetach: DETACHING");
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


}
