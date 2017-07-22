package dpyl.eddy.piedfly;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import dpyl.eddy.piedfly.model.User;

public class Database {

    private Context context;
    private FirebaseDatabase mDatabase;

    private Database(Context context){
        this.context = context;
        mDatabase = FirebaseDatabase.getInstance();
    }

    public static Database init(Context context){
        return new Database(context);
    }

    /**
     * Updates the user's data but, if the user already exists in the database, the {@code User.flocks} is ignored.
     * @param user The user that is to be written, {@code User.flocks} may be ignored.
     */
    public void writeUser(final User user){
        final DatabaseReference userRef = mDatabase.getReference("users").child(user.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    User oldUser = dataSnapshot.getValue(User.class);
                    if(oldUser != null){
                        Map<String, Boolean> flocks = oldUser.getFlocks();
                        user.setFlocks(flocks);
                    }
                } userRef.setValue(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // TODO: Error handling
            }
        });
    }

}
