package dpyl.eddy.piedfly.view.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import dpyl.eddy.piedfly.R;

public class MessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView mMessageText;
    public OnListItemClickListener mOnListItemClickListener;

    public MessageHolder(View itemView, OnListItemClickListener onListItemClickListener) {
        super(itemView);
        mOnListItemClickListener = onListItemClickListener;
        mMessageText = (TextView) itemView.findViewById(R.id.message_text);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int position = getAdapterPosition();
        mOnListItemClickListener.OnListItemClick(position, view, null);
    }
}