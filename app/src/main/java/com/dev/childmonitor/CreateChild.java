package com.dev.childmonitor;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CreateChild extends AppCompatActivity {

    public MaterialCardView progressCard;
    public EditText usernameET, passwordET, emailET;
    public DatabaseReference mDatabase;
    private FirebaseAuth mAuth, mAuthNEW;
    public MaterialButton signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_child);
        mAuth = FirebaseAuth.getInstance();

        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
                .setDatabaseUrl("https://child-monitor-a1321-default-rtdb.asia-southeast1.firebasedatabase.app")
                .setApiKey("AIzaSyAPSFjw7hWv7nh6abbHqV5fKl7XfdZKJbU")
                .setApplicationId("child-monitor-a1321").build();

        try { FirebaseApp myApp = FirebaseApp.initializeApp(getApplicationContext(), firebaseOptions, "Child Monitor");
            mAuthNEW = FirebaseAuth.getInstance(myApp);
        } catch (IllegalStateException e){
            mAuthNEW = FirebaseAuth.getInstance(FirebaseApp.getInstance("Child Monitor"));
            Log.d("CreateChildClass Exception", e.toString());
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        /*define */
        progressCard = findViewById(R.id.progress_layout);
        usernameET = findViewById(R.id.usernameET);
        passwordET = findViewById(R.id.passwordET);
        emailET = findViewById(R.id.emailET);
        signUpBtn = findViewById(R.id.signUpBtn);

        //set sign up on click listener. -> after click sign up btn, this will start sign up process
        signUpBtn.setOnClickListener(view -> {

            progressCard.setVisibility(View.VISIBLE);
            String username = usernameET.getText().toString();
            String email = emailET.getText().toString();
            String password = passwordET.getText().toString();

            if (!username.equals("") && !email.equals("") && !password.equals("") && password.length() > 5){
                mAuthNEW.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressCard.setVisibility(View.GONE);
                                    // Sign in success, update UI with the signed-in user's information

                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser userNEW = mAuthNEW.getCurrentUser();
                                    writeNewChild(userNEW.getUid(), username, email, password, false, userNEW);

                                } else {
                                    // If sign in fails, display a message to the user.
                                    progressCard.setVisibility(View.GONE);
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(CreateChild.this, "Authentication failed, please try again.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("CreateChildClass Exception", e.toString());
                            }
                        });
            } else if(password.length() < 6){
                progressCard.setVisibility(View.GONE);
                Toast.makeText(CreateChild.this, "Password length must be longer than 6 characters.", Toast.LENGTH_SHORT).show();
            }
            else {
                progressCard.setVisibility(View.GONE);
                Toast.makeText(CreateChild.this, "Please fill in all details.", Toast.LENGTH_SHORT).show();
            }

        });

    }
    public void writeNewChild(String userId, String name, String email, String password, boolean isParent, FirebaseUser user1) {
        if (mAuth.getCurrentUser() != null){
            HashMap<String, Long> map = new HashMap<>();
            HashMap<String, String> remindermap = new HashMap<>();
            remindermap.put("Value", "Initial");
            map.put("Test", 21L);
            ChildModal childModal = new ChildModal(name, email, password, isParent, "Empty", "Empty", mAuth.getCurrentUser().getUid(), map, userId, remindermap);

            mDatabase.child("Users").child("Parents").child(mAuth.getCurrentUser().getUid()).child("Children").child(userId).setValue(childModal).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                    // For setting up user display name, taken from username input.
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name).build();

                    ChildUIDGroup childUIDGroup = new ChildUIDGroup(userId);
                    mDatabase.child("Users").child("Child UIDs").setValue(childUIDGroup).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            user1.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                                Toast.makeText(CreateChild.this, "Success adding new child user!", Toast.LENGTH_SHORT).show();
                                                mAuthNEW.signOut();
                                                CreateChild.super.onBackPressed();
                                            }
                                        }
                                    });
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Exception Firebase Database", e.toString());
                }
            });

        }



    }

}