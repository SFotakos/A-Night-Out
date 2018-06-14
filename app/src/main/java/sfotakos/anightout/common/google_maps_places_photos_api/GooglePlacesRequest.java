package sfotakos.anightout.common.google_maps_places_photos_api;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.security.InvalidParameterException;

import retrofit2.Call;
import retrofit2.Callback;
import sfotakos.anightout.R;
import sfotakos.anightout.common.NetworkUtil;

public class GooglePlacesRequest {
    public static void requestPlacesFromAPI(@NonNull Resources resources, @NonNull LatLng latLng,
                                            @NonNull String searchRadius, @NonNull String placeType,
                                            @Nullable String priceRange,
                                            @NonNull Callback<GooglePlacesPlaceResponse> callback) {
        Call<GooglePlacesPlaceResponse> placesCall = NetworkUtil.googlePlaceAPI.getPlaces(
                resources.getString(R.string.google_places_key),
                latLng.latitude + "," + latLng.longitude,
                searchRadius,
                placeType,
                priceRange);
        placesCall.enqueue(callback);
    }

    // TODO make description strings.xml resources
    public enum PlaceType {

        RESTAURANT(R.drawable.ic_store, "Restaurant", "restaurant"),
        BAR(R.drawable.ic_bar, "Bar", "bar"),
        CAFE(R.drawable.ic_cafe, "Cafe", "cafe");

        private String description;
        private String tag;
        private int iconResId;

        PlaceType(int iconResId, String description, String tag) {
            this.iconResId = iconResId;
            this.description = description;
            this.tag = tag;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public int getIconResId() {
            return iconResId;
        }
    }

    public enum PlacePrice {

        FREE(R.string.googlePlace_price_free, "0"),
        CHEAP(R.string.googlePlace_price_cheap, "1"),
        MODERATE(R.string.googlePlace_price_moderate, "2"),
        EXPENSIVE(R.string.googlePlace_price_expensive, "3"),
        VERYEXPENSIVE(R.string.googlePlace_price_veryExpensive, "4");

        private int descriptionRes;
        private String tag;

        PlacePrice(int description, String tag) {
            this.descriptionRes = description;
            this.tag = tag;
        }

        public static int getDescriptionByTag(String tag) {
            for (PlacePrice placePrice : PlacePrice.values()) {
                if (placePrice.tag.equals(tag))
                    return placePrice.descriptionRes;
            }
            throw new InvalidParameterException();
        }

        public int getDescription() {
            return descriptionRes;
        }

        public void setDescription(int descriptionRes) {
            this.descriptionRes = descriptionRes;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }
}
