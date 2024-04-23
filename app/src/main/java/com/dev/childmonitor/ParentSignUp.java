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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ParentSignUp extends AppCompatActivity {

    public MaterialCardView progressCard;
    public EditText usernameET, passwordET, emailET;
    public DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    public MaterialButton signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_signup);

        //Initialize firebase
        FirebaseApp.initializeApp(ParentSignUp.this);

        mAuth = FirebaseAuth.getInstance();
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
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressCard.setVisibility(View.GONE);
                                    // Sign in success, update UI with the signed-in user's information

                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    writeNewParent(user.getUid(), username, email, password, true, username, user);

                                } else {
                                    // If sign in fails, display a message to the user.
                                    progressCard.setVisibility(View.GONE);
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(ParentSignUp.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            } else if(password.length() < 6){
                progressCard.setVisibility(View.GONE);
                Toast.makeText(ParentSignUp.this, "Password length must be longer than 6 characters.", Toast.LENGTH_SHORT).show();
            }
            else {
                progressCard.setVisibility(View.GONE);
                Toast.makeText(ParentSignUp.this, "Please fill in all details.", Toast.LENGTH_SHORT).show();
            }

        });

    }
    private void reload() {
        Intent i = new Intent(ParentSignUp.this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    public void writeNewParent(String userId, String name, String email, String password, boolean isParent, String username, FirebaseUser user1) {
        User user = new User(name, email, password, isParent);
        mDatabase.child("Users").child("Parents").child(userId).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                // For setting up user display name, taken from username input.
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(username).build();

                ParentsUIDGroup parentsUIDGroup = new ParentsUIDGroup(userId);

                mDatabase.child("Users").child("Parent UIDs").setValue(parentsUIDGroup).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        user1.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User profile updated.");
                                            reload();
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