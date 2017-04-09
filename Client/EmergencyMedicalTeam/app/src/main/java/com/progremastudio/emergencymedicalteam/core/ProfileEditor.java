package com.progremastudio.emergencymedicalteam.core;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.progremastudio.emergencymedicalteam.AppSharedPreferences;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.FirebasePath;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.User;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileEditor extends BaseActivity {

    private static String TAG = "profile-editor";

    private DatabaseReference mDatabase;

    private FirebaseStorage mStorage;

    private CircleImageView mImageView;

    private EditText mDisplayName;

    private EditText mEmail;

    private EditText mPhoneNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Initiate layout
         */
        setContentView(R.layout.activity_profile_editor);

        /*
        Initialize Firebase Real-time DB reference
         */
        mDatabase = FirebaseDatabase.getInstance().getReference();

        /*
        Initialize Firebase Storage reference
         */
        mStorage = FirebaseStorage.getInstance();

        /*
        Initiate widget
         */
        mImageView = (CircleImageView) findViewById(R.id.profile_image);
        mDisplayName = (EditText) findViewById(R.id.display_name_field);
        mEmail = (EditText) findViewById(R.id.email_field);
        mPhoneNumber = (EditText) findViewById(R.id.phone_number_field);

        /*
        Initiate button
         */
        ImageButton cameraButton = (ImageButton) findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        Button updateButton = (Button) findViewById(R.id.update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserCredential();
            }
        });

        /*
        Show current profile information
         */
        mDisplayName.setHint(AppSharedPreferences.getCurrentUserDisplayName(this));
        mEmail.setHint(AppSharedPreferences.getCurrentUserEmail(this));
        mPhoneNumber.setHint(AppSharedPreferences.getCurrentUserPhoneNumber(this));

    }

    /**
     * Update user new credential information and protected by password
     */
    private void updateUserCredential() {

        /*
        Prepare local data for Post object creation
         */
        String displayName = mDisplayName.getText().toString();
        String email = mEmail.getText().toString();
        String phoneNumber = mPhoneNumber.getText().toString();
        String pictureUrl = ""; //todo: implement picture url

        /*
        Create new User object
         */
        User user = new User(getUid(), displayName, email, phoneNumber, pictureUrl);

        /*
        Prepare hash-map value from user object
         */
        Map<String, Object> userValues = user.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        /*
        Update data in FB
         */
        childUpdates.put("/" + FirebasePath.USERS, userValues);

    }

}
