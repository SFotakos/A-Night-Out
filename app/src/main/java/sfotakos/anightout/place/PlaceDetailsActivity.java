package sfotakos.anightout.place;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import sfotakos.anightout.R;
import sfotakos.anightout.common.Constants;
import sfotakos.anightout.common.Event;
import sfotakos.anightout.common.NetworkUtil;
import sfotakos.anightout.common.google_maps_places_photos_api.GooglePlacesRequest;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;
import sfotakos.anightout.databinding.ActivityPlaceDetailsBinding;
import sfotakos.anightout.databinding.LayoutAddEventBinding;
import sfotakos.anightout.eventdetails.PlacePhotosRvAdapter;
import sfotakos.anightout.events.EventsRvAdapter;
import sfotakos.anightout.newevent.NewEventActivity;

//TODO query place details and fill layout with more information
public class PlaceDetailsActivity extends AppCompatActivity {

    private ActivityPlaceDetailsBinding mBinding;
    private AlertDialog mEventsDialog;
    private Place mPlace;

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

                mBinding.placeDetails.placeNameTextView.setText(mPlace.getName());
                mBinding.placeDetails.placeAddressTextView.setText(mPlace.getVicinity());

                if (mPlace.getPriceLevel() == null) {
                    mBinding.placeDetails.placePriceTextView.setVisibility(View.GONE);
                } else {
                    mBinding.placeDetails.placePriceTextView.setVisibility(View.VISIBLE);
                    mBinding.placeDetails.placePriceTextView.setText(
                            GooglePlacesRequest.PlacePrice.getDescriptionByTag(
                                    mPlace.getPriceLevel().toString()));
                }

                // TODO move this to a request class which returns the fully qualified uri
                if (mPlace.getPhotos() != null &&
                        mPlace.getPhotos().size() != 0 &&
                        mPlace.getPhotos().get(0) != null) {

                    List<Uri> photosUri = new ArrayList<>();
                    Uri photoUri = Uri.parse(NetworkUtil.GOOGLE_PLACE_API_BASE_URL).buildUpon()
                            .appendPath("photo")
                            .appendQueryParameter("key", getResources().getString(R.string.google_places_key))
                            .appendQueryParameter("maxheight", "400")
                            .appendQueryParameter("photo_reference", mPlace.getPhotos().get(0).getPhotoReference())
                            .build();

                    photosUri.add(photoUri);

                    mBinding.placeDetails.placePhotosRv.setVisibility(View.VISIBLE);
                    mBinding.placeDetails.placePhotosRv.setAdapter(new PlacePhotosRvAdapter(photosUri));
                    mBinding.placeDetails.placePhotosRv.setLayoutManager(
                            new LinearLayoutManager(this,
                                    LinearLayoutManager.HORIZONTAL, false));
                } else {
                    mBinding.placeDetails.placePhotosRv.setVisibility(View.GONE);
                }
            }
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
            final LayoutAddEventBinding dialogBinding =
                    DataBindingUtil.inflate(getLayoutInflater(),
                            R.layout.layout_add_event, null, false);

            dialogBinding.addEventRootCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newEventIntent =
                            new Intent(PlaceDetailsActivity.this, NewEventActivity.class);
                    newEventIntent.setAction(Constants.PLACE_DETAILS_ACTIVITY_PARENT);
                    startActivityForResult(newEventIntent, Constants.NEW_EVENT_RESULT_CODE);
                    if (mEventsDialog != null) {
                        mEventsDialog.dismiss();
                    }
                }
            });

            dialogBinding.addEventEventsRv
                    .setAdapter(new EventsRvAdapter(new EventsRvAdapter.IEventsListener() {
                        @Override
                        public void eventClicked(Event event) {
                            Event.updateEventWithPlace(getContentResolver(),
                                    Long.toString(event.getEventId()), mPlace);
                            if (mEventsDialog != null) {
                                mEventsDialog.dismiss();
                            }
                            navigateToEvent();
                        }

                    }, Event.queryEvents(getContentResolver())));
            dialogBinding.addEventEventsRv.setLayoutManager(new LinearLayoutManager(this));

            mEventsDialog = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.placeDetails_eventsDialog_title))
                    .setView(dialogBinding.getRoot()).show();
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
                    Event.updateEventWithPlace(getContentResolver(), Long.toString(eventId), mPlace);
                    navigateToEvent();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void navigateToEvent(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
