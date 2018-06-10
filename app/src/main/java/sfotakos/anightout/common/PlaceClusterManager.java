package sfotakos.anightout.common;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.ClusterRenderer;

import java.util.Collection;
import java.util.HashMap;

import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;
import sfotakos.anightout.place.PlaceDetailsActivity;

public class PlaceClusterManager<PlaceClusterItem extends ClusterItem> extends ClusterManager<PlaceClusterItem> {

    private final PlaceClusterRenderer mPlaceClusterRenderer;
    private Context mContext;

    private HashMap<String, Place> searchedPlaces = new HashMap<>();

    public PlaceClusterManager(Context context, GoogleMap map) {
        super(context, map);
        this.mContext = context;

        // TODO change this monstrosity
        this.mPlaceClusterRenderer = new PlaceClusterRenderer(context, map,
                (PlaceClusterManager<sfotakos.anightout.common.PlaceClusterItem>) this);
        this.setRenderer((ClusterRenderer<PlaceClusterItem>) this.mPlaceClusterRenderer);
    }

    @Override
    public void addItems(Collection<PlaceClusterItem> items) {
        super.addItems(items);
        cluster();
    }

    @Override
    public void addItem(PlaceClusterItem myItem) {
        super.addItem(myItem);
        cluster();
    }

    @Override
    public void clearItems() {
        super.clearItems();
        cluster();
        searchedPlaces.clear();
    }

    @Override
    public boolean onMarkerClick(Marker clickedMarker) {
        if (searchedPlaces != null) {
            Place place = searchedPlaces.get(clickedMarker.getId());
            if (place != null) {
                Intent placeDetailsIntent = new Intent(mContext, PlaceDetailsActivity.class);
                placeDetailsIntent.putExtra(Constants.PLACE_EXTRA, place);
                mContext.startActivity(placeDetailsIntent);
            }
        }
        return true;
    }

    public void addToSearchedPlaces(Marker placeMarker, Place place){
        searchedPlaces.put(placeMarker.getId(), place);
    }
}
