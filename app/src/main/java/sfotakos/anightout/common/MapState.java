package sfotakos.anightout.common;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

import sfotakos.anightout.common.google_maps_places_photos_api.GooglePlacesPlaceResponse;
import sfotakos.anightout.common.google_maps_places_photos_api.GooglePlacesRequest;

public class MapState implements Serializable {
    public static final int MIN_SEARCH_RADIUS = 100;

    private LatLng centerMarkerLatLng;
    private int mZoomLevel = 15; // Default zoom level
    private boolean hasZoomedIn = false;

    private String type = GooglePlacesRequest.PlaceType.RESTAURANT.getTag();
    private String price = GooglePlacesRequest.PlacePrice.VERYEXPENSIVE.getTag();
    private String searchRadius = Integer.toString(MIN_SEARCH_RADIUS);
    private boolean priceSearchingEnabled = false;
    private boolean isFilterEnabled = false;

    private GooglePlacesPlaceResponse currentPlacesResponse;

    public MapState() {
    }

    public String getType() {
        return type;
    }

    public void setType(String mType) {
        this.type = mType;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String mPrice) {
        this.price = mPrice;
    }

    public String getSearchRadius() {
        return searchRadius;
    }

    public void setSearchRadius(String mSearchRadius) {
        this.searchRadius = mSearchRadius;
    }

    public boolean isPriceSearchingEnabled() {
        return priceSearchingEnabled;
    }

    public void setPriceSearchingEnabled(boolean priceSearchingEnabled) {
        this.priceSearchingEnabled = priceSearchingEnabled;
    }

    public boolean isFilterEnabled() {
        return isFilterEnabled;
    }

    public void setFilterEnabled(boolean filterEnabled) {
        isFilterEnabled = filterEnabled;
    }

    public LatLng getClickedLatLng() {
        return centerMarkerLatLng;
    }

    public void setCenterMarkerLatLng(LatLng centerMarkerLatLng) {
        this.centerMarkerLatLng = centerMarkerLatLng;
    }

    public int getZoomLevel() {
        return mZoomLevel;
    }

    public void setZoomLevel(int mZoomLevel) {
        this.mZoomLevel = mZoomLevel;
    }

    public boolean hasZoomedIn() {
        return hasZoomedIn;
    }

    public void setHasZoomedIn(boolean hasZoomedIn) {
        this.hasZoomedIn = hasZoomedIn;
    }

    public GooglePlacesPlaceResponse getCurrentPlacesResponse() {
        return currentPlacesResponse;
    }

    public void setCurrentPlacesResponse(GooglePlacesPlaceResponse currentPlacesResponse) {
        this.currentPlacesResponse = currentPlacesResponse;
    }
}
