package dpyl.eddy.piedfly.room.repository;

import android.arch.lifecycle.LiveData;

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
        messageDao.insertAll(messages);
    }

    public void deleteAllMessages(Message... messages) {
        messageDao.deleteAll(messages);
    }

}
