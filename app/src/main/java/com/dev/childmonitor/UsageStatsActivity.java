package com.dev.childmonitor;

import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/**
 * Activity to display package usage statistics.
 */
public class UsageStatsActivity extends Activity implements OnItemSelectedListener, UsageAdapter.OnItemClick{
    private static final String TAG = "UsageStatsActivity";
    private static final boolean localLOGV = false;
    private UsageStatsManager mUsageStatsManager;
    private LayoutInflater mInflater;
    private PackageManager mPm;
    public RecyclerView rv;
    public UsageAdapter usageAdapter;
    public FirebaseAuth mAuth;
    public FirebaseUser user;
    public DatabaseReference mDatabase;
    public String parent_uid;

    @Override
    public void onClick(HashMap<String, Long> list) {
        Log.d("MAPFROMACTIVITY", list.toString());
        if (!parent_uid.equals("")){

            mDatabase.child("Users").child("Parents").child(parent_uid).child("Children").child(user.getUid()).child("appsList").setValue(list);
        }
    }

    public static class AppNameComparator implements Comparator<UsageStats> {
        private Map<String, String> mAppLabelList;

        AppNameComparator(Map<String, String> appList) {
            mAppLabelList = appList;
        }

        @Override
        public final int compare(UsageStats a, UsageStats b) {
            String alabel = mAppLabelList.get(a.getPackageName());
            String blabel = mAppLabelList.get(b.getPackageName());
            return alabel.compareTo(blabel);
        }
    }

    public static class LastTimeUsedComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            // return by descending order
            return (int)(b.getLastTimeUsed() - a.getLastTimeUsed());
        }
    }

    public static class UsageTimeComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            return (int)(b.getTotalTimeInForeground() - a.getTotalTimeInForeground());
        }
    }

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.usage_view);



        /*Setup Firebase*/

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        /*get intent*/
        Intent intent = getIntent();

        if (!intent.getStringExtra("parent_uid").equals("")){
            parent_uid = intent.getStringExtra("parent_uid");
        }
        /*define*/

        mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPm = getPackageManager();
        rv = findViewById(R.id.rv_usage);

        Spinner typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        typeSpinner.setOnItemSelectedListener(this);

        String[] list = getResources().getStringArray(R.array.usage_stats_display_order_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_properties,list);
        typeSpinner.setAdapter(adapter);

        rv.setLayoutManager(new LinearLayoutManager(this));
        usageAdapter = new UsageAdapter(mUsageStatsManager, UsageStatsActivity.this, mPm, user, mDatabase, this);
        rv.setAdapter(usageAdapter);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        usageAdapter.sortList(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}