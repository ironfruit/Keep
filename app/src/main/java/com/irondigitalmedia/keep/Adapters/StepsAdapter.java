package com.irondigitalmedia.keep.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.irondigitalmedia.keep.Model.Instruction;
import com.irondigitalmedia.keep.R;

import java.util.List;

public class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.ViewHolder> {
    private Context mContext;
    private List<Instruction> mStepsList;

    public StepsAdapter(Context mContext, List<Instruction> mStepsList) {
        this.mContext = mContext;
        this.mStepsList = mStepsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_recipe_instruction, parent, false);

        return new StepsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Instruction instruction = mStepsList.get(position);
        holder.tv_recipe_instruction_num.setText(instruction.getNum());
        holder.tv_recipe_instruction.setText(instruction.getStep());
    }

    @Override
    public int getItemCount() {
        return mStepsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView tv_recipe_instruction;
        public TextView tv_recipe_instruction_num;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_recipe_instruction_num = itemView.findViewById(R.id.recipe_item_instruction_list_tv_num);
            tv_recipe_instruction = itemView.findViewById(R.id.recipe_item_instruction_list_tv_step);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, mStepsList.get(getAdapterPosition()).step, Toast.LENGTH_SHORT).show();
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mStepsList.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    Toast.makeText(mContext, "Instruction Deleted. " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }
}
