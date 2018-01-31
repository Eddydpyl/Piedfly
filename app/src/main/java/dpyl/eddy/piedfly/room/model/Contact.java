package dpyl.eddy.piedfly.room.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

/**
 * A phone contact that it's not yet in Firebase.
 */

@Entity(tableName = "contacts")
public class Contact {

    @PrimaryKey
    @NotNull
    private String phone;
    private String name;
    private String photo;


    public String getPhone() {
        return phone;
    }

    public void setPhone(@NotNull String phone) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contact contact = (Contact) o;

        return phone.equals(contact.phone);
    }

    @Override
    public int hashCode() {
        return phone.hashCode();
    }
}
