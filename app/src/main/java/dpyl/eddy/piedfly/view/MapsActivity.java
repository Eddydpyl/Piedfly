package dpyl.eddy.piedfly.view;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.AppState;
import dpyl.eddy.piedfly.Constants;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.Utility;
import dpyl.eddy.piedfly.firebase.DataManager;
import dpyl.eddy.piedfly.firebase.FileManager;
import dpyl.eddy.piedfly.firebase.GlideApp;
import dpyl.eddy.piedfly.firebase.model.SimpleLocation;
import dpyl.eddy.piedfly.firebase.model.User;
import dpyl.eddy.piedfly.view.adapter.MapUserAdapter;
import dpyl.eddy.piedfly.view.viewholder.OnListItemClickListener;

import static dpyl.eddy.piedfly.Constants.ZOOM_LEVEL;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, OnListItemClickListener {

    private static final String TAG = MapsActivity.class.getSimpleName();

    private static final String FOCUS = "focus";

    private GoogleMap mMap;
    private Map<String, Marker> mMarkers;
    private Map<String, ValueEventListener> mListeners;

    private Window mWindow;
    private Toolbar mToolbar;

    private CircleImageView mContactDetailsImage;
    private TextView mContactDetailsName;
    private TextView mContactDetailsLocation;
    private ImageView mContactDetailsCall;
    private CardView mMapDetailsCardView;
    private RecyclerView mRecyclerView;
    private MapUserAdapter mMapUserAdapter;

    private String mFocus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AppState.emergencyUser(this, mSharedPreferences) || AppState.emergencyFlock(this, mSharedPreferences)) {
            setTheme(R.style.AppThemeEmergency_NoActionBar);
            mState = true;
        }

        setContentView(R.layout.activity_maps);

        mWindow = this.getWindow();
        mToolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mContactDetailsImage = (CircleImageView) findViewById(R.id.map_contact_details_image);
        mContactDetailsLocation = (TextView) findViewById(R.id.map_contact_details_location);
        mContactDetailsName = (TextView) findViewById(R.id.map_contact_details_name);
        mContactDetailsCall = (ImageView) findViewById(R.id.map_contact_details_call);
        mMapDetailsCardView = (CardView) findViewById(R.id.map_contact_details);

        CircleImageView userImage = (CircleImageView) findViewById(R.id.map_user_image);
        StorageReference storageReference = mAuth.getCurrentUser() != null && Utility.isFirebaseStorage(mAuth.getCurrentUser().getPhotoUrl()) ? FileManager.getStorage().getReferenceFromUrl(mAuth.getCurrentUser().getPhotoUrl().toString()) : null;
        GlideApp.with(this).load(storageReference).fitCenter().centerInside().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(userImage);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                focusCamera(mAuth.getCurrentUser().getUid());
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.map_recycler);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setAdapter(mMapUserAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        if (savedInstanceState != null) mFocus = savedInstanceState.getString(FOCUS);
        else mFocus = getIntent().getStringExtra(getString(R.string.intent_uid));
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachRecyclerViewAdapter();
        setCameraListener();
        // Animate stuff
        mContactDetailsLocation.setSelected(true);
    }

    @Override
    public void OnListItemClick(int position, View view, String key) {
        super.OnListItemClick(position, view, key);
        if (view.getId() == R.id.map_contact_image || view.getId() == R.id.map_user_image)
            focusCamera(key);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        // For the map to display any data or listeners to work, the user needs to be authenticated
        if (firebaseAuth.getCurrentUser() != null) {
            attachRecyclerViewAdapter();
            setCameraListener();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop listening to changes in the database and free up memory, as the Activity is not visible
        if (mMapUserAdapter != null) {
            mMapUserAdapter.stopListening();
            mMapUserAdapter = null;
        }
        if (mListeners != null) {
            // By iterating over a copy of the Collection, we avoid a potential ConcurrentModificationException
            for (final Map.Entry<String, ValueEventListener> entry : new HashSet<>(mListeners.entrySet())) {
                DataManager.getDatabase().getReference("users").child(entry.getKey()).child("lastKnownLocation").removeEventListener(entry.getValue());
                mListeners.remove(entry.getKey());
            }
        }
        mMap.setOnCameraChangeListener(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(FOCUS, mFocus);
        super.onSaveInstanceState(outState);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.setPadding(0, 0, 0, 450); // Hardcoded value is based on Layout dimensions
            // On first launching the Activity, the calls in onStart() will fail, as the GoogleMap instance is not yet available at that point
            attachRecyclerViewAdapter();
            setCameraListener();
        }
    }

    @Override
    void toEmergencyAnimation() {
        ValueAnimator colorPrimaryAnimator = getColorAnimator(getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorSecondary), Constants.TRANSITION_ANIM_TIME);
        ValueAnimator colorDarkPrimaryAnimator = getColorAnimator(getResources().getColor(R.color.colorPrimaryDark), getResources().getColor(R.color.colorSecondaryDark), Constants.TRANSITION_ANIM_TIME);

        colorPrimaryAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatorValue = (int) animator.getAnimatedValue();
                mToolbar.setBackgroundColor(animatorValue);
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

    @Override
    void toNormalAnimation() {
        ValueAnimator colorPrimaryAnimator = getColorAnimator(getResources().getColor(R.color.colorSecondary), getResources().getColor(R.color.colorPrimary), Constants.TRANSITION_ANIM_TIME);
        ValueAnimator colorDarkPrimaryAnimator = getColorAnimator(getResources().getColor(R.color.colorSecondaryDark), getResources().getColor(R.color.colorPrimaryDark), Constants.TRANSITION_ANIM_TIME);

        colorPrimaryAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int animatorValue = (int) animator.getAnimatedValue();
                mToolbar.setBackgroundColor(animatorValue);
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

    private void attachRecyclerViewAdapter() {
        if (mAuth.getCurrentUser() != null && mMapUserAdapter == null && mMap != null) {
            if (mMarkers == null) mMarkers = new HashMap<>();
            if (!mMarkers.containsKey(mAuth.getCurrentUser().getUid())) {
                Location location = Utility.getLastKnownLocation(this);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                placeMarker(mAuth.getCurrentUser().getUid(), mAuth.getCurrentUser().getDisplayName(), latLng);
            }

            Query keyQuery = DataManager.getDatabase().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("flock");
            DatabaseReference dataQuery = DataManager.getDatabase().getReference().child("users");
            FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setIndexedQuery(keyQuery, dataQuery, User.class).build();
            mMapUserAdapter = new MapUserAdapter(options, mMap, mMarkers, this, mFocus);
            mRecyclerView.setAdapter(mMapUserAdapter);
            mMapUserAdapter.startListening();
            setDetailsScreen(mFocus);

            if (mAuth.getCurrentUser().getUid().equals(mFocus)) {
                CameraPosition cameraAnimation = new CameraPosition.Builder().target(mMarkers.get(mFocus).getPosition()).zoom(ZOOM_LEVEL)
                        .tilt(mMap.getCameraPosition().tilt).bearing(mMap.getCameraPosition().bearing).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraAnimation));
            }
        }
    }

    private void setCameraListener() {
        // Listen to the camera position and add or remove database listeners based on the currently visible Markers
        if (mAuth != null && mAuth.getCurrentUser() != null && mMarkers != null && mMap != null) {
            if (mListeners == null) mListeners = new HashMap<>();
            GoogleMap.OnCameraChangeListener listener = new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                    for (final Map.Entry<String, Marker> entry : mMarkers.entrySet()) {
                        if (latLngBounds.contains(entry.getValue().getPosition())) {
                            if (!mListeners.containsKey(entry.getKey())) {
                                ValueEventListener valueEventListener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        SimpleLocation simpleLocation = dataSnapshot.getValue(SimpleLocation.class);
                                        if (simpleLocation != null) {
                                            Marker marker = mMarkers.get(entry.getKey());
                                            marker.setPosition(new LatLng(simpleLocation.getLatitude(), simpleLocation.getLongitude()));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        // TODO: Error Handling
                                    }
                                };
                                DataManager.getDatabase().getReference("users").child(entry.getKey()).child("lastKnownLocation").addValueEventListener(valueEventListener);
                                mListeners.put(entry.getKey(), valueEventListener);
                            } else if (mListeners.containsKey(entry.getKey())) {
                                ValueEventListener valueEventListener = mListeners.get(entry.getKey());
                                DataManager.getDatabase().getReference("users").child(entry.getKey()).child("lastKnownLocation").removeEventListener(valueEventListener);
                                mListeners.remove(entry.getKey());
                            }
                        }
                    }
                }
            };
            mMap.setOnCameraChangeListener(listener);
        }
    }

    @SuppressLint("ShowToast")
    private void focusCamera(String uid) {
        if (mMap != null && mMarkers != null) {
            if (mMarkers.containsKey(uid) && !Utility.isDummyLatLng(mMarkers.get(uid).getPosition())) {
                CameraPosition cameraAnimation = new CameraPosition.Builder().target(mMarkers.get(uid).getPosition()).zoom(ZOOM_LEVEL)
                        .tilt(mMap.getCameraPosition().tilt).bearing(mMap.getCameraPosition().bearing).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraAnimation));
            } else {
                CameraPosition cameraAnimation = new CameraPosition.Builder().target(new LatLng(0.0, 0.0)).zoom(0)
                        .tilt(mMap.getCameraPosition().tilt).bearing(mMap.getCameraPosition().bearing).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraAnimation));
                showToast(Toast.makeText(this, R.string.content_no_location, Toast.LENGTH_SHORT));
            }
            setDetailsScreen(uid);
            mFocus = uid;
        }
    }

    private void setDetailsScreen(final String uid) {
        DataManager.getDatabase().getReference("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    mContactDetailsName.setText(user.getName());
                    StorageReference storageReference = user.getPhotoUrl() != null ? FileManager.getStorage().getReferenceFromUrl(user.getPhotoUrl()) : null;
                    GlideApp.with(mContactDetailsImage.getContext()).load(storageReference).fitCenter().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(mContactDetailsImage);
                    if (user.getLastKnownLocation() != null) {
                        String provider = user.getLastKnownLocation().getProvider() == null ? "" : user.getLastKnownLocation().getProvider();
                        final Location location = new Location(provider);
                        location.setLatitude(user.getLastKnownLocation().getLatitude());
                        location.setLongitude(user.getLastKnownLocation().getLongitude());
                        mContactDetailsLocation.setText(lastSeen(location));
                        mContactDetailsLocation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String geoUri = "https://www.google.com/maps/dir/?api=1&destination=" + location.getLatitude() + "," + location.getLongitude();
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW);
                                mapIntent.setData(Uri.parse(geoUri));
                                startActivity(mapIntent);
                            }
                        });
                    } else mContactDetailsLocation.setText(getString(R.string.content_no_location));
                    if (mAuth.getCurrentUser() != null && uid.equals(mAuth.getCurrentUser().getUid()))
                        mContactDetailsCall.setVisibility(View.INVISIBLE);
                    else {
                        mContactDetailsCall.setVisibility(View.VISIBLE);
                        mContactDetailsCall.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startPhoneCall(user.getPhone());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: Error Handling
            }
        });
    }

    private void placeMarker(String uid, String name, LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(name);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        Marker marker = mMap.addMarker(markerOptions);
        mMarkers.put(uid, marker);
    }

    private String lastSeen(Location location) {
        String lastSeen = getString(R.string.content_last_seen_place);
        try {
            Address address = Utility.getAddress(MapsActivity.this, location);
            if (address != null) {
                lastSeen += " " + address.getAddressLine(0);
            } else {
                lastSeen += " lat: " + location.getLatitude() + ", long: " + location.getLongitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
            lastSeen += " lat: " + location.getLatitude() + ", long: " + location.getLongitude();
        }
        return lastSeen;
    }
}
