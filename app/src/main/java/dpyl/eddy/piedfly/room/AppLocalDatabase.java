package dpyl.eddy.piedfly.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import dpyl.eddy.piedfly.room.dao.ContactDao;
import dpyl.eddy.piedfly.room.dao.MessageDao;
import dpyl.eddy.piedfly.room.model.Contact;
import dpyl.eddy.piedfly.room.model.Message;

/**
 * Deals with the Room local database implementation.
 */

@Database(entities = {Contact.class, Message.class}, version = 1, exportSchema = false)
public abstract class AppLocalDatabase extends RoomDatabase {

    public abstract ContactDao contactDao();

    public abstract MessageDao messageDao();
}
