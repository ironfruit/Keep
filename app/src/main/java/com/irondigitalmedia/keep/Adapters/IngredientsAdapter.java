package com.irondigitalmedia.keep.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.irondigitalmedia.keep.Model.Ingredient;
import com.irondigitalmedia.keep.R;

import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {
    private Context mContext;
    private List<Ingredient> mIngredientsList;

    public IngredientsAdapter(Context context, List<Ingredient> mIngredientsList) {
        this.mContext = context;
        this.mIngredientsList = mIngredientsList;
    }

    @NonNull
    @Override
    public IngredientsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item_recipe_ingredient, parent, false);
        return new IngredientsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientsAdapter.ViewHolder holder, int position) {
        Ingredient ingredient = mIngredientsList.get(position);
        holder.ingred.setText(ingredient.getIngredient());
    }

    @Override
    public int getItemCount() {
        return mIngredientsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView ingred;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ingred = itemView.findViewById(R.id.recipe_ingredients_tv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, mIngredientsList.get(getAdapterPosition()).ingredient, Toast.LENGTH_SHORT).show();
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(mContext, "Long Clicked " + getAdapterPosition(), Toast.LENGTH_SHORT).show();

                    return true;
                }
            });
        }


    }
}
