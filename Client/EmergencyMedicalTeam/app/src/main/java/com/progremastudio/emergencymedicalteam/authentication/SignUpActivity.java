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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.progremastudio.emergencymedicalteam.AppContext;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.FirebasePath;
import com.progremastudio.emergencymedicalteam.MainActivity;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.User;

public class SignUpActivity extends BaseActivity {

    private static final String TAG = "sign-up-activity";

    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;

    private EditText mEmailField;

    private EditText mPasswordField;

    private EditText mDisplayNameField;

    private EditText mPhoneNumberField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Initiate activity layout
         */
        setContentView(R.layout.activity_signup);

        /*
        Initiate firebase database instance
         */
        mDatabase = FirebaseDatabase.getInstance().getReference();

        /*
        Initiate firebase authentication instance
         */
        mAuth = FirebaseAuth.getInstance();

        /*
        Initiate user input field
         */
        mEmailField = (EditText) findViewById(R.id.email_field);
        mPasswordField = (EditText) findViewById(R.id.password_field);
        mDisplayNameField = (EditText) findViewById(R.id.display_name_field);
        mPhoneNumberField = (EditText) findViewById(R.id.phone_number_field);

        /*
        Initiate button
         */
        Button signUpButton = (Button) findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

    }


    /**
     * User Sign-up Process
     */
    private void signUp() {

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
        Sign-up with email and password
         */
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        /*
                        Sign-up completes and hide progress dialog
                         */
                        Log.d(TAG, "createUser:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        if (task.isSuccessful()) {
                            /*
                            Sign-up success
                             */
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            /*
                            Sign-up failed
                             */
                            Toast.makeText(SignUpActivity.this,
                                    getString(R.string.str_Sign_Up_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
        Check name field
         */
        if (TextUtils.isEmpty(mDisplayNameField.getText().toString())) {
            mDisplayNameField.setError("Required");
            result = false;
        } else {
            mEmailField.setError(null);
        }

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
        Check phone number field
         */
        if (TextUtils.isEmpty(mPhoneNumberField.getText().toString())) {
            mPhoneNumberField.setError("Required");
            result = false;
        } else {
            mPhoneNumberField.setError(null);
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
     * Action to do if User Authentication is success
     */
    private void onAuthSuccess(FirebaseUser firebaseUser) {

        /*
        Get user email
         */
        String email = firebaseUser.getEmail();

        /*
        Get uid
         */
        String userId = firebaseUser.getUid();

        /*
        Get display name
         */
        String displayName = mDisplayNameField.getText().toString();

        /*
        Get phone number
         */
        String phoneNumber = mPhoneNumberField.getText().toString();

        /*
        Store current user details to shared-preference
         */
        AppContext.storeCurrentUser(
                this,
                displayName,
                email,
                phoneNumber
        );

        /*
        Create user object in database
         */
        User user = new User(
                displayName,
                email,
                phoneNumber
        );
        mDatabase.child(FirebasePath.USERS).child(userId).setValue(user.toMap());

        /*
        Go to main activity and close this activity
         */
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*
        Go back to Sign-In activity
         */
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }
}
