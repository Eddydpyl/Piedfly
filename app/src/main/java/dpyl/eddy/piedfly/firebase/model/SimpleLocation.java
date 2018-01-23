package dpyl.eddy.piedfly.firebase.model;

import android.location.Location;
import android.support.annotation.Keep;

@Keep
public class SimpleLocation {

    private Long time;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private String provider;

    public SimpleLocation() {}

    public SimpleLocation(Location location) {
        this.time = location.getTime();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.altitude = location.getAltitude();
        this.provider = location.getProvider();
    }

    public SimpleLocation(Long time, Double latitude, Double longitude) {
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleLocation that = (SimpleLocation) o;

        if (!time.equals(that.time)) return false;
        if (!latitude.equals(that.latitude)) return false;
        if (!longitude.equals(that.longitude)) return false;
        if (altitude != null ? !altitude.equals(that.altitude) : that.altitude != null)
            return false;
        return provider != null ? provider.equals(that.provider) : that.provider == null;
    }

    @Override
    public int hashCode() {
        int result = time.hashCode();
        result = 31 * result + latitude.hashCode();
        result = 31 * result + longitude.hashCode();
        result = 31 * result + (altitude != null ? altitude.hashCode() : 0);
        result = 31 * result + (provider != null ? provider.hashCode() : 0);
        return result;
    }
}
