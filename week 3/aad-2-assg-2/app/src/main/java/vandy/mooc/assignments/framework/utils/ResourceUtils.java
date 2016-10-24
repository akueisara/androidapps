package vandy.mooc.assignments.framework.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;

/**
 * A utility class that provides resource helper methods.
 */
public final class ResourceUtils {
    /**
     * Ensure this class is only used as a utility.
     */
    private ResourceUtils() {
        throw new AssertionError();
    }

    /**
     * Loads a Drawable from the application resources.
     *
     * @param context A context.
     * @param resId   The drawable resource to load
     * @return A Drawable resource or null if the passed id was not valid.
     */
    public static Drawable getResourceDrawable(
            Context context,
            @DrawableRes int resId) {
        return resId != 0
               ? ContextCompat.getDrawable(context, resId)
               : null;
    }
}
