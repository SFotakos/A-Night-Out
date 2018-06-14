package sfotakos.anightout.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

public class CalendarUtils {

    public static Calendar getCalendarFromDateTime(String dateStr, String timeStr) {
        DateFormat dateTimeFormat =
                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateTimeFormat.parse(dateStr + " " + timeStr));
            return cal;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
