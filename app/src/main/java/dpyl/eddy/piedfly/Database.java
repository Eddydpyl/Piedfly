package dpyl.eddy.piedfly;

import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import dpyl.eddy.piedfly.model.Emergency;
import dpyl.eddy.piedfly.model.Event;
import dpyl.eddy.piedfly.model.User;

public class Database {

    /**
     * Deletes a User from the database
     * @param uid The User's ID (obtained when signing in)
     */
    public static void deleteUser(@NonNull String uid) {
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.removeValue();
    }

    /**
     * Updates the attributes of a User
     * @param user The User to be updated. Only fields with a non-null value are updated.
     */
    public static void updateUser(@NonNull User user) {
        if(user.getUid() == null)
            throw new RuntimeException("User has no uid");
        else {
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
            if(user.getLastKnownLocation() != null)
                childUpdates.put("/lastKnownLocation", user.getLastKnownLocation());
            if(user.getFlock() != null)
                childUpdates.put("/flock", user.getFlock());
            userRef.updateChildren(childUpdates);
        }
    }

    /**
     * Establishes a bidirectional relationship between two users: Both are added to the other's flock.
     * @param uid1 The first User's ID (obtained when signing in)
     * @param uid2 The second User's ID (obtained when signing in)
     */
    public static void addToFlock(@NonNull String uid1, @NonNull String uid2) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference user1Ref = database.getReference("users").child(uid1).child("flock").child(uid2);
        final DatabaseReference user2Ref = database.getReference("users").child(uid2).child("flock").child(uid1);
        user1Ref.setValue("11");
        user2Ref.setValue("11");
    }

    /**
     * Removes the established relationship between the two users: Both are removed from the other's flock.
     * @param uid1 The first User's ID (obtained when signing in)
     * @param uid2 The second User's ID (obtained when signing in)
     */
    public static void removeFromFlock(@NonNull String uid1, @NonNull String uid2) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference user1Ref = database.getReference("users").child(uid1).child("flock").child(uid2);
        final DatabaseReference user2Ref = database.getReference("users").child(uid2).child("flock").child(uid1);
        user1Ref.removeValue();
        user2Ref.removeValue();
    }

    /**
     * Creates an emergency in the database
     * @param emergency The Emergency that is to be created.
     */
    public static void createEmergency(@NonNull Emergency emergency) {
        if(emergency.getUid() == null)
            throw new RuntimeException("Emergency has no uid");
        final DatabaseReference emergencyRef = FirebaseDatabase.getInstance().getReference("emergencies").push();
        emergency.setKey(emergencyRef.getKey());
        emergencyRef.setValue(emergency);
    }

    /**
     * Deletes an Emergency from the database
     * @param key The Emergency's key
     */
    public static void deleteEmergency(@NonNull String key) {
        final DatabaseReference emergencyRef = FirebaseDatabase.getInstance().getReference("emergencies").child(key);
        emergencyRef.removeValue();
    }

    /**
     * Updates the attributes of an Emergency.
     * @param emergency The Emergency to be updated. Only fields with a non-null value are updated.
     */
    public static void updateEmergency(@NonNull Emergency emergency) {
        if(emergency.getKey() == null)
            throw new RuntimeException("Emergency has no key");
        if(emergency.getUid() == null)
            throw new RuntimeException("Emergency has no uid");
        else {
            DatabaseReference emergencyRef = FirebaseDatabase.getInstance().getReference("emergencies").child(emergency.getKey());
            final Map<String, Object> childUpdates = new HashMap<>();
            if(emergency.getLocation() != null)
                childUpdates.put("/location", emergency.getLocation());
            if(emergency.getUsersNearby() != null)
                childUpdates.put("/usersNearby", emergency.getUsersNearby());
            if(emergency.getEvents() != null)
                childUpdates.put("/events", emergency.getEvents());
            emergencyRef.updateChildren(childUpdates);
        }
    }

    /**
     * Creates an Event in the database
     * @param key The key of the Emergency to which the Event belongs.
     * @param event The Event that is to be created.
     */
    public static void createEvent(@NonNull String key, @NonNull Event event) {
        final DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("emergencies").child(key).child("events").push();
        eventRef.setValue(event);
    }

}
