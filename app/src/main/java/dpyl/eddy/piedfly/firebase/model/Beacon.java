package dpyl.eddy.piedfly.firebase.model;

import android.support.annotation.Keep;

@Keep
public class Beacon {

    private String tinyID;
    private SimpleLocation location;
    private String message;

    public Beacon() {}

    public Beacon(String tinyID, SimpleLocation location, String message) {
        this.tinyID = tinyID;
        this.location = location;
        this.message = message;
    }

    public String getTinyID() {
        return tinyID;
    }

    public void setTinyID(String tinyID) {
        this.tinyID = tinyID;
    }

    public SimpleLocation getLocation() {
        return location;
    }

    public void setLocation(SimpleLocation location) {
        this.location = location;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Beacon)) return false;

        Beacon beacon = (Beacon) o;

        if (!tinyID.equals(beacon.tinyID)) return false;
        if (location != null ? !location.equals(beacon.location) : beacon.location != null)
            return false;
        return message != null ? message.equals(beacon.message) : beacon.message == null;

    }

    @Override
    public int hashCode() {
        int result = tinyID.hashCode();
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}
