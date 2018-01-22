package dpyl.eddy.piedfly.room.repository;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import dpyl.eddy.piedfly.room.dao.MessageDao;
import dpyl.eddy.piedfly.room.model.Message;

/**
 * Repository for dealing with our Messages.
 */

public class MessageRepository {

    private static MessageRepository sInstance;

    private final MessageDao messageDao;


    public static MessageRepository getInstance(MessageDao messageDao) {
        if (sInstance == null) {
            sInstance = new MessageRepository(messageDao);
        }
        return sInstance;
    }

    private MessageRepository(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    public LiveData<List<Message>> getAllMessagesByTimestamp() {
        return messageDao.findAllByTimeStamp();
    }

    public LiveData<Message> getMessageByFirebaseKey(String firebaseKey) {
        return messageDao.findOneByFirebaseKey(firebaseKey);
    }

    public int findUnreadMessages() {
        return messageDao.findUnreadMessages();
    }

    public void insertAllMessages(Message... messages) {
        new AddMessageTask().execute(messages);
    }

    public void deleteAllMessages(Message... messages) {
        new DeleteMessageTask().execute(messages);
    }


    // Async Tasks:
    private class DeleteMessageTask extends AsyncTask<Message, Void, Void> {
        @Override
        protected Void doInBackground(Message... messages) {
            messageDao.deleteAll(messages);
            return null;
        }
    }

    private class AddMessageTask extends AsyncTask<Message, Void, Void> {
        @Override
        protected Void doInBackground(Message... messages) {
            messageDao.insertAll(messages);
            return null;
        }
    }

}
