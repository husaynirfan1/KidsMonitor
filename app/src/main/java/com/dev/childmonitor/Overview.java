package com.dev.childmonitor;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Layout;
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

import java.util.ArrayList;
import java.util.List;

import pokercc.android.expandablerecyclerview.ExpandableRecyclerView;


public class Overview extends Fragment {

    public RecyclerView child_rv;
    public FirebaseAuth mAuth;
    public FirebaseUser user;
    public DatabaseReference mDatabase;
    public MaterialButton add_new_btn;

    public RecyclerView rv_children;
    ChildOvAdapter childOvAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_overview, container, false);

        /*define*/
        child_rv = v.findViewById(R.id.rv_children);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

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

            childOvAdapter = new ChildOvAdapter(options, rv_children);
            rv_children.setAdapter(childOvAdapter);

        }
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        childOvAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        childOvAdapter.stopListening();
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