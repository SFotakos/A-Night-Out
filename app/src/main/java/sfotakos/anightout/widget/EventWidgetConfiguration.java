package sfotakos.anightout.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

import sfotakos.anightout.R;
import sfotakos.anightout.common.Constants;
import sfotakos.anightout.common.Event;
import sfotakos.anightout.common.data.LocalRepository;
import sfotakos.anightout.events.EventsRvAdapter;

public class EventWidgetConfiguration extends AppCompatActivity implements LocalRepository.ILocalRepositoryCallback {

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private RecyclerView eventsRv;
    private TextView noEventsTv;

    public EventWidgetConfiguration() {
        super();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_event_widget_configuration);

        // In case the user press back
        setResult(RESULT_CANCELED);

        eventsRv = findViewById(R.id.widget_events_rv);
        noEventsTv = findViewById(R.id.widget_noEvents_tv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        eventsRv.setLayoutManager(layoutManager);

        // Load events
        getSupportLoaderManager().initLoader(
                Constants.LOADER_EVENTS, null,
                new LocalRepository(this, this));

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    @Override
    public void onEventListObtained(List<Event> events) {
        if (events == null || events.size() == 0) {
            eventsRv.setVisibility(View.GONE);
            noEventsTv.setVisibility(View.VISIBLE);
        } else {
            eventsRv.setAdapter(new EventsRvAdapter(new EventsRvAdapter.IEventsListener() {
                @Override
                public void eventClicked(Event event) {
                    EventWidgetPreferenceUtil.persistEvent(
                            EventWidgetConfiguration.this, pojoToJson(event), mAppWidgetId);

                    // It is the responsibility of the configuration activity to update the app widget
                    AppWidgetManager appWidgetManager =
                            AppWidgetManager.getInstance(EventWidgetConfiguration.this);
                    EventWidgetProvider.updateAppWidget(
                            EventWidgetConfiguration.this, appWidgetManager, mAppWidgetId);

                    // Make sure we pass back the original appWidgetId
                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                    setResult(RESULT_OK, resultValue);
                    finish();
                }
            }, events));
            eventsRv.setVisibility(View.VISIBLE);
            noEventsTv.setVisibility(View.GONE);
        }
    }

    private String pojoToJson(Object pojo) {
        Gson gson = new Gson();
        return gson.toJson(pojo);
    }
}
