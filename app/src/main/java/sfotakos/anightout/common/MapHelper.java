package sfotakos.anightout.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sfotakos.anightout.R;
import sfotakos.anightout.common.google_maps_places_photos_api.GooglePlacesPlaceResponse;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;
import sfotakos.anightout.place.PlaceDetailsActivity;

public class MapHelper implements GoogleMap.CancelableCallback, Callback<GooglePlacesPlaceResponse> {

    private static int ANIMATION_DURATION = 600;
    private static int DEFAULT_ZOOM_LEVEL = 15;

    private Context mContext;
    private IMapHelper mMapHelperListener;

    private GoogleMap mGoogleMap;
    private Marker mCenterMarker;
    private Circle mSearchCircle;
    private LatLng mClickedLatLng;

    private List<Marker> searchedPlacesMarker = new ArrayList<>();
    private HashMap<String, Place> searchedPlaces = new HashMap<>();

    private LocationCallback mLocationCallback;

    private int mZoomLevel = DEFAULT_ZOOM_LEVEL;

    private boolean isRequestLocationUpdatesActive = false; //This ensures only one listener is set to receive location update at a time

    public MapHelper(@NonNull Context context,
                     @NonNull GoogleMap googleMap,
                     @NonNull IMapHelper mapHelperListener) {
        this.mContext = context;
        this.mGoogleMap = googleMap;
        mMapHelperListener = mapHelperListener;

        mGoogleMap = googleMap;
        mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                context, R.raw.maps_style_json));

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
                Place place;
                if (searchedPlaces != null) {
                    place = searchedPlaces.get(marker.getId());
                    if (place != null) {
                        Intent placeDetailsIntent = new Intent(mContext, PlaceDetailsActivity.class);
                        placeDetailsIntent.putExtra(Constants.PLACE_EXTRA, place);
                        mContext.startActivity(placeDetailsIntent);
                    }
                }
                return true;
            }
        });
    }


    public LatLng getClickedLatLng() {
        return this.mClickedLatLng;
    }

    @Override
    public void onFinish() {
        Bitmap markerBitmap = DrawableUtils.getBitmapFromVectorDrawable(
                mContext, R.drawable.ic_pin);
        Bitmap tintedMarkerBitmap = DrawableUtils.tintImage(markerBitmap,
                ContextCompat.getColor(mContext, R.color.colorPrimary));

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(tintedMarkerBitmap);

        mCenterMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(mClickedLatLng)
                .icon(bitmapDescriptor)
                .draggable(false));
        mMapHelperListener.onCameraAnimationFinished();
    }

    @Override
    public void onCancel() {
        mMapHelperListener.onCameraAnimationCancelled();
    }

    @Override
    public void onResponse(@NonNull Call<GooglePlacesPlaceResponse> call, @NonNull Response<GooglePlacesPlaceResponse> response) {
        mMapHelperListener.onPlacesRequestSuccessful();
        GooglePlacesPlaceResponse placesResponse = response.body();
        if (placesResponse != null) {
            List<Place> places = placesResponse.getResults();
            for (final Place place : places) {
                LatLng placeLatLng = new LatLng(place.getGeometry().getLocation().getLat(),
                        place.getGeometry().getLocation().getLng());

                MarkerOptions placeMarketOptions = new MarkerOptions()
                        .position(placeLatLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(
                                DrawableUtils.getBitmapFromVectorDrawable(
                                        mContext, R.drawable.ic_store)))
                        .draggable(false);

                Marker placeMarker = mGoogleMap.addMarker(placeMarketOptions);
                searchedPlacesMarker.add(placeMarker);
                searchedPlaces.put(placeMarker.getId(), place);
            }
        }
    }

    @Override
    public void onFailure(@NonNull Call<GooglePlacesPlaceResponse> call, @NonNull Throwable t) {
        mMapHelperListener.onPlacesRequestFailure();
        t.printStackTrace();
    }

    public void setSearchCircle(int progress) {
        safeRemoveCircle();
        mSearchCircle = mGoogleMap.addCircle(new CircleOptions()
                .center(mCenterMarker.getPosition())
                .radius(progress)
                .strokeColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                .fillColor(Color.TRANSPARENT));
    }

    public void cleanMap() {
        safeRemoveMarker();
        safeRemoveCircle();
        clearSearchedPlaces();
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

    public void clearSearchedPlaces() {
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

    @SuppressLint("MissingPermission")
    public void requestLocationUpdates(@NonNull final FusedLocationProviderClient fusedLocationClient) {
        if (!isRequestLocationUpdatesActive) {
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult result) {
                    Location location = result.getLastLocation();
                    if (location != null) {
                        moveMapToUserLocation(
                                new LatLng(location.getLatitude(), location.getLongitude()),
                                false);
                        mMapHelperListener.onLocationResult();
                        fusedLocationClient.removeLocationUpdates(mLocationCallback);
                        isRequestLocationUpdatesActive = false;
                    }
                }
            };
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(1000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            isRequestLocationUpdatesActive = true;
            fusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
        }
    }

    private void moveMapToUserLocation(LatLng latLng, boolean shouldUseCallback) {
        GoogleMap.CancelableCallback callback = shouldUseCallback ? this : null;
        if (latLng != null && mGoogleMap != null) {
            mGoogleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(latLng, mZoomLevel),
                    ANIMATION_DURATION, callback);
        }
        mMapHelperListener.showMapTutorial();
    }

    public interface IMapHelper{
        void onPlacesRequestSuccessful();
        void onPlacesRequestFailure();

        void onCameraAnimationFinished();
        void onCameraAnimationCancelled();

        void showMapTutorial();

        void onLocationResult();
    }
}
