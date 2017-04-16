'use strict';

const TAG = 'DBG#2, '
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


/**
 * Triggers when a user send chat and then send notification back to user
 *
 * This function listen to any change at '/chat'
 * Users save their token to `/token`.
 */
exports.sendChatNotification = functions.database.ref('/chat').onWrite(event => {

    // Get the list of device notification tokens.
    const getDeviceTokensPromise = admin.database().ref(`/token`).once('value');

    return Promise.all([getDeviceTokensPromise]).then(results => {
        const tokensSnapshot = results[0];

        // Check if there are any device tokens.
        if (!tokensSnapshot.hasChildren()) {
            return console.log(TAG, 'There are no notification tokens to send to.');
        }
        console.log(TAG, 'There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');

        // Notification details.
        const payload = {
            data: {
              title: 'Kontak Dokter',
              body: 'Baca pesan'
            }
        };

        // Listing all tokens
        const tokens = Object.keys(tokensSnapshot.val());
        console.log(TAG, ' tokensSnapshot.val() ', tokensSnapshot.val());
        console.log(TAG, ' tokens ', tokens);

        // Send notificatins to all tokens.
        return admin.messaging().sendToDevice(tokens, payload).then(response => {
            // For each message check if there was an error.
            const tokensToRemove = [];
            response.results.forEach((result, index) => {
                const error = result.error;
                if (error) {
                    console.error('Failure sending notification to', tokens[index], error);
                    //Cleanup the tokens who are not registered anymore.
                    if (error.code === 'messaging/invalid-registration-token'
                     || error.code === 'messaging/registration-token-not-registered') {
                        tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
                    }
                }
            });
            return Promise.all(tokensToRemove);
        });
    });
});

/**
 * Triggers when a user send post and then send notification back to user
 *
 * This function listen to any change at '/posts'
 * Users save their token to `/token`.
 */
exports.sendPostNotification = functions.database.ref('/posts').onWrite(event => {

    // Get the list of device notification tokens.
    const getDeviceTokensPromise = admin.database().ref(`/token`).once('value');

    return Promise.all([getDeviceTokensPromise]).then(results => {
        const tokensSnapshot = results[0];

        // Check if there are any device tokens.
        if (!tokensSnapshot.hasChildren()) {
            return console.log(TAG, 'There are no notification tokens to send to.');
        }
        console.log(TAG, 'There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');

        // Notification details.
        const payload = {
            data: {
                title: 'Laporan Kecelakaan',
                body: 'Lihat laporan'
            }
        };

        // Listing all tokens
        const tokens = Object.keys(tokensSnapshot.val());
        console.log(TAG, ' tokensSnapshot.val() ', tokensSnapshot.val());
        console.log(TAG, ' tokens ', tokens);

        // Send notificatins to all tokens.
        return admin.messaging().sendToDevice(tokens, payload).then(response => {
            // For each message check if there was an error.
            const tokensToRemove = [];
            response.results.forEach((result, index) => {
                const error = result.error;
                if (error) {
                    console.error('Failure sending notification to', tokens[index], error);
                    //Cleanup the tokens who are not registered anymore.
                    if (error.code === 'messaging/invalid-registration-token'
                     || error.code === 'messaging/registration-token-not-registered') {
                        tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
                    }
                }
            });
            return Promise.all(tokensToRemove);
        });
    });
});
