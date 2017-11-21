package dpyl.eddy.piedfly.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.R;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    final private ListItemClickListener mOnClickListener;


    public interface ListItemClickListener {

        void onListItemClick(int position, View view);
    }

    public UserAdapter(Cursor mCursor, Context mContext, ListItemClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.flock_list_item, parent, false);
        return new UserHolder(itemView);
    }


    @Override
    public void onBindViewHolder(UserHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CircleImageView mContactImage;
        private TextView mContactName;
        private ImageView mContactCall, mContactDirections;
        public RelativeLayout mViewForeground, mViewBackground;

        public UserHolder(View itemView) {
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
