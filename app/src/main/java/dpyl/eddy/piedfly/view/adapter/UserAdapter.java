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

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.FileManager;
import dpyl.eddy.piedfly.GlideApp;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.model.User;
import dpyl.eddy.piedfly.model.room.Contact;

public class UserAdapter extends FirebaseRecyclerAdapter<User, RecyclerView.ViewHolder> {


    private static final int TYPE_FIREBASE = 0;
    private static final int TYPE_LOCAL = 1;

    private List<Contact> mLocalContacts;

    private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int position, View view);
    }

    public UserAdapter(FirebaseRecyclerOptions<User> options, ListItemClickListener mOnClickListener, List<Contact> mLocalContacts) {
        super(options);
        this.mOnClickListener = mOnClickListener;
        this.mLocalContacts = mLocalContacts;
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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.flock_list_item, parent, false);

        switch (viewType) {

            case TYPE_FIREBASE:
                return new UserHolder(itemView);
            case TYPE_LOCAL:
                return new ContactHolder(itemView);
        }

        return null;

    }

    @Override
    protected void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position, User model) {

        if (viewHolder instanceof UserHolder) {

            UserHolder userHolder = (UserHolder) viewHolder;
            userHolder.itemView.setTag(model.getUid());
            userHolder.mContactName.setText(model.getName());
            userHolder.mContactCall.setTag(model.getPhone());
            userHolder.mContactDirections.setTag(model.getLastKnownLocation());
            StorageReference storageReference = model.getPhotoUrl() != null ? FileManager.getStorage().getReferenceFromUrl(model.getPhotoUrl()) : null;
            GlideApp.with(userHolder.itemView.getContext()).load(storageReference).fitCenter().centerInside().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(userHolder.mContactImage);
        } else {

            ContactHolder contactHolder = (ContactHolder) viewHolder;
            contactHolder.itemView.setTag(model.getUid());
            contactHolder.mContactName.setText(model.getName());
            contactHolder.mContactCall.setTag(model.getPhone());
            contactHolder.mContactDirections.setTag(model.getLastKnownLocation());
            StorageReference storageReference = model.getPhotoUrl() != null ? FileManager.getStorage().getReferenceFromUrl(model.getPhotoUrl()) : null;
            GlideApp.with(contactHolder.itemView.getContext()).load(storageReference).fitCenter().centerInside().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(contactHolder.mContactImage);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (position > super.getItemCount()) {
            return TYPE_LOCAL;
        }

        return TYPE_FIREBASE;

    }

    @Override
    public int getItemCount() {
        int localSize = mLocalContacts == null ? 0 : mLocalContacts.size();
        return super.getItemCount() + localSize;
    }


    public List<Contact> getData() {
        return mLocalContacts;
    }

    public void setData(@NotNull List<Contact> mLocalContacts) {
        this.mLocalContacts = mLocalContacts;
        this.notifyItemRangeChanged(super.getItemCount(), super.getItemCount() + mLocalContacts.size());
    }

    // ViewHolders
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

    public class ContactHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CircleImageView mContactImage;
        private TextView mContactName;
        private ImageView mContactCall, mContactDirections;
        public RelativeLayout mViewForeground, mViewBackground;

        ContactHolder(View itemView) {
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
