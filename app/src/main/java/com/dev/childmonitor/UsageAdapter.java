package com.dev.childmonitor;/*
 * *
 *  * Created by Husayn on 22/10/2021, 5:04 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 22/10/2021, 2:29 PM
 *
 */

import static android.content.ContentValues.TAG;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsageAdapter extends RecyclerView.Adapter<UsageAdapter.ViewHolder> {

    // Constants defining order for display order

    private static final int _DISPLAY_ORDER_USAGE_TIME = 0;
    private static final int _DISPLAY_ORDER_LAST_TIME_USED = 1;
    private static final int _DISPLAY_ORDER_APP_NAME = 2;
    private static final boolean localLOGV = false;
    private int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME;
    private UsageStatsActivity.LastTimeUsedComparator mLastTimeUsedComparator = new UsageStatsActivity.LastTimeUsedComparator();
    private UsageStatsActivity.UsageTimeComparator mUsageTimeComparator = new UsageStatsActivity.UsageTimeComparator();
    private UsageStatsActivity.AppNameComparator mAppLabelComparator;
    private final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
    private final ArrayList<UsageStats> mPackageStats = new ArrayList<>();

    private UsageStatsManager mUsageStatsManager;
    private Context context;
    private PackageManager mPm;
    private LayoutInflater mInflater;
    public FirebaseUser user;
    public DatabaseReference mDatabase;
    private OnItemClick mCallback;
    public HashMap<String, Long> mapToFirebase = new HashMap<>();
    public interface OnItemClick {
        void onClick(HashMap<String, Long> list);
    }

    UsageAdapter(UsageStatsManager mUsageStatsManager, Context context, PackageManager mPm, FirebaseUser user, DatabaseReference mDatabase, OnItemClick onItemClick) {
        this.mUsageStatsManager = mUsageStatsManager;
        this.user = user;
        this.mDatabase = mDatabase;
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mPm = mPm;
        this.mCallback = onItemClick;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -5);

        final List<UsageStats> stats =
                mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                        cal.getTimeInMillis(), System.currentTimeMillis());
        if (stats == null) {
            return;
        }

        ArrayMap<String, UsageStats> map = new ArrayMap<>();

        final int statCount = stats.size();
        for (int i = 0; i < statCount; i++) {
            final android.app.usage.UsageStats pkgStats = stats.get(i);

            // load application labels for each application
            try {
                ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                String label = appInfo.loadLabel(mPm).toString();
                mAppLabelMap.put(pkgStats.getPackageName(), label);

                UsageStats existingStats =
                        map.get(pkgStats.getPackageName());
                if (existingStats == null) {
                    map.put(pkgStats.getPackageName(), pkgStats);

                } else {
                    existingStats.add(pkgStats);
                }

            } catch (PackageManager.NameNotFoundException e) {
                // This package may be gone.
            }
        }
        mPackageStats.addAll(map.values());

        // Sort list
        mAppLabelComparator = new UsageStatsActivity.AppNameComparator(mAppLabelMap);
        sortList();

    }

    public void loadDataToMapForFirebase(){
        /*TODO UPDATE DATA ONBACKGROUND*/
        String system_sign = "android";
        PackageManager pm = context.getPackageManager();

        if (mPackageStats.size() != 0){
            for (int i = 0; i < mPackageStats.size(); i++){
                if (pm.checkSignatures(system_sign, mPackageStats.get(i).getPackageName()) == PackageManager.SIGNATURE_MATCH) {
                    Log.d("CheckAppSys", mPackageStats.get(i).getPackageName() + " is sys.");
                } else {
                    String usagetime = DateUtils.formatElapsedTime(mPackageStats.get(i).getTotalTimeInForeground() / 1000);
                    Pattern special= Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = special.matcher(Objects.requireNonNull(mAppLabelMap.get(mPackageStats.get(i).getPackageName())));

                    boolean constainsSymbols = matcher.find();

                    if(!constainsSymbols){
                        mapToFirebase.put(mAppLabelMap.get(mPackageStats.get(i).getPackageName()), mPackageStats.get(i).getTotalTimeInForeground());
                    }

                    Log.d("maptofirebase", mapToFirebase.toString());
                    Log.d("CheckApp", mPackageStats.get(i).getPackageName() + " is not sys.");

                }
            }
        }
    }

    @NonNull
    @Override
    public UsageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.usage_stats_item, parent, false);
        loadDataToMapForFirebase();
        if (mapToFirebase!= null && !mapToFirebase.isEmpty()){
            mCallback.onClick(mapToFirebase);
        }
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull UsageAdapter.ViewHolder holder, int position) {
        UsageStats pkgStats = mPackageStats.get(position);

        if (pkgStats != null) {

            String label = mAppLabelMap.get(pkgStats.getPackageName());


            holder.pkgName.setText(label);
            holder.lastTimeUsed.setText(DateUtils.formatSameDayTime(pkgStats.getLastTimeUsed(),
                    System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM));
            holder.usageTime.setText(
                    DateUtils.formatElapsedTime(pkgStats.getTotalTimeInForeground() / 1000));
            try {
                Drawable icon = context.getPackageManager().getApplicationIcon(pkgStats.getPackageName());
                holder.package_icon.setImageDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Log.w(TAG, "No usage stats info for package:" + position);
        }

    }

    @Override
    public int getItemCount() {
        return mPackageStats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView pkgName;
        TextView lastTimeUsed;
        TextView usageTime;
        ImageView package_icon;

        ViewHolder(View itemView) {
            super(itemView);
            pkgName = itemView.findViewById(R.id.package_name);
            lastTimeUsed = itemView.findViewById(R.id.last_time_used);
            usageTime = itemView.findViewById(R.id.usage_time);
            package_icon = itemView.findViewById(R.id.package_icon);
        }

    }

    void sortList(int sortOrder) {
        if (mDisplayOrder == sortOrder) {
            // do nothing
            return;
        }
        mDisplayOrder = sortOrder;
        sortList();
    }

    private void sortList() {
        if (mDisplayOrder == _DISPLAY_ORDER_USAGE_TIME) {
            if (localLOGV) Log.i(TAG, "Sorting by usage time");
            Collections.sort(mPackageStats, mUsageTimeComparator);
        } else if (mDisplayOrder == _DISPLAY_ORDER_LAST_TIME_USED) {
            if (localLOGV) Log.i(TAG, "Sorting by last time used");
            Collections.sort(mPackageStats, mLastTimeUsedComparator);
        } else if (mDisplayOrder == _DISPLAY_ORDER_APP_NAME) {
            if (localLOGV) Log.i(TAG, "Sorting by application name");
            Collections.sort(mPackageStats, mAppLabelComparator);
        }
        notifyDataSetChanged();
    }
}
