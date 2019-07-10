package com.irondigitalmedia.keep.Fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.irondigitalmedia.keep.Adapters.EditIngredientAdapter;
import com.irondigitalmedia.keep.Adapters.EditStepsAdapter;
import com.irondigitalmedia.keep.Adapters.IngredientsAdapter;
import com.irondigitalmedia.keep.Adapters.StepsAdapter;
import com.irondigitalmedia.keep.MainActivity;
import com.irondigitalmedia.keep.Model.Ingredient;
import com.irondigitalmedia.keep.Model.Instruction;
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
public class RecipeDetailsFragment extends Fragment implements View.OnClickListener{
    public static final String TAG = RecipeDetailsFragment.class.getSimpleName();

    public static final String EXTRA_RECIPE_KEY = "recipe_key";
    // Views
    private Bitmap bitmap;
    private ImageView mRecipePhoto;
    private CircleImageView mUserPhoto;
    private Switch mPrivacySwitch;
    private Boolean privacy;
    private RecyclerView mIngredRV,mStepRV;
    private Uri imageFileURI;

    private List<Ingredient> mIngredsList;
    private List<Instruction> mStepsList;
    private List<String> mIngredientIds;
    private List<String> mInstructionIds;
    private LinearLayoutManager mIngredManager,mStepsManager;
    private IngredientsAdapter mIngredientsAdapter;
    private StepsAdapter mStepsAdapter;
    private EditIngredientAdapter editIngredientAdapter;
    private EditStepsAdapter editStepsAdapter;


    private ShareActionProvider shareActionProvider;
    private TextView mRecipeTitle, mRecipeCreator, mRecipeDesc,mRecipePrepTime, mUsername;
    private Recipe mRecipe;
    public String mRecipeKey, mRecipeCreatorId, username;
    private Bundle info;

    private DatabaseReference mIngredientDatabaseRef;
    private DatabaseReference mInstructionsDatabaseRef;
    private DatabaseReference mRecipeRef;
    private DatabaseReference mUserRef;
    private DatabaseReference mUserRecipeRef;
    private FirebaseDatabase mDatabase;
    private Context mContext;
    private boolean followed;
    private User user;

    private Menu menu;

    private FirebaseAuth mAuth;

    // Views
    protected int num;

    private MainActivity mainActivity;
    private Toolbar toolbar;


    public RecipeDetailsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_details, container, false);

        mainActivity = (MainActivity) view.getContext();

        toolbar = mainActivity.findViewById(R.id.main_toolbar);
        mainActivity.setSupportActionBar(toolbar);

        setHasOptionsMenu(true);
        InitiateViewsAndAdapters(view);
        RecieveRecipeKey();
        UpdateDetails();


        return view;
    }

    public String RecieveRecipeKey(){
        Log.i(TAG, "RecieveRecipeKey: =======================================RECEIVING RECIPE KEY BEGIN================================================== ");
        Bundle bundle = getArguments();
        if(bundle!=null){
            String key = bundle.getString(Constants.EXTRA_RECIPE_KEY);
            if(key!=null){
                mRecipeKey = key;
                Log.i(TAG, "RecieveRecipeKey: Recipe Key = " + mRecipeKey);
            }
        }
        Log.i(TAG, "RecieveRecipeKey: =======================================RECEIVING RECIPE KEY END================================================== ");
        return mRecipeKey;
    }

    private void UpdateDetails() {
        mRecipeRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = FirebaseDatabase.getInstance().getReference();
        mIngredientDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mInstructionsDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance();

        // Retrieve the recipe details (not the ingredients or instructions just yet)
        mRecipeRef.child(Constants.DATABASE_ROOT_RECIPES).child(mRecipeKey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mRecipe = dataSnapshot.getValue(Recipe.class);
                        if(mRecipe!=null){
                            getActivity().setTitle(mRecipe.title);
                            mRecipeDesc.setText(mRecipe.desc);
                            mRecipePrepTime.setText(mRecipe.prepTime);
                            Glide.with(getContext()).load(mRecipe.url).into(mRecipePhoto);
                            mRecipeCreatorId = mRecipe.creatorId;
                            if(mRecipeCreatorId.equalsIgnoreCase(getUid())){
                                if(menu!=null){
                                    menu.findItem(R.id.delete).setVisible(true);
                                }
                            }else{
                                Log.i(TAG, "onDataChange: User did not create this recipe.");
                            }
                            Log.i(TAG, "onDataChange: Recipe Creator Id = " + mRecipeCreatorId);
                            Log.i(TAG, "UpdateDetails: Creator Id = " + mRecipeCreatorId);

                            mUserRef.child(Constants.DATABASE_ROOT_USERS).child(mRecipeCreatorId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Log.i(TAG, "onDataChange: getting user info for recipe");

                                    user = dataSnapshot.getValue(User.class);
                                    if(user!=null){
                                        Glide.with(getContext()).load(user.getUrl()).centerCrop().into(mUserPhoto);
                                        Log.i(TAG, "onDataChange: user photo url = " + user.getUrl());
                                        mUsername.setText(user.username);
                                        Log.i(TAG, "onDataChange: user - username = " + user.getUsername());
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e(TAG, "onCancelled: Database Error: " + databaseError.getMessage());
                                }
                            });
                        }else{
                            Log.e(TAG, "onDataChange: recipe is null.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled: Recipe Ref Database Error");
                    }
                });

        // Retrieve the ingredients and add them to the list
        mIngredientDatabaseRef.child(Constants.DATABASE_RECIPE_INGREDIENT).child(mRecipeKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i(TAG, "onChildAdded: Adding ingredient " + dataSnapshot.getKey());
                Ingredient ingredient = dataSnapshot.getValue(Ingredient.class);
                Log.i(TAG, "onDataChange: Ingredient " + ingredient.ingredient);
                mIngredsList.add(ingredient);
                mIngredRV.scrollToPosition(mIngredientsAdapter.getItemCount()-1);
                mIngredientsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i(TAG, "onChildAdded: Adding ingredient " + dataSnapshot.getKey());
                Ingredient ingredient = dataSnapshot.getValue(Ingredient.class);
                Log.i(TAG, "onDataChange: Ingredient " + ingredient.ingredient);
                mIngredsList.add(ingredient);
                mIngredRV.scrollToPosition(mIngredientsAdapter.getItemCount()-1);
                mIngredientsAdapter.notifyDataSetChanged();
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


        // Retrieve the instructions and add them to the list
        mInstructionsDatabaseRef.child(Constants.DATABASE_RECIPE_INSTRUCTION).child(mRecipeKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i(TAG, "onChildAdded: Adding instruction " + dataSnapshot.getKey());
                Instruction instruction = dataSnapshot.getValue(Instruction.class);
                Log.i(TAG, "onDataChange: Instruction " + instruction.step);
                mStepsList.add(instruction);
                mStepRV.scrollToPosition(mStepsAdapter.getItemCount()-1);
                mStepsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i(TAG, "onChildAdded: Adding instruction " + dataSnapshot.getKey());
                Instruction instruction = dataSnapshot.getValue(Instruction.class);
                Log.i(TAG, "onDataChange: Instruction " + instruction.step);
                mStepsList.add(instruction);
                mStepRV.scrollToPosition(mStepsAdapter.getItemCount()-1);
                mStepsAdapter.notifyDataSetChanged();
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

    }

    private void InitiateViewsAndAdapters(View view) {

        // Initiate Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        // Initiate Views
        mRecipePhoto = view.findViewById(R.id.recipeDetails_iv_photo);
        mRecipeDesc = view.findViewById(R.id.recipe_details_desc_tv);
        mRecipePrepTime = view.findViewById(R.id.recipe_details_preptime_tv);
        mUserPhoto = view.findViewById(R.id.recipe_details_user_photo);
        mUserPhoto.setOnClickListener(this);
        mUsername = view.findViewById(R.id.recipe_details_user_username);

        // Recyclerviews
        mIngredRV = view.findViewById(R.id.recipe_details_ingreds_rv);
        mStepRV = view.findViewById(R.id.recipe_details_steps_rv);

        // Setting Recyclerview
        mIngredsList = new ArrayList<>();
        mIngredRV.setItemAnimator(new DefaultItemAnimator());
        mIngredManager = new LinearLayoutManager(getContext());
        mIngredRV.setLayoutManager(mIngredManager);
        mIngredRV.setItemAnimator(new DefaultItemAnimator());
        mIngredientsAdapter = new IngredientsAdapter(mContext,mIngredsList);
        mIngredRV.setAdapter(mIngredientsAdapter);


        // Setting Recyclerview
        mStepsList = new ArrayList<>();
        mStepRV.setItemAnimator(new DefaultItemAnimator());
        mStepsManager = new LinearLayoutManager(getContext());
        mStepRV.setLayoutManager(mStepsManager);
        mStepRV.setItemAnimator(new DefaultItemAnimator());
        mStepsAdapter = new StepsAdapter(mContext,mStepsList);
        mStepRV.setAdapter(mStepsAdapter);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inlate menu resource file
        this.menu = menu;
        getActivity().getMenuInflater().inflate(R.menu.action_menu,menu);

    }

    public String ShareBodyText(){
        String ingredients = "";
        String steps = "";
        String shareBodyText ="";

        for(int i = 0; i < mIngredsList.size(); i++){
            ingredients += mIngredsList.get(i).getIngredient() + "\n";
        }

        for(int i = 0; i < mStepsList.size(); i++){
            steps += mStepsList.get(i).getNum() + " " +mStepsList.get(i).getStep() + "\n";
        }

        shareBodyText = "Creator: " + user.getUsername() + "\n" + "\n"
                +"Title: " +mRecipe.getTitle() + "\n" + "\n"
                +"Description: " + "\n" + "\n" +mRecipe.getDesc() + "\n" + "\n"
                +"Prep Time: " +mRecipe.getPrepTime() + "\n" + "\n"
                +"Ingredients: " + "\n" + "\n" +ingredients
                + "\n"
                +"Steps: "  + "\n" + "\n" +steps;

        return shareBodyText;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");

                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Check out my recipe " + mRecipe.getTitle());
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, ShareBodyText());
                startActivity(Intent.createChooser(sharingIntent, "Shearing Option"));
                return true;
            case R.id.delete:
                DeleteRecipe();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void DeleteRecipe() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Recipe")
                .setMessage("Are you sure you want to delete this recipe?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        mDatabase = FirebaseDatabase.getInstance();
                        Log.i(TAG, "onClick: Log the Recipe Key = " + mRecipeKey);
                        DatabaseReference rootRecipeRef = mDatabase.getReference().child(Constants.DATABASE_ROOT_RECIPES).child(mRecipeKey);
                        DatabaseReference userRecipeRef = mDatabase.getReference().child(Constants.DATABASE_ROOT_USERS_RECIPES).child(mAuth.getCurrentUser().getUid()).child(mRecipeKey);
                        DatabaseReference recipeIngredientsRef = mDatabase.getReference().child(Constants.DATABASE_RECIPE_INGREDIENT).child(mRecipeKey);
                        DatabaseReference recipeInstructionsRef = mDatabase.getReference().child(Constants.DATABASE_RECIPE_INSTRUCTION).child(mRecipeKey);
                        Log.i(TAG, "onClick: User Id = " + mAuth.getCurrentUser().getUid());
                        Log.i(TAG, "onClick: Recipe CreatorId = " + mRecipe.getCreatorId());

                        removeRecipe(rootRecipeRef);
                        removeUserRecipe(userRecipeRef);
                        removeRecipeIngredients(recipeIngredientsRef);
                        removeRecipeInstructions(recipeInstructionsRef);
                        removeStoredPhoto();
                        GotoSearch();
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton("cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void GotoSearch() {
        SearchFragment search = new SearchFragment();
        Log.i(TAG, "onClick: Fragment UserId that was clicked = " + mRecipeCreatorId);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_frame,search,Constants.FRAGMENT_TAG_RECIPE_SEARCH);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: STARTED");
        Log.i(TAG, "onStart: Recipe ID = " + mRecipeKey);
        mIngredientsAdapter = new IngredientsAdapter(getContext(),mIngredsList);
        mIngredRV.setAdapter(mIngredientsAdapter);
        mStepsAdapter = new StepsAdapter(getContext(),mStepsList);
        mStepRV.setAdapter(mStepsAdapter);


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.recipe_details_user_photo:
                GoToUserProfile();
                break;
            default:
                break;
        }
    }

    private void GoToUserProfile() {
        ProfileFragment pf = new ProfileFragment();
        Log.i(TAG, "onClick: Fragment UserId that was clicked = " + mRecipeCreatorId);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.EXTRA_USER_UID,mRecipeCreatorId);
        pf.setArguments(bundle);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_frame,pf,Constants.FRAGMENT_TAG_RECIPE_PROFILE);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }


    private void removeRecipeInstructions(DatabaseReference recipeInstructionsRef) {
        recipeInstructionsRef.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Log.i(TAG, "onComplete: Recipe Instructions deleted at @...       " + "recipe-instructions/" + mRecipeKey);
                Toast.makeText(getContext(), "Recipe Deleted.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeRecipeIngredients(DatabaseReference recipeIngredientsRef) {
        recipeIngredientsRef.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Log.i(TAG, "onComplete: Recipe Ingredients deleted at " + "recipe-ingredients/" + mRecipeKey);
            }
        });
    }

    private void removeUserRecipe(DatabaseReference userRecipeRef) {
        userRecipeRef.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Log.i(TAG, "onComplete: User Recipe deleted at " + "user-recipes/" + getUid() + "/" + mRecipeKey);
            }
        });
    }

    private void removeRecipe(DatabaseReference rootRecipeRef) {
        rootRecipeRef.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Log.i(TAG, "onComplete: Recipe deleted at " + "recipes/" + mRecipeKey);
            }
        });

    }

    private void removeStoredPhoto() {
        String cloudstoragepath = "users" + "/" + getUid() + "/" + "recipes" + "/" + mRecipeKey + "/" + mRecipeKey;
        Log.i(TAG, "removeStoredPhoto: cloudstoragepath = " + cloudstoragepath);
        StorageReference one = FirebaseStorage.getInstance().getReference();
        one.child(cloudstoragepath).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "onSuccess: Finally success!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: " + e.getMessage());
            }
        });


    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
