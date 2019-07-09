package com.irondigitalmedia.keep.Fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.irondigitalmedia.keep.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditRecipeFrag extends Fragment {


    public EditRecipeFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_edit,container,false);



        return view;
    }

}
