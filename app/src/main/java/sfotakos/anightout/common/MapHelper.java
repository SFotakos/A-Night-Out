package sfotakos.anightout.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
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
import com.google.maps.android.ui.IconGenerator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import sfotakos.anightout.R;
import sfotakos.anightout.common.google_maps_places_photos_api.GooglePlacesPlaceResponse;
import sfotakos.anightout.common.google_maps_places_photos_api.GooglePlacesRequest;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;

public class MapHelper implements GoogleMap.CancelableCallback, Callback<GooglePlacesPlaceResponse> {

    private static int ANIMATION_DURATION = 600;

    private Context mContext;
    private IMapHelper mMapHelperListener;
    private MapState mapState = new MapState();

    private PlaceClusterManager<PlaceClusterItem> mClusterManager;
    private LocationCallback mLocationCallback;
    private GoogleMap mGoogleMap;
    private Marker mCenterMarker;
    private Circle mSearchCircle;

    //This ensures only one listener is set to receive location update at a time
    private boolean isRequestLocationUpdatesActive = false;

    public MapHelper(Context mContext, IMapHelper mMapHelperListener) {
        this.mContext = mContext;
        this.mMapHelperListener = mMapHelperListener;
    }

    @Override
    public void onFinish() {
        setCenterMaker();
        mMapHelperListener.onCameraAnimationFinished();
    }

    @Override
    public void onCancel() {
        mMapHelperListener.onCameraAnimationCancelled();
    }

    @Override
    public void onResponse(@NonNull Call<GooglePlacesPlaceResponse> call,
                           @NonNull Response<GooglePlacesPlaceResponse> response) {
        mMapHelperListener.onPlacesRequestSuccessful();
        mapState.setCurrentPlacesResponse(response.body());
        addPlacesToMap();
    }

    private void addPlacesToMap() {
        if (mapState.getCurrentPlacesResponse() != null) {
            List<Place> places = mapState.getCurrentPlacesResponse().getResults();
            for (final Place place : places) {
                LatLng placeLatLng = new LatLng(place.getGeometry().getLocation().getLat(),
                        place.getGeometry().getLocation().getLng());

                MarkerOptions placeMarketOptions = new MarkerOptions()
                        .position(placeLatLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(
                                new IconGenerator(mContext).makeIcon(place.getName())))
                        .draggable(false);

                mClusterManager.addItem(new PlaceClusterItem(placeLatLng, place, placeMarketOptions));
            }
        }
    }

    @Override
    public void onFailure(@NonNull Call<GooglePlacesPlaceResponse> call, @NonNull Throwable t) {
        mMapHelperListener.onPlacesRequestFailure();
        t.printStackTrace();
    }

    private void setCenterMaker() {
        Bitmap markerBmp = DrawableUtils.getBitmapFromVectorDrawable(mContext, R.drawable.ic_pin);
        Bitmap tintedMarkerBitmap = DrawableUtils.tintImage(markerBmp,
                ContextCompat.getColor(mContext, R.color.colorPrimary));
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(tintedMarkerBitmap);

        if (mapState.getClickedLatLng() != null) {
            mCenterMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(mapState.getClickedLatLng())
                    .icon(bitmapDescriptor)
                    .draggable(false));
        }
    }

    public void setSearchCircle(int progress) {
        safeRemoveCircle();
        if (mCenterMarker != null) {
            mSearchCircle = mGoogleMap.addCircle(new CircleOptions()
                    .center(mCenterMarker.getPosition())
                    .radius(progress)
                    .strokeColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                    .fillColor(Color.TRANSPARENT));
        }
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

    private void clearSearchedPlaces() {
        mClusterManager.clearItems();
        mapState.setCurrentPlacesResponse(null);
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
                        mapState.setHasZoomedIn(true);
                        isRequestLocationUpdatesActive = false;
                        fusedLocationClient.removeLocationUpdates(mLocationCallback);
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
                    CameraUpdateFactory.newLatLngZoom(latLng, mapState.getZoomLevel()),
                    ANIMATION_DURATION, callback);
        }
        mMapHelperListener.showMapTutorial();
    }

    public void requestPlaces(Resources resources, MapState mapState) {
        clearSearchedPlaces();
        GooglePlacesRequest.requestPlacesFromAPI(resources, mapState.getClickedLatLng(),
                mapState.getSearchRadius(),
                mapState.getType(),
                mapState.isPriceSearchingEnabled() ? mapState.getPrice() : null,
                this);
    }

    public void restoreMapState(MapState mapState) {
        this.mapState = mapState;
    }

    public MapState getMapState() {
        return mapState;
    }

    public void setGoogleMap(GoogleMap googleMap,
                             PlaceClusterManager.IPlaceClusterManager placeClusterManager) {
        if (mGoogleMap != null){
            mGoogleMap.clear();
        }
        this.mGoogleMap = googleMap;
        mGoogleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(mContext, R.raw.maps_style_json));

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                cleanMap();

                mapState.setCenterMarkerLatLng(latLng);
                moveMapToUserLocation(latLng, true);
            }
        });

        mClusterManager = new PlaceClusterManager<>(mContext, mGoogleMap, placeClusterManager);
        mClusterManager.setRenderer(new PlaceClusterRenderer(mContext, mGoogleMap, mClusterManager));
        mGoogleMap.setOnCameraIdleListener(mClusterManager);
        mGoogleMap.setOnMarkerClickListener(mClusterManager);

        setCenterMaker();
        setSearchCircle(Integer.valueOf(mapState.getSearchRadius()));
        addPlacesToMap();
    }

    public boolean isMapReady() {
        return mGoogleMap != null;
    }

    public interface IMapHelper {
        void onPlacesRequestSuccessful();

        void onPlacesRequestFailure();

        void onCameraAnimationFinished();

        void onCameraAnimationCancelled();

        void showMapTutorial();
    }
}
