package sfotakos.anightout.common.google_maps_places_photos_api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import sfotakos.anightout.common.google_maps_places_photos_api.model.GooglePlacesResponse;

public interface GooglePlaceAPI {

    @GET("nearbysearch/json")
    Call<GooglePlacesResponse> getPlaces(
            @Query("key") String apiKey,
            @Query("location") String location,
            @Query("radius") String radius,
            @Query("type") String type,
            @Query("maxprice") String maxPrice);

}
