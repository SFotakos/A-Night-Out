package sfotakos.anightout.newevent;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.util.Calendar;

import sfotakos.anightout.R;
import sfotakos.anightout.common.Constants;
import sfotakos.anightout.common.Event;
import sfotakos.anightout.databinding.ActivityNewEventBinding;
import sfotakos.anightout.home.HomeActivity;
import sfotakos.anightout.place.PlaceDetailsActivity;

import static sfotakos.anightout.common.Constants.HOME_ACTIVITY_PARENT;
import static sfotakos.anightout.common.Constants.PLACE_DETAILS_ACTIVITY_PARENT;

// TODO implement entry validation
public class NewEventActivity extends AppCompatActivity {

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

        setupActivity();
    }

    private void setupActivity() {
        updateEventDateTimeFields();
        setupEventDateField();
        setupEventTimeField();
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

    private void setupEventTimeField() {
        mBinding.newEventTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(NewEventActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                myCalendar.set(Calendar.HOUR_OF_DAY, hour);
                                myCalendar.set(Calendar.MINUTE, minute);
                                updateEventDateTimeFields();
                            }
                        }, myCalendar.get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE),
                        true).show();
            }
        });
    }

    private void setupEventDateField() {
        mBinding.newEventDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(NewEventActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                myCalendar.set(Calendar.YEAR, year);
                                myCalendar.set(Calendar.MONTH, month);
                                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                updateEventDateTimeFields();
                            }
                        }, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateEventDateTimeFields() {
        mBinding.newEventDateEditText.setText(
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(myCalendar.getTime()));
        mBinding.newEventTimeEditText.setText(
                DateFormat.getTimeInstance(DateFormat.SHORT).format(myCalendar.getTime()));
    }

    private void setupSaveButton() {
        mBinding.newEventSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserInputValid()) {
                    Event newEvent = new Event();
                    newEvent.setEventName(mBinding.newEventNameInputEditText.getText().toString());
                    newEvent.setEventDate(mBinding.newEventDateEditText.getText().toString());
                    newEvent.setEventTime(mBinding.newEventTimeEditText.getText().toString());
                    newEvent.setEventDescription(
                            mBinding.newEventDescriptionInputEditText.getText().toString());

                    Uri insertedUri = Event.insertEvent(getContentResolver(), newEvent);
                    if (insertedUri != null) {
                        returnToCallerWithCreatedEventId(ContentUris.parseId(insertedUri));
                    } else {
                        // TODO treat this better or at least put the string into strings.xml
                        Toast.makeText(
                                v.getContext(),
                                "There was a problem saving the event, please try again",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private boolean isUserInputValid() {
        if (mBinding.newEventNameInputEditText.getText().length() < Constants.EVENT_NAME_MIN_LENGTH) {
            mBinding.newEventNameInputLayout.setErrorEnabled(true);
            mBinding.newEventNameInputLayout.setError(
                    getString(R.string.newEvent_eventName_invalidInput, Constants.EVENT_NAME_MIN_LENGTH));
            return false;
        } else {
            mBinding.newEventNameInputLayout.setErrorEnabled(false);
            mBinding.newEventNameInputLayout.setError(null);
            return true;
        }
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
        returnIntent.putExtra(Constants.SAVED_EVENT_ID_EXTRA, eventId);
        setResult(Activity.RESULT_OK, returnIntent);

        onNavigateUp();
    }
}
