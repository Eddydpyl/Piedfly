package dpyl.eddy.piedfly.dependencyinjection;


import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import dpyl.eddy.piedfly.view.MainActivity;

@Singleton
@Component(modules = {ApplicationModule.class, RoomModule.class})
public interface ApplicationComponent {


    void inject(MainActivity mainActivity);

    Application application();
}
