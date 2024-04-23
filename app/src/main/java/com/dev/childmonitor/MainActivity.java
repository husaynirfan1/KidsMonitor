package com.dev.childmonitor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView signUpTV;
    public MaterialButton signinBtn;
    public FirebaseAuth mAuth;
    public ValueEventListener parentListener, childListener;
    public DatabaseReference mDatabase;
    public Query childrenRef, parentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(MainActivity.this);

        mAuth = FirebaseAuth.getInstance();
        signUpTV = findViewById(R.id.signUpTV);
        signinBtn = findViewById(R.id.signinBtn);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        signinBtn.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, SignIn.class);
            startActivity(i);
        });

        SpannableString ss = new SpannableString("Don't have an account ? Sign Up");

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                startActivity(new Intent(MainActivity.this, ParentSignUp.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(ContextCompat.getColor(MainActivity.this, R.color.black));
                ds.setFakeBoldText(true);
            }
        };
        ss.setSpan(clickableSpan, 24, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        signUpTV.setText(ss);
        signUpTV.setMovementMethod(LinkMovementMethod.getInstance());
        signUpTV.setHighlightColor(Color.TRANSPARENT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() != null) {
            CheckisParent(mAuth.getUid());
            Log.d("MAuthUID", mAuth.getUid());
        }

    }

    private void CheckisParent(String uid) {
        parentRef =  mDatabase.child("Users").child("Parents");

        childListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        Log.d("ChildKey", snapshot1.getKey());
                    }

                    Log.d("User Condition", uid + " == " + uid + " : isChild");
                    Intent i = new Intent(MainActivity.this, HomeChildActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        parentListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    String keyParents = snapshot1.getKey();
                    Log.d("KeyParents", snapshot1.getKey());
                    if (keyParents != null){
                        if (uid.equals(keyParents)){
                            Log.d("User Condition", keyParents + " == " + uid + " : isParent");

                            /*START HOME*/
                            Intent i = new Intent(MainActivity.this, HomeActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);

                        }else {
                            childrenRef =  mDatabase.child("Users").child("Parents").child(keyParents).child("Children").orderByChild(uid);
                            childrenRef.addListenerForSingleValueEvent(childListener);

                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        parentRef.addListenerForSingleValueEvent(parentListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (parentListener != null) {
            parentRef.removeEventListener(parentListener);
        }
        if (childListener!= null){
            childrenRef.removeEventListener(childListener);

        }
    }
}