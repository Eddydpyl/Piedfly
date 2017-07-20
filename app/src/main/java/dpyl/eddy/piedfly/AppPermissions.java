package dpyl.eddy.piedfly;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

public class AppPermissions {

    /**
     * Id to identify a read phone state permission request.
     */
    public static final int REQUEST_READ_PHONE_STATE = 0;

    /**
     * Id to identify a read sms permission request.
     */
    public static final int REQUEST_READ_SMS = 1;

    public static void requestReadPhoneStatePermission(Fragment fragment){
        fragment.requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
    }

    public static void requestReadPhoneStatePermission(Activity activity){
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
    }

    public static void requestReadSMSPermission(Fragment fragment){
        fragment.requestPermissions(new String[]{Manifest.permission.READ_SMS}, REQUEST_READ_SMS);
    }

    public static void requestReadSMSPermission(Activity activity){
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_SMS}, REQUEST_READ_SMS);
    }

}
