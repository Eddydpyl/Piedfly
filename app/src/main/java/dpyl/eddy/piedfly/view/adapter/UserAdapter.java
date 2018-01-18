package dpyl.eddy.piedfly.view.adapter;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

import dpyl.eddy.piedfly.Constants;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.firebase.DataManager;
import dpyl.eddy.piedfly.firebase.FileManager;
import dpyl.eddy.piedfly.firebase.GlideApp;
import dpyl.eddy.piedfly.firebase.model.Poke;
import dpyl.eddy.piedfly.firebase.model.User;
import dpyl.eddy.piedfly.view.viewholder.OnListItemClickListener;
import dpyl.eddy.piedfly.view.viewholder.UserHolder;

public class UserAdapter extends FirebaseRecyclerAdapter<User, UserHolder> {

    private Map<String, String> mPokes;
    private OnListItemClickListener mOnListItemClickListener;
    private boolean mEmergency;

    public UserAdapter(FirebaseRecyclerOptions<User> options, OnListItemClickListener onListItemClickListener) {
        super(options);
        this.mOnListItemClickListener = onListItemClickListener;
        this.mEmergency = false;
    }

    public UserAdapter(FirebaseRecyclerOptions<User> options, OnListItemClickListener onListItemClickListener, boolean mEmergency) {
        super(options);
        this.mOnListItemClickListener = onListItemClickListener;
        this.mEmergency = mEmergency;
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        // Called when the necessary data has been retrieved (may be of use when using a loading spinner)
    }

    @Override
    public void onError(DatabaseError error) {
        super.onError(error);
        // TODO: Error handling
        // Couldn't retrieve the data, update UI accordingly
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.flock_list_item, parent, false);
        return new UserHolder(itemView, mOnListItemClickListener);
    }

    @Override
    protected void onBindViewHolder(final UserHolder holder, int position, User model) {
        holder.uid = model.getUid();
        holder.mContactName.setText(model.getName());
        holder.mContactCall.setTag(model.getPhone());
        final Context context = holder.itemView.getContext();
        StorageReference storageReference = model.getPhotoUrl() != null ? FileManager.getStorage().getReferenceFromUrl(model.getPhotoUrl()) : null;
        GlideApp.with(context).load(storageReference).fitCenter().centerInside().placeholder(R.drawable.default_contact).error(R.drawable.default_contact).dontAnimate().into(holder.mContactImage);
        if (mEmergency || model.getEmergency() != null) {
            holder.mContactPoke.setVisibility(View.INVISIBLE);
            holder.mContactCall.setVisibility(View.VISIBLE);
        } else {
            holder.mContactPoke.setVisibility(View.VISIBLE);
            holder.mContactCall.setVisibility(View.INVISIBLE);
            if (mPokes != null) setUpPokeIcon(context, model.getUid(), holder.mContactPoke);
        }
    }

    public void setEmergency(boolean emergency) {
        this.mEmergency = emergency;
        notifyDataSetChanged();
    }

    public Map<String, String> getPokes() {
        return mPokes;
    }

    public void setPokes(Map<String, String> pokes) {
        this.mPokes = pokes;
        notifyDataSetChanged();
    }

    private void setUpPokeIcon(final Context context, String uid, final ImageView imageView) {
        String key = mPokes.get(uid);
        if (!key.equals(Constants.PLACEHOLDER)) {
            imageView.setImageDrawable(context.getDrawable(R.drawable.ic_poke_full));
            DataManager.getDatabase().getReference("pokes").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Poke poke = dataSnapshot.getValue(Poke.class);
                    if (poke != null) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        String uid = sharedPreferences.getString(context.getString(R.string.pref_uid), "");
                        if (!poke.getTrigger().equals(uid)) {
                            animatePoke(imageView);
                            imageView.setTag(Constants.POKE_FLOCK);
                        } else imageView.setTag(Constants.POKE_USER);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // TODO: Error handling
                }
            });
        } else {
            imageView.setTag(Constants.POKE_NONE);
            imageView.setImageDrawable(context.getDrawable(R.drawable.ic_poke_empty));
        }
    }

    private void animatePoke(ImageView imageView) {
        PropertyValuesHolder scalex = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2f);
        PropertyValuesHolder scaley = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2f);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(imageView, scalex, scaley);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setDuration(1000);
        anim.start();
    }

}
