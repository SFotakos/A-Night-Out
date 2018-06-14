package sfotakos.anightout.common;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sfotakos.anightout.common.google_maps_places_photos_api.GooglePlaceAPI;

public class NetworkUtil {

    // TODO resource for baseURL
    public static GooglePlaceAPI googlePlaceAPI = new Retrofit.Builder()
            .baseUrl(Constants.GOOGLE_PLACE_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(GooglePlaceAPI.class);

}
