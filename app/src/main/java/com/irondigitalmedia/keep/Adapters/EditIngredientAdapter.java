package com.irondigitalmedia.keep.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.irondigitalmedia.keep.Model.Ingredient;
import com.irondigitalmedia.keep.R;

import java.util.ArrayList;
import java.util.List;


public class EditIngredientAdapter extends RecyclerView.Adapter<EditIngredientAdapter.IngredientViewHolder> {

    private static final String TAG = EditIngredientAdapter.class.getSimpleName();

    private String dataSnapShotKey;
    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    public List<String> mIngredientIds = new ArrayList<>();
    public List<Ingredient> mIngredients = new ArrayList<>();

    public EditIngredientAdapter(final Context mContext, DatabaseReference ref) {
        this.mContext = mContext;
        this.mDatabaseReference = ref;


        // Create child event listener
        // [START child_event_listener_recycler]
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                dataSnapShotKey = dataSnapshot.getKey();
                // A new comment has been added, add it to the displayed list
                Ingredient ingredient = dataSnapshot.getValue(Ingredient.class);

                // [START_EXCLUDE]
                // Update RecyclerView
                mIngredientIds.add(dataSnapshot.getKey());
                mIngredients.add(ingredient);
                notifyItemInserted(mIngredients.size() - 1);
                // [END_EXCLUDE]
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                Ingredient newIngredient = dataSnapshot.getValue(Ingredient.class);
                String ingredientKey = dataSnapshot.getKey();

                // [START_EXCLUDE]
                int ingredientIndex = mIngredientIds.indexOf(ingredientKey);
                if (ingredientIndex > -1) {
                    // Replace with the new data
                    mIngredients.set(ingredientIndex, newIngredient);

                    // Update the RecyclerView
                    notifyItemChanged(ingredientIndex);
                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + ingredientKey);
                }
                // [END_EXCLUDE]
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String ingredientKey = dataSnapshot.getKey();

                // [START_EXCLUDE]
                int ingredientIndex = mIngredientIds.indexOf(ingredientKey);
                if (ingredientIndex > -1) {
                    // Remove data from the list
                    mIngredientIds.remove(ingredientIndex);
                    mIngredients.remove(ingredientIndex);

                    // Update the RecyclerView
                    notifyItemRemoved(ingredientIndex);
                } else {
                    Log.w(TAG, "onChildRemoved:unknown_child:" + ingredientKey);
                }
                // [END_EXCLUDE]
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
                Ingredient movedIngredient = dataSnapshot.getValue(Ingredient.class);
                String ingredientKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(mContext, "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        };


        ref.addChildEventListener(childEventListener);
        // [END child_event_listener_recycler]

        // Store reference to listener so it can be removed on app stop
        mChildEventListener = childEventListener;

    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_item_recipe_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = mIngredients.get(position);
        holder.ingred.setText(ingredient.ingredient);
    }

    @Override
    public int getItemCount() {
        return mIngredients.size();
    }

    public class IngredientViewHolder extends RecyclerView.ViewHolder {

        public TextView ingred;

        public IngredientViewHolder(View itemView) {
            super(itemView);

            ingred = itemView.findViewById(R.id.recipe_ingredients_tv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "Ingredient: " + mIngredients.get(getAdapterPosition()).ingredient, Toast.LENGTH_SHORT).show();
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

    public void RemoveIngredient(DatabaseReference reference){
        reference.removeValue();
    }

    public void cleanupListener() {
        if (mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
        }
    }


}


