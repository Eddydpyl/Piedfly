package dpyl.eddy.piedfly;

import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import dpyl.eddy.piedfly.model.Emergency;
import dpyl.eddy.piedfly.model.Event;
import dpyl.eddy.piedfly.model.Flock;
import dpyl.eddy.piedfly.model.User;

public class Database {

    public static void deleteUser(@NonNull String uid) {
        final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.removeValue();
    }

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
            if(user.getFlocks() != null)
                childUpdates.put("/flocks", user.getFlocks());
            userRef.updateChildren(childUpdates);
        }
    }

    public static void createFlock(@NonNull Flock flock) {
        if(flock.getName() == null)
            throw new RuntimeException("Flock has no name");
        if(flock.getOwner() == null)
            throw new RuntimeException("Flock has no owner");
        else {
            final DatabaseReference flockRef = FirebaseDatabase.getInstance().getReference("flocks").push();
            flock.setKey(flockRef.getKey());
            flockRef.setValue(flock);
        }
    }

    public static void deleteFlock(@NonNull String key) {
        final DatabaseReference flockRef = FirebaseDatabase.getInstance().getReference("flocks").child(key);
        flockRef.removeValue();
    }

    public static void updateFlock(@NonNull Flock flock) {
        if(flock.getKey() == null)
            throw new RuntimeException("Flock has no key");
        else {
            DatabaseReference flockRef = FirebaseDatabase.getInstance().getReference("flocks").child(flock.getKey());
            final Map<String, Object> childUpdates = new HashMap<>();
            if(flock.getName() != null)
                childUpdates.put("/name", flock.getName());
            if(flock.getOwner() != null)
                childUpdates.put("/owner", flock.getOwner());
            if(flock.getPhotoUrl() != null)
                childUpdates.put("/photoUrl", flock.getPhotoUrl());
            if(flock.getUsers() != null)
                childUpdates.put("/users", flock.getUsers());
            flockRef.updateChildren(childUpdates);
        }
    }

    public static void addUserToFlock(@NonNull String uid, @NonNull String key) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference userRef = database.getReference("users").child(uid).child("flocks").child(key);
        final DatabaseReference flockRef = database.getReference("flocks").child(key).child("users").child(uid);
        userRef.setValue(true);
        flockRef.setValue("01");
    }

    public static void removeUserFromFlock(@NonNull String uid, @NonNull String key) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference userRef = database.getReference("users").child(uid).child("flocks").child(key);
        final DatabaseReference flockRef = database.getReference("flocks").child(key).child("users").child(uid);
        userRef.removeValue();
        flockRef.removeValue();
    }

    public static void createEmergency(@NonNull Emergency emergency) {
        final DatabaseReference emergencyRef = FirebaseDatabase.getInstance().getReference("emergencies").push();
        emergency.setKey(emergencyRef.getKey());
        emergencyRef.setValue(emergency);
    }

    public static void deleteEmergency(@NonNull String key) {
        final DatabaseReference emergencyRef = FirebaseDatabase.getInstance().getReference("emergencies").child(key);
        emergencyRef.removeValue();
    }

    public static void updateEmergency(@NonNull Emergency emergency) {
        if(emergency.getKey() == null)
            throw new RuntimeException("Emergency has no key");
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

    public static void createEvent(@NonNull String key, @NonNull Event event) {
        final DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("emergencies").child(key).child("events").push();
        eventRef.setValue(event);
    }

}
