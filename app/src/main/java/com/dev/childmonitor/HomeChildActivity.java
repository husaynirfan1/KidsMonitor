package com.dev.childmonitor;

import android.Manifest;
import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.orbitalsonic.waterwave.WaterWaveView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeChildActivity extends AppCompatActivity {

    public WaterWaveView batteryWaveView;
    private LocationRequest locationRequest;
    public TextView location_tv, welcome_user_Tv;
    public Geocoder geocoder;
    public List<Address> addresses;
    public SwipeRefreshLayout swipeRefreshLayout;
    public MaterialCardView location_btn_card, apps_btn;
    public UsageStatsManager usageStatsManager;
    public DatabaseReference mDatabase;
    public FirebaseAuth mAuth;
    public FirebaseUser user;
    public float batteryPct;
    public String parentUID = "";
    public Query childDir;
    public ValueEventListener childDirListener;
    public Query childNode;
    public ValueEventListener childNodeListener;

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryPct = level * 100 / (float) scale;
            batteryWaveView.setMax(100);
            batteryWaveView.setProgress((int) batteryPct);
            if (batteryPct > 50) {
                batteryWaveView.setFrontWaveColor(ContextCompat.getColor(HomeChildActivity.this, R.color.teal_700));
                batteryWaveView.setBehindWaveColor(ContextCompat.getColor(HomeChildActivity.this, R.color.teal_200));
            } else {
                batteryWaveView.setFrontWaveColor(ContextCompat.getColor(HomeChildActivity.this, R.color.red_dark));
                batteryWaveView.setBehindWaveColor(ContextCompat.getColor(HomeChildActivity.this, R.color.red));
            }

        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*TODO PROPER DIALOG SIGN OUT*/
        if (mAuth.getCurrentUser() != null){
            FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() == null){
                        //Do anything here which needs to be done after signout is complete
                        Toast.makeText(HomeChildActivity.this, "Signed out.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else {
                    }
                }
            };
            mAuth.addAuthStateListener(authStateListener);
            mAuth.signOut();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_child);

        /*define*/
        batteryWaveView = findViewById(R.id.batteryWaveView);
        location_tv = findViewById(R.id.location_tv);
        swipeRefreshLayout = findViewById(R.id.swipe_view);
        location_btn_card = findViewById(R.id.location_btn_card);
        apps_btn = findViewById(R.id.apps_btn);
        welcome_user_Tv = findViewById(R.id.welcome_user_Tv);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (user != null) {
            getChildDir();
            welcome_user_Tv.setText("Welcome, "+user.getDisplayName()+"!");
        }

        /*set battery receiver*/
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setIntervalMillis(5000)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(100000)
                .build();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCurrentLocation();
            }
        });

        apps_btn.setOnClickListener(v -> {
            Intent i = new Intent(HomeChildActivity.this, UsageStatsActivity.class);
            if (!parentUID.equals("")){
                i.putExtra("parent_uid", parentUID);
                startActivity(i);
            }

            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);

            if (stats == null || stats.isEmpty()) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }


        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) {

                    getCurrentLocation();

                } else {

                    turnOnGPS();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                getCurrentLocation();
            }
        }
    }

    public void getChildDir(){
        childDir = mDatabase.child("Users").child("Parents");
        childDirListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String key = snapshot.getKey();

                Log.d("Parent Node", key);
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Log.d("Parent Snapshot", snapshot1.getKey());
                    if (snapshot1.getKey() != null) {
                        childNode = mDatabase.child("Users").child("Parents").child(snapshot1.getKey()).child("Children").child(user.getUid());
                        childDirListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ChildModal modal = snapshot.getValue(ChildModal.class);
                                if (modal != null){
                                    Log.d("ChildModal", modal.getUsername());
                                    Log.d("parent_uid", modal.getParent_uid());
                                    parentUID = modal.getParent_uid();

                                    mDatabase.child("Users").child("Parents").child(snapshot1.getKey()).child("Children").child(user.getUid()).child("battery").setValue(String.valueOf(batteryPct));
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        };
                        childNode.addValueEventListener(childDirListener);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        childDir.addValueEventListener(childDirListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (childDirListener != null) {
            childDir.removeEventListener(childDirListener);
        }
        if (childNodeListener != null){
            childNode.removeEventListener(childNodeListener);
        }
    }

    private void getCurrentLocation() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(HomeChildActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) {

                    LocationServices.getFusedLocationProviderClient(HomeChildActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(HomeChildActivity.this)
                                            .removeLocationUpdates(this);

                                    if (locationResult != null && locationResult.getLocations().size() > 0) {

                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();
                                        getAdresses(latitude, longitude);
                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void turnOnGPS() {


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(HomeChildActivity.this, "GPS is already turn on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(HomeChildActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }

    private void getAdresses(double latitude, double longitude) {

        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            if (city != null) {
                swipeRefreshLayout.setRefreshing(false);
                location_tv.setText(city);
                if (!parentUID.equals("")){
                    mDatabase.child("Users").child("Parents").child(parentUID).child("Children").child(user.getUid()).child("location").setValue(location_tv.getText().toString());

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}