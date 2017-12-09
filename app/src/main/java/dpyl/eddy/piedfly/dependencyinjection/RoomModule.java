package dpyl.eddy.piedfly.dependencyinjection;

import android.app.Application;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.persistence.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dpyl.eddy.piedfly.ContactDatabase;
import dpyl.eddy.piedfly.model.room.ContactRepository;
import dpyl.eddy.piedfly.model.room.dao.ContactDao;
import dpyl.eddy.piedfly.view.viewmodel.CustomViewModelFactory;

/**
 * Modules are responsible for creating/satisfying dependencies.
 */
@Module
public class RoomModule {

    private final ContactDatabase database;

    public RoomModule(Application application) {
        this.database = Room.databaseBuilder(
                application,
                ContactDatabase.class,
                "contact.db"
        ).build();
    }

    @Provides
    @Singleton
    ContactRepository provideContactRepository(ContactDao contactDao) {
        return new ContactRepository(contactDao);
    }

    @Provides
    @Singleton
    ContactDao provideContactDao(ContactDatabase database) {
        return database.contactDao();
    }

    @Provides
    @Singleton
    ContactDatabase provideContactDatabase(Application application) {
        return database;
    }

    @Provides
    @Singleton
    ViewModelProvider.Factory provideViewModelFactory(ContactRepository repository) {
        return new CustomViewModelFactory(repository);
    }
}