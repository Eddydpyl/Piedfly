package dpyl.eddy.piedfly.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import dpyl.eddy.piedfly.firebase.GlideApp;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.room.model.Contact;
import dpyl.eddy.piedfly.view.viewholder.OnListItemClickListener;
import dpyl.eddy.piedfly.view.viewholder.UserHolder;

public class ContactAdapter extends RecyclerView.Adapter<UserHolder> {

    private List<Contact> mContacts;
    private OnListItemClickListener mOnListItemClickListener;

    public ContactAdapter(List<Contact> mContacts, OnListItemClickListener listItemClickListener) {
        this.mContacts = mContacts;
        this.mOnListItemClickListener = listItemClickListener;
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item, parent, false);
        return new UserHolder(itemView, mOnListItemClickListener);
    }

    @Override
    public void onBindViewHolder(UserHolder holder, int position) {
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

}
