package dpyl.eddy.piedfly.room;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

import dpyl.eddy.piedfly.room.models.Message;

/**
 * A class to deal with the room database directly.
 */

public class RoomManager {

    private static final String DATABASE_NAME = "LOCAL_DB";

    private AppLocalDatabase roomDatabase;
    private Executor executor;

    public RoomManager(Context context) {
        roomDatabase = Room.databaseBuilder(context, AppLocalDatabase.class, DATABASE_NAME).build();
        executor = new Executor() {
            @Override
            public void execute(@NonNull Runnable runnable) {
                new Thread(runnable).start();
            }
        };
    }

    public interface LocalDataCallback {
        void onCompleted(long... values);
    }

    public void saveMessage(final Message message, final LocalDataCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                long[] ids = roomDatabase.messageDao().insertAll(message);
                if (callback != null) callback.onCompleted(ids);
            }
        });
    }

    public void deleteMessage(final Message message, final LocalDataCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                int count = roomDatabase.messageDao().deleteAll(message);
                if (callback != null) callback.onCompleted(count);
            }
        });
    }

    public void getUnreadMessagesCount(@NonNull final LocalDataCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                callback.onCompleted(roomDatabase.messageDao().findUnreadMessages());
            }
        });
    }
}
