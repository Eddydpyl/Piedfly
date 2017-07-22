package dpyl.eddy.piedfly.model;

import java.util.Map;

public class Flock {

    private String key;
    private String name;
    private Map<String, Boolean> users;

    public Flock() {}

    public Flock(String key, String name, Map<String, Boolean> users) {
        this.key = key;
        this.name = name;
        this.users = users;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Boolean> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Boolean> users) {
        this.users = users;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Flock flock = (Flock) o;

        if (!key.equals(flock.key)) return false;
        if (!name.equals(flock.name)) return false;
        return users != null ? users.equals(flock.users) : flock.users == null;

    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (users != null ? users.hashCode() : 0);
        return result;
    }
}
