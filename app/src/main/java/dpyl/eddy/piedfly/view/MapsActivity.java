package dpyl.eddy.piedfly.view;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.AppPermissions;
import dpyl.eddy.piedfly.DataManager;
import dpyl.eddy.piedfly.FileManager;
import dpyl.eddy.piedfly.GlideApp;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.Utility;
import dpyl.eddy.piedfly.model.SimpleLocation;
import dpyl.eddy.piedfly.model.User;
import dpyl.eddy.piedfly.view.adapter.MapUserAdapter;
import dpyl.eddy.piedfly.view.viewholders.OnMapListItemClickListener;

import static dpyl.eddy.piedfly.Constants.ZOOM_LEVEL;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, OnMapListItemClickListener {

    private static final String FOCUS = "focus";

    private GoogleMap mMap;
    private Map<String, Marker> mMarkers;
    private Map<String, ValueEventListener> mListeners;

    private SharedPreferences.OnSharedPreferenceChangeListener mStateListener;
    private SharedPreferences mSharedPreferences;

    private CircleImageView mContactDetailsImage;
    private TextView mContactDetailsName;
    private TextView mContactDetailsLocation;
    private ImageView mContactDetailsCall;

    private RecyclerView mRecyclerView;
    private MapUserAdapter mMapUserAdapter;

    private String mPhoneNumber;
    private String mFocus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mContactDetailsImage = (CircleImageView) findViewById(R.id.map_contact_details_image);
        mContactDetailsLocation = (TextView) findViewById(R.id.map_contact_details_location);
        mContactDetailsName = (TextView) findViewById(R.id.map_contact_details_name);
        mContactDetailsCall = (ImageView) findViewById(R.id.map_contact_details_call);

        CircleImageView userImage = (CircleImageView) findViewById(R.id.map_user_image);
        StorageReference storageReference = mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getPhotoUrl() != null ? FileManager.getStorage().getReferenceFromUrl(mAuth.getCurrentUser().getPhotoUrl().toString()) : null;
        GlideApp.with(this).load(storageReference).fitCenter().centerInside().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(userImage);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMap != null && mMarkers != null) {
                    String uid = mAuth.getCurrentUser().getUid();
                    CameraPosition cameraAnimation = new CameraPosition.Builder().target(mMarkers.get(uid).getPosition()).zoom(ZOOM_LEVEL)
                            .tilt(mMap.getCameraPosition().tilt).bearing(mMap.getCameraPosition().bearing).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraAnimation));
                    setDetailsScreen(uid);
                    mFocus = uid;
                }
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
        // Display the data and set up listeners
        attachRecyclerViewAdapter();
        setCameraListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppPermissions.REQUEST_CALL_PHONE:
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_CALL_PHONE, grantResults)) {
                    if (mPhoneNumber != null) startPhoneCall(mPhoneNumber);
                }
                break;
        }
    }


    @Override
    public void OnListItemClick(int position, View view, String uid) {
        if (mMap != null && mMarkers != null) {
            CameraPosition cameraAnimation = new CameraPosition.Builder().target(mMarkers.get(uid).getPosition()).zoom(ZOOM_LEVEL)
                    .tilt(mMap.getCameraPosition().tilt).bearing(mMap.getCameraPosition().bearing).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraAnimation));
            setDetailsScreen(uid);
            mFocus = uid;
        }
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        super.onAuthStateChanged(firebaseAuth);
        // For the map to display any data or listeners to work, the user needs to be authenticated
        if (firebaseAuth.getCurrentUser() != null) {
            attachRecyclerViewAdapter();
            setCameraListener();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop listening to changes in the App state and free up memory, as the Activity is not visible
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mStateListener);
        mSharedPreferences = null;
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

    private void attachRecyclerViewAdapter() {
        if (mAuth != null && mAuth.getCurrentUser() != null && mMapUserAdapter == null && mMap != null) {
            if (mMarkers == null) mMarkers = new HashMap<>();
            Location location = Utility.getLastKnownLocation(this);
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(mAuth.getCurrentUser().getDisplayName());
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            Marker marker = mMap.addMarker(markerOptions);
            mMarkers.put(mAuth.getCurrentUser().getUid(), marker);

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

    private void setDetailsScreen(String uid) {
        DataManager.getDatabase().getReference("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                mContactDetailsName.setText(user.getName());
                StorageReference storageReference = FileManager.getStorage().getReferenceFromUrl(user.getPhotoUrl());
                GlideApp.with(mContactDetailsImage.getContext()).load(storageReference).fitCenter().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(mContactDetailsImage);
                final Location location = new Location("");
                location.setLatitude(user.getLastKnownLocation().getLatitude());
                location.setLongitude(user.getLastKnownLocation().getLongitude());
                String lastSeenAt = getString(R.string.content_last_seen_place);
                try {
                    Address address = Utility.getAddress(MapsActivity.this, location);
                    if (address != null) {
                        lastSeenAt += " " + address.getAddressLine(0);
                    } else {
                        lastSeenAt += " lat: " + location.getLatitude() + ", long: " + location.getLongitude();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    lastSeenAt += " lat: " + location.getLatitude() + ", long: " + location.getLongitude();
                }
                ;
                mContactDetailsLocation.setText(lastSeenAt);
                mContactDetailsLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri gmmIntentUri = Uri.parse("geo:" + location.getLatitude() + "," + location.getLongitude());
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        }
                    }
                });
                mContactDetailsCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startPhoneCall(user.getPhone());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: Error Handling
            }
        });
    }

    private void startPhoneCall(String phoneNumber) {
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
}
