package sfotakos.anightout.place;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;
import sfotakos.anightout.R;
import sfotakos.anightout.databinding.ActivityPlaceDetailsBinding;
import sfotakos.anightout.eventdetails.PlacePhotosRvAdapter;

public class PlaceDetailsActivity extends AppCompatActivity {

    // TODO better name
    public final static String PLACE_EXTRA = "PLACEDETAILS_EXTRA";

    private ActivityPlaceDetailsBinding mBinding;

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
            if (intent.hasExtra(PLACE_EXTRA)) {
                Place mPlace = (Place) intent.getSerializableExtra(PLACE_EXTRA);
                if (mPlace == null) {
                    throw new RuntimeException("Place data was not recovered properly");
                }
            }
        }

        // TODO add google photos api call*
        List<Uri> tempUriList = new ArrayList<>();
        tempUriList.add(Uri.parse("http://placehold.it/350x200&text=image1"));
        tempUriList.add(Uri.parse("http://placehold.it/350x200&text=image2"));
        tempUriList.add(Uri.parse("http://placehold.it/350x200&text=image3"));

        mBinding.placeDetails.placePhotosRv.setAdapter(new PlacePhotosRvAdapter(tempUriList));
        mBinding.placeDetails.placePhotosRv.setLayoutManager(
                new LinearLayoutManager(this,
                        LinearLayoutManager.HORIZONTAL, false));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.place_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_to_event) {
            Toast.makeText(this, "Add to event!", Toast.LENGTH_LONG).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
