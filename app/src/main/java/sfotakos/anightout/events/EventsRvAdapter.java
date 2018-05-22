package sfotakos.anightout.events;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sfotakos.anightout.Event;
import sfotakos.anightout.R;

public class EventsRvAdapter extends RecyclerView.Adapter<EventsRvAdapter.EventViewHolder> {

    private List<Event> eventList = new ArrayList<>();

    public EventsRvAdapter(List<Event> eventList) {
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
            holder.mEstablishmentName.setText(event.getEventEstablishment());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder{

        TextView mEventDate;
        TextView mEventName;
        TextView mEstablishmentName;

        EventViewHolder(View itemView) {
            super(itemView);

            mEventDate = itemView.findViewById(R.id.eventItem_date_tv);
            mEventName = itemView.findViewById(R.id.eventItem_name_tv);
            mEstablishmentName = itemView.findViewById(R.id.eventItem_establishment_tv);
        }
    }
}
