package dpyl.eddy.piedfly;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * This class contains methods that check if a certain permission has been granted and reports it.
 * If the permission has not been granted, the user will be asked to grant it.
 */

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
     * Id to identify a read contacts permission request.
     */
    public static final int REQUEST_READ_CONTACTS = 3;

    /**
     * Id to identify an external storage permission request.
     */
    public static final int REQUEST_EXTERNAL_STORAGE = 4;

    /**
     * Id to identify a call permission request.
     */
    public static final int REQUEST_CALL_PHONE = 5;

    public static boolean requestReadPhoneStatePermission(Activity activity){
        return requestPermission(activity, REQUEST_READ_PHONE_STATE, Manifest.permission.READ_PHONE_STATE);
    }

    public static boolean requestReadSMSPermission(Activity activity){
        return requestPermission(activity, REQUEST_READ_SMS, Manifest.permission.READ_SMS);
    }

    public static boolean requestLocationPermission(Activity activity){
        return requestPermission(activity, REQUEST_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public static boolean requestReadContactsPermission(Activity activity){
        return requestPermission(activity, REQUEST_READ_CONTACTS, Manifest.permission.READ_CONTACTS);
    }

    public static boolean requestExternalStoragePermission(Activity activity){
        return requestPermission(activity, REQUEST_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean requestCallPermission(Activity activity){
        return requestPermission(activity, REQUEST_CALL_PHONE, Manifest.permission.CALL_PHONE);
    }

    public static boolean requestWriteSettingsPermission(final Activity activity){
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
            return false;
        } return true;
    }

    public static boolean requestPermission(Activity activity, int requestCode, String... permissions) {
        boolean granted = true;
        ArrayList<String> permissionsNeeded = new ArrayList<>();
        for (String s : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(activity, s);
            boolean hasPermission = (permissionCheck == PackageManager.PERMISSION_GRANTED);
            granted &= hasPermission;
            if (!hasPermission) permissionsNeeded.add(s);
        } if (granted) return true;
        else {
            ActivityCompat.requestPermissions(activity, permissionsNeeded.toArray(new String[permissionsNeeded.size()]), requestCode);
            return false;
        }
    }

    public static boolean permissionGranted(int requestCode, int permissionCode, int[] grantResults) {
        return requestCode == permissionCode && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

}
