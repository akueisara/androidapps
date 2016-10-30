package vandy.mooc.assignments.tools;

import android.util.Log;

import org.junit.Test;

import java.io.File;

import vandy.mooc.assignments.assignment.AssignmentTests;
//TODO: needs to be set for each skeleton
//import vandy.mooc.assignments.assignment2.AssignmentTests;
//import vandy.mooc.assignments.assignment3.AssignmentTests;
//import vandy.mooc.assignments.assignment4.AssignmentTests;

import vandy.mooc.assignments.common.ApplicationTestBase;
import vandy.mooc.assignments.common.DownloadActivityTests;
import vandy.mooc.assignments.common.EspressoTestBase;
import vandy.mooc.assignments.common.MainActivityTests;

/**
 * IGNORE THIS CLASS.
 * <p>
 * This class exists to assist in the auto-grading framework that we have
 * developed.
 * Knowledge of this class and how it works will not be required in this
 * course at all.
 * <p>
 * TODO (MIKE): You will need to set this up properly. There are 3 test files
 * required for auto-grading.
 * 1. EspressoBase (has useful common Espresso helper methods used by all
 * tests).
 * 2. MainActivityTest (MainActivity tests using a local resource URL)
 * 3. DownloadActivityTest (DownloadActivity tests using a local resource URL)
 * <p>
 * The test directory also contains a FullWebApplicationTest file that does not
 * need to be included with the grader because it tests the application using
 * a REMOTE image URL. It exists only for students to run and test their
 * application by downloading a REMOTE image URL and therefore the tests in
 * this file assume the existence of an active internet connection.
 */
public class AutoGrader {
    /**
     * The name used for each of the assignment tests is obtained
     * from a static field in each assignment's AssignmentTest file.
     * TODO: not currently implemented so this hardcoded string must
     * be manually changed in each skeleton.
     */
    private static final String NAME = "Assignment1Tests";

    @Test
    public void mainTest() {
        try {
            AndroidHandinUtil.generateHandinPackage(NAME,
                    new File("./"), EspressoTestBase.class);
            AndroidHandinUtil.generateHandinPackage(NAME,
                    new File("./"), ApplicationTestBase.class);
            AndroidHandinUtil.generateHandinPackage(NAME,
                    new File("./"), DownloadActivityTests.class);
            AndroidHandinUtil.generateHandinPackage(NAME,
                    new File("./"), MainActivityTests.class);
            AndroidHandinUtil.generateHandinPackage(NAME,
                    new File("./"), AssignmentTests.class);

            //*******************************************************
            // FILES ONLY ARE ADDED IN SOLUTION AND SKELETON BRANCHES
            //*******************************************************

        } catch (Exception e) {
            Log.d("AutoGrader :", e.getMessage());
        }
    }
}
