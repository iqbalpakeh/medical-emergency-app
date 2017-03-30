package com.progremastudio.emergencymedicalteam.core;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.progremastudio.emergencymedicalteam.AddressService;
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

public class DashboardFragment extends Fragment implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "dashboard-fragment";

    private final int PERMISSION_FINE_LOCATION_REQUEST = 0;

    private final int PERMISSION_CALL_PHONE = 1;

    private DatabaseReference mDatabase;

    private FirebaseStorage mStorage;

    private GoogleMap mGoogleMap;

    private MapView mMapView;

    private GoogleApiClient mGoogleApiClient;

    private AddressResultReceiver mResultReceiver;

    private Location mLastLocationCoordinate;

    private String mLastLocationAddress;

    private EditText mMessageEditText;

    private ImageButton mSubmitButton;

    private ImageButton mCameraButton;

    private Button mAmbulanceButton;

    private ImageView mImageView;

    private TextView mAddressTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        /*
        Initialize Firebase Real-time DB reference
         */
        mDatabase = FirebaseDatabase.getInstance().getReference();

        /*
        Initialize Firebase Storage reference
         */
        mStorage = FirebaseStorage.getInstance();

        /*
        Initialize Google-Map API
         */
        buildGoogleApiClient();
        mMapView = (MapView) rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        /*
        Initialize Location Address Service
         */
        mResultReceiver = new AddressResultReceiver(new Handler());

        /*
        Initialize Widget
         */
        mAddressTextView = (TextView) rootView.findViewById(R.id.address_text);
        mImageView = (ImageView) rootView.findViewById(R.id.image_view);
        mMessageEditText = (EditText) rootView.findViewById(R.id.edit_text);
        mSubmitButton = (ImageButton) rootView.findViewById(R.id.submit_code);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
        mCameraButton = (ImageButton) rootView.findViewById(R.id.camera_button);
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera();
            }
        });
        mAmbulanceButton = (Button) rootView.findViewById(R.id.call_ambulance_button);
        mAmbulanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callAmbulance();
            }
        });

        /*
        Show picture taken by user
         */
        showImageView();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory");
        mMapView.onLowMemory();
    }

    /**
     * Show image taken by user. This picture will be posted if User press sending post button.
     */
    private void showImageView() {

        /*
        Get access to picture file
         */
        File directoryPath = new File(getActivity().getFilesDir(), "post");
        File filePath = new File(directoryPath.getPath() + File.separator + "accident.jpg");

        /*
        Shows nothing if picture is not exist
         */
        if (!filePath.exists()) {
            return;
        }

        try {
            /*
            Hide map view
             */
            mMapView.setVisibility(View.GONE);

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
            Get the size of the ImageView
            */
            int targetW = mImageView.getWidth();
            int targetH = mImageView.getHeight();

            /*
            Get the size of the image
             */
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

		    /*
		    Figure out which way needs to be reduced less
		     */
            int scaleFactor = 1;
            if ((targetW > 0) || (targetH > 0)) {
                scaleFactor = Math.min(photoW/targetW, photoH/targetH);
            }

		    /*
		    Set bitmap options to scale the image decode target
		     */
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

		    /*
		    Decode the JPEG file into a Bitmap
		    */
            Bitmap bitmap = BitmapFactory.decodeFile(filePath.getAbsolutePath(), bmOptions);

            /*
            Present the picture
             */
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(bitmap);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Open Camera Activity after User click Open Camera button
     */
    private void openCamera() {
        startActivity(new Intent(getContext(), CameraActivity.class));
    }

    /**
     * Call Ambulance after User click Call Ambulance button
     */
    private void callAmbulance() {
        /*
        Check Call permission access
         */
        if(!checkCallAccess()) {
            Log.d(TAG, "No access to phone call");
            return;
        }

        /*
        Make a phone call to predefined ambulance if permission is granted
         */
        makePhoneCall();
    }

    /**
     * Make a phone call by using ACTION_CALL intent
     */
    private void makePhoneCall() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + getString(R.string.Ambulance_Phone_Number)));
        getContext().startActivity(intent);
    }

    /**
     * Check access for ACTION_CALL intent
     *
     * @return permission status. TRUE if granted. Otherwise, return FALSE
     */
    private boolean checkCallAccess() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CALL_PHONE);
            return false;
        } else  {
            return true;
        }
    }

    /**
     * Fetch Latitude and Longitude value of user location. Default location is Medan City,
     * and then show user last location.
     */
    private void fetchLocationAddress() {

        if (mGoogleApiClient.isConnected() && mLastLocationCoordinate != null) {

            /*
            Save current location coordinate for next request
             */
            String latitude = String.valueOf(mLastLocationCoordinate.getLatitude());
            String longitude = String.valueOf(mLastLocationCoordinate.getLongitude());

            Log.d(TAG, "Latitude = " + latitude);
            Log.d(TAG, "Longitude = " + longitude);

            AppSharedPreferences.storeCurrentUserLastLatitudeLocation(getActivity(), latitude);
            AppSharedPreferences.storeCurrentUserLastLongitudeLocation(getActivity(), longitude);

            /*
            Start Address Provider service
             */
            startAddressProviderService();
        }

        /*
        Start to show progress dialog
         */
        ((BaseActivity) getActivity()).showProgressDialog();
    }

    /**
     * Build Google API client used for providing user location
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Enable the functionality of User Location by Google Map
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION_REQUEST);
        } else if (mGoogleMap != null) {
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    /**
     * Start Address Provider Service (AddressService.java) to get name of the address of user location
     * by using Latitude Longitude information.
     */
    protected void startAddressProviderService() {
        Intent intent = new Intent(getActivity(), AddressService.class);
        intent.putExtra(AddressService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(AddressService.Constants.LOCATION_DATA_EXTRA, mLastLocationCoordinate);
        getActivity().startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*
        Continue enable My Location feature after User give permission
         */
        if (requestCode == PERMISSION_FINE_LOCATION_REQUEST) {
            enableMyLocation();
        }

        /*
        Continue making a phone call after User give permission
         */
        if (requestCode == PERMISSION_CALL_PHONE) {
            makePhoneCall();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        /*
        Fetch current user location
         */
        LatLng currentLocation = fetchCurrentLocation();

        /*
        Update map with retro style
         */
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.retro_style_json));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        /*
        Prepare google map object
         */
        mGoogleMap = googleMap;
        mGoogleMap.addMarker(new MarkerOptions().position(currentLocation).title("TBM APPS User location"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mGoogleMap.setMinZoomPreference(14.0f);
        mGoogleMap.setMaxZoomPreference(15.0f);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                fetchLocationAddress();
                return false;
            }
        });

        /*
        Enable user location feature
         */
        enableMyLocation();
    }

    /**
     * Fetch user address. Default value is Medan City, next request value return user last address
     *
     * @return location coordinate
     */
    private LatLng fetchCurrentLocation() {

        String latitude = AppSharedPreferences.fetchCurrentUserLastLatitudeLocation(getActivity());
        String longitude = AppSharedPreferences.fetchCurrentUserLastLongitudeLocation(getActivity());

        Log.d(TAG, "Latitude = " + latitude);
        Log.d(TAG, "Longitude = " + longitude);

        return new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        /*
        Check location permission
         */
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        /*
        Fetch location coordinate from Google Map Api
         */
        mLastLocationCoordinate = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        /*
        Start address provider service if location coordinate returned by Google Map Api
         */
        if (mLastLocationCoordinate != null) {
            if (!Geocoder.isPresent()) {
                Toast.makeText(getActivity(), getString(R.string.str_No_Geocoder_available), Toast.LENGTH_LONG).show();
                return;
            }
            startAddressProviderService();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, getString(R.string.str_Connection_suspended));
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, getString(R.string.str_Connection_failed) + result.getErrorCode());
    }

    /**
     * Clear post details after sending message
     */
    private void clearPost() {
        /*
        Show back map view
         */
        mMapView.setVisibility(View.VISIBLE);

        /*
        Clear address text
         */
        mAddressTextView.setText("");

        /*
        Clear message text
         */
        mMessageEditText.getText().clear();

        /*
        Show picture if image is exist
         */
        File directoryPath = new File(getActivity().getFilesDir(), "post");
        File filePath = new File(directoryPath.getPath() + File.separator + "accident.jpg");
        if (filePath.exists()) {
            filePath.delete();
            mImageView.setVisibility(View.GONE);
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
        Toast.makeText(getActivity(), getString(R.string.str_Posting), Toast.LENGTH_SHORT).show();
        ((BaseActivity) getActivity()).showProgressDialog();

        /*
        Listen for data change under users path and submit the post
         */
        final String userId = ((BaseActivity) getActivity()).getUid();
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
                            Toast.makeText(getActivity(), "Error: could not fetch user.", Toast.LENGTH_SHORT).show();

                        } else {
                            /*
                            Upload Post to Firebase and sync with other user
                             */
                            Log.d(TAG, "User:" + user.toString());
                            uploadPost(userId, content);
                        }

                        /*
                        Hide progress bar
                         */
                        ((BaseActivity) getActivity()).hideProgressDialog();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        ((BaseActivity) getActivity()).hideProgressDialog();
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
        File directoryPath = new File(getActivity().getFilesDir(), "post");
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
        String displayName = AppSharedPreferences.fetchCurrentUserDisplayName(getContext());
        String email = AppSharedPreferences.fetchCurrentUserEmail(getContext());
        String timestamp = ((BaseActivity) getActivity()).currentTimestamp();
        String locationCoordinate = mLastLocationAddress;
        String emergencyType = "Kecelakaan"; // todo: to have list option
        String phoneNumber = AppSharedPreferences.fetchCurrentUserPhoneNumber(getContext());

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
                phoneNumber);

        /*
        Prepare hash-map value from Post object
         */
        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        /*
        Prepare data for both "/posts/" and "/user-post/"
         */
        childUpdates.put("/" + FirebasePath.POSTS + "/" + key, postValues);
        childUpdates.put("/" + FirebasePath.USER_POSTS + "/" + userId + "/" + key, postValues);

        /*
        Update Firebase-RealtimeDb location
         */
        mDatabase.updateChildren(childUpdates);

        /*
        Clear post after posting message
         */
        clearPost();
    }

    /**
     * Receiver class for Address Fetch Service
     */
    private class AddressResultReceiver extends ResultReceiver {

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            /*
            Read "address" value return by Address Fetch Service
             */
            String address = resultData.getString(AddressService.Constants.RESULT_DATA_KEY);

            /*
            Replace \n with ,
             */
            mLastLocationAddress = address.replace("\n", ", ");
            mAddressTextView.setText(mLastLocationAddress);
            Log.d(TAG, "Address = " + mLastLocationAddress);

            /*
            Hide progress bar if service success.
             */
            if (resultCode == AddressService.Constants.SUCCESS_RESULT) {
                ((BaseActivity) getActivity()).hideProgressDialog();
                Log.d(TAG, "Address found");
            }
        }
    }

}
