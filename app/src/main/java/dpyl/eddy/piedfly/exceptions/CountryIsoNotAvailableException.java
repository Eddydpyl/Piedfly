package dpyl.eddy.piedfly.exceptions;

public class CountryIsoNotAvailableException extends RuntimeException{

    public CountryIsoNotAvailableException() {
        super();
    }

    public CountryIsoNotAvailableException(String message) {
        super(message);
    }

    public CountryIsoNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public CountryIsoNotAvailableException(Throwable cause) {
        super(cause);
    }
}
