package sfotakos.anightout.common.data;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import java.util.ArrayList;
import java.util.List;

import sfotakos.anightout.common.Event;
import sfotakos.anightout.common.data.NightOutContract.EventEntry;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Photo;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;

import static sfotakos.anightout.common.Constants.LOADER_EVENTS;

public class LocalRepository implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context mContext;
    private ILocalRepositoryCallback listener;

    private static Uri getEventByIdUri(@NonNull String eventId) {
        return EventEntry.CONTENT_URI.buildUpon()
                .appendPath(eventId).build();
    }

    private static Cursor queryEventById(@NonNull ContentResolver contentResolver,
                                         @NonNull String eventId) {
        return contentResolver.query(
                getEventByIdUri(eventId),
                null,
                null,
                null,
                null);
    }

    private List<Event> returnEvents(@NonNull Cursor cursor) {
        cursor.moveToFirst();
        List<Event> eventList = new ArrayList<>();
        while (cursor.moveToNext()) {
            Event event = new Event();
            int eventIdIndex = cursor.getColumnIndex(EventEntry.EVENT_ID);
            int eventNameIndex = cursor.getColumnIndex(EventEntry.EVENT_NAME);
            int eventDateIndex = cursor.getColumnIndex(EventEntry.EVENT_DATE);
            int eventTimeIndex = cursor.getColumnIndex(EventEntry.EVENT_TIME);
            int eventDescriptionIndex = cursor.getColumnIndex(EventEntry.EVENT_DESCRIPTION);

            int placeIdIndex = cursor.getColumnIndex(EventEntry.PLACE_ID);
            int placeNameIndex = cursor.getColumnIndex(EventEntry.PLACE_NAME);
            int placePhotoRefIndex = cursor.getColumnIndex(EventEntry.PLACE_PHOTO_REF);
            int placePriceRangeIndex = cursor.getColumnIndex(EventEntry.PLACE_PRICE_RANGE);
            int placeAddressIndex = cursor.getColumnIndex(EventEntry.PLACE_ADDRESS);

            event.setEventId((cursor.getInt(eventIdIndex)));
            event.setEventName(cursor.getString(eventNameIndex));
            event.setEventDate(cursor.getString(eventDateIndex));
            event.setEventTime(cursor.getString(eventTimeIndex));
            event.setEventDescription(cursor.getString(eventDescriptionIndex));

            Place place = new Place();
            place.setId(cursor.getString(placeIdIndex));
            place.setName(cursor.getString(placeNameIndex));
            place.setPriceLevel(cursor.getInt(placePriceRangeIndex));
            place.setVicinity(cursor.getString(placeAddressIndex));

            List<Photo> photoList = new ArrayList<>();
            Photo photo = new Photo();
            photo.setPhotoReference(cursor.getString(placePhotoRefIndex));
            photoList.add(photo);
            place.setPhotos(photoList);

            event.setPlace(place);
            eventList.add(event);
        }
        return eventList;
    }

    public static Uri insertEvent(@NonNull ContentResolver contentResolver,
                                  @NonNull Event event) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(EventEntry.EVENT_NAME, event.getEventName());
        contentValues.put(EventEntry.EVENT_DATE, event.getEventDate());
        contentValues.put(EventEntry.EVENT_TIME, event.getEventTime());
        contentValues.put(EventEntry.EVENT_DESCRIPTION, event.getEventDescription());
        return contentResolver.insert(EventEntry.CONTENT_URI, contentValues);
    }

    public static void deleteEvent(@NonNull ContentResolver contentResolver,
                                   @NonNull String eventId) {
        contentResolver.delete(getEventByIdUri(eventId), null, null);
    }

    public static void updateEventWithPlace(@NonNull ContentResolver contentResolver,
                                            @NonNull String eventId, @NonNull Place place) {
        Cursor eventByIdCursor =
                queryEventById(contentResolver, eventId);

        if ((eventByIdCursor != null && eventByIdCursor.getCount() != 0)) {
            eventByIdCursor.moveToFirst();
            ContentValues contentValues = new ContentValues();

            int eventIdIndex =
                    eventByIdCursor.getColumnIndex(EventEntry.EVENT_ID);
            int eventNameIndex =
                    eventByIdCursor.getColumnIndex(EventEntry.EVENT_NAME);
            int eventDateIndex =
                    eventByIdCursor.getColumnIndex(EventEntry.EVENT_DATE);
            int eventTimeIndex =
                    eventByIdCursor.getColumnIndex(EventEntry.EVENT_TIME);
            int eventDescriptionIndex =
                    eventByIdCursor.getColumnIndex(EventEntry.EVENT_DESCRIPTION);

            contentValues.put(EventEntry.EVENT_ID,
                    eventByIdCursor.getInt(eventIdIndex));
            contentValues.put(EventEntry.EVENT_NAME,
                    eventByIdCursor.getString(eventNameIndex));
            contentValues.put(EventEntry.EVENT_DATE,
                    eventByIdCursor.getString(eventDateIndex));
            contentValues.put(EventEntry.EVENT_TIME,
                    eventByIdCursor.getString(eventTimeIndex));
            contentValues.put(EventEntry.EVENT_DESCRIPTION,
                    eventByIdCursor.getString(eventDescriptionIndex));

            contentValues.put(EventEntry.PLACE_ID,
                    place.getPlaceId());
            contentValues.put(EventEntry.PLACE_NAME,
                    place.getName());
            contentValues.put(EventEntry.PLACE_ADDRESS,
                    place.getVicinity());

            if (place.getPriceLevel() != null) {
                contentValues.put(EventEntry.PLACE_PRICE_RANGE,
                        place.getPriceLevel());
            }

            if (place.getPhotos() != null && place.getPhotos().size() > 0) {
                contentValues.put(EventEntry.PLACE_PHOTO_REF,
                        place.getPhotos().get(0).getPhotoReference());
            }

            contentResolver.update(getEventByIdUri(eventId), contentValues,
                    null, null);

            eventByIdCursor.close();
        }
    }

    public LocalRepository(Context mContext, ILocalRepositoryCallback listener) {
        this.mContext = mContext;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle args) {
        switch (loaderId) {
            case LOADER_EVENTS:
                return new CursorLoader(mContext,
                        EventEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case LOADER_EVENTS:
                listener.onEventListObtained(returnEvents(data));
                break;

        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    public interface ILocalRepositoryCallback {
        void onEventListObtained(List<Event> events);
    }
}
