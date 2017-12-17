package dpyl.eddy.piedfly.room.repository;

import android.arch.lifecycle.LiveData;

import java.util.List;

import javax.inject.Inject;

import dpyl.eddy.piedfly.room.dao.MessageDao;
import dpyl.eddy.piedfly.room.model.Message;

/**
 * Repository for dealing with our Messages.
 */

public class MessageRepository {

    private final MessageDao messageDao;

    @Inject
    public MessageRepository(MessageDao messageDao) {
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
