package dpyl.eddy.piedfly.view.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.R;

/**
 * A holder for the map horizontal recycler view items.
 */
public class MapHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public String uid;
    public CircleImageView MapContactImage;

    private OnMapListItemClickListener mOnMapListItemClickListener;

    public MapHolder(View itemView, OnMapListItemClickListener onMapListItemClickListener) {
        super(itemView);
        MapContactImage = (CircleImageView) itemView.findViewById(R.id.map_contact_image);
        mOnMapListItemClickListener = onMapListItemClickListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int position = getAdapterPosition();
        mOnMapListItemClickListener.OnListItemClick(position, view, uid);
    }
}