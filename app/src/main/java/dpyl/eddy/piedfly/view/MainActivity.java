package dpyl.eddy.piedfly.view;

import android.os.Bundle;

import dpyl.eddy.piedfly.R;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
