package dpyl.eddy.piedfly;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;

import dpyl.eddy.piedfly.model.room.models.Message;

/**
 * A class to deal with the room database directly.
 */


public class LocalDataManager {


    private static final String DATABASE_NAME = "LOCAL_DB";

    private static AppLocalDatabase roomDatabase;

    private LocalDataCallback mLocalDataCallback;
    private Context mContext;

    public LocalDataManager(Context context, LocalDataCallback localDataCallback) {
        this.mContext = context;
        this.mLocalDataCallback = localDataCallback;
        if (roomDatabase == null) {
            roomDatabase = Room.databaseBuilder(mContext, AppLocalDatabase.class, DATABASE_NAME).build();
        }
    }

    interface LocalDataCallback {

        void OnGetUnreadMessages(int unreadMessages);

    }

    public void saveMessages(Message... messages) {

        new AsyncTask<Message, Void, Void>() {
            @Override
            protected Void doInBackground(Message... messages) {
                roomDatabase.messageDao().insertAll(messages);
                return null;
            }
        }.execute();
    }

    public void deleteMessages(Message... messages) {
        new AsyncTask<Message, Void, Void>() {

            @Override
            protected Void doInBackground(Message... messages) {
                roomDatabase.messageDao().deleteAll(messages);
                return null;
            }
        }.execute();
    }

    public void subscribeToGetUnreadMessages(LocalDataCallback localDataCallback) {
        new AsyncTask<Integer, Void, Void>() {

            @Override
            protected Void doInBackground(Integer... integers) {
                mLocalDataCallback.OnGetUnreadMessages(roomDatabase.messageDao().findUnreadMessages());
                return null;
            }
        }.execute();
    }


}
