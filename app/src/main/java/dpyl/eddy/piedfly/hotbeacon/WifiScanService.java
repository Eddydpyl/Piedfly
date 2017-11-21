package dpyl.eddy.piedfly.hotbeacon;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import dpyl.eddy.piedfly.hotbeacon.hotspotmanager.WifiApManager;

public class WifiScanService extends Service {

    // TODO: Intelligently set an interval and other conditions for triggering a scan ourselves

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void scanNetworks() {
        WifiApManager wifiApManager = new WifiApManager(this);
        if (wifiApManager.isWifiApEnabled()) {
            // There's a hotspot currently enabled
            String SSID = wifiApManager.getWifiApConfiguration().SSID;
            if (BeaconManager.isHotBeacon(SSID)) {
                // The device is trying to transmit a message through HotBeacon
                BeaconManager.stopBeacon(this, false);
            } else {
                //TODO: The user is using a hotspot of his own
            }
        }
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) wifiManager.setWifiEnabled(true);
        wifiManager.startScan();
    }

}
