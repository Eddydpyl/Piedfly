package dpyl.eddy.piedfly.dependencyinjection;

import android.app.Application;

import dagger.Module;
import dagger.Provides;

/**
 * Created by michael on 9/12/17.
 */

@Module
public class ApplicationModule {

    private final Application application;

    public ApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    Application provideApplication() {
        return application;
    }
}
