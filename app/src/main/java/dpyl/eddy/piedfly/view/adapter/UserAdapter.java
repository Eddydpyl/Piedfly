package dpyl.eddy.piedfly.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.FileManager;
import dpyl.eddy.piedfly.GlideApp;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.model.User;

public class UserAdapter extends FirebaseRecyclerAdapter<User, UserAdapter.UserHolder> {

    private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int position, View view);
    }

    public UserAdapter(FirebaseRecyclerOptions<User> options, ListItemClickListener mOnClickListener) {
        super(options);
        this.mOnClickListener = mOnClickListener;
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
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.flock_list_item, parent, false);
        return new UserHolder(itemView);
    }

    @Override
    protected void onBindViewHolder(UserHolder holder, int position, User model) {
        holder.itemView.setTag(model.getUid());
        holder.mContactName.setText(model.getName());
        holder.mContactCall.setTag(model.getPhone());
        holder.mContactDirections.setTag(model.getLastKnownLocation());
        StorageReference storageReference = model.getPhotoUrl() != null ? FileManager.getStorage().getReferenceFromUrl(model.getPhotoUrl()) : null;
        GlideApp.with(holder.itemView.getContext()).load(storageReference).fitCenter().centerInside().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(holder.mContactImage);
    }

    public class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CircleImageView mContactImage;
        private TextView mContactName;
        private ImageView mContactCall, mContactDirections;
        public RelativeLayout mViewForeground, mViewBackground;

        UserHolder(View itemView) {
            super(itemView);
            mContactImage = (CircleImageView) itemView.findViewById(R.id.contact_image);
            mContactName = (TextView) itemView.findViewById(R.id.contact_name);
            mContactCall = (ImageView) itemView.findViewById(R.id.contact_call);
            mContactDirections = (ImageView) itemView.findViewById(R.id.contact_directions);
            mViewBackground = (RelativeLayout) itemView.findViewById(R.id.item_background);
            mViewForeground = (RelativeLayout) itemView.findViewById(R.id.item_foreground);

            //Setting listeners
            mContactImage.setOnClickListener(this);
            mContactCall.setOnClickListener(this);
            mContactDirections.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mOnClickListener.onListItemClick(position, view);
        }
    }
}
