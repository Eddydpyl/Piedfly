package dpyl.eddy.piedfly.model;

public class Emergency {

    private String key;
    private String uid;
    private SimpleLocation location;

    public Emergency() {}

    public Emergency(String key, String uid, SimpleLocation location) {
        this.key = key;
        this.uid = uid;
        this.location = location;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public SimpleLocation getLocation() {
        return location;
    }

    public void setLocation(SimpleLocation location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Emergency)) return false;

        Emergency emergency = (Emergency) o;

        if (!key.equals(emergency.key)) return false;
        if (!uid.equals(emergency.uid)) return false;
        return location.equals(emergency.location);

    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + uid.hashCode();
        result = 31 * result + location.hashCode();
        return result;
    }
}
