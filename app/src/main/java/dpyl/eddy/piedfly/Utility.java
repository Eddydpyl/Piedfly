package dpyl.eddy.piedfly;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.webkit.URLUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import static dpyl.eddy.piedfly.Constants.SIGMIN;

public class Utility {

    private static final String USA_ISO3 = "USA";

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
            throw new SecurityException("The App lacks the necessary permissions for retrieving the last known location");
        }
    }

    // TODO: Add a 'minimum distance to last known location' check. Without this, points will "jump around" when GPS is not available
    // (when the location is being triangulated from the cell towers), or you can check if the new location is outside of the accuracy value from the last known location.

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

        if (isSignificantlyNewer) {
            // If it's been more than two minutes since the current location, use the new location because the user has likely moved
            return true;
        } else if (isSignificantlyOlder) {
            // If the new location is more than two minutes older, it must be worse
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        } return false;
    }

    /**
     * Determines whether one Location reading is better than the current Location fix, giving extra weight to the latter
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     * @param greed Extra (positive) weight that is given to currentBestLocation, the new one must be at least this far away from the former
     */
    public static boolean isBetterLocationGreedy(Location location, Location currentBestLocation, Double greed) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        } else if (greed == null || greed < 0.0) greed = 0.0;

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > SIGMIN;
        boolean isSignificantlyOlder = timeDelta < -SIGMIN;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            // If it's been more than two minutes since the current location, use the new location because the user has likely moved
            return true;
        } else if (isSignificantlyOlder) {
            // If the new location is more than two minutes older, it must be worse
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
        boolean isSignificantlyMoreAccurate = accuracyDelta < -50 || (isMoreAccurate && greed == 0.0);

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

        // Check whether the new location is far enough from the current fix
        double distance = location.distanceTo(currentBestLocation);
        boolean isFarEnough = distance > greed;

        // Determine location quality using a combination of timeliness, distance and accuracy
        if (isSignificantlyMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate && isFarEnough) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider && isFarEnough) {
            return true;
        } return false;
    }

    /**
     * Determines the address of a Location
     * @return Address of the provided Location, or null if it couldn't be retrieved
     */
    public static Address getAddress(Context context, Location location) throws IOException {
        List<Address> addresses = null;
        Geocoder geocoder = new Geocoder(context);
        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
        if(addresses != null && addresses.size() > 0 ){
            return addresses.get(0);
        } else return null;
    }

    /**
     * Asynchronously loads an image from an URI into a Bitmap, which can be accessed from the BitMapTaskListener
     */
    public static void loadIntoBitmap(Application context, Uri uri, BitMapTaskListener listener) {
        BitMapTask bitMapTask = new BitMapTask(context, listener);
        bitMapTask.execute(uri);
    }

    private static class BitMapTask extends AsyncTask<Uri, Void, Void> {

        private WeakReference<Application> weakReference;
        private BitMapTaskListener listener;

        private BitMapTask(Application context, BitMapTaskListener listener) {
            weakReference = new WeakReference<Application>(context);
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(@NonNull Uri... uris) {
            for (Uri uri : uris) {
                if (URLUtil.isValidUrl(uri.toString())) {
                    try {
                        URL url = new URL(uri.toString());
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(input);
                        listener.onSuccess(bitmap);
                    } catch (IOException e) {
                        listener.onFailure(e);
                    }
                } else {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(weakReference.get().getContentResolver(), uri);
                        listener.onSuccess(bitmap);
                    } catch (IOException e) {
                        listener.onFailure(e);
                    }
                }
            } return null;
        }
    }

    public interface BitMapTaskListener {
        void onSuccess(Bitmap bitmap);
        void onFailure(Exception e);
    }

    /**
     * TODO: Only returns either 911 or 112 (default)
     * @param ISO3 The ISO3 code of the country from which the call is going to be made
     * @return Emergency number of the country with the given ISO3 code
     */
    public static String getEmergencyNumber(String ISO3) {
        if (USA_ISO3.equals(ISO3)) return "911";
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
        if (telephonyManager != null) {
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
        } return null;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isFirebaseStorage(Uri uri) {
        final String FIREBASE_STORAGE = "https://firebasestorage.googleapis.com/";
        return uri != null && uri.toString().startsWith(FIREBASE_STORAGE);
    }

    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        } return provider1.equals(provider2);
    }
}
