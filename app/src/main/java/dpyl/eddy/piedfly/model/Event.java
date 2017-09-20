package dpyl.eddy.piedfly.model;

public class Event {

    private Long time;
    private SimpleLocation location;
    private String uid;
    private String text;
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

        if (!time.equals(event.time)) return false;
        if (!location.equals(event.location)) return false;
        if (!uid.equals(event.uid)) return false;
        if (!text.equals(event.text)) return false;
        return eventType == event.eventType;

    }

    @Override
    public int hashCode() {
        int result = time.hashCode();
        result = 31 * result + location.hashCode();
        result = 31 * result + uid.hashCode();
        result = 31 * result + text.hashCode();
        result = 31 * result + eventType.hashCode();
        return result;
    }
}
