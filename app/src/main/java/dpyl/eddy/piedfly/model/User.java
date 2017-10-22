package dpyl.eddy.piedfly.model;

import android.support.annotation.Keep;

import com.google.firebase.database.Exclude;

import java.util.Map;

@Keep
public class User {

    private String uid;
    private String tinyID;
    private String token;
    private String name;
    private String surname;
    private Integer age;
    private String phone;
    private String email;
    private String photoUrl;
    private String countryISO;
    private Map<String, String> flock; // The keys hold the uid of those in the flock, while the values hold the key of the active poke (or true if there's none)
    private SimpleLocation lastKnownLocation;
    private String emergency; // The key of the currently active Emergency for this user (or null if there's none)

    public User() {}

    public User(String uid) {
        this.uid = uid;
    }

    public User(String uid, String tinyID, String token, String name, String surname, Integer age, String phone, String email, String photoUrl, String countryISO, Map<String, String> flock, SimpleLocation lastKnownLocation, String emergency) {
        this.uid = uid;
        this.tinyID = tinyID;
        this.token = token;
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.phone = phone;
        this.email = email;
        this.photoUrl = photoUrl;
        this.countryISO = countryISO;
        this.flock = flock;
        this.lastKnownLocation = lastKnownLocation;
        this.emergency = emergency;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTinyID() {
        return tinyID;
    }

    public void setTinyID(String tinyID) {
        this.tinyID = tinyID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Map<String, String> getFlock() {
        return flock;
    }

    public void setFlock(Map<String, String> flock) {
        this.flock = flock;
    }

    public SimpleLocation getLastKnownLocation() {
        return lastKnownLocation;
    }

    public void setLastKnownLocation(SimpleLocation lastKnownLocation) {
        this.lastKnownLocation = lastKnownLocation;
    }

    public String getEmergency() {
        return emergency;
    }

    public void setEmergency(String emergency) {
        this.emergency = emergency;
    }

    @Exclude
    public String getPoke (String uid){
        return flock.get(uid);
    }

    @Exclude
    public void setPoke (String uid, String key) {
        flock.put(uid, key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (uid != null ? !uid.equals(user.uid) : user.uid != null) return false;
        if (tinyID != null ? !tinyID.equals(user.tinyID) : user.tinyID != null) return false;
        if (token != null ? !token.equals(user.token) : user.token != null) return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        if (surname != null ? !surname.equals(user.surname) : user.surname != null) return false;
        if (age != null ? !age.equals(user.age) : user.age != null) return false;
        if (phone != null ? !phone.equals(user.phone) : user.phone != null) return false;
        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        if (photoUrl != null ? !photoUrl.equals(user.photoUrl) : user.photoUrl != null)
            return false;
        if (countryISO != null ? !countryISO.equals(user.countryISO) : user.countryISO != null)
            return false;
        if (flock != null ? !flock.equals(user.flock) : user.flock != null) return false;
        if (lastKnownLocation != null ? !lastKnownLocation.equals(user.lastKnownLocation) : user.lastKnownLocation != null)
            return false;
        return emergency != null ? emergency.equals(user.emergency) : user.emergency == null;

    }

    @Override
    public int hashCode() {
        int result = uid != null ? uid.hashCode() : 0;
        result = 31 * result + (tinyID != null ? tinyID.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        result = 31 * result + (age != null ? age.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (photoUrl != null ? photoUrl.hashCode() : 0);
        result = 31 * result + (countryISO != null ? countryISO.hashCode() : 0);
        result = 31 * result + (flock != null ? flock.hashCode() : 0);
        result = 31 * result + (lastKnownLocation != null ? lastKnownLocation.hashCode() : 0);
        result = 31 * result + (emergency != null ? emergency.hashCode() : 0);
        return result;
    }
}