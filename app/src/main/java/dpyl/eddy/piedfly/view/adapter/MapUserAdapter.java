package dpyl.eddy.piedfly.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.R;

public class MapUserAdapter extends RecyclerView.Adapter<MapUserAdapter.MyViewHolder> {

    final private ListItemClickListener mOnClickListener;


    public interface ListItemClickListener {

        void onListItemClick(int position, View view);
    }

    public MapUserAdapter(Cursor mCursor, Context mContext, ListItemClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.map_list_item, parent, false);
        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

    }


    @Override
    public int getItemCount() {
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CircleImageView mMapContactImage;

        public MyViewHolder(View itemView) {
            super(itemView);
            mMapContactImage = (CircleImageView) itemView.findViewById(R.id.map_contact_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mOnClickListener.onListItemClick(position, view);
        }
    }
}


