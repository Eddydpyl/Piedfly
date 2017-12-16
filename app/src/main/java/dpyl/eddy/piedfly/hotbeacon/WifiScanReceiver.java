package dpyl.eddy.piedfly.hotbeacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import dpyl.eddy.piedfly.firebase.DataManager;
import dpyl.eddy.piedfly.Utility;
import dpyl.eddy.piedfly.firebase.model.Beacon;
import dpyl.eddy.piedfly.firebase.model.Emergency;

public class WifiScanReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            List<ScanResult> scanResults = wifiManager.getScanResults();
            for (ScanResult scanResult : scanResults){
                // Check if the SSID matches one of our own
                if (BeaconManager.isHotBeacon(scanResult.SSID)) {
                    final Beacon beacon = BeaconManager.decodeBeacon(scanResult.SSID);
                    if (Utility.isNetworkAvailable(context)) startEmergency(beacon);
                    else BeaconManager.startBeacon(context, beacon);
                }
            }
        }
    }

    private void startEmergency(final Beacon beacon) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("tinyID").child(beacon.getTinyID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String uid = dataSnapshot.getValue(String.class);
                database.getReference("users").child(uid).child("emergency").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // If there's already an Emergency ongoing for the User, there's no need to start another
                        if (!dataSnapshot.exists()) {
                            Emergency emergency = new Emergency();
                            emergency.setUid(uid);
                            emergency.setTrigger(uid);
                            emergency.setStart(beacon.getLocation());
                            DataManager.startEmergency(emergency);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // TODO:Error handling
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO:Error handling
            }
        });
    }
}
