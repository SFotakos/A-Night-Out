package sfotakos.anightout.common.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class NightOutContract {

    public static final String CONTENT_AUTHORITY = "sfotakos.anightout";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_EVENTS = "events";

    public static final class EventEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_EVENTS)
                .build();

        public static final String TABLE_NAME = "events";

        public static final String EVENT_ID = "event_id";
        public static final String EVENT_NAME = "event_name";
        public static final String EVENT_DATE = "event_date";
        public static final String EVENT_DESCRIPTION = "event_description";

        public static final String RESTAURANT_NAME = "restaurant_name";
        public static final String RESTAURANT_PRICE_RANGE = "restaurant_price_range";
        public static final String RESTAURANT_ADDRESS = "restaurant_address";
    }

}
