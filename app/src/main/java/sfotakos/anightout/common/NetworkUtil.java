package sfotakos.anightout.common;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sfotakos.anightout.common.google_maps_places_photos_api.GooglePlaceAPI;

public class NetworkUtil {

    // TODO resource for baseURL
    public static GooglePlaceAPI googlePlaceAPI = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(GooglePlaceAPI.class);

}
