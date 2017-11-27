package dpyl.eddy.piedfly.view;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ncorti.slidetoact.SlideToActView;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.AppPermissions;
import dpyl.eddy.piedfly.AppState;
import dpyl.eddy.piedfly.DataManager;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.Utility;
import dpyl.eddy.piedfly.model.Emergency;
import dpyl.eddy.piedfly.model.Request;
import dpyl.eddy.piedfly.model.RequestType;
import dpyl.eddy.piedfly.model.SimpleLocation;
import dpyl.eddy.piedfly.model.User;
import dpyl.eddy.piedfly.view.adapter.UserAdapter;
import dpyl.eddy.piedfly.view.recyclerview.BottomOffsetDecoration;
import dpyl.eddy.piedfly.view.recyclerview.RecyclerItemTouchHelper;

public class MainActivity extends BaseActivity implements UserAdapter.ListItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PICK_CONTACT = 100;
    private static final int REQUEST_PICK_IMAGE = 101;

    private SharedPreferences.OnSharedPreferenceChangeListener mStateListener;
    private SharedPreferences mSharedPreferences;
    private RecyclerView mRecyclerView;
    private UserAdapter mUserAdapter;
    private String mPhoneNumber;

    //TODO: lo hago aquí en vez de en data manager pk necesita implementar callbacks, ver si cambiar
    private static FirebaseStorage mFirebaseStorage;

    private CoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Once the Activity is ready, swap the splash screen for the actual theme
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (mFirebaseStorage == null) {
            mFirebaseStorage = FirebaseStorage.getInstance();
        }
        mCoordinatorLayout = findViewById(R.id.mainActivityRootLayout);

        final CircleImageView userImage = (CircleImageView) findViewById(R.id.userImage);
        final ImageView userDirections = (ImageView) findViewById(R.id.userDirections);
        final SlideToActView slideForAlarm = (SlideToActView) findViewById(R.id.slide_for_alarm);
        final FloatingActionButton faButton = (FloatingActionButton) findViewById(R.id.fab);

        faButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPickContact();
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPickGalleryImage();
            }
        });

        userDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), MapsActivity.class));
            }
        });

        slideForAlarm.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NotNull final SlideToActView slideToActView) {
                startEmergency();
                slideForAlarm.resetSlider();
            }
        });

        setUpRecycler();

        // Custom made swipe on recycler view (Used to delete objects)
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, new RecyclerItemTouchHelper.RecyclerItemTouchHelperListener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

                final String deleteUid = viewHolder.itemView.getTag().toString();
                final String userUID = mAuth.getCurrentUser().getUid();

                //Delete users from respective flocks
                DataManager.removeFromFlock(deleteUid, userUID);

                // showing snack bar with Undo option
                //TODO: add deleted user name
                Snackbar snackbar = Snackbar
                        .make(mCoordinatorLayout, R.string.content_removed, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.content_undo_caps, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //undo db change
                        DataManager.addToFlock(deleteUid, userUID);
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
        });
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

        // Floating button only appears at the end of the list
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)) faButton.show();
                else faButton.hide();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachRecyclerViewAdapter();
        if (mUserAdapter != null) mUserAdapter.startListening();
        // Listen to changes to the App state and update the UI accordingly
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mStateListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals(getString(R.string.pref_emergencies_flock))) {
                    Set<String> emergencies = mSharedPreferences.getStringSet(key, new HashSet<String>());
                    if (emergencies.isEmpty()) {
                        // TODO: There was at least an emergency active and now they have all been stopped
                    } else {
                        // TODO: There is at least one emergency active
                    }
                } else if (key.equals(getString(R.string.pref_emergencies_nearby))) {
                    Set<String> emergencies = mSharedPreferences.getStringSet(key, new HashSet<String>());
                    if (emergencies.isEmpty()) {
                        // TODO: There was at least an emergency active and now they have all been stopped
                    } else {
                        // TODO: There is at least one emergency active
                    }
                }
            }
        };
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mStateListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppPermissions.REQUEST_READ_CONTACTS:
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_READ_CONTACTS, grantResults)) {
                    startPickContact();
                }
                break;
            case AppPermissions.REQUEST_EXTERNAL_STORAGE:
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_EXTERNAL_STORAGE, grantResults)) {
                    startPickGalleryImage();
                }
                break;
            case AppPermissions.REQUEST_CALL_PHONE:
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_CALL_PHONE, grantResults)) {
                    if (mPhoneNumber != null) startPhoneCall(mPhoneNumber);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_CONTACT && resultCode == RESULT_OK) {
            Uri uriContact = data.getData();
            new ContactTask(getApplication(), mAuth.getCurrentUser().getUid()).execute(uriContact);
        }
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            Uri uriUserImage = data.getData();
            // TODO: Upload the image to Firebase and update the user's photoUrl

            String userUID = mAuth.getCurrentUser().getUid();
            Bitmap image = null;

            StorageReference photoRef = mFirebaseStorage.getReference().child(userUID + ".jpg");
            startUpload(userUID, photoRef, image);

        }
    }

    private void startUpload(String userUID, StorageReference photRef, Bitmap image) {


        User updatePhoto = new User();
        updatePhoto.setUid(userUID);
        updatePhoto.setPhotoUrl("");
        DataManager.updateUser(updatePhoto);
    }

    @Override
    public void onListItemClick(int position, View view, String uid) {
        switch (view.getId()) {
            case R.id.contact_call:
                startPhoneCall((String) view.getTag());
                break;
            case R.id.contact_image:
                // TODO: ???
                break;
            case R.id.contact_directions:
                // TODO: Open MapsActivity and point to the user's lastKnownLocation (View.getTag())
            default:
                Log.d(TAG, "Item view position: " + position);
                break;
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        super.onAuthStateChanged(firebaseAuth);
        attachRecyclerViewAdapter();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mUserAdapter != null) {
            mUserAdapter.stopListening();
            mUserAdapter = null;
        }
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mStateListener);
        mSharedPreferences = null;
    }

    private void attachRecyclerViewAdapter() {
        if (mAuth != null && mAuth.getCurrentUser() != null && mUserAdapter == null) {
            Query keyQuery = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("flock");
            DatabaseReference dataQuery = FirebaseDatabase.getInstance().getReference().child("users");
            FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setIndexedQuery(keyQuery, dataQuery, User.class).build();
            mUserAdapter = new UserAdapter(options, this, this);
            mRecyclerView.setAdapter(mUserAdapter);
        }
    }

    private void startEmergency() {
        Emergency emergency = new Emergency();
        String uid = mAuth.getCurrentUser().getUid();
        SimpleLocation simpleLocation = new SimpleLocation(Utility.getLastKnownLocation(getBaseContext()));
        emergency.setUid(uid);
        emergency.setTrigger(uid);
        emergency.setStart(simpleLocation);
        String key = DataManager.startEmergency(emergency);
        AppState.registerEmergencyFlock(getBaseContext(), key);
    }

    private void startPhoneCall(String phoneNumber) {
        this.mPhoneNumber = phoneNumber;
        if (AppPermissions.requestCallPermission(this)) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    private void startPickContact() {
        if (AppPermissions.requestReadContactsPermission(this)) {
            startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_PICK_CONTACT);
        }
    }

    private void startPickGalleryImage() {
        if (AppPermissions.requestExternalStoragePermission(this)) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
        }
    }

    private static class ContactTask extends AsyncTask<Uri, Void, Void> {

        private WeakReference<Application> weakReference;
        private String uid;

        ContactTask(Application context, String uid) {
            weakReference = new WeakReference<Application>(context);
            this.uid = uid;
        }

        @Override
        protected Void doInBackground(Uri... uris) {
            Cursor cursor = weakReference.get().getContentResolver().query(uris[0], new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                final String name = cursor.getString(0);
                final String phone = cursor.getString(1);
                cursor.close();
                // TODO: Format phone so that it matches the ones in the database
                FirebaseDatabase.getInstance().getReference("users").orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);
                            Request request = new Request(user.getUid(), uid, RequestType.JOIN_FLOCK);
                            DataManager.requestJoinFlock(request);
                            Toast toast = new Toast(weakReference.get());
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.setText(weakReference.get().getString(R.string.content_join_flock_request) + " " + name + ".");
                            toast.show();
                        } else {
                            // TODO: A User with the provided phone does not exist
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // TODO: Error Handling
                    }
                });
            }
            return null;
        }
    }

    private void setUpRecycler() {
        mRecyclerView = (RecyclerView) findViewById(R.id.flock_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setAdapter(mUserAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new BottomOffsetDecoration());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
    }
}
