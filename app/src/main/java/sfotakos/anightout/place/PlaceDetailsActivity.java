package sfotakos.anightout.place;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import sfotakos.anightout.R;
import sfotakos.anightout.common.Constants;
import sfotakos.anightout.common.Event;
import sfotakos.anightout.common.data.UpdatePlaceService;
import sfotakos.anightout.common.google_maps_places_photos_api.GooglePlacesRequest;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;
import sfotakos.anightout.databinding.ActivityPlaceDetailsBinding;
import sfotakos.anightout.eventdetails.PlacePhotosRvAdapter;
import sfotakos.anightout.events.EventsDialog;
import sfotakos.anightout.newevent.NewEventActivity;

//TODO query place details and fill layout with more information
public class PlaceDetailsActivity extends AppCompatActivity implements EventsDialog.IEventsDialog {

    private ActivityPlaceDetailsBinding mBinding;
    private EventsDialog mEventsDialog;
    private Place mPlace;
    private UpdatedEventReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_place_details);

        setSupportActionBar(mBinding.placeDetailsToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(Constants.PLACE_EXTRA)) {
                mPlace = (Place) intent.getSerializableExtra(Constants.PLACE_EXTRA);
                if (mPlace == null) {
                    throw new RuntimeException("Place data was not recovered properly");
                }

                getSupportActionBar().setTitle(mPlace.getName());
                getSupportActionBar().setDisplayShowTitleEnabled(true);

                if (mPlace.getPriceLevel() == null) {
                    mBinding.placeDetails.placePriceTextView.setVisibility(View.GONE);
                } else {
                    mBinding.placeDetails.placePriceTextView.setVisibility(View.VISIBLE);
                    mBinding.placeDetails.placePriceTextView.setText(getString(
                            GooglePlacesRequest.PlacePrice.getDescriptionByTag(
                                    Integer.toString(mPlace.getPriceLevel()))));
                }

                //TODO fetch more photos from place details and add to the list
                List<Place> places = new ArrayList<>();
                places.add(mPlace);
                // TODO add snapping into position for a gallery like effect
                // TODO add paging, something like https://stackoverflow.com/a/46084182
                mBinding.placeDetails.placePhotosRv.setVisibility(View.VISIBLE);
                mBinding.placeDetails.placePhotosRv.setAdapter(
                        new PlacePhotosRvAdapter(places, false));
                mBinding.placeDetails.placePhotosRv.setLayoutManager(
                        new LinearLayoutManager(this,
                                LinearLayoutManager.HORIZONTAL, false));
            }
            mReceiver = new UpdatedEventReceiver();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.place_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_to_event) {
            mEventsDialog = EventsDialog.newInstance(mPlace);
            mEventsDialog.show(getFragmentManager(), "EventsDialogFragment");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.NEW_EVENT_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                long eventId = data.getLongExtra(Constants.SAVED_EVENT_ID_EXTRA, -1);
                if (eventId != -1) {
                    updateEventWithPlace(Long.toString(eventId));
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, new IntentFilter(Constants.PLACE_SERVICE_BROADCAST_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void eventClicked(Event event) {
        updateEventWithPlace(Integer.toString(event.getEventId()));
    }

    private void updateEventWithPlace(String eventId){
        Intent i = new Intent(this, UpdatePlaceService.class);
        i.putExtra(Constants.SAVED_EVENT_ID_EXTRA, eventId);
        i.putExtra(Constants.PLACE_EXTRA, mPlace);
        startService(i);
    }

    @Override
    public void newEventClicked() {
        Intent newEventIntent = new Intent(this, NewEventActivity.class);
        newEventIntent.setAction(Constants.PLACE_DETAILS_ACTIVITY_PARENT);
        startActivityForResult(newEventIntent, Constants.NEW_EVENT_RESULT_CODE);
    }

    private void navigateToEvent() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    class UpdatedEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            navigateToEvent();
        }
    }
}
