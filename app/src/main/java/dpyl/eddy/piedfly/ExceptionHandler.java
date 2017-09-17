package dpyl.eddy.piedfly;

import dpyl.eddy.piedfly.exceptions.CountryIsoNotAvailableException;
import dpyl.eddy.piedfly.exceptions.DeviceNotAvailableException;
import dpyl.eddy.piedfly.exceptions.LocationNotAvailableException;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler{

    private Thread.UncaughtExceptionHandler mDefaultUEH;

    public ExceptionHandler(){
        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    // TODO: Error handling

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (e instanceof DeviceNotAvailableException) {

        } else if (e instanceof LocationNotAvailableException) {

        } else if (e instanceof CountryIsoNotAvailableException) {

        }else if (mDefaultUEH != null) {
            mDefaultUEH.uncaughtException(t, e);
        } else System.exit(1);
    }
}