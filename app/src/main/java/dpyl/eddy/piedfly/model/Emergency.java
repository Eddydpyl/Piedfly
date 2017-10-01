package dpyl.eddy.piedfly.model;

import java.util.Map;

public class Emergency {

    private String key;
    private String uid; // The uid of the User that is in danger
    private String trigger; // The uid of the User that triggered the Emergency
    private String checker; // The uid of the User that stopped the Emergency
    private SimpleLocation start; // Last known location of the user that triggered the Emergency when doing so
    private SimpleLocation finish; // Last known location of the user that finished the Emergency when doing so
    private Map<String, Boolean> usersNearby; // Nearby users that agreed to help

    public Emergency() {}

    public Emergency(String key) {
        this.key = key;
    }

    public Emergency(String key, String uid, String trigger, String checker, SimpleLocation start, SimpleLocation finish, Map<String, Boolean> usersNearby) {
        this.key = key;
        this.uid = uid;
        this.trigger = trigger;
        this.checker = checker;
        this.start = start;
        this.finish = finish;
        this.usersNearby = usersNearby;
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

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getChecker() {
        return checker;
    }

    public void setChecker(String checker) {
        this.checker = checker;
    }

    public SimpleLocation getStart() {
        return start;
    }

    public void setStart(SimpleLocation start) {
        this.start = start;
    }

    public SimpleLocation getFinish() {
        return finish;
    }

    public void setFinish(SimpleLocation finish) {
        this.finish = finish;
    }

    public Map<String, Boolean> getUsersNearby() {
        return usersNearby;
    }

    public void setUsersNearby(Map<String, Boolean> usersNearby) {
        this.usersNearby = usersNearby;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Emergency)) return false;

        Emergency emergency = (Emergency) o;

        if (key != null ? !key.equals(emergency.key) : emergency.key != null) return false;
        if (uid != null ? !uid.equals(emergency.uid) : emergency.uid != null) return false;
        if (trigger != null ? !trigger.equals(emergency.trigger) : emergency.trigger != null)
            return false;
        if (checker != null ? !checker.equals(emergency.checker) : emergency.checker != null)
            return false;
        if (start != null ? !start.equals(emergency.start) : emergency.start != null) return false;
        if (finish != null ? !finish.equals(emergency.finish) : emergency.finish != null)
            return false;
        return usersNearby != null ? usersNearby.equals(emergency.usersNearby) : emergency.usersNearby == null;

    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (trigger != null ? trigger.hashCode() : 0);
        result = 31 * result + (checker != null ? checker.hashCode() : 0);
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (finish != null ? finish.hashCode() : 0);
        result = 31 * result + (usersNearby != null ? usersNearby.hashCode() : 0);
        return result;
    }
}
