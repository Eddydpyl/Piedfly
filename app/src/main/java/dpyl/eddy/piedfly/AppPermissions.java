package dpyl.eddy.piedfly;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
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

    /**
     * Id to identify a location permission request.
     */
    public static final int REQUEST_LOCATION = 2;

    /**
     * Id to identify a change wifi state permission request.
     */
    public static final int REQUEST_CHANGE_WIFI_STATE = 2;

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

    public static void requestLocationPermission(Fragment fragment){
        fragment.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
    }

    public static void requestLocationPermission(Activity activity){
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
    }

    public static void requestChangeWifiStatePermission(Fragment fragment){
        fragment.requestPermissions(new String[]{Manifest.permission.CHANGE_WIFI_STATE}, REQUEST_CHANGE_WIFI_STATE);
    }

    public static void requestChangeWifiStatePermission(Activity activity){
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CHANGE_WIFI_STATE}, REQUEST_CHANGE_WIFI_STATE);
    }

    public static void requestWriteSettingsPermission(final Activity activity){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(activity)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.content_request_permission_message).setTitle(R.string.content_request_permission_title);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                    dialog.dismiss();
                }
            }); builder.create().show();
        }
    }

}
