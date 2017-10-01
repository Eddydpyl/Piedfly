package dpyl.eddy.piedfly;

import android.support.annotation.NonNull;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import dpyl.eddy.piedfly.model.Emergency;
import dpyl.eddy.piedfly.model.Event;
import dpyl.eddy.piedfly.model.Poke;
import dpyl.eddy.piedfly.model.SimpleLocation;
import dpyl.eddy.piedfly.model.User;

public class Database {

    public static void updateUser(@NonNull User user) {
        if(user.getUid() == null)
            throw new RuntimeException("User has no uid");
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        final Map<String, Object> childUpdates = new HashMap<>();
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
        if(user.getFlock() != null)
            childUpdates.put("/flock", user.getFlock());
        if(user.getLastKnownLocation() != null)
            childUpdates.put("/lastKnownLocation", user.getLastKnownLocation());
        userRef.updateChildren(childUpdates);
    }

    public static void addToFlock(@NonNull String uid1, @NonNull String uid2) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference user1Ref = database.getReference("users").child(uid1).child("flock").child(uid2);
        final DatabaseReference user2Ref = database.getReference("users").child(uid2).child("flock").child(uid1);
        user1Ref.setValue(true);
        user2Ref.setValue(true);
    }

    public static void removeFromFlock(@NonNull String uid1, @NonNull String uid2) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference user1Ref = database.getReference("users").child(uid1).child("flock").child(uid2);
        final DatabaseReference user2Ref = database.getReference("users").child(uid2).child("flock").child(uid1);
        user1Ref.removeValue();
        user2Ref.removeValue();
    }

    public static void setLastKnownLocation (@NonNull String uid, @NonNull SimpleLocation location) {
        final FirebaseDatabase databaseReference = FirebaseDatabase.getInstance();
        final DatabaseReference userRef = databaseReference.getReference("users").child(uid);
        final DatabaseReference locationRef = databaseReference.getReference("geofire");
        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/lastKnownLocation", location);
        userRef.updateChildren(childUpdates);
        GeoFire geoFire = new GeoFire(locationRef);
        geoFire.setLocation(uid, new GeoLocation(location.getLatitude(), location.getLongitude()));
    }

    public static void startEmergency(@NonNull Emergency emergency) {
        if(emergency.getUid() == null)
            throw new RuntimeException("Emergency has no uid");
        if(emergency.getTrigger() == null)
            throw new RuntimeException("Emergency has no trigger");
        final FirebaseDatabase databaseReference = FirebaseDatabase.getInstance();
        final DatabaseReference userRef = databaseReference.getReference("users").child(emergency.getUid()).child("emergency");
        final DatabaseReference emergencyRef = databaseReference.getReference("emergencies").push();
        emergency.setKey(emergencyRef.getKey());
        emergencyRef.setValue(emergency);
        userRef.setValue(emergency.getKey());
    }

    public static void stopEmergency(@NonNull Emergency emergency) {
        if(emergency.getKey() == null)
            throw new RuntimeException("Emergency has no key");
        if(emergency.getUid() == null)
            throw new RuntimeException("Emergency has no uid");
        if(emergency.getChecker() == null)
            throw new RuntimeException("Emergency has no checker");
        final FirebaseDatabase databaseReference = FirebaseDatabase.getInstance();
        final DatabaseReference userRef = databaseReference.getReference("users").child(emergency.getUid()).child("emergency");
        final DatabaseReference emergencyRef = databaseReference.getReference("emergencies").child(emergency.getKey());
        final Map<String, Object> childUpdates = new HashMap<>();
        if(emergency.getChecker() != null)
            childUpdates.put("/checker", emergency.getChecker());
        if(emergency.getFinish() != null)
            childUpdates.put("/finish", emergency.getFinish());
        emergencyRef.updateChildren(childUpdates);
        userRef.removeValue();
    }

    public static void updateEmergency(@NonNull Emergency emergency) {
        if(emergency.getKey() == null)
            throw new RuntimeException("Emergency has no key");
        DatabaseReference emergencyRef = FirebaseDatabase.getInstance().getReference("emergencies").child(emergency.getKey());
        final Map<String, Object> childUpdates = new HashMap<>();
        if(emergency.getUsersNearby() != null)
            childUpdates.put("/usersNearby", emergency.getUsersNearby());
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
        final DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("events").child(key).push();
        eventRef.setValue(event);
    }

    public static void startPoke(@NonNull Poke poke) {
        if(poke.getUid() == null)
            throw new RuntimeException("Poke has no uid");
        if(poke.getTrigger() == null)
            throw new RuntimeException("Poke has no trigger");
        final FirebaseDatabase databaseReference = FirebaseDatabase.getInstance();
        final DatabaseReference userRef = databaseReference.getReference("users").child(poke.getUid()).child("flock").child(poke.getTrigger());
        final DatabaseReference triggerRef = databaseReference.getReference("users").child(poke.getTrigger()).child("flock").child(poke.getUid());
        final DatabaseReference pokeRef = databaseReference.getReference("pokes").push();
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
        final FirebaseDatabase databaseReference = FirebaseDatabase.getInstance();
        final DatabaseReference userRef = databaseReference.getReference("users").child(poke.getUid()).child("flock").child(poke.getTrigger());
        final DatabaseReference triggerRef = databaseReference.getReference("users").child(poke.getTrigger()).child("flock").child(poke.getUid());
        final DatabaseReference pokeRef = databaseReference.getReference("pokes").child(poke.getKey());
        final Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/checker", poke.getChecker());
        childUpdates.put("/finish", poke.getFinish());
        pokeRef.updateChildren(childUpdates);
        userRef.setValue(true);
        triggerRef.setValue(true);
    }

}
