package sfotakos.anightout.common.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import sfotakos.anightout.common.data.NightOutContract.EventEntry;

public class NightOutProvider extends ContentProvider {

    public static final int CODE_EVENTS = 100;
    public static final int CODE_EVENTS_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private NightOutDbHelper mDbHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = NightOutContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, NightOutContract.PATH_EVENTS, CODE_EVENTS);
        matcher.addURI(authority, NightOutContract.PATH_EVENTS + "/#", CODE_EVENTS_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new NightOutDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case CODE_EVENTS:
                cursor = mDbHelper.getReadableDatabase().query(
                        EventEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_EVENTS_WITH_ID:
                String eventId = uri.getPathSegments().get(1);
                cursor = mDbHelper.getReadableDatabase().query(
                        EventEntry.TABLE_NAME,
                        projection,
                        EventEntry.EVENT_ID + "=?",
                        new String[]{eventId},
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);

        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case CODE_EVENTS:
                long _id = db.insert(EventEntry.TABLE_NAME,
                        null, contentValues);

                if (_id > 0) {
                    returnUri = ContentUris
                            .withAppendedId(EventEntry.CONTENT_URI, _id);
                } else {
                    throw new android.database.SQLException("Failed to insert favorite into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int deletedEvent;
        switch (sUriMatcher.match(uri)){
            case CODE_EVENTS_WITH_ID:
                String eventId = uri.getPathSegments().get(1);
                deletedEvent = db.delete(EventEntry.TABLE_NAME,
                        EventEntry.EVENT_ID + "=?", new String[]{eventId});
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if (deletedEvent != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletedEvent;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int updatedEvent;
        switch (sUriMatcher.match(uri)){
            case CODE_EVENTS_WITH_ID:
                String eventId = uri.getPathSegments().get(1);
                updatedEvent = db.update(EventEntry.TABLE_NAME, contentValues,
                        EventEntry.EVENT_ID + "=?", new String[]{eventId});
                break;

            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        if (updatedEvent != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updatedEvent;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
