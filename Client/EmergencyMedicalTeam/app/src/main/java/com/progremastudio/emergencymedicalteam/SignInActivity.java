package com.progremastudio.emergencymedicalteam;

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

public class SignInActivity extends BaseActivity {

    private static final String TAG = "sign-in-activity";

    private FirebaseAuth mAuth;

    private EditText mEmailField;
    private EditText mPasswordField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();

        mEmailField = (EditText) findViewById(R.id.email_field);
        mPasswordField = (EditText) findViewById(R.id.password_field);

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

        Button googleSignInButton = (Button) findViewById(R.id.google_sign_in_button);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: to implement Google Sign-In procedure
                Toast.makeText(SignInActivity.this, "To be implemented...",
                        Toast.LENGTH_SHORT).show();
            }
        });

        Button facebookSignInButton = (Button) findViewById(R.id.facebook_sign_in_button);
        facebookSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: to implement Faceboook Sign-In procedure
                Toast.makeText(SignInActivity.this, "To be implemented...",
                        Toast.LENGTH_SHORT).show();
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

    private void onAuthSuccess() {

        String displayName = ""; // TODO: retrieve display name from Firebase
        String email = mEmailField.getText().toString();
        String phoneNumber = ""; // TODO: retrieve display name from Firebase

        AppContext.storeCurrentUser(this, displayName, email, phoneNumber);

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private boolean validateForm() {

        boolean result = true;

        if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");
            result = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mPasswordField.setError("Required");
            result = false;
        } else {
            mPasswordField.setError(null);
        }

        // TODO: to add more comprehensive password checking

        return result;
    }

    private void signIn() {

        Log.d(TAG, "signIn");
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        if (task.isSuccessful()) {
                            onAuthSuccess();
                        } else {
                            Toast.makeText(SignInActivity.this, "Sign In Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signUp() {
        startActivity(new Intent(this, SignUpActivity.class));
        finish();
    }

}
