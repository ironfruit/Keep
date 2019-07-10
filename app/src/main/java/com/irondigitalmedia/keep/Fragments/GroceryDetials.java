package com.irondigitalmedia.keep.Fragments;


import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.irondigitalmedia.keep.MainActivity;
import com.irondigitalmedia.keep.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroceryDetials extends Fragment {

    private MainActivity mainActivity;
    private Toolbar toolbar;


    public GroceryDetials() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grocery_detials, container, false);


        mainActivity = (MainActivity) view.getContext();

        toolbar = mainActivity.findViewById(R.id.main_toolbar);
        mainActivity.setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        // Inflate the layout for this fragment
        return view;
    }

}
