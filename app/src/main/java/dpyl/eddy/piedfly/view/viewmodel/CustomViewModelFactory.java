package dpyl.eddy.piedfly.view.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

import dpyl.eddy.piedfly.model.room.ContactRepository;

/**
 * Created by michael on 9/12/17.
 */

@Singleton
public class CustomViewModelFactory implements ViewModelProvider.Factory {
    private final ContactRepository repository;

    @Inject
    public CustomViewModelFactory(ContactRepository repository) {
        this.repository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ContactCollectionViewModel.class)) {
            return (T) new ContactCollectionViewModel(repository);
        } else {
            throw new IllegalArgumentException("ViewModel Not Found");
        }
    }

}