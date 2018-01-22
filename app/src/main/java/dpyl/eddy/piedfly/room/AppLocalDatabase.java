package dpyl.eddy.piedfly.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import dpyl.eddy.piedfly.room.dao.ContactDao;
import dpyl.eddy.piedfly.room.dao.MessageDao;
import dpyl.eddy.piedfly.room.model.Contact;
import dpyl.eddy.piedfly.room.model.Message;

/**
 * Deals with the Room local database implementation.
 */

@Database(entities = {Contact.class, Message.class}, version = 1, exportSchema = false)
@TypeConverters(value = DateConverter.class)
public abstract class AppLocalDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "local-contacts-db";

    private static AppLocalDatabase sInstance;

    public static synchronized AppLocalDatabase getInstance(Context context) {
        if (sInstance == null)
            return Room.databaseBuilder(context.getApplicationContext(), AppLocalDatabase.class, DATABASE_NAME).build();
        return sInstance;
    }

    public abstract ContactDao contactDao();

    public abstract MessageDao messageDao();
}
