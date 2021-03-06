package sfotakos.anightout.common;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.view.View;

import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.ShapeType;
import co.mobiwise.materialintro.view.MaterialIntroView;

public class TutorialUtil {

    public static void showDefaultTutorial(@NonNull Activity activity, View view, String infoText,
                                           String usageId, boolean shouldDisplayDot) {
        new MaterialIntroView.Builder(activity)
                .enableDotAnimation(shouldDisplayDot)
                .enableIcon(false)
                .setFocusType(Focus.MINIMUM)
                .setDelayMillis(1000)
                .enableFadeAnimation(true)
                .performClick(false)
                .setInfoText(infoText)
                .setShape(ShapeType.CIRCLE)
                .setTarget(view)
                .setUsageId(usageId)
                .show();
    }

}
