package dpyl.eddy.piedfly;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class AppState {

    public static void registerEmergencyUser(Context context, String emergencyKey) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        registerEmergencyUser(context, sharedPreferences, emergencyKey);
    }

    public static void registerEmergencyUser(Context context, SharedPreferences sharedPreferences, String emergencyKey) {
        sharedPreferences.edit().putString(context.getString(R.string.pref_emergencies_user), emergencyKey).apply();
    }

    public static void registerEmergencyFlock(Context context, String emergencyKey) {
        registerEmergency(context, emergencyKey, context.getString(R.string.pref_emergencies_flock));
    }

    public static void registerEmergencyFlock(Context context, SharedPreferences sharedPreferences, String emergencyKey) {
        registerEmergency(sharedPreferences, emergencyKey, context.getString(R.string.pref_emergencies_flock));
    }

    public static void registerEmergencyNearby(Context context, String emergencyKey) {
        registerEmergency(context, emergencyKey, context.getString(R.string.pref_emergencies_nearby));
    }

    public static void registerEmergencyNearby(Context context, SharedPreferences sharedPreferences, String emergencyKey) {
        registerEmergency(sharedPreferences, emergencyKey, context.getString(R.string.pref_emergencies_nearby));
    }

    public static void unRegisterEmergencyUser(Context context, String emergencyKey) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        unRegisterEmergencyUser(context, sharedPreferences, emergencyKey);
    }

    public static void unRegisterEmergencyUser(Context context, SharedPreferences sharedPreferences, String emergencyKey) {
        sharedPreferences.edit().putString(context.getString(R.string.pref_emergencies_user), emergencyKey).apply();
    }

    public static void unRegisterEmergencyFlock(Context context, String emergencyKey) {
        unRegisterEmergency(context, emergencyKey, context.getString(R.string.pref_emergencies_flock));
    }

    public static void unRegisterEmergencyFlock(Context context, SharedPreferences sharedPreferences, String emergencyKey) {
        unRegisterEmergency(sharedPreferences, emergencyKey, context.getString(R.string.pref_emergencies_flock));
    }

    public static void unRegisterEmergencyNearby(Context context, String emergencyKey) {
        unRegisterEmergency(context, emergencyKey, context.getString(R.string.pref_emergencies_nearby));
    }

    public static void unRegisterEmergencyNearby(Context context, SharedPreferences sharedPreferences, String emergencyKey) {
        unRegisterEmergency(sharedPreferences, emergencyKey, context.getString(R.string.pref_emergencies_nearby));
    }

    public static boolean emergencyUser(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return emergencyUser(context, sharedPreferences);
    }

    public static boolean emergencyUser(Context context, SharedPreferences sharedPreferences) {
        return !sharedPreferences.getString(context.getString(R.string.pref_emergencies_user), "").isEmpty();
    }

    public static boolean emergencyFlock(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return emergencyFlock(context, sharedPreferences);
    }

    public static boolean emergencyFlock(Context context, SharedPreferences sharedPreferences) {
        return !sharedPreferences.getStringSet(context.getString(R.string.pref_emergencies_flock), new HashSet<String>()).isEmpty();
    }

    public static boolean emergencyNearby(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return !emergencyNearby(context, sharedPreferences);
    }

    public static boolean emergencyNearby(Context context, SharedPreferences sharedPreferences) {
        return !sharedPreferences.getStringSet(context.getString(R.string.pref_emergencies_nearby), new HashSet<String>()).isEmpty();
    }

    public static SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener(final Context context, final SharedPreferences sharedPreferences, final AppStateListener appStateListener) {
        if (appStateListener == null) return null;
        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals(context.getString(R.string.pref_emergencies_user))) {
                    String emergency = sharedPreferences.getString(key, "");
                    if (emergency.isEmpty()) {
                        appStateListener.onUserEmergencyStop();
                        if (!emergencyActive(context, sharedPreferences)) appStateListener.onAllEmergencyStop();
                    } else {
                        appStateListener.onUserEmergencyStart();
                    }
                } else if (key.equals(context.getString(R.string.pref_emergencies_flock))) {
                    Set<String> emergencies = sharedPreferences.getStringSet(key, new HashSet<String>());
                    if (emergencies.isEmpty()) {
                        appStateListener.onFlockEmergencyStop();
                        if (!emergencyActive(context, sharedPreferences)) appStateListener.onAllEmergencyStop();
                    } else {
                        appStateListener.onFlockEmergencyStart();
                    }
                } else if (key.equals(context.getString(R.string.pref_emergencies_nearby))) {
                    Set<String> emergencies = sharedPreferences.getStringSet(key, new HashSet<String>());
                    if (emergencies.isEmpty()) {
                        appStateListener.onNearbyEmergencyStop();
                        if (!emergencyActive(context, sharedPreferences)) appStateListener.onAllEmergencyStop();
                    } else {
                        appStateListener.onNearbyEmergencyStart();
                    }
                }
            }
        }; return listener;
    }

    public interface AppStateListener {
        void onUserEmergencyStart();
        void onUserEmergencyStop();
        void onFlockEmergencyStart();
        void onFlockEmergencyStop();
        void onNearbyEmergencyStart();
        void onNearbyEmergencyStop();
        void onAllEmergencyStop();
    }

    private static void registerEmergency(Context context, String emergencyKey, String type) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        registerEmergency(sharedPreferences, emergencyKey, type);
    }

    private static void registerEmergency(SharedPreferences sharedPreferences, String emergencyKey, String type) {
        Set<String> emergencies = sharedPreferences.getStringSet(type, new HashSet<String>());
        if (emergencies.add(emergencyKey)) sharedPreferences.edit().putStringSet(type, emergencies).apply();
    }

    private static void unRegisterEmergency(Context context, String emergencyKey, String type) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        unRegisterEmergency(sharedPreferences, emergencyKey, type);
    }

    private static void unRegisterEmergency(SharedPreferences sharedPreferences, String emergencyKey, String type) {
        Set<String> emergencies = sharedPreferences.getStringSet(type, new HashSet<String>());
        if (emergencies.remove(emergencyKey)) sharedPreferences.edit().putStringSet(type, emergencies).apply();
    }

    private static boolean emergencyActive(Context context, SharedPreferences sharedPreferences) {
        boolean emergency = !sharedPreferences.getString(context.getString(R.string.pref_emergencies_user), "").isEmpty();
        emergency = emergency && !sharedPreferences.getStringSet(context.getString(R.string.pref_emergencies_flock), new HashSet<String>()).isEmpty();
        return emergency && !sharedPreferences.getStringSet(context.getString(R.string.pref_emergencies_nearby), new HashSet<String>()).isEmpty();
    }
}
