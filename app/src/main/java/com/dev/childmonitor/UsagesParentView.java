package com.dev.childmonitor;

import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class UsagesParentView extends AppCompatActivity implements ParentStatsAdapter.OnSelectedTime {

    private static final String TAG = "UsageStatsActivity";
    private static final boolean localLOGV = false;

    public RecyclerView rv;
    public UsageAdapter usageAdapter;
    public FirebaseAuth mAuth;
    public FirebaseUser user;
    public DatabaseReference mDatabase;
    public String parent_uid;
    public ParentStatsAdapter adapter;
    public Query childrenRef;
    public ValueEventListener listener;
    public Map<String, Object> remindermap = new HashMap<>();
    public String child_uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usages_parent_view);
        /*Setup Firebase*/

        remindermap.clear();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        /*define*/
        rv = findViewById(R.id.rv_usage);

        NpaGridLayoutManager npaGridLayoutManager = new NpaGridLayoutManager(UsagesParentView.this, 1);
        rv.setLayoutManager(npaGridLayoutManager);

        Intent intent = getIntent();
        child_uid = intent.getStringExtra("child_uid");
        Log.d("child_uid", child_uid);

        if (user != null) {
            if (!child_uid.equals("")) {
                childrenRef = mDatabase.child("Users").child("Parents").child(user.getUid()).child("Children").child(child_uid);
                listener = new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ChildModal modal = snapshot.getValue(ChildModal.class);
                        HashMap<String, Long> map = modal.getAppsList();

                        if (map != null) {
                            Map<String, Long> sortedMap =
                                    map.entrySet().stream()
                                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                                    (e1, e2) -> e1, LinkedHashMap::new));

                            List<String> keyset = new ArrayList<>(sortedMap.keySet());
                            List<Long> values = new ArrayList<>(sortedMap.values());

                            adapter = new ParentStatsAdapter(UsagesParentView.this, rv, keyset, values, UsagesParentView.this);

                            rv.setAdapter(adapter);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };

                childrenRef.addValueEventListener(listener);


            }
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            childrenRef.removeEventListener(listener);
        }
    }

    @Override
    public void onSelected(String AppName, Integer HOUR, Integer MIN) {
        Log.d("PARENTVIEW", AppName + " - " + HOUR + " : " + MIN);
        String time = HOUR + ":" + MIN;
        remindermap.put(AppName, time);
        if (user != null) {
            if (!child_uid.equals("")){
                mDatabase.child("Users").child("Parents").child(user.getUid()).child("Children").child(child_uid).child("reminderList").updateChildren(remindermap);

                /*TODO FIX TO CHECK TIME DIFF BETWEEN*/

                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                try {
                    Date c = Calendar.getInstance().getTime();
                    Log.d("curdate", c.toString());

                    Date date = format.parse(time);
                    Log.d("cdate", date.toString());

                    long millis = c.getTime() - date.getTime();
                    int hours = (int) (millis / (1000 * 60 * 60));
                    int mins = (int) ((millis / (1000 * 60)) % 60);

                    Toast.makeText(UsagesParentView.this, hours+":"+mins, Toast.LENGTH_SHORT).show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }


        }


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