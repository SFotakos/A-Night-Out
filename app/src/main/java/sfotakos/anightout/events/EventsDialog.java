package sfotakos.anightout.events;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.List;

import sfotakos.anightout.R;
import sfotakos.anightout.common.Constants;
import sfotakos.anightout.common.Event;
import sfotakos.anightout.common.LocalRepository;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;
import sfotakos.anightout.databinding.DialogEventsBinding;

public class EventsDialog extends DialogFragment implements LocalRepository.ILocalRepositoryCallback {

    private Place mPlace;
    DialogEventsBinding dialogBinding;
    private IEventsDialog eventsDialogListener;

    public static EventsDialog newInstance(Place place) {
        EventsDialog eventsDialog = new EventsDialog();
        Bundle args = new Bundle();
        args.putSerializable(Constants.PLACE_EXTRA, place);
        eventsDialog.setArguments(args);
        return eventsDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlace = (Place) getArguments().getSerializable(Constants.PLACE_EXTRA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        dialogBinding = DataBindingUtil.inflate(inflater,
                R.layout.dialog_events, null, false);

        dialogBinding.addEventRootCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                eventsDialogListener.newEventClicked();
            }
        });

        dialogBinding.addEventEventsRv.setLayoutManager(new LinearLayoutManager(getActivity()));

        Activity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            ((AppCompatActivity) activity).getSupportLoaderManager().initLoader(
                    Constants.LOADER_EVENTS, null,
                    new LocalRepository(getActivity(), this));
        }
        return dialogBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        Point size = new Point();

        Display display = window.getWindowManager().getDefaultDisplay();
        display.getSize(size);

        int width = size.x;
        int height = size.y;

        window.setLayout((int) (width * 0.8), (int) (height * 0.7));
        window.setGravity(Gravity.CENTER);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            eventsDialogListener = (IEventsDialog) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement IEventsDialog");
        }
    }

    @Override
    public void onEventListObtained(List<Event> events) {
        dialogBinding.addEventEventsRv
                .setAdapter(new EventsRvAdapter(new EventsRvAdapter.IEventsListener() {
                    @Override
                    public void eventClicked(Event event) {
                        dismiss();
                        eventsDialogListener.eventClicked(event);
                    }
                }, events));
    }

    public interface IEventsDialog {
        void eventClicked(Event event);

        void newEventClicked();
    }
}
