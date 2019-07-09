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
import com.irondigitalmedia.keep.Model.Instruction;
import com.irondigitalmedia.keep.R;

import java.util.ArrayList;
import java.util.List;

public class EditStepsAdapter extends RecyclerView.Adapter<EditStepsAdapter.MyViewHolder> {

    private static final String TAG = EditIngredientAdapter.class.getSimpleName();

    private String dataSnapShotKey;
    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    public List<String> mInstructionIds = new ArrayList<>();
    public List<Instruction> mInstructions = new ArrayList<>();

    public EditStepsAdapter(final Context mContext, DatabaseReference ref) {
        this.mContext = mContext;
        mDatabaseReference = ref;

        // Create child event listener
        // [START child_event_listener_recycler]
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                dataSnapShotKey = dataSnapshot.getKey();
                // A new comment has been added, add it to the displayed list
                Instruction instruction = dataSnapshot.getValue(Instruction.class);

                // [START_EXCLUDE]
                // Update RecyclerView
                mInstructionIds.add(dataSnapshot.getKey());
                mInstructions.add(instruction);
                notifyItemInserted(mInstructions.size() - 1);
                // [END_EXCLUDE]
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                Instruction newInstruction = dataSnapshot.getValue(Instruction.class);
                String instructionKey = dataSnapshot.getKey();

                // [START_EXCLUDE]
                int instructionIndex = mInstructionIds.indexOf(instructionKey);
                if (instructionIndex > -1) {
                    // Replace with the new data
                    mInstructions.set(instructionIndex, newInstruction);

                    // Update the RecyclerView
                    notifyItemChanged(instructionIndex);
                } else {
                    Log.w(TAG, "onChildChanged:unknown_child:" + instructionKey);
                }
                // [END_EXCLUDE]
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String instructionKey = dataSnapshot.getKey();

                // [START_EXCLUDE]
                int instructionIndex = mInstructionIds.indexOf(instructionKey);
                if (instructionIndex > -1) {
                    // Remove data from the list
                    mInstructionIds.remove(instructionIndex);
                    mInstructions.remove(instructionIndex);

                    // Update the RecyclerView
                    notifyItemRemoved(instructionIndex);
                } else {
                    Log.w(TAG, "onChildRemoved:unknown_child:" + instructionKey);
                }
                // [END_EXCLUDE]
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
                Instruction movedInstruction = dataSnapshot.getValue(Instruction.class);
                String instructionKey = dataSnapshot.getKey();

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
    public EditStepsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_recipe_instruction, viewGroup, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

        myViewHolder.tv_recipe_instruction.setText(mInstructions.get(i).step);
        myViewHolder.tv_recipe_instruction_num.setText(mInstructions.get(i).num);

    }

    @Override
    public int getItemCount() {
        return mInstructions.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_recipe_instruction;
        TextView tv_recipe_instruction_num;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);

            tv_recipe_instruction_num = itemView.findViewById(R.id.recipe_item_instruction_list_tv_num);
            tv_recipe_instruction = itemView.findViewById(R.id.recipe_item_instruction_list_tv_step);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, "Recipe Id: " + dataSnapShotKey, Toast.LENGTH_SHORT).show();
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mInstructions.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    Toast.makeText(mContext, "Instruction Deleted. " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

        }
    }

    public void cleanupListener() {
        if (mChildEventListener != null) {
            mDatabaseReference.removeEventListener(mChildEventListener);
        }
    }

}
