package vandy.mooc.assignments.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import io.magnum.autograder.junit.Rubric;
import vandy.mooc.assignments.R;
import vandy.mooc.assignments.assignment.activities.GalleryActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.core.StringStartsWith.startsWith;

/**
 * This class uses Espresso integrated tests to verify that the assignment
 * DownloadActivity TODOs have been properly implemented.
 * <p>
 * NOTE: These tests DO NOT require an internet connection.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DownloadActivityTests extends ApplicationTestBase {
    /**
     * Debug logging tag.
     */
    private static final String TAG = "AssignmentTests";

    /**
     * Maximum time allowed for each test. Setting this value to
     * removes time limits.
     */
    private static final int TIMEOUT = 0;

    /**
     * Activity test rule.
     */
    @Rule
    public ActivityTestRule mActivityRule =
            new ActivityTestRule<>(
                    // TODO: MIKE, see what activity goes here now, if anything
                    GalleryActivity.class,
                    true,
                    false);

    @Before
    public void setup() {
//        int assignment = AssignmentUtils.getAssignment(
//                InstrumentationRegistry.getTargetContext());
//        Log.i(TAG, "Running test for assignment " + assignment);
    }

    /**
     * Activity/Context accessor helper.
     *
     * @return The currently running activity instance.
     */
    private Activity getActivity() {
        return mActivityRule.getActivity();
    }

    //-------------------------------------------------------------------------

//    /**
//     * Tests starting DownloadActivity.
//     */
//    @Rubric(
//            value = "testStartDownloadActivity",
//            goal = "The goal of this evaluation is to test " +
//                    "if the DownloadActivity starts",
//            points = 1.0,
//            reference = "This Test fails when: DownloadActivity fails to start"
//    )
//    @Test(timeout = TIMEOUT)
//    public void testStartDownloadActivity() {
//        Log.d(TAG, "starting testStartDownloadActivity()");
//
//        mActivityRule.launchActivity(makeMockIntent(R.drawable.test_image));
//        onView(withId(android.R.id.content));
//
//        // Make sure the activity is completely finished.
//        ActivityFinisher.finishOpenActivities();
//
//        Log.d(TAG, "Test completed successfully");
//    }
//
//    //-------------------------------------------------------------------------
//
//    /**
//     * Tests null url list intent extra.
//     */
//    @Rubric(
//            value = "testStartDownloadActivityNullUrlList",
//            goal = "The goal of this evaluation is to test " +
//                    "if the DownloadActivity detects a null URL list in the " +
//                    "received intent",
//            points = 1.0,
//            reference = "This Test fails when: DownloadActivity does display " +
//                    "the correct toast when a null intent is received."
//    )
//    @Test(timeout = TIMEOUT)
//    public void testStartDownloadActivityNullUrlList() {
//        Log.d(TAG, "starting testStartDownloadActivityNullUrlList()");
//        ToastTester toastMatcher = new ToastTester();
//
//        // Launch the activity.
//        mActivityRule.launchActivity(makeMockIntent((String) null));
//        onView(withId(android.R.id.content));
//
//        toastMatcher.matchToast(R.string.input_url_list_is_null);
//
//        // Make sure the activity is completely finished.
//        ActivityFinisher.finishOpenActivities();
//
//        Log.d(TAG, "Test completed successfully");
//    }
//
//    //-------------------------------------------------------------------------
//
//    /**
//     * Tests empty url list intent extra.
//     */
//    @Rubric(
//            value = "testStartDownloadActivityEmptyUrlList",
//            goal = "The goal of this evaluation is to test " +
//                    "if the DownloadActivity detects an empty URL list " +
//                    "in the received intent",
//            points = 1.0,
//            reference = "This Test fails when: DownloadActivity does display " +
//                    "the correct toast when an empty intent is received."
//    )
//    @Test(timeout = TIMEOUT)
//    public void testStartDownloadActivityEmptyUrlList() {
//        Log.d(TAG, "starting testStartDownloadActivityEmptyUrlList()");
//        ToastTester toastMatcher = new ToastTester();
//
//        // Launch the activity.
//        mActivityRule.launchActivity(makeMockIntent(""));
//        onView(withId(android.R.id.content));
//
//        toastMatcher.matchToast(R.string.input_url_list_is_empty);
//
//        // Make sure the activity is completely finished.
//        ActivityFinisher.finishOpenActivities();
//
//        Log.d(TAG, "Test completed successfully");
//    }
//
//    //-------------------------------------------------------------------------
//
//    /**
//     * Test for toast when URL input is malformed.
//     */
//    @Rubric(
//            value = "testStartDownloadActivityWithMalformedUrl",
//            goal = "The goal of this evaluation is to test " +
//                    "if MainActivity displays the appropriate Toast error " +
//                    "message when download is clicked when the input URL is " +
//                    "malformed.",
//            points = 1.0,
//            reference = "This Test fails when: the expected Toast error is " +
//                    "not displayed when download is clicked and there input " +
//                    "URL is malformed."
//    )
//    @Test(timeout = TIMEOUT)
//    public void testStartDownloadActivityWithMalformedUrl() {
//        Log.d(TAG, "starting testStartDownloadActivityWithMalformedUrl()");
//        ToastTester toastMatcher = new ToastTester();
//
//        // Make a list of 2 URLs with the first being valid and the 2nd being
//        // malformed.
//        mActivityRule.launchActivity(
//                makeMockIntent(getResourcesUrl(R.drawable.test_image),
//                        sMalformedUrl));
//        onView(withId(android.R.id.content));
//
//        toastMatcher.matchToast(R.string.invalid_image_url_input);
//
//        // Make sure the activity is completely finished.
//        ActivityFinisher.finishOpenActivities();
//
//        Log.d(TAG, "Test completed successfully");
//    }
//
//    //-------------------------------------------------------------------------
//
//    /**
//     * Tests error handling for a missing URL resource.
//     */
//    @Rubric(
//            value = "testStartDownloadActivityMissingUrlResource",
//            goal = "The goal of this evaluation is to test " +
//                    "if the DownloadActivity displays the expected Toast " +
//                    "message when attempting to download a missing URL image " +
//                    "resource.",
//            points = 1.0,
//            reference = "This Test fails when: DownloadActivity does display " +
//                    "the correct toast after attempting to download a missing" +
//                    " URL image resource."
//    )
//    @Test(timeout = TIMEOUT)
//    public void testStartDownloadActivityMissingUrlResource() {
//        Log.d(TAG, "starting testStartDownloadActivityMissingUrlResource()");
//        // Create a toast matcher to check expected toast.
//        ToastTester toastMatcher = new ToastTester();
//
//        // Construct expected download toast message.
//        final String message = getExpectedDownloadToastMessage(1, 0);
//
//        // Start the activity.
//        mActivityRule.launchActivity(makeMockIntent(sMissingUrl));
//        onView(withId(android.R.id.content));
//
//        toastMatcher.matchToast(message);
//
//        // Make sure the activity is completely finished.
//        ActivityFinisher.finishOpenActivities();
//
//        Log.d(TAG, "Test completed successfully");
//    }
//
//    //-------------------------------------------------------------------------
//
//    /**
//     * Tests download image URL succeeded
//     */
//    @Rubric(
//            value = "testDownloadOperationWithValidImageResource",
//            goal = "The goal of this evaluation is to test " +
//                    "if the DownloadActivity is able to download a valid " +
//                    "image URL resource",
//            points = 1.0,
//            reference = "This Test fails when: DownloadActivity fails to " +
//                    "download a valid URL image resources"
//    )
//    @Test(timeout = TIMEOUT)
//    public void testDownloadOperationWithValidImageResource() {
//        Log.d(TAG, "starting testDownloadOperationWithValidImageResource()");
//        // Create a toast matcher to check expected toast.
//        ToastTester toastMatcher = new ToastTester();
//
//        // The DownloadActivity automatically starts the image download
//        // in onCreate, so we just need to check if the download succeeded
//        // by looking for a local URL in the EditText view.
//        final String imageDirRootUrl =
//                "file://" + Utils.getImageDirectory(getActivity());
//
//        // Construct expected download toast message.
//        final String message = getExpectedDownloadToastMessage(1, 1);
//
//        // Launch the activity.
//        mActivityRule.launchActivity(makeMockIntent(R.drawable.test_image));
//        onView(withId(android.R.id.content));
//
//        // This check ensures the TextView contains the local URL of a
//        // downloaded image. Since the actual image file name is a randomly
//        // generated GUI id, we only check to see that the parent directory
//        // path matches the expected one generated by the Utils helper method.
//        onView(withId(R.id.output_url_text_view))
//                .check(matches(withText(startsWith(imageDirRootUrl))));
//
//        toastMatcher.matchToast(message);
//
//        // Make sure the activity is completely finished.
//        ActivityFinisher.finishOpenActivities();
//
//        Log.d(TAG, "Test completed successfully");
//
//    }
//
//    //-------------------------------------------------------------------------
//
//    /**
//     * Test download operation with mixed valid and missing image resources.
//     */
//    @Rubric(
//            value = "testDownloadOperationWithMixedValidAndMissingImageResources",
//            goal = "The goal of this evaluation is to test " +
//                    "if MainActivity displays the appropriate Toast error " +
//                    "message the download operation is only able to download " +
//                    "a subset of images in the received image URL list.",
//            points = 1.0,
//            reference = "This Test fails when: the expected Toast error is " +
//                    "not displayed when download is clicked and there input " +
//                    "URL is malformed."
//    )
//    @Test(timeout = TIMEOUT)
//    public void testDownloadOperationWithMixedValidAndMissingImageResources() {
//        Log.d(TAG, "starting " +
//                "testDownloadOperationWithMixedValidAndMissingImageResources" +
//                "()");
//
//        ToastTester toastMatcher = new ToastTester();
//
//        // Call helper to construct the expected download toast message string.
//        String expectedMessage = getExpectedDownloadToastMessage(20, 10);
//
//        // Make a list of 2 URLs with the first being valid and the 2nd being
//        // malformed.
//        mActivityRule.launchActivity(
//                makeMockIntent(
//                        getResourcesUrl(R.drawable.test_image),
//                        sMissingUrl,
//                        getResourcesUrl(R.drawable.test_image),
//                        sMissingUrl,
//                        getResourcesUrl(R.drawable.test_image),
//                        sMissingUrl,
//                        getResourcesUrl(R.drawable.test_image),
//                        sMissingUrl,
//                        getResourcesUrl(R.drawable.test_image),
//                        sMissingUrl,
//                        getResourcesUrl(R.drawable.test_image),
//                        sMissingUrl,
//                        getResourcesUrl(R.drawable.test_image),
//                        sMissingUrl,
//                        getResourcesUrl(R.drawable.test_image),
//                        sMissingUrl,
//                        getResourcesUrl(R.drawable.test_image),
//                        sMissingUrl,
//                        getResourcesUrl(R.drawable.test_image),
//                        sMissingUrl
//                ));
//        onView(withId(android.R.id.content));
//
//        // Make sure that the valid URL image was downloaded before checking
//        // the toast to avoid timing issues.
//        final String imageDirRootUrl =
//                "file://" + Utils.getImageDirectory(getActivity());
//        onView(withId(R.id.output_url_text_view))
//                .check(matches(withText(startsWith(imageDirRootUrl))));
//
//        // Now that the download of the valid image URL has completed,
//        // check the toast for the expected message.
//        toastMatcher.matchToast(expectedMessage);
//
//        // Make sure the activity is completely finished.
//        ActivityFinisher.finishOpenActivities();
//
//        Log.d(TAG, "Test completed successfully");
//    }
//
//    //-------------------------------------------------------------------------
//
//    /**
//     * Tests if downloaded image URL is displayed in the layout TextView widget.
//     */
//    @Rubric(
//            value = "testDownloadOperationShowsLocalImageUrlInTextView",
//            goal = "The goal of this evaluation is to test " +
//                    "if the DownloadActivity shows the downloaded image URL " +
//                    "in the layout TextView widget",
//            points = 1.0,
//            reference = "This Test fails when: DownloadActivity fails to " +
//                    "display the downloaded image URL in the TextView widget"
//    )
//    @Test(timeout = TIMEOUT)
//    public void testDownloadOperationShowsLocalImageUrlInTextView() {
//        Log.d(TAG, "starting " +
//                "testDownloadOperationShowsLocalImageUrlInTextView()");
//        // Launch the activity.
//        mActivityRule.launchActivity(makeMockIntent(R.drawable.test_image));
//        onView(withId(android.R.id.content));
//
//        // The DownloadActivity automatically starts the image download
//        // in onCreate, so we just need to check if the download succeeded
//        // by looking for a local URL in the EditText view.
//        final String imageDirRootUrl =
//                "file://" + Utils.getImageDirectory(getActivity());
//
//        // This check ensures the TextView contains the local URL of a
//        // downloaded image. Since the actual image file name is a randomly
//        // generated GUI id, we only check to see that the parent directory
//        // path matches the expected one generated by the Utils helper method.
//        onView(withId(R.id.output_url_text_view))
//                .check(matches(withText(startsWith(imageDirRootUrl))));
//
//        // Make sure the activity is completely finished.
//        ActivityFinisher.finishOpenActivities();
//
//        Log.d(TAG, "Test completed successfully");
//    }
//
//    //-------------------------------------------------------------------------
//
//    /**
//     * Tests DownloadActivity static makeIntent method.
//     */
//    @Rubric(
//            value = "testDownloadActivityMakeIntentMethod",
//            goal = "The goal of this evaluation is to test " +
//                    "DownloadActivity#makeIntent",
//            points = 1.0,
//            reference = "This Test fails when: DownloadActivity#makeIntent " +
//                    "does not create the expected Intent"
//    )
//    @Test(timeout = TIMEOUT)
//    public void testMakeIntentMethod() {
//        Log.d(TAG, "starting testMakeIntentMethod()");
//        ArrayList<String> inputUrls = getTestUrlList(R.drawable.test_image);
//
//        Context context = InstrumentationRegistry.getTargetContext();
//
//        Intent intent =
//                DownloadActivity.makeStartIntent(context, inputUrls);
//
//        assertNotNull("makeIntent returned a null intent.", intent);
//
//        ArrayList<String> outputUrls =
//                intent.getStringArrayListExtra(
//                        DownloadActivity.INTENT_EXTRA_IMAGE_URL_NAME);
//
//        assertNotNull("The URL list in the intent extra is null", outputUrls);
//
//        assertEquals("The URL list in the intent extra should contain exactly" +
//                        " 1 image URL entry.",
//                inputUrls.size(), outputUrls.size());
//
//        assertEquals("The URL list stored as an intent extra does not exactly" +
//                        " match the passed URL list.", inputUrls,
//                outputUrls);
//    }

    //-------------------------------------------------------------------------
}
