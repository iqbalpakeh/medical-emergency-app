/*
 * Copyright (c) 2017, Progrema Studio. All rights reserved.
 */

package com.progremastudio.emergencymedicalteam.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.progremastudio.emergencymedicalteam.AppSharedPreferences;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.FirebasePath;
import com.progremastudio.emergencymedicalteam.core.MainActivity;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.User;

public class SignInActivity extends BaseActivity {

    private static final String TAG = "sign-in-activity";

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private EditText mEmailField;

    private EditText mPasswordField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Initiate activity layout
         */
        setContentView(R.layout.activity_signin);

        /*
        Initiate firebase authentication instance
         */
        mAuth = FirebaseAuth.getInstance();

        /*
        Initiate Firebase database instance
         */
        mDatabase = FirebaseDatabase.getInstance().getReference();

        /*
        Initiate email and password edit field
         */
        mEmailField = (EditText) findViewById(R.id.email_field);
        mPasswordField = (EditText) findViewById(R.id.password_field);

        /*
        Initiate button
         */
        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        Button signUpButton = (Button) findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
        Button forgetPasswordButton = (Button) findViewById(R.id.forget_password_button);
        forgetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: to implement Forget Password procedure
                Toast.makeText(SignInActivity.this, "To be implemented...",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            onAuthSuccess();
        }
    }

    /**
     * Action to do if User Authentication is success
     */
    private void onAuthSuccess() {

        /*
        Create listener object
         */
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                /*
                Get user object
                 */
                User user = dataSnapshot.getValue(User.class);

                /*
                Log user details information
                 */
                Log.d(TAG, "user.displayName = " + user.displayName);
                Log.d(TAG, "user.email = " + user.email);
                Log.d(TAG, "user.phoneNumber = " + user.phoneNumber);

                /*
                Store current user details to shared-preference
                 */
                AppSharedPreferences.storeUserInformation(
                        SignInActivity.this,
                        getUid(),
                        user.displayName,
                        user.email,
                        user.phoneNumber,
                        user.pictureUrl
                );

                /*
                Go to main activity and close this activity
                 */
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                /*
                Getting data failed, log a message
                 */
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        };

        /*
        Add user listener object
         */
        mDatabase.child(FirebasePath.USERS)
                .child(getUid()).addListenerForSingleValueEvent(listener);
    }

    /**
     * Validate user input form to make sure it's following android security standard
     *
     * @return result of the checking. TRUE is okay. Otherwise, FALSE.
     */
    private boolean validateForm() {

        /*
        Initiate checking result
         */
        boolean result = true;

        /*
        Check email field
         */
        if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");
            result = false;
        } else {
            mEmailField.setError(null);
        }

        /*
        Check password field
         */
        if (TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mPasswordField.setError("Required");
            result = false;
        } else {
            mPasswordField.setError(null);
        }

        /*
        TODO: to add more comprehensive password checking
         */

        /*
        Return checking result
         */
        return result;
    }

    /**
     * User Sign-In process
     */
    private void signIn() {

        /*
        Validate user input
         */
        if (!validateForm()) {
            return;
        }

        /*
        Show progress dialog
         */
        showProgressDialog();

        /*
        Get email and password
         */
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        /*
        Sign-in with email and password
         */
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        /*
                        Sign-In is completed and hide progress dialog
                         */
                        Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        if (task.isSuccessful()) {
                            /*
                            Sign-In success.
                             */
                            onAuthSuccess();
                        } else {
                            /*
                            Sign-In failed
                             */
                            Toast.makeText(SignInActivity.this,
                                    getString(R.string.str_Sign_In_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * If user need to sign up. Go to Sign-Up page.
     */
    private void signUp() {
        startActivity(new Intent(this, SignUpActivity.class));
        finish();
    }

}
