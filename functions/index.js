const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// Updates the userCount and smallID when a new user registers
exports.addAccount = functions.auth.user().onCreate(event => {
    const uid = event.data.uid;
    return admin.database().ref(`/userCount`).once('value').then(function(userCount) {
        var value = (userCount) ? userCount.val() + 1 : 0;
        const tinyID = value.toString(36);
        admin.database().ref(`/userCount`).set(value);
        admin.database().ref(`/users/${uid}/tinyID`).set(tinyID);
        return admin.database().ref(`/tinyID/${tinyID}`).set(uid);
    });
});

// Sends a notification to the Users nearby the one to whom the Emergency refers to, every time they enter or leave the perimeter.
exports.emergencyNearbyNotification = functions.database.ref(`/emergencies/{pushId}/usersNearby`).onWrite(event => {

    var state;
    var newUsers;
    const usersNearby = Object.keys(event.data.val());
    if (event.data.previous.exists()) {
        const previous = Object.keys(event.data.previous.val());
        if (usersNearby.length > previous) {
            newUsers = usersNearby.filter(function(i) {return previous.indexOf(i) < 0;});
            state = true;
        } else {
            newUsers = previous.filter(function(i) {return usersNearby.indexOf(i) < 0;});
            state = false;
        }
    } else {
        newUsers = usersNearby;
        state = true;
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
                state: state,
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
            helpersNearby = Object.keys(emergency.val().helpersNearby);
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

// Sends a notification to the User that is the target of the Request
exports.requestNotification = functions.database.ref(`/requests/{pushId}/uid`).onCreate(event => {

    const requestKey = event.params.pushId;
    const uid = event.data.val();

    var promises = [];
    const promise = admin.database().ref(`/users/${uid}/token`).once('value');
    promises.push(promise);

    return Promise.all(promises).then(results => {
        var tokens = [];
        results.forEach(function(token) {
            tokens.push(token.val());
        });
        const payload = {
            data: {
                type: 'REQUEST',
                key: requestKey,
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

// Sends a notification to the User that is the target of the Poke
exports.pokeStartNotification = functions.database.ref(`/pokes/{pushId}/uid`).onCreate(event => {

    const pokeKey = event.params.pushId;
    const uid = event.data.val();

    var promises = [];
    const promise = admin.database().ref(`/users/${uid}/token`).once('value');
    promises.push(promise);

    return Promise.all(promises).then(results => {
        var tokens = [];
        results.forEach(function(token) {
            tokens.push(token.val());
        });
        const payload = {
            data: {
                type: 'START_POKE',
                key: pokeKey,
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

// Sends a notification to the Users involved in the Poke that are not the checker
exports.pokeEndNotification = functions.database.ref(`/pokes/{pushId}/checker`).onCreate(event => {

    const pokeKey = event.params.pushId;
    const checker = event.data.val();

    return event.data.ref.parent.once("value").then(poke => {

        const pokeUid = poke.val().uid;
        const pokeTrigger = poke.val().trigger;

        var promises = [];
        if (checker !== pokeUid) {
            const promise = admin.database().ref(`/users/${pokeUid}/token`).once('value');
            promises.push(promise);
        }
        if (checker !== pokeTrigger) {
            const promise = admin.database().ref(`/users/${pokeTrigger}/token`).once('value');
            promises.push(promise);
        }

        return Promise.all(promises).then(results => {
            var tokens = [];
            results.forEach(function(token) {
                tokens.push(token.val());
            });
            const payload = {
                data: {
                    type: 'END_POKE',
                    key: pokeKey,
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