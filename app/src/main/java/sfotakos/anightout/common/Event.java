package sfotakos.anightout.common;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sfotakos.anightout.common.data.NightOutContract.EventEntry;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Photo;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;

public class Event implements Serializable {

    private Integer eventId;
    private String eventName;
    private String eventDate;
    private String eventTime;
    private String eventDescription;

    private Place place;

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

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

    public static List<Event> queryEvents(@NonNull ContentResolver contentResolver) {
        List<Event> eventList = new ArrayList<>();
        Cursor cursor = contentResolver.query(
                EventEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (cursor != null) {
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
            cursor.close();
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
                Event.queryEventById(contentResolver, eventId);

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
}
