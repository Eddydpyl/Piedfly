package dpyl.eddy.piedfly.view.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.os.AsyncTask;

import java.util.List;

import dpyl.eddy.piedfly.room.models.Message;
import dpyl.eddy.piedfly.room.repositories.MessageRepository;


public class MessageCollectionViewModel extends ViewModel {


    private MessageRepository mRepository;


    public MessageCollectionViewModel(MessageRepository mRepository) {
        this.mRepository = mRepository;
    }

    public LiveData<List<Message>> getMessagesByTimestamp() {
        return mRepository.getAllMessagesByTimestamp();
    }

    public void addMessage(Message message) {
        new AddMessageTask().execute(message);
    }

    public void deleteMessage(Message message) {
        new DeleteMessageTask().execute(message);
    }

    public class DeleteMessageTask extends AsyncTask<Message, Void, Void> {

        @Override
        protected Void doInBackground(Message... messages) {
            mRepository.deleteAllMessages(messages);
            return null;
        }
    }

    public class AddMessageTask extends AsyncTask<Message, Void, Void> {
        @Override
        protected Void doInBackground(Message... messages) {
            mRepository.insertAllMessages(messages);
            return null;
        }
    }
}
