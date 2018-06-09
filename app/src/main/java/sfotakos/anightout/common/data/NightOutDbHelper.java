package sfotakos.anightout.common.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import sfotakos.anightout.common.data.NightOutContract.EventEntry;

public class NightOutDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "nightout.db";

    private static final int DATABASE_VERSION = 3;

    public NightOutDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIE_LIST_TABLE =

                "CREATE TABLE " + EventEntry.TABLE_NAME + " (" +

                        EventEntry.EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        EventEntry.EVENT_NAME + " TEXT, " +
                        EventEntry.EVENT_DATE + " TEXT," +
                        EventEntry.EVENT_TIME + " TEXT," +
                        EventEntry.EVENT_DESCRIPTION + " TEXT, " +

                        EventEntry.PLACE_ID + " TEXT, " +
                        EventEntry.PLACE_NAME + " TEXT, " +
                        EventEntry.PLACE_PHOTO_REF + " TEXT, " +
                        EventEntry.PLACE_PRICE_RANGE + " INTEGER DEFAULT -1, " +
                        EventEntry.PLACE_ADDRESS + " TEXT);";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_LIST_TABLE);
    }

    //TODO do not drop table on update.
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EventEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
