const functions = require('firebase-functions');

exports.sendEmergencyNotifications = functions.database.ref('/emergencies/{pushId}/uid').onCreate(event => {

    return event.data.ref.parent.once("value").then(function(emergency){

        const emergencyKey = emergency.val().key;
        const emergencyUid = emergency.val().uid;

        return admin.database().ref('/users/${emergencyUid}/flock').once('value').then(function(flock) {
            var promises = [];
            flock.forEach(function(snapshot) {
                var uid = snapshot.key;
                const promise = admin.database().ref('/users/${uid}/token').once('value');
                promises.push(promise);
            });
            return Promise.all(promises).then(results => {
                var tokens = [];
                results.forEach(function(token) {
                    tokens.push(token.val());
                });
                const payload = {
                    data: {
                        type: 'EMERGENCY',
                        key: emergencyKey,
                    }
                };
                return admin.messaging().sendToDevice(tokens, payload)
                .then(function (response) {
                    response.results.forEach((result, index) => {
                        const error = result.error;
                        if (error) console.error('Failure sending notification to:', tokens[index], error);
                    });
                })
                .catch(function (error) {
                    console.log('Error sending messages:', error);
                });
            });
        });

        // TODO: Search for nearby users and notify them. Then update the database with the selected users.

    });

});