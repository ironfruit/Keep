package com.irondigitalmedia.keep;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.fragment.app.FragmentTransaction;
import androidx.transition.Explode;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.irondigitalmedia.keep.Adapters.SearchAdapter;
import com.irondigitalmedia.keep.Authentication.Login;
import com.irondigitalmedia.keep.Fragments.AddNewRecipeFrag;
import com.irondigitalmedia.keep.Fragments.GroceryFragment;
import com.irondigitalmedia.keep.Fragments.ProfileFragment;
import com.irondigitalmedia.keep.Fragments.RandomRecipeFragment;
import com.irondigitalmedia.keep.Fragments.RecipeDetailsFragment;
import com.irondigitalmedia.keep.Fragments.SearchFragment;
import com.irondigitalmedia.keep.Model.Instruction;
import com.irondigitalmedia.keep.Utils.Constants;

public class MainActivity extends BaseActivity implements FirebaseAuth.AuthStateListener
{

    private static final String TAG = MainActivity.class.getSimpleName();

    public BottomNavigationView mMainNav;
    private FrameLayout mMainFrame;

    private SearchFragment searchFragment;
    private ProfileFragment profileFragment;
    private AddNewRecipeFrag addNewRecipeFrag;
    private GroceryFragment groceryFragment;
    private RandomRecipeFragment randomFragment;

    //Firebase
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;

    public ProgressBar progressBar;

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
        progressBar = findViewById(R.id.main_progressBar);
        mMainNav.setSelectedItemId(R.id.nav_search);

        searchFragment = new SearchFragment();
        profileFragment = new ProfileFragment();
        addNewRecipeFrag = new AddNewRecipeFrag();
        groceryFragment = new GroceryFragment();
        randomFragment = new RandomRecipeFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main_frame,searchFragment,Constants.FRAGMENT_TAG_RECIPE_SEARCH);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();

        mMainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_search:
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.main_frame,searchFragment);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft.commit();
                        Log.i(TAG, "======================================================     setFragment: fragment ID: " + searchFragment.getTag());
                        return true;

                    case R.id.nav_add_recipe:
                        FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                        ft2.replace(R.id.main_frame,addNewRecipeFrag,Constants.FRAGMENT_TAG_RECIPE_EDIT);
                        ft2.addToBackStack(null);
                        ft2.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft2.commit();
                        Log.i(TAG, "======================================================     setFragment: fragment ID: " + addNewRecipeFrag.getTag());
                        return true;
                    case R.id.nav_profile:
                        FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                        ft3.replace(R.id.main_frame,profileFragment,Constants.FRAGMENT_TAG_RECIPE_PROFILE);
                        ft3.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft3.commit();
                        Log.i(TAG, "======================================================     setFragment: fragment ID: " + profileFragment.getTag());
                        return true;
                    case R.id.nav_grocery:
                        FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                        ft4.replace(R.id.main_frame,groceryFragment,Constants.FRAGMENT_TAG_RECIPE_GROCERY);
                        ft4.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft4.commit();
                        Log.i(TAG, "======================================================     setFragment: fragment ID: " + groceryFragment.getTag());
                        return true;
                    case R.id.nav_random:
                        FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                        ft5.replace(R.id.main_frame,randomFragment,Constants.FRAGMENT_TAG_RECIPE_RANDOM);
                        ft5.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft5.commit();
                        Log.i(TAG, "======================================================     setFragment: fragment ID: " + randomFragment.getTag());
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
    protected void onResume() {
        super.onResume();
        CheckUserLoggedIn();
        Log.i(TAG, "onResume: --------------------------");
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        Log.i(TAG, "onAuthStateChanged: ----------------------------");
        CheckUserLoggedIn();

    }

}
