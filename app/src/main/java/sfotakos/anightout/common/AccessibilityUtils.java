package sfotakos.anightout.common;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.view.accessibility.AccessibilityManager;

import java.text.DateFormat;
import java.util.Calendar;

public class AccessibilityUtils {

    public static boolean isAccessibilityEnabled(Context context, int accessibilityType) {
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager != null) {
            Iterable<AccessibilityServiceInfo> serviceInfos = accessibilityManager
                    .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN);
            for (AccessibilityServiceInfo serviceInfo : serviceInfos) {
                if ((serviceInfo.feedbackType & accessibilityType) == accessibilityType) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getAccessibilityDateFromCalendar(Calendar calendar) {
        return calendar != null ? DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.SHORT).format(calendar.getTime()) : "";
    }
}
