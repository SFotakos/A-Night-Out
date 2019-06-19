package sfotakos.anightout.eventdetails;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import sfotakos.anightout.R;
import sfotakos.anightout.common.Constants;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;

public class PlacePhotosRvAdapter extends RecyclerView.Adapter<PlacePhotosRvAdapter.PhotosViewHolder> {

    private List<Place> places;
    private boolean shouldDisplayName;

    public PlacePhotosRvAdapter(List<Place> placeList, boolean shouldDisplayName) {
        this.places = placeList;
        this.shouldDisplayName = shouldDisplayName;
    }

    @NonNull
    @Override
    public PhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_place, parent, false);
        return new PhotosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotosViewHolder holder, int position) {
        Place place = places.get(position);
        Context context = holder.mPlacePhoto.getContext();

        Uri photoUri;
        if (place.getPhotos() != null &&
                place.getPhotos().size() > 0 &&
                place.getPhotos().get(0).getPhotoReference() != null) {
            // TODO move this to a request class which returns the fully qualified uri
            photoUri = Uri.parse(Constants.GOOGLE_PLACE_API_BASE_URL).buildUpon()
                    .appendPath("photo")
                    .appendQueryParameter("key", context.getString(R.string.google_places_key))
                    .appendQueryParameter("maxheight", "400")
                    .appendQueryParameter("photo_reference", place.getPhotos().get(0).getPhotoReference())
                    .build();
        } else {
            photoUri = Uri.parse("https://picsum.photos/400/400/?image=56").buildUpon().build();
        }
        Picasso.get().load(photoUri)
                .placeholder(android.R.drawable.gallery_thumb)
                .into(holder.mPlacePhoto);

        if (shouldDisplayName) {
            holder.mPlaceName.setText(place.getName());
            holder.mPlaceName.setVisibility(View.VISIBLE);
        } else {
            holder.mPlaceName.setVisibility(View.GONE);
        }

        holder.mPlaceAddress.setText(place.getVicinity());
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    class PhotosViewHolder extends RecyclerView.ViewHolder {
        ImageView mPlacePhoto;
        TextView mPlaceName;
        TextView mPlaceAddress;

        PhotosViewHolder(View itemView) {
            super(itemView);
            mPlacePhoto = itemView.findViewById(R.id.itemPlace_photo_imageView);
            mPlaceName = itemView.findViewById(R.id.itemPlace_name_textView);
            mPlaceAddress = itemView.findViewById(R.id.itemPlace_address_textView);
        }
    }
}
