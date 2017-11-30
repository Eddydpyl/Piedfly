package dpyl.eddy.piedfly.view.adapter;

import android.content.Context;
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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.model.User;

public class UserAdapter extends FirebaseRecyclerAdapter<User, UserAdapter.UserHolder> {

    private Context context;
    private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int position, View view, String uid);
    }

    public UserAdapter(FirebaseRecyclerOptions<User> options, Context context, ListItemClickListener mOnClickListener) {
        super(options);
        this.context = context;
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
        holder.uid = model.getUid();
        holder.itemView.setTag(model.getUid());

        Picasso.with(holder.itemView.getContext()).load(model.getPhotoUrl()).fit().centerInside().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(holder.mContactImage);

        holder.mContactName.setText(model.getName());
        holder.mContactCall.setTag(model.getPhone());
        holder.mContactDirections.setTag(model.getLastKnownLocation());
    }

    public class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private String uid;

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
            mOnClickListener.onListItemClick(position, view, uid);
        }
    }
}
