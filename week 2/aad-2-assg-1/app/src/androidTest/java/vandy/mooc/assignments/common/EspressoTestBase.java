package vandy.mooc.assignments.common;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.AnyRes;
import android.support.annotation.StringRes;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Root;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitor;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

//import vandy.mooc.assignments.tasks.TaskManager;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions
        .actionWithAssertions;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers
        .isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * This class can be used as a base class for Espresso test classes.
 * It contains a collection of helper methods and custom ViewMatcher
 * classes that can be used by any MOOC application. This class does
 * not contain any application or MOOC or assignment specific references.
 */
public class EspressoTestBase {

    /**
     * Set a sleep delay of 1 second to give toasts time to be displayed.
     */
    private static final int TOAST_SLEEP_DELAY = 10000;

    /**
     * UIAutomator device initialized and used by allowPermissionsIfNeeded().
     */
    private UiDevice mDevice;

    /*
     * GENERIC HELPER METHODS
     */

    /**
     * Returns a Uri that will map to any application resource.
     *
     * @param resId A resource id
     * @return A Uri that maps to the specified resource
     * @throws Resources.NotFoundException
     */
    protected static Uri getResourcesUri(int resId) {
        Resources resources =
                InstrumentationRegistry.getTargetContext().getResources();

        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                resources.getResourcePackageName(resId) + '/' +
                resources.getResourceTypeName(resId) + '/' +
                resources.getResourceEntryName(resId));
    }

    /**
     * Returns a URL String that will map to any application resource.
     *
     * @param resId A resource id
     * @return A String URL that maps to the specified resource
     * @throws Resources.NotFoundException
     */
    protected static String getResourcesUrl(@AnyRes int resId)
            throws Resources.NotFoundException {
        return getResourcesUri(resId).toString();
    }

    /**
     * Static helper to access a Toast matcher.
     *
     * @return A custom Toast matcher.
     */
    private static Matcher<Root> isToast() {
        return new ToastMatcher();
    }

    /**
     * Returns an action that clears an image view.
     * View constraints: the view must be displayed on screen.
     */
    protected static ViewAction clearImageView() {
        return actionWithAssertions(new ClearImageView());
    }

    /**
     * Helper class to construct and support a Toast matcher which
     * is not supplied or handled by Espresso, but is VERY handy
     * to have.
     * <p/>
     * NOTE: Doesn't work properly when running multiple tests.
     */
    protected static Matcher<Object> isImageTheSame(final Drawable drawable) {
        return new TypeSafeMatcher<Object>(ImageView.class) {

            @Override
            protected boolean matchesSafely(Object item) {
                Bitmap bitmapCompare =
                        Bitmap.createBitmap(
                                drawable.getIntrinsicWidth(),
                                drawable.getIntrinsicHeight(),
                                Bitmap.Config.ARGB_8888);
                Drawable itemDrawable = ((ImageView) item).getDrawable();
                Bitmap bitmap = Bitmap.createBitmap(
                        itemDrawable.getIntrinsicWidth(),
                        itemDrawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
                return bitmapCompare.sameAs(bitmap);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is image the same as: ");
                description.appendValue(drawable);
            }
        };
    }

    /**
     * Helper that returns a resource string.
     *
     * @param id A String resource id.
     * @return A resource String.
     */
    @SuppressWarnings("unused")
    protected String getResourceString(int id) {
        return getResources().getString(id);
    }

    /**
     * Checks image view to see if the correct image is being displayed.
     *
     * @param imageViewId ImageView resource id
     * @param drawableId  drawable resource id
     */
    @SuppressWarnings("unused")
    protected void testResourceImageView(
            Activity activity, int imageViewId, int drawableId) {
        Drawable testDrawable =
                ContextCompat.getDrawable(activity, drawableId);
        ImageView imageView = (ImageView) activity.findViewById(imageViewId);
        assertNotNull("Unable to access ImageView", imageView);
        onView(withId(imageViewId))
                .check(matches(not(isImageTheSame(testDrawable))));
    }

    /**
     * Convenience method to provide yet another way to test for
     * a displayed Toast message.
     * <p/>
     * NOTE: Doesn't work properly when running multiple tests.
     *
     * @param textId The string resource ID of the displayed text.
     */
    @SuppressWarnings("unused")
    protected void isToastMessageDisplayed(@StringRes int textId) throws
            InterruptedException {
        Thread.sleep(TOAST_SLEEP_DELAY);
        onView(withText(textId))
                .inRoot(isToast())
                .check(matches(isDisplayed()));
    }

    /**
     * Convenience method to provide yet another way to test for
     * a displayed Toast message.
     * <p/>
     * NOTE: Doesn't work properly when running multiple tests.
     *
     * @param text The string the displayed text.
     */
    @SuppressWarnings("unused")
    protected void isToastMessageDisplayed(String text) throws
            InterruptedException {
        Thread.sleep(TOAST_SLEEP_DELAY);
        onView(withText(text))
                .inRoot(isToast())
                .check(matches(isDisplayed()));
    }

    /**
     * Method using a ViewMatcher to extract the text in a TextView.
     */
    protected String getText(Matcher<View> matcher) {
        final String[] stringHolder = {null};

        onView(matcher).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextView.class);
            }

            @Override
            public String getDescription() {
                return "getting text from a TextView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                // Save, because of check in getConstraints()
                TextView tv = (TextView) view;
                stringHolder[0] = tv.getText().toString();
            }
        });

        return stringHolder[0];
    }

    /**
     * Helper that starts an activity using the home category.
     *
     * @param activity Any Activity.
     */
    @SuppressWarnings("unused")
    protected void goHome(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        activity.startActivity(intent);
    }

    /**
     * Helper that brings the specified activity into the foreground.
     *
     * @param activity Any Activity.
     */
    @SuppressWarnings("unused")
    protected void bringToForeground(Activity activity) {
        Intent intent = new Intent(activity, activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }

    /**
     * Helper that returns the resources for the current
     * instrumentation context.
     *
     * @return The resources for the current context.
     */
    protected Resources getResources() {
        return getContext().getResources();
    }

    /**
     * Helper that returns a context for accessing resources.
     *
     * @return The current instrumentation context.
     */
    protected Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    /**
     * Toggles the current orientation
     *
     * @param activity Any activity
     */
    @SuppressWarnings("unused")
    protected void toggleOrientation(Activity activity, int wait) {
        switch (activity.getRequestedOrientation()) {
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                activity.setRequestedOrientation(ActivityInfo
                        .SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                activity.setRequestedOrientation(ActivityInfo
                        .SCREEN_ORIENTATION_PORTRAIT);
                break;
            default:
                assertFalse("Unable to determine current screen orientation",
                        true);
        }

        // Give the system time to settle.
        SystemClock.sleep(wait);
    }

    /*
     * Supporting static classes
     */

    /**
     * Ensures that all permissions are granted if the app is running
     * under the API 23 permission model.
     */
    protected void allowPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 23) {
            // Initialize UiDevice instance
            if (mDevice == null) {
                mDevice = UiDevice.getInstance(InstrumentationRegistry
                        .getInstrumentation());
            }

            UiObject allowPermissions = mDevice.findObject(new UiSelector()
                    .text("Allow"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    // We don't care if the permission dialog does not appear.
                }
            }
        }
    }

    /**
     * Custom ViewAction that clear view image by setting {@link ImageView}s
     * drawable property to null.
     */
    private static final class ClearImageView implements ViewAction {

        @SuppressWarnings("unchecked")
        @Override
        public Matcher<View> getConstraints() {
            return allOf(isDisplayed(), isAssignableFrom(ImageView.class));
        }

        @Override
        public void perform(UiController uiController, View view) {
            ((ImageView) view).setImageBitmap(null);
        }

        @Override
        public String getDescription() {
            return "clear image";
        }
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    protected static class ToastMatcher extends TypeSafeMatcher<Root> {
        @Override
        public void describeTo(Description description) {
            description.appendText("is toast");
        }

        @Override
        public boolean matchesSafely(Root root) {
            int type = root.getWindowLayoutParams().get().type;
            if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
                IBinder windowToken = root.getDecorView().getWindowToken();
                IBinder appToken = root.getDecorView()
                        .getApplicationWindowToken();
                if (windowToken == appToken) {
                    // windowToken == appToken means this window
                    // isn't contained by any other windows.
                    // If it was a window for an activity,
                    // it would have TYPE_BASE_APPLICATION.
                    return true;
                }
            }

            return false;
        }

    }

    public static final class ActivityFinisher implements Runnable {

        public static void finishOpenActivities() {
         //   TaskManager.clearAndReset();
            // TODO: disabled because it is causing activities to be
            // finished out of the main thread (I think...).
            // new Handler(Looper.getMainLooper()).post(new ActivityFinisher());
        }

        private final ActivityLifecycleMonitor activityLifecycleMonitor;

        @SuppressWarnings("unused")
        public ActivityFinisher() {
            this.activityLifecycleMonitor = ActivityLifecycleMonitorRegistry.getInstance();
        }

        @Override
        public void run() {
            final List<Activity> activities = new ArrayList<>();

            for (final Stage stage : EnumSet.range(Stage.CREATED, Stage.STOPPED)) {
                activities.addAll(activityLifecycleMonitor.getActivitiesInStage(stage));
            }

            for (final Activity activity : activities) {
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
        }
    }
}
