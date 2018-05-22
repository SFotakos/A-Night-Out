package sfotakos.anightout.eventdetails;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;

import sfotakos.anightout.Event;
import sfotakos.anightout.R;

public class PlacePhotosRvAdapter extends RecyclerView.Adapter<PlacePhotosRvAdapter.PhotosViewHolder> {

    private List<Uri> placePhotos;

    public PlacePhotosRvAdapter(List<Uri> placePhotos) {
        this.placePhotos = placePhotos;
    }

    @NonNull
    @Override
    public PhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_place_photo, parent, false);
        return new PhotosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotosViewHolder holder, int position) {
        Uri uri = placePhotos.get(position);

        Picasso.get().load(uri)
                .placeholder(android.R.drawable.gallery_thumb)
                .into(holder.mPlacePhoto);
    }

    @Override
    public int getItemCount() {
        return placePhotos.size();
    }

    class PhotosViewHolder extends RecyclerView.ViewHolder {
        ImageView mPlacePhoto;

        PhotosViewHolder(View itemView) {
            super(itemView);
            mPlacePhoto = itemView.findViewById(R.id.itemPlace_photo_imageView);
        }
    }
}
