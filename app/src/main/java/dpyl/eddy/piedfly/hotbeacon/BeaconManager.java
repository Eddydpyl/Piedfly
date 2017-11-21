package dpyl.eddy.piedfly.hotbeacon;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.preference.PreferenceManager;

import java.sql.Time;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dpyl.eddy.piedfly.Constants;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.Utility;
import dpyl.eddy.piedfly.hotbeacon.hotspotmanager.WifiApManager;
import dpyl.eddy.piedfly.model.Beacon;
import dpyl.eddy.piedfly.model.SimpleLocation;

public class BeaconManager {

    // TODO: Listen for when the network becomes available once again

    private static final String IDENTIFIER = "¬";
    private static final String SEPARATOR = "{";
    private static final String HOTSPOT_DEFAULT = "HotSpot";

    /**
     * Creates a hotspot on the device, containing information encoded in its SSID
     * @param context Necessary for some inner method calls
     * @param message The information to be encoded, along with the user's last known location and time it was updated
     */
    public static void startBeacon(Context context, String message) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String tinyID = sharedPreferences.getString(context.getString(R.string.pref_tiny_ID), "");
        SimpleLocation location = new SimpleLocation(Utility.getLastKnownLocation(context));
        Beacon beacon = new Beacon(tinyID, location, message);
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = encodeBeacon(beacon);
        WifiApManager wifiApManager = new WifiApManager(context);
        wifiApManager.setWifiApEnabled(wifiConfiguration, true);
        sharedPreferences.edit().putString(context.getString(R.string.pref_last_hotbeacon), wifiConfiguration.SSID).apply();
    }

    public static void startBeacon(Context context, Beacon beacon) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = encodeBeacon(beacon);
        WifiApManager wifiApManager = new WifiApManager(context);
        wifiApManager.setWifiApEnabled(wifiConfiguration, true);
        sharedPreferences.edit().putString(context.getString(R.string.pref_last_hotbeacon), wifiConfiguration.SSID).apply();
    }

    public static void stopBeacon(Context context, boolean reset) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        if (reset) wifiConfiguration.SSID = HOTSPOT_DEFAULT;
        WifiApManager wifiApManager = new WifiApManager(context);
        wifiApManager.setWifiApEnabled(wifiConfiguration, false);
    }

    public static boolean isLastHotBeacon(Context context, String SSID) {
        if (!isHotBeacon(SSID)) return false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastHotBeacon = sharedPreferences.getString(context.getString(R.string.pref_last_hotbeacon), "");
        return !lastHotBeacon.isEmpty() && SSID.equals(lastHotBeacon);
    }

    public static boolean isHotBeacon(String SSID) {
        return SSID != null && SSID.length() >= IDENTIFIER.length() && SSID.substring(0,IDENTIFIER.length()).equals(IDENTIFIER);
    }

    private static String encodeBeacon(Beacon beacon) {
        Time time = new Time(beacon.getLocation().getTime());
        String latitude = encodeCoordinate(beacon.getLocation().getLatitude());
        String longitude = encodeCoordinate(beacon.getLocation().getLongitude());
        String hours = Integer.toString(time.getHours(), Character.MAX_RADIX);
        String minutes = Integer.toString(time.getMinutes(), Character.MAX_RADIX);
        return IDENTIFIER + beacon.getTinyID() + latitude + longitude + SEPARATOR + hours + minutes + SEPARATOR + beacon.getMessage();
    }

    static Beacon decodeBeacon(String text) {
        int firstSymbol = -1 , lastSymbol = -1;
        int firstSeparator = text.indexOf(SEPARATOR);
        int lastSeparator = text.lastIndexOf(SEPARATOR);
        Pattern pattern = Pattern.compile("[+|-]");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) firstSymbol = matcher.start();
        matcher.reset(text.substring(firstSymbol + 1, text.length()));
        if (matcher.find()) lastSymbol = matcher.start() + text.substring(0, firstSymbol + 1).length();

        double latitude = decodeCoordinate(text.substring(firstSymbol, lastSymbol));
        double longitude = decodeCoordinate(text.substring(lastSymbol, firstSeparator));
        int hours = Integer.valueOf(text.substring(firstSeparator + SEPARATOR.length(), firstSeparator + SEPARATOR.length() + 1), Character.MAX_RADIX);
        int minutes = Integer.valueOf(text.substring(firstSeparator + SEPARATOR.length() + 1, lastSeparator), Character.MAX_RADIX);
        Date currentDate = new Date(System.currentTimeMillis());
        Date decodedDate = new Date(currentDate.getYear(), currentDate.getMonth(), currentDate.getDate(), hours, minutes);
        if (decodedDate.after(currentDate)) decodedDate.setDate(currentDate.getDate() - 1);
        long time = decodedDate.getTime();

        String tinyID = text.substring(IDENTIFIER.length(), firstSymbol);
        SimpleLocation location = new SimpleLocation(time, latitude, longitude);
        String message = text.substring(lastSeparator + SEPARATOR.length());
        return new Beacon(tinyID, location, message);
    }

    private static String encodeCoordinate(double number) {
        String symbol = number < 0 ? "-" : "+";
        String integer = Integer.toString((int)number, Character.MAX_RADIX);
        String decimal = Integer.toString((int)((number - (int)number)*Math.pow(10, Constants.BEACON_PRECISION)), Character.MAX_RADIX);
        return symbol + integer + (decimal.equals("0") ? "" : "." + decimal);
    }

    private static double decodeCoordinate(String coordinate) {
        String symbol = coordinate.substring(0,1);
        String integer = Integer.valueOf(coordinate.substring(1, coordinate.indexOf(".")), Character.MAX_RADIX).toString();
        String decimal = Integer.valueOf(coordinate.substring(coordinate.indexOf(".") + 1, coordinate.length()), Character.MAX_RADIX).toString();
        return Double.valueOf(symbol + integer + "." + decimal);
    }

}
