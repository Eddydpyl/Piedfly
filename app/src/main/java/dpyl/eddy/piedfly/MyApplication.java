package dpyl.eddy.piedfly;

import android.app.Application;

import dpyl.eddy.piedfly.dependencyinjection.ApplicationComponent;
import dpyl.eddy.piedfly.dependencyinjection.ApplicationModule;
import dpyl.eddy.piedfly.dependencyinjection.DaggerApplicationComponent;
import dpyl.eddy.piedfly.dependencyinjection.RoomModule;

/**
 * Top level of our app.
 */

public class MyApplication extends Application {

    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        applicationComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .roomModule(new RoomModule(this))
                .build();

    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }
}
