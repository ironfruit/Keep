package com.irondigitalmedia.keep.Fragments;


import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.irondigitalmedia.keep.Activities.MainActivity;
import com.irondigitalmedia.keep.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RandomRecipeFragment extends Fragment {

    private MainActivity mainActivity;
    private Toolbar toolbar;


    public RandomRecipeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_random_recipe, container, false);

        mainActivity = (MainActivity) view.getContext();
        mainActivity.mMainNav.setSelectedItemId(R.id.nav_random);

        toolbar = mainActivity.findViewById(R.id.main_toolbar);
        mainActivity.setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        return view;
    }

}
