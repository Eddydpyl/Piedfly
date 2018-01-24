package dpyl.eddy.piedfly.room;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A class to deal with the room database directly.
 */


//TODO: this class is not used right now, but's useful in case we find any problems with the current modeling of the db

public class DatabaseCreator {


    private static DatabaseCreator sInstance;

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();
    private final AtomicBoolean mInitializing = new AtomicBoolean(true);

    private AppLocalDatabase mDb;

    // For Singleton instantiation
    private static final Object LOCK = new Object();

    public synchronized static DatabaseCreator getInstance(Application application) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new DatabaseCreator(application);
                }
            }
        }
        return sInstance;
    }

    public DatabaseCreator(Application application) {
        createDb(application);
    }

    /**
     * Used to observe when the database initialization is done.
     */
    public LiveData<Boolean> isDatabaseCreated() {
        return mIsDatabaseCreated;
    }

    @Nullable
    public AppLocalDatabase getDatabase() {
        return mDb;
    }


    /**
     * Creates or returns a previously-created database.
     */
    //TODO: fix warning
    private void createDb(Context context) {

        Log.d("DatabaseCreator", "Creating DB from " + Thread.currentThread().getName());

        if (!mInitializing.compareAndSet(true, false)) {
            return; // Already initializing
        }

        mIsDatabaseCreated.setValue(false);// Trigger an update to show a loading screen.

        new AsyncTask<Context, Void, AppLocalDatabase>() {

            @Override
            protected AppLocalDatabase doInBackground(Context... params) {


                Context context = params[0].getApplicationContext();
                // Build the database!
                return Room.databaseBuilder(context.getApplicationContext(),
                        AppLocalDatabase.class, AppLocalDatabase.DATABASE_NAME).build();
            }

            @Override
            protected void onPostExecute(AppLocalDatabase appDatabase) {
                super.onPostExecute(appDatabase);
                mDb = appDatabase;
                // Now on the main thread, notify observers that the db is created and ready.
                mIsDatabaseCreated.setValue(true);
            }
        }.execute(context.getApplicationContext());

    }

}
