package dpyl.eddy.piedfly.view;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import dpyl.eddy.piedfly.Database;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.model.Emergency;
import dpyl.eddy.piedfly.model.User;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Once the Activity is ready, swap the splash screen for the actual theme
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (mAuth != null && mAuth.getUid() != null) {
            User user = new User(mAuth.getUid());
            Map<String, String> flock = new HashMap<>();
            flock.put(mAuth.getUid(), "true");
            user.setFlock(flock);
            Database.updateUser(user);
            Emergency emergency = new Emergency();
            emergency.setUid(mAuth.getUid());
            emergency.setTrigger(mAuth.getUid());
            Database.startEmergency(emergency);
        }
    }
}
