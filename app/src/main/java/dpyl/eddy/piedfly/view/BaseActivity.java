package dpyl.eddy.piedfly.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;

import dpyl.eddy.piedfly.Database;
import dpyl.eddy.piedfly.ExceptionHandler;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.hotbeacon.WifiScanService;
import dpyl.eddy.piedfly.model.User;

public class BaseActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 42;

    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        Intent intent = new Intent(this, WifiScanService.class);
        startService(intent);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            // already signed in
            if (mAuth.getCurrentUser().getPhoneNumber() == null || mAuth.getCurrentUser().getPhoneNumber().isEmpty()) {
                // The user doesn't have an associated phone number
                if(!(this instanceof PhoneActivity)) requestPhoneNumber();
            } else {
                // The user has a verified phone number
            }
        } else {
            // not signed in
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == ResultCodes.OK && response != null) {
                // Successfully signed in
                if (response.getPhoneNumber() == null || response.getPhoneNumber().isEmpty()){
                    // The user doesn't have an associated phone number
                    requestPhoneNumber();
                } else {
                    // The user has a verified phone number
                    final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                    sharedPreferences.edit().putString(getString(R.string.pref_uid), mAuth.getCurrentUser().getUid()).apply();
                    String uid = mAuth.getCurrentUser().getUid();
                    String token = sharedPreferences.getString(getString(R.string.pref_token), null);
                    String phone = mAuth.getCurrentUser().getPhoneNumber();
                    String email = mAuth.getCurrentUser().getEmail();
                    User user = new User(uid, token, phone, email);
                    Database.updateUser(user);
                }
            } else {
                // Sign in failed
                if (response == null) {
                    // TODO: User pressed back button
                } else if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    // TODO: No network
                } else if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    // TODO: Unknown error
                }
            }
        }
    }

    private void requestPhoneNumber(){
        Intent intent = new Intent(this, PhoneActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
