package dpyl.eddy.piedfly.firebase.messaging;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import dpyl.eddy.piedfly.firebase.DataManager;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.firebase.model.User;

public class InstantiationService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString(getString(R.string.pref_token), refreshedToken).apply();
        final String uid = sharedPreferences.getString(getString(R.string.pref_uid), "");
        if (!uid.isEmpty()) {
            User user = new User(uid);
            user.setToken(refreshedToken);
            DataManager.updateUser(user);
        }
    }
}
