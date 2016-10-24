package vandy.mooc.assignments.common;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import java.util.ArrayList;

import vandy.mooc.assignments.R;
import vandy.mooc.assignments.assignment.activities.GalleryActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

/**
 * Base class used by all MOOC2 assignment tests classes. This class
 * contains a collection of useful common application helper methods in
 * order to avoid code duplication.
 */
public class ApplicationTestBase extends EspressoTestBase {
    /**
     * Debug logging tag.
     */
    private static final String TAG = "ApplicationTestBase";

    /*
     * DEFAULT TESTS DATA
     */

    /**
     * Default timeout used when looking for a toast message.
     */
    private static final int TOAST_SLEEP_TIMEOUT = 1000;

    /**
     * The interval used to determine how often to check for a toast.
     */
    private static final int TOAST_POLL_INTERVAL = 100;

    /**
     * A remote URL to test. This is only used by the FullWebApplicationTest
     * based classes and is never used in the derived AssignmentTest classes
     * that are used for auto-grading.
     */
    protected static final String sRemoteUrl =
            "http://acidcow.com/pics/20110920/" +
                    "famous_actors_who_got_hit_with_the_ugly_stick_19.jpg";

    /**
     * An invalid URL to test.
     */
    protected static final String sMalformedUrl =
            "foo://bar.com/foobar.jpg";

    /**
     * An invalid URL to test.
     */
    protected static final String sMissingUrl =
            "http://nowhere.com/noimage.jpg";

    /*
     * ASSIGNMENT RELATED HELPER METHODS (reference application objects).
     */

    /**
     * Force the assignment to run tests for the specified ASSIGNMENT number.
     * This method should be called before any tests are run for an
     * assignment (from the @Before handler).
     */
    protected void setAssignmentRunner(int assignment) {
        // TODO: MIKE See if need this now
//        AssignmentUtils.setAssignment(
//                InstrumentationRegistry.getTargetContext(), assignment);
//
//        int result = AssignmentUtils.getAssignment(
//                InstrumentationRegistry.getTargetContext());
//
//        assertEquals("!!!SEVERE ERROR!!! Unable to set Assignment Runner " +
//                "to run Assignment " + assignment, assignment, result);

        Log.i(TAG, "=========== RUNNING ASSIGNMENT "
                + assignment + " TESTS ===========");
    }

    /**
     * Helper that ensures that any restored state is cleared
     * before each test. It is designed to be used for both
     * MainActivity and DownloadActivity layout views and has
     * try/catch blocks to quietly handle mismatched views.
     */
    protected void clearAllCachedDataAndViews() {
//        try {
//            // Clear the EditText view.
//            onView(withId(R.id.input_url_edit_text))
//                    .perform(clearText());
//        } catch (Exception e) {
//            // We don't care if this fails.
//        }
//
//        try {
//            // Clear the DownloadActivities text view.
//            onView(withId(R.id.output_url_text_view))
//                    .perform(clearText());
//        } catch (Exception e) {
//            // We don't care if this fails.
//        }

        try {
            // Clear the ImageView view (may not exist for some
            // assignments).
            onView(withId(R.id.image_view))
                    .perform(clearImageView());
        } catch (Exception e) {
            // We don't care if this fails.
        }

        try {
            // Clear the image cache.
            clearImageCache();
        } catch (Exception e) {
            // We don't care if this fails.
        }
    }

    /**
     * Helper used to silently clear the image cache.
     */
    protected void clearImageCache() {
        // First silently clear any existing cached images.

        // TODO: MIKE, FIX THIS
//        try {
//            Utils.deleteDirectory(Utils.getImageDirectory(getContext()));
//        } catch (Exception ignored) {
//        }
    }

    /**
     * Helper to make a mock DownloadActivity intent to allow testing
     * this DownloadActivity activity without starting the MainActivity.
     *
     * @param urls A List of application resource ids (or null).
     * @return A mock Intent containing a URLs list as an extra.
     */
    protected Intent makeMockIntent(String... urls) {
        Intent intent = new Intent();

        intent.putStringArrayListExtra(
                GalleryActivity.INTENT_EXTRA_URLS,
                getTestUrlList(urls));

        return intent;
    }

    /**
     * Helper to make a mock DownloadActivity intent to allow testing
     * this DownloadActivity activity without starting the MainActivity.
     *
     * @param urls A List of application resource ids (or null).
     * @return A mock Intent containing a URLs list as an extra.
     */
    protected Intent makeMockIntent(Integer... urls) {
        Intent intent = new Intent();

        intent.putStringArrayListExtra(
                GalleryActivity.INTENT_EXTRA_URLS,
//                DownloadActivity.INTENT_EXTRA_IMAGE_URL_NAME,
                getTestUrlList(urls));

        return intent;
    }

    /**
     * Helper method that creates a test URL list which can be
     * null, empty, or filled.
     *
     * @param urls list of URL strings (or null).
     * @return A list with one resource URL.
     */
    protected ArrayList<String> getTestUrlList(String... urls) {
        ArrayList<String> urlList = null;

        // This if ensures that we return a null list if the
        // passed list is null (contains no items).
        if (urls.length > 0 && !(urls.length == 1 && urls[0] == null)) {
            urlList = new ArrayList<>();

            for (String url : urls) {
                if (!url.isEmpty())
                    urlList.add(url);
            }
        }

        return urlList;
    }

    /**
     * Helper method that creates a test URL list which can be
     * null, empty, or filled.
     *
     * @param urls list of integer resource ids (or null).
     * @return A list with one resource URL.
     */
    protected ArrayList<String> getTestUrlList(Integer... urls) {
        ArrayList<String> urlList = null;

        // This if ensures that we return a null list if the
        // passed list is null (contains no items).
        if (urls.length > 0 && !(urls.length == 1 && urls[0] == null)) {
            urlList = new ArrayList<>();

            for (Integer resId : urls) {
                urlList.add(getResourcesUrl(resId));
            }
        }

        return urlList;
    }

    /**
     * Constructs a quantity string toast to match the expected download
     * message.
     *
     * @param expectedCount Expected number of downloads
     * @param downloadCount Actual number of downloads.
     * @return The expected toast message.
     */
    protected String getExpectedDownloadToastMessage(
            int expectedCount, int downloadCount) {

        String message;
        if (expectedCount == 1) {
            // Use the string designed for downloading a single image.
            message = getResources().getQuantityString(
                    R.plurals.single_image_download_message,
                    downloadCount);
        } else {
            // Use the string designed for downloading a multiple images.
            message = getResources().getQuantityString(
                    R.plurals.multiple_images_download_message,
                    downloadCount,
                    downloadCount,
                    expectedCount);
        }

        return message;
    }

    /**
     * Supports a static string in Utils to match the last message passed
     * to Util#showToeat().
     */

//    @SuppressWarnings("WeakerAccess")
//    static protected class ToastTester {
//
//        public ToastTester() {
//            Utils.clearLastToast();
//        }
//
//        public void matchToast(String msg, int timeout) {
//            try {
//                for (int i = 0; i < timeout; i += TOAST_POLL_INTERVAL) {
//                    if (Utils.getLastToast() != null &&
//                            !Utils.getLastToast().isEmpty()) {
//                        break;
//                    }
//                    Thread.sleep(TOAST_POLL_INTERVAL);
//                }
//            } catch (InterruptedException e) {
//                assertFalse("Expected toast " + msg + "was not displayed",
//                        true);
//            }
//
//            assertEquals("Expected Toast string to match",
//                    msg, Utils.getLastToast());
//        }
//
//        public void matchToast(int matchResId, int timeout) {
//            String matchString =
//                    InstrumentationRegistry.getTargetContext()
//                            .getString(matchResId);
//
//            matchToast(matchString, timeout);
//        }
//
//        /**
//         * Check for a matching toast message using the default timeout value.
//         * @param msg String to match.
//         */
//        public void matchToast(String msg) {
//            matchToast(msg, TOAST_SLEEP_TIMEOUT);
//        }
//
//        /**
//         * Check for a matching toast message using the default timeout value.
//         * @param matchResId String resource to match.
//         */
//        public void matchToast(int matchResId) {
//            matchToast(matchResId, TOAST_SLEEP_TIMEOUT);
//        }
//    }
}

