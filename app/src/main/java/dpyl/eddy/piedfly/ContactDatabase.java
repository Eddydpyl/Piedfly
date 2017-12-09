package dpyl.eddy.piedfly;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import dpyl.eddy.piedfly.model.room.Contact;
import dpyl.eddy.piedfly.model.room.dao.ContactDao;

/**
 * Deals with the Room local database implementation.
 */

@Database(entities = {Contact.class}, version = 1, exportSchema = false)
public abstract class ContactDatabase extends RoomDatabase {
    public abstract ContactDao contactDao();
}
