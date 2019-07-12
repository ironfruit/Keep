package com.irondigitalmedia.keep.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.irondigitalmedia.keep.Fragments.RecipeDetailsFragment;
import com.irondigitalmedia.keep.Activities.MainActivity;
import com.irondigitalmedia.keep.Model.Recipe;
import com.irondigitalmedia.keep.R;
import com.irondigitalmedia.keep.Utils.Constants;

import java.util.List;

public class HomeListAdapter extends RecyclerView.Adapter<HomeListAdapter.ViewHolder> {
    private static final String TAG = "HomeFragment";
    private DatabaseReference mDatabase;
    private Context context;
    private List<Recipe> mRecipesList;
    private MainActivity mainActivity;
    private String mRecipeKey;

    public HomeListAdapter(Context context, List<Recipe> mRecipesList) {
        this.context = context;
        this.mRecipesList = mRecipesList;
        Log.i(TAG, "HomeListAdapter: List Size = " + mRecipesList.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_recipes_recipe_item, parent, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();


        return new HomeListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
        public ImageView recipe_thumbnail, like;

        public FirebaseAuth mAuth;
        public FirebaseDatabase mDatabase;

        public boolean isliked = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mainActivity = (MainActivity) itemView.getContext();
            tv_recipe_title = itemView.findViewById(R.id.recipe_item_title);
            tv_recipe_desc = itemView.findViewById(R.id.recipe_item_desc);
            tv_recipe_prepTime = itemView.findViewById(R.id.recipe_item_time);
            recipe_thumbnail = itemView.findViewById(R.id.recipe_item_photo);
            like = itemView.findViewById(R.id.recipe_item_image_like);
            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "onClick: CLICKED!");
                    mDatabase = FirebaseDatabase.getInstance();

                    mDatabase.getReference().child(Constants.DATABASE_ROOT_RECIPES).child(getUid())
                            .child(mRecipesList.get(getAdapterPosition()).getUid())
                            .child("liked").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(getUid())){
                                like.setImageDrawable(context.getApplicationContext().getResources().getDrawable(R.drawable.ic_favorite));
                                Log.i(TAG, "onDataChange: Following User...");
                            }else{
                                Log.i(TAG, "onDataChange: Not following user...");
                                like.setImageDrawable(context.getApplicationContext().getResources().getDrawable(R.drawable.ic_favorite_border));
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    mAuth = FirebaseAuth.getInstance();
                    if(isliked){
                        if(getUid() !=null){
                            mDatabase.getReference().child(Constants.DATABASE_ROOT_RECIPES).child(getUid()).child(mRecipesList.get(getAdapterPosition()).getUid()).child("liked").child(getUid()).setValue(true);
                            mDatabase.getReference().child(Constants.DATABASE_ROOT_USERS_RECIPES).child(getUid()).child(mRecipesList.get(getAdapterPosition()).getUid()).child("liked").child(getUid()).setValue(true);
                            like.setImageDrawable(context.getApplicationContext().getResources().getDrawable(R.drawable.ic_favorite));
                        }else{
                            Log.i(TAG, "FollowUser: Recipe Creator Id is null...");
                        }
                    }else {
                        if(getUid() !=null){
                            mDatabase.getReference().child(Constants.DATABASE_ROOT_RECIPES).child(getUid()).child(mRecipesList.get(getAdapterPosition()).getUid()).child("liked").child(getUid()).removeValue();
                            mDatabase.getReference().child(Constants.DATABASE_ROOT_USERS_RECIPES).child(getUid()).child(mRecipesList.get(getAdapterPosition()).getUid()).child("liked").child(getUid()).removeValue();
                            like.setImageDrawable(context.getApplicationContext().getResources().getDrawable(R.drawable.ic_favorite_border));
                        }else{
                            Log.i(TAG, "FollowUser: Recipe Creator Id is null...");
                        }
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RecipeDetailsFragment rd = new RecipeDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.EXTRA_RECIPE_KEY,mRecipesList.get(getAdapterPosition()).getUid());
                    Log.i(TAG, "onClick: Fragment Interaction recipe Key is = " + mRecipesList.get(getAdapterPosition()).getUid());
                    FragmentTransaction ft = mainActivity.getSupportFragmentManager().beginTransaction();
                    rd.setArguments(bundle);
                    ft.replace(R.id.main_frame, rd, Constants.FRAGMENT_TAG_RECIPE_DETAILS);
                    ft.addToBackStack(null);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.commit();
                }
            });
        }

        public String getUid() {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }
}
