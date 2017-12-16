package dpyl.eddy.piedfly.firebase.model;

import android.support.annotation.Keep;

@Keep
public class Poke {

    private String key;
    private String uid; // The uid of the User that's the target of the poke
    private String trigger; // The uid of the User that triggered the poke
    private String checker; // The uid of the User that stopped the poke
    private SimpleLocation start; // Location of the user that triggered the poke
    private SimpleLocation finish; // Location of the user that finished the poke

    public Poke() {}

    public Poke(String key) {
        this.key = key;
    }

    public Poke(String key, String uid, String trigger, String checker, SimpleLocation start, SimpleLocation finish) {
        this.key = key;
        this.uid = uid;
        this.trigger = trigger;
        this.checker = checker;
        this.start = start;
        this.finish = finish;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Poke)) return false;

        Poke poke = (Poke) o;

        if (key != null ? !key.equals(poke.key) : poke.key != null) return false;
        if (uid != null ? !uid.equals(poke.uid) : poke.uid != null) return false;
        if (trigger != null ? !trigger.equals(poke.trigger) : poke.trigger != null) return false;
        if (checker != null ? !checker.equals(poke.checker) : poke.checker != null) return false;
        if (start != null ? !start.equals(poke.start) : poke.start != null) return false;
        return finish != null ? finish.equals(poke.finish) : poke.finish == null;

    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        result = 31 * result + (trigger != null ? trigger.hashCode() : 0);
        result = 31 * result + (checker != null ? checker.hashCode() : 0);
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (finish != null ? finish.hashCode() : 0);
        return result;
    }
}
