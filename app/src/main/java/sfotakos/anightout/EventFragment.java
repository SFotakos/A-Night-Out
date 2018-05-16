package sfotakos.anightout;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import sfotakos.anightout.databinding.FragmentEventBinding;

public class EventFragment extends Fragment {

    private FragmentEventBinding mBinding;

    public EventFragment() {
        // Required empty public constructor
    }

    public static EventFragment newInstance() {
        return new EventFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_event, container, false);

        List<Event> eventList = new ArrayList<>();

        Event event = null;
        for (int i = 0; i < 3; i++){
            event = new Event();
            event.setEventDate("16/05/2018 0" + i + ":00");
            event.setEventName("An Event Name #" + i);
            event.setEventEstablishment("An Establishment Name #" + i);
            eventList.add(event);
        }

        mBinding.eventRv.setAdapter(new EventsRvAdapter(eventList));
        mBinding.eventRv.setLayoutManager(new LinearLayoutManager(getContext()));

        mBinding.eventFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Toast Click", Toast.LENGTH_LONG).show();
            }
        });

        return mBinding.getRoot();
    }
}
