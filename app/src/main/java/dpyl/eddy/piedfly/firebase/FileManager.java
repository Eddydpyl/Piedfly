package dpyl.eddy.piedfly.firebase;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import dpyl.eddy.piedfly.firebase.model.User;

public class FileManager {

    private static FirebaseStorage mFirebaseStorage;

    static {
        if (mFirebaseStorage == null) mFirebaseStorage = FirebaseStorage.getInstance();
    }

    public static FirebaseStorage getStorage() {
        return mFirebaseStorage;
    }

    public static void uploadProfilePicture(@NonNull final FirebaseAuth auth, @NonNull Bitmap bitmap, final OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        final String uid = auth.getCurrentUser().getUid();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        String path = "users/" + uid + "_" + System.currentTimeMillis() + "/" + "profilePicture.jpeg";
        byte[] data = baos.toByteArray();
        OnSuccessListener<UploadTask.TaskSnapshot> onUploadListener = new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String url = taskSnapshot.getDownloadUrl().toString();
                User user = new User(uid);
                user.setPhotoUrl(url);
                DataManager.updateUser(user);
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(url)).build();
                Task<Void> task = auth.getCurrentUser().updateProfile(profileUpdates);
                if (onSuccessListener != null) task.addOnSuccessListener(onSuccessListener);
            }
        }; uploadFile(path, data, onUploadListener, onFailureListener);
    }

    private static void uploadFile(@NonNull String path, @NonNull byte[] data, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener, OnFailureListener onFailureListener) {
        UploadTask uploadTask = mFirebaseStorage.getReference(path).putBytes(data);
        if (onSuccessListener != null) uploadTask.addOnSuccessListener(onSuccessListener);
        if (onFailureListener != null) uploadTask.addOnFailureListener(onFailureListener);
    }

}
