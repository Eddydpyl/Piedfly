package dpyl.eddy.piedfly.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import dpyl.eddy.piedfly.AppPermissions;
import dpyl.eddy.piedfly.R;

import static dpyl.eddy.piedfly.AppPermissions.REQUEST_READ_PHONE_STATE;
import static dpyl.eddy.piedfly.AppPermissions.REQUEST_READ_SMS;

public class PhoneActivity extends BaseActivity {

    private final static String VERIFYING_KEY = "VERIFYING_KEY";

    private Boolean mVerifying;
    private EditText mPhoneEditText;
    private EditText mCodeEditText;
    private String mVerificationId;
    private BroadcastReceiver mSMSReceiver;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    @SuppressLint("HardwareIds")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mVerifying = savedInstanceState != null && savedInstanceState.getBoolean(VERIFYING_KEY);

        mSMSReceiver = new BroadcastReceiver() {

            @Override
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void onReceive(Context context, Intent intent) {
                if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
                    for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                        String messageOriginatingAddress = smsMessage.getDisplayOriginatingAddress();
                        String messageBody = smsMessage.getMessageBody();
                        if(messageOriginatingAddress.equals(getString(R.string.content_sms_from))){
                            String code = messageBody.substring(0, 6);
                            mCodeEditText.setText(code);
                            mVerifying = false;
                        }
                    }
                }
            }
        };

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(final PhoneAuthCredential credential) {
                linkAccounts(credential);
                mVerifying = false;
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // TODO: Invalid request
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // TODO: The SMS quota for the project has been exceeded
                } else {
                    // TODO: Unknown error
                } mVerifying = false;
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                mVerificationId = verificationId;
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }
        };

        mPhoneEditText = (EditText) findViewById(R.id.editText_phone);
        mCodeEditText = (EditText) findViewById(R.id.editText_code);
        Button sendInButton = (Button) findViewById(R.id.button_send);
        sendInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(PhoneActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED){
                    AppPermissions.requestReadSMSPermission(PhoneActivity.this);
                } else {
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(mPhoneEditText.getText().toString(), 60L, TimeUnit.SECONDS, PhoneActivity.this, mCallbacks);
                    mVerifying = true;
                }
            }
        });
        Button verifyButton = (Button) findViewById(R.id.button_verify);
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = mCodeEditText.getText().toString();
                if(mVerificationId != null && !code.isEmpty()){
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                    if(!mVerifying) linkAccounts(credential);
                } else {
                    // TODO: The verification process hasn't been started
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            AppPermissions.requestReadPhoneStatePermission(this);
        } else {
            writePhoneNumber();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mVerifying) PhoneAuthProvider.getInstance().verifyPhoneNumber(mPhoneEditText.getText().toString(), 60L, TimeUnit.SECONDS, this, mCallbacks);
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mSMSReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mSMSReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(VERIFYING_KEY, mVerifying);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    writePhoneNumber();
                } break;
            }
            case REQUEST_READ_SMS: {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(mPhoneEditText.getText().toString(), 60L, TimeUnit.SECONDS, this, mCallbacks);
                mVerifying = true;
                break;
            }
        }
    }

    @SuppressLint("HardwareIds")
    private void writePhoneNumber() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = telephonyManager.getLine1Number();
        if (phoneNumber != null && !phoneNumber.isEmpty() && !phoneNumber.equals("???????")) {
            mPhoneEditText.setText(phoneNumber);
        }
    }

    private void linkAccounts(final PhoneAuthCredential credential) {
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(PhoneActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // The user has been updated
                        FirebaseUser user = task.getResult().getUser();
                        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PhoneActivity.this);
                        sharedPreferences.edit().putString(getString(R.string.pref_uid), user.getUid()).apply();
                        exitActivity();
                    } else {
                        // Authentication failed, credentials may already be linked to another user account
                        final FirebaseUser oldUser = mAuth.getCurrentUser();
                        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                FirebaseUser newUser = task.getResult().getUser();
                                if (!oldUser.getUid().equals(newUser.getUid())) {
                                    // TODO: Merge oldUser and newUser accounts and data
                                }
                                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(PhoneActivity.this);
                                sharedPreferences.edit().putString(getString(R.string.pref_uid), mAuth.getCurrentUser().getUid()).apply();
                                exitActivity();
                            }
                        });
                    }
                }
            });
        } else {
            // No user is currently authenticated
            exitActivity();
        }
    }

    private void exitActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}