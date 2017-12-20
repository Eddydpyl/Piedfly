package dpyl.eddy.piedfly.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.actionitembadge.library.ActionItemBadge;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dpyl.eddy.piedfly.AppPermissions;
import dpyl.eddy.piedfly.AppState;
import dpyl.eddy.piedfly.MyApplication;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.Utility;
import dpyl.eddy.piedfly.exceptions.ExceptionHandler;
import dpyl.eddy.piedfly.firebase.DataManager;
import dpyl.eddy.piedfly.firebase.FileManager;
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

    SharedPreferences mSharedPreferences;
    FirebaseAuth mAuth;
    String mPhoneNumber;

    private SharedPreferences.OnSharedPreferenceChangeListener mStateListener;
    private MessageAdapter mMessageAdapter;
    private Dialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        ((MyApplication) getApplication()).getApplicationComponent().inject(this);
        mAuth = FirebaseAuth.getInstance();
        setUpNotificationsView();
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
        } return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mStateListener = AppState.onSharedPreferenceChangeListener(this, mSharedPreferences, buildAppStateListener());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mStateListener);
        checkState();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppPermissions.REQUEST_LOCATION: {
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_LOCATION, grantResults)) startServices();
            } break;
            case AppPermissions.REQUEST_CALL_PHONE: {
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_CALL_PHONE, grantResults)) {
                    if (mPhoneNumber != null) startPhoneCall(mPhoneNumber);
                }
            } break;
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
                }
            } break;
            case PHONE_SIGN_IN: {
                if (resultCode == Activity.RESULT_OK) {
                    // The user is signed in and has a verified phone number
                    readyUser();
                } else if (resultCode == Activity.RESULT_CANCELED || data == null) {
                    // The user has pressed the back button or isn't signed in
                    checkState();
                }
            } break;
        }
    }

    @Override
    public void OnListItemClick(int position, View view, @Nullable String Key) {
        if (view.getId() == R.id.message_item) {
            Message message = mMessageAdapter.getMessages().get(position);
            MessageType messageType = MessageType.valueOf(message.getType());
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
        } mSharedPreferences = null;
    }

    // Listen to changes to the app state and update the UI accordingly
    abstract protected AppState.AppStateListener buildAppStateListener();

    // Avoid Toast queues within the application
    static void showToast(Toast toast) {
        if (mToast != null) mToast.cancel();
        mToast = toast;
        mToast.show();
    }

    void startPhoneCall(String phoneNumber) {
        this.mPhoneNumber = phoneNumber;
        if (phoneNumber != null) {
            if (AppPermissions.requestCallPermission(this)) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
            }
        } else {
            // TODO: Error Handling
        }
    }

    private void readyUser() {
        if (mAuth.getCurrentUser() != null) {
            mSharedPreferences.edit().putString(getString(R.string.pref_uid), mAuth.getCurrentUser().getUid()).apply();
            User user = new User(mAuth.getCurrentUser().getUid());
            user.setToken(mSharedPreferences.getString(getString(R.string.pref_token), null));
            user.setPhone(mAuth.getCurrentUser().getPhoneNumber());
            user.setEmail(mAuth.getCurrentUser().getEmail());
            user.setName(mAuth.getCurrentUser().getDisplayName());
            DataManager.createUser(user);
            if (mAuth.getCurrentUser().getPhotoUrl() != null) {
                Utility.loadIntoBitmap(getApplication(), mAuth.getCurrentUser().getPhotoUrl(), new Utility.BitMapTaskListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        FileManager.uploadProfilePicture(mAuth, bitmap, null, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // TODO: Error Handling
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // TODO: Error Handling
                    }
                });
            }
        } else checkState();
    }

    private void checkState() {
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
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), EMAIL_SIGN_IN);
        }
    }

    // We must always have the smallID in memory, just in case we need to start a beacon.
    private void checkSmallID() {
        if (mSharedPreferences.getString(getString(R.string.pref_tiny_ID), "").isEmpty()) {
            String uid = mSharedPreferences.getString(getString(R.string.pref_uid), "");
            if (uid.isEmpty()) readyUser();
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
        }); new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
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
}
