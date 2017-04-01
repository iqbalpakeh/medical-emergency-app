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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
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
import com.progremastudio.emergencymedicalteam.AddressService;
import com.progremastudio.emergencymedicalteam.AppSharedPreferences;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.R;

public class LocationFragment extends Fragment implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "location-fragment";

    private static final int PERMISSION_TO_START_ADDRESS_SERVICE = 1;

    private static final int PERMISSION_TO_ENABLE_GMAP_LOCATION = 2;

    private GoogleMap mGoogleMap;

    private MapView mMapView;

    private GoogleApiClient mGoogleApiClient;

    private AddressResultReceiver mResultReceiver;

    private Location mLastLocationCoordinate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /*
        Initiate fragment layout
         */
        View rootView = inflater.inflate(R.layout.fragment_location, container, false);

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
        Initialize search view
         */
        FloatingSearchView searchView = (FloatingSearchView) rootView.findViewById(R.id.floating_search_view);
        searchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.search_my_location) {
                    fetchLocationAddress();
                    moveCameraToCurrentLocation();
                } else if (item.getItemId() == R.id.search_local_hospital) {
                    showTBMLocation();
                }
            }
        });

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
        if ((ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            String[] permissions = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            requestPermissions(permissions, PERMISSION_TO_START_ADDRESS_SERVICE);
            return;
        }
        requestLocationAddress();
    }

    /**
     * Request location service from Address Provider Services
     */
    private void requestLocationAddress() {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_TO_START_ADDRESS_SERVICE) {
            requestLocationAddress();
        } else if (requestCode == PERMISSION_TO_ENABLE_GMAP_LOCATION) {
            enableGoogleMapLocation();
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
        mGoogleMap.addMarker(new MarkerOptions().position(currentLocation).title("TBM APPS User location").snippet("Test"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mGoogleMap.setMinZoomPreference(13.0f);
        mGoogleMap.setMaxZoomPreference(20.0f);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);

        /*
        Enable user location feature
         */
        enableMyLocation();

        /*
        Move camera
         */
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

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

        Log.d(TAG, "start address provider service");

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

        String latitude = AppSharedPreferences.fetchCurrentUserLastLatitudeLocation(getActivity());
        String longitude = AppSharedPreferences.fetchCurrentUserLastLongitudeLocation(getActivity());

        Log.d(TAG, "Latitude = " + latitude);
        Log.d(TAG, "Longitude = " + longitude);

        return new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
    }

    /**
     * Show TBM location. Location is FK UISU Medan
     */
    private void showTBMLocation() {

        /*
        FKUISU coordinate location based on GOOGLE MAP
         */
        String latitude = "3.580790";
        String longitude = "98.685101";

        LatLng currentLocation = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

        /*
        Move camera
         */
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

        /*
        Add marker
         */
        mGoogleMap.addMarker(new MarkerOptions().position(currentLocation).title("TBM FK UISU Medan").snippet("Lokasi ambulance"));
    }

    /**
     * Fetch Latitude and Longitude value of user location. Default location is Medan City,
     * and then show user last location.
     */
    private void fetchLocationAddress() {
        /*
        Start to show progress dialog
         */
        ((BaseActivity) getActivity()).showProgressDialog();

        /*
        Fetch location coordinate from Google Map Api
         */
        mLastLocationCoordinate = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

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

    /**
     * Move camera to current user location
     */
    private void moveCameraToCurrentLocation() {
        /*
        Fetch current user location
         */
        LatLng currentLocation = fetchCurrentLocation();

        /*
        Move camera
         */
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
    }

    /**
     * Enable the functionality of User Location by Google Map
     */
    private void enableMyLocation() {
        /*
        Check location permission and request for first time user
         */
        if ((ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            String[] permissions = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            requestPermissions(permissions, PERMISSION_TO_ENABLE_GMAP_LOCATION);
            return;
        }
        enableGoogleMapLocation();
    }

    /**
     * Enable google map location
     */
    private void enableGoogleMapLocation() {
        if (mGoogleMap != null) {
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
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
            String lastLocationAddress = address.replace("\n", ", ");
            Log.d(TAG, "Address = " + lastLocationAddress);

            /*
            Store current address
             */
            AppSharedPreferences.storeCurrentUserLastAddress(getContext(), lastLocationAddress);

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
