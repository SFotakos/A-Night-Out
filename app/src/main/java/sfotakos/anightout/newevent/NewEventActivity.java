package sfotakos.anightout.newevent;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.Serializable;
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

    private Calendar eventCalendar = Calendar.getInstance();
    private Calendar timePickerCalendar = Calendar.getInstance();
    private Calendar datePickerCalendar = Calendar.getInstance();

    private AlertDialog timePickerDialog;
    private DatePickerDialog datePickerDialog;

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(Constants.STATE_CALENDAR, eventCalendar);
        outState.putSerializable(Constants.STATE_TIME_PICKER_CALENDAR, timePickerCalendar);

        if (datePickerDialog != null && datePickerDialog.isShowing()) {
            DatePicker datePicker = datePickerDialog.getDatePicker();
            datePickerCalendar.set(Calendar.YEAR, datePicker.getYear());
            datePickerCalendar.set(Calendar.MONTH, datePicker.getMonth());
            datePickerCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
            outState.putSerializable(Constants.STATE_DATE_PICKER_CALENDAR, datePickerCalendar);
        }

        outState.putBoolean(Constants.STATE_DATE_PICKER_DIALOG,
                datePickerDialog != null && datePickerDialog.isShowing());
        outState.putBoolean(Constants.STATE_TIME_PICKER_DIALOG,
                timePickerDialog != null && timePickerDialog.isShowing());

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            eventCalendar = getCalendarFromSerializable(
                    savedInstanceState.getSerializable(Constants.STATE_CALENDAR));
            timePickerCalendar = getCalendarFromSerializable(
                    savedInstanceState.getSerializable(Constants.STATE_TIME_PICKER_CALENDAR));
            datePickerCalendar = getCalendarFromSerializable(
                    savedInstanceState.getSerializable(Constants.STATE_DATE_PICKER_CALENDAR));

            if (savedInstanceState.getBoolean(Constants.STATE_DATE_PICKER_DIALOG, false)) {
                showDatePicker();
            } else if (savedInstanceState.getBoolean(Constants.STATE_TIME_PICKER_DIALOG, false)) {
                showTimePicker();
            }
        }
    }

    private Calendar getCalendarFromSerializable(Serializable serializable) {
        return (serializable != null && serializable instanceof Calendar) ?
                (Calendar) serializable : Calendar.getInstance();
    }

    private void setupActivity() {
        updateEventDateTimeFields();
        setupEventDateField();
        setupEventTimeField();
        setupSaveButton();
    }

    private void setupEventTimeField() {
        mBinding.newEventTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });
    }

    private void setupEventDateField() {
        mBinding.newEventDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    private void updateEventDateTimeFields() {
        mBinding.newEventDateEditText.setText(
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(eventCalendar.getTime()));
        mBinding.newEventTimeEditText.setText(
                DateFormat.getTimeInstance(DateFormat.SHORT).format(eventCalendar.getTime()));
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
                        Toast.makeText(
                                v.getContext(),
                                R.string.newEvent_saving_error,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    // Sadly TimePickerDialog shows a blank screen on landscape in some devices so this is a workaround
    private void showTimePicker() {
        final TimePicker timePicker = new TimePicker(this);
        timePicker.setCurrentHour(timePickerCalendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(timePickerCalendar.get(Calendar.MINUTE));
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                timePickerCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                timePickerCalendar.set(Calendar.MINUTE, minute);
            }
        });

        timePickerDialog = new AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        eventCalendar.set(Calendar.HOUR_OF_DAY, timePickerCalendar.get(Calendar.HOUR_OF_DAY));
                        eventCalendar.set(Calendar.MINUTE, timePickerCalendar.get(Calendar.MINUTE));
                        updateEventDateTimeFields();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetCalendarToEventCalendar(timePickerCalendar);
                        dialog.dismiss();
                    }
                })
                .setView(timePicker).show();
    }

    private void showDatePicker() {
        datePickerDialog = new DatePickerDialog(NewEventActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        eventCalendar.set(Calendar.YEAR, year);
                        eventCalendar.set(Calendar.MONTH, month);
                        eventCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateEventDateTimeFields();
                    }
                }, datePickerCalendar.get(Calendar.YEAR), datePickerCalendar.get(Calendar.MONTH),
                datePickerCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                resetCalendarToEventCalendar(datePickerCalendar);
            }
        });
        datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                resetCalendarToEventCalendar(datePickerCalendar);
            }
        });
        datePickerDialog.show();
    }

    private void resetCalendarToEventCalendar(Calendar calendar) {
        calendar.set(Calendar.YEAR, eventCalendar.get(Calendar.YEAR));
        calendar.set(Calendar.MONTH, eventCalendar.get(Calendar.MONTH));
        calendar.set(Calendar.DAY_OF_MONTH, eventCalendar.get(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, eventCalendar.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, eventCalendar.get(Calendar.MINUTE));
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
