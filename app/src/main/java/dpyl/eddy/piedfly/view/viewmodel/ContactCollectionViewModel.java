package dpyl.eddy.piedfly.view.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.List;

import dpyl.eddy.piedfly.room.AppLocalDatabase;
import dpyl.eddy.piedfly.room.model.Contact;
import dpyl.eddy.piedfly.room.repository.ContactRepository;

public class ContactCollectionViewModel extends AndroidViewModel {

    private ContactRepository mRepository;
    private LiveData<List<Contact>> mContacts;


    public ContactCollectionViewModel(@NonNull Application application) {
        super(application);
        this.mRepository = ContactRepository.getInstance(AppLocalDatabase.getInstance(application).contactDao());

    }


    public LiveData<List<Contact>> getListOfContactsByName() {
        if (mContacts == null) {
            mContacts = mRepository.getAllContactsByName();
        }
        return mContacts;
    }

    public void addContact(Contact contact) {
        new AddContactTask().execute(contact);
    }

    public void deleteContact(Contact contact) {
        new DeleteContactTask().execute(contact);
    }

    // Async Tasks:
    private class DeleteContactTask extends AsyncTask<Contact, Void, Void> {
        @Override
        protected Void doInBackground(Contact... contacts) {
            mRepository.deleteAllContacts(contacts);
            return null;
        }
    }

    private class AddContactTask extends AsyncTask<Contact, Void, Void> {
        @Override
        protected Void doInBackground(Contact... contacts) {
            mRepository.insertAllContacts(contacts);
            return null;
        }
    }
}
