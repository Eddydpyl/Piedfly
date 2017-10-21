package dpyl.eddy.piedfly.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;

import dpyl.eddy.piedfly.AppPermissions;
import dpyl.eddy.piedfly.DataManager;
import dpyl.eddy.piedfly.exceptions.ExceptionHandler;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.hotbeacon.WifiScanService;
import dpyl.eddy.piedfly.monitor.MonitorService;
import dpyl.eddy.piedfly.model.User;

public class BaseActivity extends AppCompatActivity {

    static final int EMAIL_SIGN_IN = 42;
    static final int PHONE_SIGN_IN = 43;

    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EMAIL_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == Activity.RESULT_OK && response != null) {
                // Successfully signed in
                if (response.getPhoneNumber() == null || response.getPhoneNumber().isEmpty()){
                    // The user doesn't have an associated phone number
                    Intent intent = new Intent(this, PhoneActivity.class);
                    startActivityForResult(intent, PHONE_SIGN_IN);
                }
            } else {
                // Sign in failed
                if (response == null) {
                    // The user has pressed the back button
                    checkState();
                } else if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    // TODO: Error handling
                    // We should check if there's a uid saved in SharedPreferences
                } else if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    // TODO: Error handling
                    // This error triggers when the user needs to update Google Play Services
                }
            }
        } else if (requestCode == PHONE_SIGN_IN) {
            if(resultCode == Activity.RESULT_OK) {
                // The user is signed in and has a verified phone number
                readyUser();
            }
            else if (resultCode == Activity.RESULT_CANCELED || data == null) {
                // The user has pressed the back button or isn't signed in
                checkState();
            }
        }
    }

    private void checkState() {
        if (mAuth.getCurrentUser() != null) {
            // The user is already signed in
            if (mAuth.getCurrentUser().getPhoneNumber() == null || mAuth.getCurrentUser().getPhoneNumber().isEmpty()) {
                // The user doesn't have an associated phone number
                if(!(this instanceof PhoneActivity)) {
                    Intent intent = new Intent(this, PhoneActivity.class);
                    startActivityForResult(intent, PHONE_SIGN_IN);
                }
            } else {
                // The user has a verified phone number
                startServices();
            }
        } else {
            // The user is not signed in
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), EMAIL_SIGN_IN);
        }
    }

    private void readyUser() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString(getString(R.string.pref_uid), mAuth.getCurrentUser().getUid()).apply();
        User user = new User(mAuth.getCurrentUser().getUid());
        user.setToken(sharedPreferences.getString(getString(R.string.pref_token), null));
        user.setPhone(mAuth.getCurrentUser().getPhoneNumber());
        user.setEmail(mAuth.getCurrentUser().getEmail());
        DataManager.updateUser(user);
    }

    private void startServices() {
        if (AppPermissions.requestLocationPermission(this)) {
            Intent monitorService = new Intent(this, MonitorService.class);
            startService(monitorService);
        }
        if (AppPermissions.requestWifiStatePermission(this)){
            Intent wifiScanService = new Intent(this, WifiScanService.class);
            startService(wifiScanService);
        }
    }
}
