package dpyl.eddy.piedfly.model.room.dao;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import dpyl.eddy.piedfly.model.room.Contact;

@Dao
public interface ContactDao {

    @Query("SELECT * FROM contacts")
    List<Contact> findAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(Contact... contacts);

    @Delete
    int deleteAll(Contact... contacts);
}
