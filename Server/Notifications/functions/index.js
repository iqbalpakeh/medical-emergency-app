'use strict';

const TAG = 'DBG#7'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


/**
 * Triggers when a user send chat and then send notification back to user
 *
 * This function listen to any change at '/chat'
 * Users save their token to `/users/{followedUid}/notificationTokens/{notificationToken}`.
 */
exports.sendNotification = functions.database.ref('/chat').onWrite(event => {

    console.log(TAG, " Start of new functions ====================================")
    console.log(TAG, ' New messages are posted by user');

    // Get the list of device notification tokens.
    const getDeviceTokensPromise = admin.database().ref(`/users/RjqlkkTE7YdSw1gM8gFZZ4DzVDG3/token`).once('value');

    return Promise.all([getDeviceTokensPromise]).then(results => {
        const tokensSnapshot = results[0];

        // Notification details.
        const payload = {
          notification: {
            title: 'TBM Chat notification',
            body: `TBM Chat`
          }
        };

        // Listing all tokens
        const tokens = Object.keys(tokensSnapshot.val());
        console.log(TAG, ' tokensSnapshot.val() ', tokensSnapshot.val());

        // Send notificatins to all tokens.
        return admin.messaging().sendToDevice(tokens, payload).then(response => {
          // For each message check if there was an error.
          const tokensToRemove = [];
          response.results.forEach((result, index) => {
          const error = result.error;
          if (error) {
              console.error('Failure sending notification to', tokens[index], error);
              // Cleanup the tokens who are not registered anymore.
              //if (error.code === 'messaging/invalid-registration-token' ||
              //    error.code === 'messaging/registration-token-not-registered') {
              //  tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
              //}
            }
          });
          return Promise.all(tokensToRemove);
        });

    });
});
