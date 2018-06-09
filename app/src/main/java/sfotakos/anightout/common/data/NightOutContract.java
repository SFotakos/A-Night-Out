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
        public static final String EVENT_TIME = "event_time";
        public static final String EVENT_DESCRIPTION = "event_description";

        public static final String PLACE_ID = "place_id";
        public static final String PLACE_NAME = "place_name";
        public static final String PLACE_PHOTO_REF = "place_photo_ref";
        public static final String PLACE_PRICE_RANGE = "place_price_range";
        public static final String PLACE_ADDRESS = "place_address";
    }

}
