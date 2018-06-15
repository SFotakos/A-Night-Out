package sfotakos.anightout.widget;

import android.content.Context;
import android.content.SharedPreferences;

public class EventWidgetPreferenceUtil {


    private static final String widgetPrefs = "WIDGET_PREFERENCES";
    private static final String widgetEvent = "WIDGET_SAVED_EVENT";

    static void persistEvent(Context context, String eventJson, int widgetId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(widgetPrefs, 0);
        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();
        sharedPrefEditor.putString(widgetEvent + widgetId, eventJson);
        sharedPrefEditor.apply();
    }

    static String getPersistedEvent(Context context, int widgetId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(widgetPrefs, 0);
        return sharedPreferences.getString(widgetEvent + widgetId, null);
    }

    static void deleteEvent(Context context, int widgetId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(widgetPrefs, 0);
        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();
        sharedPrefEditor.remove(widgetEvent + widgetId);
        sharedPrefEditor.apply();
    }
}
