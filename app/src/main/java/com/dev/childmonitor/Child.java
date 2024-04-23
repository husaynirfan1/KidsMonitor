package com.dev.childmonitor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


public class Child extends Fragment {

    public FirebaseAuth mAuth;
    public FirebaseUser user;
    public DatabaseReference mDatabase;
    public MaterialButton add_new_btn;

    public RecyclerView rv_children;
    ChildAdapter childAdapteradapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_child, container, false);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        add_new_btn = v.findViewById(R.id.add_new_btn);
        add_new_btn.setOnClickListener(view -> {
            Intent i = new Intent(getActivity(), CreateChild.class);
            startActivity(i);
        });

        if (user != null){
            /* Adapter / RV / FIREBASE */
            Query childrenRef = mDatabase.child("Users").child("Parents").child(user.getUid()).child("Children");
            rv_children = v.findViewById(R.id.rv_children);

            NpaGridLayoutManager npaGridLayoutManager = new NpaGridLayoutManager(getActivity(), 1);
            rv_children.setLayoutManager(npaGridLayoutManager);
            FirebaseRecyclerOptions<ChildModal> options
                    = new FirebaseRecyclerOptions.Builder<ChildModal>()
                    .setQuery(childrenRef, ChildModal.class)
                    .build();

            childAdapteradapter = new ChildAdapter(options);
            rv_children.setAdapter(childAdapteradapter);

        }


        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        childAdapteradapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        childAdapteradapter.stopListening();
    }
    private static class NpaGridLayoutManager extends GridLayoutManager {

        /**
         * Disable predictive animations. There is a bug in RecyclerView which causes views that
         * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
         * adapter size has decreased since the ViewHolder was recycled.
         */
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        public NpaGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public NpaGridLayoutManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        public NpaGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
            super(context, spanCount, orientation, reverseLayout);
        }
    }
}