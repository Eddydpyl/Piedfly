package dpyl.eddy.piedfly.view.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.R;

public class MapHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public String uid;

    public CircleImageView MapContactImage;

    private OnListItemClickListener mOnListItemClickListener;

    public MapHolder(View itemView, OnListItemClickListener onMapListItemClickListener) {
        super(itemView);
        mOnListItemClickListener = onMapListItemClickListener;
        MapContactImage = (CircleImageView) itemView.findViewById(R.id.map_contact_image);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int position = getAdapterPosition();
        mOnListItemClickListener.OnListItemClick(position, view, uid);
    }
}