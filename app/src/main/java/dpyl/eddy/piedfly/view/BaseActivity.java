package dpyl.eddy.piedfly.view;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;

import dpyl.eddy.piedfly.AppPermissions;
import dpyl.eddy.piedfly.Database;
import dpyl.eddy.piedfly.exceptions.ExceptionHandler;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.hotbeacon.WifiScanService;
import dpyl.eddy.piedfly.MonitorService;
import dpyl.eddy.piedfly.model.User;

public class BaseActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 42;

    public FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Intent monitorService = new Intent(this, MonitorService.class);
            startService(monitorService);
        } else AppPermissions.requestLocationPermission(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED){
            Intent wifiScanService = new Intent(this, WifiScanService.class);
            startService(wifiScanService);
        } else AppPermissions.requestWifiStatePermission(this);

        mAuth = FirebaseAuth.getInstance();
        firebaseLogin();
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
                    User user = new User(mAuth.getCurrentUser().getUid());
                    user.setToken(sharedPreferences.getString(getString(R.string.pref_token), null));
                    user.setPhone(mAuth.getCurrentUser().getPhoneNumber());
                    user.setEmail(mAuth.getCurrentUser().getEmail());
                    Database.updateUser(user);
                }
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    firebaseLogin();
                } else if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    // TODO: No network
                } else if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    // TODO: Unknown error
                }
            }
        }
    }

    private void firebaseLogin(){
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

    private void requestPhoneNumber(){
        Intent intent = new Intent(this, PhoneActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
