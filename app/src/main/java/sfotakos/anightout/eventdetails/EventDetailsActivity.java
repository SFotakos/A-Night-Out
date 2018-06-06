package sfotakos.anightout.eventdetails;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.ByteArrayOutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import sfotakos.anightout.R;
import sfotakos.anightout.common.Event;
import sfotakos.anightout.common.NetworkUtil;
import sfotakos.anightout.common.data.NightOutContract;
import sfotakos.anightout.common.google_maps_places_photos_api.model.GooglePlacesRequestParams;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;
import sfotakos.anightout.databinding.ActivityEventDetailsBinding;
import sfotakos.anightout.home.HomeActivity;
import sfotakos.anightout.place.PlaceDetailsActivity;

import static sfotakos.anightout.home.HomeActivity.HOME_ACTIVITY_PARENT;
import static sfotakos.anightout.place.PlaceDetailsActivity.PLACE_DETAILS_ACTIVITY_PARENT;

public class EventDetailsActivity extends AppCompatActivity {

    public final static int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 28145;

    public final static String EVENT_EXTRA = "EVENTDETAILS_EVENT";

    private ActivityEventDetailsBinding mBinding;
    private Event mEvent;
    private AlertDialog deleteConfirmationDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_event_details);

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
                    if (place.getPhotos() != null &&
                            place.getPhotos().size() > 0 &&
                            place.getPhotos().get(0).getPhotoReference() != null) {
                        List<Uri> photosUri = new ArrayList<>();
                        Uri photoUri = Uri.parse(NetworkUtil.GOOGLE_PLACE_API_BASE_URL).buildUpon()
                                .appendPath("photo")
                                .appendQueryParameter("key", getResources().getString(R.string.google_places_key))
                                .appendQueryParameter("maxheight", "400")
                                .appendQueryParameter("photo_reference", place.getPhotos().get(0).getPhotoReference())
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

                    if (place.getName() != null) {
                        mBinding.placeDetails.placeNameTextView.setVisibility(View.VISIBLE);
                        mBinding.placeDetails.placeNameTextView.setText(place.getName());
                    } else {
                        mBinding.placeDetails.placeNameTextView.setVisibility(View.GONE);
                    }

                    if (place.getVicinity() != null) {
                        mBinding.placeDetails.placeAddressTextView.setVisibility(View.VISIBLE);
                        mBinding.placeDetails.placeAddressTextView.setText(place.getVicinity());
                    } else {
                        mBinding.placeDetails.placeAddressTextView.setVisibility(View.GONE);
                    }

                    if (place.getPriceLevel() >= 0) {
                        mBinding.placeDetails.placePriceTextView.setVisibility(View.VISIBLE);
                        mBinding.placeDetails.placePriceTextView.setText(
                                GooglePlacesRequestParams.PlacePrice.getDescriptionByTag(
                                        Integer.toString(place.getPriceLevel())));
                    } else {
                        mBinding.placeDetails.placePriceTextView.setVisibility(View.GONE);
                    }
                } else {
                    mBinding.placeDetails.placePhotosRv.setVisibility(View.GONE);
                    mBinding.placeDetails.placeNameTextView.setVisibility(View.GONE);
                    mBinding.placeDetails.placeAddressTextView.setVisibility(View.GONE);
                    mBinding.placeDetails.placePriceTextView.setVisibility(View.GONE);
                    mBinding.placeDetails.placePriceTextView.setVisibility(View.GONE);
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

            case R.id.detailsAction_share:
                if (hasStoragePermission()) {
                    shareEvent();
                } else {
                    requestStoragePermission();
                }
                return true;

            case R.id.detailsAction_delete:
                if (deleteConfirmationDialog == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Uri eventByIdUri = NightOutContract.EventEntry.CONTENT_URI.buildUpon()
                                    .appendPath(String.valueOf(mEvent.getEventId())).build();

                            getContentResolver().delete(eventByIdUri, null, null);
                            onNavigateUp();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    builder.setTitle("Confirm event deletion?");
                    deleteConfirmationDialog = builder.create();
                }

                if (!deleteConfirmationDialog.isShowing())
                    deleteConfirmationDialog.show();

                return true;

            default:
                break;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            // After permission was granted
            case WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    shareEvent();
                }
            }
        }
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

    private boolean hasStoragePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
    }

    private void shareEvent() {
        Bitmap shareableBitmap = getBitmapByView(mBinding.eventDetailsShareableContentCl);

        final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Intent.EXTRA_STREAM, getImageUri(this, shareableBitmap));
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent, "Share image with ..."));
    }

    // As seen from https://www.logicchip.com/share-image-without-saving/
    private Bitmap getBitmapByView(ViewGroup view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return bitmap;
    }

    // As seen from https://stackoverflow.com/a/16168087/5075144
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(),
                inImage, "Title", null);
        return Uri.parse(path);
    }
}
