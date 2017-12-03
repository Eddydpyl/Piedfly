package dpyl.eddy.piedfly.model.room;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * A phone contact that it's not yet in firebase.
 */


@Entity(tableName = "contacts")
public class Contact {

    @PrimaryKey
    @NonNull
    private String phone;
    private String name;
    private String photo;


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
