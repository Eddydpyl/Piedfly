package dpyl.eddy.piedfly.model;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class Flock {

    private String key;
    private String name;
    private String owner;
    private String photoUrl;
    private Map<String, String> users; // values in the Map store a pair of booleans as bits in a String

    public Flock() {}

    public Flock(String key, String name, String owner, String photoUrl, Map<String, String> users) {
        this.key = key;
        this.name = name;
        this.owner = owner;
        this.photoUrl = photoUrl;
        this.users = users;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(Map<String, String> users) {
        this.users = users;
    }

    @Exclude
    public boolean getUserSilenced(String uid) {
        return Integer.valueOf(users.get(uid).substring(0, 1)) > 0;
    }

    @Exclude
    public void setUserSilenced(String uid, boolean silenced) {
        String val = (silenced ? "1" : "0") + users.get(uid).substring(1,2);
        users.put(uid, val);
    }

    @Exclude
    public boolean getUserTracking(String uid) {
        return Integer.valueOf(users.get(uid).substring(1, 2)) > 0;
    }

    @Exclude
    public void setUserTracking(String uid, boolean tracking) {
        String val = users.get(uid).substring(0,1) + (tracking ? "1" : "0");
        users.put(uid, val);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Flock)) return false;

        Flock flock = (Flock) o;

        if (key != null ? !key.equals(flock.key) : flock.key != null) return false;
        if (name != null ? !name.equals(flock.name) : flock.name != null) return false;
        if (owner != null ? !owner.equals(flock.owner) : flock.owner != null) return false;
        if (photoUrl != null ? !photoUrl.equals(flock.photoUrl) : flock.photoUrl != null)
            return false;
        return users != null ? users.equals(flock.users) : flock.users == null;

    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (photoUrl != null ? photoUrl.hashCode() : 0);
        result = 31 * result + (users != null ? users.hashCode() : 0);
        return result;
    }
}
