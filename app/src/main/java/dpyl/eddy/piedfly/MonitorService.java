package dpyl.eddy.piedfly;

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

import dpyl.eddy.piedfly.model.SimpleLocation;

public class MonitorService extends Service {

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    public MonitorService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mLocationCallback = new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location bestLocation = null;
                    for (Location location : locationResult.getLocations()) {
                        if (Utility.isBetterLocation(location, bestLocation)) bestLocation = location;
                    }
                    final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    String uid = sharedPreferences.getString(getBaseContext().getString(R.string.pref_uid), "");
                    if (bestLocation != null && !uid.isEmpty()) {
                        SimpleLocation simpleLocation = new SimpleLocation(bestLocation.getTime(), bestLocation.getLatitude(), bestLocation.getLongitude());
                        Database.setLastKnownLocation(uid, simpleLocation);
                    }
                };
            };
            mFusedLocationClient.requestLocationUpdates(createLocationRequest(), mLocationCallback, null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

}
