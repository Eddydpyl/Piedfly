package dpyl.eddy.piedfly.model;

import java.util.List;
import java.util.Map;

public class Emergency {

    private String key;
    private SimpleLocation location;
    private Map<String, Boolean> usersNearby;
    private List<Event> events;

    public Emergency() {}

    public Emergency(String key, SimpleLocation location, Map<String, Boolean> usersNearby, List<Event> events) {
        this.key = key;
        this.location = location;
        this.usersNearby = usersNearby;
        this.events = events;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public SimpleLocation getLocation() {
        return location;
    }

    public void setLocation(SimpleLocation location) {
        this.location = location;
    }

    public Map<String, Boolean> getUsersNearby() {
        return usersNearby;
    }

    public void setUsersNearby(Map<String, Boolean> usersNearby) {
        this.usersNearby = usersNearby;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Emergency)) return false;

        Emergency emergency = (Emergency) o;

        if (key != null ? !key.equals(emergency.key) : emergency.key != null) return false;
        if (location != null ? !location.equals(emergency.location) : emergency.location != null)
            return false;
        if (usersNearby != null ? !usersNearby.equals(emergency.usersNearby) : emergency.usersNearby != null)
            return false;
        return events != null ? events.equals(emergency.events) : emergency.events == null;

    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (usersNearby != null ? usersNearby.hashCode() : 0);
        result = 31 * result + (events != null ? events.hashCode() : 0);
        return result;
    }
}
