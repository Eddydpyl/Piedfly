package dpyl.eddy.piedfly.view.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.os.AsyncTask;

import java.util.List;

import dpyl.eddy.piedfly.model.room.Contact;
import dpyl.eddy.piedfly.model.room.ContactRepository;


public class ContactCollectionViewModel extends ViewModel {

    private ContactRepository mRepository;


    public ContactCollectionViewModel(ContactRepository mRepository) {
        this.mRepository = mRepository;
    }

    public LiveData<List<Contact>> getListOfContactsByName() {
        return mRepository.getAllContactsByName();
    }

    public void addContact(Contact contact) {
        new AddContactTask().execute(contact);
    }

    public void deleteContact(Contact contact) {
        new DeleteContactTask().execute(contact);
    }

    public class DeleteContactTask extends AsyncTask<Contact, Void, Void> {

        @Override
        protected Void doInBackground(Contact... contacts) {
            mRepository.deleteAllContacts(contacts);
            return null;
        }
    }

    public class AddContactTask extends AsyncTask<Contact, Void, Void> {
        @Override
        protected Void doInBackground(Contact... contacts) {
            mRepository.insertAllContacts(contacts);
            return null;
        }
    }
}
