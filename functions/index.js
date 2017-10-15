const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// Sends a notification to the Users inside the flock of the one to whom the Emergency refers to.
exports.emergencyFlockNotification = functions.database.ref(`/emergencies/{pushId}/uid`).onCreate(event => {

    const emergencyKey = event.params.pushId;
    const emergencyUid = event.data.val();

    return admin.database().ref(`/users/${emergencyUid}/flock`).once('value').then(function(flock) {
        var promises = [];
        if (flock) {
            flock.forEach(function(snapshot) {
                var uid = snapshot.key;
                const promise = admin.database().ref(`/users/${uid}/token`).once('value');
                promises.push(promise);
            });
        }
        return Promise.all(promises).then(results => {
            var tokens = [];
            results.forEach(function(token) {
                tokens.push(token.val());
            });
            const payload = {
                data: {
                    type: 'EMERGENCY_FLOCK',
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

});

// Sends a notification to the Users nearby the one to whom the Emergency refers to, every time they enter the perimeter.
exports.emergencyNearbyNotification = functions.database.ref(`/emergencies/{pushId}/usersNearby`).onWrite(event => {

    var newUsers;
    const usersNearby = Object.keys(event.data.val());
    if (event.data.previous.exists()) {
        const previous = Object.keys(event.data.previous.val());
        newUsers = usersNearby.filter(function(i) {return previous.indexOf(i) < 0;});
    } else {
        newUsers = usersNearby;
    }

    const emergencyKey = event.params.pushId;

    var promises = [];
    if (newUsers) {
        newUsers.forEach(function(uid) {
            const promise = admin.database().ref(`/users/${uid}/token`).once('value');
            promises.push(promise);
        });
    }
    return Promise.all(promises).then(results => {
        var tokens = [];
        results.forEach(function(token) {
            tokens.push(token.val());
        });
        const payload = {
            data: {
                type: 'EMERGENCY_NEARBY',
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

// Sends a notification to the Users participating in the Emergency to which the Event belongs
exports.eventNotification = functions.database.ref(`/events/{emergency}/{pushId}`).onCreate(event => {

    const eventKey = event.params.pushId;
    const emergencyKey = event.params.emergency;

    return admin.database().ref(`/emergencies/${emergencyKey}`).once('value').then(function(emergency) {

        const emergencyUid = emergency.val().uid;

        const helpersNearby = [];
        if (emergency.val().helpersNearby) {
            const helpersNearby = Object.keys(emergency.val().helpersNearby);
        }

        return admin.database().ref(`/users/${emergencyUid}/flock`).once('value').then(function(flock) {
            var promises = [];
            if (flock) {
                flock.forEach(function(snapshot) {
                    var uid = snapshot.key;
                    const promise = admin.database().ref(`/users/${uid}/token`).once('value');
                    promises.push(promise);
                });
            }
            helpersNearby.forEach(function(uid) {
                const promise = admin.database().ref(`/users/${uid}/token`).once('value');
                promises.push(promise);
            });
            return Promise.all(Array.from(new Set(promises))).then(results => {
                var tokens = [];
                results.forEach(function(token) {
                    tokens.push(token.val());
                });
                const payload = {
                    data: {
                        type: 'EVENT',
                        key: eventKey,
                        emergency: emergencyKey,
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

    });

});