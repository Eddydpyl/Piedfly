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
import dpyl.eddy.piedfly.Database;
import dpyl.eddy.piedfly.exceptions.ExceptionHandler;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.hotbeacon.WifiScanService;
import dpyl.eddy.piedfly.MonitorService;
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
        firebaseLogin();
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
                    requestPhoneNumber();
                }
            } else {
                // Sign in failed
                if (response == null) {
                    // The user has pressed the back button
                    firebaseLogin();
                } else if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    // TODO: Error handling
                    // We should check if there's a uid saved in SharedPreferences
                } else if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    // TODO: Error handling
                    // This error triggers when the user needs to update Google Play Services
                }
            }
        } else if (requestCode == PHONE_SIGN_IN) {
            if(resultCode == Activity.RESULT_OK){
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                sharedPreferences.edit().putString(getString(R.string.pref_uid), mAuth.getCurrentUser().getUid()).apply();
                User user = new User(mAuth.getCurrentUser().getUid());
                user.setToken(sharedPreferences.getString(getString(R.string.pref_token), null));
                user.setPhone(mAuth.getCurrentUser().getPhoneNumber());
                user.setEmail(mAuth.getCurrentUser().getEmail());
                Database.updateUser(user);
            } if (resultCode == Activity.RESULT_CANCELED || data == null) {
                // The user has pressed the back button or isn't signed in
                firebaseLogin();
            }
        }
    }

    private void firebaseLogin(){
        if (mAuth.getCurrentUser() != null) {
            // The user is already signed in
            if (mAuth.getCurrentUser().getPhoneNumber() == null || mAuth.getCurrentUser().getPhoneNumber().isEmpty()) {
                // The user doesn't have an associated phone number
                if(!(this instanceof PhoneActivity)) requestPhoneNumber();
            } else {
                // The user has a verified phone number
                if(!(this instanceof PhoneActivity)) {
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
        } else {
            // The user is not signed in
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), EMAIL_SIGN_IN);
        }
    }

    private void requestPhoneNumber(){
        Intent intent = new Intent(this, PhoneActivity.class);
        startActivityForResult(intent, PHONE_SIGN_IN);
    }
}
