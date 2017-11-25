package dpyl.eddy.piedfly;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class AppState {

    public static void registerEmergencyFlock(Context context, String emergencyKey) {
        registerEmergency(context, emergencyKey, context.getString(R.string.pref_emergencies_flock));
    }

    public static void unRegisterEmergencyFlock(Context context, String emergencyKey) {
        unRegisterEmergency(context, emergencyKey, context.getString(R.string.pref_emergencies_flock));
    }

    public static void registerEmergencyNearby(Context context, String emergencyKey) {
        registerEmergency(context, emergencyKey, context.getString(R.string.pref_emergencies_nearby));
    }

    public static void unRegisterEmergencyNearby(Context context, String emergencyKey) {
        unRegisterEmergency(context, emergencyKey, context.getString(R.string.pref_emergencies_nearby));
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

    private static void registerEmergency(Context context, String emergencyKey, String type) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> emergencies = sharedPreferences.getStringSet(context.getString(R.string.pref_emergencies_flock), new HashSet<String>());
        emergencies.add(emergencyKey);
        sharedPreferences.edit().putStringSet(context.getString(R.string.pref_emergencies_flock), emergencies).apply();
    }

    private static void unRegisterEmergency(Context context, String emergencyKey, String type) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> emergencies = sharedPreferences.getStringSet(context.getString(R.string.pref_emergencies_flock), new HashSet<String>());
        emergencies.remove(emergencyKey);
        sharedPreferences.edit().putStringSet(context.getString(R.string.pref_emergencies_flock), emergencies).apply();
    }

}
