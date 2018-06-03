package sfotakos.anightout.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sfotakos.anightout.common.DrawableUtils;
import sfotakos.anightout.common.google_maps_places_photos_api.model.GooglePlacesResponse;
import sfotakos.anightout.common.IconAndTextAdapter;
import sfotakos.anightout.common.NetworkUtil;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;
import sfotakos.anightout.R;
import sfotakos.anightout.databinding.FragmentMapBinding;
import sfotakos.anightout.newevent.GooglePlacesRequestParams;
import sfotakos.anightout.place.PlaceDetailsActivity;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
// TODO finish filter layout
// TODO persist state after rotation
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.CancelableCallback, Callback<GooglePlacesResponse> {

    public final static int LOCATION_PERMISSION_REQUEST_CODE = 12045;
    public final static int REQUEST_GPS_SETTINGS_CODE = 45012;

    private static int ANIMATION_DURATION = 600;
    private static int DEFAULT_ZOOM_LEVEL = 15;
    private static int MIN_SEARCH_RADIUS = 100; // This is a workaround so SeekBar has a min value

    private FragmentMapBinding mBinding;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    private GoogleMap mGoogleMap;
    private Marker mCenterMarker;
    private Circle mSearchCircle;
    private LatLng mClickedLatLng;

    private int mZoomLevel = DEFAULT_ZOOM_LEVEL;

    private boolean hasZoomedIn = false;
    private boolean isRequestLocationUpdatesActive = false; //This ensures only one listener is set to receive location update at a time

    private List<Marker> searchedPlacesMarker = new ArrayList<>();
    private HashMap<String, Place> searchedPlaces = new HashMap<>();
    private GooglePlacesRequestParams mPlacesRequest = new GooglePlacesRequestParams();

    private boolean isPriceFilteringEnabled = true;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment supportMapFragment =
                ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        supportMapFragment.getMapAsync(this);

        setupFragment();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        if (getUserVisibleHint() && !hasZoomedIn) {
            getUserLastKnownLocationWithChecks();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !hasZoomedIn) {
            getUserLastKnownLocationWithChecks();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            // After permission was granted
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Check if GPS is turned on
                    requestGPS();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Return when GPS was turned on after request
        if (requestCode == REQUEST_GPS_SETTINGS_CODE) {
            if (resultCode == RESULT_OK) {
                setRequestLocationUpdates();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                getContext(), R.raw.maps_style_json));

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                cleanMap();

                mClickedLatLng = latLng;
                moveMapToUserLocation(latLng, true);
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Place place = null;
                if (searchedPlaces != null) {
                    place = searchedPlaces.get(marker.getId());
                    if (place != null) {
                        Intent placeDetailsIntent = new Intent(getActivity(), PlaceDetailsActivity.class);
                        placeDetailsIntent.putExtra(PlaceDetailsActivity.PLACE_EXTRA, place);
                        startActivity(placeDetailsIntent);
                    }
                }
                return true;
            }
        });
    }

    //region cameraAnimationCallback
    @Override
    public void onFinish() {
        Bitmap markerBitmap = DrawableUtils.getBitmapFromVectorDrawable(
                getContext(), R.drawable.ic_pin);
        Bitmap tintedMarkerBitmap = DrawableUtils.tintImage(markerBitmap,
                ContextCompat.getColor(getContext(), R.color.colorPrimary));

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(tintedMarkerBitmap);

        mCenterMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(mClickedLatLng)
                .icon(bitmapDescriptor)
                .draggable(false));

        int seekbarProgress = 0;
        try {
            seekbarProgress = mBinding.mapFilter.filterDistanceSeekBar.getProgress();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        setSearchCircle(MIN_SEARCH_RADIUS + seekbarProgress);
        showFilter();
    }

    @Override
    public void onCancel() {
    }
    //endregion

    //region retrofitCallResponse
    @Override
    public void onResponse(@NonNull Call<GooglePlacesResponse> call, @NonNull Response<GooglePlacesResponse> response) {
        canUseFilterActions(true);
        GooglePlacesResponse placesResponse = response.body();
        if (placesResponse != null) {
            List<Place> places = placesResponse.getResults();
            for (final Place place : places) {
                LatLng placeLatLng = new LatLng(place.getGeometry().getLocation().getLat(),
                        place.getGeometry().getLocation().getLng());

                MarkerOptions placeMarketOptions = new MarkerOptions()
                        .position(placeLatLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(
                                DrawableUtils.getBitmapFromVectorDrawable(
                                        getContext(), R.drawable.ic_store)))
                        .draggable(false);

                Marker placeMarker = mGoogleMap.addMarker(placeMarketOptions);
                searchedPlacesMarker.add(placeMarker);
                searchedPlaces.put(placeMarker.getId(), place);
            }
        }
    }

    @Override
    public void onFailure(@NonNull Call<GooglePlacesResponse> call, @NonNull Throwable t) {
        canUseFilterActions(true);
        t.printStackTrace();
    }
    //endregion

    private void showFilter() {
        mBinding.mapFilter.getRoot().setVisibility(View.VISIBLE);

        //TODO persist filterOptions and reapply them on marker change
        mBinding.mapFilter.filterSearchButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestPlacesFromAPI();
                    }
                });

        mBinding.mapFilter.filterCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFilterOptions();
                cleanMap();
                mBinding.mapFilter.getRoot().setVisibility(View.GONE);
            }
        });

        mBinding.mapFilter.filterDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int progressWithOffset = progress + MIN_SEARCH_RADIUS;
                mBinding.mapFilter.filterDistanceTextView.setText(progressWithOffset + " m");
                safeRemoveCircle();
                setSearchCircle(progressWithOffset);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void resetFilterOptions() {
        mBinding.mapFilter.filterDistanceSeekBar.setProgress(0);
    }

    private void safeRemoveMarker() {
        if (mCenterMarker != null) {
            mCenterMarker.remove();
        }
    }

    private void safeRemoveCircle() {
        if (mSearchCircle != null) {
            mSearchCircle.remove();
        }
    }

    private void clearSearchedPlaces() {
        if (searchedPlacesMarker != null) {
            for (Marker placeMarker : searchedPlacesMarker) {
                placeMarker.remove();
            }
            searchedPlacesMarker.clear();
        }

        if (searchedPlaces != null) {
            searchedPlaces.clear();
        }
    }

    private void cleanMap() {
        safeRemoveMarker();
        safeRemoveCircle();
        clearSearchedPlaces();
    }

    private void moveMapToUserLocation(LatLng latLng, boolean shouldUseCallback) {
        GoogleMap.CancelableCallback callback = shouldUseCallback ? MapFragment.this : null;
        if (latLng != null && mGoogleMap != null) {
            mGoogleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, mZoomLevel),
                    ANIMATION_DURATION, callback);
        }
    }

    private boolean hasLocationPermission() {
        Activity activity = getActivity();
        Context context = getContext();

        if (activity == null || context == null)
            return false;

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void requestGPS() {
        final Activity activity = getActivity();
        Context context = getContext();

        if (activity == null || context == null)
            return;

        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(context);

        client.checkLocationSettings(settingsRequest)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ApiException apiException = ((ApiException) e);
                        if (apiException.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                MapFragment.this.startIntentSenderForResult(
                                        resolvable.getResolution().getIntentSender(),
                                        REQUEST_GPS_SETTINGS_CODE, null,
                                        0, 0, 0, null);
                            } catch (IntentSender.SendIntentException sendEx) {
                                // Ignore this error
                            }
                        }
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        if (locationSettingsResponse.getLocationSettingsStates().isLocationPresent() &&
                                locationSettingsResponse.getLocationSettingsStates().isLocationUsable()) {
                            setRequestLocationUpdates();
                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void setRequestLocationUpdates() {
        if (mFusedLocationClient != null && !isRequestLocationUpdatesActive) {
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult result) {
                    Location location = result.getLastLocation();
                    if (location != null) {
                        hasZoomedIn = true;
                        moveMapToUserLocation(
                                new LatLng(location.getLatitude(), location.getLongitude()),
                                false);
                        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                        isRequestLocationUpdatesActive = false;
                    }
                }
            };
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(1000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            isRequestLocationUpdatesActive = true;
            mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
        }
    }

    private void getUserLastKnownLocationWithChecks() {
        if (hasLocationPermission()) {
            requestGPS();
        }
    }

    //TODO improve how this call is made
    //TODO create a class to handle these requests
    private void requestPlacesFromAPI() {
        clearSearchedPlaces();
        canUseFilterActions(false);
        Call<GooglePlacesResponse> placesCall = NetworkUtil.googlePlaceAPI.getPlaces(
                getResources().getString(R.string.google_places_key),
                mClickedLatLng.latitude + "," + mClickedLatLng.longitude,
                Integer.toString(mBinding.mapFilter.filterDistanceSeekBar.getProgress() + MIN_SEARCH_RADIUS),
                mPlacesRequest.getType(),
                isPriceFilteringEnabled ? mPlacesRequest.getPrice() : null);
        placesCall.enqueue(this);
    }

    private void setupFragment() {
        mBinding.mapFilter.filterDistanceTextView.setText(MIN_SEARCH_RADIUS + " m");

        List<Integer> iconResList = new ArrayList<>();
        final List<String> placeDescriptionList = new ArrayList<>();

        for (GooglePlacesRequestParams.PlaceType placeType : GooglePlacesRequestParams.PlaceType.values()) {
            iconResList.add(placeType.getIconResId());
            placeDescriptionList.add(placeType.getDescription());
        }

        IconAndTextAdapter placeIconAndTextAdapter =
                new IconAndTextAdapter(
                        getContext(),
                        R.layout.spinner_icon_and_text,
                        placeDescriptionList,
                        iconResList);

        mBinding.mapFilter.filterPlaceSpinner.setAdapter(placeIconAndTextAdapter);
        mBinding.mapFilter.filterPlaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView adapterView, View view, int position, long rowId) {
                mPlacesRequest.setType(GooglePlacesRequestParams.PlaceType.values()[position].getTag());
            }

            @Override
            public void onNothingSelected(AdapterView adapterView) {

            }
        });

        final List<String> priceDescriptionList = new ArrayList<>();

        for (GooglePlacesRequestParams.PlacePrice placePrice : GooglePlacesRequestParams.PlacePrice.values()) {
            priceDescriptionList.add(placePrice.getDescription());
        }

        IconAndTextAdapter priceIconAndTextAdapter =
                new IconAndTextAdapter(
                        getContext(),
                        R.layout.spinner_icon_and_text,
                        priceDescriptionList);

        mBinding.mapFilter.filterPriceRangeSpinner.setAdapter(priceIconAndTextAdapter);
        mBinding.mapFilter.filterPriceRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView adapterView, View view, int position, long rowId) {
                mPlacesRequest.setPrice(GooglePlacesRequestParams.PlacePrice.values()[position].getTag());
            }

            @Override
            public void onNothingSelected(AdapterView adapterView) {

            }
        });

        mBinding.mapFilter.filterPriceRangeCheckBox.setChecked(isPriceFilteringEnabled);
        mBinding.mapFilter.filterPriceRangeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isPriceFilteringEnabled = isChecked;
                mBinding.mapFilter.filterPriceRangeSpinner.setEnabled(isChecked);
                mBinding.mapFilter.filterPriceRangeSpinner.getSelectedView().setEnabled(isChecked);
                mBinding.mapFilter.filterPriceRangeImageView.setColorFilter(isChecked ?
                                getResources().getColor(android.R.color.white) :
                                getResources().getColor(android.R.color.darker_gray),
                        PorterDuff.Mode.SRC_ATOP);
            }
        });

    }

    private void setSearchCircle(int progress) {
        mSearchCircle = mGoogleMap.addCircle(new CircleOptions()
                .center(mCenterMarker.getPosition())
                .radius(progress)
                .strokeColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                .fillColor(Color.TRANSPARENT));
    }

    private void canUseFilterActions(boolean enabled) {
        mBinding.mapFilter.filterSearchButton.setEnabled(enabled);
        mBinding.mapFilter.filterCancelButton.setEnabled(enabled);
    }

}