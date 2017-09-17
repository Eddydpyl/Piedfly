package dpyl.eddy.piedfly.model;

import java.util.Map;

public class User {

    private String uid;
    private String displayName;
    private String photoUrl;
    private String countryISO;
    private SimpleLocation lastKnownLocation;
    private Map<String, Boolean> flocks;

    public User() {}

    public User(String uid, String displayName, String photoUrl, String countryISO, SimpleLocation lastKnownLocation, Map<String, Boolean> flocks) {
        this.uid = uid;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.countryISO = countryISO;
        this.lastKnownLocation = lastKnownLocation;
        this.flocks = flocks;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getCountryISO() {
        return countryISO;
    }

    public void setCountryISO(String countryISO) {
        this.countryISO = countryISO;
    }

    public SimpleLocation getLastKnownLocation() {
        return lastKnownLocation;
    }

    public void setLastKnownLocation(SimpleLocation lastKnownLocation) {
        this.lastKnownLocation = lastKnownLocation;
    }

    public Map<String, Boolean> getFlocks() {
        return flocks;
    }

    public void setFlocks(Map<String, Boolean> flocks) {
        this.flocks = flocks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (!uid.equals(user.uid)) return false;
        if (!displayName.equals(user.displayName)) return false;
        if (photoUrl != null ? !photoUrl.equals(user.photoUrl) : user.photoUrl != null)
            return false;
        if (countryISO != null ? !countryISO.equals(user.countryISO) : user.countryISO != null)
            return false;
        if (lastKnownLocation != null ? !lastKnownLocation.equals(user.lastKnownLocation) : user.lastKnownLocation != null)
            return false;
        return flocks != null ? flocks.equals(user.flocks) : user.flocks == null;

    }

    @Override
    public int hashCode() {
        int result = uid.hashCode();
        result = 31 * result + displayName.hashCode();
        result = 31 * result + (photoUrl != null ? photoUrl.hashCode() : 0);
        result = 31 * result + (countryISO != null ? countryISO.hashCode() : 0);
        result = 31 * result + (lastKnownLocation != null ? lastKnownLocation.hashCode() : 0);
        result = 31 * result + (flocks != null ? flocks.hashCode() : 0);
        return result;
    }
}
