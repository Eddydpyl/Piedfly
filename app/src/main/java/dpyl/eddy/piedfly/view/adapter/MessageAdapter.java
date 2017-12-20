package dpyl.eddy.piedfly.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.room.model.Message;
import dpyl.eddy.piedfly.view.viewholder.MessageHolder;
import dpyl.eddy.piedfly.view.viewholder.OnListItemClickListener;

public class MessageAdapter extends RecyclerView.Adapter<MessageHolder>{

    private List<Message> mMessages;
    private OnListItemClickListener mOnListItemClickListener;

    public MessageAdapter(List<Message> mMessages, OnListItemClickListener mOnListItemClickListener) {
        this.mMessages = mMessages;
        this.mOnListItemClickListener = mOnListItemClickListener;
    }

    @Override
    public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_item, parent, false);
        return new MessageHolder(itemView, mOnListItemClickListener);
    }

    @Override
    public void onBindViewHolder(MessageHolder holder, int position) {
        Message model = mMessages.get(position);
        holder.mMessageText.setText(model.getText());
        // TODO: Set icon according to MessageType
    }

    @Override
    public int getItemCount() {
        return mMessages == null ? 0 : mMessages.size();
    }

    public List<Message> getMessages() {
        return mMessages;
    }

    public void setMessages(List<Message> messages) {
        this.mMessages = messages;
        this.notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        mMessages.add(message);
        this.notifyItemInserted(mMessages.size() - 1);
    }
}
