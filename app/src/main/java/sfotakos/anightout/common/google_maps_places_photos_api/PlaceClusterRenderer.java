package sfotakos.anightout.common.google_maps_places_photos_api;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class PlaceClusterRenderer extends DefaultClusterRenderer<PlaceClusterItem> {

    private PlaceClusterManager<PlaceClusterItem> mPlaceClusterManager;

    public PlaceClusterRenderer(Context context, GoogleMap map,
                                PlaceClusterManager<PlaceClusterItem> clusterManager) {
        super(context, map, clusterManager);
        this.mPlaceClusterManager = clusterManager;
    }

    @Override
    protected void onBeforeClusterItemRendered(PlaceClusterItem item, MarkerOptions markerOptions) {
        markerOptions.icon(item.getMarkerOptions().getIcon());
        super.onBeforeClusterItemRendered(item, markerOptions);
    }

    @Override
    protected void onClusterItemRendered(PlaceClusterItem placeClusterItem, Marker placeMarker) {
        super.onClusterItemRendered(placeClusterItem, placeMarker);
        mPlaceClusterManager.addToSearchedPlaces(placeMarker, placeClusterItem.getPlace());
    }

    @Override
    protected void onClusterRendered(Cluster<PlaceClusterItem> cluster, Marker marker) {
        super.onClusterRendered(cluster, marker);
    }
}
