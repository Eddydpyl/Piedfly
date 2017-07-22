package dpyl.eddy.piedfly.exceptions;

public class LocationNotAvailableException extends RuntimeException {

    public LocationNotAvailableException() {
        super();
    }

    public LocationNotAvailableException(String message) {
        super(message);
    }

    public LocationNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public LocationNotAvailableException(Throwable cause) {
        super(cause);
    }
}
