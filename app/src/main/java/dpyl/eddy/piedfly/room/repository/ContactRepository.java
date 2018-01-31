package dpyl.eddy.piedfly.room.repository;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

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

    public List<Contact> getAllContacts() {
        return contactDao.findAll();
    }

    public LiveData<Contact> getContactByPhone(String phoneNumber) {
        return contactDao.findOneByPhone(phoneNumber);
    }

    public void insertAllContacts(Contact... contacts) {
        new AddContactTask().execute(contacts);
    }

    public void deleteAllContacts(Contact... contacts) {
        new DeleteContactTask().execute(contacts);
    }


    // Async Tasks:
    private class DeleteContactTask extends AsyncTask<Contact, Void, Void> {
        @Override
        protected Void doInBackground(Contact... contacts) {
            contactDao.deleteAll(contacts);
            return null;
        }
    }

    private class AddContactTask extends AsyncTask<Contact, Void, Void> {
        @Override
        protected Void doInBackground(Contact... contacts) {
            contactDao.insertAll(contacts);
            return null;
        }
    }


}
