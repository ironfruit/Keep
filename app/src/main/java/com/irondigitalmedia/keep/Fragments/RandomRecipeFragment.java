package com.irondigitalmedia.keep.Fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.irondigitalmedia.keep.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RandomRecipeFragment extends Fragment {


    public RandomRecipeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_random_recipe, container, false);
    }

}
