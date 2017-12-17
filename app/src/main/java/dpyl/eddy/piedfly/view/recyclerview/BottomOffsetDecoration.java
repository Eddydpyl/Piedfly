package dpyl.eddy.piedfly.view.recyclerview;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class BottomOffsetDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        super.getItemOffsets(outRect, view, parent, state);

        int dataSize = state.getItemCount();
        int position = parent.getChildAdapterPosition(view);

        //scrolling only implemented on the last element, if there are more than 2 elements in the list
        if (dataSize > 2 && position == dataSize - 1) {

            int itemHeight = view.getHeight() > 0 ? view.getHeight() : parent.findViewHolderForLayoutPosition(position - 1).itemView.getHeight();

            //You can always scroll the whole recycler view up but the last item
            outRect.set(0, 0, 0, parent.getHeight() - itemHeight);

        } else {
            outRect.set(0, 0, 0, 0);
        }
    }
}
