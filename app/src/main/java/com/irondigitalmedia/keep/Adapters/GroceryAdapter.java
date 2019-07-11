package com.irondigitalmedia.keep.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.irondigitalmedia.keep.Activities.MainActivity;
import com.irondigitalmedia.keep.Model.Grocery;
import com.irondigitalmedia.keep.R;
import com.irondigitalmedia.keep.Utils.Constants;

import java.util.List;

public class GroceryAdapter extends RecyclerView.Adapter<GroceryAdapter.ViewHolder> {

    private Context mContext;
    private List<Grocery> mGroceryList;
    public FirebaseStorage mStorage;
    public FirebaseDatabase mDatabase;
    public String mGroceryKey;
    public FirebaseUser mUser;
    public FirebaseAuth mAuth;

    public GroceryAdapter(Context mContext, List<Grocery> mGroceryList) {
        this.mContext = mContext;
        this.mGroceryList = mGroceryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_grocery_item, parent, false);

        return new GroceryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        mGroceryKey = mGroceryList.get(position).getUid();
        Glide.with(mContext).load(mGroceryList.get(position).getUrl()).centerCrop().into(holder.groceryItemImage);
        holder.groceryItemName.setText(mGroceryList.get(position).getName());

    }

    @Override
    public int getItemCount() {
        return mGroceryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView groceryItemImage;
        private TextView groceryItemName;
        private MainActivity mainActivity;
        private ImageView groceryItemView;
        private TextView groceryItemViewName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mDatabase = FirebaseDatabase.getInstance();
            mStorage = FirebaseStorage.getInstance();
            mainActivity = (MainActivity) itemView.getContext();
            groceryItemImage = itemView.findViewById(R.id.grocery_list_item_image);
            groceryItemName = itemView.findViewById(R.id.grocery_list_item_name);
            mAuth = FirebaseAuth.getInstance();
            mUser = mAuth.getCurrentUser();

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    LayoutInflater inflater = mainActivity.getLayoutInflater();
                    View builderView = inflater.inflate(R.layout.grocery_view_item,null);
                    groceryItemView = builderView.findViewById(R.id.grocery_dialog_viewItem_image);
                    groceryItemViewName = builderView.findViewById(R.id.grocery_dialog_viewItem_name);
                    groceryItemViewName.setText(mGroceryList.get(getAdapterPosition()).getName());
                    Glide.with(builderView).load(mGroceryList.get(getAdapterPosition()).getUrl()).into(groceryItemView);
                            builder.setView(builderView)
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    // create and show the alert dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new android.app.AlertDialog.Builder(mContext)
                            .setTitle("Delete Grocery Item?")
                            .setMessage("Are you sure you want to delete this item?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Continue with delete operation
                                    mDatabase.getReference().child(Constants.DATABASE_ROOT_GROCERY).child(mUser.getUid()).child(mGroceryKey).removeValue();
                                    mStorage.getReference().child(Constants.DATABASE_ROOT_USERS).child(mUser.getUid()).child(Constants.DATABASE_ROOT_GROCERY).child(mGroceryKey).delete();
                                    mGroceryList.remove(getAdapterPosition());
                                    notifyItemRemoved(getAdapterPosition());
                                    Toast.makeText(mainActivity, "Grocery Item Deleted.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("cancel", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                    return true;
                }
            });
        }
    }
}
