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
import dpyl.eddy.piedfly.model.Poke;
import dpyl.eddy.piedfly.model.Request;

public class MessagingService extends FirebaseMessagingService {

    private static final String MESSAGE_TYPE_EMERGENCY_FLOCK = "EMERGENCY_FLOCK";
    private static final String MESSAGE_TYPE_EMERGENCY_NEARBY = "EMERGENCY_NEARBY";
    private static final String MESSAGE_TYPE_EVENT = "EVENT";
    private static final String MESSAGE_TYPE_REQUEST = "REQUEST";
    private static final String MESSAGE_TYPE_START_POKE = "START_POKE";
    private static final String MESSAGE_TYPE_END_POKE = "END_POKE";

    private static FirebaseDatabase mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = FirebaseDatabase.getInstance();
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
            final String emergency = data.get("emergency");
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
            // The user has either entered or left the perimeter of an Emergency
            final Boolean state = Boolean.valueOf(data.get("state"));
            mDatabase.getReference("emergencies").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Emergency emergency = dataSnapshot.getValue(Emergency.class);
                    if (state) {
                        // The user has entered the perimeter
                    } else {
                        // The user has left the perimeter
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // TODO: Error handling
                }
            });
        } else if (type.equals(MESSAGE_TYPE_REQUEST)) {
            // A Request has been sent to the user
            mDatabase.getReference("requests").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Request request = dataSnapshot.getValue(Request.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // TODO: Error handling
                }
            });
        } else if (type.equals(MESSAGE_TYPE_START_POKE)) {
            // A Poke has been sent to the user
            mDatabase.getReference("pokes").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Poke poke = dataSnapshot.getValue(Poke.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // TODO: Error handling
                }
            });
        } else if (type.equals(MESSAGE_TYPE_END_POKE)) {
            // A Poke involving the user has been checked
            mDatabase.getReference("pokes").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Poke poke = dataSnapshot.getValue(Poke.class);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // TODO: Error handling
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabase = null;
    }
}
