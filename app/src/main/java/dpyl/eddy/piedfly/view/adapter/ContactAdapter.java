package dpyl.eddy.piedfly.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dpyl.eddy.piedfly.GlideApp;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.model.room.Contact;

/**
 * An adapter for local contacts.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactHolder> {


    private List<Contact> mContacts;
    private ListItemClickListener mOnClickListener;


    public interface ListItemClickListener {
        void onListItemClick(int position, View view);
    }


    public ContactAdapter(List<Contact> mContacts, ListItemClickListener mOnClickListener) {
        this.mContacts = mContacts;
        this.mOnClickListener = mOnClickListener;
    }


    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item, parent, false);
        return new ContactHolder(itemView);
    }


    @Override
    public void onBindViewHolder(ContactHolder holder, int position) {
        Contact model = mContacts.get(position);
        holder.mContactName.setText(model.getName());
        holder.mContactCall.setTag(model.getPhone());
        GlideApp.with(holder.itemView.getContext()).load(model.getPhoto()).fitCenter().centerInside().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).into(holder.mContactImage);
    }


    @Override
    public int getItemCount() {
        return mContacts == null ? 0 : mContacts.size();
    }

    public List<Contact> getContacts() {
        return mContacts;
    }

    public void setContacts(List<Contact> mContacts) {
        this.mContacts = mContacts;
        this.notifyDataSetChanged();
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
