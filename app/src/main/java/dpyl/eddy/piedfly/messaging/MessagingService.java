package dpyl.eddy.piedfly.messaging;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import dpyl.eddy.piedfly.Constants;

public class MessagingService extends FirebaseMessagingService {

    private static final String MESSAGE_TYPE_EMERGENCY_FLOCK = "EMERGENCY_FLOCK";
    private static final String MESSAGE_TYPE_EMERGENCY_NEARBY = "EMERGENCY_NEARBY";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            sendNotification(data.get("type"), data.get("key"));
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
        }
    }

    private void sendNotification(String type, String key){
        Log.i("notificationFIRE", type);
        // TODO: Create a push notification if required
        if (type.equals(MESSAGE_TYPE_EMERGENCY_FLOCK)) {
            // An Emergency has been triggered by someone in the user's flock
            // TODO: Retrieve the Emergency
        } else if (type.equals(MESSAGE_TYPE_EMERGENCY_NEARBY)) {
            // An Emergency has been triggered by someone nearby the user
            // TODO: Retrieve the Emergency
        }
    }
}
