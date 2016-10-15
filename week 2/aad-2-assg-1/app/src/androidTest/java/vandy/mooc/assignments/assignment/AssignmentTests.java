package vandy.mooc.assignments.assignment;

import android.app.Activity;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.internal.ForegroundLinearLayout;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.MediumTest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import io.magnum.autograder.junit.Rubric;
import vandy.mooc.assignments.R;
import vandy.mooc.assignments.assignment.activities.MainActivity;
import vandy.mooc.assignments.common.ApplicationTestBase;


import vandy.mooc.assignments.assignment.activities.GalleryActivity;
import vandy.mooc.assignments.assignment.activities.MainActivity;
import vandy.mooc.assignments.common.ApplicationTestBase;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;


import android.support.test.InstrumentationRegistry;
import android.util.Xml;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.TrustAnchor;
import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
//import static android.support.test.espresso.matcher.
import static junit.framework.Assert.assertFalse;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import static android.support.test.espresso.assertion.ViewAssertions.matches;

/**
 * This class uses Espresso integrated tests to verify that the assignment
 * DownloadActivity TODOs have been properly implemented.
 * <p/>
 * NOTE: These tests DO NOT require an internet connection.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class AssignmentTests extends ApplicationTestBase {
    /**
     * Debug logging tag.
     */
    private static final String TAG = "AssignmentTests";

    /**
     * Sets the runner to test only assignment 1 code.
     */
    private static final int ASSIGNMENT = 3;

    /**
     * Maximum time allowed for each test. Setting this value to
     * removes time limits.
     */
    private static final int TIMEOUT = 0;

    /**
     * MainActivity test rule.
     */
    @Rule
    public ActivityTestRule mMainActivityRule =
            new ActivityTestRule<>(
                    MainActivity.class,
                    true,
                    false);

    /**
     * DownloadActivity test rule.
     */
    @Rule
    public ActivityTestRule mDownloadActivityRule =
            new ActivityTestRule<>(
                    GalleryActivity.class,
                    true,
                    false);

    /**
     * Force the assignment to run code specific to this test for this
     * assignment which is defined in the ASSIGNMENT field.
     */
    @Before
    public void setup() {
        setAssignmentRunner(ASSIGNMENT);
    }

    /**
     * Broadcast intent sent by DownloadActivity and received by mock
     * broadcast receiver.
     */
    private Intent mBroadcastIntent = null;



    /**
     * Activity test rule.
     */
    @Rule
    public ActivityTestRule mActivityRule =
            new ActivityTestRule<>(
                    MainActivity.class,
                    true,
                    true);


    @Rule
    public ActivityTestRule mGallaryActivityRule =
            new ActivityTestRule<>(
                    GalleryActivity.class,
                    true,
                    true);

    /**
     * Activity/Context accessor helper.
     *
     * @return The currently running activity instance.
     */
    private Activity getActivity() {
        return mActivityRule.getActivity();
    }

    /**
     * Full application test that starts the application and attempts to
     * download a mal-formed URL.
     * <p/>
     * NOTE: THIS TEST REQUIRES AN ACTIVE INTERNET CONNECTION.
     */
    @Rubric(
            value = "fullAppDownloadAMissingUrlResourceTest",
            goal = "The goal of this evaluation is to test " +
                    "if the application responds with the correct Toast when " +
                    "the user attempts to download a missing URL resource",
            points = 1.0,
            reference = "This Test fails when: DownloadActivity fail to show " +
                    "the expected Toast message when user attempts to " +
                    "download a malformed image URL."
    )
    @Test(timeout = TIMEOUT)
    public void MyTestTest() {


        // Ensure that permissions are acquired.
        allowPermissionsIfNeeded();
        // Clear all cached and restored data.
        clearAllCachedDataAndViews();
        // Open the overflow menu OR open the options menu,
        // depending on if the device has a hardware or software overflow menu button.
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation()
                .getTargetContext());
        // delay 50ms to prevent Emulator lag causing test to fail.
        SystemClock.sleep(50);


        try {
            onView(withText("Select All")).perform(click());
            // delay 50ms to prevent Emulator lag causing test to fail.
            SystemClock.sleep(50);
            onView(withId(R.id.action_delete)).perform(click());
            // delay 50ms to prevent Emulator lag causing test to fail.
            SystemClock.sleep(50);
            openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation()
                    .getTargetContext());
            // delay 50ms to prevent Emulator lag causing test to fail.
            SystemClock.sleep(50);
        } catch (NoMatchingViewException e) {
            //view not displayed logic
        }

        // Click the item.
        onView(withText("Load Defaults")).perform(click());
        // delay 50ms to prevent Emulator lag causing test to fail.
        SystemClock.sleep(50);
        // click the FAB to start the download process.
        onView(withId(R.id.download_fab)).perform(click());


        /**
         * This code is somewhat needlessly complex, but it saves you time running the tests.
         *
         * It sees if the URLs have been downloaded yet every 2 seconds 5 times.
         * It only throws an error if the last check fails. This cuts down on clock-time needed
         * for test if emulator/internet are of sufficient speed.
         */
        for (int i = 0 ; i < 5 ; i=i+2){
            // Sleep 2 seconds to between attempts.
            SystemClock.sleep(2000);
            if (i < 4){
                // first 4 times check URLs but don't fail if they aren't yet downloaded.
                try {
                    if (checkDefaultURLs()){
                        break;
                    }
                }catch (Exception Ex) {
                    // Is alright if this happens first 4 times
                }
            }else
            {
                // Last time check URLs and fail if they aren't yet downloaded.
                checkDefaultURLs();
            }
        }
    }

    /**
     * This is a list of the default URLs to be downloaded.
     */
    private final static String[] defaultURLs = {
        "http://www.hireworks.tv/wp-content/gallery/bout_us/gameofthrones.jpg",
        "http://colleges.usnews.rankingsandreviews.com/img/college-photo_313._445x280-zmm.jpg",
        "https://upload.wikimedia.org/wikipedia/en/0/09/DataTNG.jpg",
        "http://www.dre.vanderbilt.edu/~schmidt/gifs/dougs-xsmall.jpg",
        "http://2.bp.blogspot.com/-c2U3HUQZVy8/UV7KI2bodLI/AAAAAAAAA4g/DJEEmv-FmNY/s1600" +
            "/galaxy_universe-normal.jpg",
        "http://acidcow.com/pics/20110920/famous_actors_who_got_hit_with_the_ugly_stick_19.jpg",
    };

    /**
     * Helper method to check to see if all the default URLs are present in the GallaryActivity.
     * @return true if tests didn't fail.
     */
    public static boolean checkDefaultURLs(){
        for (String urlToCheck :
                defaultURLs) {
            onView(withId(R.id.recycler_view_fragment))
                    .check(matches(hasDescendant(withContentDescription(urlToCheck))));
        }
        return true;
    }

}