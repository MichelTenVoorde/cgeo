package cgeo.calendar;

import cgeo.geocaching.models.Geocache;
import cgeo.geocaching.R;
import cgeo.geocaching.location.GeopointFormatter;
import cgeo.geocaching.network.Parameters;
import cgeo.geocaching.ui.dialog.Dialogs;
import cgeo.geocaching.utils.ProcessUtils;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;

import java.util.Date;

public class CalendarAddon {

    private CalendarAddon() {
        // utility class
    }

    public static boolean isAvailable() {
        return ProcessUtils.isIntentAvailable(ICalendar.INTENT, Uri.parse(ICalendar.URI_SCHEME + "://" + ICalendar.URI_HOST));
    }

    public static void addToCalendarWithIntent(@NonNull final Activity activity, @NonNull final Geocache cache) {
        final Resources res = activity.getResources();
        if (isAvailable()) {
            final Date hiddenDate = cache.getHiddenDate();
            if (cache.isPastEvent()) {
                // Event is in the past, only add to calendar after confirmation
                Dialogs.confirmYesNo(activity, R.string.helper_calendar_pastevent_title, res.getString(R.string.helper_calendar_pastevent_question),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {
                        addToCalendarWithIntentInternal(activity, cache, hiddenDate);
                    }
                });
            } else {
                addToCalendarWithIntentInternal(activity, cache, hiddenDate);
            }
        } else {
            // Inform user the calendar add-on is not installed and let them get it from Google Play
            Dialogs.confirmYesNo(activity, R.string.addon_missing_title, new StringBuilder(res.getString(R.string.helper_calendar_missing))
                    .append(' ')
                    .append(res.getString(R.string.addon_download_prompt))
                    .toString(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int id) {
                    ProcessUtils.openMarket(activity, "cgeo.calendar");
                }
            });
        }
    }

    private static void addToCalendarWithIntentInternal(@NonNull final Activity activity, @NonNull final Geocache cache, final Date hiddenDate) {
        final String startTime = cache.guessEventTimeMinutes() >= 0 ? String.valueOf(cache.guessEventTimeMinutes()) : StringUtils.EMPTY;
        final Parameters params = new Parameters(
                ICalendar.PARAM_NAME, cache.getName(),
                ICalendar.PARAM_NOTE, StringUtils.defaultString(cache.getPersonalNote()),
                ICalendar.PARAM_HIDDEN_DATE, hiddenDate != null ? String.valueOf(hiddenDate.getTime()) : StringUtils.EMPTY,
                ICalendar.PARAM_URL, StringUtils.defaultString(cache.getUrl()),
                ICalendar.PARAM_COORDS, cache.getCoords() == null ? "" : cache.getCoords().format(GeopointFormatter.Format.LAT_LON_DECMINUTE_RAW),
                ICalendar.PARAM_LOCATION, StringUtils.defaultString(cache.getLocation()),
                ICalendar.PARAM_SHORT_DESC, StringUtils.defaultString(cache.getShortDescription()),
                ICalendar.PARAM_START_TIME_MINUTES, startTime
                );

        activity.startActivity(new Intent(ICalendar.INTENT,
                Uri.parse(ICalendar.URI_SCHEME + "://" + ICalendar.URI_HOST + "?" + params.toString())));
    }

}
