package sfotakos.anightout.newevent;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import sfotakos.anightout.R;
import sfotakos.anightout.common.data.NightOutContract.EventEntry;
import sfotakos.anightout.databinding.ActivityNewEventBinding;
import sfotakos.anightout.home.HomeActivity;
import sfotakos.anightout.place.PlaceDetailsActivity;

import static sfotakos.anightout.home.HomeActivity.HOME_ACTIVITY_PARENT;
import static sfotakos.anightout.place.PlaceDetailsActivity.PLACE_DETAILS_ACTIVITY_PARENT;

// TODO implement entry validation
public class NewEventActivity extends AppCompatActivity {

    public final static String SAVED_EVENT_ID_EXTRA = "SAVED EVENT ID EXTRA";

    private ActivityNewEventBinding mBinding;

    private final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_new_event);

        setSupportActionBar(mBinding.newEventToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setupEventDateField();
        setupSaveButton();
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return this.getNavigationUpIntent();
    }

    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        return this.getNavigationUpIntent();
    }

    // TODO Add custom view for date and time picking
    // Temporary implementation as seen on https://stackoverflow.com/a/19897981
    private void setupEventDateField() {
        updateEventDateField();

        final DatePickerDialog.OnDateSetListener date = new
                DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateEventDateField();
                    }

                };

        mBinding.newEventDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(NewEventActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateEventDateField() {
        String myFormat = "dd/MM/yyyy HH:mm:ss"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

        mBinding.newEventDateEditText.setText(sdf.format(myCalendar.getTime()));
    }

    private void setupSaveButton() {
        mBinding.newEventSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues contentValues = new ContentValues();

                contentValues.put(EventEntry.EVENT_NAME,
                        mBinding.newEventNameInputEditText.getText().toString());
                contentValues.put(EventEntry.EVENT_DATE,
                        mBinding.newEventDateEditText.getText().toString());
                contentValues.put(EventEntry.EVENT_DESCRIPTION,
                        mBinding.newEventDescriptionInputEditText.getText().toString());

                Uri uri = getContentResolver()
                        .insert(EventEntry.CONTENT_URI, contentValues);

                if (uri != null) {

                    returnToCallerWithCreatedEventId(ContentUris.parseId(uri));
                } else {
                    Toast.makeText(
                            v.getContext(),
                            "There was a problem saving the event, please try again",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private Intent getNavigationUpIntent() {
        Intent navigationIntent = null;

        String action = getIntent().getAction();
        if (action != null) {
            switch (action) {
                case HOME_ACTIVITY_PARENT:
                    navigationIntent = new Intent(this, HomeActivity.class);
                    navigationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    break;
                case PLACE_DETAILS_ACTIVITY_PARENT:
                    navigationIntent = new Intent(this, PlaceDetailsActivity.class);
                    navigationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    break;
                default:
                    throw new InvalidParameterException("Unknown parent activity");
            }
        }
        return navigationIntent;
    }

    private void returnToCallerWithCreatedEventId(long eventId) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(SAVED_EVENT_ID_EXTRA, eventId);
        setResult(Activity.RESULT_OK, returnIntent);

        onNavigateUp();
    }
}
