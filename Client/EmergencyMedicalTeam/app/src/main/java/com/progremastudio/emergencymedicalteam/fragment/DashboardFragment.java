package com.progremastudio.emergencymedicalteam.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.models.Post;

import java.util.HashMap;
import java.util.Map;

public class DashboardFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "DashboardFragment";
    private static final String REQUIRED = "Required";

    private final int PERMISSION_FINE_LOCATION_REQUEST = 0;

    private DatabaseReference mDatabase;

    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private AddressResultReceiver mResultReceiver;
    private Boolean mAddressRequested;

    private EditText mEditText;
    private Button mSubmitButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mAddressRequested = false;

        mResultReceiver = new AddressResultReceiver(new Handler());

        mDatabase = FirebaseDatabase.getInstance().getReference();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mEditText = (EditText) rootView.findViewById(R.id.edit_text);

        mSubmitButton = (Button) rootView.findViewById(R.id.submit_code);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {

                            try {
                                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                            } catch (SecurityException exception) {
                                Log.e(TAG, exception.getStackTrace().toString());
                            }

                            if (mLastLocation != null) {
                                Log.d(TAG, "Latitude = " + String.valueOf(mLastLocation.getLatitude()));
                                Log.d(TAG, "Longitude = " + String.valueOf(mLastLocation.getLongitude()));
                            }

                            if (mAddressRequested) {
                                startIntentService();
                            }
                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
        }

        Button fetchLocationButton = (Button) rootView.findViewById(R.id.fetch_address_button);
        fetchLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Only start the service to fetch the address if GoogleApiClient is
                // connected.
                if (mGoogleApiClient.isConnected() && mLastLocation != null) {
                    startIntentService();
                }
                // If GoogleApiClient isn't connected, process the user's request by
                // setting mAddressRequested to true. Later, when GoogleApiClient connects,
                // launch the service to fetch the address. As far as the user is
                // concerned, pressing the Fetch Address button
                // immediately kicks off the process of getting the address.
                mAddressRequested = true;
                //todo: do necessary ui update here!
            }
        });

        return rootView;
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            Log.d(TAG, "Request FINE ACCESS Permission");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION_REQUEST);
        } else if (mGoogleMap != null) {
            // Access to the location has been granted to the app.
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != PERMISSION_FINE_LOCATION_REQUEST) {
            return;
        }
        // Enable the my location layer if the permission has been granted.
        enableMyLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng sydney = new LatLng(-34, 151);
        mGoogleMap = googleMap;
        mGoogleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        enableMyLocation();
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    private void submit() {

        final String content = mEditText.getText().toString();

        if (TextUtils.isEmpty(content)) {
            mEditText.setError(REQUIRED);
            return;
        }

        setEditingEnabled(false);
        Toast.makeText(getActivity(), "Posting...", Toast.LENGTH_SHORT).show();

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
                        setEditingEnabled(true);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        setEditingEnabled(true);
                    }
                });
    }

    private void setEditingEnabled(boolean enabled) {
        mEditText.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    private void writeNewPost(String userId, String message) {

        // Create new post at /user-posts/$userid/$postid and
        // at /posts/$postid simultaneously

        String key = mDatabase.child("posts").push().getKey();

        String displayName = AppContext.fetchCurrentUserDisplayName(getActivity());
        String email = AppContext.fetchCurrentUserEmail(getActivity());
        String timestamp = "to be implemented"; //todo: get timestamp
        String locationCoordinate = "to be implemented"; //todo: get location coordinate
        String pictureUrl = "to be implemented"; //todo: get picture url
        String emergencyType = "to be implemented"; //todo: get emergency type

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

    class AddressResultReceiver extends ResultReceiver {

        private String mAddressOutput;

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(AddressService.Constants.RESULT_DATA_KEY);
            Log.d(TAG, "Address = " + mAddressOutput);

            // Show a toast message if an address was found.
            if (resultCode == AddressService.Constants.SUCCESS_RESULT) {
                Log.d(TAG, "Address found");
            }

        }
    }

}
