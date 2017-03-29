package com.progremastudio.emergencymedicalteam.core;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.progremastudio.emergencymedicalteam.AddressService;
import com.progremastudio.emergencymedicalteam.AppContext;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.R;

public class LocationFragment extends Fragment implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "location-fragment";

    private final int PERMISSION_FINE_LOCATION_REQUEST = 0;

    private DatabaseReference mDatabase;

    private FirebaseStorage mStorage;

    private GoogleMap mGoogleMap;

    private MapView mMapView;

    private GoogleApiClient mGoogleApiClient;

    private AddressResultReceiver mResultReceiver;

    private Location mLastLocationCoordinate;

    private String mLastLocationAddress;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /*
        Initiate fragment layout
         */
        View rootView = inflater.inflate(R.layout.fragment_location, container, false);

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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, getString(R.string.str_Connection_failed) + connectionResult.getErrorCode());
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
     * Start Address Provider Service (AddressService.java) to get name of the address of user location
     * by using Latitude Longitude information.
     */
    protected void startAddressProviderService() {
        Intent intent = new Intent(getActivity(), AddressService.class);
        intent.putExtra(AddressService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(AddressService.Constants.LOCATION_DATA_EXTRA, mLastLocationCoordinate);
        getActivity().startService(intent);
    }

    /**
     * Fetch user address. Default value is Medan City, next request value return user last address
     *
     * @return location coordinate
     */
    private LatLng fetchCurrentLocation() {

        String latitude = AppContext.fetchCurrentUserLastLatitudeLocation(getActivity());
        String longitude = AppContext.fetchCurrentUserLastLongitudeLocation(getActivity());

        Log.d(TAG, "Latitude = " + latitude);
        Log.d(TAG, "Longitude = " + longitude);

        return new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
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

            AppContext.storeCurrentUserLastLatitudeLocation(getActivity(), latitude);
            AppContext.storeCurrentUserLastLongitudeLocation(getActivity(), longitude);

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
            Log.d(TAG, "Address = " + mLastLocationAddress);

            /*
            Show current location address
            //TODO: implement the view
             */
            //mAddressTextView.setText(mLastLocationAddress);


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
