package com.progremastudio.emergencymedicalteam;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddressService extends IntentService {

    private final static String TAG = "address-service";

    protected ResultReceiver mReceiver;

    public AddressService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        /*
        Initiate error message
         */
        String errorMessage = "";

        /*
        Initiate addresses list
         */
        List<Address> addresses = null;

        /*
        Initiate Geocoder object
         */
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        /*
        Initiate receiver object
         */
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        /*
        Get the location passed to this service through an intent extra.
         */
        Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

        /*
        Log latitude and longitude location
         */
        Log.d(TAG, String.valueOf(location.getLatitude()));
        Log.d(TAG, String.valueOf(location.getLongitude()));

        /*
        Just get a single address.
         */
        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        } catch (IOException ioException) {
            /*
            Catch network or other I/O problems.
             */
            errorMessage = getString(R.string.str_Service_is_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            /*
            Catch invalid latitude or longitude values.
             */
            errorMessage = getString(R.string.str_Invalid_latitude_or_longitude_values);
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() + ", " +
                    "Longitude = " + location.getLongitude(), illegalArgumentException);
        }

        if (addresses == null || addresses.size() == 0) {
            /*
            Handle case where no address was found.
            */
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.str_No_addresses_found);
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            /*
            Handle case where address was found.
            */
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            /*
            Fetch the address lines using getAddressLine,
            join them, and send them to the thread.
             */
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, getString(R.string.str_Address_found));
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"), addressFragments));
        }

    }

    /**
     * Deliver back the address to the caller activity
     *
     * @param resultCode result code
     * @param message message conveyed
     */
    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

    /**
     * Constants used by this class
     */
    public final class Constants {

        public static final int SUCCESS_RESULT = 0;

        public static final int FAILURE_RESULT = 1;

        public static final String PACKAGE_NAME = "com.progremastudio.emergencymedicalteam";

        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";

        public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";

        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    }


}
