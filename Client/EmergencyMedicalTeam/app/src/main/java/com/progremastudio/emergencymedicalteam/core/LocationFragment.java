package com.progremastudio.emergencymedicalteam.core;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.Util;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.progremastudio.emergencymedicalteam.AddressService;
import com.progremastudio.emergencymedicalteam.AppSharedPreferences;
import com.progremastudio.emergencymedicalteam.BaseActivity;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.locationsearch.SearchResultsListAdapter;
import com.progremastudio.emergencymedicalteam.locationsearch.data.ColorSuggestion;
import com.progremastudio.emergencymedicalteam.locationsearch.data.ColorWrapper;
import com.progremastudio.emergencymedicalteam.locationsearch.data.DataHelper;

import java.util.ArrayList;
import java.util.List;

import static com.progremastudio.emergencymedicalteam.R.id.map;

public class LocationFragment extends Fragment implements RoutingListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "location-fragment";

    private static final int PERMISSION_TO_START_ADDRESS_SERVICE = 1;

    private static final int PERMISSION_TO_ENABLE_GMAP_LOCATION = 2;

    private static final int MAP_ANIMATION_DURATION = 1000;

    private static final long FIND_SUGGESTION_SIMULATED_DELAY = 250;

    private static final int[] COLORS = new int[]{
            R.color.primary_dark,
            R.color.primary,
            R.color.primary_light,
            R.color.accent,
            R.color.primary_dark_material_light
    };

    private GoogleMap mGoogleMap;

    private MapView mMapView;

    private GoogleApiClient mGoogleApiClient;

    private AddressResultReceiver mResultReceiver;

    private Location mLastLocationCoordinate;

    private Marker mUserMarker;

    private Marker mTBMMarker;

    private List<Polyline> mPolylines;

    private FloatingSearchView mSearchView;

    private RecyclerView mSearchResultsList;

    private SearchResultsListAdapter mSearchResultsAdapter;

    private String mLastQuery = "";

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
        mMapView = (MapView) rootView.findViewById(map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        /*
        Initiate polyline used for drawing track
         */
        mPolylines = new ArrayList<>();

        /*
        Initialize Location Address Service
         */
        mResultReceiver = new AddressResultReceiver(new Handler());

        /*
        Initialize search view
         */
        mSearchResultsList = (RecyclerView) rootView.findViewById(R.id.search_results_list);
        mSearchView = (FloatingSearchView) rootView.findViewById(R.id.floating_search_view);
        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.search_my_location) {
                    /*
                    Show user location
                     */
                    showUserLocation();
                } else if (item.getItemId() == R.id.search_local_hospital) {
                    /*
                    Show TBM location
                     */
                    showTBMLocation();
                }
            }
        });

        setupFloatingSearch();
        setupResultsList();

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
            /*
            Request permission for the first time
             */
            String[] permissions = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
            requestPermissions(permissions, PERMISSION_TO_START_ADDRESS_SERVICE);
            return;
        }
        requestLocationAddress();
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
//        try {
//            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
//                    getContext(), R.raw.retro_style_json));
//            if (!success) {
//                Log.e(TAG, "Style parsing failed.");
//            }
//        } catch (Resources.NotFoundException e) {
//            Log.e(TAG, "Can't find style. Error: ", e);
//        }

        /*
        Prepare google map object
         */
        mGoogleMap = googleMap;
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
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(16), MAP_ANIMATION_DURATION, null);

        /*
        Add marker
         */
        if (mUserMarker != null) {
            mUserMarker.remove();
        }
        mUserMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title(AppSharedPreferences.fetchCurrentUserDisplayName(getContext())));
        mUserMarker.showInfoWindow();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        /*
        The Routing request failed
         */
        ((BaseActivity) getActivity()).hideProgressDialog();
        if (e != null) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        /*
        Move camera
         */
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(fetchCurrentLocation()));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(16), MAP_ANIMATION_DURATION, null);

        /*
        Clear polyline if exist
         */
        clearPolylines();

        /*
        Add routes to the map
         */
        String distance = "";
        String duration = "";

        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mGoogleMap.addPolyline(polyOptions);
            mPolylines.add(polyline);

            Log.d(TAG, "Route " + (i + 1)
                    + ": distance = " + route.get(i).getDistanceText()
                    + ": duration = " + route.get(i).getDurationText());

            /*
            Todo: handle generic cases
             */
            distance = route.get(i).getDistanceText();
            duration = route.get(i).getDurationText();

        }

        /*
        Add marker
         */
        if (mUserMarker != null) {
            mUserMarker.remove();
        }
        mUserMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(fetchCurrentLocation())
                .title(AppSharedPreferences.fetchCurrentUserDisplayName(getContext()))
                .snippet("jarak " + distance + ", waktu " + duration));
        mUserMarker.showInfoWindow();
    }

    @Override
    public void onRoutingCancelled() {

    }

    /**
     * Clean polylines from map
     */
    private void clearPolylines() {
        if (mPolylines.size() > 0) {
            for (Polyline poly : mPolylines) {
                poly.remove();
            }
        }
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
     * Shows current user location
     */
    private void showUserLocation() {
        clearPolylines();
        fetchLocationAddress();
        moveCameraToCurrentLocation();
    }

    /**
     * Show TBM location. Location is FK UISU Medan
     */
    private void showTBMLocation() {

        /*
        FKUISU coordinate location based on GOOGLE MAP
         */

        //Debug code
//        String latitude = "3.580790";
//        String longitude = "98.685101";

        String latitude = "1.130368";
        String longitude = "104.055226";

        LatLng currentLocation = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

        /*
        Move camera
         */
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(16), MAP_ANIMATION_DURATION, null);

        /*
        Add marker
         */
        if (mTBMMarker == null) {
            mTBMMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(currentLocation)
                    .title("TBM FK UISU Medan"));
            mTBMMarker.showInfoWindow();
        }

        /*
        Execute routing
         */
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(fetchCurrentLocation(), currentLocation)
                .build();
        routing.execute();
    }

    /**
     * Fetch Latitude and Longitude value of user location. Default location is Medan City,
     * and then show user last location.
     */
    private void fetchLocationAddress() {
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
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(16), MAP_ANIMATION_DURATION, null);

        /*
        Add marker
         */
        if (mUserMarker != null) {
            mUserMarker.remove();
        }
        mUserMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title(AppSharedPreferences.fetchCurrentUserDisplayName(getContext())));
        mUserMarker.showInfoWindow();
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
            /*
            Request permission for the first time
             */
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

    private void setupFloatingSearch() {
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {

            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {

                if (!oldQuery.equals("") && newQuery.equals("")) {
                    mSearchView.clearSuggestions();
                } else {

                    //this shows the top left circular progress
                    //you can call it where ever you want, but
                    //it makes sense to do it when loading something in
                    //the background.
                    mSearchView.showProgress();

                    //simulates a query call to a data source
                    //with a new query.
                    DataHelper.findSuggestions(getActivity(), newQuery, 5,
                            FIND_SUGGESTION_SIMULATED_DELAY, new DataHelper.OnFindSuggestionsListener() {

                                @Override
                                public void onResults(List<ColorSuggestion> results) {

                                    //this will swap the data and
                                    //render the collapse/expand animations as necessary
                                    mSearchView.swapSuggestions(results);

                                    //let the users know that the background
                                    //process has completed
                                    mSearchView.hideProgress();
                                }
                            });
                }

                Log.d(TAG, "onSearchTextChanged()");
            }
        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {

                ColorSuggestion colorSuggestion = (ColorSuggestion) searchSuggestion;
                DataHelper.findColors(getActivity(), colorSuggestion.getBody(),
                        new DataHelper.OnFindColorsListener() {

                            @Override
                            public void onResults(List<ColorWrapper> results) {
                                mSearchResultsAdapter.swapData(results);
                            }

                        });
                Log.d(TAG, "onSuggestionClicked()");

                mLastQuery = searchSuggestion.getBody();
            }

            @Override
            public void onSearchAction(String query) {
                mLastQuery = query;

                DataHelper.findColors(getActivity(), query,
                        new DataHelper.OnFindColorsListener() {

                            @Override
                            public void onResults(List<ColorWrapper> results) {
                                mSearchResultsAdapter.swapData(results);
                            }

                        });
                Log.d(TAG, "onSearchAction()");
            }
        });

        mSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {

                //show suggestions when search bar gains focus (typically history suggestions)
                mSearchView.swapSuggestions(DataHelper.getHistory(getActivity(), 3));

                Log.d(TAG, "onFocus()");
            }

            @Override
            public void onFocusCleared() {

                //set the title of the bar so that when focus is returned a new query begins
                mSearchView.setSearchBarTitle(mLastQuery);

                //you can also set setSearchText(...) to make keep the query there when not focused and when focus returns
                //mSearchView.setSearchText(searchSuggestion.getBody());

                Log.d(TAG, "onFocusCleared()");
            }
        });


//        //handle menu clicks the same way as you would
//        //in a regular activity
//        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
//            @Override
//            public void onActionMenuItemSelected(MenuItem item) {
//
//                if (item.getItemId() == R.id.action_change_colors) {
//
//                    mIsDarkSearchTheme = true;
//
//                    //demonstrate setting colors for items
//                    mSearchView.setBackgroundColor(Color.parseColor("#787878"));
//                    mSearchView.setViewTextColor(Color.parseColor("#e9e9e9"));
//                    mSearchView.setHintTextColor(Color.parseColor("#e9e9e9"));
//                    mSearchView.setActionMenuOverflowColor(Color.parseColor("#e9e9e9"));
//                    mSearchView.setMenuItemIconColor(Color.parseColor("#e9e9e9"));
//                    mSearchView.setLeftActionIconColor(Color.parseColor("#e9e9e9"));
//                    mSearchView.setClearBtnColor(Color.parseColor("#e9e9e9"));
//                    mSearchView.setDividerColor(Color.parseColor("#BEBEBE"));
//                    mSearchView.setLeftActionIconColor(Color.parseColor("#e9e9e9"));
//                } else {
//
//                    //just print action
//                    Toast.makeText(getActivity().getApplicationContext(), item.getTitle(),
//                            Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });

        //use this listener to listen to menu clicks when app:floatingSearch_leftAction="showHome"
        mSearchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {

                Log.d(TAG, "onHomeClicked()");
            }
        });

        /*
         * Here you have access to the left icon and the text of a given suggestion
         * item after as it is bound to the suggestion list. You can utilize this
         * callback to change some properties of the left icon and the text. For example, you
         * can load the left icon images using your favorite image loading library, or change text color.
         *
         *
         * Important:
         * Keep in mind that the suggestion list is a RecyclerView, so views are reused for different
         * items in the list.
         */
        mSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon,
                                         TextView textView, SearchSuggestion item, int itemPosition) {
                ColorSuggestion colorSuggestion = (ColorSuggestion) item;

                String textColor = false ? "#ffffff" : "#000000";
                String textLight = false ? "#bfbfbf" : "#787878";

                if (colorSuggestion.getIsHistory()) {
                    leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                            R.drawable.ic_history_black_24dp, null));

                    Util.setIconColor(leftIcon, Color.parseColor(textColor));
                    leftIcon.setAlpha(.36f);
                } else {
                    leftIcon.setAlpha(0.0f);
                    leftIcon.setImageDrawable(null);
                }

                textView.setTextColor(Color.parseColor(textColor));
                String text = colorSuggestion.getBody()
                        .replaceFirst(mSearchView.getQuery(),
                                "<font color=\"" + textLight + "\">" + mSearchView.getQuery() + "</font>");
                textView.setText(Html.fromHtml(text));
            }

        });

        //listen for when suggestion list expands/shrinks in order to move down/up the
        //search results list
        mSearchView.setOnSuggestionsListHeightChanged(new FloatingSearchView.OnSuggestionsListHeightChanged() {
            @Override
            public void onSuggestionsListHeightChanged(float newHeight) {
                mSearchResultsList.setTranslationY(newHeight);
            }
        });
    }

    private void setupResultsList() {
        mSearchResultsAdapter = new SearchResultsListAdapter();
        mSearchResultsList.setAdapter(mSearchResultsAdapter);
        mSearchResultsList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

//    @Override
//    public boolean onActivityBackPress() {
//        //if mSearchView.setSearchFocused(false) causes the focused search
//        //to close, then we don't want to close the activity. if mSearchView.setSearchFocused(false)
//        //returns false, we know that the search was already closed so the call didn't change the focus
//        //state and it makes sense to call supper onBackPressed() and close the activity
//        if (!mSearchView.setSearchFocused(false)) {
//            return false;
//        }
//        return true;
//    }

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
                Log.d(TAG, "Address found");
            }
        }
    }

}
