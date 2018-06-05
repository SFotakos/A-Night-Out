package sfotakos.anightout.eventdetails;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import sfotakos.anightout.R;
import sfotakos.anightout.common.Event;
import sfotakos.anightout.common.NetworkUtil;
import sfotakos.anightout.common.google_maps_places_photos_api.model.GooglePlacesRequestParams;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;
import sfotakos.anightout.databinding.ActivityEventDetailsBinding;
import sfotakos.anightout.home.HomeActivity;
import sfotakos.anightout.place.PlaceDetailsActivity;

import static sfotakos.anightout.home.HomeActivity.HOME_ACTIVITY_PARENT;
import static sfotakos.anightout.place.PlaceDetailsActivity.PLACE_DETAILS_ACTIVITY_PARENT;

public class EventDetailsActivity extends AppCompatActivity {

    // TODO better name
    public final static String EVENT_EXTRA = "EVENTDETAILS_EVENT";

    private ActivityEventDetailsBinding mBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_event_details);

        Event mEvent = null;
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EVENT_EXTRA)) {
                mEvent = (Event) intent.getSerializableExtra(EVENT_EXTRA);
                if (mEvent == null) {
                    throw new RuntimeException("Event data was not recovered properly");
                }

                setSupportActionBar(mBinding.eventDetailsToolbar);
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setTitle(mEvent.getEventName());
                }

                mBinding.eventDetailsDescriptionTextView.setText(mEvent.getEventDescription());
                mBinding.eventDetailsDateTimeTextView.setText(mEvent.getEventDate());

                Place place = mEvent.getPlace();
                if (place != null) {
                    if (place.getPhotos() != null && place.getPhotos().size() > 0) {
                        List<Uri> photosUri = new ArrayList<>();
                        Uri photoUri = Uri.parse(NetworkUtil.GOOGLE_PLACE_API_BASE_URL).buildUpon()
                                .appendPath("photo")
                                .appendQueryParameter("key", getResources().getString(R.string.google_places_key))
                                .appendQueryParameter("maxheight", "400")
                                .appendQueryParameter("photo_reference", mEvent.getPlace().getPhotos().get(0).getPhotoReference())
                                .build();

                        photosUri.add(photoUri);
                        // TODO add snapping into position for a gallery like effect
                        // TODO add paging, something like https://stackoverflow.com/a/46084182
                        mBinding.placeDetails.placePhotosRv.setVisibility(View.VISIBLE);
                        mBinding.placeDetails.placePhotosRv.setAdapter(new PlacePhotosRvAdapter(photosUri));
                        mBinding.placeDetails.placePhotosRv.setLayoutManager(
                                new LinearLayoutManager(this,
                                        LinearLayoutManager.HORIZONTAL, false));
                    } else {
                        mBinding.placeDetails.placePhotosRv.setVisibility(View.GONE);
                    }

                    mBinding.placeDetails.placeNameTextView.setText(place.getName());
                    mBinding.placeDetails.placeAddressTextView.setText(place.getVicinity());

                    if (place.getPriceLevel() <= 4) {
                        mBinding.placeDetails.placePriceTextView.setVisibility(View.VISIBLE);
                        mBinding.placeDetails.placePriceTextView.setText(
                                GooglePlacesRequestParams.PlacePrice.getDescriptionByTag(
                                        Integer.toString(place.getPriceLevel())));
                    } else {
                        mBinding.placeDetails.placePriceTextView.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.detailsAction_share:
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                return true;
            default:
                break;
        }
        return false;
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return this.getNavigationUpIntent();
    }

    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        return this.getNavigationUpIntent();
    }

    private Intent getNavigationUpIntent() {
        Intent navigationIntent = null;

        String action = getIntent().getAction();
        if (action != null) {
            switch (action) {
                case HOME_ACTIVITY_PARENT:
                    navigationIntent = new Intent(this, HomeActivity.class);
                    navigationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    break;
                case PLACE_DETAILS_ACTIVITY_PARENT:
                    navigationIntent = new Intent(this, PlaceDetailsActivity.class);
                    navigationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    break;

                default:
                    throw new InvalidParameterException("Unknown parent activity");
            }
        }
        return navigationIntent;
    }
}
