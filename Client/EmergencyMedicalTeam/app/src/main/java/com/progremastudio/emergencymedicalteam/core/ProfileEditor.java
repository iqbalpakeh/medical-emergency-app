package com.progremastudio.emergencymedicalteam.core;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.progremastudio.emergencymedicalteam.AppSharedPreferences;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.CameraActivity;
import com.progremastudio.emergencymedicalteam.FirebasePath;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileEditor extends BaseActivity {

    private static String TAG = "profile-editor";

    private final int INTENT_CAMERA = 0;

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
        Shows message to user and progress bar
         */
        Toast.makeText(this, getString(R.string.str_Update_profile), Toast.LENGTH_SHORT).show();
        showProgressDialog();

        /*
        Create storage reference used by Firebase
         */
        StorageReference pictureReference = mStorage.getReference().child(FirebasePath.USERS).child(getUid()).child("profile_picture.jpg");

        /*
        Create access to posting image and check if it's exist
         */
        File directoryPath = new File(getFilesDir(), "post");
        File filePath = new File(directoryPath.getPath() + File.separator + "accident.jpg");

        /*
        Check image file existency
         */
        if(filePath.exists()) {

            /*
            Create bitmap for image posting
             */
            Bitmap bitmap = BitmapFactory.decodeFile(filePath.getAbsolutePath());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] data = stream.toByteArray();

            /*
            Upload image to Firebase-Storage server
             */
            UploadTask uploadTask = pictureReference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    /*
                    Handle unsuccessful uploads
                     */
                    Log.d(TAG, "Upload fail");
                }

            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    /*
                    taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                     */
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Log.d(TAG, "donwload Url " + downloadUrl.toString());

                    /*
                    Write remaining details of post with image exist
                     */
                    updateUserCredential(downloadUrl.toString());

                }
            });

        } else {
            /*
            Update user credential without picture
             */
            updateUserCredential("No Picture");
        }
    }

    /**
     * Update user new credential information and protected by password
     *
     * @param pictureUrl reference of picture download url from Firebase storage
     */
    private void updateUserCredential(String pictureUrl) {
        /*
        Prepare local data for Post object creation
         */
        String displayName = mDisplayName.getText().toString();
        String email = mEmail.getText().toString();
        String phoneNumber = mPhoneNumber.getText().toString();

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
        Prepare data for /USERS/#uid#
         */
        childUpdates.put("/" + FirebasePath.USERS + "/" + getUid(), userValues);

        /*
        Update data in Firebase
         */
        mDatabase.updateChildren(childUpdates);

        /*
        Propagate to other data location in FB
         */
        //todo: implement this!

        /*
        Hide progress bar
         */
        hideProgressDialog();

        /*
        Go back to Main activity
         */
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }


    /**
     * Take picture by using camera
     */
    private void openCameraApps() {
        startActivityForResult(new Intent(this, CameraActivity.class), INTENT_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case INTENT_CAMERA:
                showCameraImage();
                break;
        }
    }

    /**
     * Show image taken by camera
     */
    private void showCameraImage() {
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
