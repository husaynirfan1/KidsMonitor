package com.dev.childmonitor;/*
 * *
 *  * Created by Husayn on 22/10/2021, 5:04 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 22/10/2021, 2:29 PM
 *
 */

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.card.MaterialCardView;
import com.orbitalsonic.waterwave.WaterWaveView;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// FirebaseRecyclerAdapter is a class provided by
// FirebaseUI. it provides functions to bind, adapt and show
// database contents in a Recycler View
public class ChildOvAdapter extends FirebaseRecyclerAdapter<
        ChildModal, ChildOvAdapter.ChildViewHolder> {


    public ChildOvAdapter(@NonNull FirebaseRecyclerOptions<ChildModal> options, RecyclerView rv) {
        super(options);
        this.rv = rv;
    }

    RecyclerView rv;

    @Override
    protected void
    onBindViewHolder(@NonNull ChildViewHolder holder,
                     int position, @NonNull ChildModal model) {

        holder.child_username.setText(model.getUsername());
        Log.d("model.getusername", model.getUsername());
        holder.location_tv.setText(model.getLocation());
        String battery = model.getBattery();
        if (battery != null){

            if (battery.equals("Empty")) {
                holder.waterWaveView.setProgress(0);
            } else {
                holder.waterWaveView.setProgress((int) Float.parseFloat(battery));
            }
        }
        HashMap<String, Long> maps = model.getAppsList();
        if (maps != null){
            List<String> keys = maps.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(1).map(Map.Entry::getKey).collect(Collectors.toList());
            String mostUsed = keys.get(0);
            Long usageInMillis = maps.get(mostUsed);
            String inTime = DateUtils.formatElapsedTime(usageInMillis / 1000);
            holder.most_screentime_Tv.setText(mostUsed + " : "+inTime);
            Log.d("1Limit", keys.toString());
        }

        holder.expand_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (holder.exp_item.isExpanded()) {
                    holder.exp_item.collapse();
                } else {
                    holder.exp_item.expand();
                }
            }
        });

        if (holder.most_screentime_card.getContext() != null){
            holder.most_screentime_card.setOnClickListener(view -> {
                Intent i = new Intent(holder.most_screentime_card.getContext(), UsagesParentView.class);
                i.putExtra("child_uid", model.getChild_uid());
                holder.most_screentime_card.getContext().startActivity(i);
            });
        }

    }

    @NonNull
    @Override
    public ChildViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType) {
        View view
                = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.child_overview_item, parent, false);

        return new ChildOvAdapter.ChildViewHolder(view);
    }

    class ChildViewHolder
            extends RecyclerView.ViewHolder {
        TextView child_username, location_tv, most_screentime_Tv;
        WaterWaveView waterWaveView;
        ImageButton expand_btn;
        //        ConstraintLayout exp_item;
        ExpandableLayout exp_item;
        MaterialCardView most_screentime_card;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);

            most_screentime_card = itemView.findViewById(R.id.most_screentime_card);
            child_username
                    = itemView.findViewById(R.id.child_username);
            location_tv = itemView.findViewById(R.id.location_tv);
            waterWaveView = itemView.findViewById(R.id.waterWaveView);
            expand_btn = itemView.findViewById(R.id.expand_btn);
            exp_item = itemView.findViewById(R.id.exp_item);
            most_screentime_Tv = itemView.findViewById(R.id.most_screentime_Tv);
            waterWaveView.setMax(100);
            waterWaveView.startAnimation();
            exp_item.collapse();
        }
    }
}