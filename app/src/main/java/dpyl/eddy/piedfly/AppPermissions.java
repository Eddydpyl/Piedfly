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
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

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
     * Id to identify an access & change wifi state permission request.
     */
    public static final int REQUEST_WIFI_STATE = 2;

    public static boolean requestReadPhoneStatePermission(Fragment fragment){
        if (ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            fragment.requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
            return false;
        } return true;
    }

    public static boolean requestReadPhoneStatePermission(Activity activity){
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
            return false;
        } return true;
    }

    public static boolean requestReadSMSPermission(Fragment fragment){
        if (ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            fragment.requestPermissions(new String[]{Manifest.permission.READ_SMS}, REQUEST_READ_SMS);
            return false;
        } return true;
    }

    public static boolean requestReadSMSPermission(Activity activity){
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_SMS}, REQUEST_READ_SMS);
            return false;
        } return true;
    }

    public static boolean requestLocationPermission(Fragment fragment){
        if (ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            fragment.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            return false;
        } return true;
    }

    public static boolean requestLocationPermission(Activity activity){
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
            return false;
        } return true;
    }

    public static boolean requestWifiStatePermission(Fragment fragment){
        if (ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            fragment.requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE}, REQUEST_WIFI_STATE);
            return false;
        } return true;
    }

    public static boolean requestWifiStatePermission(Activity activity){
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE}, REQUEST_WIFI_STATE);
            return false;
        } return true;
    }

    public static boolean requestWriteSettingsPermission(final Fragment fragment){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(fragment.getContext())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
            builder.setMessage(R.string.content_request_permission_message).setTitle(R.string.content_request_permission_title);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + fragment.getActivity().getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    fragment.startActivity(intent);
                    dialog.dismiss();
                }
            }); builder.create().show();
            return false;
        } return true;
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

}
