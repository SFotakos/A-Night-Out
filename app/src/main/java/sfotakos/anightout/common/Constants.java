package sfotakos.anightout.common;

public class Constants {
    public final static String PLACE_EXTRA = "placeDetails_placeExtra";
    public final static String EVENT_EXTRA = "eventDetails_eventExtra";
    public final static String SAVED_EVENT_ID_EXTRA = "savedEventId";

    public final static String STATE_MAP = "state_map";

    public final static int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 28145;
    public final static int LOCATION_PERMISSION_REQUEST_CODE = 12045;
    public final static int REQUEST_GPS_SETTINGS_CODE = 45012;

    public static final String PLACE_DETAILS_ACTIVITY_PARENT = "placeDetailsParent";
    public static final String HOME_ACTIVITY_PARENT = "homeActivityParent";

    public static final int NEW_EVENT_RESULT_CODE = 20194;
    public static final int PLACE_ADDED_TO_EVENT_RESULT_CODE = 58903;
    public final static int EVENT_NAME_MIN_LENGTH = 5;

    public static final String MAP_TUTORIAL = "mapTutorial";
    public static final String MAP_FILTER_TUTORIAL_PRICE = "mapFilterTutorial_price";

    public enum HomeTabs {
        EVENT_TAB(0),
        MAP_TAB(1);

        private int tabPosition;

        HomeTabs(int tabPosition){
            this.tabPosition = tabPosition;
        }

        public int getTabPosition() {
            return tabPosition;
        }
    }
}
