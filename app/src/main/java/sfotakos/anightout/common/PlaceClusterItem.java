package sfotakos.anightout.common;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;

import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;

public class PlaceClusterItem implements ClusterItem, Serializable {

    private LatLng mPosition;
    private MarkerOptions mMarkerOptions;
    private Place mPlace;

    public PlaceClusterItem(LatLng mPosition, Place mPlace, MarkerOptions mMarkerOptions) {
        this.mPosition = mPosition;
        this.mPlace = mPlace;
        this.mMarkerOptions = mMarkerOptions;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public MarkerOptions getMarkerOptions() {
        return mMarkerOptions;
    }

    public Place getPlace() {
        return mPlace;
    }
}
