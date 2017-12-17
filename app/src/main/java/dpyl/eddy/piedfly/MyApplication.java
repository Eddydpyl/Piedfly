package dpyl.eddy.piedfly;

import android.app.Application;

import dpyl.eddy.piedfly.room.injection.ApplicationComponent;
import dpyl.eddy.piedfly.room.injection.ApplicationModule;
import dpyl.eddy.piedfly.room.injection.DaggerApplicationComponent;
import dpyl.eddy.piedfly.room.injection.RoomModule;

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
