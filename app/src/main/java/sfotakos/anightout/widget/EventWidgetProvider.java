package sfotakos.anightout.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import sfotakos.anightout.R;
import sfotakos.anightout.common.Constants;
import sfotakos.anightout.common.Event;
import sfotakos.anightout.eventdetails.EventDetailsActivity;

public class EventWidgetProvider extends AppWidgetProvider {

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.event_widget);

        String jsonRecipe = EventWidgetPreferenceUtil.getPersistedEvent(context, appWidgetId);
        Event event = eventFromJson(jsonRecipe);
        if (event != null) {

            views.setTextViewText(R.id.widgetEventItem_name_tv, event.getEventName());
            views.setTextViewText(R.id.widgetEventItem_date_tv, event.getEventDate());
            views.setTextViewText(R.id.widgetEventItem_time_tv, event.getEventTime());

            // Pending intent that opens activity with the proper event
            Intent eventDetailsIntent = new Intent(context, EventDetailsActivity.class);
            eventDetailsIntent.putExtra(Constants.EVENT_EXTRA, event);
            eventDetailsIntent.setAction(Constants.HOME_ACTIVITY_PARENT);
            PendingIntent eventDetailsPendingIntent =
                    PendingIntent.getActivity(context, appWidgetId, eventDetailsIntent, 0);
            views.setOnClickPendingIntent(R.id.widgetRoot_rl, eventDetailsPendingIntent);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            EventWidgetPreferenceUtil.deleteEvent(context, appWidgetId);
        }
        super.onDeleted(context, appWidgetIds);
    }

    public static Event eventFromJson(String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<Event>() {
        }.getType();
        Event event = gson.fromJson(json, listType);
        return event;
    }
}
