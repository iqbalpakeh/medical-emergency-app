package com.progremastudio.emergencymedicalteam.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.progremastudio.emergencymedicalteam.AppContext;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.CameraActivity;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.Post;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DashboardFragment extends Fragment implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "dashboard-fragment";

    private final int PERMISSION_FINE_LOCATION_REQUEST = 0;

    private DatabaseReference mDatabase;

    private FirebaseStorage mStorage;

    private GoogleMap mGoogleMap;

    private MapView mMapView;

    private GoogleApiClient mGoogleApiClient;

    private AddressResultReceiver mResultReceiver;

    private Location mLastLocationCoordinate;

    private String mLastLocationAddress;

    private EditText mEditText;

    private ImageButton mSubmitButton;

    private ImageButton mCameraButton;

    private ImageView mImageView;

    private TextView mAddressTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        /**
         * Initialize Firebase Real-time DB reference
         */
        mDatabase = FirebaseDatabase.getInstance().getReference();

        /**
         * Initialize Firebase Storage reference
         */
        mStorage = FirebaseStorage.getInstance();

        /**
         * Initialize Google-Map API
         */
        buildGoogleApiClient();
        mMapView = (MapView) rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        /**
         * Initialize Location Address Service
         */
        mResultReceiver = new AddressResultReceiver(new Handler());

        /**
         * Initialize Widget
         */
        mAddressTextView = (TextView) rootView.findViewById(R.id.address_text);
        mImageView = (ImageView) rootView.findViewById(R.id.image_view);
        mEditText = (EditText) rootView.findViewById(R.id.edit_text);
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

        /**
         * Show picture taken by user
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

    private void showImageView() {

        File directoryPath = new File(getActivity().getFilesDir(), "post");
        File filePath = new File(directoryPath.getPath() + File.separator + "accident.jpg");

        if (!filePath.exists()) {
            // Shows nothing if picture is not exist
            return;
        }

        try {

            Bitmap myBitmap = BitmapFactory.decodeFile(filePath.getAbsolutePath());
            ExifInterface exif = new ExifInterface(filePath.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Log.d(TAG, "Picture orientation: " + orientation);

            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }

            // rotating bitmap
            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);

            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(myBitmap);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void openCamera() {
        startActivity(new Intent(getContext(), CameraActivity.class));
    }

    private void fetchLocationAddress() {
        if (mGoogleApiClient.isConnected() && mLastLocationCoordinate != null) {
            startAddressProviderService();
        }
        ((BaseActivity) getActivity()).showProgressDialog();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION_REQUEST);
        } else if (mGoogleMap != null) {
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    protected void startAddressProviderService() {
        Intent intent = new Intent(getActivity(), AddressService.class);
        intent.putExtra(AddressService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(AddressService.Constants.LOCATION_DATA_EXTRA, mLastLocationCoordinate);
        getActivity().startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERMISSION_FINE_LOCATION_REQUEST) {
            return;
        }
        enableMyLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        LatLng currentLocation = fetchCurrentLocation();
        mGoogleMap = googleMap;
        mGoogleMap.addMarker(new MarkerOptions().position(currentLocation).title("TBM APPS User location"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                fetchLocationAddress();
                return false;
            }
        });

        enableMyLocation();
    }

    private LatLng fetchCurrentLocation() {

        String latitude = AppContext.fetchCurrentUserLastLatitudeLocation(getActivity());
        String longitude = AppContext.fetchCurrentUserLastLongitudeLocation(getActivity());

        Log.d(TAG, "Latitude = " + latitude);
        Log.d(TAG, "Longitude = " + longitude);

        return new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mLastLocationCoordinate = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocationCoordinate != null) {

            String latitude = String.valueOf(mLastLocationCoordinate.getLatitude());
            String longitude = String.valueOf(mLastLocationCoordinate.getLatitude());

            AppContext.storeCurrentUserLastLatitudeLocation(getActivity(), latitude);
            AppContext.storeCurrentUserLastLongitudeLocation(getActivity(), longitude);

            Log.d(TAG, "Latitude = " + latitude);
            Log.d(TAG, "Longitude = " + longitude);

            if (!Geocoder.isPresent()) {
                Toast.makeText(getActivity(), "No Geocoder available", Toast.LENGTH_LONG).show();
                return;
            }

            startAddressProviderService();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    private void clearPost() {

        // clear location
        mAddressTextView.setText("");

        // clear message
        mEditText.getText().clear();

        // clear image
        File directoryPath = new File(getActivity().getFilesDir(), "post");
        File filePath = new File(directoryPath.getPath() + File.separator + "accident.jpg");
        if (filePath.exists()) {
            filePath.delete();
            mImageView.setVisibility(View.GONE);
        }

    }

    private void submitPost() {

        final String content = mEditText.getText().toString();

        if (TextUtils.isEmpty(content)) {
            mEditText.setError("Required");
            return;
        }

        Toast.makeText(getActivity(), "Posting...", Toast.LENGTH_SHORT).show();
        ((BaseActivity) getActivity()).showProgressDialog();

        final String userId = ((BaseActivity) getActivity()).getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Post post = dataSnapshot.getValue(Post.class);
                        if (post == null) {

                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(getActivity(), "Error: could not fetch user.", Toast.LENGTH_SHORT).show();

                        } else {
                            /**
                             * Upload Post to Firebase and sync with other user
                             */
                            uploadPost(userId, content);
                        }

                        ((BaseActivity) getActivity()).hideProgressDialog();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        ((BaseActivity) getActivity()).hideProgressDialog();
                    }

                });
    }

    private void uploadPost(final String userId, final String message) {

        final String key = mDatabase.child("posts").push().getKey();

        StorageReference pictureReference = mStorage.getReference().child("post").child(key).child("accident.jpg");

        File directoryPath = new File(getActivity().getFilesDir(), "post");
        File filePath = new File(directoryPath.getPath() + File.separator + "accident.jpg");

        if(filePath.exists()) {

            Bitmap bitmap = BitmapFactory.decodeFile(filePath.getAbsolutePath());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] data = stream.toByteArray();

            UploadTask uploadTask = pictureReference.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d(TAG, "Upload fail");
                }

            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Log.d(TAG, "donwload Url " + downloadUrl.toString());

                    writeNewPost(userId, message, downloadUrl.toString(), key);

                }
            });

        } else {

            writeNewPost(userId, message, "No Picture", key);

        }

    }

    private void writeNewPost(String userId, String message, String downloadUrl, String key) {

        String displayName = ((BaseActivity) getActivity()).getDisplayName();
        String email = ((BaseActivity) getActivity()).getUserEmail();
        String timestamp = ((BaseActivity) getActivity()).currentTimestamp();
        String locationCoordinate = mLastLocationAddress;
        String pictureUrl = downloadUrl;
        String emergencyType = "#Kecelakaan"; // todo: to have list option

        Post post = new Post(
                userId,
                displayName,
                email,
                timestamp,
                locationCoordinate,
                message,
                pictureUrl,
                emergencyType);

        Map<String, Object> postValues = post.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);

        clearPost();
    }

    private class AddressResultReceiver extends ResultReceiver {

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            String address = resultData.getString(AddressService.Constants.RESULT_DATA_KEY);
            mLastLocationAddress = address.replace("\n", ", ");
            mAddressTextView.setText(mLastLocationAddress);

            Log.d(TAG, "Address = " + mLastLocationAddress);

            if (resultCode == AddressService.Constants.SUCCESS_RESULT) {
                ((BaseActivity) getActivity()).hideProgressDialog();
                Log.d(TAG, "Address found");
            }
        }
    }

}
