package dpyl.eddy.piedfly.firebase.model;

import android.support.annotation.Keep;

@Keep
public class Request {

    private String uid; // The uid of the User that's the target of the request
    private String trigger; // The uid of the User that sent the request
    private RequestType requestType;

    public Request() {}

    public Request(String uid, String trigger, RequestType requestType) {
        this.uid = uid;
        this.trigger = trigger;
        this.requestType = requestType;
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

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Request)) return false;

        Request request = (Request) o;

        if (!uid.equals(request.uid)) return false;
        if (!trigger.equals(request.trigger)) return false;
        return requestType == request.requestType;

    }

    @Override
    public int hashCode() {
        int result = uid.hashCode();
        result = 31 * result + trigger.hashCode();
        result = 31 * result + requestType.hashCode();
        return result;
    }
}
