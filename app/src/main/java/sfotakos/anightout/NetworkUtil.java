package sfotakos.anightout;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkUtil {

    // TODO resource for baseURL
    public static GooglePlaceAPI googlePlaceAPI = new Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/place/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(GooglePlaceAPI.class);

}
