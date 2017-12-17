package dpyl.eddy.piedfly.view.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

import dpyl.eddy.piedfly.room.repository.ContactRepository;
import dpyl.eddy.piedfly.room.repository.MessageRepository;

@Singleton
public class CustomViewModelFactory implements ViewModelProvider.Factory {

    private final ContactRepository mContactRepository;
    private final MessageRepository mMessageRepository;

    @Inject
    public CustomViewModelFactory(ContactRepository contactRepository, MessageRepository messageRepository) {
        this.mContactRepository = contactRepository;
        this.mMessageRepository = messageRepository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ContactCollectionViewModel.class)) {
            return (T) new ContactCollectionViewModel(mContactRepository);
        } else if (modelClass.isAssignableFrom(MessageCollectionViewModel.class)) {
            return (T) new MessageCollectionViewModel(mMessageRepository);
        } else {
            throw new IllegalArgumentException("ViewModel Not Found");
        }
    }

}