package sfotakos.anightout.events;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import sfotakos.anightout.R;
import sfotakos.anightout.common.Event;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;

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
            Event event = eventList.get(position);

            holder.mEventDate.setText(event.getEventDate());
            holder.mEventName.setText(event.getEventName());

            Place place = event.getPlace();
            if (place != null && place.getName() != null && !place.getName().isEmpty()){
                holder.mEstablishmentName.setText(place.getName());
                holder.mEstablishmentName.setVisibility(View.VISIBLE);
            } else {
                holder.mEstablishmentName.setVisibility(View.GONE);
            }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView mEventDate;
        TextView mEventName;
        TextView mEstablishmentName;

        EventViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            mEventDate = itemView.findViewById(R.id.eventItem_date_tv);
            mEventName = itemView.findViewById(R.id.eventItem_name_tv);
            mEstablishmentName = itemView.findViewById(R.id.eventItem_establishment_tv);
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
