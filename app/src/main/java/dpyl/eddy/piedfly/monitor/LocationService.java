package dpyl.eddy.piedfly.monitor;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import dpyl.eddy.piedfly.Constants;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.Utility;
import dpyl.eddy.piedfly.firebase.DataManager;
import dpyl.eddy.piedfly.firebase.model.SimpleLocation;

public class LocationService extends Service {

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    protected Location lastKnownLocation;
    private boolean mLocationUpdating;

    public LocationService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location bestLocation = null;
                for (Location location : locationResult.getLocations()) {
                    if (Utility.isBetterLocation(location, bestLocation)) bestLocation = location;
                }
                if (bestLocation != null && (lastKnownLocation == null || Utility.isBetterLocationGreedy(bestLocation, lastKnownLocation, 5.0))) {
                    final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    String uid = sharedPreferences.getString(getBaseContext().getString(R.string.pref_uid), "");
                    if (uid.isEmpty()) stopSelf();
                    SimpleLocation simpleLocation = new SimpleLocation(bestLocation.getTime(), bestLocation.getLatitude(), bestLocation.getLongitude());
                    DataManager.setLastKnownLocation(uid, simpleLocation);
                    lastKnownLocation = bestLocation;
                }
            }
        }; mLocationUpdating = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mLocationUpdating) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.requestLocationUpdates(createLocationRequest(), mLocationCallback, null);
                mLocationUpdating = true;
            }
        } return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        mFusedLocationClient = null;
        mLocationCallback = null;
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(Constants.LOCATION_SLOWEST_INTERVAL);
        locationRequest.setFastestInterval(Constants.LOCATION_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

}
