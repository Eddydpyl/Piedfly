package dpyl.eddy.piedfly.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

import dpyl.eddy.piedfly.firebase.FileManager;
import dpyl.eddy.piedfly.firebase.GlideApp;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.firebase.model.SimpleLocation;
import dpyl.eddy.piedfly.firebase.model.User;
import dpyl.eddy.piedfly.view.viewholder.MapHolder;
import dpyl.eddy.piedfly.view.viewholder.OnListItemClickListener;

import static dpyl.eddy.piedfly.Constants.ZOOM_LEVEL;

public class MapUserAdapter extends FirebaseRecyclerAdapter<User, MapHolder> {

    final private Map<String, Marker> mMarkers;
    final private GoogleMap mMap;
    final private String mFocus;

    private boolean focused;
    private OnListItemClickListener mOnListItemClickListener;

    public MapUserAdapter(FirebaseRecyclerOptions<User> options, GoogleMap map, Map<String, Marker> markers, OnListItemClickListener onMapListItemClickListener, String focus) {
        super(options);
        this.mMap = map;
        this.mMarkers = markers;
        this.mFocus = focus;
        this.mOnListItemClickListener = onMapListItemClickListener;
        focused = false;
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        // Called when the necessary data has been retrieved (may be of use when using a loading spinner)
    }

    @Override
    public void onError(DatabaseError error) {
        super.onError(error);
        // TODO: Error handling
        // Couldn't retrieve the data, update UI accordingly
    }

    @Override
    public MapHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.map_list_item, parent, false);
        return new MapHolder(itemView, mOnListItemClickListener);
    }

    @Override
    protected void onBindViewHolder(MapHolder holder, int position, User model) {
        StorageReference storageReference = model.getPhotoUrl() != null ? FileManager.getStorage().getReferenceFromUrl(model.getPhotoUrl()) : null;
        GlideApp.with(holder.itemView.getContext()).load(storageReference).fitCenter().centerInside().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(holder.MapContactImage);
        holder.uid = model.getUid();

        SimpleLocation simpleLocation = model.getLastKnownLocation();
        if (simpleLocation != null && !mMarkers.containsKey(model.getUid())) {
            LatLng latLng = new LatLng(simpleLocation.getLatitude(), simpleLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title(model.getName());
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            Marker marker = mMap.addMarker(markerOptions);
            mMarkers.put(model.getUid(), marker);
        }

        if (model.getUid().equals(mFocus) && !focused) {
            CameraPosition cameraAnimation = new CameraPosition.Builder().target(mMarkers.get(mFocus).getPosition()).zoom(ZOOM_LEVEL)
                    .tilt(mMap.getCameraPosition().tilt).bearing(mMap.getCameraPosition().bearing).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraAnimation));
            focused = true;
        }
    }

}


