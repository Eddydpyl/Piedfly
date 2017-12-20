package dpyl.eddy.piedfly.view.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import dpyl.eddy.piedfly.R;

public class MessageHolder extends ItemTouchHolder implements View.OnClickListener {

    public TextView mMessageText;
    public ImageView mMessageIcon;
    public OnListItemClickListener mOnListItemClickListener;

    public MessageHolder(View itemView, OnListItemClickListener onListItemClickListener) {
        super(itemView);
        mOnListItemClickListener = onListItemClickListener;
        mMessageText = (TextView) itemView.findViewById(R.id.message_text);
        mMessageIcon = (ImageView) itemView.findViewById(R.id.message_icon);
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int position = getAdapterPosition();
        mOnListItemClickListener.OnListItemClick(position, view, null);
    }
}