package com.dev.childmonitor;/*
 * *
 *  * Created by Husayn on 22/10/2021, 5:04 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 22/10/2021, 2:29 PM
 *
 */

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.orbitalsonic.waterwave.WaterWaveView;

// FirebaseRecyclerAdapter is a class provided by
// FirebaseUI. it provides functions to bind, adapt and show
// database contents in a Recycler View
public class ChildAdapter extends FirebaseRecyclerAdapter<
        ChildModal, ChildAdapter.personsViewholder> {
    public ChildAdapter(
            @NonNull FirebaseRecyclerOptions<ChildModal> options)
    {
        super(options);
    }

    // Function to bind the view in Card view(here
    // "person.xml") iwth data in
    // model class(here "person.class")
    @Override
    protected void
    onBindViewHolder(@NonNull personsViewholder holder,
                     int position, @NonNull ChildModal model)
    {

        holder.username.setText(model.getUsername());
        Log.d("model.getusername", model.getUsername());
        holder.email.setText(model.getEmail());
        holder.location.setText(model.getLocation());
    }

    @NonNull
    @Override
    public personsViewholder
    onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType)
    {
        View view
                = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_child_item, parent, false);

        return new ChildAdapter.personsViewholder(view);
    }

    class personsViewholder
            extends RecyclerView.ViewHolder {
        TextView username, email, location;
        public personsViewholder(@NonNull View itemView)
        {
            super(itemView);

            username
                    = itemView.findViewById(R.id.username);
            email = itemView.findViewById(R.id.email);
            location = itemView.findViewById(R.id.location);
        }
    }
}