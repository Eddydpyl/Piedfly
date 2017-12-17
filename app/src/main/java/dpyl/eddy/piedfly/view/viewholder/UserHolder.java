package dpyl.eddy.piedfly.view.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.R;

public class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public String uid;

    public CircleImageView mContactImage;
    public TextView mContactName;
    public ImageView mContactCall, mContactDirections;
    public RelativeLayout mViewForeground, mViewBackground;

    public OnListItemClickListener mOnListItemClickListener;

    public UserHolder(View itemView, OnListItemClickListener onListItemClickListener) {
        super(itemView);
        mOnListItemClickListener = onListItemClickListener;
        mContactImage = (CircleImageView) itemView.findViewById(R.id.contact_image);
        mContactName = (TextView) itemView.findViewById(R.id.contact_name);
        mContactCall = (ImageView) itemView.findViewById(R.id.contact_call);
        mContactDirections = (ImageView) itemView.findViewById(R.id.contact_directions);
        mViewBackground = (RelativeLayout) itemView.findViewById(R.id.item_background);
        mViewForeground = (RelativeLayout) itemView.findViewById(R.id.item_foreground);
        mContactImage.setOnClickListener(this);
        mContactCall.setOnClickListener(this);
        mContactDirections.setOnClickListener(this);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int position = getAdapterPosition();
        mOnListItemClickListener.OnListItemClick(position, view, uid);
    }
}