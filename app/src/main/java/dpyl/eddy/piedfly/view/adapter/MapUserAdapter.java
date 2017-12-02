package dpyl.eddy.piedfly.view.adapter;

import android.support.v7.widget.RecyclerView;
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

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.FileManager;
import dpyl.eddy.piedfly.GlideApp;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.model.SimpleLocation;
import dpyl.eddy.piedfly.model.User;

import static dpyl.eddy.piedfly.Constants.ZOOM_LEVEL;

public class MapUserAdapter extends FirebaseRecyclerAdapter<User, MapUserAdapter.MapHolder> {

    final private ListItemClickListener mOnClickListener;
    final private Map<String, Marker> mMarkers;
    final private GoogleMap mMap;
    final private String mFocus;

    private boolean focused;

    public interface ListItemClickListener {
        void onListItemClick(int position, View view, String uid);
    }

    public MapUserAdapter(FirebaseRecyclerOptions<User> options, ListItemClickListener mOnClickListener, GoogleMap map, Map<String, Marker> markers, String focus) {
        super(options);
        this.mOnClickListener = mOnClickListener;
        this.mMap = map;
        this.mMarkers = markers;
        this.mFocus = focus;
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
        return new MapHolder(itemView);
    }

    @Override
    protected void onBindViewHolder(MapHolder holder, int position, User model) {
        StorageReference storageReference = model.getPhotoUrl() != null ? FileManager.getStorage().getReferenceFromUrl(model.getPhotoUrl()) : null;
        GlideApp.with(holder.itemView.getContext()).load(storageReference).fitCenter().centerInside().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(holder.mMapContactImage);
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

    public class MapHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String uid;

        private CircleImageView mMapContactImage;

        public MapHolder(View itemView) {
            super(itemView);
            mMapContactImage = (CircleImageView) itemView.findViewById(R.id.map_contact_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mOnClickListener.onListItemClick(position, view, uid);
        }
    }
}


