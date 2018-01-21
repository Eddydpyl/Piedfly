package dpyl.eddy.piedfly.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.actionitembadge.library.ActionItemBadge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dpyl.eddy.piedfly.AppPermissions;
import dpyl.eddy.piedfly.AppState;
import dpyl.eddy.piedfly.MyApplication;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.exceptions.ExceptionHandler;
import dpyl.eddy.piedfly.firebase.DataManager;
import dpyl.eddy.piedfly.firebase.model.User;
import dpyl.eddy.piedfly.monitor.LocationService;
import dpyl.eddy.piedfly.monitor.PassiveService;
import dpyl.eddy.piedfly.room.model.Message;
import dpyl.eddy.piedfly.room.model.MessageType;
import dpyl.eddy.piedfly.view.adapter.MessageAdapter;
import dpyl.eddy.piedfly.view.recyclerview.CustomItemTouchHelper;
import dpyl.eddy.piedfly.view.viewholder.OnListItemClickListener;
import dpyl.eddy.piedfly.view.viewmodel.MessageCollectionViewModel;

@SuppressLint("Registered")
public abstract class BaseActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, OnListItemClickListener {

    static final int EMAIL_SIGN_IN = 42;
    static final int PHONE_SIGN_IN = 43;

    static Toast mToast;
    static MessageCollectionViewModel mMessageCollectionViewModel;

    @Inject
    ViewModelProvider.Factory mCustomViewModelFactory;

    String mPhoneNumber; // Used for replicating a user action after they grant an Android permission
    SharedPreferences mSharedPreferences;
    FirebaseAuth mAuth;
    boolean mState; // Auxiliary variable used to avoid repeating animations when AppState changes

    private SharedPreferences.OnSharedPreferenceChangeListener mStateListener;
    private MessageAdapter mMessageAdapter;
    private Dialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        ((MyApplication) getApplication()).getApplicationComponent().inject(this);
        mAuth = FirebaseAuth.getInstance();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setUpNotificationsView();
        mState = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        setUpNotificationsMenuItem(menu.findItem(R.id.action_badge), menu.findItem(R.id.action_bell));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_badge) {
            mDialog.show();
            return true;
        } else if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStateListener = AppState.onSharedPreferenceChangeListener(this, mSharedPreferences, buildAppStateListener());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mStateListener);
        if (!mState && AppState.emergencyUserFlock(this, mSharedPreferences)) {
            toEmergencyAnimation();
            mState = true;
        } else if (mState && !AppState.emergencyUserFlock(this, mSharedPreferences)) {
            toNormalAnimation();
            mState = false;
        }
        checkAuthState();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppPermissions.REQUEST_LOCATION: {
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_LOCATION, grantResults))
                    startServices();
            }
            break;
            case AppPermissions.REQUEST_CALL_PHONE: {
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_CALL_PHONE, grantResults)) {
                    if (mPhoneNumber != null) startPhoneCall(mPhoneNumber);
                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EMAIL_SIGN_IN: {
                if (mAuth.getCurrentUser() == null) {
                    IdpResponse response = IdpResponse.fromResultIntent(data);
                    if (resultCode == Activity.RESULT_OK && response != null) {
                        // Successfully signed in
                        if (response.getPhoneNumber() == null || response.getPhoneNumber().isEmpty()) {
                            // The user doesn't have an associated phone number
                            Intent intent = new Intent(this, PhoneActivity.class);
                            startActivityForResult(intent, PHONE_SIGN_IN);
                        }
                    } else {
                        // Sign in failed
                        if (response == null) {
                            // The user has pressed the back button
                            checkAuthState();
                        } else if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                            // TODO: Error handling
                            // We should check if there's a uid saved in SharedPreferences
                        } else if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                            // TODO: Error handling
                            // This error triggers when the user needs to update Google Play Services
                        }
                    }
                }
            }
            break;
            case PHONE_SIGN_IN: {
                if (resultCode == Activity.RESULT_OK) {
                    // The user is signed in and has a verified phone number
                    readyUserData();
                } else if (resultCode == Activity.RESULT_CANCELED || data == null) {
                    // The user has pressed the back button or isn't signed in
                    checkAuthState();
                }
            }
            break;
        }
    }

    @Override
    public void OnListItemClick(int position, View view, @Nullable String Key) {
        if (view.getId() == R.id.message_item) {
            Message message = mMessageAdapter.getMessages().get(position);
            MessageType messageType = MessageType.valueOf(message.getType());
            switch (messageType) {
                case REQUEST_FLOCK: {
                    showFlockDialog(message, position);
                }
                break;
            }
            // TODO: Actions dependant on MessageType
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop listening to changes in the app state and free up memory, as the Activity is not visible
        if (mStateListener != null) {
            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mStateListener);
            mStateListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSharedPreferences = null;
    }

    // Listen to changes to the app state and update the UI accordingly
    AppState.AppStateListener buildAppStateListener() {

        return new AppState.AppStateListener() {

            @Override
            public void onUserEmergencyStart() {
                // The user has activated an emergency
                if (!mState) {
                    mState = true;
                    toEmergencyAnimation();
                }
            }

            @Override
            public void onUserEmergencyStop() {
                // The user had an emergency active and now it has been stopped
                if (mState && !AppState.emergencyFlock(BaseActivity.this, mSharedPreferences)) {
                    mState = false;
                    toNormalAnimation();
                }
            }

            @Override
            public void onFlockEmergencyStart() {
                // There is at least one emergency active
                if (!mState) {
                    mState = true;
                    toEmergencyAnimation();
                }
            }

            @Override
            public void onFlockEmergencyStop() {
                // There was at least a flock emergency active and now they have all been stopped
                if (mState && !AppState.emergencyUser(BaseActivity.this, mSharedPreferences)) {
                    mState = false;
                    toNormalAnimation();
                }
            }

            @Override
            public void onNearbyEmergencyStart() {
                // TODO: There is at least one emergency active
            }

            @Override
            public void onNearbyEmergencyStop() {
                // TODO: There was at least a nearby emergency active and now they have all been stopped
            }

            @Override
            public void onAllEmergencyStop() {
                // TODO: There was at least an emergency active and now they have all been stopped
            }
        };
    }

    // Updates the Activity's look based on the AppState
    abstract void toEmergencyAnimation();

    // Updates the Activity's look based on the AppState
    abstract void toNormalAnimation();

    // Avoid Toast queues within the application
    static void showToast(Toast toast) {
        if (mToast != null) mToast.cancel();
        mToast = toast;
        mToast.show();
    }

    @SuppressLint("ShowToast")
    void startPhoneCall(String phoneNumber) {
        this.mPhoneNumber = phoneNumber;
        if (phoneNumber != null) {
            if (AppPermissions.requestCallPermission(this)) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
            }
        } else showToast(Toast.makeText(this, R.string.content_no_phone, Toast.LENGTH_SHORT));
    }

    /**
     * Returns a color value animator that can be used with along an update listener to obtain the values between two colors.
     *
     * @param colorFrom Starting color
     * @param colorTo   End color
     * @param duration  Animation duration in milliseconds
     * @return Value Animator with the corresponding parameters
     */
    ValueAnimator getColorAnimator(int colorFrom, int colorTo, int duration) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(duration);
        colorAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        return colorAnimation;
    }

    private void readyUserData() {
        if (mAuth.getCurrentUser() != null) {
            mSharedPreferences.edit().putString(getString(R.string.pref_uid), mAuth.getCurrentUser().getUid()).apply();
            User user = new User(mAuth.getCurrentUser().getUid());
            user.setToken(mSharedPreferences.getString(getString(R.string.pref_token), null));
            user.setEmail(mAuth.getCurrentUser().getEmail());
            user.setName(mAuth.getCurrentUser().getDisplayName());
            user.setPhone(mAuth.getCurrentUser().getPhoneNumber());
            DataManager.updateUser(user);
            readyAppState();
        } else checkAuthState();
    }

    private void readyAppState() {
        DataManager.getDatabase().getReference("users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    String emergency = user.getEmergency();
                    if (emergency != null)
                        AppState.registerEmergencyUser(BaseActivity.this, mSharedPreferences, emergency);
                    if (user.getFlock() != null) {
                        for (String uid : user.getFlock().keySet()) {
                            DataManager.getDatabase().getReference("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User user = dataSnapshot.getValue(User.class);
                                    if (user != null) {
                                        String emergency = user.getEmergency();
                                        if (emergency != null)
                                            AppState.registerEmergencyFlock(BaseActivity.this, mSharedPreferences, emergency);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // TODO: Error Handling
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: Error Handling
            }
        });
    }

    private void checkAuthState() {
        if (mAuth.getCurrentUser() != null) {
            // The user is already signed in
            if (mAuth.getCurrentUser().getPhoneNumber() == null || mAuth.getCurrentUser().getPhoneNumber().isEmpty()) {
                // The user doesn't have an associated phone number
                if (!(this instanceof PhoneActivity)) {
                    Intent intent = new Intent(this, PhoneActivity.class);
                    startActivityForResult(intent, PHONE_SIGN_IN);
                }
            } else {
                // The user has a verified phone number
                checkSmallID();
                startServices();
            }
        } else {
            // The user is not signed in
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                    .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build())).build(), EMAIL_SIGN_IN);
        }
    }

    // We must always have the smallID in memory, just in case we need to start a beacon.
    private void checkSmallID() {
        if (mSharedPreferences.getString(getString(R.string.pref_tiny_ID), "").isEmpty()) {
            String uid = mSharedPreferences.getString(getString(R.string.pref_uid), "");
            if (uid.isEmpty()) readyUserData();
            else {
                DataManager.getDatabase().getReference("users").child(uid).child("smallID").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String smallID = dataSnapshot.getValue(String.class);
                        mSharedPreferences.edit().putString(getString(R.string.pref_tiny_ID), smallID).apply();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // TODO: Error handling
                    }
                });
            }
        }
    }

    private void startServices() {
        Intent passiveService = new Intent(this, PassiveService.class);
        if (mSharedPreferences.getBoolean(getString(R.string.pref_power_toggle), true))
            startService(passiveService);
        if (AppPermissions.requestLocationPermission(this)) {
            Intent locationService = new Intent(this, LocationService.class);
            startService(locationService);
        }
    }

    private void setUpNotificationsView() {
        mDialog = new Dialog(this, R.style.Theme_AppCompat_Dialog);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mDialog.setContentView(R.layout.dialog_layout);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);

        RecyclerView recyclerView = (RecyclerView) mDialog.findViewById(R.id.list_messages);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mMessageAdapter = new MessageAdapter(new ArrayList<Message>(), this);
        recyclerView.setAdapter(mMessageAdapter);

        // Custom made swipe on recycler view (Used to delete objects)
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new CustomItemTouchHelper(0, ItemTouchHelper.LEFT, new CustomItemTouchHelper.RecyclerItemTouchHelperListener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                mMessageCollectionViewModel.deleteMessage(mMessageAdapter.getMessages().get(position));
                mMessageAdapter.notifyItemRemoved(position);
            }
        });
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    private void setUpNotificationsMenuItem(final MenuItem menuCounter, final MenuItem menuEmpty) {
        ActionItemBadge.hide(menuCounter);
        mMessageCollectionViewModel = ViewModelProviders.of(this, mCustomViewModelFactory).get(MessageCollectionViewModel.class);
        mMessageCollectionViewModel.getMessagesByTimestamp().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(@Nullable List<Message> messages) {
                // Stored messages arrive ordered by date
                mMessageAdapter.setMessages(messages);
                if (mMessageAdapter.getItemCount() > 0) {
                    ActionItemBadge.update(BaseActivity.this, menuCounter, getDrawable(R.drawable.ic_action_notifications), ActionItemBadge.BadgeStyles.DARK_GREY, mMessageAdapter.getItemCount());
                    menuEmpty.setVisible(false);
                } else {
                    ActionItemBadge.hide(menuCounter);
                    menuEmpty.setVisible(true);
                    if (mDialog.isShowing()) mDialog.dismiss();
                }
            }
        });
    }

    private void showFlockDialog(final Message message, final int position) {
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.content_dialog_flock));
            builder.setPositiveButton(getString(R.string.content_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    DataManager.addToFlock(mAuth.getCurrentUser().getUid(), message.getFirebaseKey());
                    mMessageCollectionViewModel.deleteMessage(message);
                    mMessageAdapter.notifyItemRemoved(position);
                    dialogInterface.dismiss();
                }
            }).setNegativeButton(getString(R.string.content_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
        }
    }

}
