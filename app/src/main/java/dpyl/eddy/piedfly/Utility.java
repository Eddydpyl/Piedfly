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

import dpyl.eddy.piedfly.exceptions.CountryIsoNotAvailableException;
import dpyl.eddy.piedfly.exceptions.DeviceNotAvailableException;
import dpyl.eddy.piedfly.exceptions.LocationNotAvailableException;

public class Utility {

    private static final long TENSEC = 10000;
    private static final long FIVMET = 5;

    /**
     *
     * @param context Necessary for some inner method calls
     * @return Last known location of the device
     */
    public static Location getLastKnownLocation(Context context) {
        Location currentLocation;
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                throw new DeviceNotAvailableException("Neither the GPS nor the Network are available");
            } else {
                List<String> providers = locationManager.getProviders(true);
                Location bestLocation = null;
                for (String provider : providers) {
                    Location location = locationManager.getLastKnownLocation(provider);
                    if (location == null) continue;
                    if (bestLocation == null) bestLocation = location;
                    else {
                        long timeDifference = location.getTime() - bestLocation.getTime();
                        float accuraryDifference = bestLocation.getAccuracy() - location.getAccuracy();
                        if(timeDifference >= TENSEC || (timeDifference >= TENSEC/2 && accuraryDifference >= FIVMET/2) || accuraryDifference >= FIVMET)
                            bestLocation = location;
                    }
                } currentLocation = bestLocation;
            }
        } else {
            throw new SecurityException("The App lacks the necessary permissions");
        }
        if (currentLocation == null)
            throw new LocationNotAvailableException("The location could not be retrieved");
        return currentLocation;
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
     *
     * @param context Necessary for some inner method calls
     * @return The device's country ISO3 code
     */
    public static String getCountryISO3(Context context) {
        String countryISO = getCountryISO(context);
        if (countryISO.matches("[a-zA-Z]{2}")) {
            Locale locale = new Locale("", countryISO);
            return locale.getISO3Country();
        } return countryISO;
    }

    /**
     *
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
        throw new CountryIsoNotAvailableException("Could not retrieve a Country ISO");
    }
}
