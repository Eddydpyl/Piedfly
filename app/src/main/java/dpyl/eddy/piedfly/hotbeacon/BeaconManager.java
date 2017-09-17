package dpyl.eddy.piedfly.hotbeacon;

import android.content.Context;
import android.net.wifi.WifiConfiguration;

import dpyl.eddy.piedfly.hotbeacon.hotspotmanager.WifiApManager;
import dpyl.eddy.piedfly.model.Emergency;

public class BeaconManager {

    /**
     * Creates a hotspot on the device, containing information pertinent to the emergency, encoded into its SSID
     * @param context Necessary for some inner method calls
     * @param emergency Emergency describing the user's situation
     */
    public static void createBeacon(Context context, Emergency emergency) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = emergency.toString();
        WifiApManager wifiApManager = new WifiApManager(context);
        wifiApManager.setWifiApEnabled(wifiConfiguration, true);
    }

}
