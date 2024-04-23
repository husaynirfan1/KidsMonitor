package com.dev.childmonitor;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SignIn extends AppCompatActivity {

    public FirebaseAuth firebaseAuth;
    public FirebaseUser user;
    public EditText emailET, passET;
    public MaterialButton signInButton;
    public TextView forgetpasswordTV;
    public DatabaseReference mDatabase;
    boolean checker = false;

    public ValueEventListener parentListener, childListener;
    public Query childrenRef, parentRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //define views
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        emailET = findViewById(R.id.emailET);
        passET = findViewById(R.id.passwordET);
        forgetpasswordTV = findViewById(R.id.forgetpasswordTV);
        signInButton = findViewById(R.id.signInButton);
        mDatabase = FirebaseDatabase.getInstance().getReference();


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailET.getText().toString();
                String password = passET.getText().toString();

                //if email and pass not empty
                if (!email.equals("") && !password.equals("")){
                    firebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignIn.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success");
                                        FirebaseUser user = firebaseAuth.getCurrentUser();
                                        if (user != null){
                                            CheckisParent(user.getUid());
                                            Toast.makeText(SignIn.this, "Welcome, "+user.getDisplayName(),
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(SignIn.this, "Authentication failed, please check your email and password are correct.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                } else Toast.makeText(SignIn.this, "Please fill in all details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void CheckisParent(String uid) {
        parentRef =  mDatabase.child("Users").child("Parents");
        childListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    /*start activity*/
                    ChildModal modal = snapshot.getValue(ChildModal.class);
                    /*if want to use child dir use above*/

                    Log.d("User Condition", uid + " == " + uid + " : isChild");
                    Intent i = new Intent(SignIn.this, HomeChildActivity.class);
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
                    if (keyParents != null){
                        if (uid.equals(keyParents)){
                            Log.d("User Condition", keyParents + " == " + uid + " : isParent");
                    
                            /*START HOME*/
                            Intent i = new Intent(SignIn.this, HomeActivity.class);
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
}