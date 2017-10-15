package dpyl.eddy.piedfly.messaging;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import dpyl.eddy.piedfly.model.Emergency;
import dpyl.eddy.piedfly.model.Event;

public class MessagingService extends FirebaseMessagingService {

    private static final String MESSAGE_TYPE_EMERGENCY_FLOCK = "EMERGENCY_FLOCK";
    private static final String MESSAGE_TYPE_EMERGENCY_NEARBY = "EMERGENCY_NEARBY";
    private static final String MESSAGE_TYPE_EVENT = "EVENT";

    private static FirebaseDatabase mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mDatabase == null) mDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            sendNotification(data);
        }
    }

    private void sendNotification(Map<String, String> data){
        String type = data.get("type");
        String key = data.get("key");
        // TODO: Create a push notification if required
        if (type.equals(MESSAGE_TYPE_EVENT)) {
            // An Event in an Emergency the user is taking part in has been created
            String emergency = data.get("emergency");
            mDatabase.getReference("events").child(emergency).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Event event = dataSnapshot.getValue(Event.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // TODO: Error handling
                }
            });
        } else if (type.equals(MESSAGE_TYPE_EMERGENCY_FLOCK)) {
            // An Emergency has been triggered by someone in the user's flock
            mDatabase.getReference("emergencies").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Emergency emergency = dataSnapshot.getValue(Emergency.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // TODO: Error handling
                }
            });
        } else if (type.equals(MESSAGE_TYPE_EMERGENCY_NEARBY)) {
            // An Emergency has been triggered by someone nearby the user
            mDatabase.getReference("emergencies").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Emergency emergency = dataSnapshot.getValue(Emergency.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // TODO: Error handling
                }
            });
        }
    }
}
