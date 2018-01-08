package dpyl.eddy.piedfly;

/**
 * Constants that control the inner workings of the App
 */

public class Constants {

    public static final int TRANSITION_ANIM_TIME = 1600; // Time our app takes to change colors when an emergency is triggered.
    public static final String DATABASE_NAME = "LOCAL_DB";
    public static final String POKE_NONE = "noPoke"; // Used as a Tag to identify the state of a poke icon in MainActivity.
    public static final String POKE_USER = "userPoke"; // Used as a Tag to identify the state of a poke icon in MainActivity.
    public static final String POKE_FLOCK = "flockPoke"; // Used as a Tag to identify the state of a poke icon in MainActivity.
    public static final String PLACEHOLDER = "placeholder"; // Used to fill a value in the database when we don't want it to be null.
    public static final int ZOOM_LEVEL = 15; // Default zoom level for when a GoogleMap's camera position is set.
    public static final double RADIUS_KM = 0.5; // Distance in kilometers for someone to be considered nearby.
    public static final int SIGMIN = 1000 * 60 * 2; // Time in milliseconds for a location to be considered significantly older/newer.
    public static final long LOCATION_SLOWEST_INTERVAL = 10000L; // Maximum time in milliseconds between each lastKnownLocation update.
    public static final long LOCATION_FASTEST_INTERVAL = 5000L; // Minimum time in milliseconds between each lastKnownLocation update.
    public static final int BEACON_PRECISION = 5; // Precision the location transmitted through HotBeacon: +/- 1,1 * 10^(5-x) meters.
    public static final int POWER_INTERVAL = 3000; // Maximum time in milliseconds between ACTION_SCREEN_ON or ACTION_SCREEN_OFF.
    public static final int POWER_CLICKS = 5; // Number of times the power button must be pressed before starting an Emergency.
}
