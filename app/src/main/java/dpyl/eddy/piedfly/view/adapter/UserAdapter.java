package dpyl.eddy.piedfly.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.StorageReference;

import dpyl.eddy.piedfly.firebase.FileManager;
import dpyl.eddy.piedfly.firebase.GlideApp;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.firebase.model.User;
import dpyl.eddy.piedfly.view.viewholder.OnListItemClickListener;
import dpyl.eddy.piedfly.view.viewholder.UserHolder;

public class UserAdapter extends FirebaseRecyclerAdapter<User, UserHolder> {

    private OnListItemClickListener mOnListItemClickListener;

    public UserAdapter(FirebaseRecyclerOptions<User> options, OnListItemClickListener onListItemClickListener) {
        super(options);
        this.mOnListItemClickListener = onListItemClickListener;
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
        return new UserHolder(itemView, mOnListItemClickListener);
    }

    @Override
    protected void onBindViewHolder(UserHolder holder, int position, User model) {
        holder.uid = model.getUid();
        holder.mContactName.setText(model.getName());
        StorageReference storageReference = model.getPhotoUrl() != null ? FileManager.getStorage().getReferenceFromUrl(model.getPhotoUrl()) : null;
        GlideApp.with(holder.itemView.getContext()).load(storageReference).fitCenter().centerInside().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(holder.mContactImage);
    }

}
