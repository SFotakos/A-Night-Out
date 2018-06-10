package sfotakos.anightout.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import sfotakos.anightout.R;
import sfotakos.anightout.common.Constants;
import sfotakos.anightout.common.IconAndTextAdapter;
import sfotakos.anightout.common.MapHelper;
import sfotakos.anightout.common.TutorialUtil;
import sfotakos.anightout.common.google_maps_places_photos_api.GooglePlacesRequest;
import sfotakos.anightout.databinding.FragmentMapBinding;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
// TODO finish filter layout
// TODO persist state after rotation
public class MapFragment extends Fragment implements OnMapReadyCallback, MapHelper.IMapHelper {

    private static int MIN_SEARCH_RADIUS = 100; // This is a workaround so SeekBar has a min value

    private FragmentMapBinding mBinding;
    private Context mContext;
    private Activity mActivity;

    private MapHelper mMapHelper;

    private FusedLocationProviderClient mFusedLocationClient;
    private GooglePlacesRequest mPlacesRequest = new GooglePlacesRequest();

    private boolean isPriceFilteringEnabled = true;
    private boolean hasZoomedIn = false;

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

        if (context instanceof Activity) {
            mActivity = (Activity) context;
        } else {
            mActivity = getActivity();
        }
        mContext = context;
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

    //region GPS and Permission callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            // After permission was granted
            case Constants.LOCATION_PERMISSION_REQUEST_CODE: {
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
        // Returns when GPS was turned on after our request
        if (requestCode == Constants.REQUEST_GPS_SETTINGS_CODE) {
            if (resultCode == RESULT_OK) {
                mMapHelper.requestLocationUpdates(mFusedLocationClient);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //endregion

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMapHelper = new MapHelper(mContext, googleMap, this);
    }

    //region IMapHelper callbacks
    @Override
    public void onPlacesRequestSuccessful() {
        canUseFilterActions(true);
    }

    @Override
    public void onPlacesRequestFailure() {
        canUseFilterActions(true);
    }

    @Override
    public void onLocationResult() {
        hasZoomedIn = true;
        mBinding.mapCenterPositionImageButton.setVisibility(View.VISIBLE);
    }
    //endregion

    //region Camera callback
    @Override
    public void onCameraAnimationFinished() {
        int seekbarProgress = 0;
        try {
            seekbarProgress = mBinding.mapFilter.filterDistanceSeekBar.getProgress();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }

        mMapHelper.setSearchCircle(MIN_SEARCH_RADIUS + seekbarProgress);
        showFilter();
        showPriceFilterTutorial();
    }

    @Override
    public void onCameraAnimationCancelled() {
        // Nothing to be done
    }
    //endregion

    //region Filter and places request
    private void showFilter() {
        mBinding.mapFilter.getRoot().setVisibility(View.VISIBLE);

        //TODO persist filterOptions and reapply them on marker change
        mBinding.mapFilter.filterSearchButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestPlaces();
                    }
                });

        mBinding.mapFilter.filterCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFilterOptions();
                mMapHelper.cleanMap();
                mBinding.mapFilter.getRoot().setVisibility(View.GONE);
            }
        });

        mBinding.mapFilter.filterDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int progressWithOffset = progress + MIN_SEARCH_RADIUS;
                mBinding.mapFilter.filterDistanceTextView.setText(
                        getResources().getString(R.string.any_distanceWithMeters, progressWithOffset));
                mMapHelper.setSearchCircle(progressWithOffset);
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

    private void canUseFilterActions(boolean enabled) {
        mBinding.mapFilter.filterSearchButton.setEnabled(enabled);
        mBinding.mapFilter.filterCancelButton.setEnabled(enabled);
    }

    private void requestPlaces() {
        canUseFilterActions(false);
        mMapHelper.clearSearchedPlaces();
        GooglePlacesRequest.requestPlacesFromAPI(getResources(), mMapHelper.getClickedLatLng(),
                Integer.toString(mBinding.mapFilter.filterDistanceSeekBar.getProgress() + MIN_SEARCH_RADIUS),
                mPlacesRequest.getType(),
                isPriceFilteringEnabled ? mPlacesRequest.getPrice() : null, mMapHelper);
    }
    //endregion

    private void getUserLastKnownLocationWithChecks() {
        if (hasLocationPermission()) {
            requestGPS();
        }
    }

    public void onLocationAndGpsSuccess() {
        mMapHelper.requestLocationUpdates(mFusedLocationClient);
    }

    //region Fragment interaction setup
    private void setupFragment() {
        mBinding.mapFilter.filterDistanceTextView.setText(
                getResources().getString(R.string.any_distanceWithMeters, MIN_SEARCH_RADIUS));

        List<Integer> iconResList = new ArrayList<>();
        final List<String> placeDescriptionList = new ArrayList<>();

        for (GooglePlacesRequest.PlaceType placeType : GooglePlacesRequest.PlaceType.values()) {
            iconResList.add(placeType.getIconResId());
            placeDescriptionList.add(placeType.getDescription());
        }

        IconAndTextAdapter placeIconAndTextAdapter =
                new IconAndTextAdapter(
                        mContext,
                        R.layout.spinner_icon_and_text,
                        placeDescriptionList,
                        iconResList);

        mBinding.mapFilter.filterPlaceSpinner.setAdapter(placeIconAndTextAdapter);
        mBinding.mapFilter.filterPlaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView adapterView, View view, int position, long rowId) {
                mPlacesRequest.setType(GooglePlacesRequest.PlaceType.values()[position].getTag());
            }

            @Override
            public void onNothingSelected(AdapterView adapterView) {

            }
        });

        final List<String> priceDescriptionList = new ArrayList<>();

        for (GooglePlacesRequest.PlacePrice placePrice : GooglePlacesRequest.PlacePrice.values()) {
            priceDescriptionList.add(placePrice.getDescription());
        }

        IconAndTextAdapter priceIconAndTextAdapter =
                new IconAndTextAdapter(
                        mContext,
                        R.layout.spinner_icon_and_text,
                        priceDescriptionList);

        mBinding.mapFilter.filterPriceRangeSpinner.setAdapter(priceIconAndTextAdapter);
        mBinding.mapFilter.filterPriceRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView adapterView, View view, int position, long rowId) {
                mPlacesRequest.setPrice(GooglePlacesRequest.PlacePrice.values()[position].getTag());
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

        mBinding.mapCenterPositionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserLastKnownLocationWithChecks();
            }
        });
    }
    //endregion

    //region Tutorials
    @Override
    public void showMapTutorial() {
        TutorialUtil.showDefaultTutorial(
                mActivity,
                mBinding.getRoot().findViewById(R.id.map),
                getString(R.string.mapFragment_marker_tutorial),
                Constants.MAP_TUTORIAL, false);
    }

    private void showPriceFilterTutorial() {
        TutorialUtil.showDefaultTutorial(
                mActivity,
                mBinding.mapFilter.filterPriceRangeCheckBox,
                getString(R.string.mapFilter_priceRange_tutorial),
                Constants.MAP_FILTER_TUTORIAL_PRICE, false);
    }
    //endregion

    //region Location permission and GPS
    // TODO Try to remove this from the fragment
    private boolean hasLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    public void requestGPS() {
        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest settingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(mContext);

        client.checkLocationSettings(settingsRequest)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ApiException apiException = ((ApiException) e);
                        if (apiException.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                startIntentSenderForResult(
                                        resolvable.getResolution().getIntentSender(),
                                        Constants.REQUEST_GPS_SETTINGS_CODE, null,
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
                            onLocationAndGpsSuccess();
                        }
                    }
                });
    }
    //endregion
}