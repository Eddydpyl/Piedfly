package dpyl.eddy.piedfly.exceptions;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler{

    private Thread.UncaughtExceptionHandler mDefaultUEH;

    public ExceptionHandler(){
        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    // TODO: Error handling

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        // Handle specific uncaught exceptions
        if (e instanceof IllegalDatabaseState) {
            // TODO: Report error to developers
        } else if (mDefaultUEH != null) {
            mDefaultUEH.uncaughtException(t, e);
        } else System.exit(1);
    }

    public static void handleException(Thread t, Throwable e) {
        new ExceptionHandler().uncaughtException(t, e);
    }
}