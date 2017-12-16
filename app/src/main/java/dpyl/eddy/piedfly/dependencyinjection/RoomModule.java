package dpyl.eddy.piedfly.dependencyinjection;

import android.app.Application;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.persistence.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dpyl.eddy.piedfly.AppLocalDatabase;
import dpyl.eddy.piedfly.Constants;
import dpyl.eddy.piedfly.model.room.dao.ContactDao;
import dpyl.eddy.piedfly.model.room.dao.MessageDao;
import dpyl.eddy.piedfly.model.room.repositories.ContactRepository;
import dpyl.eddy.piedfly.model.room.repositories.MessageRepository;
import dpyl.eddy.piedfly.view.viewmodel.CustomViewModelFactory;

/**
 * Modules are responsible for creating/satisfying dependencies.
 */
@Module
public class RoomModule {

    private final AppLocalDatabase database;

    public RoomModule(Application application) {
        this.database = Room.databaseBuilder(
                application,
                AppLocalDatabase.class,
                Constants.DATABASE_NAME
        ).build();
    }

    @Provides
    @Singleton
    ContactRepository provideContactRepository(ContactDao contactDao) {
        return new ContactRepository(contactDao);
    }

    @Provides
    @Singleton
    ContactDao provideContactDao(AppLocalDatabase database) {
        return database.contactDao();
    }


    @Provides
    @Singleton
    MessageRepository provideMessageRepository(MessageDao messageDao) {
        return new MessageRepository(messageDao);
    }

    @Provides
    @Singleton
    MessageDao provideMessageDao(AppLocalDatabase database) {
        return database.messageDao();
    }

    @Provides
    @Singleton
    AppLocalDatabase provideAppLocalDatabase(Application application) {
        return database;
    }

    @Provides
    @Singleton
    ViewModelProvider.Factory provideContactViewModelFactory(ContactRepository contactRepository, MessageRepository messageRepository) {
        return new CustomViewModelFactory(contactRepository, messageRepository);
    }
}