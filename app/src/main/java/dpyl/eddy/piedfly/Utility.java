package dpyl.eddy.piedfly;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Utility {

    private static final int SIGMIN = 1000 * 60 * 2;

    /**
     * @param context Necessary for some inner method calls
     * @return Last known location of the device
     */
    public static Location getLastKnownLocation(Context context) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = locationManager.getProviders(true);
            Location bestLocation = null;
            for (String provider : providers) {
                Location location = locationManager.getLastKnownLocation(provider);
                if(location != null && isBetterLocation(location, bestLocation)) bestLocation = location;
            } return bestLocation;
        } else {
            throw new SecurityException("The App lacks the necessary permissions");
        }
    }

    /**
     * Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > SIGMIN;
        boolean isSignificantlyOlder = timeDelta < -SIGMIN;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * TODO: Only returns either 911 or 112 (default)
     * @param ISO3 The ISO3 code of the country from which the call is going to be made
     * @return Emergency number of the country with the given ISO3 code
     */
    public static String getEmergencyNumber(String ISO3) {
        if (Constants.USA_ISO3.equals(ISO3)) return "911";
        else return "112";
    }

    /**
     * @param context Necessary for some inner method calls
     * @return The device's country ISO3 code
     */
    public static String getCountryISO3(Context context) {
        String countryISO = getCountryISO(context);
        if (countryISO != null && countryISO.matches("[a-zA-Z]{2}")) {
            Locale locale = new Locale("", countryISO);
            return locale.getISO3Country();
        } return countryISO;
    }

    /**
     * @param context Necessary for some inner method calls
     * @return The device's country ISO2 or ISO3 code
     */
    public static String getCountryISO(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryISO = telephonyManager.getSimCountryIso();
        if (countryISO != null && !countryISO.isEmpty() && countryISO.matches("[a-zA-Z]{2,3}"))
            return countryISO.toUpperCase();
        countryISO = telephonyManager.getNetworkCountryIso();
        if (countryISO != null && !countryISO.isEmpty() && countryISO.matches("[a-zA-Z]{2,3}"))
            return countryISO.toUpperCase();
        Locale locale = Locale.getDefault();
        try {
            Location lastKnownLocation = getLastKnownLocation(context);
            Geocoder geocoder = new Geocoder(context, locale);
            List<Address> addresses = geocoder.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1);
            if (addresses.size() > 0) countryISO = addresses.get(0).getCountryCode();
            if (countryISO != null && !countryISO.isEmpty() && countryISO.matches("[a-zA-Z]{2,3}"))
                return countryISO.toUpperCase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        countryISO = locale.getCountry();
        if (countryISO != null && !countryISO.isEmpty() && countryISO.matches("[a-zA-Z]{2,3}"))
            return countryISO.toUpperCase();
        return null;
    }

    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        } return provider1.equals(provider2);
    }
}
