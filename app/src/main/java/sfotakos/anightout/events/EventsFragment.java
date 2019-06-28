package sfotakos.anightout.events;

import android.app.Activity;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sfotakos.anightout.R;
import sfotakos.anightout.common.Constants;
import sfotakos.anightout.common.Event;
import sfotakos.anightout.common.data.LocalRepository;
import sfotakos.anightout.databinding.FragmentEventBinding;
import sfotakos.anightout.eventdetails.EventDetailsActivity;
import sfotakos.anightout.newevent.NewEventActivity;

public class EventsFragment extends Fragment implements LocalRepository.ILocalRepositoryCallback {

    private FragmentEventBinding mBinding;

    public static EventsFragment newInstance() {
        return new EventsFragment();
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
        Activity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            ((AppCompatActivity) activity).getSupportLoaderManager().initLoader(
                    Constants.LOADER_EVENTS, null,
                    new LocalRepository(getActivity(), this));
        }
    }

    private void setupEventRv(List<Event> events) {
        if (events != null && events.size() > 0) {
            mBinding.eventRv.setAdapter(new EventsRvAdapter(new EventsRvAdapter.IEventsListener() {
                @Override
                public void eventClicked(Event event) {
                    Intent eventDetailsIntent = new Intent(getActivity(), EventDetailsActivity.class);
                    eventDetailsIntent.putExtra(Constants.EVENT_EXTRA, event);
                    eventDetailsIntent.setAction(Constants.HOME_ACTIVITY_PARENT);
                    startActivity(eventDetailsIntent);
                }
            }, events));
            mBinding.eventRv.setLayoutManager(new LinearLayoutManager(getContext()));

            mBinding.eventNoEventTextView.setVisibility(View.GONE);
            mBinding.eventRv.setVisibility(View.VISIBLE);
        } else {
            mBinding.eventNoEventTextView.setVisibility(View.VISIBLE);
            mBinding.eventRv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEventListObtained(List<Event> events) {
        setupEventRv(events);
    }

    public void openEventById(Integer eventId){
        if (mBinding.eventRv.getAdapter() != null) {
            ((EventsRvAdapter) mBinding.eventRv.getAdapter()).openEventById(eventId);
        }
    }
}
