package com.irondigitalmedia.keep.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.irondigitalmedia.keep.Fragments.RecipeDetailsFragment;
import com.irondigitalmedia.keep.MainActivity;
import com.irondigitalmedia.keep.Model.Recipe;
import com.irondigitalmedia.keep.R;
import com.irondigitalmedia.keep.Utils.Constants;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private static final String TAG = "SearchFragment";
    private DatabaseReference mDatabase;
    private Context context;
    private List<Recipe> mRecipesList;
    private MainActivity mainActivity;
    private String mRecipeKey;

    public SearchAdapter(Context context, List<Recipe> mRecipesList) {
        this.context = context;
        this.mRecipesList = mRecipesList;
        Log.i(TAG, "SearchAdapter: List Size = " + mRecipesList.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_recipes_recipe_item, parent, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();


        return new SearchAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mRecipeKey = mRecipesList.get(position).getUid();
        holder.tv_recipe_title.setText(mRecipesList.get(position).getTitle());
        holder.tv_recipe_desc.setText(mRecipesList.get(position).getDesc());
        holder.tv_recipe_prepTime.setText(mRecipesList.get(position).getPrepTime());
        Glide.with(context).load(mRecipesList.get(position).getUrl()).centerCrop().into(holder.recipe_thumbnail);
        //holder.setLikedBtn(recipe.getUid());
        // perform Like button click.
    }

    @Override
    public int getItemCount() {
        return mRecipesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tv_recipe_title, tv_recipe_desc, tv_recipe_prepTime;
        public ImageView recipe_thumbnail;
        public FloatingActionButton like;

        public FirebaseAuth mAuth;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mainActivity = (MainActivity) itemView.getContext();
            tv_recipe_title = itemView.findViewById(R.id.recipe_item_title);
            tv_recipe_desc = itemView.findViewById(R.id.recipe_item_desc);
            tv_recipe_prepTime = itemView.findViewById(R.id.recipe_item_time);
            recipe_thumbnail = itemView.findViewById(R.id.recipe_item_photo);
            like = itemView.findViewById(R.id.recipe_item_fab_like);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RecipeDetailsFragment rd = new RecipeDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.EXTRA_RECIPE_KEY,mRecipeKey);
                    Log.i(TAG, "onClick: Fragment Interaction recipe Key is = " + mRecipeKey);
                    FragmentTransaction ft = mainActivity.getSupportFragmentManager().beginTransaction();
                    rd.setArguments(bundle);
                    ft.replace(R.id.main_frame, rd, Constants.FRAGMENT_TAG_RECIPE_DETAILS);
                    ft.addToBackStack(null);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.commit();
                }
            });
        }



    }
}
