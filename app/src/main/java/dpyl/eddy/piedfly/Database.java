package dpyl.eddy.piedfly;

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

import dpyl.eddy.piedfly.exceptions.ExceptionHandler;
import dpyl.eddy.piedfly.exceptions.IllegalDatabaseState;
import dpyl.eddy.piedfly.model.Emergency;
import dpyl.eddy.piedfly.model.Event;
import dpyl.eddy.piedfly.model.Poke;
import dpyl.eddy.piedfly.model.SimpleLocation;
import dpyl.eddy.piedfly.model.User;

public class Database {

    private static FirebaseDatabase mDatabase;
    private static GeoFire mGeoFire;
    private static GeoQuery mGeoQuery;

    static {
        if (mDatabase == null) mDatabase = FirebaseDatabase.getInstance();
        if (mGeoFire == null) mGeoFire = new GeoFire(mDatabase.getReference("geofire"));
    }

    public static void updateUser(@NonNull User user) {
        if(user.getUid() == null)
            throw new RuntimeException("User has no uid");
        final DatabaseReference userRef = mDatabase.getReference("users").child(user.getUid());
        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/uid", user.getUid());
        if(user.getToken() != null)
            childUpdates.put("/token", user.getToken());
        if(user.getName() != null)
            childUpdates.put("/name", user.getName());
        if(user.getSurname() != null)
            childUpdates.put("/surname", user.getSurname());
        if(user.getAge() != null)
            childUpdates.put("/age", user.getAge());
        if(user.getPhone() != null)
            childUpdates.put("/phone", user.getPhone());
        if(user.getEmail() != null)
            childUpdates.put("/email", user.getEmail());
        if(user.getPhotoUrl() != null)
            childUpdates.put("/photoUrl", user.getPhotoUrl());
        if(user.getCountryISO() != null)
            childUpdates.put("/countryISO", user.getCountryISO());
        userRef.updateChildren(childUpdates);
    }

    public static void addToFlock(@NonNull String uid1, @NonNull String uid2) {
        final DatabaseReference user1Ref = mDatabase.getReference("users").child(uid1).child("flock").child(uid2);
        final DatabaseReference user2Ref = mDatabase.getReference("users").child(uid2).child("flock").child(uid1);
        user1Ref.setValue(true);
        user2Ref.setValue(true);
    }

    public static void removeFromFlock(@NonNull String uid1, @NonNull String uid2) {
        final DatabaseReference user1Ref = mDatabase.getReference("users").child(uid1).child("flock").child(uid2);
        final DatabaseReference user2Ref = mDatabase.getReference("users").child(uid2).child("flock").child(uid1);
        user1Ref.removeValue();
        user2Ref.removeValue();
    }

    public static void setLastKnownLocation (@NonNull String uid, @NonNull SimpleLocation location) {
        final DatabaseReference userRef = mDatabase.getReference("users").child(uid);
        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/lastKnownLocation", location);
        userRef.updateChildren(childUpdates);
        mGeoFire.setLocation(uid, new GeoLocation(location.getLatitude(), location.getLongitude()));
        if (mGeoQuery != null) mGeoQuery.setCenter(new GeoLocation(location.getLatitude(), location.getLongitude()));
    }

    public static void startEmergency(@NonNull Emergency emergency) {
        if(emergency.getUid() == null)
            throw new RuntimeException("Emergency has no uid");
        if(emergency.getTrigger() == null)
            throw new RuntimeException("Emergency has no trigger");
        final DatabaseReference userRef = mDatabase.getReference("users").child(emergency.getUid()).child("emergency");
        final DatabaseReference emergencyRef = mDatabase.getReference("emergencies").push();
        emergency.setKey(emergencyRef.getKey());
        emergencyRef.setValue(emergency);
        userRef.setValue(emergency.getKey());
        if (emergency.getStart() != null) readyGeoQuery(emergency);
    }

    public static void stopEmergency(@NonNull Emergency emergency) {
        if(emergency.getKey() == null)
            throw new RuntimeException("Emergency has no key");
        if(emergency.getUid() == null)
            throw new RuntimeException("Emergency has no uid");
        if(emergency.getChecker() == null)
            throw new RuntimeException("Emergency has no checker");
        final DatabaseReference userRef = mDatabase.getReference("users").child(emergency.getUid()).child("emergency");
        final DatabaseReference emergencyRef = mDatabase.getReference("emergencies").child(emergency.getKey());
        final Map<String, Object> childUpdates = new HashMap<>();
        if(emergency.getChecker() != null)
            childUpdates.put("/checker", emergency.getChecker());
        if(emergency.getFinish() != null)
            childUpdates.put("/finish", emergency.getFinish());
        emergencyRef.updateChildren(childUpdates);
        userRef.removeValue();
        dropGeoQuery();
    }

    public static void updateEmergency(@NonNull Emergency emergency) {
        if(emergency.getKey() == null)
            throw new RuntimeException("Emergency has no key");
        DatabaseReference emergencyRef = mDatabase.getReference("emergencies").child(emergency.getKey());
        final Map<String, Object> childUpdates = new HashMap<>();
        if(emergency.getUsersNearby() != null)
            childUpdates.put("/usersNearby", emergency.getUsersNearby());
        if(emergency.getHelpersNearby() != null)
            childUpdates.put("/helpersNearby", emergency.getHelpersNearby());
        emergencyRef.updateChildren(childUpdates);
    }

    /**
     * @param key The key of the Emergency that the Event belongs to
     */
    public static void createEvent(@NonNull String key, @NonNull Event event) {
        if(event.getTime() == null)
            throw new RuntimeException("Event has no time");
        if(event.getUid() == null)
            throw new RuntimeException("Event has no uid");
        if(event.getEventType() == null)
            throw new RuntimeException("Event has no eventType");
        final DatabaseReference eventRef = mDatabase.getReference("events").child(key).push();
        eventRef.setValue(event);
    }

    public static void startPoke(@NonNull Poke poke) {
        if(poke.getUid() == null)
            throw new RuntimeException("Poke has no uid");
        if(poke.getTrigger() == null)
            throw new RuntimeException("Poke has no trigger");
        final DatabaseReference userRef = mDatabase.getReference("users").child(poke.getUid()).child("flock").child(poke.getTrigger());
        final DatabaseReference triggerRef = mDatabase.getReference("users").child(poke.getTrigger()).child("flock").child(poke.getUid());
        final DatabaseReference pokeRef = mDatabase.getReference("pokes").push();
        poke.setKey(pokeRef.getKey());
        pokeRef.setValue(poke);
        userRef.setValue(poke.getKey());
        triggerRef.setValue(poke.getKey());
    }

    public static void finishPoke(@NonNull Poke poke) {
        if(poke.getKey() == null)
            throw new RuntimeException("Poke has no key");
        if(poke.getUid() == null)
            throw new RuntimeException("Poke has no uid");
        if(poke.getTrigger() == null)
            throw new RuntimeException("Poke has no trigger");
        if(poke.getChecker() == null)
            throw new RuntimeException("Poke has no checker");
        final DatabaseReference userRef = mDatabase.getReference("users").child(poke.getUid()).child("flock").child(poke.getTrigger());
        final DatabaseReference triggerRef = mDatabase.getReference("users").child(poke.getTrigger()).child("flock").child(poke.getUid());
        final DatabaseReference pokeRef = mDatabase.getReference("pokes").child(poke.getKey());
        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/checker", poke.getChecker());
        childUpdates.put("/finish", poke.getFinish());
        pokeRef.updateChildren(childUpdates);
        userRef.setValue(true);
        triggerRef.setValue(true);
    }

    private static void readyGeoQuery(@NonNull final Emergency emergency) {
        dropGeoQuery();
        SimpleLocation location = emergency.getStart();
        mGeoQuery = mGeoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), Constants.RADIUS_KM);
        mGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, GeoLocation location) {
                DatabaseReference emergencyRef = mDatabase.getReference("emergencies").child(emergency.getKey());
                emergencyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Emergency dataSnapshotValue = dataSnapshot.getValue(Emergency.class);
                       if (dataSnapshotValue != null && dataSnapshotValue.getKey() != null) {
                           Map<String, Boolean> usersNearby = dataSnapshotValue.getUsersNearby();
                           if (usersNearby == null) usersNearby = new HashMap<>();
                           usersNearby.put(key, true);
                           dataSnapshotValue.setUsersNearby(usersNearby);
                           updateEmergency(dataSnapshotValue);
                       } else ExceptionHandler.handleException(Thread.currentThread(),
                               new IllegalDatabaseState("An Emergency was deleted from the database"));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // TODO: Error handling
                    }
                });
            }

            @Override
            public void onKeyExited(final String key) {
                DatabaseReference emergencyRef = mDatabase.getReference("emergencies").child(emergency.getKey());
                emergencyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Emergency dataSnapshotValue = dataSnapshot.getValue(Emergency.class);
                        if (dataSnapshotValue != null && dataSnapshotValue.getKey() != null) {
                            Map<String, Boolean> usersNearby = dataSnapshotValue.getUsersNearby();
                            if (usersNearby != null) {
                                usersNearby.remove(key);
                                dataSnapshotValue.setUsersNearby(usersNearby);
                                updateEmergency(dataSnapshotValue);
                            }
                        } else ExceptionHandler.handleException(Thread.currentThread(),
                                new IllegalDatabaseState("An Emergency was deleted from the database"));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // TODO: Error handling
                    }
                });
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {}

            @Override
            public void onGeoQueryReady() {}

            @Override
            public void onGeoQueryError(DatabaseError error) {
                // TODO: Error handling
            }
        });
    }

    private static void dropGeoQuery() {
        if (mGeoQuery != null) {
            mGeoQuery.removeAllListeners();
            mGeoQuery = null;
        }
    }
}
