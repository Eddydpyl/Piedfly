package dpyl.eddy.piedfly.model.room.repositories;

import android.arch.lifecycle.LiveData;

import java.util.List;

import javax.inject.Inject;

import dpyl.eddy.piedfly.model.room.dao.ContactDao;
import dpyl.eddy.piedfly.model.room.models.Contact;

/**
 * Repository for dealing with our contacts.
 */

public class ContactRepository {

    private final ContactDao contactDao;

    @Inject
    public ContactRepository(ContactDao contactDao) {
        this.contactDao = contactDao;
    }


    public LiveData<List<Contact>> getAllContactsByName() {
        return contactDao.findAllByName();
    }

    public LiveData<Contact> getContactByPhone(String phoneNumber) {
        return contactDao.findOneByPhone(phoneNumber);
    }

    public void insertAllContacts(Contact... contacts) {
        contactDao.insertAll(contacts);
    }

    public void deleteAllContacts(Contact... contacts) {
        contactDao.deleteAll(contacts);
    }

}
