package dpyl.eddy.piedfly.model;

public class Event {

    private Long time; // Time at which the Event was triggered
    private SimpleLocation location; // Last known location of the user that triggered the Event
    private String uid; // The uid of the User that triggered the Event
    private String text; // Information pertaining the Event
    private EventType eventType;

    public Event() {}

    public Event(Long time, SimpleLocation location, String uid, String text, EventType eventType) {
        this.time = time;
        this.location = location;
        this.uid = uid;
        this.text = text;
        this.eventType = eventType;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public SimpleLocation getLocation() {
        return location;
    }

    public void setLocation(SimpleLocation location) {
        this.location = location;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;

        Event event = (Event) o;

        if (time != null ? !time.equals(event.time) : event.time != null) return false;
        if (location != null ? !location.equals(event.location) : event.location != null)
            return false;
        if (uid != null ? !uid.equals(event.uid) : event.uid != null) return false;
        if (text != null ? !text.equals(event.text) : event.text != null) return false;
        return eventType == event.eventType;

    }

    @Override
    public int hashCode() {
        int result = time != null ? time.hashCode() : 0;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
        return result;
    }
}
