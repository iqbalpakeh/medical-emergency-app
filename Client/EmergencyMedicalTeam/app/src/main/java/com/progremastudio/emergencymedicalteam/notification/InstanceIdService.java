package com.progremastudio.emergencymedicalteam.notification;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class InstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "instance-id-service";

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {

        /*
        // Store token to shared-preference
        AppSharedPreferences.storeMessagingToken(getApplicationContext(), token);

        // If user already signin, update database on server
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {

            // Create new User object
            User user = new User(
                    AppSharedPreferences.getCurrentUserId(getApplicationContext()),
                    AppSharedPreferences.getCurrentUserDisplayName(getApplicationContext()),
                    AppSharedPreferences.getCurrentUserEmail(getApplicationContext()),
                    AppSharedPreferences.getCurrentUserPhoneNumber(getApplicationContext()),
                    AppSharedPreferences.getCurrentUserPictureUrl(getApplicationContext()),
                    AppSharedPreferences.getMessagingToken(getApplicationContext())
            );

            // Prepare hash-map value from user object
            Map<String, Object> userValues = user.toMap();
            Map<String, Object> childUpdates = new HashMap<>();

            //Prepare data for /USERS/#uid#
            childUpdates.put("/" + FirebasePath.USERS + "/" +
                    AppSharedPreferences.getCurrentUserId(getApplicationContext()), userValues);

            // Update data in Firebase
            mDatabase.updateChildren(childUpdates);

        }
        */
    }

}
