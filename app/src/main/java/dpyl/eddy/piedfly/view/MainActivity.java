package dpyl.eddy.piedfly.view;

import android.Manifest;
import android.app.Application;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.ncorti.slidetoact.SlideToActView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.AppPermissions;
import dpyl.eddy.piedfly.AppState;
import dpyl.eddy.piedfly.MyApplication;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.Utility;
import dpyl.eddy.piedfly.firebase.DataManager;
import dpyl.eddy.piedfly.firebase.FileManager;
import dpyl.eddy.piedfly.firebase.GlideApp;
import dpyl.eddy.piedfly.firebase.model.Emergency;
import dpyl.eddy.piedfly.firebase.model.Request;
import dpyl.eddy.piedfly.firebase.model.RequestType;
import dpyl.eddy.piedfly.firebase.model.SimpleLocation;
import dpyl.eddy.piedfly.firebase.model.User;
import dpyl.eddy.piedfly.room.models.Contact;
import dpyl.eddy.piedfly.view.adapter.ContactAdapter;
import dpyl.eddy.piedfly.view.adapter.UserAdapter;
import dpyl.eddy.piedfly.view.recyclerview.UserHolderItemTouchHelper;
import dpyl.eddy.piedfly.view.viewholder.OnListItemClickListener;
import dpyl.eddy.piedfly.view.viewmodel.ContactCollectionViewModel;

public class MainActivity extends BaseActivity implements OnListItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PICK_CONTACT = 100;
    private static final int REQUEST_PICK_IMAGE = 101;

    // Used to avoid toast queues
    private static Toast mToast;

    private UserAdapter mUserAdapter;

    private CoordinatorLayout mCoordinatorLayout;
    private CircleImageView mCircleImageView;

    private RecyclerView mRecyclerView;
    private RecyclerView mSecondRecyclerView;
    //TODO: alomejor hacer algo con el scroll view para que permita overscrollear
    private NestedScrollView mNestedScrollView;

    private ContactAdapter mContactAdapter;

    static ContactCollectionViewModel mContactCollectionViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Once the Activity is ready, swap the splash screen for the actual theme
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up main user UI
        mCoordinatorLayout = findViewById(R.id.content);
        mCircleImageView = (CircleImageView) findViewById(R.id.userImage);
        StorageReference storageReference = mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getPhotoUrl() != null ? FileManager.getStorage().getReferenceFromUrl(mAuth.getCurrentUser().getPhotoUrl().toString()) : null;
        GlideApp.with(this).load(storageReference).fitCenter().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(mCircleImageView);

        final ImageView userDirections = (ImageView) findViewById(R.id.userDirections);
        final SlideToActView slideForAlarm = (SlideToActView) findViewById(R.id.slide_for_alarm);
        final FloatingActionButton faButton = (FloatingActionButton) findViewById(R.id.fab);

        faButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPickContact();
            }
        });

        mCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPickGalleryImage();
            }
        });

        userDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    AppPermissions.requestLocationPermission(MainActivity.this);
                } else {
                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                    intent.putExtra(getString(R.string.intent_uid), mAuth.getCurrentUser().getUid());
                    startActivity(intent);
                }
            }
        });

        slideForAlarm.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NotNull final SlideToActView slideToActView) {
                startEmergency();
                slideForAlarm.resetSlider();
            }
        });


        setUpRecyclers();

        // Second recycler
        if (mContactAdapter == null) {
            mContactAdapter = new ContactAdapter(new ArrayList<Contact>(), this);
            mSecondRecyclerView.setAdapter(mContactAdapter);
        }

        // Dagger injecting stuff
        ((MyApplication) getApplication()).getApplicationComponent().inject(this);

        // Getting our ViewModels and data observing
        mContactCollectionViewModel = ViewModelProviders.of(this, mCustomViewModelFactory).get(ContactCollectionViewModel.class);
        mContactCollectionViewModel.getListOfContactsByName().observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(@Nullable List<Contact> contacts) {
                mContactAdapter.setContacts(contacts);
            }
        });


        // Custom made swipe on recycler view (Used to delete objects)
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new UserHolderItemTouchHelper(0, ItemTouchHelper.LEFT, new UserHolderItemTouchHelper.RecyclerItemTouchHelperListener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                final String uid1 = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
                final String uid2 = viewHolder.itemView.getTag().toString();
                if (uid1 != null && uid2 != null) {
                    DataManager.removeFromFlock(uid2, uid1);
                    // Show snackBar with undo option
                    DataManager.getDatabase().getReference("users").child(uid2).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            Snackbar snackbar = Snackbar.make(mCoordinatorLayout, R.string.content_removed + " " + user.getName(), Snackbar.LENGTH_LONG);
                            snackbar.setAction(R.string.content_undo_caps, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    DataManager.addToFlock(uid2, uid1);
                                }
                            });
                            snackbar.setActionTextColor(Color.YELLOW);
                            snackbar.show();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // TODO: Error Handling
                        }
                    });
                }
            }
        });
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

        // OnSwipe delete for the second recycler view
        ItemTouchHelper.SimpleCallback itemTouchHelperSecondRecycler = new UserHolderItemTouchHelper(0, ItemTouchHelper.LEFT, new UserHolderItemTouchHelper.RecyclerItemTouchHelperListener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                mContactCollectionViewModel.deleteContact(mContactAdapter.getContacts().get(position));
                mContactAdapter.notifyItemRemoved(position);
            }
        });
        new ItemTouchHelper(itemTouchHelperSecondRecycler).attachToRecyclerView(mSecondRecyclerView);

        mNestedScrollView = (NestedScrollView) findViewById(R.id.main_nestedScrollView);

        // Floating button only appears at the end of the list
        mNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (!v.canScrollVertically(1)) faButton.show();
                else faButton.hide();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        attachFirebaseRecyclerViewAdapter();
        if (mUserAdapter != null) mUserAdapter.startListening();
        // Listen to changes to the App state and update the UI accordingly
        mStateListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals(getString(R.string.pref_emergencies_user))) {
                    String emergency = mSharedPreferences.getString(key, "");
                    if (emergency.isEmpty()) {
                        // TODO: The user had an emergency active and now it has been stopped
                    } else {
                        // TODO: There user has activated an emergency
                    }
                } else if (key.equals(getString(R.string.pref_emergencies_flock))) {
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_CONTACT && resultCode == RESULT_OK) {
            if (mAuth.getCurrentUser() != null)
                new ContactTask(getApplication(), mAuth.getCurrentUser().getUid()).execute(data.getData());
        } else if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                GlideApp.with(mCircleImageView.getContext()).load(bitmap).fitCenter().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(mCircleImageView);
                FileManager.uploadProfilePicture(mAuth, bitmap, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        StorageReference storageReference = mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getPhotoUrl() != null ? FileManager.getStorage().getReferenceFromUrl(mAuth.getCurrentUser().getPhotoUrl().toString()) : null;
                        GlideApp.with(mCircleImageView.getContext()).load(storageReference).fitCenter().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(mCircleImageView);
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // TODO: Error Handling
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void OnListItemClick(int position, View view) {

        switch (view.getId()) {
            case R.id.contact_call:
                startPhoneCall((String) view.getTag());
                break;
            case R.id.contact_image:
                break;
            case R.id.contact_directions:
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    AppPermissions.requestLocationPermission(MainActivity.this);
                } else {
                    if (view.getTag() == null) {
                        if (mToast != null) mToast.cancel();
                        mToast = Toast.makeText(this, R.string.content_only_local_user_warning, Toast.LENGTH_SHORT);
                        mToast.show();
                        break;
                    }
                    Intent intent = new Intent(this, MapsActivity.class);
                    intent.putExtra(getString(R.string.intent_uid), view.getTag().toString());
                    startActivity(intent);
                }
                break;
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        super.onAuthStateChanged(firebaseAuth);
        attachFirebaseRecyclerViewAdapter();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mUserAdapter != null) {
            mUserAdapter.stopListening();
            mUserAdapter = null;
        }
    }

    private void attachFirebaseRecyclerViewAdapter() {
        if (mAuth != null && mAuth.getCurrentUser() != null && mUserAdapter == null) {
            Query keyQuery = DataManager.getDatabase().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("flock");
            DatabaseReference dataQuery = DataManager.getDatabase().getReference().child("users");
            FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setIndexedQuery(keyQuery, dataQuery, User.class).build();
            mUserAdapter = new UserAdapter(options, this);
            mRecyclerView.setAdapter(mUserAdapter);
        }
    }

    private void startEmergency() {
        if (mAuth.getCurrentUser() != null) {
            Emergency emergency = new Emergency();
            String uid = mAuth.getCurrentUser().getUid();
            SimpleLocation simpleLocation = new SimpleLocation(Utility.getLastKnownLocation(MainActivity.this));
            emergency.setUid(uid);
            emergency.setTrigger(uid);
            emergency.setStart(simpleLocation);
            String key = DataManager.startEmergency(emergency);
            AppState.registerEmergencyUser(MainActivity.this, key);
        }
    }

    private void startPickContact() {
        if (AppPermissions.requestReadContactsPermission(this)) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            startActivityForResult(intent, REQUEST_PICK_CONTACT);
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
            Cursor cursor = weakReference.get().getContentResolver().query(uris[0], new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                final String name = cursor.getString(0);
                final String phone = cursor.getString(1);
                cursor.close();
                // TODO: Format phone so that it matches the ones in the database
                if (name != null && phone != null) {
                    DataManager.getDatabase().getReference("users").orderByChild("phone").equalTo(phone).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    User user = childSnapshot.getValue(User.class);
                                    Request request = new Request(user.getUid(), uid, RequestType.JOIN_FLOCK);
                                    DataManager.requestJoinFlock(request);
                                    if (mToast != null) mToast.cancel();
                                    mToast = Toast.makeText(weakReference.get(),
                                            weakReference.get().getString(R.string.content_join_flock_request) + " " + user.getName() + ".",
                                            Toast.LENGTH_SHORT);
                                    mToast.show();
                                }
                            } else {
                                // TODO: A User with the provided phone does not exist
                                Contact localContact = new Contact();
                                localContact.setName(name);
                                localContact.setPhone(phone);
                                mContactCollectionViewModel.addContact(localContact);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // TODO: Error Handling
                        }
                    });
                } else {
                    // TODO: Error Handling
                }
            }
            return null;
        }
    }

    private void setUpRecyclers() {

        // Firebase recycler
        mRecyclerView = (RecyclerView) findViewById(R.id.flock_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        // Contact recycler
        mSecondRecyclerView = (RecyclerView) findViewById(R.id.local_contacts_list);
        RecyclerView.LayoutManager secondLayoutManager = new LinearLayoutManager(this);
        mSecondRecyclerView.setLayoutManager(secondLayoutManager);
        mSecondRecyclerView.addItemDecoration(new DividerItemDecoration(mSecondRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        // So that it looks just like 1 recycler
        mRecyclerView.setNestedScrollingEnabled(false);
        mSecondRecyclerView.setNestedScrollingEnabled(false);

    }

}
