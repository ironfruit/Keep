package com.irondigitalmedia.keep.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.irondigitalmedia.keep.Adapters.SearchAdapter;
import com.irondigitalmedia.keep.BaseActivity;
import com.irondigitalmedia.keep.MainActivity;
import com.irondigitalmedia.keep.Model.Instruction;
import com.irondigitalmedia.keep.Model.Recipe;
import com.irondigitalmedia.keep.R;
import com.irondigitalmedia.keep.Utils.Constants;

import java.util.ArrayList;
import java.util.List;


public class SearchFragment extends Fragment {

    private static final String TAG = SearchFragment.class.getSimpleName();

    private List<Recipe> mRecipeList;
    private List<String> mRecipeIds;
    private RecyclerView SearchRecyclerView;
    private SearchAdapter adapter;
    private LinearLayoutManager LLM;
    private Context mContext;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private Recipe mRecipe;
    private MainActivity mainActivity;
    private BaseActivity baseActivity;
    private String dataSnapShotKey;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,container,false);
        mContext = view.getContext();
        mRecipeList = new ArrayList<>();
        mRecipeIds = new ArrayList<>();
        SearchRecyclerView = view.findViewById(R.id.frag_search_rv);
        SearchRecyclerView.setItemAnimator(new DefaultItemAnimator());
        LLM = new LinearLayoutManager(getContext());
        SearchRecyclerView.setLayoutManager(LLM);
        SearchRecyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new SearchAdapter(mContext, mRecipeList);
        SearchRecyclerView.setAdapter(adapter);
        mainActivity = (MainActivity) view.getContext();
        mainActivity.mMainNav.setSelectedItemId(R.id.nav_search);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mRecipeList.clear();
        mRecipeIds.clear();
        myRef.child(Constants.DATABASE_ROOT_RECIPES).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                dataSnapShotKey = dataSnapshot.getKey();
                // A new comment has been added, add it to the displayed list
                mRecipe = dataSnapshot.getValue(Recipe.class);
                // Update RecyclerView
                mRecipeIds.add(dataSnapshot.getKey());
                mRecipeList.add(mRecipe);
                adapter.notifyItemInserted(mRecipeList.size() - 1);
                adapter.notifyDataSetChanged();
                SearchRecyclerView.scrollToPosition(mRecipeList.lastIndexOf(mRecipe));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Recipe newRecipe = dataSnapshot.getValue(Recipe.class);
                String instructionKey = dataSnapshot.getKey();
                // [START_EXCLUDE]
                int instructionIndex = mRecipeIds.indexOf(instructionKey);
                if (instructionIndex > -1) {
                    // Replace with the new data
                    mRecipeList.set(instructionIndex, newRecipe);

                    // Update the RecyclerView
                    adapter.notifyItemChanged(instructionIndex);
                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + instructionKey);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String recipeKey = dataSnapshot.getKey();

                // [START_EXCLUDE]
                int instructionIndex = mRecipeIds.indexOf(recipeKey);
                if (instructionIndex > -1) {
                    // Remove data from the list
                    mRecipeList.remove(instructionIndex);
                    mRecipeIds.remove(instructionIndex);

                    // Update the RecyclerView
                    adapter.notifyItemRemoved(instructionIndex);
                } else {
                    Log.w(TAG, "onChildRemoved:unknown_child:" + recipeKey);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Keep database synced.
        myRef.keepSynced(true);
        Log.i(TAG, "onCreateView: mRecipeList = " + mRecipeList.size());

        return view;
    }



    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle("Search");
    }


    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}
