package dpyl.eddy.piedfly.view;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.AppPermissions;
import dpyl.eddy.piedfly.Constants;
import dpyl.eddy.piedfly.DataManager;
import dpyl.eddy.piedfly.FileManager;
import dpyl.eddy.piedfly.GlideApp;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.Utility;
import dpyl.eddy.piedfly.model.User;
import dpyl.eddy.piedfly.view.adapter.MapUserAdapter;

import static dpyl.eddy.piedfly.Constants.ZOOM_LEVEL;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, MapUserAdapter.ListItemClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Map<String, Marker> mMarkers;

    private SharedPreferences.OnSharedPreferenceChangeListener mStateListener;
    private SharedPreferences mSharedPreferences;

    // TODO: Add an ImageView for the current user in the upper RecyclerView

    private CircleImageView mContactDetailsImage;
    private TextView mContactDetailsName;
    private TextView mContactDetailsLocation;
    private ImageView mContactDetailsCall;

    private RecyclerView mRecyclerView;
    private MapUserAdapter mMapUserAdapter;

    private String mPhoneNumber;

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
        mContactDetailsCall =(ImageView) findViewById(R.id.map_contact_details_call);

        mRecyclerView = (RecyclerView) findViewById(R.id.map_recycler);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setAdapter(mMapUserAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
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
        }; mSharedPreferences.registerOnSharedPreferenceChangeListener(mStateListener);
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
        } return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AppPermissions.REQUEST_CALL_PHONE:
                if (AppPermissions.permissionGranted(requestCode, AppPermissions.REQUEST_CALL_PHONE, grantResults)) {
                    if (mPhoneNumber != null) startPhoneCall(mPhoneNumber);
                } break;
        }
    }

    @Override
    public void onListItemClick(int position, View view, final String uid) {
        if (mMap != null && mMarkers != null) {
            CameraPosition cameraAnimation = new CameraPosition.Builder().target(mMarkers.get(uid).getPosition()).zoom(ZOOM_LEVEL)
                    .tilt(mMap.getCameraPosition().tilt).bearing(mMap.getCameraPosition().bearing).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraAnimation));
            setDetailsScreen(uid);
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
        if (mMapUserAdapter != null) {
            mMapUserAdapter.stopListening();
            mMapUserAdapter = null;
        } mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mStateListener);
        mSharedPreferences = null;
        mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
        mMap = null;
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
            createGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            attachRecyclerViewAdapter();
            if (mMapUserAdapter != null) mMapUserAdapter.startListening();
        }
    }

    private synchronized void createGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Marker marker = mMarkers.get(mAuth.getCurrentUser().getUid());
        marker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(Constants.LOCATION_SLOWEST_INTERVAL);
        locationRequest.setFastestInterval(Constants.LOCATION_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO: Error Handling
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // TODO: Error Handling
    }

    private void attachRecyclerViewAdapter() {
        if (mAuth != null && mAuth.getCurrentUser() != null && mMapUserAdapter == null) {

            if (mMarkers == null) mMarkers = new HashMap<>();
            Location location = Utility.getLastKnownLocation(this);
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(mAuth.getCurrentUser().getDisplayName());
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            Marker marker = mMap.addMarker(markerOptions);
            mMarkers.put(mAuth.getCurrentUser().getUid(), marker);

            String focus = getIntent().getStringExtra(getString(R.string.intent_uid));
            Query keyQuery = DataManager.getDatabase().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("flock");
            DatabaseReference dataQuery = DataManager.getDatabase().getReference().child("users");
            FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setIndexedQuery(keyQuery, dataQuery, User.class).build();
            mMapUserAdapter = new MapUserAdapter(options, this, mMap, mMarkers, focus);
            mRecyclerView.setAdapter(mMapUserAdapter);
            setDetailsScreen(focus);

            if (mAuth.getCurrentUser().getUid().equals(focus)) {
                CameraPosition cameraAnimation = new CameraPosition.Builder().target(mMarkers.get(focus).getPosition()).zoom(ZOOM_LEVEL)
                        .tilt(mMap.getCameraPosition().tilt).bearing(mMap.getCameraPosition().bearing).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraAnimation));
            }
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
                // TODO: Beautify mContactDetailsLocation text
                mContactDetailsLocation.setText(user.getLastKnownLocation().toString());
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
        if (AppPermissions.requestCallPermission(this)) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }
}
