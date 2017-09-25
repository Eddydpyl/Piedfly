package dpyl.eddy.piedfly.hotbeacon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class WifiScanService extends Service {

    // TODO: Intelligently set an interval and other conditions for triggering a scan ourselves

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
