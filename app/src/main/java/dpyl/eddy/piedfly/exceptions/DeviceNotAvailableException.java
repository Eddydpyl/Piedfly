package dpyl.eddy.piedfly.exceptions;

public class DeviceNotAvailableException extends RuntimeException{

    public DeviceNotAvailableException() {
        super();
    }

    public DeviceNotAvailableException(String message) {
        super(message);
    }

    public DeviceNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceNotAvailableException(Throwable cause) {
        super(cause);
    }
}