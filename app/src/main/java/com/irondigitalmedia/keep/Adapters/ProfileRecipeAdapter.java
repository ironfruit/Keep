package com.irondigitalmedia.keep.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.irondigitalmedia.keep.Fragments.RecipeDetailsFragment;
import com.irondigitalmedia.keep.MainActivity;
import com.irondigitalmedia.keep.Model.Recipe;
import com.irondigitalmedia.keep.R;
import com.irondigitalmedia.keep.Utils.Constants;

import java.util.List;

public class ProfileRecipeAdapter extends RecyclerView.Adapter<ProfileRecipeAdapter.ViewHolder> {
    private static final String TAG = ProfileRecipeAdapter.class.getSimpleName();

    private Context context;
    private List<Recipe> mRecipePhotos;
    private String mRecipeKey;
    private MainActivity mainActivity;

    public ProfileRecipeAdapter(Context context, List<Recipe> mRecipePhotos) {
        this.context = context;
        this.mRecipePhotos = mRecipePhotos;
    }

    @NonNull
    @Override
    public ProfileRecipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_recipe_user_profile_item, parent, false);

        return new ProfileRecipeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        mRecipeKey = mRecipePhotos.get(position).getUid();
        Glide.with(context).load(mRecipePhotos.get(position).getUrl()).centerCrop().into(holder.userRecipeGridItem);

    }

    @Override
    public int getItemCount() {
        return mRecipePhotos.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView userRecipeGridItem;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            mainActivity = (MainActivity) itemView.getContext();
            userRecipeGridItem = itemView.findViewById(R.id.profile_rv_item_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: ITEM HAS BEEN CLICKED " + getAdapterPosition());
                    RecipeDetailsFragment rd = new RecipeDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.EXTRA_RECIPE_KEY,mRecipeKey);
                    rd.setArguments(bundle);
                    Log.i(TAG, "onClick: Fragment Interaction recipe Key is = " + mRecipePhotos.get(getAdapterPosition()).uid);
                    FragmentTransaction ft = mainActivity.getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.main_frame,rd, Constants.FRAGMENT_TAG_RECIPE_DETAILS);
                    ft.addToBackStack(null);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.commit();
                }
            });
        }
    }
}
