package com.progremastudio.emergencymedicalteam.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.progremastudio.emergencymedicalteam.AddressService;
import com.progremastudio.emergencymedicalteam.AppContext;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.CameraActivityTbd;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.Post;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DashboardFragment extends Fragment implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "dashboard-fragment";

    private static final String ADDRESS_REQUESTED_KEY = "address-request-key";

    private static final String LOCATION_ADDRESS_KEY = "location-address-key";

    private static final String DEFAULT_EMERGENCY_TYPE = "Kecelakaan";

    private final int PERMISSION_FINE_LOCATION_REQUEST = 0;

    private DatabaseReference mDatabase;

    private GoogleMap mGoogleMap;

    private MapView mMapView;

    private GoogleApiClient mGoogleApiClient;

    private AddressResultReceiver mResultReceiver;

    private Location mLastLocation;

    private Boolean mAddressRequested;

    private String mCurrentAddress;

    private String mEmergencyType;

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

        mDatabase = FirebaseDatabase.getInstance().getReference();

        buildGoogleApiClient();
        mAddressRequested = false;
        mCurrentAddress = "";
        mEmergencyType = DEFAULT_EMERGENCY_TYPE;
        mResultReceiver = new AddressResultReceiver(new Handler());

        mMapView = (MapView) rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

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

        mAddressTextView = (TextView) rootView.findViewById(R.id.address_text);

        mImageView = (ImageView) rootView.findViewById(R.id.image_view);
        showImageView();

        updateValuesFromBundle(savedInstanceState);

        Log.d(TAG, "Display Name = " + AppContext.fetchCurrentUserDisplayName(getContext()));

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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState");
        savedInstanceState.putBoolean(ADDRESS_REQUESTED_KEY, mAddressRequested);
        savedInstanceState.putString(LOCATION_ADDRESS_KEY, mCurrentAddress);
        super.onSaveInstanceState(savedInstanceState);
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

        try {

            File directoryPath = new File(getActivity().getFilesDir(), "post");
            File filePath = new File(directoryPath.getPath() + File.separator + "accident.jpg");
            if (filePath.exists()) {
                mImageView.setImageURI(Uri.parse("file://" + filePath.getPath()));
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    private void openCamera() {
        startActivity(new Intent(getContext(), CameraActivityTbd.class));
    }

    private void fetchLocationAddress() {
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService();
        }
        mAddressRequested = true;
        ((BaseActivity) getActivity()).showProgressDialog();
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mCurrentAddress = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                mAddressTextView.setText(mCurrentAddress);
            }
        }
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
            Log.d(TAG, "Request FINE ACCESS Permission");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION_REQUEST);
        } else if (mGoogleMap != null) {
            Log.d(TAG, "setMyLocationEnabled(true)");
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    protected void startIntentService() {
        Intent intent = new Intent(getActivity(), AddressService.class);
        intent.putExtra(AddressService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(AddressService.Constants.LOCATION_DATA_EXTRA, mLastLocation);
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

        // TODO: to improve camera zoom position
        mGoogleMap.setMinZoomPreference(14.0f);
        mGoogleMap.setMaxZoomPreference(22.0f);

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
        // TODO: Map should goes to user current location automatically
        return new LatLng(1.1252494, 104.0668836);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException exception) {
            // Access is not granted by user
            Log.e(TAG, exception.getStackTrace().toString());
        }

        if (mLastLocation != null) {

            String latitude = String.valueOf(mLastLocation.getLatitude());
            String longitude = String.valueOf(mLastLocation.getLatitude());

            AppContext.storeCurrentUserLastLatitudeLocation(getActivity(), latitude);
            AppContext.storeCurrentUserLastLongitudeLocation(getActivity(), longitude);

            Log.d(TAG, "Latitude = " + latitude);
            Log.d(TAG, "Longitude = " + longitude);

            if (!Geocoder.isPresent()) {
                Toast.makeText(getActivity(), "No Geocoder available", Toast.LENGTH_LONG).show();
                return;
            }

            if (mAddressRequested) {
                startIntentService();
            }
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

    private void submitPost() {

        final String content = mEditText.getText().toString();

        if (TextUtils.isEmpty(content)) {
            mEditText.setError("Required");
            return;
        }

        Toast.makeText(getActivity(), "Posting...", Toast.LENGTH_SHORT).show();
        ((BaseActivity)getActivity()).showProgressDialog();

        final String userId = ((BaseActivity) getActivity()).getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get text value
                        Post post = dataSnapshot.getValue(Post.class);

                        if (post == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(getActivity(),
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writeNewPost(userId, content);
                        }

                        // Finish this Activity, back to the stream
                        ((BaseActivity)getActivity()).hideProgressDialog();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        ((BaseActivity)getActivity()).hideProgressDialog();
                    }
                });
    }

    private void writeNewPost(String userId, String message) {

        // Create new post at /user-posts/$userid/$postid and
        // at /posts/$postid simultaneously

        String key = mDatabase.child("posts").push().getKey();

        String displayName = ((BaseActivity) getActivity()).getDisplayName();
        String email = ((BaseActivity) getActivity()).getUserEmail();
        String timestamp = currentTimestamp();
        String locationCoordinate = mCurrentAddress;
        String pictureUrl = "to be implemented"; //todo: get picture url
        String emergencyType = mEmergencyType; // todo: to have list option

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
    }

    private String currentTimestamp() {
        return String.valueOf(Calendar.getInstance().getTimeInMillis());
    }

    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mCurrentAddress = resultData.getString(AddressService.Constants.RESULT_DATA_KEY);
            Log.d(TAG, "Address = " + mCurrentAddress);
            mAddressTextView.setText(mCurrentAddress);
            if (resultCode == AddressService.Constants.SUCCESS_RESULT) {
                ((BaseActivity) getActivity()).hideProgressDialog();
                Log.d(TAG, "Address found");
            }
        }
    }

}
