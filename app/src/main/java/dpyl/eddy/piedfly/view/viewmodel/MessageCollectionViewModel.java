package dpyl.eddy.piedfly.view.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.List;

import dpyl.eddy.piedfly.room.AppLocalDatabase;
import dpyl.eddy.piedfly.room.model.Message;
import dpyl.eddy.piedfly.room.repository.MessageRepository;

public class MessageCollectionViewModel extends AndroidViewModel {

    private MessageRepository mRepository;
    private LiveData<List<Message>> mMessages;


    public MessageCollectionViewModel(@NonNull Application application) {
        super(application);
        mRepository = MessageRepository.getInstance(AppLocalDatabase.getInstance(application).messageDao());
    }

    public LiveData<List<Message>> getMessagesByTimestamp() {
        if (mMessages == null) {
            mMessages = mRepository.getAllMessagesByTimestamp();
        }
        return mMessages;
    }


    public void addMessage(Message message) {
        new AddMessageTask().execute(message);
    }

    public void deleteMessage(Message message) {
        new DeleteMessageTask().execute(message);
    }

    // Async Tasks:
    private class DeleteMessageTask extends AsyncTask<Message, Void, Void> {
        @Override
        protected Void doInBackground(Message... messages) {
            mRepository.deleteAllMessages(messages);
            return null;
        }
    }

    private class AddMessageTask extends AsyncTask<Message, Void, Void> {
        @Override
        protected Void doInBackground(Message... messages) {
            mRepository.insertAllMessages(messages);
            return null;
        }
    }
}
