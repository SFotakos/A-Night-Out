package sfotakos.anightout.events;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import sfotakos.anightout.R;
import sfotakos.anightout.common.AccessibilityUtils;
import sfotakos.anightout.common.Event;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;

import static sfotakos.anightout.common.CalendarUtils.getCalendarFromDateTime;

public class EventsRvAdapter extends RecyclerView.Adapter<EventsRvAdapter.EventViewHolder> {

    private final IEventsListener listener;
    private List<Event> eventList;

    public EventsRvAdapter(IEventsListener listener, List<Event> eventList) {
        this.listener = listener;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Context context = holder.mEventName.getContext();
        Event event = eventList.get(position);

        boolean spokenAccessibility = AccessibilityUtils.isAccessibilityEnabled(
                context, AccessibilityServiceInfo.FEEDBACK_SPOKEN);

        if (spokenAccessibility) {
            holder.mEventName.setContentDescription(
                    context.getString(R.string.accessibility_eventItem_eventName, event.getEventName()));
            Calendar cal = getCalendarFromDateTime(event.getEventDate(), event.getEventTime());
            // TODO [QUESTION] This is working fine the first time, but not after that, why?
            holder.mEventDate.setContentDescription(
                    AccessibilityUtils.getAccessibilityDateFromCalendar(cal));
        }

        holder.mEventName.setText(event.getEventName());
        holder.mEventDate.setText(event.getEventDate());
        holder.mEventTime.setText(event.getEventTime());

        Place place = event.getPlace();
        if (place != null && place.getName() != null && !place.getName().isEmpty()) {
            holder.mPlaceName.setText(place.getName());
            holder.mPlaceName.setVisibility(View.VISIBLE);
        } else {
            holder.mPlaceName.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mEventName;
        TextView mEventDate;
        TextView mEventTime;
        TextView mPlaceName;

        EventViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            mEventName = itemView.findViewById(R.id.eventItem_name_tv);
            mEventDate = itemView.findViewById(R.id.eventItem_date_tv);
            mEventTime = itemView.findViewById(R.id.eventItem_time_tv);
            mPlaceName = itemView.findViewById(R.id.eventItem_placeName_tv);
        }

        @Override
        public void onClick(View v) {
            listener.eventClicked(eventList.get(getAdapterPosition()));
        }
    }

    public interface IEventsListener {
        void eventClicked(Event event);
    }
}
