package com.irondigitalmedia.keep.Fragments;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.irondigitalmedia.keep.Authentication.Login;
import com.irondigitalmedia.keep.MainActivity;
import com.irondigitalmedia.keep.Model.Option;
import com.irondigitalmedia.keep.Model.User;
import com.irondigitalmedia.keep.R;

import java.util.List;

public class SettingsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;


    private static final String TAG = SettingsFragment.class.getSimpleName();

    private Button mLogOut;

    private DatabaseReference mDatabase;
    private FirebaseUser mFBuser;
    private User user;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;

    // String
    private String users = "users";

    private MainActivity mainActivity;
    private Toolbar toolbar;
    private Context mContext;

    public SettingsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mainActivity = (MainActivity) view.getContext();

        toolbar = mainActivity.findViewById(R.id.main_toolbar);
        mainActivity.setSupportActionBar(toolbar);
        toolbar.setTitle("Settings");

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity().getApplicationContext(),gso);

        mLogOut = view.findViewById(R.id.settings_button_logout);
        mLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignOut();
                GoToLogin();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Views
        if(mUser!=null){
            mUser = mAuth.getCurrentUser();
            Log.i(TAG, "onCreate: mUser UID " + mUser.getUid());
            mDatabase.child(users).child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);

                    if(user!=null){
                        Log.i(TAG, "onDataChange: user's name " + user.name);
                        Log.i(TAG, "onDataChange: user's name " + user.email);
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        /*mSettingsList = findViewById(R.id.settings_list);
        SettingsRecyclerViewAdapter adapter = new SettingsRecyclerViewAdapter(this,mUserInfo, this);
        mSettingsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mSettingsList.setItemAnimator(new DefaultItemAnimator());
        mSettingsList.setAdapter(adapter);*/
        return view;
    }

    private void SignOut() {
        Log.i(TAG, "SignOut: Users are being signed out.");
        mAuth.signOut();
        mGoogleSignInClient.signOut();
    }

    private void GoToLogin() {
        Log.i(TAG, "GoToLogin: user is being taken back to the login");
        Intent intent = new Intent(getContext(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.getCurrentUser();
    }



    public class SettingsRecyclerViewAdapter extends RecyclerView.Adapter<SettingsRecyclerViewAdapter.MyViewHolder> {

        private Context mContext;
        private List<Option> mOption;

        public SettingsRecyclerViewAdapter(Context mContext, List<Option> mOption){
            this.mContext = mContext;
            this.mOption = mOption;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.list_item_settings_headings,viewGroup,false);

            return new SettingsRecyclerViewAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
            viewHolder.setting_mainHead.setText(mOption.get(i).mainHeading);
            viewHolder.settingSubHead.setText(mOption.get(i).subHeading);
        }

        @Override
        public int getItemCount() {
            return mOption.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView setting_mainHead, settingSubHead;

            public MyViewHolder(@NonNull final View itemView) {
                super(itemView);
                setting_mainHead = itemView.findViewById(R.id.settings_row_MainHeading);
                settingSubHead = itemView.findViewById(R.id.settings_row_SubHeading);

            }

        }

    }


}
