package dpyl.eddy.piedfly.monitor;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import dpyl.eddy.piedfly.Constants;
import dpyl.eddy.piedfly.DataManager;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.Utility;
import dpyl.eddy.piedfly.model.Emergency;
import dpyl.eddy.piedfly.model.SimpleLocation;

// TODO: Passive Monitoring

public class PassiveService extends Service {

    private static  final int NOTIFICATION_ID = 861997;

    private BroadcastReceiver mBroadcastReceiver;
    private boolean mBroadcastReceiverOn;

    public PassiveService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        mBroadcastReceiverOn = false;
        mBroadcastReceiver = new BroadcastReceiver() {

            private Long lastReceived = 0L;
            private int counter = 0;

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_SCREEN_ON) || action.equals(Intent.ACTION_SCREEN_OFF)) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastReceived < Constants.POWER_INTERVAL) {
                        counter = counter + 1;
                        if (counter >= Constants.POWER_CLICKS) {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                            String uid = sharedPreferences.getString(context.getString(R.string.pref_uid), "");
                            SimpleLocation simpleLocation = new SimpleLocation(Utility.getLastKnownLocation(context));
                            Emergency emergency = new Emergency();
                            emergency.setUid(uid);
                            emergency.setTrigger(uid);
                            emergency.setStart(simpleLocation);
                            DataManager.startEmergency(emergency);
                            // TODO: Open the App for the user
                        }
                    } else counter = 0;
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean powerToggle = sharedPreferences.getBoolean(getString(R.string.pref_power_toggle), true);
        if (powerToggle && !mBroadcastReceiverOn) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            registerReceiver(mBroadcastReceiver, filter);
            mBroadcastReceiverOn = true;
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
        } else if (!powerToggle && mBroadcastReceiverOn) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiverOn = false;
            stopForeground(true);
        } return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiverOn) {
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiverOn = false;
        } stopForeground(true);
    }
}
