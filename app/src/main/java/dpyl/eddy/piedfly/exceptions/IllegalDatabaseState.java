package dpyl.eddy.piedfly.exceptions;

/**
 * This exception being thrown means that the FireBase database is in a state that was not contemplated by the developers.
 * Either someone managed to make and unauthorised change, or one of the methods doesn't work as expected.
 */

public class IllegalDatabaseState extends RuntimeException {

    public IllegalDatabaseState() {
        super();
    }

    public IllegalDatabaseState(String message) {
        super(message);
    }

    public IllegalDatabaseState(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalDatabaseState(Throwable cause) {
        super(cause);
    }
}
