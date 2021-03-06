package dpyl.eddy.piedfly.monitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import dpyl.eddy.piedfly.AppState;
import dpyl.eddy.piedfly.Constants;
import dpyl.eddy.piedfly.firebase.DataManager;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.Utility;
import dpyl.eddy.piedfly.firebase.model.Emergency;
import dpyl.eddy.piedfly.firebase.model.SimpleLocation;
import dpyl.eddy.piedfly.view.MainActivity;

// TODO: Passive Monitoring

public class PassiveService extends Service {

    static final String NOTIFICATION_CANCELED = "nCANCELLED";
    private static final int NOTIFICATION_ID = 12345;
    private static final int PUSH_ID = 54321;

    private BroadcastReceiver mBroadcastReceiver;

    public PassiveService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerBroadcastReceiver();
        createStickyNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null && intent.getBooleanExtra(NOTIFICATION_CANCELED, false))
            createStickyNotification();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
        stopForeground(true);
    }

    private void registerBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {

            private Long lastReceived;
            private Integer counter = 0;

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && (action.equals(Intent.ACTION_SCREEN_ON) || action.equals(Intent.ACTION_SCREEN_OFF))) {
                    long currentTime = System.currentTimeMillis();
                    if (lastReceived == null) lastReceived = currentTime;
                    if (currentTime - lastReceived < Constants.POWER_INTERVAL) {
                        if (Constants.POWER_CLICKS <= counter++) {
                            if (!AppState.emergencyUser(context)) {
                                startEmergency(context);
                            }
                            counter = 0;
                        }
                    } else counter = 0;
                    lastReceived = currentTime;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void startEmergency(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String uid = sharedPreferences.getString(context.getString(R.string.pref_uid), "");
        Emergency emergency = new Emergency();
        emergency.setUid(uid);
        emergency.setTrigger(uid);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            SimpleLocation simpleLocation = new SimpleLocation(Utility.getLastKnownLocation(this));
            emergency.setStart(simpleLocation);
        }
        String key = DataManager.startEmergency(emergency);
        AppState.registerEmergencyUser(context, sharedPreferences, key);

        final long[] pattern = {0, 500, 250, 500};
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle(context.getString(R.string.content_push_emergency_user_title))
                        .setContentText(context.getString(R.string.content_push_emergency_user_text))
                        .setContentIntent(resultPendingIntent)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setVibrate(pattern)
                        .setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) mNotificationManager.notify(PUSH_ID, mBuilder.build());
    }

    private void createStickyNotification() {
        Intent intent = new Intent(this, NotificationCancelledReceiver.class).putExtra(NOTIFICATION_CANCELED, true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle(getString(R.string.content_notification_title))
                        .setContentText(getString(R.string.content_notification_text))
                        .setOngoing(true)
                        .setShowWhen(false)
                        .setDeleteIntent(pendingIntent);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(NOTIFICATION_ID, notification);
    }

}
