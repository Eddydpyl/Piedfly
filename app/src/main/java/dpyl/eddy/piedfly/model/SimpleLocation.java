package dpyl.eddy.piedfly.model;

import android.support.annotation.Keep;

@Keep
public class SimpleLocation {

    private Long time;
    private Double latitude;
    private Double longitude;
    private Double altitude;

    public SimpleLocation() {}

    public SimpleLocation(Long time, Double latitude, Double longitude) {
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public SimpleLocation(Long time, Double latitude, Double longitude, Double altitude) {
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleLocation)) return false;

        SimpleLocation that = (SimpleLocation) o;

        if (!time.equals(that.time)) return false;
        if (!latitude.equals(that.latitude)) return false;
        if (!longitude.equals(that.longitude)) return false;
        return altitude != null ? altitude.equals(that.altitude) : that.altitude == null;

    }

    @Override
    public int hashCode() {
        int result = time.hashCode();
        result = 31 * result + latitude.hashCode();
        result = 31 * result + longitude.hashCode();
        result = 31 * result + (altitude != null ? altitude.hashCode() : 0);
        return result;
    }
}
