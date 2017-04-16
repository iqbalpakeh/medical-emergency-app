package com.progremastudio.emergencymedicalteam.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.progremastudio.emergencymedicalteam.R;
import com.progremastudio.emergencymedicalteam.core.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "messaging-service";

    private NotificationCompat.Builder mChatNotificationBuilder;

    private NotificationManager mChatNotificationManager;

    private NotificationCompat.Builder mPostNotificationBuilder;

    private NotificationManager mPostNotificationManager;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            // Also if you intend on generating your own notifications as a result of a received FCM
            // message, here is where that should be initiated. See sendNotification method below.
            sendNotification(remoteMessage.getData());
        }
    }


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageData FCM message body received.
     */
    private void sendNotification(Map<String, String> messageData) {

        JSONObject data = new JSONObject(messageData);

        String messageTitle = "";
        String messageBody = "";

        try {
            messageTitle = data.getString("title");
            messageBody = data.getString("body");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "data = " + data.toString());
        Log.d(TAG, "title = " + messageTitle);
        Log.d(TAG, "body = " + messageBody);

        if (messageTitle.equals(getString(R.string.heading_chat))) {
            sendChatNotification(messageTitle, messageBody);
        } else {
            sendPostNotification(messageTitle, messageBody);
        }

    }

    /**
     * Send chat notification
     *
     * @param title message's title
     * @param body message's body
     */
    private void sendChatNotification(String title, String body) {

        /*
        Prepare pending intent
         */
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_OPEN_PAGE, MainActivity.PAGE_CHAT);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        /*
        Prepare notification
         */
        if (mChatNotificationBuilder == null) {
            Log.d(TAG, "new builder");
            mChatNotificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_doctor_white_24dp)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(pendingIntent);
        }

        /*
        Prepare notification manager
         */
        if (mChatNotificationManager == null) {
            mChatNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        /*
        Fire notification
         */
        mChatNotificationManager.notify(1, mChatNotificationBuilder.build());
    }

    /**
     * Send Post notification
     *
     * @param title message's title
     * @param body message's body
     */
    private void sendPostNotification(String title, String body) {

        /*
        Prepare pending intent
         */
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_OPEN_PAGE, MainActivity.PAGE_POST);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        /*
        Prepare notification
         */
        if (mPostNotificationBuilder == null) {
            Log.d(TAG, "new builder");
            mPostNotificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_electrocardiogram_report_24dp)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(pendingIntent);
        }

        /*
        Prepare notification manager
         */
        if (mPostNotificationManager == null) {
            mPostNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        /*
        Fire notification
         */
        mPostNotificationManager.notify(2, mPostNotificationBuilder.build());
    }

}
