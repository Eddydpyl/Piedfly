package dpyl.eddy.piedfly.view;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import dpyl.eddy.piedfly.Database;
import dpyl.eddy.piedfly.R;
import dpyl.eddy.piedfly.model.User;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: Used for live testing the database, delete on production
        if(auth != null && auth.getCurrentUser() != null){
            Map<String, Boolean> flocks = new HashMap<>();
            flocks.put("123", true);
            flocks.put("456", true);
            User user = new User(auth.getCurrentUser().getUid(), "pepe", "", "ES", "123", flocks);
            Database.init(this).writeUser(user);
        }
    }
}
