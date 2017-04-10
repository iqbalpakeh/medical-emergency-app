package com.progremastudio.emergencymedicalteam.core;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.progremastudio.emergencymedicalteam.AppSharedPreferences;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.CameraActivity;
import com.progremastudio.emergencymedicalteam.FirebasePath;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileEditor extends BaseActivity {

    private static String TAG = "profile-editor";

    private final int INTENT_GALLERY = 0;

    private final int INTENT_CAMERA = 1;

    private DatabaseReference mDatabase;

    private FirebaseStorage mStorage;

    private CircleImageView mImageView;

    private EditText mDisplayName;

    private EditText mEmail;

    private EditText mPhoneNumber;

    private Uri mPictureUri;

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
                openCameraApps();
            }
        });
        Button updateButton = (Button) findViewById(R.id.update_button);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserCredential();
            }
        });
        /* todo: there's bug in exif orientation if pic taken from gallery. This feature is hidden for now
        ImageButton galleryButton = (ImageButton) findViewById(R.id.gallery_button);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGalleryApps();
            }
        });
        */

        /*
        Show current profile information
         */
        mDisplayName.setHint(AppSharedPreferences.getCurrentUserDisplayName(this));
        mEmail.setHint(AppSharedPreferences.getCurrentUserEmail(this));
        mPhoneNumber.setHint(AppSharedPreferences.getCurrentUserPhoneNumber(this));

        /*
        Check read external storage permission
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
            ActivityCompat.requestPermissions(this, permissions, 0);
        }

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

    /**
     * Take picture by using camera
     */
    private void openCameraApps() {
        startActivityForResult(new Intent(this, CameraActivity.class), INTENT_CAMERA);
    }

    /**
     * Take picture by picking from gallery apps
     */
    private void openGalleryApps() {
        /*
        Create file save location
         */
        File directoryPath = new File(this.getFilesDir(), "post");
        if (!directoryPath.exists()) {
            directoryPath.mkdirs();
        }
        File filePath = new File(directoryPath.getPath() + File.separator + "accident.jpg");
        mPictureUri = Uri.parse(filePath.getPath());

        /*
        Call action pick intent
         */
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri);
            startActivityForResult(intent, INTENT_GALLERY);
        } catch (ActivityNotFoundException error) {
            Log.e(TAG, error.getStackTrace().toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INTENT_GALLERY:
                showGalleryImage(data);
                break;
            case INTENT_CAMERA:
                showCameraImage(data);
                break;
        }
    }

    /**
     * Show image selected by gallery
     *
     * @param intent picture file
     */
    private void showGalleryImage(Intent intent) {
        try {

            OutputStream fOut;
            File directoryPath;
            File filePath;

            /*
            Get bitmap from intent
             */
            Bitmap myBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), intent.getData());

            /*
            Get bitmap rotation
             */
            directoryPath = new File(this.getFilesDir(), "post");
            if (!directoryPath.exists()) {
                directoryPath.mkdirs();
            }
            filePath = new File(directoryPath.getPath() + File.separator + "accident.jpg");

            ExifInterface exif = new ExifInterface(filePath.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Log.d(TAG, "Picture orientation: " + orientation);

            /*
            Prepare the matrix for bitmap rotation
             */
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }

            /*
            Rotate the bitmap
             */
            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);

            /*
            Save bitmap after rotated
            */
            fOut = new FileOutputStream(filePath);
            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            /*
            Show bitmap to image view
             */
            mImageView.setImageBitmap(myBitmap);
        } catch (Exception exception) {
            Log.e(TAG, exception.getStackTrace().toString());
        }
    }

    /**
     * Show image taken by camera
     */
    private void showCameraImage(Intent data) {
        /*
        Get access to picture file
         */
        File directoryPath = new File(this.getFilesDir(), "post");
        File filePath = new File(directoryPath.getPath() + File.separator + "accident.jpg");

        /*
        Shows nothing if picture is not exist
         */
        if (!filePath.exists()) {
            return;
        }

        try {
            /*
            Get picture bitmap
             */
            Bitmap myBitmap = BitmapFactory.decodeFile(filePath.getAbsolutePath());

            ExifInterface exif = new ExifInterface(filePath.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Log.d(TAG, "Picture orientation: " + orientation);

            /*
            Prepare the matrix for bitmap rotation
             */
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }

            /*
            Rotate the bitmap
             */
            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);

            /*
            Save bitmap after rotated
            */
            OutputStream fOut = new FileOutputStream(filePath);
            myBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            /*
            Present the picture
             */
            mImageView.setImageBitmap(myBitmap);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
