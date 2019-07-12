package com.irondigitalmedia.keep.Activities;

import android.content.Intent;
import androidx.annotation.NonNull;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.irondigitalmedia.keep.Authentication.Login;
import com.irondigitalmedia.keep.Fragments.EditRecipeFragment;
import com.irondigitalmedia.keep.Fragments.GroceryFragment;
import com.irondigitalmedia.keep.Fragments.HomeFragment;
import com.irondigitalmedia.keep.Fragments.ProfileFragment;
import com.irondigitalmedia.keep.Fragments.RandomRecipeFragment;
import com.irondigitalmedia.keep.Fragments.RecipeDetailsFragment;
import com.irondigitalmedia.keep.R;
import com.irondigitalmedia.keep.Utils.Constants;

public class MainActivity extends BaseActivity implements FirebaseAuth.AuthStateListener, RecipeDetailsFragment.DataListener {


    private static final String TAG = MainActivity.class.getSimpleName();

    public BottomNavigationView mMainNav;
    private FrameLayout mMainFrame;

    private HomeFragment homeFragment;
    private ProfileFragment profileFragment;
    private EditRecipeFragment editRecipeFragment;
    private GroceryFragment groceryFragment;
    private RandomRecipeFragment randomFragment;

    //Firebase
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;

    // String
    private String otherUserId;


    // Bundle
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckUserLoggedIn();

        // Firebase Instances
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if(mUser==null){
            GoToLogin();
        }

        mMainFrame = findViewById(R.id.main_frame);
        mMainNav = findViewById(R.id.main_nav);
        mMainNav.setSelectedItemId(R.id.nav_home);

        homeFragment = new HomeFragment();
        profileFragment = new ProfileFragment();
        editRecipeFragment = new EditRecipeFragment();
        groceryFragment = new GroceryFragment();
        randomFragment = new RandomRecipeFragment();
        bundle = new Bundle();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_frame,homeFragment,Constants.FRAGMENT_TAG_HOME);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();

        mMainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.main_frame,homeFragment);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft.commit();
                        return true;

                    case R.id.nav_edit_recipe:
                        FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                        ft2.replace(R.id.main_frame, editRecipeFragment,Constants.FRAGMENT_TAG_RECIPE_EDIT);
                        ft2.addToBackStack(null);
                        ft2.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft2.commit();
                        return true;
                    case R.id.nav_profile:
                        FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                        ft3.replace(R.id.main_frame,profileFragment,Constants.FRAGMENT_TAG_PROFILE);
                        ft3.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft3.commit();
                        return true;
                    case R.id.nav_grocery:
                        FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                        ft4.replace(R.id.main_frame,groceryFragment,Constants.FRAGMENT_TAG_GROCERYITEMS);
                        ft4.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft4.commit();
                        return true;
                    case R.id.nav_random:
                        FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                        ft5.replace(R.id.main_frame,randomFragment,Constants.FRAGMENT_TAG_RECIPE_RANDOM);
                        ft5.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft5.commit();
                        return true;
                    default:
                        return false;

                }
            }
        });
    }


    private void CheckUserLoggedIn() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if(mUser != null && mAuth != null){
            Log.i(TAG, "CheckUserLoggedIn: User Available ..............." + mUser.getEmail());
            mUser = mAuth.getCurrentUser();

        } else {

            Log.e(TAG, "onStart: mUser is NULL sending to login...");
            GoToLogin();
        }
    }

    private void GoToLogin() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Firebase Instances
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            GoToLogin();
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        Log.i(TAG, "onAuthStateChanged: ----------------------------");
        CheckUserLoggedIn();

    }


    @Override
    public void DataListener(String userId) {
        Log.e(TAG, "DataListener: otherUserId " + otherUserId);
        profileFragment.setDataListener(userId);
    }
}
