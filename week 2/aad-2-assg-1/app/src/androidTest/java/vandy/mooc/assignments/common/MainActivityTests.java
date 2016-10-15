package vandy.mooc.assignments.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import io.magnum.autograder.junit.Rubric;
import vandy.mooc.assignments.R;
//import vandy.mooc.assignments.activities.DownloadActivity;
//import vandy.mooc.assignments.activities.MainActivity;
//import vandy.mooc.assignments.utils.Utils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.BundleMatchers
        .hasEntry;
import static android.support.test.espresso.intent.matcher.IntentMatchers
        .hasExtras;
import static android.support.test.espresso.intent.matcher.IntentMatchers
        .toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.allOf;

/**
 * This class uses Espresso integrated tests to verify that the assignment
 * MainActivity TODOs have been properly implemented.
 * <p/>
 * NOTE: These tests DO NOT require an internet connection.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTests extends ApplicationTestBase {
    /**
     * Debug logging tag.
     */
    private static final String TAG = "AssignmentTests";

//    /**
//     * Maximum time allowed for each test. Setting this value to
//     * removes time limits.
//     */
//    private static final int TIMEOUT = 0;
//
//    /**
//     * Activity test rule.
//     */
//    @Rule
//    public ActivityTestRule mActivityRule =
//            new ActivityTestRule<>(
//                    MainActivity.class,
//                    true,
//                    true);
//
//    /**
//     * Activity/Context accessor helper.
//     *
//     * @return The currently running activity instance.
//     */
//    private Activity getActivity() {
//        return mActivityRule.getActivity();
//    }
//
//    //-------------------------------------------------------------------------
//
//    /**
//     * Tests starting MainActivity.
//     */
//    @Rubric(
//            value = "test1_testStartMainActivity",
//            goal = "The goal of this evaluation is to test " +
//                    "if MainActivity starts",
//            points = 1.0,
//            reference = "This Test fails when: MainActivity fails to start"
//    )
//    @Test(timeout = TIMEOUT)
//    public void testStartMainActivity() {
//        onView(withId(android.R.id.content));
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
//            value = "testInputUrl",
//            goal = "The goal of this evaluation is to test " +
//                    "if the user is able to enter an URL into the " +
//                    "MainActivity EditView",
//            points = 1.0,
//            reference = "This Test fails when: MainActivity does display " +
//                    "the expected URL entered through the soft keyboard."
//    )
//    @Test(timeout = TIMEOUT)
//    public void testInputUrlAction() {
//        // First clear any restored values.
//        clearAllCachedDataAndViews();
//
//        // Populate URL EditText
//        onView(ViewMatchers.withId(R.id.input_url_edit_text))
//                .perform(typeText(getResourcesUrl(R.drawable.test_image)));
//
//        // Force close the soft keyboard.
//        Espresso.closeSoftKeyboard();
//
//        // Sanity check: make sure the text was entered properly.
//        onView(withId(R.id.input_url_edit_text))
//                .check(matches(withText(getResourcesUrl(R.drawable
//                        .test_image))));
//
//        Log.d(TAG, "Test completed successfully");
//    }
//
//    //-------------------------------------------------------------------------
//
//    /**
//     * Tests for Toast message when Download clicked with no input URL.
//     */
//    @Rubric(
//            value = "testDownloadClickedWithNoInputUrl",
//            goal = "The goal of this evaluation is to test " +
//                    "if the MainActivity displays the expected Toast message " +
//                    "when download FAB is clicked with no Input URL entered.",
//            points = 1.0,
//            reference = "This Test fails when: MainActivity does display " +
//                    "the correct toast download is clicked with no input URL."
//    )
//    @Test(timeout = TIMEOUT)
//    public void testDownloadClickWithNoInputUrl() {
//        // First clear any restored values.
//        clearAllCachedDataAndViews();
//
//        ToastTester toastMatcher = new ToastTester();
//
//        // Clear the EditText view.
//        onView(withId(R.id.input_url_edit_text))
//                .perform(clearText());
//
//        // Force close the soft keyboard.
//        Espresso.closeSoftKeyboard();
//
//        // Click the download FAB.
//        onView(withId(R.id.download_fab))
//                .perform(click());
//
//        toastMatcher.matchToast(R.string.enter_valid_url);
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
//            value = "testDownloadClickWithMalformedUrl",
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
//    public void testDownloadClickWithMalformedUrl() {
//        // First clear any restored values.
//        clearAllCachedDataAndViews();
//
//        ToastTester toastMatcher = new ToastTester();
//
//        // Type in the mal-formed URL.
//        onView(withId(R.id.input_url_edit_text))
//                .perform(typeText(sMalformedUrl));
//
//        // Force close the soft keyboard.
//        Espresso.closeSoftKeyboard();
//
//        // Click the download FAB.
//        onView(withId(R.id.download_fab))
//                .perform(click());
//
//        toastMatcher.matchToast(R.string.invalid_image_url_input);
//
//        Log.d(TAG, "Test completed successfully");
//    }
//
//    //-------------------------------------------------------------------------
//
//    /**
//     * Tests deleting images from the cache.
//     */
//    @Rubric(
//            value = "testDeleteAction",
//            goal = "The goal of this evaluation is to test " +
//                    "if the MainActivity delete FAB click will delete the " +
//                    "expected number of cached images.",
//            points = 1.0,
//            reference = "This Test fails when: MainActivity delete FAB click " +
//                    "does delete the expected number of cached images."
//    )
//    @Test(timeout = TIMEOUT)
//    public void testStartMainActivityMissingUrlResource() {
//        // First clear any restored values.
//        clearAllCachedDataAndViews();
//
//        // Call helper to clear the image cache.
//        clearImageCache();
//
//        // Now create a couple of dummy cached image files.
//        final String cacheDirectory = Utils.getImageDirectory(getActivity());
//
//        // Add the dummy files to the cache folder.
//        final int total = 3;
//
//        for (int i = 1; i < total; i++) {
//            File testFile =
//                    new File(cacheDirectory + "/" + "__test__delete__" + i);
//            try {
//                //noinspection ResultOfMethodCallIgnored
//                testFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//                assertTrue("Unable to create temporary file for delete test",
//                        testFile.isFile());
//            }
//        }
//
//        // Type in the image resource URI.
//        onView(withId(R.id.input_url_edit_text))
//                .perform(typeText(getResourcesUrl(R.drawable.test_image)));
//
//        // Force close the soft keyboard.
//        Espresso.closeSoftKeyboard();
//
//        // Click the download FAB.
//        onView(withId(R.id.download_fab))
//                .perform(click());
//
//        // Check for the local URL in the TextView.
//        onView(withId(R.id.output_url_text_view))
//                .check(matches(withText(containsString(cacheDirectory))));
//
//        // Return to MainActivity.
//        Espresso.pressBack();
//
//        // Setup a toast matcher to test delete toast.
//        ToastTester toastMatcher = new ToastTester();
//
//        // Click the download FAB.
//        onView(withId(R.id.delete_fab))
//                .perform(click());
//
//        // Construct expected download toast message.
//        final String message =
//                getResources().getQuantityString(
//                        R.plurals.images_deleted_message, total, total);
//
//        toastMatcher.matchToast(message);
//
//        Log.d(TAG, "Test completed successfully");
//    }
//
//    //-------------------------------------------------------------------------
//
//    /**
//     * Test if MainActivity sends correct Intent to DownloadActivity
//     */
//    @Rubric(
//            value = "testStartDownloadActivityIntent",
//            goal = "The goal of this evaluation is to test " +
//                    "if MainActivity sends the expected intent to the " +
//                    "DownloadActivity",
//            points = 1.0,
//            reference = "This Test fails when: MainActivity does not send " +
//                    "expected intent to the DownloadActivity"
//    )
//    @Test(timeout = TIMEOUT)
//    public void testStartDownloadActivityIntent() {
//        // First clear any restored values.
//        clearAllCachedDataAndViews();
//
//        // Call helper to test a cancelled result.
//        testOnActivityResult(
//                Activity.RESULT_OK,
//                new Intent());
//
//        Log.d(TAG, "Test completed successfully");
//    }
//
//    //-------------------------------------------------------------------------
//
//    /**
//     * Helper method to test different results returned to onActivityResults.
//     */
//    public void testOnActivityResult(int resultStatus, Intent resultIntent) {
//        // First clear any restored values.
//        clearAllCachedDataAndViews();
//
//        // Use the test url in the app resources.
//        String testUrl = getResourcesUrl(R.drawable.test_image);
//
//        // Type in the image resource URI.
//        onView(withId(R.id.input_url_edit_text))
//                .perform(typeText(testUrl));
//
//        // Force close the soft keyboard.
//        Espresso.closeSoftKeyboard();
//
//        // The expected extra name.
//        final String extraName =
//                DownloadActivity.INTENT_EXTRA_IMAGE_URL_NAME;
//
//        // The expected extra value.
//        final ArrayList<String> extraValue = getTestUrlList(testUrl);
//
//        // Set up the expected intent matcher.
//        @SuppressWarnings("unchecked")
//        final Matcher<Intent> expectedIntent =
//                allOf(hasExtras(allOf(
//                        hasEntry(Matchers.equalTo(extraName),
//                                Matchers.equalTo(extraValue)))),
//                        toPackage(mActivityRule.getActivity().getPackageName
//                                ()));
//
//        // Now create a fake result to return to this test method.
//        final Instrumentation.ActivityResult fakeActivityResult =
//                new Instrumentation.ActivityResult(resultStatus, resultIntent);
//
//        // Setup for Intent interception.
//        Intents.init();
//
//        // Set up intercept for the startActivity(...) using the fake
//        // activity result as response.
//        intending(expectedIntent).respondWith(fakeActivityResult);
//
//        // Now click on show image FAB to invoke the gallery viewer.
//        onView(withId(R.id.download_fab))
//                .perform(click());
//
//        // Check to see if the intent matches our expectations.
//        intended(expectedIntent);
//
//        // Release Intents resources.
//        Intents.release();
//
//        Log.d(TAG, "Test completed successfully");
//    }
//
//    //-------------------------------------------------------------------------
//
//    /**
//     * Tests if MainActivity will success fully start the DownloadActivity
//     * when the download FAB is clicked.
//     */
//    @Rubric(
//            value = "testClickDownloadToStartDownloadActivity",
//            goal = "The goal of this evaluation is to test " +
//                    "if the MainActivity is able to download a valid " +
//                    "image URL resource",
//            points = 1.0,
//            reference = "This Test fails when: MainActivity fails to " +
//                    "download a valid URL image resources"
//    )
//    @Test(timeout = TIMEOUT)
//    public void testClickDownloadToStartDownloadActivity() {
//        // First clear any restored values.
//        clearAllCachedDataAndViews();
//
//        // Type in the mal-formed URL.
//        onView(withId(R.id.input_url_edit_text))
//                .perform(typeText(getResourcesUrl(R.drawable.test_image)));
//
//        // Force close the soft keyboard.
//        Espresso.closeSoftKeyboard();
//
//        // Click the download FAB to start DownloadActivity.
//        onView(withId(R.id.download_fab))
//                .perform(click());
//
//        // Check for the DownloadActivity started.
//        onView(withId(R.id.output_url_text_view));
//
//        Log.d(TAG, "Test completed successfully");
//    }

    //-------------------------------------------------------------------------
}
