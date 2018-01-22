package dpyl.eddy.piedfly.room;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * DateConverter for room.
 */
public class DateConverter {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

}


