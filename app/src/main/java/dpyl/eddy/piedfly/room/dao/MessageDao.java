package dpyl.eddy.piedfly.room.dao;


import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import dpyl.eddy.piedfly.room.models.Message;

@Dao
public interface MessageDao {

    @Query("SELECT * FROM messages ORDER BY timestamp")
    LiveData<List<Message>> findAllByTimeStamp();

    @Query("SELECT * FROM messages WHERE firebase_key=:firebaseKey")
    LiveData<Message> findOneByFirebaseKey(String firebaseKey);

    @Query("SELECT COUNT(*) FROM messages WHERE read='false'")
    int findUnreadMessages();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(Message... messages);

    @Delete
    int deleteAll(Message... messages);

}
