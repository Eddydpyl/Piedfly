package dpyl.eddy.piedfly.model;

import java.util.Map;

public class User {

    private String uid;
    private String token;
    private String name;
    private String surname;
    private Integer age;
    private String phone;
    private String email;
    private String photoUrl;
    private String countryISO;
    private SimpleLocation lastKnownLocation;
    private Map<String, Boolean> flocks;

    public User() {}

    public User(String uid) {
        this.uid = uid;
    }

    public User(String uid, String token, String phone, String email) {
        this.uid = uid;
    }

    public User(String uid, String token, String name, String surname, Integer age, String phone, String email, String photoUrl, String countryISO, SimpleLocation lastKnownLocation, Map<String, Boolean> flocks) {
        this.uid = uid;
        this.token = token;
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.phone = phone;
        this.email = email;
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
        if (lastKnownLocation != null ? !lastKnownLocation.equals(user.lastKnownLocation) : user.lastKnownLocation != null)
            return false;
        return flocks != null ? flocks.equals(user.flocks) : user.flocks == null;

    }

    @Override
    public int hashCode() {
        int result = uid.hashCode();
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        result = 31 * result + (age != null ? age.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (photoUrl != null ? photoUrl.hashCode() : 0);
        result = 31 * result + (countryISO != null ? countryISO.hashCode() : 0);
        result = 31 * result + (lastKnownLocation != null ? lastKnownLocation.hashCode() : 0);
        result = 31 * result + (flocks != null ? flocks.hashCode() : 0);
        return result;
    }
}