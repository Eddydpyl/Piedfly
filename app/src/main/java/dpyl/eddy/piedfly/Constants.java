package dpyl.eddy.piedfly;

/**
 * Constants that control the inner workings of the App
 */

public class Constants {

    public static final int LOADED_IMAGE_RESOLUTION = 500;// Used to resize upcoming images from firebase.
    public static final String PLACEHOLDER = "placeholder"; // Used to fill a value in the database when we don't want it to be null.
    public static final double RADIUS_KM = 0.5; // Distance in kilometers for someone to be considered nearby.
    public static final int SIGMIN = 1000 * 60 * 2; // Time in milliseconds for a location to be considered significantly older/newer.
    public static final int LOCATION_SLOWEST_INTERVAL = 10000; // Maximum time in milliseconds between each lastKnownLocation update.
    public static final int LOCATION_FASTEST_INTERVAL = 5000; // Minimum time in milliseconds between each lastKnownLocation update.
    public static final int BEACON_PRECISION = 5; // Precision the location transmitted through HotBeacon: +/- 1,1 * 10^(5-x) meters.
    public static final int POWER_INTERVAL = 3000; // Maximum time in milliseconds between ACTION_SCREEN_ON or ACTION_SCREEN_OFF.
    public static final int POWER_CLICKS = 10; // Number of times the power button must be pressed before starting an Emergency.
}
