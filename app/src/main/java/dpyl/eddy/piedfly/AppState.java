package dpyl.eddy.piedfly;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class AppState {

    public static void registerEmergencyUser(Context context, String emergencyKey) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(context.getString(R.string.pref_emergencies_user), emergencyKey).apply();
    }

    public static void registerEmergencyFlock(Context context, String emergencyKey) {
        registerEmergency(context, emergencyKey, context.getString(R.string.pref_emergencies_flock));
    }

    public static void registerEmergencyNearby(Context context, String emergencyKey) {
        registerEmergency(context, emergencyKey, context.getString(R.string.pref_emergencies_nearby));
    }

    public static void unRegisterEmergencyUser(Context context, String emergencyKey) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(context.getString(R.string.pref_emergencies_user), emergencyKey).apply();
    }

    public static void unRegisterEmergencyFlock(Context context, String emergencyKey) {
        unRegisterEmergency(context, emergencyKey, context.getString(R.string.pref_emergencies_flock));
    }

    public static void unRegisterEmergencyNearby(Context context, String emergencyKey) {
        unRegisterEmergency(context, emergencyKey, context.getString(R.string.pref_emergencies_nearby));
    }

    public static boolean emergencyUser(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return !sharedPreferences.getString(context.getString(R.string.pref_emergencies_user), "").isEmpty();
    }

    public static boolean emergencyFlock(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> emergencies = sharedPreferences.getStringSet(context.getString(R.string.pref_emergencies_flock), new HashSet<String>());
        return !emergencies.isEmpty();
    }

    public static boolean emergencyNearby(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> emergencies = sharedPreferences.getStringSet(context.getString(R.string.pref_emergencies_nearby), new HashSet<String>());
        return !emergencies.isEmpty();
    }

    public static SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener(final Context context, final SharedPreferences sharedPreferences, final AppStateListener appStateListener) {
        if (appStateListener == null) return null;
        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals(context.getString(R.string.pref_emergencies_user))) {
                    String emergency = sharedPreferences.getString(key, "");
                    if (emergency.isEmpty()) {
                        appStateListener.onUserEmergencyStop();
                    } else {
                        appStateListener.onUserEmergencyStart();
                    }
                } else if (key.equals(context.getString(R.string.pref_emergencies_flock))) {
                    Set<String> emergencies = sharedPreferences.getStringSet(key, new HashSet<String>());
                    if (emergencies.isEmpty()) {
                        appStateListener.onFlockEmergencyStop();
                    } else {
                        appStateListener.onFlockEmergencyStart();
                    }
                } else if (key.equals(context.getString(R.string.pref_emergencies_nearby))) {
                    Set<String> emergencies = sharedPreferences.getStringSet(key, new HashSet<String>());
                    if (emergencies.isEmpty()) {
                        appStateListener.onNearbyEmergencyStop();
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
    }

    private static void registerEmergency(Context context, String emergencyKey, String type) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> emergencies = sharedPreferences.getStringSet(type, new HashSet<String>());
        if (emergencies.add(emergencyKey)) sharedPreferences.edit().putStringSet(type, emergencies).apply();
    }

    private static void unRegisterEmergency(Context context, String emergencyKey, String type) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> emergencies = sharedPreferences.getStringSet(type, new HashSet<String>());
        if (emergencies.remove(emergencyKey)) sharedPreferences.edit().putStringSet(type, emergencies).apply();
    }

}
