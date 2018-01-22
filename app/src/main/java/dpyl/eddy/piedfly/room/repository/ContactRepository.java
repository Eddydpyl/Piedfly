package dpyl.eddy.piedfly.room.repository;

import android.arch.lifecycle.LiveData;

import java.util.List;

import dpyl.eddy.piedfly.room.dao.ContactDao;
import dpyl.eddy.piedfly.room.model.Contact;

/**
 * Repository for dealing with our contacts.
 */

public class ContactRepository {

    private static ContactRepository sInstance;

    private final ContactDao contactDao;


    public static ContactRepository getInstance(ContactDao contactDao) {

        if (sInstance == null) {
            sInstance = new ContactRepository(contactDao);
        }

        return sInstance;
    }

    private ContactRepository(ContactDao contactDao) {
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
