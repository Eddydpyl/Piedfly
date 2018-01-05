package dpyl.eddy.piedfly.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dpyl.eddy.piedfly.R;

import static dpyl.eddy.piedfly.monitor.PassiveService.NOTIFICATION_CANCELED;

public class NotificationCancelledReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean notificationCancelled = intent.getExtras() != null && intent.getExtras().getBoolean(NOTIFICATION_CANCELED, false);
        if (notificationCancelled) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            Intent passiveService = new Intent(context, PassiveService.class);
            passiveService.putExtra(NOTIFICATION_CANCELED, true);
            if(sharedPreferences.getBoolean(context.getString(R.string.pref_power_toggle), true))
                context.startService(passiveService);
        }
    }

}
