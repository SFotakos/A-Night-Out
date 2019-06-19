package sfotakos.anightout.common.data;

import android.app.IntentService;
import android.content.Intent;
import androidx.annotation.Nullable;

import sfotakos.anightout.common.Constants;
import sfotakos.anightout.common.google_maps_places_photos_api.model.Place;

import static sfotakos.anightout.common.Constants.PLACE_EXTRA;
import static sfotakos.anightout.common.Constants.SAVED_EVENT_ID_EXTRA;

public class UpdatePlaceService extends IntentService {

    public static final String TAG = UpdatePlaceService.class.getSimpleName();

    public UpdatePlaceService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null)
            return;

        String eventId = intent.getStringExtra(SAVED_EVENT_ID_EXTRA);
        Place place = (Place) intent.getSerializableExtra(PLACE_EXTRA);
        if (eventId != null && place != null) {
            boolean hasUpdated = LocalRepository.updateEventWithPlace(getContentResolver(),
                    eventId, place);

            Intent broadcastIntent = new Intent(Constants.PLACE_SERVICE_BROADCAST_ACTION)
                    .putExtra(Constants.PLACE_SERVICE_UPDATE_STATUS, hasUpdated);
            sendBroadcast(broadcastIntent);
        }
    }
}
