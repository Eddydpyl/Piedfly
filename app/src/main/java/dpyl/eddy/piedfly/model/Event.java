package dpyl.eddy.piedfly.model;

public class Event {

    private String key;
    private Long time;
    private SimpleLocation location;
    private String uid;
    private String text;
    private EventType eventType;

    public Event() {}

    public Event(String key, Long time, SimpleLocation location, String uid, String text, EventType eventType) {
        this.key = key;
        this.time = time;
        this.location = location;
        this.uid = uid;
        this.text = text;
        this.eventType = eventType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

        if (key != null ? !key.equals(event.key) : event.key != null) return false;
        if (!time.equals(event.time)) return false;
        if (location != null ? !location.equals(event.location) : event.location != null)
            return false;
        if (!uid.equals(event.uid)) return false;
        if (text != null ? !text.equals(event.text) : event.text != null) return false;
        return eventType == event.eventType;

    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + time.hashCode();
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + uid.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + eventType.hashCode();
        return result;
    }
}
