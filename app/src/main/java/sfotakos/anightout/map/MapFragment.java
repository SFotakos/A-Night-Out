package sfotakos.anightout.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import sfotakos.anightout.R;
import sfotakos.anightout.databinding.FragmentMapBinding;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
// TODO finish filter layout
// TODO persist state after rotation
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.CancelableCallback {

    public final static int LOCATION_PERMISSION_REQUEST_CODE = 12045;
    public final static int REQUEST_GPS_SETTINGS_CODE = 45012;

    private static int ANIMATION_DURATION = 600;
    private static int DEFAULT_ZOOM_LEVEL = 15;

    private FragmentMapBinding mBinding;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    private GoogleMap mGoogleMap;
    private Marker mCenterMarker;
    private Circle mSearchCircle;
    private LatLng mClickedLatLng;

    private int mZoomLevel = DEFAULT_ZOOM_LEVEL;

    private boolean hasZoomedIn = false;
    private boolean isRequestLocationUpdatesActive = false;

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
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        if (getUserVisibleHint()) {
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !hasZoomedIn) {
            getUserLastKnownLocationWithChecks();
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

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                cleanMap();

                mClickedLatLng = latLng;
                moveMapToUserLocation(latLng, true);
            }
        });
    }

    @Override
    public void onFinish() {
        mCenterMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(mClickedLatLng)
                .draggable(false));

        showFilter();
    }

    @Override
    public void onCancel() {
    }

    private void showFilter() {
        mBinding.mapFilter.getRoot().setVisibility(View.VISIBLE);

        //TODO persist filterOptions and reapply them on marker change
        mBinding.mapFilter.filterSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Search", Toast.LENGTH_SHORT).show();
            }
        });

        mBinding.mapFilter.filterCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.mapFilter.getRoot().setVisibility(View.GONE);
                cleanMap();
                resetFilterOptions();
            }
        });

        mBinding.mapFilter.filterDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("MyMapFragment", "Progress: " + progress);
                safeRemoveCircle();
                mSearchCircle = mGoogleMap.addCircle(new CircleOptions()
                        .center(mCenterMarker.getPosition())
                        .radius(progress)
                        .strokeColor(Color.DKGRAY)
                        .fillColor(Color.TRANSPARENT));
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

    private void cleanMap() {
        safeRemoveMarker();
        safeRemoveCircle();
    }

    private void moveMapToUserLocation(LatLng latLng, boolean shouldUseCallback) {
        GoogleMap.CancelableCallback callback = shouldUseCallback ? MapFragment.this : null;
        if (latLng != null) {
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
                }).addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
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
}