package sfotakos.anightout.common.google_maps_places_photos_api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Geometry implements Serializable{

    @SerializedName("location")
    @Expose
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
