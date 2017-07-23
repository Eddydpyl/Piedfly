package dpyl.eddy.piedfly;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import dpyl.eddy.piedfly.model.Flock;
import dpyl.eddy.piedfly.model.User;

public class Database {

    public static void createUser(@NonNull User user) {
        if(user.getUid() == null)
            throw new RuntimeException("User has no uid");
        else {
            final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            userRef.setValue(user);
        }
    }

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
            if(user.getDisplayName() != null)
                childUpdates.put("/displayName", user.getDisplayName());
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
        flockRef.setValue(true);
    }

    public static void removeUserFromFlock(@NonNull String uid, @NonNull String key) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference userRef = database.getReference("users").child(uid).child("flocks").child(key);
        final DatabaseReference flockRef = database.getReference("flocks").child(key).child("users").child(uid);
        userRef.removeValue();
        flockRef.removeValue();
    }

}
