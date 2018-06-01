package sfotakos.anightout;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GooglePlaceAPI {

    @GET("nearbysearch/json")
    Call<GooglePlacesResponse> getPlaces(
            @Query("key") String apiKey,
            @Query("location") String location,
            @Query("radius") String radius);

}
