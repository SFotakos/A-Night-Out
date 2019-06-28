package sfotakos.anightout.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import sfotakos.anightout.common.google_maps_places_photos_api.MapHelper;
import sfotakos.anightout.common.google_maps_places_photos_api.MapState;
import sfotakos.anightout.common.google_maps_places_photos_api.PlaceClusterManager;
import sfotakos.anightout.common.TutorialUtil;
import sfotakos.anightout.common.google_maps_places_photos_api.GooglePlacesRequest;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;
import sfotakos.anightout.databinding.FragmentMapBinding;
import sfotakos.anightout.filter.FilterAdapter;
import sfotakos.anightout.home.HomeActivity;
import sfotakos.anightout.place.PlaceDetailsActivity;

import static android.app.Activity.RESULT_OK;

public class MapFragment extends Fragment implements OnMapReadyCallback,
        MapHelper.IMapHelper, PlaceClusterManager.IPlaceClusterManager {

    private FragmentMapBinding mBinding;
    private Context mContext;
    private Activity mActivity;

    private MapHelper mMapHelper;

    private FusedLocationProviderClient mFusedLocationClient;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    //region Lifecycle management
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        SupportMapFragment supportMapFragment =
                ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        supportMapFragment.getMapAsync(this);
        setupFragment();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = mContext instanceof Activity ? (Activity) context : getActivity();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mMapHelper = new MapHelper(getContext(), this);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mMapHelper.isMapReady() && !mMapHelper.getMapState().hasZoomedIn()) {
            getUserLastKnownLocationWithChecks();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapHelper.getMapState().setCenterMarkerLatLng(mMapHelper.getMapState().getClickedLatLng());
        outState.putParcelable(Constants.STATE_MAP, mMapHelper.getMapState());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            Parcelable parcelable = savedInstanceState.getParcelable(Constants.STATE_MAP);
            if (parcelable instanceof MapState) {
                mMapHelper.restoreMapState((MapState) parcelable);
                if (mMapHelper.getMapState().isFilterEnabled()) {
                    showFilter();
                }
            }
        }
    }
    //endregion

    //region GPS and Permission callback
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            // After permission was granted
            case Constants.LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Check if GPS is turned on
                    requestGPS();
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
        } else if (requestCode == Constants.PLACE_ADDED_TO_EVENT_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                if (data.getExtras() != null) {
                    Integer eventId = data.getExtras().getInt(Constants.EVENT_ID_EXTRA);
                    ((HomeActivity) getActivity()).navigateToEvent(eventId);
                }
                // TODO this is absurdly coupled, change this..
//                ((HomeActivity) getActivity()).navigateToTab(Constants.HomeTabs.EVENT_TAB);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //endregion

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMapHelper.setGoogleMap(googleMap, this);
        mBinding.mapCenterPositionImageButton.setVisibility(View.VISIBLE);
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
    //endregion

    //region Camera callback
    @Override
    public void onCameraAnimationFinished() {
        mMapHelper.setSearchCircle(Integer.valueOf(mMapHelper.getMapState().getSearchRadius()));
        showFilter();
    }

    @Override
    public void onCameraAnimationCancelled() { // Nothing to be done
    }
    //endregion

    //region Filter and places request
    private void showFilter() {
        mMapHelper.getMapState().setFilterEnabled(true);
        mBinding.mapFilter.getRoot().setVisibility(View.VISIBLE);
    }

    private void canUseFilterActions(boolean enabled) {
        mBinding.mapFilter.filterSearchButton.setEnabled(enabled);
    }

    private void requestPlaces() {
        canUseFilterActions(false);
        mMapHelper.requestPlaces(getResources(), mMapHelper.getMapState());
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
        setupPlaceFilter();
        setupRangeFilter();
        setupFilterActions();

        mBinding.mapCenterPositionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserLastKnownLocationWithChecks();
            }
        });
    }

    private void setupRangeFilter() {
        mBinding.mapFilter.filterDistanceSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        int progressWithOffset = progress + MapState.MIN_SEARCH_RADIUS;
                        mBinding.mapFilter.filterDistanceTextView.setText(
                                getResources().getString(R.string.any_distanceWithMeters, progressWithOffset));
                        mMapHelper.setSearchCircle(progressWithOffset);
                        mMapHelper.getMapState().setSearchRadius(Integer.toString(progressWithOffset));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
        mBinding.mapFilter.filterDistanceTextView.setText(
                getResources().getString(R.string.any_distanceWithMeters,
                        Integer.valueOf(mMapHelper.getMapState().getSearchRadius())));
    }

    private void setupPlaceFilter() {
        mBinding.mapFilter.filterPlacesRv.setAdapter(new FilterAdapter());
        mBinding.mapFilter.filterPlacesRv.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL));
    }

    private void setupFilterActions() {
        mBinding.mapFilter.filterSearchButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestPlaces();
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

    @Override
    public void placeClicked(Place place) {
        Intent placeDetailsIntent = new Intent(mContext, PlaceDetailsActivity.class);
        placeDetailsIntent.putExtra(Constants.PLACE_EXTRA, place);
        startActivityForResult(placeDetailsIntent, Constants.PLACE_ADDED_TO_EVENT_RESULT_CODE);
    }
    //endregion
}