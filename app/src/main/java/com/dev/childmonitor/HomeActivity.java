package com.dev.childmonitor;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    ColorStateList def;
    TextView item1;
    TextView item2;
    TextView select;
    public TextView welcome_user_Tv;
    public ViewPager2 pager;
    private static final int NUM_PAGES = 2;
    private FragmentStateAdapter pagerAdapter;
    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        /*Initialise Firebase*/
        mAuth = FirebaseAuth.getInstance();

        item1 = findViewById(R.id.item1);
        item2 = findViewById(R.id.item2);
        item1.setOnClickListener(this);
        item2.setOnClickListener(this);
        select = findViewById(R.id.select);
        def = item2.getTextColors();
        pager = findViewById(R.id.pager);
        welcome_user_Tv = findViewById(R.id.welcome_user_Tv);

        if (mAuth.getCurrentUser() != null) {
            welcome_user_Tv.setText("Welcome, " + mAuth.getCurrentUser().getDisplayName() + "!");

        }
        /*set pager adapter*/
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        pager.setAdapter(pagerAdapter);
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    item1.setTextColor(Color.WHITE);
                    item2.setTextColor(def);
                    select.animate().x(0).setDuration(100);
                } else {
                    item1.setTextColor(def);
                    item2.setTextColor(Color.WHITE);
                    int size = item2.getWidth();
                    select.animate().x(size).setDuration(100);
                }
            }
        });


    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.item1) {
            select.animate().x(0).setDuration(100);
            item1.setTextColor(Color.WHITE);
            item2.setTextColor(def);
            pager.setCurrentItem(0);
        } else if (view.getId() == R.id.item2) {
            item1.setTextColor(def);
            item2.setTextColor(Color.WHITE);
            int size = item2.getWidth();
            select.animate().x(size).setDuration(100);
            pager.setCurrentItem(1);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*TODO PROPER SIGN OUT DIALOG*/
        if (mAuth.getCurrentUser() != null) {
            FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (firebaseAuth.getCurrentUser() == null){
                        //Do anything here which needs to be done after signout is complete
                        Toast.makeText(HomeActivity.this, "Signed out.", Toast.LENGTH_SHORT).show();
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

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            Log.d("Pager Pos", String.valueOf(position));
            if (position == 0) {

                return new Overview();

            } else {

                return new Child();
            }
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}