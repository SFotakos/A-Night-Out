package sfotakos.anightout.events;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import sfotakos.anightout.R;
import sfotakos.anightout.common.Event;
import sfotakos.anightout.common.data.NightOutContract.EventEntry;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;
import sfotakos.anightout.databinding.FragmentEventBinding;
import sfotakos.anightout.eventdetails.EventDetailsActivity;
import sfotakos.anightout.home.HomeActivity;
import sfotakos.anightout.newevent.NewEventActivity;

public class EventsFragment extends Fragment {

    private FragmentEventBinding mBinding;

    public EventsFragment() {
        // Required empty public constructor
    }

    public static EventsFragment newInstance() {
        return new EventsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_event, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.eventFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newEventIntent = new Intent(getActivity(), NewEventActivity.class);
                newEventIntent.setAction(HomeActivity.HOME_ACTIVITY_PARENT);
                startActivity(newEventIntent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setupEventRv();
    }

    private void setupEventRv() {
        mBinding.eventRv.setAdapter(new EventsRvAdapter(new EventsRvAdapter.IEventsListener() {
            @Override
            public void eventClicked(Event event) {
                Intent eventDetailsIntent = new Intent(getActivity(), EventDetailsActivity.class);
                eventDetailsIntent.putExtra(EventDetailsActivity.EVENT_EXTRA, event);
                eventDetailsIntent.setAction(HomeActivity.HOME_ACTIVITY_PARENT);
                startActivity(eventDetailsIntent);
            }
        }, queryEvents()));
        mBinding.eventRv.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    // TODO this is duplicated
    private List<Event> queryEvents() {
        List<Event> eventList = new ArrayList<>();
        Cursor cursor = getActivity().getContentResolver().query(
                EventEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Event event = new Event();
                int eventIdIndex = cursor.getColumnIndex(EventEntry.EVENT_ID);
                int eventNameIndex = cursor.getColumnIndex(EventEntry.EVENT_NAME);
                int eventDateIndex = cursor.getColumnIndex(EventEntry.EVENT_DATE);
                int eventDescriptionIndex = cursor.getColumnIndex(EventEntry.EVENT_DESCRIPTION);

                int placeNameIndex = cursor.getColumnIndex(EventEntry.RESTAURANT_NAME);
                int placePriceRangeIndex = cursor.getColumnIndex(EventEntry.RESTAURANT_PRICE_RANGE);
                int placeAddressIndex = cursor.getColumnIndex(EventEntry.RESTAURANT_ADDRESS);

                event.setEventId((cursor.getInt(eventIdIndex)));
                event.setEventName(cursor.getString(eventNameIndex));
                event.setEventDate(cursor.getString(eventDateIndex));
                event.setEventDescription(cursor.getString(eventDescriptionIndex));

                Place place = new Place();
                place.setName(cursor.getString(placeNameIndex));
                place.setPriceLevel(cursor.getInt(placePriceRangeIndex));
                place.setVicinity(cursor.getString(placeAddressIndex));

                event.setPlace(place);
                eventList.add(event);
            }
            cursor.close();
        }
        return eventList;
    }
}
