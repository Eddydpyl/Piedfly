package dpyl.eddy.piedfly.firebase;

import android.support.annotation.NonNull;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import dpyl.eddy.piedfly.Constants;
import dpyl.eddy.piedfly.firebase.model.Emergency;
import dpyl.eddy.piedfly.firebase.model.Event;
import dpyl.eddy.piedfly.firebase.model.EventType;
import dpyl.eddy.piedfly.firebase.model.Poke;
import dpyl.eddy.piedfly.firebase.model.Request;
import dpyl.eddy.piedfly.firebase.model.RequestType;
import dpyl.eddy.piedfly.firebase.model.SimpleLocation;
import dpyl.eddy.piedfly.firebase.model.User;

public class DataManager {

    private static FirebaseDatabase mDatabase;
    private static GeoFire mGeoFire;
    private static Map<String, GeoQuery> mGeoQueries;

    static {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        if (mGeoFire == null) mGeoFire = new GeoFire(mDatabase.getReference("geofire"));
        if (mGeoQueries == null) mGeoQueries = new HashMap<>();
    }

    public static FirebaseDatabase getDatabase() {
        return mDatabase;
    }

    public static void updateUser(@NonNull User user) {
        if (user.getUid() == null)
            throw new RuntimeException("User has no uid");
        final DatabaseReference userRef = mDatabase.getReference("users").child(user.getUid());
        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/uid", user.getUid());
        if (user.getToken() != null)
            childUpdates.put("/token", user.getToken());
        if (user.getPhone() != null)
            childUpdates.put("/phone", user.getPhone());
        if (user.getEmail() != null)
            childUpdates.put("/email", user.getEmail());
        if (user.getName() != null)
            childUpdates.put("/name", user.getName());
        if (user.getAge() != null)
            childUpdates.put("/age", user.getAge());
        if (user.getPhotoUrl() != null)
            childUpdates.put("/photoUrl", user.getPhotoUrl());
        if (user.getCountryISO() != null)
            childUpdates.put("/countryISO", user.getCountryISO());
        userRef.updateChildren(childUpdates);
    }

    public static String requestJoinFlock(@NonNull Request request) {
        if (request.getUid() == null)
            throw new RuntimeException("Request has no uid");
        if (request.getTrigger() == null)
            throw new RuntimeException("Request has no trigger");
        request.setRequestType(RequestType.JOIN_FLOCK);
        final DatabaseReference requestRef = mDatabase.getReference("requests").push();
        requestRef.setValue(request);
        return requestRef.getKey();
    }

    public static void addToFlock(@NonNull String uid1, @NonNull String uid2) {
        final DatabaseReference user1Ref = mDatabase.getReference("users").child(uid1).child("flock").child(uid2);
        final DatabaseReference user2Ref = mDatabase.getReference("users").child(uid2).child("flock").child(uid1);
        user1Ref.setValue(Constants.PLACEHOLDER);
        user2Ref.setValue(Constants.PLACEHOLDER);
    }

    public static void removeFromFlock(@NonNull String uid1, @NonNull String uid2) {
        final DatabaseReference user1Ref = mDatabase.getReference("users").child(uid1).child("flock").child(uid2);
        final DatabaseReference user2Ref = mDatabase.getReference("users").child(uid2).child("flock").child(uid1);
        user1Ref.removeValue();
        user2Ref.removeValue();
    }

    public static void setLastKnownLocation(@NonNull String uid, @NonNull final SimpleLocation simpleLocation) {
        final DatabaseReference userRef = mDatabase.getReference("users").child(uid);
        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/lastKnownLocation", simpleLocation);
        userRef.updateChildren(childUpdates);
        mGeoFire.setLocation(uid, new GeoLocation(simpleLocation.getLatitude(), simpleLocation.getLongitude()));
        if (!mGeoQueries.isEmpty()) {
            userRef.child("emergency").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String key = dataSnapshot.getValue(String.class);
                        if (mGeoQueries.containsKey(key)) {
                            GeoQuery geoQuery = mGeoQueries.get(key);
                            geoQuery.setCenter(new GeoLocation(simpleLocation.getLatitude(), simpleLocation.getLongitude()));
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // TODO: Error handling
                }
            });
        }
    }

    public static String startEmergency(@NonNull Emergency emergency) {
        if (emergency.getUid() == null)
            throw new RuntimeException("Emergency has no uid");
        if (emergency.getTrigger() == null)
            throw new RuntimeException("Emergency has no trigger");
        final DatabaseReference emergencyRef = mDatabase.getReference("emergencies").push();
        final DatabaseReference userRef = mDatabase.getReference("users").child(emergency.getUid()).child("emergency");
        final DatabaseReference eventRef = mDatabase.getReference("events").child(emergencyRef.getKey()).push();
        emergency.setKey(emergencyRef.getKey());
        emergencyRef.setValue(emergency);
        userRef.setValue(emergency.getKey());
        Event event = new Event(System.currentTimeMillis(), emergency.getStart(), emergency.getTrigger(), null, EventType.START);
        eventRef.setValue(event);
        if (emergency.getStart() != null) readyGeoQuery(emergency);
        stopAllPokes(emergency.getUid());
        return emergencyRef.getKey();
    }

    public static void stopEmergency(@NonNull Emergency emergency) {
        if (emergency.getKey() == null)
            throw new RuntimeException("Emergency has no key");
        if (emergency.getUid() == null)
            throw new RuntimeException("Emergency has no uid");
        if (emergency.getChecker() == null)
            throw new RuntimeException("Emergency has no checker");
        final DatabaseReference emergencyRef = mDatabase.getReference("emergencies").child(emergency.getKey());
        final DatabaseReference userRef = mDatabase.getReference("users").child(emergency.getUid()).child("emergency");
        final DatabaseReference eventRef = mDatabase.getReference("events").child(emergency.getKey()).push();
        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/checker", emergency.getChecker());
        if (emergency.getFinish() != null)
            childUpdates.put("/finish", emergency.getFinish());
        emergencyRef.updateChildren(childUpdates);
        userRef.removeValue();
        Event event = new Event(System.currentTimeMillis(), emergency.getFinish(), emergency.getChecker(), null, EventType.FINISH);
        eventRef.setValue(event);
        dropGeoQuery(emergency);
    }

    public static void joinEmergency(@NonNull String key, @NonNull String uid) {
        DatabaseReference emergencyRef = mDatabase.getReference("emergencies").child(key).child("helpersNearby").child(uid);
        emergencyRef.setValue(true);
    }

    public static String createEvent(@NonNull String key, @NonNull Event event) {
        if (event.getTime() == null)
            throw new RuntimeException("Event has no time");
        if (event.getUid() == null)
            throw new RuntimeException("Event has no uid");
        if (event.getEventType() == null)
            throw new RuntimeException("Event has no eventType");
        final DatabaseReference eventRef = mDatabase.getReference("events").child(key).push();
        eventRef.setValue(event);
        return eventRef.getKey();
    }

    public static String startPoke(@NonNull Poke poke) {
        if (poke.getUid() == null)
            throw new RuntimeException("Poke has no uid");
        if (poke.getTrigger() == null)
            throw new RuntimeException("Poke has no trigger");
        final DatabaseReference pokeRef = mDatabase.getReference("pokes").push();
        final DatabaseReference userRef = mDatabase.getReference("users").child(poke.getUid()).child("flock").child(poke.getTrigger());
        final DatabaseReference triggerRef = mDatabase.getReference("users").child(poke.getTrigger()).child("flock").child(poke.getUid());
        poke.setKey(pokeRef.getKey());
        pokeRef.setValue(poke);
        userRef.setValue(poke.getKey());
        triggerRef.setValue(poke.getKey());
        return pokeRef.getKey();
    }

    public static void stopPoke(@NonNull Poke poke) {
        if (poke.getKey() == null)
            throw new RuntimeException("Poke has no key");
        if (poke.getUid() == null)
            throw new RuntimeException("Poke has no uid");
        if (poke.getTrigger() == null)
            throw new RuntimeException("Poke has no trigger");
        if (poke.getChecker() == null)
            throw new RuntimeException("Poke has no checker");
        final DatabaseReference pokeRef = mDatabase.getReference("pokes").child(poke.getKey());
        final DatabaseReference userRef = mDatabase.getReference("users").child(poke.getUid()).child("flock").child(poke.getTrigger());
        final DatabaseReference triggerRef = mDatabase.getReference("users").child(poke.getTrigger()).child("flock").child(poke.getUid());
        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/checker", poke.getChecker());
        if (poke.getFinish() != null)
            childUpdates.put("/finish", poke.getFinish());
        pokeRef.updateChildren(childUpdates);
        userRef.setValue(Constants.PLACEHOLDER);
        triggerRef.setValue(Constants.PLACEHOLDER);
    }

    /**
     * Stops all active Pokes that target the User with the provided uid
     */
    private static void stopAllPokes(final String uid) {
        mDatabase.getReference("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    Map<String, String> flock = user.getFlock();
                    if (flock != null) {
                        for (String key : flock.keySet()) {
                            if (flock.get(key) != null && !flock.get(key).equals(Constants.PLACEHOLDER)) {
                                mDatabase.getReference("pokes").child(flock.get(key)).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Poke poke = dataSnapshot.getValue(Poke.class);
                                        if (poke != null && poke.getUid().equals(uid)) {
                                            poke.setChecker(uid);
                                            stopPoke(poke);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        // TODO: Error handling
                                    }
                                });
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: Error handling
            }
        });
    }

    private static void readyGeoQuery(@NonNull final Emergency emergency) {
        dropGeoQuery(emergency);
        SimpleLocation location = emergency.getStart();
        GeoQuery geoQuery = mGeoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), Constants.RADIUS_KM);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                DatabaseReference emergencyRef = mDatabase.getReference("emergencies").child(emergency.getKey()).child("usersNearby").child(key);
                emergencyRef.setValue(true);
                checkEmergency();
            }

            @Override
            public void onKeyExited(final String key) {
                DatabaseReference emergencyRef = mDatabase.getReference("emergencies").child(emergency.getKey()).child("usersNearby").child(key);
                emergencyRef.removeValue();
                checkEmergency();
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                // TODO: Error handling
            }

            private void checkEmergency() {
                mDatabase.getReference("emergencies").child(emergency.getKey()).child("checker").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) dropGeoQuery(emergency);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // TODO: Error handling
                    }
                });
            }
        });
        mGeoQueries.put(emergency.getKey(), geoQuery);
    }

    private static void dropGeoQuery(@NonNull final Emergency emergency) {
        if (mGeoQueries.containsKey(emergency.getKey())) {
            mGeoQueries.get(emergency.getKey()).removeAllListeners();
            mGeoQueries.remove(emergency.getKey());
        }
    }
}
