'use strict';

const TAG = 'DBG#5'

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
    const getDeviceTokensPromise = admin.database().ref(`/users/d65vrFI5ouTvBsaozx5peKftdPE3/token`).once('value');

    // Get the follower profile.
    const getFollowerProfilePromise = admin.auth().getUser("d65vrFI5ouTvBsaozx5peKftdPE3");

    return Promise.all([getDeviceTokensPromise, getFollowerProfilePromise]).then(results =>{
        const tokensSnapshot = results[0];
        const follower = results[1];

        const tokens = Object.keys(tokensSnapshot.val());

        console.log(TAG, ' tokensSnapshot.val() = ', tokensSnapshot.val());
        console.log(TAG, ' follower = ', follower);

    });

});
