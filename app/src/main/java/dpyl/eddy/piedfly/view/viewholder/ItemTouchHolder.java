package dpyl.eddy.piedfly.view.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import dpyl.eddy.piedfly.R;

public class ItemTouchHolder extends RecyclerView.ViewHolder {

    public View mViewForeground, mViewBackground;

    public ItemTouchHolder(View itemView) {
        super(itemView);
        mViewBackground = itemView.findViewById(R.id.item_background);
        mViewForeground = itemView.findViewById(R.id.item_foreground);
    }
}
