package dpyl.eddy.piedfly.room.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

/**
 * A message object to be used for the storage of app notifications.
 */

@Entity(tableName = "messages")
public class Message {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NotNull
    private String type;
    @NotNull
    private String firebase_key;
    private String text;
    private String aux;

    private long timestamp;
    private boolean read;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean getRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFirebase_key() {
        return firebase_key;
    }

    public void setFirebase_key(@NotNull String firebase_key) {
        this.firebase_key = firebase_key;
    }

    public String getType() {
        return type;
    }

    public void setType(@NotNull String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAux() {
        return aux;
    }

    public void setAux(String aux) {
        this.aux = aux;
    }
}
