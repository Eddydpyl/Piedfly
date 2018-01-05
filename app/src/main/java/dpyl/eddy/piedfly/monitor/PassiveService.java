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
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
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

    private static final String NOTIFICATION_CANCELED = "nCANCELLED";
    private static final int NOTIFICATION_ID = 12345;
    private static final int PUSH_ID = 54321;

    private BroadcastReceiver mBroadcastReceiver;

    public PassiveService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        registerBroadcastReceiver();
        createStickyNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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

    private void startEmergency(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String uid = sharedPreferences.getString(context.getString(R.string.pref_uid), "");
        SimpleLocation simpleLocation = new SimpleLocation(Utility.getLastKnownLocation(context));
        Emergency emergency = new Emergency();
        emergency.setUid(uid);
        emergency.setTrigger(uid);
        emergency.setStart(simpleLocation);
        String key = DataManager.startEmergency(emergency);
        AppState.registerEmergencyUser(context, key);

        final long[] pattern = {250,500,250,500};
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle(context.getString(R.string.content_push_emergency_user_title))
                        .setContentText(context.getString(R.string.content_push_emergency_user_text))
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setVibrate(pattern)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_HIGH);
        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(PUSH_ID, mBuilder.build());
    }

    private void registerBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {

            private Long lastReceived;
            private Integer counter = 0;

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                boolean notificationCancelled = intent.getExtras() != null && intent.getExtras().getBoolean(NOTIFICATION_CANCELED, false);
                if (action != null && (action.equals(Intent.ACTION_SCREEN_ON) || action.equals(Intent.ACTION_SCREEN_OFF))) {
                    long currentTime = System.currentTimeMillis();
                    if (lastReceived == null) lastReceived = currentTime;
                    if (currentTime - lastReceived < Constants.POWER_INTERVAL) {
                        if (Constants.POWER_CLICKS <= counter++) {
                            if (!AppState.emergencyUser(context)) {
                                startEmergency(context);
                            } counter = 0;
                        }
                    } else counter = 0;
                    lastReceived = currentTime;
                } else if (notificationCancelled) createStickyNotification();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void createStickyNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle(getString(R.string.content_notification_title))
                        .setContentText(getString(R.string.content_notification_text))
                        .setOngoing(true)
                        .setShowWhen(false);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(NOTIFICATION_ID, notification);
        Intent intent = new Intent(this, PassiveService.class).putExtra(NOTIFICATION_CANCELED, true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);
        builder.setDeleteIntent(pendingIntent);
    }

}
