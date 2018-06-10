package sfotakos.anightout.events;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sfotakos.anightout.R;
import sfotakos.anightout.common.Constants;
import sfotakos.anightout.common.Event;
import sfotakos.anightout.databinding.FragmentEventBinding;
import sfotakos.anightout.eventdetails.EventDetailsActivity;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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
                newEventIntent.setAction(Constants.HOME_ACTIVITY_PARENT);
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
        List<Event>  eventList = Event.queryEvents(getActivity().getContentResolver());
        if (eventList != null && eventList.size() > 0) {
            mBinding.eventRv.setAdapter(new EventsRvAdapter(new EventsRvAdapter.IEventsListener() {
                @Override
                public void eventClicked(Event event) {
                    Intent eventDetailsIntent = new Intent(getActivity(), EventDetailsActivity.class);
                    eventDetailsIntent.putExtra(Constants.EVENT_EXTRA, event);
                    eventDetailsIntent.setAction(Constants.HOME_ACTIVITY_PARENT);
                    startActivity(eventDetailsIntent);
                }
            }, eventList));
            mBinding.eventRv.setLayoutManager(new LinearLayoutManager(getContext()));

            mBinding.eventNoEventTextView.setVisibility(View.GONE);
            mBinding.eventRv.setVisibility(View.VISIBLE);
        } else {
            mBinding.eventNoEventTextView.setVisibility(View.VISIBLE);
            mBinding.eventRv.setVisibility(View.GONE);
        }
    }
}
