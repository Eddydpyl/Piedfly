package dpyl.eddy.piedfly;

import android.app.Application;
import android.content.Context;


public class App extends Application {

    private static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static App getInstance() {
        if (sInstance == null) {
            sInstance = new App();
        }
        return sInstance;
    }

    public static Context getContext() {
        return getInstance().getApplicationContext();
    }

}
