package dpyl.eddy.piedfly.firebase.messaging;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import dpyl.eddy.piedfly.AppState;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.firebase.model.Emergency;
import dpyl.eddy.piedfly.firebase.model.Event;
import dpyl.eddy.piedfly.firebase.model.EventType;
import dpyl.eddy.piedfly.firebase.model.Poke;
import dpyl.eddy.piedfly.firebase.model.Request;
import dpyl.eddy.piedfly.firebase.model.User;
import dpyl.eddy.piedfly.room.RoomManager;
import dpyl.eddy.piedfly.room.model.Message;
import dpyl.eddy.piedfly.room.model.MessageType;
import dpyl.eddy.piedfly.view.MainActivity;

public class MessagingService extends FirebaseMessagingService {

    private static final String MESSAGE_TYPE_EMERGENCY_NEARBY = "EMERGENCY_NEARBY";
    private static final String MESSAGE_TYPE_EVENT = "EVENT";
    private static final String MESSAGE_TYPE_REQUEST = "REQUEST";
    private static final String MESSAGE_TYPE_START_POKE = "START_POKE";
    private static final String MESSAGE_TYPE_END_POKE = "END_POKE";

    private FirebaseDatabase mDatabase;
    private int mNotificationID;
    private Map<String, Integer> emergencies;

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = FirebaseDatabase.getInstance();
        mNotificationID = 0;
        emergencies = new HashMap<>();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            sendNotification(data);
        }
    }

    private void sendNotification(final Map<String, String> data) {
        final String type = data.get("type");
        final String key = data.get("key");
        switch (type) {
            case MESSAGE_TYPE_EVENT:
                // An Event in an Emergency the user is taking part in has been created
                final String emergency = data.get("emergency");
                mDatabase.getReference("events").child(emergency).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final Event event = dataSnapshot.getValue(Event.class);
                        if (event != null && event.getEventType() != null) {
                            if (event.getEventType().equals(EventType.START)) {
                                // An Emergency has been triggered by someone in the user's flock
                                AppState.registerEmergencyFlock(getBaseContext(), emergency);
                                mDatabase.getReference("users").child(event.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);
                                        if (user != null) {
                                            pushNotification(user.getName() + " " + getString(R.string.content_push_emergency_flock_title), getString(R.string.content_push_emergency_flock_text), MainActivity.class);
                                            emergencies.put(emergency, mNotificationID);
                                            Message message = buildMessage(emergency, user.getName() + " " + getString(R.string.content_push_emergency_flock_title), MessageType.EMERGENCY_START, null);
                                            storeMessage(message, null);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        // TODO: Error Handling
                                    }
                                });
                            } else if (event.getEventType().equals(EventType.FINISH)) {
                                // An Emergency has been terminated by some user
                                AppState.unRegisterEmergencyFlock(getBaseContext(), emergency);
                                AppState.unRegisterEmergencyNearby(getBaseContext(), emergency);
                                mDatabase.getReference("users").child(event.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);
                                        if (user != null) {
                                            pushNotification(user.getName() + " " + getString(R.string.content_push_emergency_finish_title), getString(R.string.content_push_emergency_finish_text), MainActivity.class);
                                            removeNotification(emergencies.remove(emergency));
                                            Message message = buildMessage(emergency, user.getName() + " " + getString(R.string.content_push_emergency_finish_title), MessageType.EMERGENCY_FINISH, null);
                                            storeMessage(message, null);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        // TODO: Error Handling
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // TODO: Error handling
                    }
                });
                break;
            case MESSAGE_TYPE_EMERGENCY_NEARBY:
                // The user has either entered or left the perimeter of an Emergency
                final Boolean state = Boolean.valueOf(data.get("state"));
                mDatabase.getReference("emergencies").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Emergency emergency = dataSnapshot.getValue(Emergency.class);
                        if (emergency != null) {
                            if (state) {
                                // The user has entered the perimeter
                                AppState.registerEmergencyNearby(getBaseContext(), key);
                                pushNotification(getString(R.string.content_push_emergency_nearby_title), getString(R.string.content_push_emergency_nearby_text), MainActivity.class);
                                emergencies.put(emergency.getKey(), mNotificationID);
                                Message message = buildMessage(emergency.getKey(), getString(R.string.content_push_emergency_nearby_title), MessageType.EMERGENCY_NEARBY, null);
                                storeMessage(message, null);
                            } else {
                                // The user has left the perimeter
                                AppState.unRegisterEmergencyNearby(getBaseContext(), key);
                                removeNotification(emergencies.remove(emergency.getKey()));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // TODO: Error handling
                    }
                });
                break;
            case MESSAGE_TYPE_REQUEST:
                // A Request has been sent to the user
                mDatabase.getReference("requests").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Request request = dataSnapshot.getValue(Request.class);
                        if (request != null) {
                            mDatabase.getReference("users").child(request.getTrigger()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User user = dataSnapshot.getValue(User.class);
                                    if (user != null) {
                                        pushNotification(user.getName() + " " + getString(R.string.content_push_request_title), getString(R.string.content_push_request_text), MainActivity.class);
                                        Message message = buildMessage(user.getUid(), user.getName() + " " + getString(R.string.content_push_request_title), MessageType.REQUEST_FLOCK, null);
                                        storeMessage(message, null);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // TODO: Error handling
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // TODO: Error handling
                    }
                });
                break;
            case MESSAGE_TYPE_START_POKE:
                // A Poke has been sent to the user
                mDatabase.getReference("pokes").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Poke poke = dataSnapshot.getValue(Poke.class);
                        if (poke != null) {
                            mDatabase.getReference("users").child(poke.getTrigger()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User user = dataSnapshot.getValue(User.class);
                                    if (user != null) {
                                        pushNotification(user.getName() + " " + getString(R.string.content_push_poke_start_title), getString(R.string.content_push_poke_start_text), MainActivity.class);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // TODO: Error handling
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // TODO: Error handling
                    }
                });
                break;
            case MESSAGE_TYPE_END_POKE:
                // A Poke involving the user has been checked
                mDatabase.getReference("pokes").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Poke poke = dataSnapshot.getValue(Poke.class);
                        if (poke != null) {
                            mDatabase.getReference("users").child(poke.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User user = dataSnapshot.getValue(User.class);
                                    if (user != null) {
                                        pushNotification(user.getName() + " " + getString(R.string.content_push_poke_end_title), getString(R.string.content_push_poke_end_text), MainActivity.class);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // TODO: Error handling
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // TODO: Error handling
                    }
                });
                break;
        }
    }

    private void pushNotification(String title, String text, Class<?> target) {
        final long[] pattern = {250, 500, 250, 500};
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setVibrate(pattern)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_HIGH);
        Intent intent = new Intent(this, target);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        if (!target.isAssignableFrom(MainActivity.class))
            stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.notify(mNotificationID++, mBuilder.build());
    }

    private void removeNotification(Integer id) {
        if (id != null) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) notificationManager.cancel(id);
        }
    }

    private Message buildMessage(String key, String text, MessageType messageType, String aux) {
        Message message = new Message();
        message.setFirebaseKey(key);
        message.setText(text);
        message.setType(messageType.name());
        if (aux != null) message.setAux(aux);
        message.setRead(false);
        return message;
    }

    private void storeMessage(Message message, RoomManager.LocalDataCallback callback) {
        RoomManager roomManager = new RoomManager(this);
        roomManager.saveMessage(message, callback);
    }
}
