package dpyl.eddy.piedfly;

import dpyl.eddy.piedfly.exceptions.DeviceNotAvailableException;
import dpyl.eddy.piedfly.exceptions.LocationNotAvailableException;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler{

    private Thread.UncaughtExceptionHandler defaultUEH;

    public ExceptionHandler(){
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    // TODO: Error handling

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (e instanceof DeviceNotAvailableException) {

        } else if (e instanceof LocationNotAvailableException) {

        } else if (defaultUEH != null) {
            defaultUEH.uncaughtException(t, e);
        } else System.exit(1);
    }
}