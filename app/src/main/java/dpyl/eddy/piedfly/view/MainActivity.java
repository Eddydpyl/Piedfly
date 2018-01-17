package dpyl.eddy.piedfly.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
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
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.AppPermissions;
import dpyl.eddy.piedfly.AppState;
import dpyl.eddy.piedfly.Constants;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.Utility;
import dpyl.eddy.piedfly.firebase.DataManager;
import dpyl.eddy.piedfly.firebase.FileManager;
import dpyl.eddy.piedfly.firebase.GlideApp;
import dpyl.eddy.piedfly.firebase.model.Emergency;
import dpyl.eddy.piedfly.firebase.model.Poke;
import dpyl.eddy.piedfly.firebase.model.Request;
import dpyl.eddy.piedfly.firebase.model.RequestType;
import dpyl.eddy.piedfly.firebase.model.SimpleLocation;
import dpyl.eddy.piedfly.firebase.model.User;
import dpyl.eddy.piedfly.room.model.Contact;
import dpyl.eddy.piedfly.view.adapter.ContactAdapter;
import dpyl.eddy.piedfly.view.adapter.UserAdapter;
import dpyl.eddy.piedfly.view.recyclerview.CustomItemTouchHelper;
import dpyl.eddy.piedfly.view.viewholder.UserHolder;
import dpyl.eddy.piedfly.view.viewmodel.ContactCollectionViewModel;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PICK_CONTACT = 100;
    private static final int REQUEST_PICK_IMAGE = 101;

    private Window mWindow;
    private CoordinatorLayout mCoordinatorLayout;
    private CircleImageView mCircleImageView;
    private SlideToActView mSlideForAlarm;
    private FloatingActionButton mFab;
    private RecyclerView mRecyclerView;
    private RecyclerView mSecondRecyclerView;
    private NestedScrollView mNestedScrollView;
    private Toolbar mToolbar;

    private boolean mState; // Auxiliary variable used to avoid repeating animations when AppState changes
    private String mKey; // Used for replicating a user action after they grant an Android permission
    private String mPokeType; // Used for replicating a user action after they grant an Android permission
    private DatabaseReference mPokeReference;
    private ValueEventListener mPokeListener;

    private UserAdapter mUserAdapter;
    private ContactAdapter mContactAdapter;
    static ContactCollectionViewModel mContactCollectionViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Once the Activity is ready, swap the splash screen for the actual theme
        if (AppState.emergencyUser(this, mSharedPreferences) || AppState.emergencyFlock(this, mSharedPreferences)) {
            setTheme(R.style.AppThemeEmergency_NoActionBar);
            mState = true;
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
            mState = false;
        }

        setContentView(R.layout.activity_main);

        mWindow = this.getWindow();
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mCoordinatorLayout = findViewById(R.id.content);
        mCircleImageView = (CircleImageView) findViewById(R.id.userImage);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mSlideForAlarm = (SlideToActView) findViewById(R.id.slide_for_alarm);
        mNestedScrollView = (NestedScrollView) findViewById(R.id.mainActivity_nestedScrollView);

        setUpSliderListeners();

        mFab.setOnClickListener(new View.OnClickListener() {
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

        mNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                // Floating button only appears at the end of the list
                if (!v.canScrollVertically(1)) mFab.show();
                else mFab.hide();
            }
        });

        setProfilePicture();
        setUpRecyclerViews();

        // Getting our ViewModels and data observing
        mContactCollectionViewModel = ViewModelProviders.of(this, mCustomViewModelFactory).get(ContactCollectionViewModel.class);
        mContactCollectionViewModel.getListOfContactsByName().observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(@Nullable List<Contact> contacts) {
                mContactAdapter.setContacts(contacts);
            }
        });

        // Custom made swipe on recycler view (Used to delete objects)
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new CustomItemTouchHelper(0, ItemTouchHelper.LEFT, new CustomItemTouchHelper.RecyclerItemTouchHelperListener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                showUndoMessage(viewHolder);
            }
        });
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

        // OnSwipe delete for the second recycler view
        ItemTouchHelper.SimpleCallback itemTouchHelperSecondRecycler = new CustomItemTouchHelper(0, ItemTouchHelper.LEFT, new CustomItemTouchHelper.RecyclerItemTouchHelperListener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                mContactCollectionViewModel.deleteContact(mContactAdapter.getContacts().get(position));
                mContactAdapter.notifyItemRemoved(position);
            }
        });
        new ItemTouchHelper(itemTouchHelperSecondRecycler).attachToRecyclerView(mSecondRecyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachFirebaseRecyclerViewAdapter();
        if (mUserAdapter != null) {
            mUserAdapter.startListening();
            setUpPokeListener();
        }
        // Workaround for a strange crash caused by the SlideToAct library
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getPhoneNumber() != null && !mAuth.getCurrentUser().getPhoneNumber().isEmpty())
            showAppropriateSlider();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppPermissions.REQUEST_READ_CONTACTS: {
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_READ_CONTACTS, grantResults)) {
                    startPickContact();
                }
            }
            break;
            case AppPermissions.REQUEST_EXTERNAL_STORAGE: {
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_EXTERNAL_STORAGE, grantResults)) {
                    startPickGalleryImage();
                }
            }
            break;
            case AppPermissions.REQUEST_LOCATION: {
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_LOCATION, grantResults)) {
                    if (mKey != null) {
                        if (mPokeType == null) startShowInMap(mKey);
                        else pokeAction(mKey, mPokeType);
                    }
                } else mPokeType = null;
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
                updateProfilePicture(data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    @SuppressLint("ShowToast")
    public void OnListItemClick(int position, View view, String key) {
        super.OnListItemClick(position, view, key);
        switch (view.getId()) {
            case R.id.contact_call: {
                startPhoneCall((String) view.getTag());
            }
            break;
            case R.id.contact_poke: {
                pokeAction(key, (String) view.getTag());
                this.mPokeType = (String) view.getTag();
                this.mKey = key;
            }
            break;
            case R.id.contact_image: {
                // TODO: maybe show picture bigger like whatssap
                startShowInMap(key);
                this.mKey = key;

            }
            break;
            case R.id.contact_directions: {
                startShowInMap(key);
                this.mKey = key;
            }
            break;
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        attachFirebaseRecyclerViewAdapter();
        if (mUserAdapter != null) setUpPokeListener();
        setProfilePicture();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mUserAdapter != null) {
            mUserAdapter.stopListening();
            mUserAdapter = null;
        }
        removePokeListener();
    }

    @Override
    protected AppState.AppStateListener buildAppStateListener() {

        return new AppState.AppStateListener() {

            @Override
            public void onUserEmergencyStart() {
                // The user has activated an emergency
                mUserAdapter.setEmergency(true);
                if (!mState) {
                    mState = true;
                    toEmergencyAnimation();
                }
            }

            @Override
            public void onUserEmergencyStop() {
                // The user had an emergency active and now it has been stopped
                if (mState && !AppState.emergencyFlock(MainActivity.this, mSharedPreferences)) {
                    mState = false;
                    toNormalAnimation();
                }
            }

            @Override
            public void onFlockEmergencyStart() {
                // There is at least one emergency active
                mUserAdapter.setEmergency(true);
                if (!mState) {
                    mState = true;
                    toEmergencyAnimation();
                }
            }

            @Override
            public void onFlockEmergencyStop() {
                // There was at least a flock emergency active and now they have all been stopped
                if (mState && !AppState.emergencyUser(MainActivity.this, mSharedPreferences)) {
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
                // There was at least an emergency active and now they have all been stopped
                mUserAdapter.setEmergency(false);
            }
        };
    }

    private void startEmergency() {
        if (mAuth.getCurrentUser() != null && AppPermissions.requestLocationPermission(this)) {
            Emergency emergency = new Emergency();
            String uid = mAuth.getCurrentUser().getUid();
            emergency.setUid(uid);
            emergency.setTrigger(uid);
            if (AppPermissions.requestLocationPermission(this)) {
                SimpleLocation simpleLocation = new SimpleLocation(Utility.getLastKnownLocation(this));
                emergency.setStart(simpleLocation);
            }
            String key = DataManager.startEmergency(emergency);
            AppState.registerEmergencyUser(this, mSharedPreferences, key);
            mSlideForAlarm.setText(getString(R.string.content_slide_to_cancel));
        }
    }

    private void stopEmergency() {
        String key = mSharedPreferences.getString(getString(R.string.pref_emergencies_user), "");
        if (mAuth.getCurrentUser() != null && !key.isEmpty()) {
            Emergency emergency = new Emergency(key);
            String uid = mAuth.getCurrentUser().getUid();
            emergency.setUid(uid);
            emergency.setChecker(uid);
            if (AppPermissions.requestLocationPermission(this)) {
                SimpleLocation simpleLocation = new SimpleLocation(Utility.getLastKnownLocation(this));
                emergency.setFinish(simpleLocation);
            }
            DataManager.stopEmergency(emergency);
            AppState.unRegisterEmergencyUser(this, mSharedPreferences);
            mSlideForAlarm.setText(getString(R.string.content_slide_for_alarm));
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

    @SuppressLint("ShowToast")
    private void startShowInMap(String key) {
        if (AppPermissions.requestLocationPermission(this)) {
            if (key != null) {
                Intent intent = new Intent(this, MapsActivity.class);
                intent.putExtra(getString(R.string.intent_uid), key);
                startActivity(intent);
            } else
                showToast(Toast.makeText(this, R.string.content_only_local_user_warning, Toast.LENGTH_SHORT));
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
            Cursor cursor = weakReference.get().getContentResolver().query(uris[0], new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                final String name = cursor.getString(0);
                final String phone = cursor.getString(1);
                final String normalized = cursor.getString(2);
                cursor.close();
                if (name != null && (phone != null || normalized != null)) {
                    // TODO: Format phone so that it matches the ones in the database
                    final String target = (normalized != null ? normalized : phone).replaceAll("\\s+", "");
                    DataManager.getDatabase().getReference("users").orderByChild("phone").equalTo(target).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        @SuppressLint("ShowToast")
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    User user = childSnapshot.getValue(User.class);
                                    if (user != null) {
                                        Request request = new Request(user.getUid(), uid, RequestType.JOIN_FLOCK);
                                        DataManager.requestJoinFlock(request);
                                        showToast(Toast.makeText(weakReference.get(), weakReference.get().getString(R.string.content_join_flock_request) + " " + user.getName() + ".", Toast.LENGTH_SHORT));
                                    }
                                }
                            } else {
                                // TODO: A User with the provided phone does not exist
                                Contact localContact = new Contact();
                                localContact.setName(name);
                                localContact.setPhone(target);
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

    private void setUpRecyclerViews() {
        // Firebase RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.flock_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        // Contact RecyclerView
        mSecondRecyclerView = (RecyclerView) findViewById(R.id.local_contacts_list);
        RecyclerView.LayoutManager secondLayoutManager = new LinearLayoutManager(this);
        mSecondRecyclerView.setLayoutManager(secondLayoutManager);
        mSecondRecyclerView.addItemDecoration(new DividerItemDecoration(mSecondRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        mContactAdapter = new ContactAdapter(new ArrayList<Contact>(), this);
        mSecondRecyclerView.setAdapter(mContactAdapter);

        // Makes it look like there's a single RecyclerView
        mRecyclerView.setNestedScrollingEnabled(false);
        mSecondRecyclerView.setNestedScrollingEnabled(false);
    }

    private void attachFirebaseRecyclerViewAdapter() {
        if (mAuth != null && mAuth.getCurrentUser() != null && mUserAdapter == null) {
            Query keyQuery = DataManager.getDatabase().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("flock");
            DatabaseReference dataQuery = DataManager.getDatabase().getReference().child("users");
            FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setIndexedQuery(keyQuery, dataQuery, User.class).build();
            mUserAdapter = new UserAdapter(options, this, AppState.emergencyUser(this, mSharedPreferences));
            mRecyclerView.setAdapter(mUserAdapter);
        }
    }

    private void setUpPokeListener() {
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            mPokeReference = DataManager.getDatabase().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("flock");
            mPokeListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String, String> pokes = (Map<String, String>) dataSnapshot.getValue();
                    if (pokes != null) mUserAdapter.setPokes(pokes);
                    mUserAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // TODO: Error handling
                }
            };
            mPokeReference.addValueEventListener(mPokeListener);
        }
    }

    private void removePokeListener() {
        if (mPokeReference != null && mPokeListener != null) {
            mPokeReference.removeEventListener(mPokeListener);
            mPokeReference = null;
            mPokeListener = null;
        }
    }

    //TODO: maybe cache the profile image or save it locally too
    private void setProfilePicture() {
        StorageReference storageReference = mAuth.getCurrentUser() != null && Utility.isFirebaseStorage(mAuth.getCurrentUser().getPhotoUrl()) ? FileManager.getStorage().getReferenceFromUrl(mAuth.getCurrentUser().getPhotoUrl().toString()) : null;
        GlideApp.with(this).load(storageReference).fitCenter().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(mCircleImageView);
    }

    private void updateProfilePicture(Uri uri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        GlideApp.with(mCircleImageView.getContext()).load(bitmap).fitCenter().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(mCircleImageView);
        FileManager.uploadProfilePicture(mAuth, bitmap, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "Image uploaded successfully. It can be found here: " + mAuth.getCurrentUser().getPhotoUrl());
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, getString(R.string.content_upload_error), Toast.LENGTH_SHORT).show();
                GlideApp.with(mCircleImageView.getContext()).load(R.drawable.default_contact).fitCenter().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(mCircleImageView);

            }
        });
    }

    private void showUndoMessage(RecyclerView.ViewHolder viewHolder) {
        final String uid1 = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        final String uid2 = ((UserHolder) viewHolder).uid;
        if (uid1 != null && uid2 != null) {
            DataManager.removeFromFlock(uid2, uid1);
            // Show snackBar with undo option
            DataManager.getDatabase().getReference("users").child(uid2).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, getString(R.string.content_removed) + " " + user.getName(), Snackbar.LENGTH_LONG);
                        snackbar.setAction(R.string.content_undo_caps, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                DataManager.addToFlock(uid2, uid1);
                            }
                        });
                        snackbar.setActionTextColor(Color.YELLOW);
                        snackbar.show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // TODO: Error Handling
                }
            });
        }
    }

    @SuppressLint("ShowToast")
    private void pokeAction(String key, String type) {
        if (AppPermissions.requestLocationPermission(this)) {
            SimpleLocation simpleLocation = new SimpleLocation(Utility.getLastKnownLocation(this));
            String uid = mAuth.getCurrentUser().getUid();
            switch (type) {
                case Constants.POKE_NONE: {
                    Poke poke = new Poke();
                    poke.setUid(key);
                    poke.setTrigger(uid);
                    poke.setStart(simpleLocation);
                    DataManager.startPoke(poke);
                    showToast(Toast.makeText(this, R.string.content_poke_none, Toast.LENGTH_SHORT));
                }
                break;
                case Constants.POKE_FLOCK: {
                    Poke poke = new Poke(mUserAdapter.getPokes().get(key));
                    poke.setUid(uid);
                    poke.setTrigger(key);
                    poke.setChecker(uid);
                    poke.setFinish(simpleLocation);
                    DataManager.stopPoke(poke);
                    showToast(Toast.makeText(this, R.string.content_poke_flock, Toast.LENGTH_SHORT));
                }
                break;
                case Constants.POKE_USER: {
                    Poke poke = new Poke(mUserAdapter.getPokes().get(key));
                    poke.setUid(key);
                    poke.setTrigger(uid);
                    poke.setChecker(uid);
                    poke.setFinish(simpleLocation);
                    DataManager.stopPoke(poke);
                    showToast(Toast.makeText(this, R.string.content_poke_user, Toast.LENGTH_SHORT));
                }
                break;
            }
        }
    }

    /**
     * Returns a color value animator that can be used with along an update listener to obtain the values between two colors.
     *
     * @param colorFrom Starting color
     * @param colorTo   End color
     * @param duration  Animation duration in milliseconds
     * @return Value Animator with the corresponding parameters
     */
    private ValueAnimator getColorAnimator(int colorFrom, int colorTo, int duration) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(duration);
        colorAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        return colorAnimation;
    }

    private void toEmergencyAnimation() {
        ValueAnimator colorPrimaryAnimator = getColorAnimator(getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorSecondary), Constants.TRANSITION_ANIM_TIME);
        ValueAnimator colorDarkPrimaryAnimator = getColorAnimator(getResources().getColor(R.color.colorPrimaryDark), getResources().getColor(R.color.colorSecondaryDark), Constants.TRANSITION_ANIM_TIME);

        colorPrimaryAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatorValue = (int) animator.getAnimatedValue();
                mToolbar.setBackgroundColor(animatorValue);
                int[][] states = new int[][]{
                        new int[]{android.R.attr.state_enabled},
                        new int[]{-android.R.attr.state_enabled},
                };
                mFab.setBackgroundTintList(new ColorStateList(states, new int[]{animatorValue, animatorValue}));
                mSlideForAlarm.setMOuterColor(animatorValue);
            }

        });

        colorDarkPrimaryAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatorValue = (int) animator.getAnimatedValue();
                mWindow.setStatusBarColor(animatorValue);
            }
        });

        colorPrimaryAnimator.start();
        colorDarkPrimaryAnimator.start();
    }

    private void toNormalAnimation() {
        ValueAnimator colorPrimaryAnimator = getColorAnimator(getResources().getColor(R.color.colorSecondary), getResources().getColor(R.color.colorPrimary), Constants.TRANSITION_ANIM_TIME);
        ValueAnimator colorDarkPrimaryAnimator = getColorAnimator(getResources().getColor(R.color.colorSecondaryDark), getResources().getColor(R.color.colorPrimaryDark), Constants.TRANSITION_ANIM_TIME);

        colorPrimaryAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatorValue = (int) animator.getAnimatedValue();
                mToolbar.setBackgroundColor(animatorValue);
                int[][] states = new int[][]{
                        new int[]{android.R.attr.state_enabled},
                        new int[]{-android.R.attr.state_enabled},
                };
                mFab.setBackgroundTintList(new ColorStateList(states, new int[]{animatorValue, animatorValue}));
                mSlideForAlarm.setMOuterColor(animatorValue);
            }

        });

        colorDarkPrimaryAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatorValue = (int) animator.getAnimatedValue();
                mWindow.setStatusBarColor(animatorValue);
            }
        });

        colorPrimaryAnimator.start();
        colorDarkPrimaryAnimator.start();
    }


    private void toEmergency() {
        int color = getResources().getColor(R.color.colorSecondary);
        int colorDark = getResources().getColor(R.color.colorSecondaryDark);
        mToolbar.setBackgroundColor(color);
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_enabled},
        };
        mFab.setBackgroundTintList(new ColorStateList(states, new int[]{color, color}));
        mWindow.setStatusBarColor(colorDark);
        mSlideForAlarm.setMOuterColor(color);
    }

    private void toNormal() {
        int color = getResources().getColor(R.color.colorPrimary);
        int colorDark = getResources().getColor(R.color.colorPrimaryDark);
        mToolbar.setBackgroundColor(color);
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled},
                new int[]{-android.R.attr.state_enabled},
        };
        mFab.setBackgroundTintList(new ColorStateList(states, new int[]{color, color}));
        mWindow.setStatusBarColor(colorDark);
        mSlideForAlarm.setMOuterColor(color);
    }


    /**
     * Shows the slide to cancel or the slide for alarm slider depending on this user active emergencies.
     */
    private void showAppropriateSlider() {
        if (AppState.emergencyUser(this, mSharedPreferences)) {
            mSlideForAlarm.setText(getString(R.string.content_slide_to_cancel));
            toEmergency();

        } else {
            mSlideForAlarm.setText(getString(R.string.content_slide_for_alarm));
            toNormal();
        }

    }

    private void setUpSliderListeners() {
        mSlideForAlarm.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NotNull final SlideToActView slideToActView) {
                if (AppState.emergencyUser(getApplicationContext(), mSharedPreferences)) {
                    stopEmergency();
                } else {
                    startEmergency();
                }
            }
        });

        mSlideForAlarm.setOnSlideToActAnimationEventListener(new SlideToActView.OnSlideToActAnimationEventListener() {
            @Override
            public void onSlideCompleteAnimationStarted(SlideToActView slideToActView, float v) {

            }

            @Override
            public void onSlideCompleteAnimationEnded(SlideToActView slideToActView) {
                slideToActView.resetSlider();
            }

            @Override
            public void onSlideResetAnimationStarted(SlideToActView slideToActView) {

            }

            @Override
            public void onSlideResetAnimationEnded(SlideToActView slideToActView) {

            }
        });

    }

}
