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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.progremastudio.emergencymedicalteam.AppSharedPreferences;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.CameraActivity;
import com.progremastudio.emergencymedicalteam.FirebasePath;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.Post;
import com.progremastudio.emergencymedicalteam.models.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostEditor extends BaseActivity {

    private static final String TAG = "post-editor";

    private DatabaseReference mDatabase;

    private FirebaseStorage mStorage;

    private TextView mAddressView;

    private ImageView mImageView;

    private CircleImageView mProfileView;

    private EditText mMessageEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Initiate post editor layout
         */
        setContentView(R.layout.activity_post_editor);

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
        mImageView = (ImageView) findViewById(R.id.image_view);
        mAddressView = (TextView) findViewById(R.id.address_text_view);
        mMessageEditText = (EditText) findViewById(R.id.edit_text);
        mProfileView = (CircleImageView) findViewById(R.id.profile_image);

        /*
        Initiate button
         */
        ImageButton submitButton = (ImageButton) findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitPost();
            }
        });
        ImageButton cameraButton = (ImageButton) findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });

        /*
        Show user address
         */
        mAddressView.setText(AppSharedPreferences.getCurrentUserAddress(this));

                /*
        Show profile picture if exist
         */
        String profileUrl = AppSharedPreferences.getCurrentUserPictureUrl(this);
        if (!profileUrl.equals("No picture")) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(profileUrl);
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageReference)
                    .into(mProfileView);
        }

    }

    /**
     * Open Camera Activity after User click Open Camera button
     */
    private void openCamera() {
        startActivityForResult(new Intent(this, CameraActivity.class), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(myBitmap);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Submit Post to Firebase. Picture taken by user is sent to Firebase-Storage and remaining
     * details is sent to Firebase-RealtimeDb.
     */
    private void submitPost() {

        /*
        Get user message and check as it's required field
         */
        final String content = mMessageEditText.getText().toString();
        if (TextUtils.isEmpty(content)) {
            mMessageEditText.setError("Required");
            return;
        }

        /*
        Shows posting message to user and progress bar
         */
        Toast.makeText(this, getString(R.string.str_Posting), Toast.LENGTH_SHORT).show();
        showProgressDialog();

        /*
        Listen for data change under users path and submit the post
         */
        final String userId = getUid();
        mDatabase.child(FirebasePath.USERS).child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        /*
                        Get User object from datasnapshot
                         */
                        User user = dataSnapshot.getValue(User.class);

                        if (user == null) {
                            /*
                            Show error log if User is unexpectedly NULL
                             */
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(PostEditor.this, "Error: could not fetch user.", Toast.LENGTH_SHORT).show();

                        } else {
                            /*
                            Upload Post to Firebase and sync with other user
                             */
                            Log.d(TAG, "User:" + user.toString());
                            uploadPost(userId, content);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        hideProgressDialog();
                    }

                });
    }

    /**
     * One of the main code portion to load the message. This method loads the image taken by user,
     * and if it's success, code continue to load remaining details and use download url generated
     * by Firebase
     *
     * @param userId userId of current posting action
     * @param message message to show at post fragment
     */
    private void uploadPost(final String userId, final String message) {

        /*
        Generate random key for addressing every new post
         */
        final String key = mDatabase.child("posts").push().getKey();

        /*
        Create storage reference used by Firebase
         */
        StorageReference pictureReference = mStorage.getReference().child("post").child(key).child("accident.jpg");

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
                    writeNewPost(userId, message, downloadUrl.toString(), key);

                }
            });

        } else {
            /*
            Write remaining details of post without image exist
             */
            writeNewPost(userId, message, "No Picture", key);
        }
    }

    /**
     * Write post details to Firebase-RealtimeDb. Details are written at two location
     * "/posts/" and "/user-post/" for efficient data retrieval.
     *
     * @param userId userId of current posting action
     * @param message message to show at post fragment
     * @param downloadUrl url generated by Firebase-Storage for post image reference
     * @param key random generated key for addressing every new post
     */
    private void writeNewPost(String userId, String message, String downloadUrl, String key) {

        /*
        Prepare local data for Post object creation
         */
        String displayName = AppSharedPreferences.getCurrentUserDisplayName(this);
        String email = AppSharedPreferences.getCurrentUserEmail(this);
        String timestamp = currentTimestamp();
        String locationCoordinate = AppSharedPreferences.getCurrentUserAddress(this);
        String emergencyType = "Kecelakaan"; // todo: to have list option
        String phoneNumber = AppSharedPreferences.getCurrentUserPhoneNumber(this);
        String profileUrl = AppSharedPreferences.getCurrentUserPictureUrl(this);

        /*
        Create new Post object
         */
        Post post = new Post(
                userId,
                displayName,
                email,
                timestamp,
                locationCoordinate,
                message,
                downloadUrl,
                emergencyType,
                phoneNumber,
                profileUrl
        );

        /*
        Prepare hash-map value from Post object
         */
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        /*
        Prepare data for "/posts/"
         */
        childUpdates.put("/" + FirebasePath.POSTS + "/" + key, postValues);

        /*
        Update Firebase-RealtimeDb location
         */
        mDatabase.updateChildren(childUpdates);

        /*
        Clear post after posting message
         */
        clearPost();

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
     * Clear post details after sending message
     */
    private void clearPost() {
        /*
        Clear address text
         */
        mAddressView.setText("");

        /*
        Clear message text
         */
        mMessageEditText.getText().clear();

        /*
        Show picture if image is exist
         */
        File directoryPath = new File(getFilesDir(), "post");
        File filePath = new File(directoryPath.getPath() + File.separator + "accident.jpg");
        if (filePath.exists()) {
            filePath.delete();
            mImageView.setVisibility(View.GONE);
        }

    }

}
